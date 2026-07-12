package Product_Management.Service;

import Product_Management.DTO.OrderRequest;
import Product_Management.Model.Customer;
import Product_Management.Model.Order;
import Product_Management.Model.OrderItem;
import Product_Management.Model.Product;
import Product_Management.Repository.CustomerRepository;
import Product_Management.Repository.OrderRepository;
import Product_Management.Repository.ProductRepository;
import Product_Management.event.LowStockEvent;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {
    @Autowired private OrderRepository orderRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private InvoiceProcessingService invoiceService;
    @Autowired private ApplicationEventPublisher eventPublisher;

    private static final int LOW_STOCK_THRESHOLD=5;

    @Transactional
    public Order placeOrder(OrderRequest request) {
        // 1. Fetch and validate Customer
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PROCESSING");
        order.setCustomer(customer);

        double totalAmount = 0;
        List<OrderItem> orderItems = new ArrayList<>();

        // 2. Loop through requested items and calculate transactional pricing
        for (OrderRequest.ItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + itemReq.getProductId()));

            if (product.getStockQuantity() < itemReq.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName()
                        + ". Available: " + product.getStockQuantity() + ", Requested: " + itemReq.getQuantity());
            }

            product.setStockQuantity(product.getStockQuantity() - itemReq.getQuantity());
            productRepository.save(product);

            if (product.getStockQuantity() <= LOW_STOCK_THRESHOLD) {
                eventPublisher.publishEvent(new LowStockEvent(product.getId(), product.getName(), product.getStockQuantity()));
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemReq.getQuantity());
            // Lock down the price at historical checkout time (Production best-practice!)
            orderItem.setPriceAtPurchase(product.getPrice());

            totalAmount += product.getPrice() * itemReq.getQuantity();
            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);

        // 3. Persist transaction to PostgreSQL
        Order savedOrder = orderRepository.save(order);

        // 4. Fire-and-Forget Asynchronous Task! (Doesn't block web thread)
        invoiceService.generateAndEmailInvoice(savedOrder.getOrderNumber());

        return savedOrder;
    }
}

package Product_Management.example.API.Service;

import Product_Management.example.API.DTO.OrderRequest;
import Product_Management.example.API.Model.Customer;
import Product_Management.example.API.Model.Order;
import Product_Management.example.API.Model.OrderItem;
import Product_Management.example.API.Model.Product;
import Product_Management.example.API.Repository.CustomerRepository;
import Product_Management.example.API.Repository.OrderRepository;
import Product_Management.example.API.Repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Transactional // 👈 CRITICAL: Ensures atomicity. If any line fails, database rolls back.
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

package Product_Management.Controller;

import Product_Management.DTO.OrderRequest;
import Product_Management.Model.Order;
import Product_Management.Service.OrderService;
import Product_Management.Repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    @Autowired private OrderService orderService;
    @Autowired private OrderRepository orderRepository;

    @PostMapping("/checkout")
    public Order checkout(@RequestBody OrderRequest request){
        return orderService.placeOrder(request);
    }

    @GetMapping
    public List<Order> getAllOrders(){
        return orderRepository.findAll();
    }
}

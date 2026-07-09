package Product_Management.example.API.Controller;

import Product_Management.example.API.DTO.OrderRequest;
import Product_Management.example.API.Model.Order;
import Product_Management.example.API.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    @Autowired private OrderService orderService;

    @PostMapping("/checkout")
    public Order checkout(@RequestBody OrderRequest request){
        return orderService.placeOrder(request);
    }
}

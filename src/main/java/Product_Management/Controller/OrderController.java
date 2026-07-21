package Product_Management.Controller;

import Product_Management.DTO.OrderRequest;
import Product_Management.Model.Order;
import Product_Management.Service.OrderService;
import Product_Management.Repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    @Autowired private OrderService orderService;
    @Autowired private OrderRepository orderRepository;

    private final List<SseEmitter> emitters=new CopyOnWriteArrayList<>();

    @PostMapping("/checkout")
    public Order checkout(@RequestBody OrderRequest request){
        // 1. Execute transactional logic to deduct stock and create order
        Order savedOrder = orderService.placeOrder(request);

        // 2. 🔴 CRITICAL FIX: Re-fetch the newly created order using our custom JOIN FETCH query
        // This guarantees the customer relationship details are attached before JSON serialization!
        Order fullyPopulatedOrder = orderRepository.findById(savedOrder.getId())
                .orElse(savedOrder);

        // 3. Broadcast the deep relational data to all active dashboards instantly!
        synchronized (this.emitters) {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(fullyPopulatedOrder, MediaType.APPLICATION_JSON);
                } catch (IOException e) {
                    emitters.remove(emitter);
                }
            }
        }
        return fullyPopulatedOrder;
    }


    @GetMapping
    public List<Order> getAllOrders(){
        return orderRepository.findAll();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamOrders(){
        SseEmitter emitter=new SseEmitter(60*1000L);
        this.emitters.add(emitter);

        emitter.onCompletion(() -> this.emitters.remove(emitter));
        emitter.onTimeout(() -> this.emitters.remove(emitter)) ;
        emitter.onError((e) -> this.emitters.remove(emitter));

        return emitter;
    }

    // Inside OrderController.java

    @PatchMapping("/{id}/status")
    public Order updateOrderStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> payload) {
        String newStatus = payload.get("status");

        // 1. Fetch the order eagerly using the custom join fetch we set up earlier
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));

        // 2. Update the status field
        order.setStatus(newStatus.toUpperCase());
        Order updatedOrder = orderRepository.save(order);

        // 3. 🔴 BROADCAST UPDATE: Inform all dashboards about the status change instantly!
        synchronized (this.emitters) {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(updatedOrder, MediaType.APPLICATION_JSON);
                } catch (IOException e) {
                    emitters.remove(emitter);
                }
            }
        }

        return updatedOrder;
    }
}

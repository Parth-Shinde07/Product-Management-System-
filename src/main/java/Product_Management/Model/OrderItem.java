package Product_Management.Model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;

@Entity
@Data
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int quantity;
    private double priceAtPurchase;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonBackReference // 👈 This stops the loop back to Order!
    private Order order;

    @ManyToOne
    @JoinColumn(name="product_id",nullable = false)
    private Product product;
}

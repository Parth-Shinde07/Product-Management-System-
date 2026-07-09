package Product_Management.example.API.Model;

import jakarta.persistence.*;
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
    @JoinColumn(name="product_id",nullable = false)
    private Product product;
}

package Product_Management.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name="orders")

public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;
    private LocalDateTime orderDate;
    private double totalAmount;
    private String status;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="customer_id" , nullable = false)
    @JsonManagedReference
    private Customer customer;

    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    @JoinColumn(name="order_id")
    @JsonManagedReference
    private List<OrderItem> items;
}

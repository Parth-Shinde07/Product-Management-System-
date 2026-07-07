package Product_Management.example.API.Model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data // generates getters,setters & tostring via lombook
@Table(name="products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private double price;
}

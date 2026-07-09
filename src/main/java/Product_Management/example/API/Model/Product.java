package Product_Management.example.API.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Data // generates getters,setters & tostring via lombook
@Table(name="products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name cannot be blank")
    @Size(min=2,max=50,message = "Name must be between 2 and 50 characters")
    private String name;

    @Min(value = 0, message = "Price cannot be negative")
    private double price;
}

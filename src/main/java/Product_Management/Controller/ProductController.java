package Product_Management.Controller;

import Product_Management.Model.Product;
import java.util.List;
import Product_Management.Service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/products")
public class ProductController {
    @Autowired
    private ProductService service;

    @GetMapping
    public List<Product>  getAll(){
        return service.getAllProducts();
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id){
        return service.getProductById(id);
    }

    @PostMapping
    public Product create(@Valid @RequestBody Product product){
        return service.createProduct(product);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id,@Valid @RequestBody Product product){
        return  service.updateProduct(id,product);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id){
        service.deleteProduct(id);
        return "Product deleted successfully";
    }
}

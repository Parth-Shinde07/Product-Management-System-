package Product_Management.Service;

import Product_Management.Model.Product;
import Product_Management.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProductService {
    @Autowired
    private ProductRepository repository;

    public List<Product> getAllProducts(){
        return repository.findAll();
    }

    public Product getProductById(Long id){
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Product createProduct(Product product){
        return repository.save(product);
    }

    public  Product updateProduct(Long id,Product updatedProduct){
        Product existingProduct=getProductById(id);
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setPrice(updatedProduct.getPrice());
        return repository.save(existingProduct);
    }

    public void deleteProduct(Long id){
        repository.deleteById(id);
    }
}

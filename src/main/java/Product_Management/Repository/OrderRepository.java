package Product_Management.Repository;

import Product_Management.Model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.customer LEFT JOIN FETCH o.items")
    List<Order> findAll();

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.customer LEFT JOIN FETCH o.items WHERE o.id = :id")
    java.util.Optional<Order> findById(Long id);
}

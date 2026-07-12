package Product_Management.DTO;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private Long customerId;
    private List<ItemRequest> items;

    @Data
    public static class ItemRequest{
        private Long productId;
        private int quantity;
    }
}

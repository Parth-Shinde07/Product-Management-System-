package Product_Management.event;

public class LowStockEvent {
    private final Long productId;
    private final String productName;
    private final int remainingStock;

    public LowStockEvent(Long productId, String productName, int remainingStock) {
        this.productId = productId;
        this.productName = productName;
        this.remainingStock = remainingStock;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getRemainingStock() {
        return remainingStock;
    }
}

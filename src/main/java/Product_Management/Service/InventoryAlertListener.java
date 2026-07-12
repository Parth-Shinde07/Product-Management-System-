package Product_Management.Service;

import Product_Management.event.LowStockEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class InventoryAlertListener {

//    @Async
    @EventListener
    public void handleStockAlert(LowStockEvent event){
        System.out.println(">>> EVENT LISTENER RECEIVED AN EVENT FOR: " + event.getProductName());
        System.out.println("[INVENTORY ALERT] Product '"+event.getProductName()
        + "' (ID: "+ event.getProductId()+ ") is runnig low! Remaining stock: "
        + event.getRemainingStock());
    }
}

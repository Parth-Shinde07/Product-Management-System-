package Product_Management.example.API.Service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class InvoiceProcessingService {

    @Async
    public CompletableFuture<Boolean> generateAndEmailInvoice(String orderNumber){
        try{
            System.out.println("Starting invoice generation for:" + orderNumber + " on thread: "+Thread.currentThread().getName());

            Thread.sleep(5000);

            System.out.println("Invoice successfully emailed for: "+orderNumber);
            return CompletableFuture.completedFuture(true);
        }catch (InterruptedException e){
            return CompletableFuture.completedFuture(false);
        }
    }
}

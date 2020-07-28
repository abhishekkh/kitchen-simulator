package com.kitchen;


import com.google.gson.Gson;
import com.kitchen.courier.CourierScheduler;
import com.kitchen.kitchen.IKitchenWorker;
import com.kitchen.kitchen.KitchenWorker;
import com.kitchen.order.Order;
import com.kitchen.shelf.IShelfAccessor;
import com.kitchen.shelf.ShelfAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Ingests the orders from the orders.json file into memory. The ingestion rate and the orders.json file path should be passed as
 * arguments to the program.
 *
 * Sample Usage:
 * java com.cloudkitchen.OrderIngestor 2 "/my/fully/qualified/path/orders.json"
 */
public class OrderIngestor {
    static Logger logger = LoggerFactory.getLogger(OrderIngestor.class);

    public static void main(String[] args){
        // check arguments
        if(args.length != 2)
            throw new IllegalArgumentException("Please provide the arguments in the order: INGESTION_RATE FILE_PATH");

        final int INGESTION_RATE = Integer.parseInt(args[0]);
        final String FILE_PATH = args[1];

        CourierScheduler courierScheduler = new CourierScheduler();
        IShelfAccessor<Order> shelfAccessor = new ShelfAccessor();

        // ingest from the file
        try {
            Gson gson = new Gson();
            Reader reader = Files.newBufferedReader(Paths.get(FILE_PATH));
            Order[] orders = gson.fromJson(reader, Order[].class);
            int curr = 0;
            for (Order order: orders){
                if(curr % INGESTION_RATE == 0){
                    Thread.sleep(1000);
                }
                logger.info("Processing order: {}", order.getId());
                IKitchenWorker kitchenWorker = new KitchenWorker(order, courierScheduler, shelfAccessor);
                boolean result = kitchenWorker.processOrder();
                if(!result)
                    logger.error("Processing failed for order: {}", order.getId());
                curr++;
            }
            courierScheduler.shutdown();

        } catch (FileNotFoundException e) {
            logger.error("Invalid file path provided: {}", e.getMessage());
        } catch (IOException | InterruptedException e) {
            logger.error("IOException", e.getMessage());
        }

    }

}

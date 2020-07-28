package com.kitchen.courier;

import com.kitchen.order.Order;
import com.kitchen.shelf.IShelfAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Courier implements Runnable {
    static Logger logger = LoggerFactory.getLogger(Courier.class);

    int delayInSeconds;
    String orderId;
    IShelfAccessor shelfAccessor;
    Order order;

    public Courier(int delayInSeconds, String orderId, IShelfAccessor shelfAccessor){
        this.delayInSeconds = delayInSeconds;
        this.orderId = orderId;
        this.shelfAccessor = shelfAccessor;
    }

    @Override
    public void run() {
        // courier arrives, picks up and delivers the order
        pickupOrder();
    }

    /**
     * kick off the delivery process, send out notifications etc
     */
    private void deliverOrder() {
        logger.info("Order: {} ready for delivery", order.getId());
    }

    /**
     * find the order on the shelf and deliver it
     * for orders with negative values do nothing.
     */
    private void pickupOrder() {
        try{
            // take order off the shelf
            shelfAccessor.showShelf();
            order = (Order) shelfAccessor.removeFromShelf(orderId);
            logger.info("Order: {} picked up by courier: {}", order.getId(), Thread.currentThread().getId());

            if(order.getValue() > 0)
                deliverOrder();
            else
                logger.info("Order: {} value is negative {}, will be trashed!!", order.getId(), order.getValue());
        } catch (Exception e){
            logger.error("Order: {} could not be picked by courier, message: {}", orderId, e.getMessage());
        }

    }
}

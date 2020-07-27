package com.cloudkitchen.kitchen;

import com.cloudkitchen.courier.CourierScheduler;
import com.cloudkitchen.order.Order;
import com.cloudkitchen.shelf.IShelfAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Random;



public class KitchenWorker implements IKitchenWorker {
    static Logger logger = LoggerFactory.getLogger(KitchenWorker.class);
    private Order order;
    private Random random = new Random();
    private static final int DELIVERY_FLOOR = 2;
    private static final int DELIVERY_CEILING = 6;
    private CourierScheduler courierScheduler;
    private IShelfAccessor<Order> shelfAccessor;
    private List<String> allowedTemps = Arrays.asList("hot", "cold", "frozen");


    public KitchenWorker(Order order, CourierScheduler courierScheduler, IShelfAccessor<Order> shelfAccessor){
        this.order = order;
        this.courierScheduler = courierScheduler;
        this.shelfAccessor = shelfAccessor;
    }

    public boolean processOrder() {
        try{
            //validate order
            validateOrder();
            //cook order
            cookOrder();
            //find shelf
            routeToShelf();
            //dispatch courier
            dispatchCourier();
            return true;
        } catch (Exception e){
            logger.error("There was an error handling order: {}, message: {}", order.getId(), e.getMessage());
            return false;
        }

    }

    /**
     * Validates the order, order temperature must be {hot, cold, frozen}
     *
     * @throws IllegalArgumentException
     */
    public void validateOrder() {
        if(!allowedTemps.contains(order.getTemp()))
            throw new IllegalArgumentException("Order temp " + order.getTemp() + " invalid");
    }

    /**
     * order is ready to be dispatched
     */
    private void dispatchCourier() {
        // Trigger a courier thread to run after a delay
        int delayInSeconds = DELIVERY_FLOOR + random.nextInt(DELIVERY_CEILING - DELIVERY_FLOOR + 1);
        courierScheduler.schedule(order, delayInSeconds, shelfAccessor);
        logger.info("Dispatching courier for order: {} with delay of {} seconds", order.getId(), delayInSeconds);
    }

    /**
     * once order is cooked, update the processed time and route to appropriate shelf
     */
    private void routeToShelf() {
        order.setOrderProcessedTime(System.currentTimeMillis());
        shelfAccessor.placeOnShelf(order);
    }

    /**
     * cook the order assigned to this kitchen worker instance.
     */
    private void cookOrder() {
        logger.info("Cooking order: {}", order.getId());
    }
}

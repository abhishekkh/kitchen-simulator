package com.kitchen.shelf;

import com.kitchen.order.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShelfAccessor implements IShelfAccessor<Order>{
    private static Logger logger = LoggerFactory.getLogger(ShelfAccessor.class);
    Random random = new Random();
    private static final int SHELF_CAPACITY = 10;
    Map<ShelfType, List<Order>> shelfMap;
    Map<String, ShelfType> orderToShelfMap;

    /**
     * Initialize the shelf map and the reverse shelf map
     */
    public ShelfAccessor(){
        shelfMap = new ConcurrentHashMap<>();
        shelfMap.put(ShelfType.HOT, new ArrayList<>(SHELF_CAPACITY));
        shelfMap.put(ShelfType.COLD, new ArrayList<>(SHELF_CAPACITY));
        shelfMap.put(ShelfType.FROZEN, new ArrayList<>(SHELF_CAPACITY));
        shelfMap.put(ShelfType.OVERFLOW, new ArrayList<>(SHELF_CAPACITY));
        orderToShelfMap = new ConcurrentHashMap<>();
        logger.info("Shelf initialized");
    }

    /**
     * Figure out the appropriate shelf for the given order based on the order details and the current shelf capacities.
     *
     * @param order The order to be placed on shelf
     */
    @Override
    public synchronized void placeOnShelf(Order order){

        // first check shelf has space
        if (checkSpaceOnProvidedShelf(order)) return;

        // if shelf is full, add to overflow shelf
        if (checkSpaceOnOverFlowShelf(order)) return;

        // if overflow is full, pick an order from overflow and move to available shelf
        if (moveOrderFromOverFlow(order)) return;

        // if all shelves are full, pick a random order from overflow shelf and discard it
        discardExistingOrderFromOverFlow(order);
    }

    /**
     * Discard a random order from the overflow shelf.
     * @param order new order to be added to shelf
     */
    private void discardExistingOrderFromOverFlow(Order order) {
        List<Order> overflowShelf = shelfMap.get(ShelfType.OVERFLOW);

        int idx = random.nextInt(SHELF_CAPACITY);
        Order randomPickedOrder = overflowShelf.get(idx);
        overflowShelf.remove(idx);
        orderToShelfMap.remove(randomPickedOrder.getId());
        logger.info("Order: {} discarded from shelf: {}", randomPickedOrder.getId(), ShelfType.OVERFLOW);

        // now add the order to overflow shelf
        addOrderToShelf(ShelfType.OVERFLOW, order);
    }

    /**
     *  Moves an order from the overflow shelf to another shelf.
     *  We will start looking at order from the end of the list to make this more efficient by
     *  reducing the amount of shuffling once the order is removed.
     *
     * @param order new order to be added to shelf
     * @return if no move is possible, because the other potential destination shelves
     * are full return false
     */
    private boolean moveOrderFromOverFlow(Order order) {

        // calculate the order values for overflow shelf only.
        List<Order> overflowShelf = shelfMap.get(ShelfType.OVERFLOW);

        for(int i = SHELF_CAPACITY - 1; i > 0; i--){
            // pick an order from the overflow shelf to be moved to available shelf
            // we will start looking at order from the end of the list
            // check if the picked order can be added back to the shelf as per its temp

            Order pickedOrder = overflowShelf.get(i);
            ShelfType pickedOrderShelf = ShelfType.valueOf(pickedOrder.getTemp().toUpperCase());
            if(shelfMap.get(pickedOrderShelf).size() < SHELF_CAPACITY){
                // remove from overflow shelf
                overflowShelf.remove(pickedOrder);
                orderToShelfMap.remove(pickedOrder.getId());
                addOrderToShelf(pickedOrderShelf, pickedOrder);

                // now add the new order to overflow shelf
                addOrderToShelf(ShelfType.OVERFLOW, order);
                return true;
            }
        }
        // we could not find an order to be moved
        return false;
    }

    /**
     * Returns true if we are able to add to overflow shelf
     * @param order to be added to shelf
     * @return
     */
    private boolean checkSpaceOnOverFlowShelf(Order order) {
        List<Order> overflowShelf = shelfMap.get(ShelfType.OVERFLOW);
        if(overflowShelf.size() < SHELF_CAPACITY){
            addOrderToShelf(ShelfType.OVERFLOW, order);
            return true;
        }
        return false;
    }

    /**
     * Returns true if we are able to add to provided shelf as per the order temp
     * @param order to be added to shelf
     * @return
     */
    private boolean checkSpaceOnProvidedShelf(Order order) {
        List<Order> currentShelf = shelfMap.get(ShelfType.valueOf(order.getTemp().toUpperCase()));
        if(currentShelf.size() < SHELF_CAPACITY){
            ShelfType shelfType = ShelfType.valueOf(order.getTemp().toUpperCase());
            addOrderToShelf(shelfType, order);
            return true;
        }
        return false;
    }

    /**
     * Display the contents for each shelf.
     * Time complexity O(m*n) where m = number of shelves, n = number of orders in each shelf
     * Worst case complexity O(n) where n = max orders per shelf
     */
    @Override
    public synchronized void showShelf(){
        for(Map.Entry<ShelfType, List<Order>> entry: shelfMap.entrySet()) {
            List<Order> currentShelf = entry.getValue();
            updateOrderValue(currentShelf, entry.getKey());
        }
        logger.info("{} shelf size: {} -> {}", ShelfType.HOT, shelfMap.get(ShelfType.HOT).size(), shelfMap.get(ShelfType.HOT));
        logger.info("{} shelf size: {} -> {}", ShelfType.COLD, shelfMap.get(ShelfType.COLD).size(), shelfMap.get(ShelfType.COLD));
        logger.info("{} shelf size: {} -> {}", ShelfType.FROZEN, shelfMap.get(ShelfType.FROZEN).size(), shelfMap.get(ShelfType.FROZEN));
        logger.info("{} shelf size: {} -> {}", ShelfType.OVERFLOW, shelfMap.get(ShelfType.OVERFLOW).size(), shelfMap.get(ShelfType.OVERFLOW));
    }

    /**
     * Calculate the value for each item in the given shelf and update the order value
     * This method is synchronized as we dont want other threads reading the map during the updates.
     *
     * @param orders
     * @param shelfType
     */
    private void updateOrderValue(List<Order> orders, ShelfType shelfType) {
        if(!orders.isEmpty()){
            int shelfDecayModifier = (shelfType.equals(ShelfType.OVERFLOW)) ? 2 : 1;
            for(Order order: orders){
                // calculate order value
                float orderValue = (order.getShelfLife() - (order.getDecayRate() * (System.currentTimeMillis() - order.getOrderProcessedTime())/1000 * shelfDecayModifier))/order.getShelfLife();
                // update the value
                order.setValue(orderValue);
            }
        }

    }

    /**
     * Looks up the shelf for the given order id and removes the order from the shelf
     *
     * @param orderId Order id
     * @throws NoSuchElementException when order id not found on the shelf
     */
    @Override
    public synchronized Order removeFromShelf(String orderId) {
        if (orderToShelfMap.containsKey(orderId)) {
            ShelfType shelfType = orderToShelfMap.get(orderId);
            List<Order> orders = shelfMap.get(shelfType);
            for (Order order : orders) {
                if (order.getId().equals(orderId)) {
                    orders.remove(order);
                    orderToShelfMap.remove(orderId);
                    return order;
                }
            }
        }
        throw new NoSuchElementException("Order: " + orderId + " not found");
    }

    /**
     * Helper method to add order to shelf
     * @param shelfType
     * @param order
     */
    private void addOrderToShelf(ShelfType shelfType, Order order){
        List<Order> currentShelf = shelfMap.get(shelfType);
        currentShelf.add(order);
        orderToShelfMap.put(order.getId(), shelfType);
        logger.info("Order: {} placed on shelf: {}", order.getId(), shelfType);
    }
}

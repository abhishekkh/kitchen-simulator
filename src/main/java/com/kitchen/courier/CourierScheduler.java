package com.kitchen.courier;

import com.kitchen.order.Order;
import com.kitchen.shelf.IShelfAccessor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CourierScheduler {
    private static final int COURIER_POOL = 5;
    private static final int POOL_WAIT_TIMEOUT = 10;
    private ScheduledExecutorService executorService;

    public CourierScheduler(){
        executorService = Executors.newScheduledThreadPool(COURIER_POOL);
    }

    public void schedule(Order order, int delayInSeconds, IShelfAccessor<Order> shelfAccessor) {
        executorService.schedule(new Courier(delayInSeconds, order.getId(), shelfAccessor),
                delayInSeconds, TimeUnit.SECONDS);
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(POOL_WAIT_TIMEOUT, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}

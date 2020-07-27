package com.cloudkitchen.kitchen;

import com.cloudkitchen.courier.CourierScheduler;
import com.cloudkitchen.order.Order;
import com.cloudkitchen.shelf.ShelfAccessor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class KitchenWorkerTest {
    @Mock
    private CourierScheduler courierScheduler;

    @Mock
    private ShelfAccessor shelfAccessor;

    @Mock
    private Order order;

    @InjectMocks
    private KitchenWorker kitchenWorker;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test_processOrder_invalidOrder(){
        // given
        Mockito.when(order.getTemp()).thenReturn("hott");

        // then
        Assert.assertNotEquals(true, kitchenWorker.processOrder());
    }
}

package com.kitchen.shelf;

import com.kitchen.order.Order;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class ShelfAccessorTest {
    ShelfAccessor shelfAccessor;

    @Before
    public void setup() {
        shelfAccessor = new ShelfAccessor();
    }

    @Test
    public void test_placeOnShelf_spaceAvailableOnProvidedShelf() {
        // given
        Order order = new Order();
        order.setId("1");
        order.setTemp("hot");

        //when
        shelfAccessor.placeOnShelf(order);

        //then
        Assert.assertEquals("added", 1, shelfAccessor.shelfMap.get(ShelfType.HOT).size());
        Assert.assertTrue("added to reverse map", shelfAccessor.orderToShelfMap.containsKey("1"));
    }

    @Test
    public void test_placeOnShelf_spaceFullOnProvidedAvailableInOverflow() {
        // given
        Order order = new Order();
        order.setId("1");
        order.setTemp("hot");
        List<Order> orders = shelfAccessor.shelfMap.get(ShelfType.HOT);
        for (int i = 0; i < 10; i++) {
            Order existingOrder = new Order();
            existingOrder.setId("2");
            existingOrder.setTemp("hot");
            orders.add(order);
        }

        // when space is full
        shelfAccessor.placeOnShelf(order);

        // then add to overflow
        Assert.assertEquals("not added to specified", 10, shelfAccessor.shelfMap.get(ShelfType.HOT).size());
        Assert.assertEquals("added to overflow", 1, shelfAccessor.shelfMap.get(ShelfType.OVERFLOW).size());
        Assert.assertTrue("added to reverse map", shelfAccessor.orderToShelfMap.containsKey("1"));
    }

    @Test
    public void test_placeOnShelf_providedFullAndOverflowfull() {
        // given
        Order order = new Order();
        order.setId("1");
        order.setTemp("hot");

        List<Order> hotOrders = shelfAccessor.shelfMap.get(ShelfType.HOT);
        for (int i = 0; i < 10; i++) {
            Order existingOrder = new Order();
            existingOrder.setId("2");
            existingOrder.setTemp("hot");
            hotOrders.add(existingOrder);
        }

        List<Order> overflowOrders = shelfAccessor.shelfMap.get(ShelfType.OVERFLOW);
        for (int i = 0; i < 10; i++) {
            Order existingOrder = new Order();
            existingOrder.setId("3");
            existingOrder.setTemp("cold");
            overflowOrders.add(existingOrder);
        }

        // when overflow is also full with cold orders, look for space in cold shelf
        shelfAccessor.placeOnShelf(order);

        // then
        Assert.assertEquals("not added to overflow", 10, shelfAccessor.shelfMap.get(ShelfType.OVERFLOW).size());
        Assert.assertTrue("added to cold shelf",
                shelfAccessor.shelfMap.get(ShelfType.COLD).size() == 1);
    }

    @Test
    public void test_showShelf_verifyOverflowShelfOrderValue() {
        // given

        List<Order> overflowOrders = shelfAccessor.shelfMap.get(ShelfType.OVERFLOW);

        Order order = new Order();
        order.setId("1");
        order.setTemp("hot");
        order.setShelfLife(100);
        order.setDecayRate(0.9f);
        order.setOrderProcessedTime(System.currentTimeMillis());
        overflowOrders.add(order);

        // when overflow is also full, look for space in cold or frozen shelf
        shelfAccessor.showShelf();

        // then
        Assert.assertEquals(1.0,
                shelfAccessor.shelfMap.get(ShelfType.OVERFLOW).get(0).getValue(), 0.1);
    }

    @Test
    public void test_placeOnShelf_allShelvesfull() {
        // given
        Order order = new Order();
        order.setId("1");
        order.setTemp("hot");

        List<Order> hotOrders = shelfAccessor.shelfMap.get(ShelfType.HOT);
        for (int i = 0; i < 10; i++) {
            Order existingOrder = new Order();
            existingOrder.setId("2");
            existingOrder.setTemp("hot");
            hotOrders.add(existingOrder);
        }

        List<Order> overflowOrders = shelfAccessor.shelfMap.get(ShelfType.OVERFLOW);
        for (int i = 0; i < 10; i++) {
            Order existingOrder = new Order();
            existingOrder.setId(String.valueOf(i + 1));
            existingOrder.setTemp("hot");
            overflowOrders.add(existingOrder);
        }

        List<Order> coldOrders = shelfAccessor.shelfMap.get(ShelfType.COLD);
        for (int i = 0; i < 10; i++) {
            Order existingOrder = new Order();
            existingOrder.setId("4");
            existingOrder.setTemp("cold");
            coldOrders.add(existingOrder);
        }

        List<Order> frozenOrders = shelfAccessor.shelfMap.get(ShelfType.FROZEN);
        for (int i = 0; i < 10; i++) {
            Order existingOrder = new Order();
            existingOrder.setId("5");
            existingOrder.setTemp("frozen");
            frozenOrders.add(existingOrder);
        }


        // when everything is full
        shelfAccessor.placeOnShelf(order);

        // then
        Assert.assertTrue("added to overflow after discarding existing order from overflow",
                shelfAccessor.shelfMap.get(ShelfType.OVERFLOW).contains(order));

    }
}
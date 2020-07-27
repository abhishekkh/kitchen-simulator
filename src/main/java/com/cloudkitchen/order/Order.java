package com.cloudkitchen.order;

public class Order {

    String id;
    String name;
    String temp;
    int shelfLife;
    float value;
    long orderProcessedTime;
    float decayRate;


    public float getDecayRate() {
        return decayRate;
    }

    public void setDecayRate(float decayRate) {
        this.decayRate = decayRate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public int getShelfLife() {
        return shelfLife;
    }

    public void setShelfLife(int shelfLife) {
        this.shelfLife = shelfLife;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public long getOrderProcessedTime() {
        return orderProcessedTime;
    }

    public void setOrderProcessedTime(long orderProcessedTime) {
        this.orderProcessedTime = orderProcessedTime;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", temp='" + temp + '\'' +
                ", shelfLife=" + shelfLife +
                ", value=" + value +
                ", orderProcessedTime=" + orderProcessedTime +
                ", decayRate=" + decayRate +
                '}';
    }
}

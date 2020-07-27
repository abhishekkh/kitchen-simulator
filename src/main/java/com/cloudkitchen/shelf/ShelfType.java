package com.cloudkitchen.shelf;

public enum ShelfType {
    HOT("hot"),
    COLD("cold"),
    FROZEN("frozen"),
    OVERFLOW("overflow");

    String shelfType;

    ShelfType(String shelfType) {
        this.shelfType = shelfType;
    }

    public String getName(){
        return shelfType;
    }
}

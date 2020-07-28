package com.kitchen.shelf;

public interface IShelfAccessor<T> {

    void placeOnShelf(T order);

    T removeFromShelf(String id);

    void showShelf();

}

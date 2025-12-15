package com.example.formularioapp;

import java.util.ArrayList;
import java.util.List;

public class RepositorioCarrito {

    private static List<Producto> cartItems = new ArrayList<>();

    public static List<Producto> getCartItems() {
        return cartItems;
    }

    public static void addItem(Producto item) {
        cartItems.add(item);
    }

    public static void removeItem(Producto item) {
        cartItems.remove(item);
    }

    public static void clearCart() {
        cartItems.clear();
    }

    // Necesario para el contador del carrito
    public static int getItemCount() {
        return cartItems.size();
    }
}
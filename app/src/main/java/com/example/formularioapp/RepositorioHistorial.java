package com.example.formularioapp;

import java.util.ArrayList;
import java.util.List;

public class RepositorioHistorial {

    // Lista que almacena todas las órdenes completadas
    private static List<OrdenCompra> historialDeCompras = new ArrayList<>();

    public static void agregarOrden(OrdenCompra orden) {
        // Añadir al inicio para que el historial sea LIFO (más reciente primero)
        historialDeCompras.add(0, orden);
    }

    public static List<OrdenCompra> getHistorial() {
        return historialDeCompras;
    }

    public static void clearHistorial() {
        historialDeCompras.clear();
    }
}
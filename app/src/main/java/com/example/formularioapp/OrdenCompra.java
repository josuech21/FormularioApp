package com.example.formularioapp;

import java.util.List;
import java.util.UUID;
import java.io.Serializable;

public class OrdenCompra implements Serializable {
    private String idOrden;
    private long fechaCompra; // Timestamp
    private double totalFinal;
    private List<Producto> itemsComprados;

    // Constructor que usa la lista del carrito y el total calculado
    public OrdenCompra(List<Producto> items, double total) {
        this.idOrden = UUID.randomUUID().toString(); // Genera un ID único
        this.fechaCompra = System.currentTimeMillis();
        this.totalFinal = total;
        this.itemsComprados = items;
    }


    public String getIdOrden() {
        return idOrden;
    }

    public long getFechaCompra() {
        return fechaCompra;
    }

    public double getTotalFinal() {
        return totalFinal;
    }

    public List<Producto> getItemsComprados() {
        return itemsComprados;
    }
}
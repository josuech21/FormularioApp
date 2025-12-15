package com.example.formularioapp;

/**
 * Clase Modelo para mapear documentos de la colección 'productos' en Firebase Firestore.
 */
public class Producto {

    private String nombre;
    private double precio;
    private String detalle;
    private long stock;         // Usamos 'long' para mayor robustez con los enteros de Firestore
    private String fotoUrl;
    private String vendedorId;

    // 1. CONSTRUCTOR VACÍO (OBLIGATORIO para Firebase)
    public Producto() {
    }

    // 2. CONSTRUCTOR COMPLETO (Opcional)
    public Producto(String nombre, double precio, String detalle, long stock, String fotoUrl, String vendedorId) {
        this.nombre = nombre;
        this.precio = precio;
        this.detalle = detalle;
        this.stock = stock;
        this.fotoUrl = fotoUrl;
        this.vendedorId = vendedorId;
    }

    // 3. GETTERS Y SETTERS (OBLIGATORIOS para Firebase)

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public long getStock() {
        return stock;
    }

    public void setStock(long stock) {
        this.stock = stock;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public String getVendedorId() {
        return vendedorId;
    }

    public void setVendedorId(String vendedorId) {
        this.vendedorId = vendedorId;
    }
}
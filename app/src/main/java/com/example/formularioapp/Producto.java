package com.example.formularioapp;

import java.io.Serializable;
import java.util.Objects;
// 🛑 IMPORTANTE: Añadir estas importaciones si usas anotaciones de Firebase en el futuro.
// import com.google.firebase.firestore.Exclude;
// import com.google.firebase.firestore.IgnoreExtraProperties;

public class Producto implements Serializable {

    // 1. Atributos del Producto
    private String productoId; // Identificador único
    private String nombre;
    private double precio;
    private long stock;
    private String fotoUrl;
    private String vendedorId; // 🔴 ID del vendedor que posee el producto (EXISTENTE)

    // 🟢 NUEVOS ATRIBUTOS DE UBICACIÓN y ESTADO (Añadidos)
    private String descripcion;
    private String ubicacion; // Dirección legible (ej: San José, centro)
    private double latitud;
    private double longitud;
    private boolean disponible; // Para el Switch "Vendido / Disponible"


    // Constructor vacío (Modificado para inicializar nuevos campos)
    public Producto() {
        this.descripcion = "";
        this.ubicacion = "";
        this.latitud = 0.0;
        this.longitud = 0.0;
        this.disponible = true; // Asumimos que al crear es disponible
    }

    // Constructor completo (Modificado para incluir nuevos campos)
    public Producto(String productoId, String nombre, double precio, long stock, String fotoUrl, String vendedorId,
                    String descripcion, String ubicacion, double latitud, double longitud, boolean disponible) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.fotoUrl = fotoUrl;
        this.vendedorId = vendedorId;

        // 🟢 Inicialización de nuevos atributos
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.latitud = latitud;
        this.longitud = longitud;
        this.disponible = disponible;
    }

    // -----------------------------------------------------
    // 2. Getters (Existentes + Nuevos)
    // -----------------------------------------------------

    public String getProductoId() {
        return productoId;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public long getStock() {
        return stock;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public String getVendedorId() {
        return vendedorId;
    }

    // 🟢 NUEVOS GETTERS

    public String getDescripcion() {
        return descripcion;
    }

    public String getUbicacion() {
        return ubicacion; // 🛑 ESTO RESUELVE el error 'cannot find symbol method getUbicacion()' 🛑
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public boolean isDisponible() {
        return disponible;
    }

    // -----------------------------------------------------
    // 3. Setters (Existentes + Nuevos)
    // -----------------------------------------------------

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public void setStock(long stock) {
        this.stock = stock;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public void setVendedorId(String vendedorId) {
        this.vendedorId = vendedorId;
    }

    // 🟢 NUEVOS SETTERS

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }


    // -----------------------------------------------------
    // 4. Métodos de Utilidad (Sin Cambios)
    // -----------------------------------------------------

    /**
     * Define que dos productos son iguales si su productoId es el mismo.
     * Crucial para métodos como List.remove() en RepositorioCarrito.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Producto producto = (Producto) o;
        // La igualdad se basa solo en el ID
        return Objects.equals(productoId, producto.productoId);
    }

    /**
     * Genera el hash code basado en el productoId.
     */
    @Override
    public int hashCode() {
        return Objects.hash(productoId);
    }
}
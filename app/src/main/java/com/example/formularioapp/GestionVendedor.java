package com.example.formularioapp;

import java.util.ArrayList;
import java.util.List;

public class GestionVendedor {


    private static List<Producto> inventarioGeneral = new ArrayList<>();


    private static String idVendedorActual = "";

    private GestionVendedor() {}


    public static void setVendedorActual(String id) {
        idVendedorActual = id;
    }


    private static boolean isVendedorAutorizado() {
        // En este stub, asumimos que si el ID no es nulo o vacío, está logueado.
        return idVendedorActual != null && !idVendedorActual.isEmpty();
    }

    /**

     */
    public static boolean agregarProducto(Producto nuevoProducto) {
        if (!isVendedorAutorizado()) {
            return false;
        }

        // Asignar el ID del vendedor actual al producto
        nuevoProducto.setVendedorId(idVendedorActual);

        inventarioGeneral.add(nuevoProducto);
        return true;
    }


    public static List<Producto> getInventarioDelVendedor() {
        List<Producto> productosVendedor = new ArrayList<>();

        if (!isVendedorAutorizado()) {
            return productosVendedor;
        }

        // Filtrar la lista general por el ID del vendedor actual
        for (Producto producto : inventarioGeneral) {
            if (idVendedorActual.equals(producto.getVendedorId())) {
                productosVendedor.add(producto);
            }
        }
        return productosVendedor;
    }


    public static boolean eliminarProducto(String productoId) {
        if (!isVendedorAutorizado()) {
            return false;
        }

        java.util.Iterator<Producto> iterator = inventarioGeneral.iterator();
        while (iterator.hasNext()) {
            Producto p = iterator.next();


            if (p.getProductoId().equals(productoId) &&
                    idVendedorActual.equals(p.getVendedorId())) {

                iterator.remove();
                return true;
            }
        }

        return false;
    }


    public static Producto buscarProductoPorId(String productoId) {
        if (!isVendedorAutorizado()) {
            return null;
        }

        for (Producto p : inventarioGeneral) {
            if (p.getProductoId().equals(productoId) &&
                    idVendedorActual.equals(p.getVendedorId())) {
                return p;
            }
        }
        return null;
    }
}
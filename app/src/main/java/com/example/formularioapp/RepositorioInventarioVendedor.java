package com.example.formularioapp;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class RepositorioInventarioVendedor {


    private static List<Producto> inventarioGeneral = new ArrayList<>();


    private static String idVendedorActual = "";


    private RepositorioInventarioVendedor() {}


    public static void setVendedorActual(String id) {
        idVendedorActual = id;
    }


    public static boolean isVendedorAutorizado() {

        return idVendedorActual != null && !idVendedorActual.isEmpty();
    }


    public static boolean agregarProducto(Producto nuevoProducto) {
        if (!isVendedorAutorizado()) {
            return false;
        }


        if (nuevoProducto.getVendedorId() == null || nuevoProducto.getVendedorId().isEmpty()) {
            nuevoProducto.setVendedorId(idVendedorActual);
        }

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

        // Usamos Iterator para eliminar elementos de forma segura mientras recorremos la lista
        java.util.Iterator<Producto> iterator = inventarioGeneral.iterator();
        while (iterator.hasNext()) {
            Producto p = iterator.next();

            // Verificar pertenencia al vendedor actual y coincidencia de ID
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
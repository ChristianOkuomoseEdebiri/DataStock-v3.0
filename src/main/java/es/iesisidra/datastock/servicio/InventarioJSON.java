package es.iesisidra.datastock.servicio;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import es.iesisidra.datastock.modelo.Producto;
import es.iesisidra.datastock.util.ColoresConsola;
import es.iesisidra.datastock.util.LectorConsola;

public class InventarioJSON {

    // cabe aclarar que aqui nos preparamos para pasar la lista de productos pero realmente se pasa en Inventario
    private final List<Producto> productos;

    // instancio el gestor para poder hacer uso de metodos de gestion de productos
    private final GestorProductos gestorProductos;

    public InventarioJSON(List<Producto> productos) {
        this.productos = productos;
        this.gestorProductos = new GestorProductos();
    }

    /**
     * Metodo para importar productos JSON utilizando el componente reutilizable
     * LectorProductosJson.
     *
     * @param ruta ruta del fichero JSON
     */
    public void importarJson(String ruta) {

        try {

            // Creo el componente encargado de leer los productos.
            LectorProductosJson lector = new LectorProductosJson();

            // Leo el fichero y obtengo los productos validos.
            List<Producto> productosLeidos = lector.leer(ruta);

            // Recupero los resultados de la lectura.
            int encontrados = lector.getEncontrados();
            List<String> incidencias = lector.getIncidencias();

            int importados = 0;
            int rechazados = incidencias.size();

            // Recorro los productos validos obtenidos del JSON.
            for (Producto p : productosLeidos) {

                // Compruebo que no haya errores respecto al inventario actual.
                List<String> errores = gestorProductos.validarProducto(p, productos);

                if (errores.isEmpty()) {

                    // Añado el producto al inventario.
                    productos.add(p);
                    importados++;

                } else {

                    rechazados++;

                    // Construyo el mensaje con los motivos de rechazo.
                    StringBuilder mensajeError = new StringBuilder();

                    for (int j = 0; j < errores.size(); j++) {
                        mensajeError.append(errores.get(j));

                        if (j < errores.size() - 1) {
                            mensajeError.append(", ");
                        }
                    }

                    incidencias.add(p.getCodigo() + ": " + mensajeError.toString());
                }
            }

            // Muestro el resultado de la importacion.
            System.out.println("--- IMPORTAR JSON ---");
            System.out.println("Ruta: " + ruta);
            System.out.println("Productos encontrados: " + encontrados);
            System.out.println("Importados: " + importados + " | Rechazados: " + rechazados);

            // Muestro las incidencias registradas.
            if (!incidencias.isEmpty()) {

                System.out.println("[AVISO] INCIDENCIAS:");

                for (String inc : incidencias) {
                    System.out.println("  - " + inc);
                }

                System.out.println(ColoresConsola.ROJO + "[ERROR] Importacion finalizada con incidencias" + ColoresConsola.RESET);

            } else {

                System.out.println(ColoresConsola.VERDE + "[OK] Importacion finalizada con exito" + ColoresConsola.RESET);
            }

        } catch (IllegalArgumentException | SecurityException e) {

            System.out.println(ColoresConsola.ROJO + "[ERROR] " + e.getMessage() + ColoresConsola.RESET);

        } catch (IOException e) {

            System.out.println(ColoresConsola.ROJO + "[ERROR] al leer el fichero: " + e.getMessage() + ColoresConsola.RESET);
        }
    }

    /**
     * Metodo exportarJson que nos permite exportar el catalogo de productos a
     * un fichero en formato JSON usando la libreria GSON
     *
     * @param ruta Ruta del fichero donde se guaradara el JSON
     */
    public void exportarJson(String ruta) {
        // si no hay nada no exportamos
        if (productos.isEmpty()) {
            System.out.println(
                    ColoresConsola.ROJO + "[ERROR] El inventario está vacío. No hay nada que exportar." + ColoresConsola.RESET);
            return;
        }

        Path path;

        try {
            path = Ruta.validarParaGuardar(ruta);

        } catch (IllegalArgumentException | SecurityException e) {
            System.out.println(ColoresConsola.ROJO + "[ERROR] " + e.getMessage() + ColoresConsola.RESET);
            return;
        }

        if (Files.exists(path) && !LectorConsola.confirmar("[AVISO] Seguro que quieres sobrescribir: " + path)) {
            System.out.println(ColoresConsola.VERDE + "[OK] Cancelacion recibida saliendo correctamente" + ColoresConsola.RESET);
            return;
        }

        // ordeno los productos sin modificar el inventario
        List<Producto> copiaOrdenada = new ArrayList<>(productos);
        gestorProductos.ordenarPorCodigo(copiaOrdenada);

        // Construyo el objeto raiz que espera importarJSON
        JsonObject raiz = new JsonObject();
        raiz.add("productos", new Gson().toJsonTree(copiaOrdenada));

        // creo una instancia de Gson configurada para aplicar formato legible (pretty
        // printing)
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // try-with-resources para abrir y cerrar el writer de forma segura
        try (FileWriter writer = new FileWriter(path.toFile())) {

            // convierto la lista de productos completa a texto JSON
            gson.toJson(raiz, writer);

            System.out.println(ColoresConsola.VERDE + "[OK] Catalogo exportado correctamente a JSON en: " + ruta
                    + ColoresConsola.RESET);

        } catch (Exception e) {
            System.out.println(
                    ColoresConsola.ROJO + "[ERROR] al exportar a JSON: " + e.getMessage() + ColoresConsola.RESET);
        }

    }

}

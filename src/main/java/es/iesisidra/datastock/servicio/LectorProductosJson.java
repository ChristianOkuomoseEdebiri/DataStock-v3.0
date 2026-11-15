
package es.iesisidra.datastock.servicio;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import es.iesisidra.datastock.modelo.Producto;

/**
 * Componente reutilizable que lee un fichero JSON
 * y devuelve una lista de productos validos.
 *
 * No modifica el inventario ni muestra mensajes por consola.
 */
public class LectorProductosJson {

    // Guardo los resultados de la ultima lectura.
    private int encontrados;
    private List<String> incidencias = new ArrayList<>();

    // Reutilizo el gestor que ya tenemos.
    private final GestorProductos gestorProductos = new GestorProductos();

    /**
     * Lee un fichero JSON, comprueba los productos y devuelve
     * aquellos que cumplen las reglas de validacion.
     *
     * @param ruta ruta del fichero JSON
     * @return lista de productos validos
     * @throws IOException si falla la lectura del fichero
     * @throws IllegalArgumentException si la ruta o el JSON son invalidos
     */
    public List<Producto> leer(String ruta) throws IOException {

        // Reinicio los datos de la lectura anterior.
        encontrados = 0;
        incidencias.clear();

        // Lista auxiliar para almacenar los productos validos.
        List<Producto> productosValidos = new ArrayList<>();

        // Valido la ruta con nuestro metodo de Ruta.java.
        Path path = Ruta.validarParaLeer(ruta);

        // Abro el fichero con cierre automatico.
        try (FileReader reader = new FileReader(path.toFile())) {

            // Obtengo el contenido JSON.
            JsonElement contenido = JsonParser.parseReader(reader);

            // La raiz debe ser un objeto JSON.
            if (!contenido.isJsonObject()) {
                throw new IllegalArgumentException(
                        "La raiz debe ser un objeto JSON.");
            }

            JsonObject root = contenido.getAsJsonObject();

            // Compruebo que exista el array de productos.
            if (!root.has("productos") || !root.get("productos").isJsonArray()) {
                throw new IllegalArgumentException(
                        "La raiz debe contener el array de productos.");
            }

            JsonArray arrayproductos = root.getAsJsonArray("productos");

            Gson gson = new Gson();

            // Numero total de elementos encontrados.
            encontrados = arrayproductos.size();

            // Recorro los productos del fichero.
            for (int i = 0; i < encontrados; i++) {

                try {

                    // Cada elemento debe ser un objeto JSON.
                    if (!arrayproductos.get(i).isJsonObject()) {
                        throw new IllegalArgumentException(
                                "El elemento no es un objeto JSON valido.");
                    }

                    JsonObject elem = arrayproductos.get(i).getAsJsonObject();

                    // Compruebo los campos obligatorios.
                    if (!elem.has("codigo") || elem.get("codigo").isJsonNull()) {
                        throw new IllegalArgumentException(
                                "El codigo es obligatorio.");
                    }

                    if (!elem.has("precio") || elem.get("precio").isJsonNull()) {
                        throw new IllegalArgumentException(
                                "El precio es obligatorio.");
                    }

                    if (!elem.has("stock") || elem.get("stock").isJsonNull()) {
                        throw new IllegalArgumentException(
                                "El stock es obligatorio.");
                    }

                    // Convierto el JSON en un objeto Producto.
                    Producto p = gson.fromJson(elem, Producto.class);

                    // Elimino los espacios del codigo.
                    if (p.getCodigo() != null) {
                        p.setCodigo(p.getCodigo().trim());
                    }

                    // Valido con las reglas que ya tenemos.
                    List<String> errores = gestorProductos.validarProducto(p, productosValidos);

                    if (errores.isEmpty()) {

                        productosValidos.add(p);

                    } else {

                        // Identifico el producto rechazado.
                        String cod;

                        if (p.getCodigo() != null && !p.getCodigo().isEmpty()) {
                            cod = p.getCodigo();
                        } else {
                            cod = "Producto " + (i + 1);
                        }

                        // Construyo el mensaje con sus errores.
                        StringBuilder mensajeError = new StringBuilder();

                        for (int j = 0; j < errores.size(); j++) {
                            mensajeError.append(errores.get(j));

                            if (j < errores.size() - 1) {
                                mensajeError.append(", ");
                            }
                        }

                        incidencias.add(cod + ": " + mensajeError.toString());
                    }

                } catch (IllegalArgumentException e) {

                    incidencias.add("Producto " + (i + 1) + ": " + e.getMessage());

                } catch (Exception e) {

                    incidencias.add("Producto " + (i + 1) + ": Error en el formato de datos.");
                }
            }

            // Devuelvo la lista sin modificar el inventario.
            return productosValidos;

        } catch (JsonSyntaxException e) {

            throw new IllegalArgumentException(
                    "El fichero JSON esta malformado.", e);
        }
    }

    /**
     * Devuelve el numero de elementos encontrados en el JSON.
     *
     * @return cantidad de elementos encontrados
     */
    public int getEncontrados() {
        return encontrados;
    }

    /**
     * Devuelve las incidencias de la ultima lectura.
     *
     * @return lista de incidencias
     */
    public List<String> getIncidencias() {
        return new ArrayList<>(incidencias);
    }
}

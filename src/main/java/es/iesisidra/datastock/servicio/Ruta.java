package es.iesisidra.datastock.servicio;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import es.iesisidra.datastock.util.ColoresConsola;

public class Ruta {

    /**
     * Comprueba que la ruta no este vacia y la convierte en un objeto Path.
     *
     * @param ruta ruta introducida por el usuario
     * @return objeto Path
     */
    private static Path convertirRuta(String ruta) {

        if (ruta == null || ruta.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "La ruta no puede estar vacia.");
        }

        try {
            return Path.of(ruta.trim());
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException(
                    "El formato de la ruta es incorrecto.", e);
        }
    }

    /**
     * Valida una ruta para explorar archivos o directorios.
     *
     * @param ruta ruta que queremos explorar
     * @return Path validado
     */
    public static Path validarParaExplorar(String ruta) {

        Path path = convertirRuta(ruta);

        if (!Files.exists(path)) {
            throw new IllegalArgumentException(
                    "La ruta introducida no existe.");
        }

        if (!Files.isReadable(path)) {
            throw new IllegalArgumentException(
                    "No tienes permisos para leer la ruta.");
        }

        return path;
    }

    /**
     * Valida la ruta de un fichero que queremos leer. Se utiliza para importar
     * JSON, XML y DAT.
     *
     * @param ruta ruta del fichero
     * @return Path validado
     */
    public static Path validarParaLeer(String ruta) {

        Path path = validarParaExplorar(ruta);

        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException(
                    "La ruta no corresponde a un fichero.");
        }

        return path;
    }

    /**
     * Valida una ruta donde queremos guardar un fichero. Se utiliza para
     * exportar JSON, XML y DAT.
     *
     * @param ruta ruta de destino
     * @return Path validado
     */
    public static Path validarParaGuardar(String ruta) {

        Path path = convertirRuta(ruta);

        // Obtengo la carpeta donde voy a guardar.
        Path padre = path.toAbsolutePath().getParent();

        if (padre == null || !Files.isDirectory(padre)) {
            throw new IllegalArgumentException(
                    "El directorio de destino no existe.");
        }

        if (!Files.isWritable(padre)) {
            throw new IllegalArgumentException(
                    "No tienes permisos de escritura en el directorio.");
        }

        // Si ya existe, debe ser un fichero que pueda escribirse.
        if (Files.exists(path)) {

            if (!Files.isRegularFile(path)) {
                throw new IllegalArgumentException(
                        "La ruta corresponde a un directorio.");
            }

            if (!Files.isWritable(path)) {
                throw new IllegalArgumentException(
                        "No puedes sobrescribir el fichero.");
            }
        }

        return path;
    }

    /**
     * Explora una ruta y muestra la informacion del fichero o directorio.
     *
     * @param ruta ruta introducida por el usuario
     */
    public void explorarRuta(String ruta) {

        try {

            // Validamos la ruta.
            Path path = validarParaExplorar(ruta);

            File archivo = path.toFile();

            if (archivo.isFile()) {
                System.out.println("---- EXPLORAR FICHERO ----");
                System.out.println("[NOMBRE] " + archivo.getName());
                System.out.println("[RUTA ABSOLUTA] " + archivo.getAbsolutePath());
                System.out.println("[TAMAÑO EN BYTES] " + archivo.length());
                return;
            }

            if (archivo.isDirectory()) {

                System.out.println("---- EXPLORAR DIRECTORIO ----");
                System.out.println("[RUTA] " + path);

                File[] contenido = archivo.listFiles();

                if (contenido == null) {
                    System.out.println(ColoresConsola.ROJO + "[ERROR] No se puede listar el directorio."
                            + ColoresConsola.RESET);
                    return;
                }

                if (contenido.length == 0) {
                    System.out.println(ColoresConsola.AMARILLO + "[AVISO] El directorio esta vacio."
                            + ColoresConsola.RESET);
                    return;
                }

                int totalFicheros = 0;
                int totalDirectorios = 0;

                for (File file : contenido) {

                    if (file.isFile()) {
                        System.out.println("[FICHERO] " + file.getName() + " " + file.length() + " bytes");
                        totalFicheros++;

                    } else if (file.isDirectory()) {
                        System.out.println("[DIR] " + file.getName());
                        totalDirectorios++;
                    }
                }

                System.out.println("Total ficheros: " + totalFicheros + " | Directorios: " + totalDirectorios);
            }

        } catch (IllegalArgumentException e) {

            System.out.println(ColoresConsola.ROJO + "[ERROR] " + e.getMessage() + ColoresConsola.RESET);

        } catch (SecurityException e) {

            System.out.println(ColoresConsola.ROJO + "[ERROR] Permisos denegados: " + e.getMessage()
                    + ColoresConsola.RESET);

        } catch (Exception e) {

            System.out.println(ColoresConsola.ROJO + "[ERROR] Error inesperado: " + e.getMessage()
                    + ColoresConsola.RESET);
        }
    }

}

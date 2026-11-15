package es.iesisidra.datastock.servicio;

import java.io.File;

import es.iesisidra.datastock.util.ColoresConsola;

public class Ruta {
     /**
     * metodo explorarRuta que nos permite ver toda la informacion subyacente de
     * una ruta dada por el usuario
     *
     * @param ruta la ruta que se va a explorar
     */
    public void explorarRuta(String ruta) {

        try {
            if (ruta == null || ruta.trim().isEmpty()) { // la ruta que me dan no debe estar vacia
                System.out.println(ColoresConsola.ROJO
                        + "ERROR: Se debe proporcionar una ruta válida (el texto está vacio)" + ColoresConsola.RESET);
                return; // no seguimos
            }
            // en caso de que la entrada no este vacia

            File archivo = new File(ruta.trim());

            // Primer caso: la ruta no existe
            if (!archivo.exists()) {
                System.out.println(ColoresConsola.ROJO + "ERROR: Se debe proporcionar una ruta válida (no existe)"
                        + ColoresConsola.RESET);
                return; // no seguimos
            }

            // comprobamos si es un fichero y si es el caso mostramos su informacion
            if (archivo.isFile()) {
                System.out.println("---- EXPLORAR FICHERO ----");
                System.out.println("[NOMBRE] " + archivo.getName());
                System.out.println("[RUTA ABSOLUTA] " + archivo.getAbsolutePath());
                System.out.println("[TAMAÑO EN BYTES] " + archivo.length());
                return;
            }

            if (archivo.isDirectory()) {
                System.out.println("---- EXPLORAR DIRECTORIO ----");
                System.out.println("[RUTA] " + ruta);
                File[] contenido = archivo.listFiles(); // obtenemos el contenido del directorio y nos preparamos para
                // recorrerlo en caso de que no este vacio

                if (contenido == null) { // es nulo o no contiene nada
                    System.out.println(ColoresConsola.AMARILLO + "AVISO: NO HAY CONTENIDO EN LA RUTA PROPORCIONADA"
                            + ColoresConsola.RESET);
                    return; // no seguimos
                }

                // inicializo y sumare mientras recorro
                int totalFicheros = 0;
                int totalDirectorios = 0;

                for (File file : contenido) {
                    if (file.isFile()) {
                        System.out.println("[FICHERO] " + file.getName() + " " + file.length() + " bytes");
                        totalFicheros++; // sumo
                    } else if (file.isDirectory()) {
                        System.out.println("[DIR] " + file.getName());
                        totalDirectorios++; // sumo
                    }
                }

                System.out.println("Total ficheros: " + totalFicheros + " | Directorios: " + totalDirectorios);

            }

        } catch (SecurityException se) { // el sistema operativo bloquea por permisos
            System.out.println(ColoresConsola.ROJO + "Error de acceso: Permisos denegados por el sistema "
                    + se.getMessage() + ColoresConsola.RESET);

        } catch (Exception e) { // cualquier otra excepcion inesperada
            System.out.println(ColoresConsola.ROJO + "Error inesperado al explorar la ruta " + e.getMessage()
                    + ColoresConsola.RESET);
        }

    }
    
}

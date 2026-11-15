package es.iesisidra.datastock.servicio;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import es.iesisidra.datastock.config.ConexionBD;
import es.iesisidra.datastock.modelo.Producto;

/**
 * Componente reutilizable que permite consultar un producto en MySQL utilizando
 * JDBC.
 *
 * No solicita datos ni muestra mensajes por consola.
 */
public class ConsultaProductoJdbc {

    // Guardo los nombres asociados al producto consultado.
    private String nombreCategoria;
    private String nombreProveedor;

    /**
     * Busca un producto en MySQL mediante su codigo.
     *
     * @param codigo codigo del producto que queremos buscar
     * @return Producto encontrado o null si no existe
     * @throws SQLException si ocurre un error en la consulta JDBC
     * @throws IllegalArgumentException si el codigo esta vacio
     */
    public Producto buscarPorCodigo(String codigo) throws SQLException {

        // Reinicio los datos de una posible consulta anterior.
        nombreCategoria = null;
        nombreProveedor = null;

        // Compruebo que el codigo no este vacio.
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El codigo no puede estar vacio.");
        }

        // Normalizo el codigo.
        String codigoLimpio = codigo.trim().toUpperCase();

        // Consulta SQL con los nombres de categoria y proveedor.
        // Consulta SQL con los nombres de categoria y proveedor.
        String sqlConsulta = "SELECT p.codigo, p.nombre, p.precio, p.stock, p.categoria_id, p.proveedor_id, c.nombre AS nombre_categoria, pr.nombre AS nombre_proveedor FROM producto p INNER JOIN categoria c ON p.categoria_id = c.id INNER JOIN proveedor pr ON p.proveedor_id = pr.id WHERE p.codigo = ?";

        // Abro la conexion y preparo la consulta.
        try (Connection con = ConexionBD.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sqlConsulta)) {

            // Sustituyo la interrogacion por el codigo.
            ps.setString(1, codigoLimpio);

            // Ejecuto la consulta y obtengo sus resultados.
            try (ResultSet rs = ps.executeQuery()) {

                // Si no hay resultados, devuelvo null.
                if (!rs.next()) {
                    return null;
                }

                // Recupero los datos del producto.
                String codigoProducto = rs.getString("codigo");
                String nombre = rs.getString("nombre");
                BigDecimal precio = rs.getBigDecimal("precio");
                int stock = rs.getInt("stock");
                int idCategoria = rs.getInt("categoria_id");
                int idProveedor = rs.getInt("proveedor_id");

                // Recupero los nombres de categoria y proveedor.
                nombreCategoria = rs.getString("nombre_categoria");
                nombreProveedor = rs.getString("nombre_proveedor");

                // Creo el producto con los datos obtenidos.
                Producto p = new Producto(
                        codigoProducto,
                        nombre,
                        precio,
                        stock,
                        idCategoria,
                        idProveedor);

                return p;
            }
        }
    }

    /**
     * Devuelve el nombre de la categoria del ultimo producto encontrado.
     *
     * @return nombre de la categoria o null
     */
    public String getNombreCategoria() {
        return nombreCategoria;
    }

    /**
     * Devuelve el nombre del proveedor del ultimo producto encontrado.
     *
     * @return nombre del proveedor o null
     */
    public String getNombreProveedor() {
        return nombreProveedor;
    }
}

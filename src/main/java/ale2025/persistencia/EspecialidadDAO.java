package ale2025.persistencia;

import java.sql.PreparedStatement; // Clase para ejecutar consultas SQL preparadas, previniendo inyecciones SQL.
import java.sql.ResultSet;          // Interfaz para representar el resultado de una consulta SQL.
import java.sql.SQLException;       // Clase para manejar errores relacionados con la base de datos SQL.
import java.util.ArrayList;

import ale2025.dominio.Especialidad; // Clase que representa la entidad de especialidad en el dominio de la aplicación.

public class EspecialidadDAO {
    private ConnectionManager conn; // Objeto para gestionar la conexión con la base de datos.
    private PreparedStatement ps;   // Objeto para ejecutar consultas SQL preparadas.
    private ResultSet rs;           // Objeto para almacenar el resultado de una consulta SQL.

    public EspecialidadDAO() {
        conn = ConnectionManager.getInstance();
    }

    /**
     * Crea una nueva especialidad en la base de datos.
     *
     * @param especialidad El objeto especialidad que contiene la información de la nueva especialidad a crear.
     * Se espera que el objeto Especialidad tenga los campos 'nombre' y 'descripcion' correctamente establecidos. El campo 'id' sera generado automáticamente por la base de datos.
     * @return El objeto Especialidad recién creado, incluyendo el ID generado por la base de datos, o null si ocurre algún error durante la creación.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos durante la creación de la especialidad.
     */
    public Especialidad create(Especialidad especialidad) throws SQLException {
        Especialidad res = null; // Variable para almacenar la especialidad creada que se retornará.
        PreparedStatement localPs = null; // Usar una variable local para el PreparedStatement del try
        try {
            // Preparar la sentencia SQL para la inserción de una nueva especialidad.
            // Se especifica que se retornen las claves generadas automáticamente.
            localPs = conn.connect().prepareStatement(
                    "INSERT INTO " +
                            "Especialidades (nombre, descripcion)" +
                            "VALUES (?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS
            );
            // Establecer los valores de los parámetros en la sentencia preparada.
            localPs.setString(1, especialidad.getNombre()); // Asignar el nombre de la especialidad.
            localPs.setString(2, especialidad.getDescripcion()); // Asignar la descripción de la especialidad.
            // Ejecutar la sentencia de inserción y obtener el número de filas afectadas.
            int affectedRows = localPs.executeUpdate();
            // Verificar si la inserción fue exitosa (al menos una fila afectada).
            if (affectedRows != 0) {
                // Obtener las claves generadas automáticamente por la base de datos (en este caso, el ID).
                ResultSet generatedKeys = localPs.getGeneratedKeys();
                // Mover el cursor al primer resultado (si existe).
                if (generatedKeys.next()) {
                    // Obtener el ID generado. Generalmente la primera columna contiene la clave primaria.
                    int idGenerado = generatedKeys.getInt(1);
                    // Recuperar la especialidad completa utilizando el ID generado.
                    res = getById(idGenerado);
                } else {
                    // Lanzar una excepción si la creación de la especialidad falló y no se obtuvo un ID.
                    throw new SQLException("Creating especialidad failed, no ID obtained.");
                }
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al crear la especialidad: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (localPs != null) {
                try {
                    localPs.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en create (EspecialidadDAO): " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return res; // Retornar la especialidad creada (con su ID asignado) o null si hubo un error.
    }

    /**
     * Actualiza la información de una especialidad existente en la base de datos.
     *
     * @param especialidad El objeto Especialidad que contiene la información actualizada de la especialidad.
     * Se requiere que el objeto Especialidad tenga los campos 'id', 'nombre' y 'descripcion'
     * correctamente establecidos para realizar la actualización.
     * @return true si la actualización de la especialidad fue exitosa (al menos una fila afectada),
     * false en caso contrario.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la actualización de la especialidad.
     */
    public boolean update(Especialidad especialidad) throws SQLException {
        boolean res = false; // Variable para indicar si la actualización fue exitosa.
        try {
            // Preparar la sentencia SQL para actualizar la información de una especialidad.
            ps = conn.connect().prepareStatement(
                    "UPDATE Especialidades " +
                            "SET nombre = ?, descripcion = ? " +
                            "WHERE id = ?"
            );
            // Establecer los valores de los parámetros en la sentencia preparada.
            ps.setString(1, especialidad.getNombre()); // Asignar el nuevo nombre de la especialidad.
            ps.setString(2, especialidad.getDescripcion()); // Asignar la nueva descripción de la especialidad.
            ps.setInt(3, especialidad.getId()); // Establecer la condición WHERE para identificar la especialidad a actualizar por su ID.
            // Ejecutar la sentencia de actualización y verificar si se afectó alguna fila.
            if (ps.executeUpdate() > 0) {
                res = true; // Si executeUpdate() retorna un valor mayor que 0, significa que la actualización fue exitosa.
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al modificar la especialidad: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (ps != null) {
                try {
                    ps.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en update (EspecialidadDAO): " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return res; // Retornar el resultado de la operación de actualización.
    }

    /**
     * Elimina una especialidad de la base de datos basándose en su ID.
     *
     * @param especialidad El objeto Especialidad que contiene el ID de la especialidad a eliminar.
     * Se requiere que el objeto Especialidad tenga el campo 'id' correctamente establecido.
     * @return true si la eliminación de la especialidad fue exitosa (al menos una fila afectada),
     * false en caso contrario.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la eliminación de la especialidad.
     */
    public boolean delete(Especialidad especialidad) throws SQLException {
        boolean res = false; // Variable para indicar si la eliminación fue exitosa.
        try {
            // Preparar la sentencia SQL para eliminar una especialidad por su ID.
            ps = conn.connect().prepareStatement(
                    "DELETE FROM Especialidades WHERE id = ?"
            );
            // Establecer el valor del parámetro en la sentencia preparada (el ID de la especialidad a eliminar).
            ps.setInt(1, especialidad.getId());
            // Ejecutar la sentencia de eliminación y verificar si se afectó alguna fila.
            if (ps.executeUpdate() > 0) {
                res = true; // Si executeUpdate() retorna un valor mayor que 0, significa que la eliminación fue exitosa.
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al eliminar la especialidad: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (ps != null) {
                try {
                    ps.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en delete (EspecialidadDAO): " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return res; // Retornar el resultado de la operación de eliminación.
    }

    /**
     * Busca especialidades en la base de datos cuyo nombre contenga la cadena de búsqueda proporcionada.
     * La búsqueda se realiza de forma parcial, es decir, si el nombre de la especialidad contiene
     * la cadena de búsqueda (ignorando mayúsculas y minúsculas), será incluida en los resultados.
     *
     * @param nombre La cadena de texto a buscar dentro de los nombres de las especialidades.
     * @return Un ArrayList de objetos Especialidad que coinciden con el criterio de búsqueda.
     * Retorna una lista vacía si no se encuentran especialidades con el nombre especificado.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la búsqueda de especialidades.
     */
    public ArrayList<Especialidad> search(String nombre) throws SQLException {
        ArrayList<Especialidad> records = new ArrayList<>(); // Lista para almacenar las especialidades encontradas.
        try {
            // Preparar la sentencia SQL para buscar especialidades por nombre (usando LIKE para búsqueda parcial).
            ps = conn.connect().prepareStatement("SELECT id, nombre, descripcion " +
                    "FROM Especialidades " +
                    "WHERE nombre LIKE ?");
            // Establecer el valor del parámetro en la sentencia preparada.
            // El '%' al inicio y al final permiten la búsqueda de la cadena 'nombre' en cualquier parte del nombre de la especialidad.
            ps.setString(1, "%" + nombre + "%");
            // Ejecutar la consulta SQL y obtener el resultado.
            rs = ps.executeQuery();
            // Iterar a través de cada fila del resultado.
            while (rs.next()) {
                // Crear un nuevo objeto Especialidad para cada registro encontrado.
                Especialidad especialidad = new Especialidad();
                // Asignar los valores de las columnas a los atributos del objeto Especialidad.
                especialidad.setId(rs.getInt(1)); // Obtener el ID de la especialidad.
                especialidad.setNombre(rs.getString(2)); // Obtener el nombre de la especialidad.
                especialidad.setDescripcion(rs.getString(3)); // Obtener la descripción de la especialidad.
                // Agregar el objeto Especialidad a la lista de resultados.
                records.add(especialidad);
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al buscar especialidades: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (ps != null) {
                try {
                    ps.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en search (EspecialidadDAO): " + e.getMessage());
                }
            }
            if (rs != null) {
                try {
                    rs.close(); // Cerrar el conjunto de resultados para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar ResultSet en search (EspecialidadDAO): " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return records; // Retornar la lista de especialidades encontradas.
    }

    /**
     * Obtiene una especialidad de la base de datos basado en su ID.
     *
     * @param id El ID de la especialidad que se desea obtener.
     * @return Un objeto Especialidad si se encuentra una especialidad con el ID especificado,
     * null si no se encuentra ninguna especialidad con ese ID.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la obtención de la especialidad.
     */
    public Especialidad getById(int id) throws SQLException {
        Especialidad especialidad = null; // Inicializar a null si no se encuentra la especialidad.
        try {
            // Preparar la sentencia SQL para seleccionar una especialidad por su ID.
            ps = conn.connect().prepareStatement("SELECT id, nombre, descripcion " +
                    "FROM Especialidades " +
                    "WHERE id = ?");
            // Establecer el valor del parámetro en la sentencia preparada (el ID a buscar).
            ps.setInt(1, id);
            // Ejecutar la consulta SQL y obtener el resultado.
            rs = ps.executeQuery();
            // Verificar si se encontró algún registro.
            if (rs.next()) {
                // Si se encontró una especialidad, crear el objeto Especialidad y asignar los valores de las columnas.
                especialidad = new Especialidad();
                especialidad.setId(rs.getInt(1)); // Obtener el ID de la especialidad.
                especialidad.setNombre(rs.getString(2)); // Obtener el nombre de la especialidad.
                especialidad.setDescripcion(rs.getString(3)); // Obtener la descripción de la especialidad.
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al obtener una especialidad por id: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (ps != null) {
                try {
                    ps.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en getById (EspecialidadDAO): " + e.getMessage());
                }
            }
            if (rs != null) {
                try {
                    rs.close(); // Cerrar el conjunto de resultados para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar ResultSet en getById (EspecialidadDAO): " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return especialidad; // Retornar el objeto Especialidad encontrado o null si no existe.
    }
}
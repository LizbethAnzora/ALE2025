package ale2025.persistencia;

import java.sql.PreparedStatement; // Clase para ejecutar consultas SQL preparadas, previniendo inyecciones SQL.
import java.sql.ResultSet;          // Interfaz para representar el resultado de una consulta SQL.
import java.sql.SQLException;       // Clase para manejar errores relacionados con la base de datos SQL.
import java.util.ArrayList;

import ale2025.dominio.Medico; // Clase que representa la entidad de médico en el dominio de la aplicación.

public class MedicoDAO {
    private ConnectionManager conn; // Objeto para gestionar la conexión con la base de datos.
    private PreparedStatement ps;   // Objeto para ejecutar consultas SQL preparadas.
    private ResultSet rs;           // Objeto para almacenar el resultado de una consulta SQL.

    public MedicoDAO() {
        conn = ConnectionManager.getInstance();
    }

    /**
     * Crea un nuevo médico en la base de datos.
     *
     * @param medico El objeto medico que contiene la información del nuevo médico a crear.
     * Se espera que el objeto Medico tenga los campos 'nombreCompleto', 'especialidadId' y 'sueldo' correctamente establecidos. El campo 'id' sera generado automáticamente por la base de datos.
     * @return El objeto Medico recién creado, incluyendo el ID generado por la base de datos, o null si ocurre algún error durante la creación.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos durante la creación del médico.
     */
    public Medico create(Medico medico) throws SQLException {
        Medico res = null; // Variable para almacenar el médico creado que se retornará.
        PreparedStatement localPs = null; // Usar una variable local para el PreparedStatement del try
        try {
            // Preparar la sentencia SQL para la inserción de un nuevo médico.
            // Se especifica que se retornen las claves generadas automáticamente.
            localPs = conn.connect().prepareStatement(
                    "INSERT INTO " +
                            "Medicos (nombreCompleto, especialidadId, sueldo)" +
                            "VALUES (?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS
            );
            // Establecer los valores de los parámetros en la sentencia preparada.
            localPs.setString(1, medico.getNombreCompleto()); // Asignar el nombre completo del médico.
            localPs.setInt(2, medico.getEspecialidadId()); // Asignar el ID de la especialidad del médico.
            localPs.setDouble(3, medico.getSueldo()); // Asignar el sueldo del médico.
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
                    // Recuperar el médico completo utilizando el ID generado.
                    res = getById(idGenerado);
                } else {
                    // Lanzar una excepción si la creación del médico falló y no se obtuvo un ID.
                    throw new SQLException("Creating medico failed, no ID obtained.");
                }
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al crear el médico: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (localPs != null) {
                try {
                    localPs.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en create (MedicoDAO): " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return res; // Retornar el médico creado (con su ID asignado) o null si hubo un error.
    }

    /**
     * Actualiza la información de un médico existente en la base de datos.
     *
     * @param medico El objeto Medico que contiene la información actualizada del médico.
     * Se requiere que el objeto Medico tenga los campos 'id', 'nombreCompleto', 'especialidadId' y 'sueldo'
     * correctamente establecidos para realizar la actualización.
     * @return true si la actualización del médico fue exitosa (al menos una fila afectada),
     * false en caso contrario.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la actualización del médico.
     */
    public boolean update(Medico medico) throws SQLException {
        boolean res = false; // Variable para indicar si la actualización fue exitosa.
        try {
            // Preparar la sentencia SQL para actualizar la información de un médico.
            ps = conn.connect().prepareStatement(
                    "UPDATE Medicos " +
                            "SET nombreCompleto = ?, especialidadId = ?, sueldo = ? " +
                            "WHERE id = ?"
            );
            // Establecer los valores de los parámetros en la sentencia preparada.
            ps.setString(1, medico.getNombreCompleto()); // Asignar el nuevo nombre completo del médico.
            ps.setInt(2, medico.getEspecialidadId()); // Asignar el nuevo ID de especialidad del médico.
            ps.setDouble(3, medico.getSueldo()); // Asignar el nuevo sueldo del médico.
            ps.setInt(4, medico.getId()); // Establecer la condición WHERE para identificar al médico a actualizar por su ID.
            // Ejecutar la sentencia de actualización y verificar si se afectó alguna fila.
            if (ps.executeUpdate() > 0) {
                res = true; // Si executeUpdate() retorna un valor mayor que 0, significa que la actualización fue exitosa.
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al modificar el médico: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (ps != null) {
                try {
                    ps.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en update (MedicoDAO): " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return res; // Retornar el resultado de la operación de actualización.
    }

    /**
     * Elimina un médico de la base de datos basándose en su ID.
     *
     * @param medico El objeto Medico que contiene el ID del médico a eliminar.
     * Se requiere que el objeto Medico tenga el campo 'id' correctamente establecido.
     * @return true si la eliminación del médico fue exitosa (al menos una fila afectada),
     * false en caso contrario.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la eliminación del médico.
     */
    public boolean delete(Medico medico) throws SQLException {
        boolean res = false; // Variable para indicar si la eliminación fue exitosa.
        try {
            // Preparar la sentencia SQL para eliminar un médico por su ID.
            ps = conn.connect().prepareStatement(
                    "DELETE FROM Medicos WHERE id = ?"
            );
            // Establecer el valor del parámetro en la sentencia preparada (el ID del médico a eliminar).
            ps.setInt(1, medico.getId());
            // Ejecutar la sentencia de eliminación y verificar si se afectó alguna fila.
            if (ps.executeUpdate() > 0) {
                res = true; // Si executeUpdate() retorna un valor mayor que 0, significa que la eliminación fue exitosa.
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al eliminar el médico: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (ps != null) {
                try {
                    ps.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en delete (MedicoDAO): " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return res; // Retornar el resultado de la operación de eliminación.
    }

    /**
     * Busca médicos en la base de datos cuyo nombre completo contenga la cadena de búsqueda proporcionada.
     * La búsqueda se realiza de forma parcial, es decir, si el nombre del médico contiene
     * la cadena de búsqueda (ignorando mayúsculas y minúsculas), será incluido en los resultados.
     *
     * @param nombreCompleto La cadena de texto a buscar dentro de los nombres completos de los médicos.
     * @return Un ArrayList de objetos Medico que coinciden con el criterio de búsqueda.
     * Retorna una lista vacía si no se encuentran médicos con el nombre especificado.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la búsqueda de médicos.
     */
    public ArrayList<Medico> search(String nombreCompleto) throws SQLException {
        ArrayList<Medico> records = new ArrayList<>(); // Lista para almacenar los médicos encontrados.
        try {
            // Preparar la sentencia SQL para buscar médicos por nombre completo (usando LIKE para búsqueda parcial).
            ps = conn.connect().prepareStatement("SELECT id, nombreCompleto, especialidadId, sueldo " +
                    "FROM Medicos " +
                    "WHERE nombreCompleto LIKE ?");
            // Establecer el valor del parámetro en la sentencia preparada.
            // El '%' al inicio y al final permiten la búsqueda de la cadena 'nombreCompleto' en cualquier parte del nombre del médico.
            ps.setString(1, "%" + nombreCompleto + "%");
            // Ejecutar la consulta SQL y obtener el resultado.
            rs = ps.executeQuery();
            // Iterar a través de cada fila del resultado.
            while (rs.next()) {
                // Crear un nuevo objeto Medico para cada registro encontrado.
                Medico medico = new Medico();
                // Asignar los valores de las columnas a los atributos del objeto Medico.
                medico.setId(rs.getInt(1)); // Obtener el ID del médico.
                medico.setNombreCompleto(rs.getString(2)); // Obtener el nombre completo del médico.
                medico.setEspecialidadId(rs.getInt(3)); // Obtener el ID de la especialidad del médico.
                medico.setSueldo(rs.getDouble(4)); // Obtener el sueldo del médico.
                // Agregar el objeto Medico a la lista de resultados.
                records.add(medico);
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al buscar médicos: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (ps != null) {
                try {
                    ps.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en search (MedicoDAO): " + e.getMessage());
                }
            }
            if (rs != null) {
                try {
                    rs.close(); // Cerrar el conjunto de resultados para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar ResultSet en search (MedicoDAO): " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return records; // Retornar la lista de médicos encontrados.
    }

    /**
     * Obtiene un médico de la base de datos basado en su ID.
     *
     * @param id El ID del médico que se desea obtener.
     * @return Un objeto Medico si se encuentra un médico con el ID especificado,
     * null si no se encuentra ningún médico con ese ID.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la obtención del médico.
     */
    public Medico getById(int id) throws SQLException {
        Medico medico = null; // Inicializar a null si no se encuentra el médico.
        try {
            // Preparar la sentencia SQL para seleccionar un médico por su ID.
            ps = conn.connect().prepareStatement("SELECT id, nombreCompleto, especialidadId, sueldo " +
                    "FROM Medicos " +
                    "WHERE id = ?");
            // Establecer el valor del parámetro en la sentencia preparada (el ID a buscar).
            ps.setInt(1, id);
            // Ejecutar la consulta SQL y obtener el resultado.
            rs = ps.executeQuery();
            // Verificar si se encontró algún registro.
            if (rs.next()) {
                // Si se encontró un médico, crear el objeto Medico y asignar los valores de las columnas.
                medico = new Medico();
                medico.setId(rs.getInt(1)); // Obtener el ID del médico.
                medico.setNombreCompleto(rs.getString(2)); // Obtener el nombre completo del médico.
                medico.setEspecialidadId(rs.getInt(3)); // Obtener el ID de la especialidad del médico.
                medico.setSueldo(rs.getDouble(4)); // Obtener el sueldo del médico.
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al obtener un médico por id: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (ps != null) {
                try {
                    ps.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en getById (MedicoDAO): " + e.getMessage());
                }
            }
            if (rs != null) {
                try {
                    rs.close(); // Cerrar el conjunto de resultados para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar ResultSet en getById (MedicoDAO): " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return medico; // Retornar el objeto Medico encontrado o null si no existe.
    }
}
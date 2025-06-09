package ale2025.persistencia;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import ale2025.dominio.Paciente;

public class PacienteDAO {
    private ConnectionManager conn;
    private PreparedStatement ps;
    private ResultSet rs;

    public PacienteDAO() {
        conn = ConnectionManager.getInstance();
    }

    /**
     * Crea un nuevo paciente en la base de datos.
     *
     * @param paciente El objeto paciente que contiene la información del nuevo paciente a crear.
     * Se espera que el objeto Paciente tenga los campos 'nombreCompleto', 'telefono','fechaNacimiento' correctamente establecidos. El campo 'id' sera generado automáticamente por la base de datos.
     * @return El objeto Paciente recién creado, incluyendo el ID generado por la base de datos, o null si ocurre algún error durante la creación.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos durante la creación del paciente.
     */
    public Paciente create(Paciente paciente) throws SQLException {
        Paciente res = null; // Variable para almacenar el paciente creado que se retornará.
        PreparedStatement localPs = null; // Usar una variable local para el PreparedStatement del try
        try {
            // Preparar la sentencia SQL para la inserción de un nuevo paciente.
            // Se especifica que se retornen las claves generadas automáticamente.
            localPs = conn.connect().prepareStatement(
                    "INSERT INTO " +
                            "Pacientes (nombreCompleto, telefono, fechaNacimiento)" +
                            "VALUES (?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS
            );
            // Establecer los valores de los parámetros en la sentencia preparada.
            localPs.setString(1, paciente.getNombreCompleto()); // Asignar el nombre del paciente.
            localPs.setString(2, paciente.getTelefono()); // Asignar el teléfono del paciente.
            localPs.setDate(3, paciente.getFechaNacimiento()); // Asignar la fecha de nacimiento del paciente.
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
                    // Recuperar el paciente completo utilizando el ID generado.
                    res = getById(idGenerado);
                } else {
                    // Lanzar una excepción si la creación del paciente falló y no se obtuvo un ID.
                    throw new SQLException("Creating patient failed, no ID obtained.");
                }
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al crear el paciente: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (localPs != null) {
                try {
                    localPs.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    // Log the error or handle it appropriately
                    System.err.println("Error al cerrar PreparedStatement en create: " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return res; // Retornar el paciente creado (con su ID asignado) o null si hubo un error.
    }

    /**
     * Actualiza la información de un paciente existente en la base de datos.
     *
     * @param paciente El objeto Paciente que contiene la información actualizada del paciente.
     * Se requiere que el objeto Paciente tenga los campos 'id', 'nombreCompleto', 'telefono' y 'fechaNacimiento'
     * correctamente establecidos para realizar la actualización.
     * @return true si la actualización del paciente fue exitosa (al menos una fila afectada),
     * false en caso contrario.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la actualización del paciente.
     */
    public boolean update(Paciente paciente) throws SQLException {
        boolean res = false; // Variable para indicar si la actualización fue exitosa.
        try {
            // Preparar la sentencia SQL para actualizar la información de un paciente.
            ps = conn.connect().prepareStatement(
                    "UPDATE Pacientes " +
                            "SET nombreCompleto = ?, telefono= ?, fechaNacimiento = ? " +
                            "WHERE id = ?"
            );
            // Establecer los valores de los parámetros en la sentencia preparada.
            ps.setString(1, paciente.getNombreCompleto()); // Asignar el nuevo nombre del paciente.
            ps.setString(2, paciente.getTelefono()); // Asignar el nuevo teléfono del paciente.
            ps.setDate(3, paciente.getFechaNacimiento()); // Asignar la nueva fecha de nacimiento del paciente.
            ps.setInt(4, paciente.getId()); // Establecer la condición WHERE para identificar el paciente a actualizar por su ID.
            // Ejecutar la sentencia de actualización y verificar si se afectó alguna fila.
            if (ps.executeUpdate() > 0) {
                res = true; // Si executeUpdate() retorna un valor mayor que 0, significa que la actualización fue exitosa.
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al modificar el paciente: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (ps != null) {
                try {
                    ps.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en update: " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return res; // Retornar el resultado de la operación de actualización.
    }

    /**
     * Elimina un paciente de la base de datos basándose en su ID.
     *
     * @param paciente El objeto Paciente que contiene el ID del paciente a eliminar.
     * Se requiere que el objeto Paciente tenga el campo 'id' correctamente establecido.
     * @return true si la eliminación del paciente fue exitosa (al menos una fila afectada),
     * false en caso contrario.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la eliminación del paciente.
     */
    public boolean delete(Paciente paciente) throws SQLException {
        boolean res = false; // Variable para indicar si la eliminación fue exitosa.
        try {
            // Preparar la sentencia SQL para eliminar un paciente por su ID.
            ps = conn.connect().prepareStatement(
                    "DELETE FROM Pacientes WHERE id = ?"
            );
            // Establecer el valor del parámetro en la sentencia preparada (el ID del paciente a eliminar).
            ps.setInt(1, paciente.getId());
            // Ejecutar la sentencia de eliminación y verificar si se afectó alguna fila.
            if (ps.executeUpdate() > 0) {
                res = true; // Si executeUpdate() retorna un valor mayor que 0, significa que la eliminación fue exitosa.
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al eliminar el paciente: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (ps != null) {
                try {
                    ps.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en delete: " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return res; // Retornar el resultado de la operación de eliminación.
    }

    /**
     * Busca pacientes en la base de datos cuyo nombre contenga la cadena de búsqueda proporcionada.
     * La búsqueda se realiza de forma parcial, es decir, si el nombre del paciente contiene
     * la cadena de búsqueda (ignorando mayúsculas y minúsculas), será incluido en los resultados.
     *
     * @param nombreCompleto La cadena de texto a buscar dentro de los nombres de los pacientes.
     * @return Un ArrayList de objetos Paciente que coinciden con el criterio de búsqueda.
     * Retorna una lista vacía si no se encuentran pacientes con el nombre especificado.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la búsqueda de pacientes.
     */
    public ArrayList<Paciente> search(String nombreCompleto) throws SQLException {
        ArrayList<Paciente> records = new ArrayList<>(); // Lista para almacenar los pacientes encontrados.
        try {
            // Preparar la sentencia SQL para buscar pacientes por nombre (usando LIKE para búsqueda parcial).
            ps = conn.connect().prepareStatement("SELECT id, nombreCompleto, telefono, fechaNacimiento " +
                    "FROM Pacientes " +
                    "WHERE nombreCompleto LIKE ?");
            // Establecer el valor del parámetro en la sentencia preparada.
            // El '%' al inicio y al final permiten la búsqueda de la cadena 'nombreCompleto' en cualquier parte del nombre del paciente.
            ps.setString(1, "%" + nombreCompleto + "%");
            // Ejecutar la consulta SQL y obtener el resultado.
            rs = ps.executeQuery();
            // Iterar a través de cada fila del resultado.
            while (rs.next()) {
                // Crear un nuevo objeto Paciente para cada registro encontrado.
                Paciente paciente = new Paciente();
                // Asignar los valores de las columnas a los atributos del objeto Paciente.
                paciente.setId(rs.getInt(1)); // Obtener el ID del paciente.
                paciente.setNombreCompleto(rs.getString(2)); // Obtener el nombre del paciente.
                paciente.setTelefono(rs.getString(3)); // Obtener el teléfono del paciente.
                paciente.setFechaNacimiento(rs.getDate(4)); // Obtener la fecha de nacimiento del paciente.
                // Agregar el objeto Paciente a la lista de resultados.
                records.add(paciente);
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al buscar pacientes: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (ps != null) {
                try {
                    ps.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en search: " + e.getMessage());
                }
            }
            if (rs != null) {
                try {
                    rs.close(); // Cerrar el conjunto de resultados para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar ResultSet en search: " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return records; // Retornar la lista de pacientes encontrados.
    }

    /**
     * Obtiene un paciente de la base de datos basado en su ID.
     *
     * @param id El ID del paciente que se desea obtener.
     * @return Un objeto Paciente si se encuentra un paciente con el ID especificado,
     * null si no se encuentra ningún paciente con ese ID.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la obtención del paciente.
     */
    public Paciente getById(int id) throws SQLException {
        Paciente paciente = null; // Inicializar a null si no se encuentra el paciente.
        try {
            // Preparar la sentencia SQL para seleccionar un paciente por su ID.
            ps = conn.connect().prepareStatement("SELECT id, nombreCompleto, telefono, fechaNacimiento " +
                    "FROM Pacientes " +
                    "WHERE id = ?");
            // Establecer el valor del parámetro en la sentencia preparada (el ID a buscar).
            ps.setInt(1, id);
            // Ejecutar la consulta SQL y obtener el resultado.
            rs = ps.executeQuery();
            // Verificar si se encontró algún registro.
            if (rs.next()) {
                // Si se encontró un paciente, crear el objeto Paciente y asignar los valores de las columnas.
                paciente = new Paciente();
                paciente.setId(rs.getInt(1)); // Obtener el ID del paciente.
                paciente.setNombreCompleto(rs.getString(2)); // Obtener el nombre del paciente.
                paciente.setTelefono(rs.getString(3)); // Obtener el teléfono del paciente.
                paciente.setFechaNacimiento(rs.getDate(4)); // Obtener la fecha de nacimiento del paciente.
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al obtener un paciente por id: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (ps != null) {
                try {
                    ps.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en getById: " + e.getMessage());
                }
            }
            if (rs != null) {
                try {
                    rs.close(); // Cerrar el conjunto de resultados para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar ResultSet en getById: " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return paciente; // Retornar el objeto Paciente encontrado o null si no existe.
    }
}
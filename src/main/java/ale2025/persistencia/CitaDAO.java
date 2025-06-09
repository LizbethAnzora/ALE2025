package ale2025.persistencia;

import java.sql.PreparedStatement; // Clase para ejecutar consultas SQL preparadas, previniendo inyecciones SQL.
import java.sql.ResultSet;          // Interfaz para representar el resultado de una consulta SQL.
import java.sql.SQLException;       // Clase para manejar errores relacionados con la base de datos SQL.
import java.sql.Date;               // Necesario para el tipo DATE de SQL
import java.util.ArrayList;

import ale2025.dominio.Cita; // Clase que representa la entidad de cita en el dominio de la aplicación.

public class CitaDAO {
    private ConnectionManager conn; // Objeto para gestionar la conexión con la base de datos.
    private PreparedStatement ps;   // Objeto para ejecutar consultas SQL preparadas.
    private ResultSet rs;           // Objeto para almacenar el resultado de una consulta SQL.

    public CitaDAO() {
        conn = ConnectionManager.getInstance();
    }

    /**
     * Crea una nueva cita en la base de datos.
     *
     * @param cita El objeto cita que contiene la información de la nueva cita a crear.
     * Se espera que el objeto Cita tenga los campos 'pacienteId', 'medicoId', 'fechaCita' y 'costoConsulta' correctamente establecidos. El campo 'id' sera generado automáticamente por la base de datos.
     * @return El objeto Cita recién creado, incluyendo el ID generado por la base de datos, o null si ocurre algún error durante la creación.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos durante la creación de la cita.
     */
    public Cita create(Cita cita) throws SQLException {
        Cita res = null; // Variable para almacenar la cita creada que se retornará.
        PreparedStatement localPs = null; // Usar una variable local para el PreparedStatement del try
        try {
            // Preparar la sentencia SQL para la inserción de una nueva cita.
            // Se especifica que se retornen las claves generadas automáticamente.
            localPs = conn.connect().prepareStatement(
                    "INSERT INTO " +
                            "Citas (pacienteId, medicoId, fechaCita, costoConsulta)" +
                            "VALUES (?, ?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS
            );
            // Establecer los valores de los parámetros en la sentencia preparada.
            localPs.setInt(1, cita.getPacienteId()); // Asignar el ID del paciente.
            localPs.setInt(2, cita.getMedicoId()); // Asignar el ID del médico.
            localPs.setDate(3, cita.getFechaCita()); // Asignar la fecha de la cita.
            localPs.setDouble(4, cita.getCostoConsulta()); // Asignar el costo de la consulta.
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
                    // Recuperar la cita completa utilizando el ID generado.
                    res = getById(idGenerado);
                } else {
                    // Lanzar una excepción si la creación de la cita falló y no se obtuvo un ID.
                    throw new SQLException("Creating cita failed, no ID obtained.");
                }
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al crear la cita: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (localPs != null) {
                try {
                    localPs.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en create (CitaDAO): " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return res; // Retornar la cita creada (con su ID asignado) o null si hubo un error.
    }

    /**
     * Actualiza la información de una cita existente en la base de datos.
     *
     * @param cita El objeto Cita que contiene la información actualizada de la cita.
     * Se requiere que el objeto Cita tenga los campos 'id', 'pacienteId', 'medicoId', 'fechaCita' y 'costoConsulta'
     * correctamente establecidos para realizar la actualización.
     * @return true si la actualización de la cita fue exitosa (al menos una fila afectada),
     * false en caso contrario.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la actualización de la cita.
     */
    public boolean update(Cita cita) throws SQLException {
        boolean res = false; // Variable para indicar si la actualización fue exitosa.
        try {
            // Preparar la sentencia SQL para actualizar la información de una cita.
            ps = conn.connect().prepareStatement(
                    "UPDATE Citas " +
                            "SET pacienteId = ?, medicoId = ?, fechaCita = ?, costoConsulta = ? " +
                            "WHERE id = ?"
            );
            // Establecer los valores de los parámetros en la sentencia preparada.
            ps.setInt(1, cita.getPacienteId()); // Asignar el nuevo ID del paciente.
            ps.setInt(2, cita.getMedicoId()); // Asignar el nuevo ID del médico.
            ps.setDate(3, cita.getFechaCita()); // Asignar la nueva fecha de la cita.
            ps.setDouble(4, cita.getCostoConsulta()); // Asignar el nuevo costo de la consulta.
            ps.setInt(5, cita.getId()); // Establecer la condición WHERE para identificar a la cita a actualizar por su ID.
            // Ejecutar la sentencia de actualización y verificar si se afectó alguna fila.
            if (ps.executeUpdate() > 0) {
                res = true; // Si executeUpdate() retorna un valor mayor que 0, significa que la actualización fue exitosa.
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al modificar la cita: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (ps != null) {
                try {
                    ps.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en update (CitaDAO): " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return res; // Retornar el resultado de la operación de actualización.
    }

    /**
     * Elimina una cita de la base de datos basándose en su ID.
     *
     * @param cita El objeto Cita que contiene el ID de la cita a eliminar.
     * Se requiere que el objeto Cita tenga el campo 'id' correctamente establecido.
     * @return true si la eliminación de la cita fue exitosa (al menos una fila afectada),
     * false en caso contrario.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la eliminación de la cita.
     */
    public boolean delete(Cita cita) throws SQLException {
        boolean res = false; // Variable para indicar si la eliminación fue exitosa.
        try {
            // Preparar la sentencia SQL para eliminar una cita por su ID.
            ps = conn.connect().prepareStatement(
                    "DELETE FROM Citas WHERE id = ?"
            );
            // Establecer el valor del parámetro en la sentencia preparada (el ID de la cita a eliminar).
            ps.setInt(1, cita.getId());
            // Ejecutar la sentencia de eliminación y verificar si se afectó alguna fila.
            if (ps.executeUpdate() > 0) {
                res = true; // Si executeUpdate() retorna un valor mayor que 0, significa que la eliminación fue exitosa.
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al eliminar la cita: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (ps != null) {
                try {
                    ps.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en delete (CitaDAO): " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return res; // Retornar el resultado de la operación de eliminación.
    }

    /**
     * Busca citas en la base de datos cuya fecha de cita coincida con la cadena de búsqueda proporcionada.
     * La búsqueda se realiza de forma parcial o por fecha exacta dependiendo de la implementación de la base de datos.
     *
     * @param fechaCitaString La cadena de texto de la fecha a buscar (ej. "2024-12-31").
     * @return Un ArrayList de objetos Cita que coinciden con el criterio de búsqueda.
     * Retorna una lista vacía si no se encuentran citas con la fecha especificada.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la búsqueda de citas.
     */
    public ArrayList<Cita> search(String fechaCitaString) throws SQLException {
        ArrayList<Cita> records = new ArrayList<>(); // Lista para almacenar las citas encontradas.
        try {
            // Preparar la sentencia SQL para buscar citas por fecha.
            // En SQL Server, se puede usar CONVERT para comparar la parte de la fecha o DATE_FORMAT.
            // Para una búsqueda exacta por fecha, se usa el operador =
            ps = conn.connect().prepareStatement("SELECT id, pacienteId, medicoId, fechaCita, costoConsulta " +
                    "FROM Citas " +
                    "WHERE fechaCita = CONVERT(DATE, ?)"); // CONVERT(DATE, ?) para comparar solo la fecha
            // Establecer el valor del parámetro en la sentencia preparada.
            ps.setString(1, fechaCitaString); // Se asume que fechaCitaString viene en formato 'YYYY-MM-DD'
            // Ejecutar la consulta SQL y obtener el resultado.
            rs = ps.executeQuery();
            // Iterar a través de cada fila del resultado.
            while (rs.next()) {
                // Crear un nuevo objeto Cita para cada registro encontrado.
                Cita cita = new Cita();
                // Asignar los valores de las columnas a los atributos del objeto Cita.
                cita.setId(rs.getInt(1)); // Obtener el ID de la cita.
                cita.setPacienteId(rs.getInt(2)); // Obtener el ID del paciente asociado.
                cita.setMedicoId(rs.getInt(3)); // Obtener el ID del médico asociado.
                cita.setFechaCita(rs.getDate(4)); // Obtener la fecha de la cita.
                cita.setCostoConsulta(rs.getDouble(5)); // Obtener el costo de la consulta.
                // Agregar el objeto Cita a la lista de resultados.
                records.add(cita);
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al buscar citas: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (ps != null) {
                try {
                    ps.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en search (CitaDAO): " + e.getMessage());
                }
            }
            if (rs != null) {
                try {
                    rs.close(); // Cerrar el conjunto de resultados para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar ResultSet en search (CitaDAO): " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return records; // Retornar la lista de citas encontradas.
    }

    /**
     * Obtiene una cita de la base de datos basado en su ID.
     *
     * @param id El ID de la cita que se desea obtener.
     * @return Un objeto Cita si se encuentra una cita con el ID especificado,
     * null si no se encuentra ninguna cita con ese ID.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la obtención de la cita.
     */
    public Cita getById(int id) throws SQLException {
        Cita cita = null; // Inicializar a null si no se encuentra la cita.
        try {
            // Preparar la sentencia SQL para seleccionar una cita por su ID.
            ps = conn.connect().prepareStatement("SELECT id, pacienteId, medicoId, fechaCita, costoConsulta " +
                    "FROM Citas " +
                    "WHERE id = ?");
            // Establecer el valor del parámetro en la sentencia preparada (el ID a buscar).
            ps.setInt(1, id);
            // Ejecutar la consulta SQL y obtener el resultado.
            rs = ps.executeQuery();
            // Verificar si se encontró algún registro.
            if (rs.next()) {
                // Si se encontró una cita, crear el objeto Cita y asignar los valores de las columnas.
                cita = new Cita();
                cita.setId(rs.getInt(1)); // Obtener el ID de la cita.
                cita.setPacienteId(rs.getInt(2)); // Obtener el ID del paciente asociado.
                cita.setMedicoId(rs.getInt(3)); // Obtener el ID del médico asociado.
                cita.setFechaCita(rs.getDate(4)); // Obtener la fecha de la cita.
                cita.setCostoConsulta(rs.getDouble(5)); // Obtener el costo de la consulta.
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al obtener una cita por id: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (ps != null) {
                try {
                    ps.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en getById (CitaDAO): " + e.getMessage());
                }
            }
            if (rs != null) {
                try {
                    rs.close(); // Cerrar el conjunto de resultados para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar ResultSet en getById (CitaDAO): " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return cita; // Retornar el objeto Cita encontrado o null si no existe.
    }
}
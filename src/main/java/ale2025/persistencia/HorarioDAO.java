package ale2025.persistencia;

import java.sql.PreparedStatement; // Clase para ejecutar consultas SQL preparadas, previniendo inyecciones SQL.
import java.sql.ResultSet;          // Interfaz para representar el resultado de una consulta SQL.
import java.sql.SQLException;       // Clase para manejar errores relacionados con la base de datos SQL.
import java.sql.Time;               // Necesario para los tipos TIME de SQL
import java.util.ArrayList;

import ale2025.dominio.Horario; // Clase que representa la entidad de horario en el dominio de la aplicación.

public class HorarioDAO {
    private ConnectionManager conn; // Objeto para gestionar la conexión con la base de datos.
    private PreparedStatement ps;   // Objeto para ejecutar consultas SQL preparadas.
    private ResultSet rs;           // Objeto para almacenar el resultado de una consulta SQL.

    public HorarioDAO() {
        conn = ConnectionManager.getInstance();
    }

    /**
     * Crea un nuevo horario en la base de datos.
     *
     * @param horario El objeto horario que contiene la información del nuevo horario a crear.
     * Se espera que el objeto Horario tenga los campos 'medicoId', 'diaSemana', 'horaInicio' y 'horaFin' correctamente establecidos. El campo 'id' sera generado automáticamente por la base de datos.
     * @return El objeto Horario recién creado, incluyendo el ID generado por la base de datos, o null si ocurre algún error durante la creación.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos durante la creación del horario.
     */
    public Horario create(Horario horario) throws SQLException {
        Horario res = null; // Variable para almacenar el horario creado que se retornará.
        PreparedStatement localPs = null; // Usar una variable local para el PreparedStatement del try
        try {
            // Preparar la sentencia SQL para la inserción de un nuevo horario.
            // Se especifica que se retornen las claves generadas automáticamente.
            localPs = conn.connect().prepareStatement(
                    "INSERT INTO " +
                            "Horarios (medicoId, diaSemana, horaInicio, horaFin)" +
                            "VALUES (?, ?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS
            );
            // Establecer los valores de los parámetros en la sentencia preparada.
            localPs.setInt(1, horario.getMedicoId()); // Asignar el ID del médico.
            localPs.setString(2, horario.getDiaSemana()); // Asignar el día de la semana.
            localPs.setTime(3, horario.getHoraInicio()); // Asignar la hora de inicio.
            localPs.setTime(4, horario.getHoraFin()); // Asignar la hora de fin.
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
                    // Recuperar el horario completo utilizando el ID generado.
                    res = getById(idGenerado);
                } else {
                    // Lanzar una excepción si la creación del horario falló y no se obtuvo un ID.
                    throw new SQLException("Creating horario failed, no ID obtained.");
                }
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al crear el horario: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (localPs != null) {
                try {
                    localPs.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en create (HorarioDAO): " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return res; // Retornar el horario creado (con su ID asignado) o null si hubo un error.
    }

    /**
     * Actualiza la información de un horario existente en la base de datos.
     *
     * @param horario El objeto Horario que contiene la información actualizada del horario.
     * Se requiere que el objeto Horario tenga los campos 'id', 'medicoId', 'diaSemana', 'horaInicio' y 'horaFin'
     * correctamente establecidos para realizar la actualización.
     * @return true si la actualización del horario fue exitosa (al menos una fila afectada),
     * false en caso contrario.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la actualización del horario.
     */
    public boolean update(Horario horario) throws SQLException {
        boolean res = false; // Variable para indicar si la actualización fue exitosa.
        try {
            // Preparar la sentencia SQL para actualizar la información de un horario.
            ps = conn.connect().prepareStatement(
                    "UPDATE Horarios " +
                            "SET medicoId = ?, diaSemana = ?, horaInicio = ?, horaFin = ? " +
                            "WHERE id = ?"
            );
            // Establecer los valores de los parámetros en la sentencia preparada.
            ps.setInt(1, horario.getMedicoId()); // Asignar el nuevo ID del médico.
            ps.setString(2, horario.getDiaSemana()); // Asignar el nuevo día de la semana.
            ps.setTime(3, horario.getHoraInicio()); // Asignar la nueva hora de inicio.
            ps.setTime(4, horario.getHoraFin()); // Asignar la nueva hora de fin.
            ps.setInt(5, horario.getId()); // Establecer la condición WHERE para identificar al horario a actualizar por su ID.
            // Ejecutar la sentencia de actualización y verificar si se afectó alguna fila.
            if (ps.executeUpdate() > 0) {
                res = true; // Si executeUpdate() retorna un valor mayor que 0, significa que la actualización fue exitosa.
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al modificar el horario: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (ps != null) {
                try {
                    ps.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en update (HorarioDAO): " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return res; // Retornar el resultado de la operación de actualización.
    }

    /**
     * Elimina un horario de la base de datos basándose en su ID.
     *
     * @param horario El objeto Horario que contiene el ID del horario a eliminar.
     * Se requiere que el objeto Horario tenga el campo 'id' correctamente establecido.
     * @return true si la eliminación del horario fue exitosa (al menos una fila afectada),
     * false en caso contrario.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la eliminación del horario.
     */
    public boolean delete(Horario horario) throws SQLException {
        boolean res = false; // Variable para indicar si la eliminación fue exitosa.
        try {
            // Preparar la sentencia SQL para eliminar un horario por su ID.
            ps = conn.connect().prepareStatement(
                    "DELETE FROM Horarios WHERE id = ?"
            );
            // Establecer el valor del parámetro en la sentencia preparada (el ID del horario a eliminar).
            ps.setInt(1, horario.getId());
            // Ejecutar la sentencia de eliminación y verificar si se afectó alguna fila.
            if (ps.executeUpdate() > 0) {
                res = true; // Si executeUpdate() retorna un valor mayor que 0, significa que la eliminación fue exitosa.
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al eliminar el horario: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (ps != null) {
                try {
                    ps.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en delete (HorarioDAO): " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return res; // Retornar el resultado de la operación de eliminación.
    }

    /**
     * Busca horarios en la base de datos cuyo día de la semana contenga la cadena de búsqueda proporcionada.
     * La búsqueda se realiza de forma parcial, es decir, si el día de la semana del horario contiene
     * la cadena de búsqueda (ignorando mayúsculas y minúsculas), será incluido en los resultados.
     *
     * @param diaSemana La cadena de texto a buscar dentro de los días de la semana de los horarios.
     * @return Un ArrayList de objetos Horario que coinciden con el criterio de búsqueda.
     * Retorna una lista vacía si no se encuentran horarios con el día de la semana especificado.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la búsqueda de horarios.
     */
    public ArrayList<Horario> search(String diaSemana) throws SQLException {
        ArrayList<Horario> records = new ArrayList<>(); // Lista para almacenar los horarios encontrados.
        try {
            // Preparar la sentencia SQL para buscar horarios por día de la semana (usando LIKE para búsqueda parcial).
            ps = conn.connect().prepareStatement("SELECT id, medicoId, diaSemana, horaInicio, horaFin " +
                    "FROM Horarios " +
                    "WHERE diaSemana LIKE ?");
            // Establecer el valor del parámetro en la sentencia preparada.
            // El '%' al inicio y al final permiten la búsqueda de la cadena 'diaSemana' en cualquier parte del día de la semana.
            ps.setString(1, "%" + diaSemana + "%");
            // Ejecutar la consulta SQL y obtener el resultado.
            rs = ps.executeQuery();
            // Iterar a través de cada fila del resultado.
            while (rs.next()) {
                // Crear un nuevo objeto Horario para cada registro encontrado.
                Horario horario = new Horario();
                // Asignar los valores de las columnas a los atributos del objeto Horario.
                horario.setId(rs.getInt(1)); // Obtener el ID del horario.
                horario.setMedicoId(rs.getInt(2)); // Obtener el ID del médico asociado.
                horario.setDiaSemana(rs.getString(3)); // Obtener el día de la semana.
                horario.setHoraInicio(rs.getTime(4)); // Obtener la hora de inicio.
                horario.setHoraFin(rs.getTime(5)); // Obtener la hora de fin.
                // Agregar el objeto Horario a la lista de resultados.
                records.add(horario);
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al buscar horarios: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (ps != null) {
                try {
                    ps.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en search (HorarioDAO): " + e.getMessage());
                }
            }
            if (rs != null) {
                try {
                    rs.close(); // Cerrar el conjunto de resultados para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar ResultSet en search (HorarioDAO): " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return records; // Retornar la lista de horarios encontrados.
    }

    /**
     * Obtiene un horario de la base de datos basado en su ID.
     *
     * @param id El ID del horario que se desea obtener.
     * @return Un objeto Horario si se encuentra un horario con el ID especificado,
     * null si no se encuentra ningún horario con ese ID.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la obtención del horario.
     */
    public Horario getById(int id) throws SQLException {
        Horario horario = null; // Inicializar a null si no se encuentra el horario.
        try {
            // Preparar la sentencia SQL para seleccionar un horario por su ID.
            ps = conn.connect().prepareStatement("SELECT id, medicoId, diaSemana, horaInicio, horaFin " +
                    "FROM Horarios " +
                    "WHERE id = ?");
            // Establecer el valor del parámetro en la sentencia preparada (el ID a buscar).
            ps.setInt(1, id);
            // Ejecutar la consulta SQL y obtener el resultado.
            rs = ps.executeQuery();
            // Verificar si se encontró algún registro.
            if (rs.next()) {
                // Si se encontró un horario, crear el objeto Horario y asignar los valores de las columnas.
                horario = new Horario();
                horario.setId(rs.getInt(1)); // Obtener el ID del horario.
                horario.setMedicoId(rs.getInt(2)); // Obtener el ID del médico asociado.
                horario.setDiaSemana(rs.getString(3)); // Obtener el día de la semana.
                horario.setHoraInicio(rs.getTime(4)); // Obtener la hora de inicio.
                horario.setHoraFin(rs.getTime(5)); // Obtener la hora de fin.
            }
        } catch (SQLException ex) {
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al obtener un horario por id: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            if (ps != null) {
                try {
                    ps.close(); // Cerrar la sentencia preparada para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar PreparedStatement en getById (HorarioDAO): " + e.getMessage());
                }
            }
            if (rs != null) {
                try {
                    rs.close(); // Cerrar el conjunto de resultados para liberar recursos.
                } catch (SQLException e) {
                    System.err.println("Error al cerrar ResultSet en getById (HorarioDAO): " + e.getMessage());
                }
            }
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return horario; // Retornar el objeto Horario encontrado o null si no existe.
    }
}

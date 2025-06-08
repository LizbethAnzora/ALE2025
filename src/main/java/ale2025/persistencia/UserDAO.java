package ale2025.persistencia;

import java.sql.PreparedStatement; // Clase para ejecutar consultas SQL preparadas, previniendo inyecciones SQL.
import java.sql.ResultSet;        // Interfaz para representar el resultado de una consulta SQL.
import java.sql.SQLException;     // Clase para manejar errores relacionados con la base de datos SQL.
import java.util.ArrayList;       // Clase para crear listas dinámicas de objetos.
import ale2025.dominio.User;        // Clase que representa la entidad de usuario en el dominio de la aplicación.
import ale2025.utils.PasswordHasher; // Clase utilitaria para el manejo seguro de contraseñas (hash, verificación).
public class UserDAO {
    private ConnectionManager conn; // Objeto para gestionar la conexión con la base de datos.
    private PreparedStatement ps;   // Objeto para ejecutar consultas SQL preparadas.
    private ResultSet rs;           // Objeto para almacenar el resultado de una consulta SQL.

    public UserDAO(){
        conn = ConnectionManager.getInstance();
    }
    /**
     * Crea un nuevo usuario en la base de datos.
     *
     * @param user El objeto User que contiene la información del nuevo usuario a crear.
     * Se espera que el objeto User tenga los campos 'name', 'passwordHash',
     * 'email' y 'status' correctamente establecidos. El campo 'id' será
     * generado automáticamente por la base de datos.
     * @return El objeto User recién creado, incluyendo el ID generado por la base de datos,
     * o null si ocurre algún error durante la creación.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la creación del usuario.
     */
    public User create(User user) throws SQLException {
        User res = null; // Variable para almacenar el usuario creado que se retornará.
        try{
            // Preparar la sentencia SQL para la inserción de un nuevo usuario.
            // Se especifica que se retornen las claves generadas automáticamente.
            PreparedStatement ps = conn.connect().prepareStatement(
                    "INSERT INTO " +
                            "Users (name, passwordHash, email, status)" +
                            "VALUES (?, ?, ?, ?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS
            );
            // Establecer los valores de los parámetros en la sentencia preparada.
            ps.setString(1, user.getName()); // Asignar el nombre del usuario.
            ps.setString(2, PasswordHasher.hashPassword(user.getPasswordHash())); // Hashear la contraseña antes de guardarla.
            ps.setString(3, user.getEmail()); // Asignar el correo electrónico del usuario.
            ps.setByte(4, user.getStatus());   // Asignar el estado del usuario.
            // Ejecutar la sentencia de inserción y obtener el número de filas afectadas.
            int affectedRows = ps.executeUpdate();
            // Verificar si la inserción fue exitosa (al menos una fila afectada).
            if (affectedRows != 0) {
                // Obtener las claves generadas automáticamente por la base de datos (en este caso, el ID).
                ResultSet  generatedKeys = ps.getGeneratedKeys();
                // Mover el cursor al primer resultado (si existe).
                if (generatedKeys.next()) {
                    // Obtener el ID generado. Generalmente la primera columna contiene la clave primaria.
                    int idGenerado= generatedKeys.getInt(1);
                    // Recuperar el usuario completo utilizando el ID generado.
                    res = getById(idGenerado);
                } else {
                    // Lanzar una excepción si la creación del usuario falló y no se obtuvo un ID.
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
            ps.close(); // Cerrar la sentencia preparada para liberar recursos.
        }catch (SQLException ex){
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al crear el usuario: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            ps = null;         // Establecer la sentencia preparada a null.
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return res; // Retornar el usuario creado (con su ID asignado) o null si hubo un error.
    }
    /**
     * Actualiza la información de un usuario existente en la base de datos.
     *
     * @param user El objeto User que contiene la información actualizada del usuario.
     * Se requiere que el objeto User tenga los campos 'id', 'name', 'email' y 'status'
     * correctamente establecidos para realizar la actualización.
     * @return true si la actualización del usuario fue exitosa (al menos una fila afectada),
     * false en caso contrario.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la actualización del usuario.
     */
    public boolean update(User user) throws SQLException{
        boolean res = false; // Variable para indicar si la actualización fue exitosa.
        try{
            // Preparar la sentencia SQL para actualizar la información de un usuario.
            ps = conn.connect().prepareStatement(
                    "UPDATE Users " +
                            "SET name = ?, email = ?, status = ? " +
                            "WHERE id = ?"
            );
            // Establecer los valores de los parámetros en la sentencia preparada.
            ps.setString(1, user.getName());  // Asignar el nuevo nombre del usuario.
            ps.setString(2, user.getEmail()); // Asignar el nuevo correo electrónico del usuario.
            ps.setByte(3, user.getStatus());    // Asignar el nuevo estado del usuario.
            ps.setInt(4, user.getId());       // Establecer la condición WHERE para identificar el usuario a actualizar por su ID.
            // Ejecutar la sentencia de actualización y verificar si se afectó alguna fila.
            if(ps.executeUpdate() > 0){
                res = true; // Si executeUpdate() retorna un valor mayor que 0, significa que la actualización fue exitosa.
            }
            ps.close(); // Cerrar la sentencia preparada para liberar recursos.
        }catch (SQLException ex){
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al modificar el usuario: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            ps = null;         // Establecer la sentencia preparada a null.
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return res; // Retornar el resultado de la operación de actualización.
    }
    /**
     * Elimina un usuario de la base de datos basándose en su ID.
     *
     * @param user El objeto User que contiene el ID del usuario a eliminar.
     * Se requiere que el objeto User tenga el campo 'id' correctamente establecido.
     * @return true si la eliminación del usuario fue exitosa (al menos una fila afectada),
     * false en caso contrario.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la eliminación del usuario.
     */
    public boolean delete(User user) throws SQLException{
        boolean res = false; // Variable para indicar si la eliminación fue exitosa.
        try{
            // Preparar la sentencia SQL para eliminar un usuario por su ID.
            ps = conn.connect().prepareStatement(
                    "DELETE FROM Users WHERE id = ?"
            );
            // Establecer el valor del parámetro en la sentencia preparada (el ID del usuario a eliminar).
            ps.setInt(1, user.getId());
            // Ejecutar la sentencia de eliminación y verificar si se afectó alguna fila.
            if(ps.executeUpdate() > 0){
                res = true; // Si executeUpdate() retorna un valor mayor que 0, significa que la eliminación fue exitosa.
            }
            ps.close(); // Cerrar la sentencia preparada para liberar recursos.
        }catch (SQLException ex){
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al eliminar el usuario: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            ps = null;         // Establecer la sentencia preparada a null.
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return res; // Retornar el resultado de la operación de eliminación.
    }
    /**
     * Busca usuarios en la base de datos cuyo nombre contenga la cadena de búsqueda proporcionada.
     * La búsqueda se realiza de forma parcial, es decir, si el nombre del usuario contiene
     * la cadena de búsqueda (ignorando mayúsculas y minúsculas), será incluido en los resultados.
     *
     * @param name La cadena de texto a buscar dentro de los nombres de los usuarios.
     * @return Un ArrayList de objetos User que coinciden con el criterio de búsqueda.
     * Retorna una lista vacía si no se encuentran usuarios con el nombre especificado.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la búsqueda de usuarios.
     */
    public ArrayList<User> search(String name) throws SQLException{
        ArrayList<User> records  = new ArrayList<>(); // Lista para almacenar los usuarios encontrados.
        try {
            // Preparar la sentencia SQL para buscar usuarios por nombre (usando LIKE para búsqueda parcial).
            ps = conn.connect().prepareStatement("SELECT id, name, email, status " +
                    "FROM Users " +
                    "WHERE name LIKE ?");
            // Establecer el valor del parámetro en la sentencia preparada.
            // El '%' al inicio y al final permiten la búsqueda de la cadena 'name' en cualquier parte del nombre del usuario.
            ps.setString(1, "%" + name + "%");
            // Ejecutar la consulta SQL y obtener el resultado.
            rs = ps.executeQuery();
            // Iterar a través de cada fila del resultado.
            while (rs.next()){
                // Crear un nuevo objeto User para cada registro encontrado.
                User user = new User();
                // Asignar los valores de las columnas a los atributos del objeto User.
                user.setId(rs.getInt(1));       // Obtener el ID del usuario.
                user.setName(rs.getString(2));   // Obtener el nombre del usuario.
                user.setEmail(rs.getString(3));  // Obtener el correo electrónico del usuario.
                user.setStatus(rs.getByte(4));    // Obtener el estado del usuario.
                // Agregar el objeto User a la lista de resultados.
                records.add(user);
            }
            ps.close(); // Cerrar la sentencia preparada para liberar recursos.
            rs.close(); // Cerrar el conjunto de resultados para liberar recursos.
        } catch (SQLException ex){
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al buscar usuarios: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            ps = null;         // Establecer la sentencia preparada a null.
            rs = null;         // Establecer el conjunto de resultados a null.
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return records; // Retornar la lista de usuarios encontrados.
    }
    /**
     * Obtiene un usuario de la base de datos basado en su ID.
     *
     * @param id El ID del usuario que se desea obtener.
     * @return Un objeto User si se encuentra un usuario con el ID especificado,
     * null si no se encuentra ningún usuario con ese ID.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la obtención del usuario.
     */
    public User getById(int id) throws SQLException{
        User user  = new User(); // Inicializar un objeto User que se retornará.
        try {
            // Preparar la sentencia SQL para seleccionar un usuario por su ID.
            ps = conn.connect().prepareStatement("SELECT id, name, email, status " +
                    "FROM Users " +
                    "WHERE id = ?");
            // Establecer el valor del parámetro en la sentencia preparada (el ID a buscar).
            ps.setInt(1, id);
            // Ejecutar la consulta SQL y obtener el resultado.
            rs = ps.executeQuery();
            // Verificar si se encontró algún registro.
            if (rs.next()) {
                // Si se encontró un usuario, asignar los valores de las columnas al objeto User.
                user.setId(rs.getInt(1));       // Obtener el ID del usuario.
                user.setName(rs.getString(2));   // Obtener el nombre del usuario.
                user.setEmail(rs.getString(3));  // Obtener el correo electrónico del usuario.
                user.setStatus(rs.getByte(4));    // Obtener el estado del usuario.
            } else {
                // Si no se encontró ningún usuario con el ID especificado, establecer el objeto User a null.
                user = null;
            }
            ps.close(); // Cerrar la sentencia preparada para liberar recursos.
            rs.close(); // Cerrar el conjunto de resultados para liberar recursos.
        } catch (SQLException ex){
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al obtener un usuario por id: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            ps = null;         // Establecer la sentencia preparada a null.
            rs = null;         // Establecer el conjunto de resultados a null.
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return user; // Retornar el objeto User encontrado o null si no existe.
    }
    /**
     * Autentica a un usuario en la base de datos verificando su correo electrónico,
     * contraseña (comparando el hash) y estado (activo).
     *
     * @param user El objeto User que contiene el correo electrónico y la contraseña
     * del usuario que se intenta autenticar. Se espera que estos campos estén
     * correctamente establecidos.
     * @return Un objeto User si la autenticación es exitosa (se encuentra un usuario
     * con las credenciales proporcionadas y su estado es activo), o null si la
     * autenticación falla. El objeto User retornado contendrá el ID, nombre,
     * correo electrónico y estado del usuario autenticado.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante el proceso de autenticación.
     */
    public User authenticate(User user) throws SQLException{
        User userAutenticate = new User(); // Inicializar un objeto User para almacenar el usuario autenticado.
        try {
            // Preparar la sentencia SQL para seleccionar un usuario por su correo electrónico,
            // contraseña hasheada y estado activo (status = 1).
            ps = conn.connect().prepareStatement("SELECT id, name, email, status " +
                    "FROM Users " +
                    "WHERE email = ? AND passwordHash = ? AND status = 1");
            // Establecer los valores de los parámetros en la sentencia preparada.
            ps.setString(1, user.getEmail()); // Asignar el correo electrónico del usuario a autenticar.
            ps.setString(2, PasswordHasher.hashPassword(user.getPasswordHash())); // Hashear la contraseña proporcionada para compararla con la almacenada.
            rs = ps.executeQuery(); // Ejecutar la consulta SQL y obtener el resultado.
            // Verificar si se encontró un registro que coincida con las credenciales y el estado.
            if (rs.next()) {
                // Si se encontró un usuario, asignar los valores de las columnas al objeto userAutenticate.
                userAutenticate.setId(rs.getInt(1));       // Obtener el ID del usuario autenticado.
                userAutenticate.setName(rs.getString(2));   // Obtener el nombre del usuario autenticado.
                userAutenticate.setEmail(rs.getString(3));  // Obtener el correo electrónico del usuario autenticado.
                userAutenticate.setStatus(rs.getByte(4));    // Obtener el estado del usuario autenticado.
            } else {
                // Si no se encontraron coincidencias, la autenticación falla y se establece userAutenticate a null.
                userAutenticate = null;
            }
            ps.close(); // Cerrar la sentencia preparada para liberar recursos.
            rs.close(); // Cerrar el conjunto de resultados para liberar recursos.
        } catch (SQLException ex){
            // Capturar cualquier excepción SQL que ocurra durante el proceso de autenticación.
            throw new SQLException("Error al autenticar un usuario por id: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            ps = null;         // Establecer la sentencia preparada a null.
            rs = null;         // Establecer el conjunto de resultados a null.
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return userAutenticate; // Retornar el objeto User autenticado o null si la autenticación falló.
    }
    /**
     * Actualiza la contraseña de un usuario existente en la base de datos.
     * La nueva contraseña proporcionada se hashea antes de ser almacenada.
     *
     * @param user El objeto User que contiene el ID del usuario cuya contraseña se
     * actualizará y la nueva contraseña (sin hashear) en el campo 'passwordHash'.
     * Se requiere que los campos 'id' y 'passwordHash' del objeto User estén
     * correctamente establecidos.
     * @return true si la actualización de la contraseña fue exitosa (al menos una
     * fila afectada), false en caso contrario.
     * @throws SQLException Si ocurre un error al interactuar con la base de datos
     * durante la actualización de la contraseña.
     */
    public boolean updatePassword(User user) throws SQLException{
        boolean res = false; // Variable para indicar si la actualización de la contraseña fue exitosa.
        try{
            // Preparar la sentencia SQL para actualizar solo la columna 'passwordHash' de un usuario.
            ps = conn.connect().prepareStatement(
                    "UPDATE Users " +
                            "SET passwordHash = ? " +
                            "WHERE id = ?"
            );
            // Hashear la nueva contraseña proporcionada antes de establecerla en la consulta.
            ps.setString(1, PasswordHasher.hashPassword(user.getPasswordHash()));
            // Establecer el ID del usuario cuya contraseña se va a actualizar en la cláusula WHERE.
            ps.setInt(2, user.getId());
            // Ejecutar la sentencia de actualización y verificar si se afectó alguna fila.
            if(ps.executeUpdate() > 0){
                res = true; // Si executeUpdate() retorna un valor mayor que 0, la actualización fue exitosa.
            }
            ps.close(); // Cerrar la sentencia preparada para liberar recursos.
        }catch (SQLException ex){
            // Capturar cualquier excepción SQL que ocurra durante el proceso.
            throw new SQLException("Error al modificar el password del usuario: " + ex.getMessage(), ex);
        } finally {
            // Bloque finally para asegurar que los recursos se liberen.
            ps = null;         // Establecer la sentencia preparada a null.
            conn.disconnect(); // Desconectar de la base de datos.
        }
        return res; // Retornar el resultado de la operación de actualización de la contraseña.
    }
}


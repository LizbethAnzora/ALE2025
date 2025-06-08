package ale2025.persistencia;

import org.junit.jupiter.api.BeforeEach; // Anotación para indicar que el método se ejecuta antes de cada método de prueba.
import org.junit.jupiter.api.Test;       // Anotación para indicar que el método es un caso de prueba.
import ale2025.dominio.Usuario;                // Clase que representa la entidad de usuario utilizada en las pruebas.

import java.util.ArrayList;              // Clase para crear listas dinámicas de objetos, utilizada en algunas pruebas.
import java.util.Random;
import ale2025.utils.PasswordHasher;// Clase para generar números aleatorios, útil para crear datos de prueba.

import java.sql.SQLException;             // Clase para manejar excepciones relacionadas con la base de datos, aunque no se espera que las pruebas unitarias interactúen directamente con ella (idealmente se mockean las dependencias).

import static org.junit.jupiter.api.Assertions.*; // Importación estática de métodos de aserción de JUnit 5 para verificar el comportamiento esperado en las pruebas.



class UsuarioDAOTest {
    private UsuarioDAO usuarioDAO; // Instancia de la clase UserDAO que se va a probar.

    @BeforeEach
    void setUp() {
        // Método que se ejecuta antes de cada método de prueba (@Test).
        // Su propósito es inicializar el entorno de prueba, en este caso,
        // creando una nueva instancia de UserDAO para cada prueba.
        usuarioDAO = new UsuarioDAO();
    }

    private Usuario create(Usuario usuario) throws SQLException {
        // Llama al método 'create' del UsuarioDAO para persistir el usuario en la base de datos (simulada).
        Usuario res = usuarioDAO.create(usuario);

        // Realiza aserciones para verificar que la creación del usuario fue exitosa
        // y que los datos del usuario retornado coinciden con los datos originales.
        assertNotNull(res, "El usuario creado no debería ser nulo."); // Verifica que el objeto retornado no sea nulo.
        assertEquals(usuario.getNombre(), res.getNombre(), "El nombre del usuario creado debe ser igual al original.");
        assertEquals(usuario.getCorreoElectronico(), res.getCorreoElectronico(), "El correoElectronico del usuario creado debe ser igual al original.");
//        assertEquals(usuario.getEstado(), res.getEstado(), "El status del usuario creado debe ser igual al original.");

        // Retorna el objeto User creado (tal como lo devolvió el UserDAO).
        return res;
    }

    private void update(Usuario usuario) throws SQLException {
        // Modifica los atributos del objeto User para simular una actualización.
        usuario.setNombre(usuario.getNombre() + "_u"); // Añade "_u" al final del nombre.
        usuario.setCorreoElectronico("u" + usuario.getCorreoElectronico()); // Añade "u" al inicio del correoElectronico.
        usuario.setEstado((byte) 1);             // Establece el status a 1.

        // Llama al método 'update' del UsuarioDAO para actualizar el usuario en la base de datos (simulada).
        boolean res = usuarioDAO.update(usuario);

        // Realiza una aserción para verificar que la actualización fue exitosa.
        assertTrue(res, "La actualización del usuario debería ser exitosa.");

        // Llama al método 'getById' para verificar que los cambios se persistieron correctamente.
        // Aunque el método 'getById' ya tiene sus propias aserciones, esta llamada adicional
        // ayuda a asegurar que la actualización realmente tuvo efecto en la capa de datos.
        getById(usuario);
    }

    private void getById(Usuario usuario) throws SQLException {
        // Llama al método 'getById' del UsuarioDAO para obtener un usuario por su ID.
        Usuario res = usuarioDAO.getById(usuario.getId());

        // Realiza aserciones para verificar que el usuario obtenido coincide
        // con el usuario original (o el usuario modificado en pruebas de actualización).
        assertNotNull(res, "El usuario obtenido por ID no debería ser nulo.");
        assertEquals(usuario.getId(), res.getId(), "El ID del usuario obtenido debe ser igual al original.");
        assertEquals(usuario.getNombre(), res.getNombre(), "El nombre del usuario obtenido debe ser igual al esperado.");
        assertEquals(usuario.getCorreoElectronico(), res.getCorreoElectronico(), "El correoElectronico del usuario obtenido debe ser igual al esperado.");
        assertEquals(usuario.getEstado(), res.getEstado(), "El status del usuario obtenido debe ser igual al esperado.");
    }

    private void search(Usuario usuario) throws SQLException {
        // Llama al método 'search' del UsuarioDAO para buscar usuarios por nombre.
        ArrayList<Usuario> usuarios = usuarioDAO.search(usuario.getNombre());
        boolean find = false; // Variable para rastrear si se encontró un usuario con el nombre buscado.

        // Itera sobre la lista de usuarios devuelta por la búsqueda.
        for (Usuario userItem : usuarios) {
            // Verifica si el nombre de cada usuario encontrado contiene la cadena de búsqueda.
            if (userItem.getNombre().contains(usuario.getNombre())) {
                find = true; // Si se encuentra una coincidencia, se establece 'find' a true.
            } else {
                find = false; // Si un nombre no contiene la cadena de búsqueda, se establece 'find' a false.
                break;      // Se sale del bucle, ya que se esperaba que todos los resultados contuvieran la cadena.
            }
        }

        // Realiza una aserción para verificar que todos los usuarios con el nombre buscado fue encontrado.
        assertTrue(find, "el nombre buscado no fue encontrado : " + usuario.getNombre());
    }

    private void delete(Usuario usuario) throws SQLException {
        // Llama al método 'delete' del UsuarioDAO para eliminar un usuario por su ID.
        boolean res = usuarioDAO.delete(usuario);

        // Realiza una aserción para verificar que la eliminación fue exitosa.
        assertTrue(res, "La eliminación del usuario debería ser exitosa.");

        // Intenta obtener el usuario por su ID después de la eliminación.
        Usuario res2 = usuarioDAO.getById(usuario.getId());

        // Realiza una aserción para verificar que el usuario ya no existe en la base de datos
        // después de la eliminación (el método 'getById' debería retornar null).
        assertNull(res2, "El usuario debería haber sido eliminado y no encontrado por ID.");
    }

    private void autenticate(Usuario usuario) throws SQLException {
        // Llama al método 'authenticate' del UsuarioDAO para intentar autenticar un usuario.
        Usuario res = usuarioDAO.authenticate(usuario);

        // Realiza aserciones para verificar si la autenticación fue exitosa.
        assertNotNull(res, "La autenticación debería retornar un usuario no nulo si es exitosa.");
        assertEquals(res.getCorreoElectronico(), usuario.getCorreoElectronico(), "El correoElectronico del usuario autenticado debe coincidir con el correoElectronico proporcionado.");
        assertEquals(res.getEstado(), 1, "El estado del usuario autenticado debe ser 1 (activo).");
    }

    private void autenticacionFails(Usuario usuario) throws SQLException {
        // Llama al método 'authenticate' del UsuarioDAO para intentar autenticar un usuario
        // con credenciales que se espera que fallen.
        Usuario res = usuarioDAO.authenticate(usuario);

        // Realiza una aserción para verificar que la autenticación falló,
        // lo cual se espera que se represente con el método 'authenticate'
        // retornando 'null' cuando las credenciales no son válidas o el usuario no está activo.
        assertNull(res, "La autenticación debería fallar y retornar null para credenciales inválidas.");
    }

    private void updatePassword(Usuario usuario) throws SQLException {
        // Llama al método 'updatePassword' del UsuarioDAO para actualizar la contraseña del usuario.
        boolean res = usuarioDAO.updatePassword(usuario);

        // Realiza una aserción para verificar que la actualización de la contraseña fue exitosa.
        assertTrue(res, "La actualización de la contraseña debería ser exitosa.");

        // Llama al método 'autenticate' para verificar que la nueva contraseña es válida
        // y el usuario aún puede autenticarse con ella. Esto asume que el objeto 'user'
        // contiene la nueva contraseña (sin hashear) antes de llamar a 'updatePassword'.
        // Es importante asegurarse de que la prueba configure correctamente la nueva
        // contraseña en el objeto 'user' antes de esta llamada.
        autenticate(usuario);
    }

    @Test
    void testUsuarioDAO() throws SQLException {
        // Crea una instancia de la clase Random para generar datos de prueba aleatorios.
        Random random = new Random();
        // Genera un número aleatorio entre 1 y 1000 para asegurar la unicidad del email en cada prueba.
        int num = random.nextInt(1000) + 1;
        // Define una cadena base para el email y le concatena el número aleatorio generado.
        String strcorreoElectronico = "test" + num + "@example.com";
        // Crea un nuevo objeto Usuario con datos de prueba. El ID se establece en 0 ya que será generado por la base de datos.
        Usuario usuario = new Usuario(0, "TestUsuario", "password", strcorreoElectronico, (byte) 2);

        // Llama al método 'create' para persistir el usuario de prueba en la base de datos (simulada) y verifica su creación.
        Usuario testUsuario = create(usuario);

        // Llama al método 'update' para modificar los datos del usuario de prueba y verifica la actualización.
        update(testUsuario);

        // Llama al método 'search' para buscar usuarios por el nombre del usuario de prueba y verifica que se encuentre.
        search(testUsuario);

        // Restablece la contraseña original del usuario de prueba antes de intentar la autenticación exitosa.
        testUsuario.setPasswordHash(usuario.getPasswordHash());
        // Llama al método 'autenticate' para verificar que el usuario puede autenticarse con sus credenciales correctas.
        autenticate(testUsuario);

        // Intenta autenticar al usuario con una contraseña incorrecta para verificar el fallo de autenticación.
        testUsuario.setPasswordHash("12345");
        autenticacionFails(testUsuario);

        // Intenta actualizar la contraseña del usuario de prueba con una nueva contraseña.
        testUsuario.setPasswordHash("new_password"); // Establece la *nueva* contraseña para la actualización.
        updatePassword(testUsuario); // Llama al método para actualizar la contraseña en la base de datos.
        testUsuario.setPasswordHash("new_password"); // **Importante:** Actualiza el objeto 'testUser' con la *nueva* contraseña para la siguiente verificación.
        autenticate(testUsuario); // Verifica que la autenticación sea exitosa con la *nueva* contraseña.


        // Llama al método 'delete' para eliminar el usuario de prueba de la base de datos y verifica la eliminación.
        delete(testUsuario);
    }

    @Test
    void createUsuario() throws SQLException {
        Usuario usuario = new Usuario(0, "admin", "12345", "admin@gmail.com", (byte) 1);
        Usuario res = usuarioDAO.create(usuario);
        assertNotEquals(res, null);
    }
}
package ale2025.persistencia;

import org.junit.jupiter.api.BeforeEach; // Anotación para indicar que el método se ejecuta antes de cada método de prueba.
import org.junit.jupiter.api.Test;         // Anotación para indicar que el método es un caso de prueba.
import ale2025.dominio.Especialidad;        // Clase que representa la entidad de especialidad utilizada en las pruebas.

import java.util.ArrayList;                 // Clase para crear listas dinámicas de objetos, utilizada en algunas pruebas.
import java.sql.SQLException;               // Clase para manejar excepciones relacionadas con la base de datos.

import static org.junit.jupiter.api.Assertions.*; // Importación estática de métodos de aserción de JUnit 5 para verificar el comportamiento esperado en las pruebas.

class EspecialidadDAOTest {
    private EspecialidadDAO especialidadDAO; // Instancia de la clase EspecialidadDAO que se va a probar.

    @BeforeEach
    void setUp() {
        // Método que se ejecuta antes de cada método de prueba (@Test).
        // Su propósito es inicializar el entorno de prueba, en este caso,
        // creando una nueva instancia de EspecialidadDAO para cada prueba.
        especialidadDAO = new EspecialidadDAO();
    }

    private Especialidad create(Especialidad especialidad) throws SQLException {
        // Llama al método 'create' del EspecialidadDAO para persistir la especialidad en la base de datos.
        Especialidad res = especialidadDAO.create(especialidad);

        // Realiza aserciones para verificar que la creación de la especialidad fue exitosa
        // y que los datos de la especialidad retornada coinciden con los datos originales.
        assertNotNull(res, "La especialidad creada no debería ser nula."); // Verifica que el objeto retornado no sea nulo.
        assertTrue(res.getId() > 0, "El ID de la especialidad creada debe ser mayor que 0 (generado por la BD)."); // Asegurarse que el ID fue generado
        assertEquals(especialidad.getNombre(), res.getNombre(), "El nombre de la especialidad creada debe ser igual al original.");
        assertEquals(especialidad.getDescripcion(), res.getDescripcion(), "La descripción de la especialidad creada debe ser igual a la original.");

        // Retorna el objeto Especialidad creado (tal como lo devolvió el EspecialidadDAO).
        return res;
    }

    private void update(Especialidad especialidad) throws SQLException {
        // Modifica los atributos del objeto Especialidad para simular una actualización.
        especialidad.setNombre(especialidad.getNombre() + " Actualizada"); // Añade " Actualizada" al final del nombre.
        especialidad.setDescripcion("Nueva descripción para " + especialidad.getNombre()); // Cambia la descripción.

        // Llama al método 'update' del EspecialidadDAO para actualizar la especialidad en la base de datos.
        boolean res = especialidadDAO.update(especialidad);

        // Realiza una aserción para verificar que la actualización fue exitosa.
        assertTrue(res, "La actualización de la especialidad debería ser exitosa.");

        // Llama al método 'getById' para verificar que los cambios se persistieron correctamente.
        getById(especialidad);
    }

    private void getById(Especialidad especialidad) throws SQLException {
        // Llama al método 'getById' del EspecialidadDAO para obtener una especialidad por su ID.
        Especialidad res = especialidadDAO.getById(especialidad.getId());

        // Realiza aserciones para verificar que la especialidad obtenida coincide
        // con la especialidad original (o la especialidad modificada en pruebas de actualización).
        assertNotNull(res, "La especialidad obtenida por ID no debería ser nula.");
        assertEquals(especialidad.getId(), res.getId(), "El ID de la especialidad obtenida debe ser igual al original.");
        assertEquals(especialidad.getNombre(), res.getNombre(), "El nombre de la especialidad obtenida debe ser igual al esperado.");
        assertEquals(especialidad.getDescripcion(), res.getDescripcion(), "La descripción de la especialidad obtenida debe ser igual a la esperada.");
    }

    private void search(Especialidad especialidad) throws SQLException {
        // Llama al método 'search' del EspecialidadDAO para buscar especialidades por nombre.
        ArrayList<Especialidad> especialidadesEncontradas = especialidadDAO.search(especialidad.getNombre());

        // Verifica que se hayan encontrado especialidades y que la lista no esté vacía.
        assertFalse(especialidadesEncontradas.isEmpty(), "La búsqueda debería encontrar al menos una especialidad.");

        boolean encontrado = false;
        for (Especialidad e : especialidadesEncontradas) {
            if (e.getNombre().contains(especialidad.getNombre())) {
                encontrado = true;
                break; // Si encontramos al menos uno que coincida, es suficiente para esta aserción.
            }
        }
        assertTrue(encontrado, "La especialidad buscada no fue encontrada en los resultados de la búsqueda.");
    }

    private void delete(Especialidad especialidad) throws SQLException {
        // Llama al método 'delete' del EspecialidadDAO para eliminar una especialidad por su ID.
        boolean res = especialidadDAO.delete(especialidad);

        // Realiza una aserción para verificar que la eliminación fue exitosa.
        assertTrue(res, "La eliminación de la especialidad debería ser exitosa.");

        // Intenta obtener la especialidad por su ID después de la eliminación.
        Especialidad res2 = especialidadDAO.getById(especialidad.getId());

        // Realiza una aserción para verificar que la especialidad ya no existe en la base de datos
        // después de la eliminación (el método 'getById' debería retornar null).
        assertNull(res2, "La especialidad debería haber sido eliminada y no encontrada por ID.");
    }

    @Test
    void testEspecialidadDAO() throws SQLException {
        // Crea un nuevo objeto Especialidad con datos de prueba. El ID se establece en 0 ya que será generado por la base de datos.
        Especialidad especialidad = new Especialidad(0,"Cardiología", "Estudio y tratamiento del corazón.");

        // Llama al método 'create' para persistir la especialidad de prueba en la base de datos y verifica su creación.
        Especialidad testEspecialidad = create(especialidad);

        // Llama al método 'update' para modificar los datos de la especialidad de prueba y verifica la actualización.
        update(testEspecialidad);

        // Llama al método 'search' para buscar especialidades por el nombre de la especialidad de prueba y verifica que se encuentre.
        search(testEspecialidad);

        // Llama al método 'delete' para eliminar la especialidad de prueba de la base de datos y verifica la eliminación.
        delete(testEspecialidad);
    }
}
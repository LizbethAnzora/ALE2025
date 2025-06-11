package ale2025.persistencia;

import org.junit.jupiter.api.BeforeEach; // Anotación para indicar que el método se ejecuta antes de cada método de prueba.
import org.junit.jupiter.api.Test;         // Anotación para indicar que el método es un caso de prueba.
import ale2025.dominio.Medico;              // Clase que representa la entidad de médico utilizada en las pruebas.

import java.util.ArrayList;                 // Clase para crear listas dinámicas de objetos, utilizada en algunas pruebas.
import java.sql.SQLException;               // Clase para manejar excepciones relacionadas con la base de datos.

import static org.junit.jupiter.api.Assertions.*; // Importación estática de métodos de aserción de JUnit 5 para verificar el comportamiento esperado en las pruebas.

class MedicoDAOTest {
    private MedicoDAO medicoDAO; // Instancia de la clase MedicoDAO que se va a probar.

    @BeforeEach
    void setUp() {
        // Método que se ejecuta antes de cada método de prueba (@Test).
        // Su propósito es inicializar el entorno de prueba, en este caso,
        // creando una nueva instancia de MedicoDAO para cada prueba.
        medicoDAO = new MedicoDAO();
    }

    private Medico create(Medico medico) throws SQLException {
        // Llama al método 'create' del MedicoDAO para persistir el médico en la base de datos.
        Medico res = medicoDAO.create(medico);

        // Realiza aserciones para verificar que la creación del médico fue exitosa
        // y que los datos del médico retornado coinciden con los datos originales.
        assertNotNull(res, "El médico creado no debería ser nulo."); // Verifica que el objeto retornado no sea nulo.
        assertTrue(res.getId() > 0, "El ID del médico creado debe ser mayor que 0 (generado por la BD)."); // Asegurarse que el ID fue generado
        assertEquals(medico.getNombreCompleto(), res.getNombreCompleto(), "El nombre completo del médico creado debe ser igual al original.");
        assertEquals(medico.getEspecialidadId(), res.getEspecialidadId(), "El ID de especialidad del médico creado debe ser igual al original.");
        assertEquals(medico.getSueldo(), res.getSueldo(), 0.001, "El sueldo del médico creado debe ser igual al original."); // Usar delta para doubles

        // Retorna el objeto Medico creado (tal como lo devolvió el MedicoDAO).
        return res;
    }

    private void update(Medico medico) throws SQLException {
        // Modifica los atributos del objeto Medico para simular una actualización.
        medico.setNombreCompleto(medico.getNombreCompleto() + " (Actualizado)"); // Añade "(Actualizado)" al final del nombre.
        // Asegúrate de que el especialidadId que usas aquí exista en tu tabla Especialidades
        medico.setEspecialidadId(medico.getEspecialidadId());
        medico.setSueldo(medico.getSueldo() * 1.10); // Aumenta el sueldo en un 10%

        // Llama al método 'update' del MedicoDAO para actualizar el médico en la base de datos.
        boolean res = medicoDAO.update(medico);

        // Realiza una aserción para verificar que la actualización fue exitosa.
        assertTrue(res, "La actualización del médico debería ser exitosa.");

        // Llama al método 'getById' para verificar que los cambios se persistieron correctamente.
        getById(medico);
    }

    private void getById(Medico medico) throws SQLException {
        // Llama al método 'getById' del MedicoDAO para obtener un médico por su ID.
        Medico res = medicoDAO.getById(medico.getId());

        // Realiza aserciones para verificar que el médico obtenido coincide
        // con el médico original (o el médico modificado en pruebas de actualización).
        assertNotNull(res, "El médico obtenido por ID no debería ser nulo.");
        assertEquals(medico.getId(), res.getId(), "El ID del médico obtenido debe ser igual al original.");
        assertEquals(medico.getNombreCompleto(), res.getNombreCompleto(), "El nombre completo del médico obtenido debe ser igual al esperado.");
        assertEquals(medico.getEspecialidadId(), res.getEspecialidadId(), "El ID de especialidad del médico obtenido debe ser igual al esperado.");
        assertEquals(medico.getSueldo(), res.getSueldo(), 0.001, "El sueldo del médico obtenido debe ser igual al esperado.");
    }

    private void search(Medico medico) throws SQLException {
        // Llama al método 'search' del MedicoDAO para buscar médicos por nombre completo.
        ArrayList<Medico> medicosEncontrados = medicoDAO.search(medico.getNombreCompleto());

        // Verifica que se hayan encontrado médicos y que la lista no esté vacía.
        assertFalse(medicosEncontrados.isEmpty(), "La búsqueda debería encontrar al menos un médico.");

        boolean encontrado = false;
        for (Medico m : medicosEncontrados) {
            if (m.getNombreCompleto().contains(medico.getNombreCompleto())) {
                encontrado = true;
                break; // Si encontramos al menos uno que coincida, es suficiente para esta aserción.
            }
        }
        assertTrue(encontrado, "El médico buscado no fue encontrado en los resultados de la búsqueda.");
    }

    private void delete(Medico medico) throws SQLException {
        // Llama al método 'delete' del MedicoDAO para eliminar un médico por su ID.
        boolean res = medicoDAO.delete(medico);

        // Realiza una aserción para verificar que la eliminación fue exitosa.
        assertTrue(res, "La eliminación del médico debería ser exitosa.");

        // Intenta obtener el médico por su ID después de la eliminación.
        Medico res2 = medicoDAO.getById(medico.getId());

        // Realiza una aserción para verificar que el médico ya no existe en la base de datos
        // después de la eliminación (el método 'getById' debería retornar null).
        assertNull(res2, "El médico debería haber sido eliminado y no encontrado por ID.");
    }

    @Test
    void testMedicoDAO() throws SQLException {
        // NOTA IMPORTANTE: Para que este test funcione correctamente,
        // la tabla 'Especialidades' debe existir y contener al menos una especialidad
        // con el ID especificado en 'especialidadId' (por ejemplo, 1).
        // Si no existe, este test fallará con un error de clave foránea.
        Medico medico = new Medico(0, "Dr. Juan Pérez", 1, 3500.00); // Usamos especialidadId = 1 como ejemplo

        // Llama al método 'create' para persistir el médico de prueba en la base de datos y verifica su creación.
        Medico testMedico = create(medico);

        // Llama al método 'update' para modificar los datos del médico de prueba y verifica la actualización.
        update(testMedico);

        // Llama al método 'search' para buscar médicos por el nombre del médico de prueba y verifica que se encuentre.
        search(testMedico);

        // Llama al método 'delete' para eliminar el médico de prueba de la base de datos y verifica la eliminación.
        delete(testMedico);
    }
}
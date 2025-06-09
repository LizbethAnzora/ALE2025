package ale2025.persistencia;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ale2025.dominio.Paciente;

import java.util.ArrayList;
import java.sql.Date; // Asegúrate de que sea java.sql.Date
import java.util.Random;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class PacienteDAOTest {
    private PacienteDAO pacienteDAO; // Instancia de la clase PacienteDAO que se va a probar.

    @BeforeEach
    void setUp() {
        // Método que se ejecuta antes de cada método de prueba (@Test).
        // Su propósito es inicializar el entorno de prueba, en este caso,
        // creando una nueva instancia de PacienteDAO para cada prueba.
        pacienteDAO = new PacienteDAO();
    }

    private Paciente create(Paciente paciente) throws SQLException {
        // Llama al método 'create' del PacienteDAO para persistir el paciente en la base de datos (simulada).
        Paciente res = pacienteDAO.create(paciente);

        // Realiza aserciones para verificar que la creación del paciente fue exitosa
        // y que los datos del paciente retornado coinciden con los datos originales.
        assertNotNull(res, "El paciente creado no debería ser nulo."); // Verifica que el objeto retornado no sea nulo.
        assertTrue(res.getId() > 0, "El ID del paciente creado debe ser mayor que 0 (generado por la BD)."); // Asegurarse que el ID fue generado
        assertEquals(paciente.getNombreCompleto(), res.getNombreCompleto(), "El nombre del paciente creado debe ser igual al original.");
        assertEquals(paciente.getTelefono(), res.getTelefono(), "El telefono del paciente creado debe ser igual al original.");
        assertEquals(paciente.getFechaNacimiento(), res.getFechaNacimiento(), "La fecha de nacimiento del paciente creado debe ser igual al original.");

        // Retorna el objeto Paciente creado (tal como lo devolvió el PacienteDAO).
        return res;
    }

    private void update(Paciente paciente) throws SQLException {
        // Modifica los atributos del objeto Paciente para simular una actualización.
        paciente.setNombreCompleto(paciente.getNombreCompleto() + "_u"); // Añade "_u" al final del nombre.
        paciente.setTelefono(paciente.getTelefono());
        paciente.setFechaNacimiento(Date.valueOf("2025-01-01")); // Establece una nueva fecha específica o new Date(System.currentTimeMillis()) para la actual.

        // Llama al método 'update' del PacienteDAO para actualizar el paciente en la base de datos (simulada).
        boolean res = pacienteDAO.update(paciente);

        // Realiza una aserción para verificar que la actualización fue exitosa.
        assertTrue(res, "La actualización del paciente debería ser exitosa.");

        // Llama al método 'getById' para verificar que los cambios se persistieron correctamente.
        // Aunque el método 'getById' ya tiene sus propias aserciones, esta llamada adicional
        // ayuda a asegurar que la actualización realmente tuvo efecto en la capa de datos.
        getById(paciente);
    }

    private void getById(Paciente paciente) throws SQLException {
        // Llama al método 'getById' del PacienteDAO para obtener un paciente por su ID.
        Paciente res = pacienteDAO.getById(paciente.getId());

        // Realiza aserciones para verificar que el paciente obtenido coincide
        // con el paciente original (o el paciente modificado en pruebas de actualización).
        assertNotNull(res, "El paciente obtenido por ID no debería ser nulo.");
        assertEquals(paciente.getId(), res.getId(), "El ID del paciente obtenido debe ser igual al original.");
        assertEquals(paciente.getNombreCompleto(), res.getNombreCompleto(), "El nombre del paciente obtenido debe ser igual al esperado.");
        assertEquals(paciente.getTelefono(), res.getTelefono(), "El telefono del paciente obtenido debe ser igual al esperado.");
        assertEquals(paciente.getFechaNacimiento(), res.getFechaNacimiento(), "La fecha de nacimiento del paciente obtenido debe ser igual al esperado.");
    }

    private void search(Paciente paciente) throws SQLException {
        // Llama al método 'search' del PacienteDAO para buscar pacientes por nombre.
        ArrayList<Paciente> pacientesEncontrados = pacienteDAO.search(paciente.getNombreCompleto());

        // Verifica que se hayan encontrado pacientes y que la lista no esté vacía.
        assertFalse(pacientesEncontrados.isEmpty(), "La búsqueda debería encontrar al menos un paciente.");

        boolean encontrado = false;
        for (Paciente p : pacientesEncontrados) {
            if (p.getNombreCompleto().contains(paciente.getNombreCompleto())) {
                encontrado = true;
                break; // Si encontramos al menos uno que coincida, es suficiente para esta aserción.
            }
        }
        assertTrue(encontrado, "El paciente buscado no fue encontrado en los resultados de la búsqueda.");
    }

    private void delete(Paciente paciente) throws SQLException {
        // Llama al método 'delete' del PacienteDAO para eliminar un paciente por su ID.
        boolean res = pacienteDAO.delete(paciente);

        // Realiza una aserción para verificar que la eliminación fue exitosa.
        assertTrue(res, "La eliminación del paciente debería ser exitosa.");

        // Intenta obtener el paciente por su ID después de la eliminación.
        Paciente res2 = pacienteDAO.getById(paciente.getId());

        // Realiza una aserción para verificar que el paciente ya no existe en la base de datos
        // después de la eliminación (el método 'getById' debería retornar null).
        assertNull(res2, "El paciente debería haber sido eliminado y no encontrado por ID.");
    }

    @Test
    void testPacienteDAO() throws SQLException {
        // Crea un nuevo objeto Paciente con datos de prueba. El ID se establece en 0 ya que será generado por la base de datos.
        // Se corrige la forma de instanciar java.sql.Date para una fecha específica.
        Paciente paciente = new Paciente(0,"Eneida Anzora", "7890-5678", Date.valueOf("2005-08-08"));

        // Llama al método 'create' para persistir el paciente de prueba en la base de datos (simulada) y verifica su creación.
        Paciente testPaciente = create(paciente);

        // Llama al método 'update' para modificar los datos del paciente de prueba y verifica la actualización.
        update(testPaciente);

        // Llama al método 'search' para buscar pacientes por el nombre del paciente de prueba y verifica que se encuentre.
        search(testPaciente);

        // Llama al método 'delete' para eliminar el paciente de prueba de la base de datos y verifica la eliminación.
        delete(testPaciente);
    }
}
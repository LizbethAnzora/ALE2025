package ale2025.persistencia;

import org.junit.jupiter.api.BeforeEach; // Anotación para indicar que el método se ejecuta antes de cada método de prueba.
import org.junit.jupiter.api.Test;         // Anotación para indicar que el método es un caso de prueba.
import ale2025.dominio.Cita;                // Clase que representa la entidad de cita utilizada en las pruebas.

import java.sql.SQLException;               // Clase para manejar excepciones relacionadas con la base de datos.
import java.sql.Date;                       // Necesario para java.sql.Date
import java.util.ArrayList;                 // Clase para crear listas dinámicas de objetos, utilizada en algunas pruebas.

import static org.junit.jupiter.api.Assertions.*; // Importación estática de métodos de aserción de JUnit 5 para verificar el comportamiento esperado en las pruebas.

class CitaDAOTest {
    private CitaDAO citaDAO; // Instancia de la clase CitaDAO que se va a probar.

    @BeforeEach
    void setUp() {
        // Método que se ejecuta antes de cada método de prueba (@Test).
        // Su propósito es inicializar el entorno de prueba, en este caso,
        // creando una nueva instancia de CitaDAO para cada prueba.
        citaDAO = new CitaDAO();
    }

    private Cita create(Cita cita) throws SQLException {
        // Llama al método 'create' del CitaDAO para persistir la cita en la base de datos.
        Cita res = citaDAO.create(cita);

        // Realiza aserciones para verificar que la creación de la cita fue exitosa
        // y que los datos de la cita retornada coinciden con los datos originales.
        assertNotNull(res, "La cita creada no debería ser nula."); // Verifica que el objeto retornado no sea nulo.
        assertTrue(res.getId() > 0, "El ID de la cita creada debe ser mayor que 0 (generado por la BD)."); // Asegurarse que el ID fue generado
        assertEquals(cita.getPacienteId(), res.getPacienteId(), "El ID del paciente de la cita creada debe ser igual al original.");
        assertEquals(cita.getMedicoId(), res.getMedicoId(), "El ID del médico de la cita creada debe ser igual al original.");
        assertEquals(cita.getFechaCita().toString(), res.getFechaCita().toString(), "La fecha de la cita creada debe ser igual a la original."); // Comparar como String por precisión de Date
        assertEquals(cita.getCostoConsulta(), res.getCostoConsulta(), 0.001, "El costo de la consulta de la cita creada debe ser igual al original."); // Usar delta para doubles

        // Retorna el objeto Cita creado (tal como lo devolvió el CitaDAO).
        return res;
    }

    private void update(Cita cita) throws SQLException {
        // Modifica los atributos del objeto Cita para simular una actualización.
        // Asegúrate de que los IDs de paciente y médico existan en sus respectivas tablas.
        cita.setFechaCita(Date.valueOf("2025-01-15")); // Cambia la fecha de la cita.
        cita.setCostoConsulta(cita.getCostoConsulta() + 50.00); // Aumenta el costo de la consulta.
        // cita.setPacienteId(cita.getPacienteId() + 1); // Cambiar si necesitas probar con otro paciente
        // cita.setMedicoId(cita.getMedicoId() + 1);   // Cambiar si necesitas probar con otro médico

        // Llama al método 'update' del CitaDAO para actualizar la cita en la base de datos.
        boolean res = citaDAO.update(cita);

        // Realiza una aserción para verificar que la actualización fue exitosa.
        assertTrue(res, "La actualización de la cita debería ser exitosa.");

        // Llama al método 'getById' para verificar que los cambios se persistieron correctamente.
        getById(cita);
    }

    private void getById(Cita cita) throws SQLException {
        // Llama al método 'getById' del CitaDAO para obtener una cita por su ID.
        Cita res = citaDAO.getById(cita.getId());

        // Realiza aserciones para verificar que la cita obtenida coincide
        // con la cita original (o la cita modificada en pruebas de actualización).
        assertNotNull(res, "La cita obtenida por ID no debería ser nula.");
        assertEquals(cita.getId(), res.getId(), "El ID de la cita obtenida debe ser igual al original.");
        assertEquals(cita.getPacienteId(), res.getPacienteId(), "El ID del paciente de la cita obtenida debe ser igual al esperado.");
        assertEquals(cita.getMedicoId(), res.getMedicoId(), "El ID del médico de la cita obtenida debe ser igual al esperado.");
        assertEquals(cita.getFechaCita().toString(), res.getFechaCita().toString(), "La fecha de la cita obtenida debe ser igual a la esperada.");
        assertEquals(cita.getCostoConsulta(), res.getCostoConsulta(), 0.001, "El costo de la consulta de la cita obtenida debe ser igual al esperado.");
    }

    private void search(Cita cita) throws SQLException {
        // Llama al método 'search' del CitaDAO para buscar citas por fecha.
        // Asegúrate de que el formato de la fecha sea 'YYYY-MM-DD' para la búsqueda.
        ArrayList<Cita> citasEncontradas = citaDAO.search(cita.getFechaCita().toString());

        // Verifica que se hayan encontrado citas y que la lista no esté vacía.
        assertFalse(citasEncontradas.isEmpty(), "La búsqueda debería encontrar al menos una cita.");

        boolean encontrado = false;
        for (Cita c : citasEncontradas) {
            // Para la búsqueda por fecha, comparamos las cadenas de fecha
            if (c.getFechaCita().toString().equals(cita.getFechaCita().toString())) {
                encontrado = true;
                break; // Si encontramos al menos uno que coincida, es suficiente para esta aserción.
            }
        }
        assertTrue(encontrado, "La cita buscada por fecha no fue encontrada en los resultados de la búsqueda.");
    }

    private void delete(Cita cita) throws SQLException {
        // Llama al método 'delete' del CitaDAO para eliminar una cita por su ID.
        boolean res = citaDAO.delete(cita);

        // Realiza una aserción para verificar que la eliminación fue exitosa.
        assertTrue(res, "La eliminación de la cita debería ser exitosa.");

        // Intenta obtener la cita por su ID después de la eliminación.
        Cita res2 = citaDAO.getById(cita.getId());

        // Realiza una aserción para verificar que la cita ya no existe en la base de datos
        // después de la eliminación (el método 'getById' debería retornar null).
        assertNull(res2, "La cita debería haber sido eliminada y no encontrada por ID.");
    }

    @Test
    void testCitaDAO() throws SQLException {
        // NOTA IMPORTANTE: Para que este test funcione correctamente,
        // las tablas 'Pacientes' y 'Medicos' deben existir y contener registros
        // con los IDs especificados en 'pacienteId' y 'medicoId' (por ejemplo, 1 y 1).
        // Si no existen, este test fallará con un error de clave foránea.
        // Asegúrate de crear al menos un paciente y un médico antes de ejecutar este test.
        Cita cita = new Cita(0, 1, 1, Date.valueOf("2024-12-25"), 75.00); // pacienteId=1, medicoId=1 como ejemplo

        // Llama al método 'create' para persistir la cita de prueba en la base de datos y verifica su creación.
        Cita testCita = create(cita);

        // Llama al método 'update' para modificar los datos de la cita de prueba y verifica la actualización.
        update(testCita);

        // Llama al método 'search' para buscar citas por la fecha de la cita de prueba y verifica que se encuentre.
        search(testCita);

        // Llama al método 'delete' para eliminar la cita de prueba de la base de datos y verifica la eliminación.
        delete(testCita);
    }
}
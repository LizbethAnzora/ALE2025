package ale2025.persistencia;

import org.junit.jupiter.api.BeforeEach; // Anotación para indicar que el método se ejecuta antes de cada método de prueba.
import org.junit.jupiter.api.Test;         // Anotación para indicar que el método es un caso de prueba.
import ale2025.dominio.Horario;             // Clase que representa la entidad de horario utilizada en las pruebas.

import java.sql.SQLException;               // Clase para manejar excepciones relacionadas con la base de datos.
import java.sql.Time;                       // Necesario para java.sql.Time
import java.util.ArrayList;                 // Clase para crear listas dinámicas de objetos, utilizada en algunas pruebas.

import static org.junit.jupiter.api.Assertions.*; // Importación estática de métodos de aserción de JUnit 5 para verificar el comportamiento esperado en las pruebas.

class HorarioDAOTest {
    private HorarioDAO horarioDAO; // Instancia de la clase HorarioDAO que se va a probar.

    @BeforeEach
    void setUp() {
        // Método que se ejecuta antes de cada método de prueba (@Test).
        // Su propósito es inicializar el entorno de prueba, en este caso,
        // creando una nueva instancia de HorarioDAO para cada prueba.
        horarioDAO = new HorarioDAO();
    }

    private Horario create(Horario horario) throws SQLException {
        // Llama al método 'create' del HorarioDAO para persistir el horario en la base de datos.
        Horario res = horarioDAO.create(horario);

        // Realiza aserciones para verificar que la creación del horario fue exitosa
        // y que los datos del horario retornado coinciden con los datos originales.
        assertNotNull(res, "El horario creado no debería ser nulo."); // Verifica que el objeto retornado no sea nulo.
        assertTrue(res.getId() > 0, "El ID del horario creado debe ser mayor que 0 (generado por la BD)."); // Asegurarse que el ID fue generado
        assertEquals(horario.getMedicoId(), res.getMedicoId(), "El ID del médico del horario creado debe ser igual al original.");
        assertEquals(horario.getDiaSemana(), res.getDiaSemana(), "El día de la semana del horario creado debe ser igual al original.");
        assertEquals(horario.getHoraInicio().toString(), res.getHoraInicio().toString(), "La hora de inicio del horario creado debe ser igual a la original."); // Comparar como String por precisión de Time
        assertEquals(horario.getHoraFin().toString(), res.getHoraFin().toString(), "La hora de fin del horario creado debe ser igual a la original."); // Comparar como String por precisión de Time

        // Retorna el objeto Horario creado (tal como lo devolvió el HorarioDAO).
        return res;
    }

    private void update(Horario horario) throws SQLException {
        // Modifica los atributos del objeto Horario para simular una actualización.
        horario.setDiaSemana("Martes"); // Cambia el día de la semana.
        horario.setHoraInicio(Time.valueOf("09:00:00")); // Cambia la hora de inicio.
        horario.setHoraFin(Time.valueOf("17:00:00")); // Cambia la hora de fin.
        // Se puede cambiar el medicoId si es necesario, pero asegúrate que exista en la BD.
        // horario.setMedicoId(horario.getMedicoId() + 1);

        // Llama al método 'update' del HorarioDAO para actualizar el horario en la base de datos.
        boolean res = horarioDAO.update(horario);

        // Realiza una aserción para verificar que la actualización fue exitosa.
        assertTrue(res, "La actualización del horario debería ser exitosa.");

        // Llama al método 'getById' para verificar que los cambios se persistieron correctamente.
        getById(horario);
    }

    private void getById(Horario horario) throws SQLException {
        // Llama al método 'getById' del HorarioDAO para obtener un horario por su ID.
        Horario res = horarioDAO.getById(horario.getId());

        // Realiza aserciones para verificar que el horario obtenido coincide
        // con el horario original (o el horario modificado en pruebas de actualización).
        assertNotNull(res, "El horario obtenido por ID no debería ser nulo.");
        assertEquals(horario.getId(), res.getId(), "El ID del horario obtenido debe ser igual al original.");
        assertEquals(horario.getMedicoId(), res.getMedicoId(), "El ID del médico del horario obtenido debe ser igual al esperado.");
        assertEquals(horario.getDiaSemana(), res.getDiaSemana(), "El día de la semana del horario obtenido debe ser igual al esperado.");
        assertEquals(horario.getHoraInicio().toString(), res.getHoraInicio().toString(), "La hora de inicio del horario obtenido debe ser igual a la esperada.");
        assertEquals(horario.getHoraFin().toString(), res.getHoraFin().toString(), "La hora de fin del horario obtenido debe ser igual a la esperada.");
    }

    private void search(Horario horario) throws SQLException {
        // Llama al método 'search' del HorarioDAO para buscar horarios por día de la semana.
        ArrayList<Horario> horariosEncontrados = horarioDAO.search(horario.getDiaSemana());

        // Verifica que se hayan encontrado horarios y que la lista no esté vacía.
        assertFalse(horariosEncontrados.isEmpty(), "La búsqueda debería encontrar al menos un horario.");

        boolean encontrado = false;
        for (Horario h : horariosEncontrados) {
            if (h.getDiaSemana().contains(horario.getDiaSemana())) {
                encontrado = true;
                break; // Si encontramos al menos uno que coincida, es suficiente para esta aserción.
            }
        }
        assertTrue(encontrado, "El horario buscado no fue encontrado en los resultados de la búsqueda.");
    }

    private void delete(Horario horario) throws SQLException {
        // Llama al método 'delete' del HorarioDAO para eliminar un horario por su ID.
        boolean res = horarioDAO.delete(horario);

        // Realiza una aserción para verificar que la eliminación fue exitosa.
        assertTrue(res, "La eliminación del horario debería ser exitosa.");

        // Intenta obtener el horario por su ID después de la eliminación.
        Horario res2 = horarioDAO.getById(horario.getId());

        // Realiza una aserción para verificar que el horario ya no existe en la base de datos
        // después de la eliminación (el método 'getById' debería retornar null).
        assertNull(res2, "El horario debería haber sido eliminado y no encontrado por ID.");
    }

    @Test
    void testHorarioDAO() throws SQLException {
        // NOTA IMPORTANTE: Para que este test funcione correctamente,
        // la tabla 'Medicos' debe existir y contener al menos un médico
        // con el ID especificado en 'medicoId' (por ejemplo, 1).
        // Si no existe, este test fallará con un error de clave foránea.
        Horario horario = new Horario(0, 4, "Lunes", Time.valueOf("08:00:00"), Time.valueOf("16:00:00"));

        // Llama al método 'create' para persistir el horario de prueba en la base de datos y verifica su creación.
        Horario testHorario = create(horario);

        // Llama al método 'update' para modificar los datos del horario de prueba y verifica la actualización.
        update(testHorario);

        // Llama al método 'search' para buscar horarios por el día de la semana del horario de prueba y verifica que se encuentre.
        search(testHorario);

        // Llama al método 'delete' para eliminar el horario de prueba de la base de datos y verifica la eliminación.
        delete(testHorario);
    }
}
package ale2025.presentacion;

import ale2025.persistencia.CitaDAO;      // Importa la clase CitaDAO.
import ale2025.persistencia.PacienteDAO;  // Para obtener el nombre del paciente.
import ale2025.persistencia.MedicoDAO;    // Para obtener el nombre del médico.
import ale2025.dominio.Cita;        // Importa la clase Cita.
import ale2025.dominio.Paciente;     // Importa la clase Paciente.
import ale2025.dominio.Medico;       // Importa la clase Medico.
import ale2025.utils.CUD;           // Importa el enum CUD.

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import javax.swing.*;                   // Importa Swing para GUI.
import javax.swing.table.DefaultTableModel; // Importa DefaultTableModel.
import java.awt.*;                      // Necesario para BorderLayout y eventos.
import java.beans.PropertyChangeEvent;  // Para escuchar cambios en el DatePicker.
import java.beans.PropertyChangeListener; // Para escuchar cambios en el DatePicker.
import java.sql.SQLException;           // Para manejo de errores SQL.
import java.sql.Date;                   // Para java.sql.Date.
import java.time.LocalDate;             // Para java.time.LocalDate.
import java.util.ArrayList;             // Para listas dinámicas.

public class CitaReadingForm extends JDialog {
    private JPanel mainPanel;
    private JButton btnCreate;
    private JPanel panelFechaCita; // Panel vacío para el DatePicker de búsqueda
    private JTable table1; // NOTA: Renombrado a tableCitas en el código para claridad
    private JButton btnUpdate;
    private JButton btnDelete;

    private CitaDAO citaDAO;
    private PacienteDAO pacienteDAO; // Necesario para mostrar el nombre del paciente
    private MedicoDAO medicoDAO;     // Necesario para mostrar el nombre del médico
    private MainForm mainForm;

    private DatePicker datePickerFechaCitaSearch; // Instancia del DatePicker para búsqueda

    public CitaReadingForm(MainForm mainForm) {
        this.mainForm = mainForm;
        citaDAO = new CitaDAO();
        pacienteDAO = new PacienteDAO();
        medicoDAO = new MedicoDAO();
        setContentPane(mainPanel);
        setModal(true);
        setTitle("Buscar Cita");
        pack();
        setLocationRelativeTo(mainForm);

        // Renombrar table1 a tableCitas para consistencia
        this.tableCitas = table1; // Asigna la referencia del componente del diseñador

        setupDatePickerSearch(); // Llama al método para configurar el DatePicker de búsqueda

        // Listener para el DatePicker de búsqueda: activa la búsqueda al cambiar la fecha
        datePickerFechaCitaSearch.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("date".equals(evt.getPropertyName())) { // Escucha cambios en la propiedad 'date'
                    LocalDate selectedDate = (LocalDate) evt.getNewValue();
                    if (selectedDate != null) {
                        // Convierte LocalDate a String en formato YYYY-MM-DD para el DAO
                        search(selectedDate.toString());
                    } else {
                        // Si la fecha se borra, limpiar la tabla
                        DefaultTableModel emptyModel = new DefaultTableModel();
                        tableCitas.setModel(emptyModel);
                    }
                }
            }
        });


        // Agrega un ActionListener al botón btnCreate.
        btnCreate.addActionListener(s -> {
            CitaWriteForm citaWriteForm = new CitaWriteForm(this.mainForm, CUD.CREATE, new Cita());
            citaWriteForm.setVisible(true);
            // Si la operación fue exitosa y hay una fecha seleccionada, refresca la búsqueda.
            if (citaWriteForm.getResult() && datePickerFechaCitaSearch.getDate() != null) {
                search(datePickerFechaCitaSearch.getDate().toString());
            } else {
                // Si no hay fecha seleccionada o se canceló, limpia la tabla.
                DefaultTableModel emptyModel = new DefaultTableModel();
                tableCitas.setModel(emptyModel);
            }
        });

        // Agrega un ActionListener al botón btnUpdate.
        btnUpdate.addActionListener(s -> {
            Cita cita = getCitaFromTableRow();
            if (cita != null) {
                CitaWriteForm citaWriteForm = new CitaWriteForm(this.mainForm, CUD.UPDATE, cita);
                citaWriteForm.setVisible(true);
                if (citaWriteForm.getResult() && datePickerFechaCitaSearch.getDate() != null) {
                    search(datePickerFechaCitaSearch.getDate().toString());
                } else {
                    DefaultTableModel emptyModel = new DefaultTableModel();
                    tableCitas.setModel(emptyModel);
                }
            }
        });

        // Agrega un ActionListener al botón btnDelete.
        btnDelete.addActionListener(s -> {
            Cita cita = getCitaFromTableRow();
            if (cita != null) {
                CitaWriteForm citaWriteForm = new CitaWriteForm(this.mainForm, CUD.DELETE, cita);
                citaWriteForm.setVisible(true);
                if (citaWriteForm.getResult() && datePickerFechaCitaSearch.getDate() != null) {
                    search(datePickerFechaCitaSearch.getDate().toString());
                } else {
                    DefaultTableModel emptyModel = new DefaultTableModel();
                    tableCitas.setModel(emptyModel);
                }
            }
        });
    }

    // Campo JTable renombrado para consistencia en el código
    private JTable tableCitas;

    // Método para configurar el DatePicker de búsqueda
    private void setupDatePickerSearch() {
        datePickerFechaCitaSearch = new DatePicker();
        DatePickerSettings settings = new DatePickerSettings();
        settings.setFormatForDatesCommonEra("dd/MM/yyyy"); // Formato de visualización
        datePickerFechaCitaSearch.setSettings(settings);

        // Asegúrate de que panelFechaCita tenga un layout adecuado
        panelFechaCita.setLayout(new BorderLayout());
        panelFechaCita.add(datePickerFechaCitaSearch, BorderLayout.CENTER);
    }

    // Método privado para buscar citas.
    private void search(String queryDateString) {
        try {
            ArrayList<Cita> citas = citaDAO.search(queryDateString);
            createTable(citas); // Actualiza la tabla con los resultados.
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al buscar citas: " + ex.getMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Método para crear y llenar la tabla de citas.
    public void createTable(ArrayList<Cita> citas) {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Ninguna celda es editable.
            }
        };

        // Define las columnas de la tabla.
        model.addColumn("Id");
        model.addColumn("Paciente");    // Mostrará el nombre del paciente
        model.addColumn("Médico");      // Mostrará el nombre del médico
        model.addColumn("Fecha Cita");
        model.addColumn("Costo Consulta");

        this.tableCitas.setModel(model); // Usar tableCitas aquí

        Object rowData[] = null;

        // Itera a través de la lista de citas.
        for (int i = 0; i < citas.size(); i++) {
            Cita cita = citas.get(i);
            model.addRow(rowData);
            // Llena las celdas con los datos de la cita.
            model.setValueAt(cita.getId(), i, 0);

            // Obtener el nombre del paciente
            String pacienteNombre = "Desconocido";
            try {
                Paciente paciente = pacienteDAO.getById(cita.getPacienteId());
                if (paciente != null) {
                    pacienteNombre = paciente.getNombreCompleto();
                }
            } catch (SQLException e) {
                System.err.println("Error al obtener nombre de paciente para Cita ID " + cita.getId() + ": " + e.getMessage());
            }
            model.setValueAt(pacienteNombre, i, 1);

            // Obtener el nombre del médico
            String medicoNombre = "Desconocido";
            try {
                Medico medico = medicoDAO.getById(cita.getMedicoId());
                if (medico != null) {
                    medicoNombre = medico.getNombreCompleto();
                }
            } catch (SQLException e) {
                System.err.println("Error al obtener nombre de médico para Cita ID " + cita.getId() + ": " + e.getMessage());
            }
            model.setValueAt(medicoNombre, i, 2);

            // Formatear la fecha para visualización
            String fechaCitaStr = (cita.getFechaCita() != null) ?
                    cita.getFechaCita().toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
            model.setValueAt(fechaCitaStr, i, 3);
            model.setValueAt(String.format("%.2f", cita.getCostoConsulta()), i, 4); // Formatear el costo
        }

        hideCol(0); // Oculta la columna del ID.
    }

    // Método privado para ocultar una columna de la tabla.
    private void hideCol(int pColumna) {
        this.tableCitas.getColumnModel().getColumn(pColumna).setMaxWidth(0);
        this.tableCitas.getColumnModel().getColumn(pColumna).setMinWidth(0);
        this.tableCitas.getTableHeader().getColumnModel().getColumn(pColumna).setMaxWidth(0);
        this.tableCitas.getTableHeader().getColumnModel().getColumn(pColumna).setMinWidth(0);
    }

    // Método privado para obtener el objeto Cita seleccionado de la fila de la tabla.
    private Cita getCitaFromTableRow() {
        Cita cita = null;
        try {
            int filaSelect = this.tableCitas.getSelectedRow();
            int id = 0;

            if (filaSelect != -1) {
                id = (int) this.tableCitas.getValueAt(filaSelect, 0);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Seleccionar una fila de la tabla.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            cita = citaDAO.getById(id);

            if (cita == null || cita.getId() == 0) {
                JOptionPane.showMessageDialog(this,
                        "No se encontró ninguna cita con el ID seleccionado.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            return cita;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al obtener cita de la tabla: " + ex.getMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return null;
        }
    }
}
package ale2025.presentacion;

import ale2025.persistencia.HorarioDAO;     // Importa la clase HorarioDAO.
import ale2025.persistencia.MedicoDAO;      // Para obtener el nombre del médico.
import ale2025.dominio.Horario;       // Importa la clase Horario.
import ale2025.dominio.Medico;        // Importa la clase Medico.
import ale2025.utils.CUD;           // Importa el enum CUD.

import javax.swing.*;                   // Importa Swing para GUI.
import javax.swing.table.DefaultTableModel; // Importa DefaultTableModel.
import java.awt.event.KeyAdapter;       // Para eventos de teclado.
import java.awt.event.KeyEvent;         // Para eventos de teclado.
import java.util.ArrayList;             // Para listas dinámicas.
import java.sql.SQLException;           // Para manejo de errores SQL.
import java.sql.Time;                   // Para manejar java.sql.Time

public class HorarioReadingForm extends JDialog {
    private JTextField txtDiaSemana;
    private JButton btnCreate;
    private JTable tableHorarios;
    private JButton btnDelete;
    private JButton btnUpdate;
    private JPanel mainPanel; // Asegúrate de que este panel sea el contenido principal del JDialog

    private HorarioDAO horarioDAO;
    private MedicoDAO medicoDAO; // Necesario para mostrar el nombre del médico
    private MainForm mainForm;

    public HorarioReadingForm(MainForm mainForm) {
        this.mainForm = mainForm;
        horarioDAO = new HorarioDAO();
        medicoDAO = new MedicoDAO(); // Inicializa MedicoDAO
        setContentPane(mainPanel);
        setModal(true);
        setTitle("Buscar Horario");
        pack();
        setLocationRelativeTo(mainForm);

        // Agrega un listener de teclado al campo de texto txtDiaSemana.
        txtDiaSemana.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (!txtDiaSemana.getText().trim().isEmpty()) {
                    search(txtDiaSemana.getText());
                } else {
                    DefaultTableModel emptyModel = new DefaultTableModel();
                    tableHorarios.setModel(emptyModel);
                }
            }
        });

        // Agrega un ActionListener al botón btnCreate.
        btnCreate.addActionListener(s -> {
            HorarioWriteForm horarioWriteForm = new HorarioWriteForm(this.mainForm, CUD.CREATE, new Horario());
            horarioWriteForm.setVisible(true);
            if (horarioWriteForm.getResult() && !txtDiaSemana.getText().trim().isEmpty()) {
                search(txtDiaSemana.getText());
            } else {
                DefaultTableModel emptyModel = new DefaultTableModel();
                tableHorarios.setModel(emptyModel);
            }
        });

        // Agrega un ActionListener al botón btnUpdate.
        btnUpdate.addActionListener(s -> {
            Horario horario = getHorarioFromTableRow();
            if (horario != null) {
                HorarioWriteForm horarioWriteForm = new HorarioWriteForm(this.mainForm, CUD.UPDATE, horario);
                horarioWriteForm.setVisible(true);
                if (horarioWriteForm.getResult() && !txtDiaSemana.getText().trim().isEmpty()) {
                    search(txtDiaSemana.getText());
                } else {
                    DefaultTableModel emptyModel = new DefaultTableModel();
                    tableHorarios.setModel(emptyModel);
                }
            }
        });

        // Agrega un ActionListener al botón btnDelete.
        btnDelete.addActionListener(s -> {
            Horario horario = getHorarioFromTableRow();
            if (horario != null) {
                HorarioWriteForm horarioWriteForm = new HorarioWriteForm(this.mainForm, CUD.DELETE, horario);
                horarioWriteForm.setVisible(true);
                if (horarioWriteForm.getResult() && !txtDiaSemana.getText().trim().isEmpty()) {
                    search(txtDiaSemana.getText());
                } else {
                    DefaultTableModel emptyModel = new DefaultTableModel();
                    tableHorarios.setModel(emptyModel);
                }
            }
        });
    }

    // Método privado para buscar horarios.
    private void search(String query) {
        try {
            ArrayList<Horario> horarios = horarioDAO.search(query);
            createTable(horarios); // Actualiza la tabla con los resultados.
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al buscar horarios: " + ex.getMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Método para crear y llenar la tabla de horarios.
    public void createTable(ArrayList<Horario> horarios) {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Ninguna celda es editable.
            }
        };

        // Define las columnas de la tabla.
        model.addColumn("Id");
        model.addColumn("Médico");       // Ahora mostraremos el nombre del médico
        model.addColumn("Día de la Semana");
        model.addColumn("Hora Inicio");
        model.addColumn("Hora Fin");

        this.tableHorarios.setModel(model);

        Object rowData[] = null;

        // Itera a través de la lista de horarios.
        for (int i = 0; i < horarios.size(); i++) {
            Horario horario = horarios.get(i);
            model.addRow(rowData);
            // Llena las celdas con los datos del horario.
            model.setValueAt(horario.getId(), i, 0);

            // Obtener el nombre del médico usando MedicoDAO
            String medicoNombre = "Desconocido";
            try {
                Medico medico = medicoDAO.getById(horario.getMedicoId());
                if (medico != null) {
                    medicoNombre = medico.getNombreCompleto();
                }
            } catch (SQLException e) {
                System.err.println("Error al obtener nombre de médico para Horario ID " + horario.getId() + ": " + e.getMessage());
                // Podrías mostrar un mensaje de error en la celda o dejarlo como "Desconocido"
            }
            model.setValueAt(medicoNombre, i, 1); // Muestra el nombre del médico
            model.setValueAt(horario.getDiaSemana(), i, 2);

            // Formatear las horas para una mejor visualización en la tabla
            String horaInicioStr = horario.getHoraInicio() != null ? horario.getHoraInicio().toLocalTime().toString() : "";
            String horaFinStr = horario.getHoraFin() != null ? horario.getHoraFin().toLocalTime().toString() : "";

            model.setValueAt(horaInicioStr, i, 3);
            model.setValueAt(horaFinStr, i, 4);
        }

        hideCol(0); // Oculta la columna del ID.
    }

    // Método privado para ocultar una columna de la tabla.
    private void hideCol(int pColumna) {
        this.tableHorarios.getColumnModel().getColumn(pColumna).setMaxWidth(0);
        this.tableHorarios.getColumnModel().getColumn(pColumna).setMinWidth(0);
        this.tableHorarios.getTableHeader().getColumnModel().getColumn(pColumna).setMaxWidth(0);
        this.tableHorarios.getTableHeader().getColumnModel().getColumn(pColumna).setMinWidth(0);
    }

    // Método privado para obtener el objeto Horario seleccionado de la fila de la tabla.
    private Horario getHorarioFromTableRow() {
        Horario horario = null;
        try {
            int filaSelect = this.tableHorarios.getSelectedRow();
            int id = 0;

            if (filaSelect != -1) {
                id = (int) this.tableHorarios.getValueAt(filaSelect, 0);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Seleccionar una fila de la tabla.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            horario = horarioDAO.getById(id);

            if (horario == null || horario.getId() == 0) {
                JOptionPane.showMessageDialog(this,
                        "No se encontró ningún horario con el ID seleccionado.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            return horario;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al obtener horario de la tabla: " + ex.getMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return null;
        }
    }
}
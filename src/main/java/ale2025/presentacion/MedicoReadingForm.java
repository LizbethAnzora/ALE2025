package ale2025.presentacion;

import ale2025.persistencia.MedicoDAO;     // Importa la clase MedicoDAO.
import ale2025.persistencia.EspecialidadDAO; // Para obtener el nombre de la especialidad
import ale2025.dominio.Medico;       // Importa la clase Medico.
import ale2025.dominio.Especialidad;  // Importa la clase Especialidad.
import ale2025.utils.CUD;           // Importa el enum CUD.

import javax.swing.*;                   // Importa Swing para GUI.
import javax.swing.table.DefaultTableModel; // Importa DefaultTableModel.
import java.awt.event.KeyAdapter;       // Para eventos de teclado.
import java.awt.event.KeyEvent;         // Para eventos de teclado.
import java.util.ArrayList;             // Para listas dinámicas.
import java.sql.SQLException;           // Para manejo de errores SQL.

public class MedicoReadingForm extends JDialog {
    private JPanel mainPanel;
    private JTextField txtNombreCompleto;
    private JButton btnCreate;
    private JTable tableMedicos;
    private JButton btnUpdate;
    private JButton btnDelete;

    private MedicoDAO medicoDAO;
    private EspecialidadDAO especialidadDAO; // Necesario para mostrar el nombre de la especialidad
    private MainForm mainForm;

    public MedicoReadingForm(MainForm mainForm) {
        this.mainForm = mainForm;
        medicoDAO = new MedicoDAO();
        especialidadDAO = new EspecialidadDAO(); // Inicializa EspecialidadDAO
        setContentPane(mainPanel);
        setModal(true);
        setTitle("Buscar Médico");
        pack();
        setLocationRelativeTo(mainForm);

        // Agrega un listener de teclado al campo de texto txtNombreCompleto.
        txtNombreCompleto.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (!txtNombreCompleto.getText().trim().isEmpty()) {
                    search(txtNombreCompleto.getText());
                } else {
                    DefaultTableModel emptyModel = new DefaultTableModel();
                    tableMedicos.setModel(emptyModel);
                }
            }
        });

        // Agrega un ActionListener al botón btnCreate.
        btnCreate.addActionListener(s -> {
            MedicoWriteForm medicoWriteForm = new MedicoWriteForm(this.mainForm, CUD.CREATE, new Medico());
            medicoWriteForm.setVisible(true);
            if (medicoWriteForm.getResult() && !txtNombreCompleto.getText().trim().isEmpty()) {
                search(txtNombreCompleto.getText());
            } else {
                DefaultTableModel emptyModel = new DefaultTableModel();
                tableMedicos.setModel(emptyModel);
            }
        });

        // Agrega un ActionListener al botón btnUpdate.
        btnUpdate.addActionListener(s -> {
            Medico medico = getMedicoFromTableRow();
            if (medico != null) {
                MedicoWriteForm medicoWriteForm = new MedicoWriteForm(this.mainForm, CUD.UPDATE, medico);
                medicoWriteForm.setVisible(true);
                if (medicoWriteForm.getResult() && !txtNombreCompleto.getText().trim().isEmpty()) {
                    search(txtNombreCompleto.getText());
                } else {
                    DefaultTableModel emptyModel = new DefaultTableModel();
                    tableMedicos.setModel(emptyModel);
                }
            }
        });

        // Agrega un ActionListener al botón btnDelete.
        btnDelete.addActionListener(s -> {
            Medico medico = getMedicoFromTableRow();
            if (medico != null) {
                MedicoWriteForm medicoWriteForm = new MedicoWriteForm(this.mainForm, CUD.DELETE, medico);
                medicoWriteForm.setVisible(true);
                if (medicoWriteForm.getResult() && !txtNombreCompleto.getText().trim().isEmpty()) {
                    search(txtNombreCompleto.getText());
                } else {
                    DefaultTableModel emptyModel = new DefaultTableModel();
                    tableMedicos.setModel(emptyModel);
                }
            }
        });
    }

    // Método privado para buscar médicos.
    private void search(String query) {
        try {
            ArrayList<Medico> medicos = medicoDAO.search(query);
            createTable(medicos); // Actualiza la tabla con los resultados.
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al buscar médicos: " + ex.getMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Método para crear y llenar la tabla de médicos.
    public void createTable(ArrayList<Medico> medicos) {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Ninguna celda es editable.
            }
        };

        // Define las columnas de la tabla.
        model.addColumn("Id");
        model.addColumn("Nombre Completo");
        model.addColumn("Especialidad"); // Ahora mostraremos el nombre de la especialidad
        model.addColumn("Sueldo");

        this.tableMedicos.setModel(model);

        Object rowData[] = null;

        // Itera a través de la lista de médicos.
        for (int i = 0; i < medicos.size(); i++) {
            Medico medico = medicos.get(i);
            model.addRow(rowData);
            // Llena las celdas con los datos del médico.
            model.setValueAt(medico.getId(), i, 0);
            model.setValueAt(medico.getNombreCompleto(), i, 1);

            // Obtener el nombre de la especialidad usando EspecialidadDAO
            String especialidadNombre = "Desconocida";
            try {
                Especialidad especialidad = especialidadDAO.getById(medico.getEspecialidadId());
                if (especialidad != null) {
                    especialidadNombre = especialidad.getNombre();
                }
            } catch (SQLException e) {
                System.err.println("Error al obtener nombre de especialidad para Medico ID " + medico.getId() + ": " + e.getMessage());
                // Podrías mostrar un mensaje de error en la celda o dejarlo como "Desconocida"
            }
            model.setValueAt(especialidadNombre, i, 2); // Muestra el nombre de la especialidad
            model.setValueAt(medico.getSueldo(), i, 3);
        }

        hideCol(0); // Oculta la columna del ID.
    }

    // Método privado para ocultar una columna de la tabla.
    private void hideCol(int pColumna) {
        this.tableMedicos.getColumnModel().getColumn(pColumna).setMaxWidth(0);
        this.tableMedicos.getColumnModel().getColumn(pColumna).setMinWidth(0);
        this.tableMedicos.getTableHeader().getColumnModel().getColumn(pColumna).setMaxWidth(0);
        this.tableMedicos.getTableHeader().getColumnModel().getColumn(pColumna).setMinWidth(0);
    }

    // Método privado para obtener el objeto Medico seleccionado de la fila de la tabla.
    private Medico getMedicoFromTableRow() {
        Medico medico = null;
        try {
            int filaSelect = this.tableMedicos.getSelectedRow();
            int id = 0;

            if (filaSelect != -1) {
                id = (int) this.tableMedicos.getValueAt(filaSelect, 0);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Seleccionar una fila de la tabla.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            medico = medicoDAO.getById(id);

            if (medico == null || medico.getId() == 0) {
                JOptionPane.showMessageDialog(this,
                        "No se encontró ningún médico con el ID seleccionado.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            return medico;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al obtener médico de la tabla: " + ex.getMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return null;
        }
    }
}
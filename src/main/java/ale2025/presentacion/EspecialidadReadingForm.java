package ale2025.presentacion;

import ale2025.persistencia.EspecialidadDAO; // Importa la clase EspecialidadDAO.
import javax.swing.*;                   // Importa Swing para GUI.
import javax.swing.table.DefaultTableModel; // Importa DefaultTableModel para la tabla.
import ale2025.dominio.Especialidad;     // Importa la clase Especialidad.
import ale2025.utils.CUD;           // Importa el enum CUD.

import java.awt.event.KeyAdapter;       // Para eventos de teclado.
import java.awt.event.KeyEvent;         // Para eventos de teclado.
import java.util.ArrayList;             // Para listas dinámicas.
// No se necesita java.sql.Date para Especialidad

public class EspecialidadReadingForm extends JDialog {
    private JPanel mainPanel;          // Panel principal.
    private JTextField txtNombre;      // Campo de texto para buscar por nombre de especialidad.
    private JButton btnCreate;         // Botón para crear nueva especialidad.
    private JTable tableEspecialidades; // Tabla para mostrar especialidades.
    private JButton btnUpdate;         // Botón para actualizar especialidad.
    private JButton btnDelete;         // Botón para eliminar especialidad.

    private EspecialidadDAO especialidadDAO; // Instancia de EspecialidadDAO.
    private MainForm mainForm;           // Referencia a la ventana principal.

    // Constructor de la clase EspecialidadReadingForm.
    public EspecialidadReadingForm(MainForm mainForm) {
        this.mainForm = mainForm; // Asigna la instancia de MainForm.
        especialidadDAO = new EspecialidadDAO(); // Crea una nueva instancia de EspecialidadDAO.

        setContentPane(mainPanel); // Establece el panel principal como contenido del diálogo.
        setModal(true); // Diálogo modal.
        setTitle("Buscar Especialidad"); // Título de la ventana.
        pack(); // Ajusta tamaño.
        setLocationRelativeTo(mainForm); // Centra respecto a MainForm.

        // Agrega un listener de teclado al campo de texto txtNombre.
        txtNombre.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (!txtNombre.getText().trim().isEmpty()) {
                    search(txtNombre.getText()); // Busca si el campo no está vacío.
                } else {
                    // Si el campo está vacío, limpia la tabla.
                    DefaultTableModel emptyModel = new DefaultTableModel();
                    tableEspecialidades.setModel(emptyModel);
                }
            }
        });

        // Agrega un ActionListener al botón btnCreate.
        btnCreate.addActionListener(s -> {
            // Abre EspecialidadWriteForm en modo CREATE.
            EspecialidadWriteForm especialidadWriteForm = new EspecialidadWriteForm(this.mainForm, CUD.CREATE, new Especialidad());
            especialidadWriteForm.setVisible(true);
            // Refresca la tabla si la operación fue exitosa y hay texto de búsqueda, o limpia.
            if (especialidadWriteForm.getResult() && !txtNombre.getText().trim().isEmpty()) {
                search(txtNombre.getText());
            } else {
                DefaultTableModel emptyModel = new DefaultTableModel();
                tableEspecialidades.setModel(emptyModel);
            }
        });

        // Agrega un ActionListener al botón btnUpdate.
        btnUpdate.addActionListener(s -> {
            Especialidad especialidad = getEspecialidadFromTableRow(); // Obtiene la especialidad seleccionada.
            if (especialidad != null) {
                // Abre EspecialidadWriteForm en modo UPDATE.
                EspecialidadWriteForm especialidadWriteForm = new EspecialidadWriteForm(this.mainForm, CUD.UPDATE, especialidad);
                especialidadWriteForm.setVisible(true);
                // Refresca la tabla si la operación fue exitosa y hay texto de búsqueda, o limpia.
                if (especialidadWriteForm.getResult() && !txtNombre.getText().trim().isEmpty()) {
                    search(txtNombre.getText());
                } else {
                    DefaultTableModel emptyModel = new DefaultTableModel();
                    tableEspecialidades.setModel(emptyModel);
                }
            }
        });

        // Agrega un ActionListener al botón btnDelete.
        btnDelete.addActionListener(s -> {
            Especialidad especialidad = getEspecialidadFromTableRow(); // Obtiene la especialidad seleccionada.
            if (especialidad != null) {
                // Abre EspecialidadWriteForm en modo DELETE.
                EspecialidadWriteForm especialidadWriteForm = new EspecialidadWriteForm(this.mainForm, CUD.DELETE, especialidad);
                especialidadWriteForm.setVisible(true);
                // Refresca la tabla si la operación fue exitosa y hay texto de búsqueda, o limpia.
                if (especialidadWriteForm.getResult() && !txtNombre.getText().trim().isEmpty()) {
                    search(txtNombre.getText());
                } else {
                    DefaultTableModel emptyModel = new DefaultTableModel();
                    tableEspecialidades.setModel(emptyModel);
                }
            }
        });
    }

    // Método privado para buscar especialidades.
    private void search(String query) {
        try {
            ArrayList<Especialidad> especialidades = especialidadDAO.search(query);
            createTable(especialidades); // Actualiza la tabla con los resultados.
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al buscar especialidades: " + ex.getMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Método para crear y llenar la tabla de especialidades.
    public void createTable(ArrayList<Especialidad> especialidades) {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Ninguna celda es editable.
            }
        };

        // Define las columnas de la tabla.
        model.addColumn("Id");
        model.addColumn("Nombre");
        model.addColumn("Descripción");

        this.tableEspecialidades.setModel(model);

        Object rowData[] = null;

        // Itera a través de la lista de especialidades.
        for (int i = 0; i < especialidades.size(); i++) {
            Especialidad especialidad = especialidades.get(i);
            model.addRow(rowData);
            // Llena las celdas con los datos de la especialidad.
            model.setValueAt(especialidad.getId(), i, 0);
            model.setValueAt(especialidad.getNombre(), i, 1);
            model.setValueAt(especialidad.getDescripcion(), i, 2);
        }

        hideCol(0); // Oculta la columna del ID.
    }

    // Método privado para ocultar una columna de la tabla.
    private void hideCol(int pColumna) {
        this.tableEspecialidades.getColumnModel().getColumn(pColumna).setMaxWidth(0);
        this.tableEspecialidades.getColumnModel().getColumn(pColumna).setMinWidth(0);
        this.tableEspecialidades.getTableHeader().getColumnModel().getColumn(pColumna).setMaxWidth(0);
        this.tableEspecialidades.getTableHeader().getColumnModel().getColumn(pColumna).setMinWidth(0);
    }

    // Método privado para obtener el objeto Especialidad seleccionado de la fila de la tabla.
    private Especialidad getEspecialidadFromTableRow() {
        Especialidad especialidad = null;
        try {
            int filaSelect = this.tableEspecialidades.getSelectedRow();
            int id = 0;

            if (filaSelect != -1) {
                id = (int) this.tableEspecialidades.getValueAt(filaSelect, 0);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Seleccionar una fila de la tabla.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            especialidad = especialidadDAO.getById(id);

            if (especialidad == null || especialidad.getId() == 0) {
                JOptionPane.showMessageDialog(this,
                        "No se encontró ninguna especialidad con el ID seleccionado.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            return especialidad;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al obtener especialidad de la tabla: " + ex.getMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return null;
        }
    }
}
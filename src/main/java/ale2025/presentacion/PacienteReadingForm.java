package ale2025.presentacion;

import ale2025.dominio.Paciente; // Importa la clase Paciente.
import ale2025.persistencia.PacienteDAO; // Importa la clase PacienteDAO.
import ale2025.utils.CUD; // Importa el enum CUD (Create, Update, Delete).

import javax.swing.*; // Importa el paquete Swing para GUI.
import javax.swing.table.DefaultTableModel; // Importa DefaultTableModel para manejar la tabla.

import java.awt.event.KeyAdapter; // Importa KeyAdapter para eventos de teclado.
import java.awt.event.KeyEvent; // Importa KeyEvent para eventos de teclado.
import java.sql.Date; // Importa java.sql.Date para el manejo de fechas desde la base de datos.
import java.util.ArrayList; // Importa ArrayList para listas dinámicas.
import java.sql.SQLException; // Importa SQLException para manejo de errores de base de datos.

public class PacienteReadingForm extends JDialog {
    private JPanel mainPanel;
    private JTextField txtNombreCompleto; // Campo de texto para buscar por nombre.
    private JButton btnCreate;
    private JTable tablePacientes; // Tabla para mostrar los pacientes.
    private JButton btnUpdate;
    private JButton btnDelete;

    private PacienteDAO pacienteDAO; // Instancia de PacienteDAO para operaciones de base de datos.
    private MainForm mainForm; // Referencia a la ventana principal de la aplicación.

    // Constructor de la clase PacienteReadingForm. Recibe una instancia de MainForm.
    public PacienteReadingForm(MainForm mainForm) {
        // Llama al constructor de la clase padre JDialog.
        super(mainForm, "Gestión de Pacientes", true); // Título de la ventana.
        this.mainForm = mainForm; // Asigna la instancia de MainForm.
        pacienteDAO = new PacienteDAO(); // Crea una nueva instancia de PacienteDAO.

        setContentPane(mainPanel); // Establece el panel principal.
        setModal(true); // Hace que este diálogo sea modal.
        setTitle("Buscar Paciente"); // Establece el título de la ventana.
        pack(); // Ajusta el tamaño de la ventana.
        setLocationRelativeTo(mainForm); // Centra la ventana respecto a la principal.

        // Agrega un listener de teclado al campo de texto txtNombreCompleto.
        txtNombreCompleto.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Cuando se suelta una tecla, si el campo no está vacío, realiza la búsqueda.
                if (!txtNombreCompleto.getText().trim().isEmpty()) {
                    search(txtNombreCompleto.getText());
                } else {
                    // Si el campo está vacío, limpia la tabla.
                    DefaultTableModel emptyModel = new DefaultTableModel();
                    tablePacientes.setModel(emptyModel);
                }
            }
        });

        // Agrega un ActionListener al botón btnCreate.
        btnCreate.addActionListener(s -> {
            // Crea una nueva instancia de PacienteWriteForm para la creación.
            PacienteWriteForm pacienteWriteForm = new PacienteWriteForm(this.mainForm, CUD.CREATE, new Paciente());
            pacienteWriteForm.setVisible(true); // Hace visible el formulario de escritura.
            // Limpia y recarga la tabla si se desea refrescar después de la creación.
            // Para mantener la consistencia con UserReadingForm, limpiaremos la tabla.
            DefaultTableModel emptyModel = new DefaultTableModel();
            tablePacientes.setModel(emptyModel);
            // Opcional: Si quieres que la búsqueda se realice automáticamente después de cerrar el write form,
            // y si el campo de texto tiene algo, podrías llamar a search(txtNombreCompleto.getText());
            // Si no, la tabla se mantendrá vacía hasta que el usuario escriba algo.
        });

        // Agrega un ActionListener al botón btnUpdate.
        btnUpdate.addActionListener(s -> {
            // Obtiene el paciente seleccionado de la tabla.
            Paciente paciente = getPacienteFromTableRow();
            if (paciente != null) {
                // Crea una nueva instancia de PacienteWriteForm para la actualización.
                PacienteWriteForm pacienteWriteForm = new PacienteWriteForm(this.mainForm, CUD.UPDATE, paciente);
                pacienteWriteForm.setVisible(true); // Hace visible el formulario de escritura.
                // Limpia y recarga la tabla después de la actualización.
                DefaultTableModel emptyModel = new DefaultTableModel();
                tablePacientes.setModel(emptyModel);
                // Opcional: Actualizar la búsqueda si el campo de texto tiene algo
                if (!txtNombreCompleto.getText().trim().isEmpty()) {
                    search(txtNombreCompleto.getText());
                }
            }
        });

        // Agrega un ActionListener al botón btnDelete.
        btnDelete.addActionListener(s -> {
            // Obtiene el paciente seleccionado de la tabla.
            Paciente paciente = getPacienteFromTableRow();
            if (paciente != null) {
                // Crea una nueva instancia de PacienteWriteForm para la eliminación.
                PacienteWriteForm pacienteWriteForm = new PacienteWriteForm(this.mainForm, CUD.DELETE, paciente);
                pacienteWriteForm.setVisible(true); // Hace visible el formulario de escritura.
                // Limpia y recarga la tabla después de la eliminación.
                DefaultTableModel emptyModel = new DefaultTableModel();
                tablePacientes.setModel(emptyModel);
                // Opcional: Actualizar la búsqueda si el campo de texto tiene algo
                if (!txtNombreCompleto.getText().trim().isEmpty()) {
                    search(txtNombreCompleto.getText());
                }
            }
        });
    }

    // Método para buscar pacientes por nombre.
    private void search(String query) {
        try {
            ArrayList<Paciente> pacientes = pacienteDAO.search(query); // Llama al DAO para buscar.
            createTable(pacientes); // Actualiza la tabla con los resultados.
        } catch (SQLException ex) { // Captura excepciones SQL.
            JOptionPane.showMessageDialog(null,
                    "Error al buscar pacientes: " + ex.getMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Imprime la traza para depuración.
        } catch (Exception ex) { // Captura otras excepciones.
            JOptionPane.showMessageDialog(null,
                    "Ha ocurrido un error inesperado durante la búsqueda: " + ex.getMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Método para crear y popular la tabla de pacientes.
    public void createTable(ArrayList<Paciente> pacientes) {
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hace que las celdas de la tabla no sean editables.
            }
        };

        // Define las columnas de la tabla para Paciente.
        model.addColumn("Id");
        model.addColumn("NombreCompleto");
        model.addColumn("Teléfono");
        model.addColumn("FechaNacimiento");

        this.tablePacientes.setModel(model); // Asigna el modelo a la tabla.

        Object row[] = null;

        // Itera a través de la lista de pacientes y agrega los datos a la tabla.
        for (int i = 0; i < pacientes.size(); i++) {
            Paciente paciente = pacientes.get(i);
            model.addRow(row); // Agrega una nueva fila vacía.
            model.setValueAt(paciente.getId(), i, 0); // ID
            model.setValueAt(paciente.getNombreCompleto(), i, 1); // Nombre Completo
            model.setValueAt(paciente.getTelefono(), i, 2); // Teléfono
            model.setValueAt(paciente.getFechaNacimiento(), i, 3); // Fecha de Nacimiento
        }

        hideCol(0); // Oculta la columna del ID.
    }

    // Método para ocultar una columna en la tabla.
    private void hideCol(int pColumna) {
        this.tablePacientes.getColumnModel().getColumn(pColumna).setMaxWidth(0);
        this.tablePacientes.getColumnModel().getColumn(pColumna).setMinWidth(0);
        this.tablePacientes.getTableHeader().getColumnModel().getColumn(pColumna).setMaxWidth(0);
        this.tablePacientes.getTableHeader().getColumnModel().getColumn(pColumna).setMinWidth(0);
    }

    // Método para obtener el objeto Paciente de la fila seleccionada de la tabla.
    private Paciente getPacienteFromTableRow() {
        Paciente paciente = null;
        try {
            int filaSelect = this.tablePacientes.getSelectedRow();
            int id = 0;

            if (filaSelect != -1) {
                id = (int) this.tablePacientes.getValueAt(filaSelect, 0); // Obtiene el ID de la columna oculta.
            } else {
                JOptionPane.showMessageDialog(null,
                        "Seleccione una fila de la tabla.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            // Llama al método 'getById' del PacienteDAO para obtener el objeto Paciente completo.
            paciente = pacienteDAO.getById(id);

            if (paciente == null || paciente.getId() == 0) { // Verifica si el paciente no fue encontrado.
                JOptionPane.showMessageDialog(null,
                        "No se encontró ningún paciente con el ID seleccionado.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return null;
            }

            return paciente; // Retorna el paciente encontrado.
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(null,
                    "Error al obtener paciente de la tabla: " + ex.getMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return null;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Ha ocurrido un error inesperado al obtener paciente de la tabla: " + ex.getMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
            return null;
        }
    }
}
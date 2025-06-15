package ale2025.presentacion;

import ale2025.dominio.Especialidad; // Importa la clase Especialidad.
import ale2025.persistencia.EspecialidadDAO; // Importa la clase EspecialidadDAO.
import ale2025.utils.CUD; // Importa el enum CUD (Create, Update, Delete).

import javax.swing.*; // Importa el paquete Swing para GUI.
import java.sql.SQLException; // Importa SQLException para manejo de errores de base de datos.
// No se necesita java.sql.Date ni java.time.LocalDate para Especialidad
// No se necesita com.github.lgooddatepicker.components.DatePicker para Especialidad

public class EspecialidadWriteForm extends JDialog {
    private JPanel mainPanel;      // Panel principal del formulario.
    private JTextField txtNombre;  // Campo de texto para el nombre de la especialidad.
    private JTextField txtDescripcion; // Campo de texto para la descripción de la especialidad.
    private JButton btnOk;         // Botón para confirmar la operación (Guardar/Eliminar).
    private JButton btnCancel;     // Botón para cancelar y cerrar el formulario.

    private EspecialidadDAO especialidadDAO; // Instancia de la clase EspecialidadDAO para interactuar con la base de datos de especialidades.
    private MainForm mainForm;             // Referencia a la ventana principal de la aplicación.
    private CUD cud;                       // Variable para almacenar el tipo de operación (Create, Update, Delete).
    private Especialidad en;               // Variable para almacenar el objeto Especialidad que se está creando/actualizando/eliminando.

    // Variable para indicar si la operación fue exitosa (usado por el formulario de lectura para refrescar).
    private boolean result = false;

    // Constructor de la clase EspecialidadWriteForm.
    // Recibe la ventana principal, el tipo de operación CUD y un objeto Especialidad como parámetros.
    public EspecialidadWriteForm(MainForm mainForm, CUD cud, Especialidad especialidad) {
        super(mainForm, "Gestión de Especialidades", true); // Título genérico, se ajusta en init()
        this.cud = cud; // Asigna el tipo de operación CUD.
        this.en = especialidad; // Asigna el objeto Especialidad.
        this.mainForm = mainForm; // Asigna la instancia de MainForm.
        especialidadDAO = new EspecialidadDAO(); // Crea una nueva instancia de EspecialidadDAO.

        setContentPane(mainPanel); // Establece el panel principal como el contenido de este diálogo.
        setModal(true); // Hace que este diálogo sea modal.

        init(); // Llama al método 'init' para inicializar y configurar el formulario.
        pack(); // Ajusta el tamaño de la ventana.
        setLocationRelativeTo(mainForm); // Centra la ventana del diálogo relativo a la ventana principal.

        // Agrega un ActionListener al botón 'btnCancel' para cerrar la ventana.
        btnCancel.addActionListener(s -> this.dispose());
        // Agrega un ActionListener al botón 'btnOk' para ejecutar la acción de guardar/actualizar/eliminar.
        btnOk.addActionListener(s -> ok());
    }

    private void init() {
        // Realiza acciones específicas en la interfaz de usuario basadas en el tipo de operación (CUD).
        switch (this.cud) {
            case CREATE:
                setTitle("Crear Especialidad");
                btnOk.setText("Guardar");
                break;
            case UPDATE:
                setTitle("Modificar Especialidad");
                btnOk.setText("Guardar");
                break;
            case DELETE:
                setTitle("Eliminar Especialidad");
                btnOk.setText("Eliminar");
                break;
        }

        // Llama al método 'setValuesControls' para llenar los campos del formulario
        // con los valores del objeto Especialidad proporcionado ('this.en').
        setValuesControls(this.en);
    }

    private void setValuesControls(Especialidad especialidad) {
        // Llena el campo de texto 'txtNombre' con el nombre de la especialidad.
        txtNombre.setText(especialidad.getNombre());

        // Llena el campo de texto 'txtDescripcion' con la descripción de la especialidad.
        txtDescripcion.setText(especialidad.getDescripcion());

        // Si la operación actual es la eliminación de una especialidad (CUD.DELETE).
        if (this.cud == CUD.DELETE) {
            // Deshabilita la edición de los campos para evitar modificaciones antes de eliminar.
            txtNombre.setEditable(false);
            txtDescripcion.setEditable(false);
        }
    }

    private boolean getValuesControls() {
        boolean res = false; // Inicializa a false.

        // Realiza una serie de validaciones en los campos de entrada:

        // 1. Verifica si el campo de texto 'txtNombre' está vacío.
        if (txtNombre.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El campo 'Nombre' es obligatorio.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return res; // Retorna false.
        }
        // 2. Verifica si el campo de texto 'txtDescripcion' está vacío.
        else if (txtDescripcion.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El campo 'Descripción' es obligatorio.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return res; // Retorna false.
        }
        // 3. Verifica el ID para operaciones de actualización/eliminación.
        else if (this.cud != CUD.CREATE && this.en.getId() == 0) {
            JOptionPane.showMessageDialog(this,
                    "ID de especialidad inválido para esta operación.",
                    "Error de Datos", JOptionPane.ERROR_MESSAGE);
            return res; // Retorna false.
        }

        // Si todas las validaciones anteriores pasan, los datos son válidos.
        res = true;

        // Actualiza los atributos del objeto Especialidad 'en' con los valores ingresados:
        this.en.setNombre(txtNombre.getText().trim());
        this.en.setDescripcion(txtDescripcion.getText().trim());

        return res;
    }

    private void ok() {
        try {
            // Obtener y validar los valores de los controles del formulario.
            boolean res = getValuesControls();

            // Si la validación de los controles fue exitosa.
            if (res) {
                boolean r = false; // Variable para almacenar el resultado de la operación de la base de datos.

                // Realiza la operación de la base de datos según el tipo de operación actual.
                switch (this.cud) {
                    case CREATE:
                        // Caso de creación de una nueva especialidad.
                        Especialidad especialidadCreada = especialidadDAO.create(this.en);
                        // Verifica si la creación fue exitosa comprobando si la nueva especialidad tiene un ID asignado.
                        if (especialidadCreada != null && especialidadCreada.getId() > 0) {
                            this.en.setId(especialidadCreada.getId()); // Asigna el ID generado al objeto 'en'
                            r = true; // Establece 'r' a true si la creación fue exitosa.
                        }
                        break;
                    case UPDATE:
                        // Caso de actualización de una especialidad existente.
                        r = especialidadDAO.update(this.en); // 'r' será true si la actualización fue exitosa.
                        break;
                    case DELETE:
                        // Caso de eliminación de una especialidad.
                        r = especialidadDAO.delete(this.en); // 'r' será true si la eliminación fue exitosa.
                        break;
                }

                // Si la operación de la base de datos fue exitosa.
                if (r) {
                    JOptionPane.showMessageDialog(this,
                            "Transacción realizada exitosamente",
                            "Información", JOptionPane.INFORMATION_MESSAGE);
                    this.result = true; // Establece el resultado a true para indicar éxito.
                    this.dispose(); // Cierra la ventana actual.
                } else {
                    // Si la operación de la base de datos falló.
                    JOptionPane.showMessageDialog(this,
                            "No se logró realizar ninguna acción",
                            "ERROR", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            // Captura cualquier excepción SQL que ocurra.
            JOptionPane.showMessageDialog(this,
                    "Error de base de datos: " + ex.getMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Imprime la traza de la pila para depuración.
        } catch (Exception ex) {
            // Captura cualquier otra excepción.
            JOptionPane.showMessageDialog(this,
                    "Ha ocurrido un error inesperado: " + ex.getMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Imprime la traza de la pila para depuración.
        }
    }

    // Método para permitir que el formulario de lectura sepa si la operación fue exitosa.
    public boolean getResult() {
        return result;
    }
}
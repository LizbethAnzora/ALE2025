package ale2025.presentacion;

import ale2025.dominio.Paciente; // Importa la clase Paciente, que representa la entidad de paciente en el dominio.
import ale2025.persistencia.PacienteDAO; // Importa la interfaz o clase PacienteDAO, que define las operaciones de acceso a datos para la entidad Paciente.
import ale2025.utils.CUD; // Importa el enum CUD (Create, Update, Delete), para indicar el tipo de operación.

import com.github.lgooddatepicker.components.DatePicker; // Importa la clase DatePicker de LGoodDatePicker.
import com.github.lgooddatepicker.components.DatePickerSettings; // Importa DatePickerSettings para configurar el DatePicker.

import javax.swing.*; // Importa el paquete Swing para GUI.
import java.awt.*; // Importa AWT para LayoutManager y BorderLayout.
import java.time.LocalDate; // Importa LocalDate para manejar fechas modernas de Java.
import java.sql.Date; // Importa java.sql.Date para la conversión hacia y desde la base de datos.
import java.sql.SQLException; // Importa SQLException para manejo de errores de base de datos.

public class PacienteWriteForm extends JDialog {
    private JPanel mainPanel;
    private JTextField txtNombreCompleto;
    private JTextField txtTelefono;
    private JPanel panelFechaNacimientoContainer; // Tu JPanel vacío para el DatePicker
    private JButton btnOk;
    private JButton btnCancel;

    private PacienteDAO pacienteDAO; // Instancia de la clase PacienteDAO para interactuar con la base de datos de pacientes.
    private MainForm mainForm; // Referencia a la ventana principal de la aplicación (se asume que existe).
    private CUD cud; // Variable para almacenar el tipo de operación (Create, Update, Delete) que se está realizando.
    private Paciente en; // Variable para almacenar el objeto Paciente que se está creando, actualizando o eliminando.

    // Declaración de la variable para el DatePicker
    private DatePicker datePickerFechaNacimiento;

    // Constructor de la clase PacienteWriteForm.
    // Recibe la ventana principal, el tipo de operación CUD y un objeto Paciente como parámetros.
    public PacienteWriteForm(MainForm mainForm, CUD cud, Paciente paciente) {
        // Llama al constructor de la clase padre JDialog.
        // Se asume que MainForm es una subclase de JFrame o similar, para pasarla como 'owner'.
        // Si no tienes MainForm o es diferente, podrías usar 'null' o un 'JFrame' padre.
        super(mainForm, "Gestión de Pacientes", true); // Título genérico, se ajusta en init()
        this.cud = cud; // Asigna el tipo de operación CUD.
        this.en = paciente; // Asigna el objeto Paciente.
        this.mainForm = mainForm; // Asigna la instancia de MainForm.
        pacienteDAO = new PacienteDAO(); // Crea una nueva instancia de PacienteDAO.

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
        // -----------------------------------------------------------------------------------
        // Inicialización del DatePicker (Adaptado de la discusión anterior)
        // -----------------------------------------------------------------------------------
        datePickerFechaNacimiento = new DatePicker();
        DatePickerSettings settings = new DatePickerSettings();
        settings.setFormatForDatesCommonEra("dd/MM/yyyy"); // Formato día/mes/año
        // Opcional: No permite fechas futuras para una fecha de nacimiento
        // settings.setDateRangeLimits(null, LocalDate.now());
        datePickerFechaNacimiento.setSettings(settings);

        // Es crucial que este JPanel tenga un LayoutManager para que el DatePicker se muestre correctamente.
        panelFechaNacimientoContainer.setLayout(new BorderLayout());
        // Añade el DatePicker al JPanel contenedor
        panelFechaNacimientoContainer.add(datePickerFechaNacimiento, BorderLayout.CENTER);
        // -----------------------------------------------------------------------------------
        // Fin de la inicialización del DatePicker
        // -----------------------------------------------------------------------------------


        // Realiza acciones específicas en la interfaz de usuario basadas en el tipo de operación (CUD).
        switch (this.cud) {
            case CREATE:
                setTitle("Crear Paciente");
                btnOk.setText("Guardar");
                break;
            case UPDATE:
                setTitle("Modificar Paciente");
                btnOk.setText("Guardar");
                break;
            case DELETE:
                setTitle("Eliminar Paciente");
                btnOk.setText("Eliminar");
                break;
        }

        // Llama al método 'setValuesControls' para llenar los campos del formulario
        // con los valores del objeto Paciente proporcionado ('this.en').
        setValuesControls(this.en);
    }

    // No necesitamos initCBStatus para Paciente ya que no hay ComboBox de estatus.
    // private void initCBStatus() { ... }

    private void setValuesControls(Paciente paciente) {
        // Llena el campo de texto 'txtNombreCompleto' con el nombre del paciente.
        txtNombreCompleto.setText(paciente.getNombreCompleto()); // Corregido: asumo que txtName es txtNombreCompleto

        // Llena el campo de texto 'txtTelefono' con el teléfono del paciente.
        txtTelefono.setText(paciente.getTelefono());

        // Llena el DatePicker con la fecha de nacimiento del paciente.
        // Convierte java.sql.Date a java.time.LocalDate para el DatePicker.
        if (paciente.getFechaNacimiento() != null) {
            datePickerFechaNacimiento.setDate(paciente.getFechaNacimiento().toLocalDate());
        } else {
            // Opcional: Si es una creación, puedes establecer una fecha inicial, por ejemplo, la actual.
            if (this.cud == CUD.CREATE) {
                datePickerFechaNacimiento.setDate(LocalDate.now());
            } else {
                datePickerFechaNacimiento.clear(); // O limpiar si no hay fecha
            }
        }

        // Si la operación actual es la eliminación de un paciente (CUD.DELETE).
        if (this.cud == CUD.DELETE) {
            // Deshabilita la edición de los campos para evitar modificaciones.
            txtNombreCompleto.setEditable(false);
            txtTelefono.setEditable(false);
            datePickerFechaNacimiento.setEnabled(false); // Deshabilita el DatePicker
        }
        // No hay necesidad de ocultar campos de contraseña o estatus, ya que no existen para Paciente.
    }

    private boolean getValuesControls() {
        boolean res = false; // Inicializa a false.

        // Obtener la fecha del DatePicker
        LocalDate localDateFechaNacimiento = datePickerFechaNacimiento.getDate();
        // Convertir LocalDate a java.sql.Date para el objeto Paciente
        Date sqlDateFechaNacimiento = null;
        if (localDateFechaNacimiento != null) {
            sqlDateFechaNacimiento = Date.valueOf(localDateFechaNacimiento);
        }

        // Realiza una serie de validaciones en los campos de entrada:

        // 1. Verifica si el campo de texto 'txtNombreCompleto' está vacío.
        if (txtNombreCompleto.getText().trim().isEmpty()) {
            // Muestra mensaje de validación directamente aquí para consistencia con el flujo del UserWriteForm.
            JOptionPane.showMessageDialog(this,
                    "El campo 'Nombre Completo' es obligatorio.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return res; // Retorna false.
        }
        // 2. Verifica si el campo de texto 'txtTelefono' está vacío.
        else if (txtTelefono.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El campo 'Teléfono' es obligatorio.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return res; // Retorna false.
        }
        // 3. Verifica si se ha seleccionado una fecha de nacimiento.
        else if (sqlDateFechaNacimiento == null) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar una fecha de nacimiento.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return res; // Retorna false.
        }
        // 4. Verifica el ID para operaciones de actualización/eliminación.
        else if (this.cud != CUD.CREATE && this.en.getId() == 0) {
            JOptionPane.showMessageDialog(this,
                    "ID de paciente inválido para esta operación.",
                    "Error de Datos", JOptionPane.ERROR_MESSAGE);
            return res; // Retorna false.
        }

        // Si todas las validaciones anteriores pasan, los datos son válidos.
        res = true;

        // Actualiza los atributos del objeto Paciente 'en' con los valores ingresados:
        this.en.setNombreCompleto(txtNombreCompleto.getText().trim());
        this.en.setTelefono(txtTelefono.getText().trim());
        this.en.setFechaNacimiento(sqlDateFechaNacimiento);

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
                        // Caso de creación de un nuevo paciente.
                        // Llama al método 'create' de pacienteDAO para persistir el nuevo paciente.
                        Paciente pacienteCreado = pacienteDAO.create(this.en);
                        // Verifica si la creación fue exitosa comprobando si el nuevo paciente tiene un ID asignado.
                        if (pacienteCreado != null && pacienteCreado.getId() > 0) {
                            this.en.setId(pacienteCreado.getId()); // Asigna el ID generado al objeto 'en'
                            r = true; // Establece 'r' a true si la creación fue exitosa.
                        }
                        break;
                    case UPDATE:
                        // Caso de actualización de un paciente existente.
                        r = pacienteDAO.update(this.en); // 'r' será true si la actualización fue exitosa.
                        break;
                    case DELETE:
                        // Caso de eliminación de un paciente.
                        r = pacienteDAO.delete(this.en); // 'r' será true si la eliminación fue exitosa.
                        break;
                }

                // Si la operación de la base de datos fue exitosa.
                if (r) {
                    JOptionPane.showMessageDialog(this,
                            "Transacción realizada exitosamente",
                            "Información", JOptionPane.INFORMATION_MESSAGE);
                    this.dispose(); // Cierra la ventana actual.
                } else {
                    // Si la operación de la base de datos falló.
                    JOptionPane.showMessageDialog(this,
                            "No se logró realizar ninguna acción",
                            "ERROR", JOptionPane.ERROR_MESSAGE);
                    return; // Sale del método.
                }
            } else {
                // El mensaje de validación ya se mostró dentro de getValuesControls(),
                // pero si quieres uno general, aquí estaría.
                // JOptionPane.showMessageDialog(this,
                //         "Por favor, complete todos los campos obligatorios.",
                //         "Validación", JOptionPane.WARNING_MESSAGE);
                return; // Sale del método.
            }
        } catch (SQLException ex) {
            // Captura cualquier excepción SQL que ocurra.
            JOptionPane.showMessageDialog(this,
                    "Error de base de datos: " + ex.getMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Imprime la traza de la pila para depuración.
            return; // Sale del método.
        } catch (Exception ex) {
            // Captura cualquier otra excepción.
            JOptionPane.showMessageDialog(this,
                    "Ha ocurrido un error inesperado: " + ex.getMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(); // Imprime la traza de la pila para depuración.
            return; // Sale del método;
        }
    }

    // --- Métodos adicionales que podrías necesitar ---
    // Si tu MainForm necesita saber si el diálogo se cerró con éxito (OK/Guardar)
    private boolean result = false;

    public boolean getResult() {
        return result;
    }

    private void onOk() {
        if (getValuesControls()) {
            try {
                boolean operationSuccessful = false;
                switch (this.cud) {
                    case CREATE:
                        Paciente createdPaciente = pacienteDAO.create(this.en);
                        if (createdPaciente != null && createdPaciente.getId() > 0) {
                            this.en.setId(createdPaciente.getId());
                            operationSuccessful = true;
                        }
                        break;
                    case UPDATE:
                        operationSuccessful = pacienteDAO.update(this.en);
                        break;
                    case DELETE:
                        operationSuccessful = pacienteDAO.delete(this.en);
                        break;
                }

                if (operationSuccessful) {
                    JOptionPane.showMessageDialog(this,
                            "Transacción realizada exitosamente",
                            "Información", JOptionPane.INFORMATION_MESSAGE);
                    this.result = true; // Indica éxito
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "No se logró realizar ninguna acción",
                            "ERROR", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                        "Error de base de datos: " + ex.getMessage(),
                        "ERROR", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Ha ocurrido un error inesperado: " + ex.getMessage(),
                        "ERROR", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
}
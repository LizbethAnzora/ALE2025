package ale2025.presentacion;

import ale2025.dominio.Cita;        // Importa la clase Cita.
import ale2025.dominio.Medico;       // Importa la clase Medico para el JComboBox.
import ale2025.dominio.Paciente;     // Importa la clase Paciente para el JComboBox.
import ale2025.persistencia.CitaDAO;      // Importa la clase CitaDAO.
import ale2025.persistencia.MedicoDAO;    // Importa MedicoDAO para cargar médicos.
import ale2025.persistencia.PacienteDAO;  // Importa PacienteDAO para cargar pacientes.
import ale2025.utils.CUD;           // Importa el enum CUD.

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import javax.swing.*;               // Importa Swing para GUI.
import java.awt.*;                  // Necesario para BorderLayout.
import java.sql.SQLException;       // Importa SQLException para manejo de errores de base de datos.
import java.sql.Date;               // Para convertir LocalDate a java.sql.Date.
import java.time.LocalDate;         // Para manejar los objetos de fecha con LGoodDatePicker.
import java.util.ArrayList;         // Importa ArrayList para listas dinámicas.

public class CitaWriteForm extends JDialog {
    private JComboBox<Medico> cbMedicoId;
    private JTextField txtCostoConsulta; // JTextField para el costo (tipo double)
    private JButton btnOk;
    private JButton btnCancel;
    private JPanel mainPanel;
    private JComboBox<Paciente> cbPacienteId;
    private JPanel panelFechaCita; // Panel vacío para el DatePicker de fecha de cita

    private CitaDAO citaDAO;
    private MedicoDAO medicoDAO;
    private PacienteDAO pacienteDAO;
    private MainForm mainForm;
    private CUD cud;
    private Cita en; // 'en' se refiere a la entidad Cita

    private DatePicker datePickerFechaCita; // Instancia del DatePicker

    private boolean result = false;

    public CitaWriteForm(MainForm mainForm, CUD cud, Cita cita) {
        super(mainForm, "Gestión de Citas", true);
        this.cud = cud;
        this.en = cita;
        this.mainForm = mainForm;
        citaDAO = new CitaDAO();
        medicoDAO = new MedicoDAO(); // Inicializa MedicoDAO
        pacienteDAO = new PacienteDAO(); // Inicializa PacienteDAO

        setContentPane(mainPanel);
        setModal(true);

        setupDatePickers(); // Llama al método para configurar el DatePicker
        init();             // Llama al método 'init' para inicializar y configurar el formulario.
        pack();
        setLocationRelativeTo(mainForm);

        btnCancel.addActionListener(s -> this.dispose());
        btnOk.addActionListener(s -> ok());
    }

    private void setupDatePickers() {
        // Configuración para el DatePicker de Fecha de Cita
        datePickerFechaCita = new DatePicker();
        DatePickerSettings settings = new DatePickerSettings();
        settings.setFormatForDatesCommonEra("dd/MM/yyyy"); // Formato día/mes/año
        datePickerFechaCita.setSettings(settings);

        panelFechaCita.setLayout(new BorderLayout()); // Usa panelFechaCita aquí
        panelFechaCita.add(datePickerFechaCita, BorderLayout.CENTER);
    }

    private void init() {
        // Carga los pacientes y médicos en sus respectivos JComboBoxes
        loadPacientes();
        loadMedicos();

        switch (this.cud) {
            case CREATE:
                setTitle("Crear Cita");
                btnOk.setText("Guardar");
                break;
            case UPDATE:
                setTitle("Modificar Cita");
                btnOk.setText("Guardar");
                break;
            case DELETE:
                setTitle("Eliminar Cita");
                btnOk.setText("Eliminar");
                break;
        }

        setValuesControls(this.en);
    }

    // Método para cargar los pacientes en el JComboBox
    private void loadPacientes() {
        try {
            ArrayList<Paciente> pacientes = pacienteDAO.search(""); // Busca todos los pacientes
            cbPacienteId.removeAllItems(); // Limpia elementos existentes

            // Agrega un elemento por defecto si no es una actualización o eliminación
            if (this.cud == CUD.CREATE || (this.cud == CUD.UPDATE && en.getPacienteId() == 0)) {
                cbPacienteId.addItem(null); // Permite una selección "vacía" o "no seleccionada"
            }

            for (Paciente pac : pacientes) {
                cbPacienteId.addItem(pac); // Agrega el objeto Paciente completo al JComboBox
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar pacientes: " + ex.getMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // Método para cargar los médicos en el JComboBox
    private void loadMedicos() {
        try {
            ArrayList<Medico> medicos = medicoDAO.search(""); // Busca todos los médicos
            cbMedicoId.removeAllItems(); // Limpia elementos existentes

            // Agrega un elemento por defecto si no es una actualización o eliminación
            if (this.cud == CUD.CREATE || (this.cud == CUD.UPDATE && en.getMedicoId() == 0)) {
                cbMedicoId.addItem(null); // Permite una selección "vacía" o "no seleccionada"
            }

            for (Medico med : medicos) {
                cbMedicoId.addItem(med); // Agrega el objeto Medico completo al JComboBox
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar médicos: " + ex.getMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void setValuesControls(Cita cita) {
        // Selecciona el paciente correcto en el JComboBox
        if (cita.getPacienteId() != 0) {
            for (int i = 0; i < cbPacienteId.getItemCount(); i++) {
                Paciente pac = cbPacienteId.getItemAt(i);
                if (pac != null && pac.getId() == cita.getPacienteId()) {
                    cbPacienteId.setSelectedItem(pac);
                    break;
                }
            }
        } else {
            cbPacienteId.setSelectedItem(null);
        }

        // Selecciona el médico correcto en el JComboBox
        if (cita.getMedicoId() != 0) {
            for (int i = 0; i < cbMedicoId.getItemCount(); i++) {
                Medico med = cbMedicoId.getItemAt(i);
                if (med != null && med.getId() == cita.getMedicoId()) {
                    cbMedicoId.setSelectedItem(med);
                    break;
                }
            }
        } else {
            cbMedicoId.setSelectedItem(null);
        }

        // Establecer la fecha en el DatePicker
        if (cita.getFechaCita() != null) {
            datePickerFechaCita.setDate(cita.getFechaCita().toLocalDate()); // Convierte java.sql.Date a LocalDate
        } else {
            // Opcional: Para CREAR, establecer la fecha actual como valor por defecto
            if (this.cud == CUD.CREATE) {
                datePickerFechaCita.setDate(LocalDate.now());
            } else {
                datePickerFechaCita.clear(); // O limpiar si no hay fecha
            }
        }

        txtCostoConsulta.setText(String.valueOf(cita.getCostoConsulta())); // Convierte double a String

        // Si la operación es DELETE, deshabilitar todos los campos
        if (this.cud == CUD.DELETE) {
            cbPacienteId.setEnabled(false);
            cbMedicoId.setEnabled(false);
            datePickerFechaCita.setEnabled(false);
            txtCostoConsulta.setEditable(false);
            btnOk.setText("Eliminar");
            setTitle("Eliminar Cita");
        }
    }

    private boolean getValuesControls() {
        // 1. Validación de Paciente
        Paciente selectedPaciente = (Paciente) cbPacienteId.getSelectedItem();
        if (selectedPaciente == null) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar un 'Paciente'.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 2. Validación de Médico
        Medico selectedMedico = (Medico) cbMedicoId.getSelectedItem();
        if (selectedMedico == null) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar un 'Médico'.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 3. Validación y obtención de Fecha de Cita
        LocalDate fechaCitaLocal = datePickerFechaCita.getDate();
        if (fechaCitaLocal == null) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar una 'Fecha de Cita'.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        Date fechaCitaSql = Date.valueOf(fechaCitaLocal); // Convierte LocalDate a java.sql.Date

        // 4. Validación y conversión de Costo de Consulta (double)
        String costoText = txtCostoConsulta.getText().trim();
        if (costoText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El campo 'Costo de Consulta' es obligatorio.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        double costo;
        try {
            costo = Double.parseDouble(costoText);
            if (costo < 0) { // Opcional: Validar que el costo no sea negativo
                JOptionPane.showMessageDialog(this,
                        "El 'Costo de Consulta' no puede ser un valor negativo.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "El 'Costo de Consulta' debe ser un valor numérico válido (ej. 100.00).",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Si todas las validaciones pasan, actualiza el objeto Cita.
        this.en.setPacienteId(selectedPaciente.getId());
        this.en.setMedicoId(selectedMedico.getId());
        this.en.setFechaCita(fechaCitaSql);
        this.en.setCostoConsulta(costo);

        return true;
    }

    private void ok() {
        try {
            boolean res = getValuesControls();

            if (res) {
                boolean r = false;

                switch (this.cud) {
                    case CREATE:
                        Cita citaCreada = citaDAO.create(this.en);
                        if (citaCreada != null && citaCreada.getId() > 0) {
                            this.en.setId(citaCreada.getId());
                            r = true;
                        }
                        break;
                    case UPDATE:
                        r = citaDAO.update(this.en);
                        break;
                    case DELETE:
                        r = citaDAO.delete(this.en);
                        break;
                }

                if (r) {
                    JOptionPane.showMessageDialog(this,
                            "Transacción realizada exitosamente",
                            "Información", JOptionPane.INFORMATION_MESSAGE);
                    this.result = true;
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "No se logró realizar ninguna acción",
                            "ERROR", JOptionPane.ERROR_MESSAGE);
                }
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

    public boolean getResult() {
        return result;
    }
}
package ale2025.presentacion;

import ale2025.dominio.Horario;
import ale2025.dominio.Medico;
import ale2025.persistencia.HorarioDAO;
import ale2025.persistencia.MedicoDAO;
import ale2025.utils.CUD;

import com.github.lgooddatepicker.components.TimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
// import java.time.format.DateTimeFormatter; // Esta importación ya no es necesaria si no se usa DateTimeFormatter directamente aquí
import java.util.ArrayList;

public class HorarioWriteForm extends JDialog {
    private JComboBox<Medico> cbMedicoId;
    private JButton btnOk;
    private JButton btnCancel;
    private JPanel mainPanel;
    private JTextField txtDiaSemana;
    private JPanel panelHoraInicio;
    private JPanel panelHoraFin;

    private HorarioDAO horarioDAO;
    private MedicoDAO medicoDAO;
    private MainForm mainForm;
    private CUD cud;
    private Horario en;

    private TimePicker timePickerHoraInicio;
    private TimePicker timePickerHoraFin;

    private boolean result = false;

    public HorarioWriteForm(MainForm mainForm, CUD cud, Horario horario) {
        super(mainForm, "Gestión de Horarios", true);
        this.cud = cud;
        this.en = horario;
        this.mainForm = mainForm;
        horarioDAO = new HorarioDAO();
        medicoDAO = new MedicoDAO();

        setContentPane(mainPanel);
        setModal(true);

        setupTimePickers(); // Llama al método para configurar los TimePickers
        init();
        pack();
        setLocationRelativeTo(mainForm);

        btnCancel.addActionListener(s -> this.dispose());
        btnOk.addActionListener(s -> ok());
    }

    private void setupTimePickers() {
        // Configuración para el TimePicker de Hora de Inicio
        timePickerHoraInicio = new TimePicker();
        // TimePickerSettings settingsInicio = new TimePickerSettings(); // Ya no es necesario si no se configuran.
        // Si tu versión de LGoodDatePicker no tiene setTimeFormatForDisplayAndEdit,
        // simplemente omite la configuración del formato aquí y usa el predeterminado.
        // settingsInicio.setTimeFormatForDisplayAndEdit(DateTimeFormatter.ofPattern("HH:mm"));
        // timePickerHoraInicio.setSettings(settingsInicio); // Solo si settingsInicio se usa para algo más.

        panelHoraInicio.setLayout(new BorderLayout());
        panelHoraInicio.add(timePickerHoraInicio, BorderLayout.CENTER);

        // Configuración para el TimePicker de Hora de Fin
        timePickerHoraFin = new TimePicker();
        // TimePickerSettings settingsFin = new TimePickerSettings(); // Ya no es necesario si no se configuran.
        // settingsFin.setTimeFormatForDisplayAndEdit(DateTimeFormatter.ofPattern("HH:mm"));
        // timePickerHoraFin.setSettings(settingsFin); // Solo si settingsFin se usa para algo más.

        panelHoraFin.setLayout(new BorderLayout());
        panelHoraFin.add(timePickerHoraFin, BorderLayout.CENTER);
    }

    private void init() {
        loadMedicos();

        switch (this.cud) {
            case CREATE:
                setTitle("Crear Horario");
                btnOk.setText("Guardar");
                break;
            case UPDATE:
                setTitle("Modificar Horario");
                btnOk.setText("Guardar");
                break;
            case DELETE:
                setTitle("Eliminar Horario");
                btnOk.setText("Eliminar");
                break;
        }

        setValuesControls(this.en);
    }

    private void loadMedicos() {
        try {
            ArrayList<Medico> medicos = medicoDAO.search("");
            cbMedicoId.removeAllItems();

            if (this.cud == CUD.CREATE || (this.cud == CUD.UPDATE && en.getMedicoId() == 0)) {
                cbMedicoId.addItem(null);
            }

            for (Medico med : medicos) {
                cbMedicoId.addItem(med);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar médicos: " + ex.getMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void setValuesControls(Horario horario) {
        txtDiaSemana.setText(horario.getDiaSemana());

        if (horario.getMedicoId() != 0) {
            for (int i = 0; i < cbMedicoId.getItemCount(); i++) {
                Medico med = cbMedicoId.getItemAt(i);
                if (med != null && med.getId() == horario.getMedicoId()) {
                    cbMedicoId.setSelectedItem(med);
                    break;
                }
            }
        } else {
            cbMedicoId.setSelectedItem(null);
        }

        if (horario.getHoraInicio() != null) {
            timePickerHoraInicio.setTime(horario.getHoraInicio().toLocalTime());
        } else {
            timePickerHoraInicio.setTime(null);
        }

        if (horario.getHoraFin() != null) {
            timePickerHoraFin.setTime(horario.getHoraFin().toLocalTime());
        } else {
            timePickerHoraFin.setTime(null);
        }

        if (this.cud == CUD.DELETE) {
            txtDiaSemana.setEditable(false);
            cbMedicoId.setEnabled(false);
            timePickerHoraInicio.setEnabled(false);
            timePickerHoraFin.setEnabled(false);
            btnOk.setText("Eliminar");
            setTitle("Eliminar Horario");
        }
    }

    private boolean getValuesControls() {
        if (txtDiaSemana.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El campo 'Día de la Semana' es obligatorio.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        Medico selectedMedico = (Medico) cbMedicoId.getSelectedItem();
        if (selectedMedico == null) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar un 'Médico'.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        LocalTime horaInicioLocal = timePickerHoraInicio.getTime();
        if (horaInicioLocal == null) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar una 'Hora de Inicio'.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        Time horaInicioSql = Time.valueOf(horaInicioLocal);

        LocalTime horaFinLocal = timePickerHoraFin.getTime();
        if (horaFinLocal == null) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar una 'Hora de Fin'.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        Time horaFinSql = Time.valueOf(horaFinLocal);

        if (horaFinLocal.isBefore(horaInicioLocal)) {
            JOptionPane.showMessageDialog(this,
                    "La 'Hora de Fin' no puede ser anterior a la 'Hora de Inicio'.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        this.en.setDiaSemana(txtDiaSemana.getText().trim());
        this.en.setMedicoId(selectedMedico.getId());
        this.en.setHoraInicio(horaInicioSql);
        this.en.setHoraFin(horaFinSql);

        return true;
    }

    private void ok() {
        try {
            boolean res = getValuesControls();

            if (res) {
                boolean r = false;

                switch (this.cud) {
                    case CREATE:
                        Horario horarioCreado = horarioDAO.create(this.en);
                        if (horarioCreado != null && horarioCreado.getId() > 0) {
                            this.en.setId(horarioCreado.getId());
                            r = true;
                        }
                        break;
                    case UPDATE:
                        r = horarioDAO.update(this.en);
                        break;
                    case DELETE:
                        r = horarioDAO.delete(this.en);
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
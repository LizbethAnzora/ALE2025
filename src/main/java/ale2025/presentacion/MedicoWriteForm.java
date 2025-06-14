package ale2025.presentacion;

import ale2025.dominio.Medico;       // Importa la clase Medico.
import ale2025.dominio.Especialidad;  // Importa la clase Especialidad para el JComboBox.
import ale2025.persistencia.MedicoDAO;     // Importa la clase MedicoDAO.
import ale2025.persistencia.EspecialidadDAO; // Importa la clase EspecialidadDAO para cargar el JComboBox.
import ale2025.utils.CUD;           // Importa el enum CUD.

import javax.swing.*;               // Importa Swing para GUI.
import java.sql.SQLException;       // Importa SQLException para manejo de errores de base de datos.
import java.util.ArrayList;         // Importa ArrayList para listas dinámicas.

public class MedicoWriteForm extends JDialog {
    private JPanel mainPanel;
    private JTextField txtNombreCompleto;
    private JComboBox<Especialidad> cbEspecialidadId; // JComboBox para seleccionar la especialidad
    private JTextField txtSueldo; // JTextField para el sueldo (tipo double)
    private JButton btnOk;
    private JButton btnCancel;

    private MedicoDAO medicoDAO;
    private EspecialidadDAO especialidadDAO; // Necesario para cargar las especialidades en el JComboBox
    private MainForm mainForm;
    private CUD cud;
    private Medico en; // 'en' se refiere a la entidad Medico

    private boolean result = false;

    public MedicoWriteForm(MainForm mainForm, CUD cud, Medico medico) {
        super(mainForm, "Gestión de Médicos", true);
        this.cud = cud;
        this.en = medico;
        this.mainForm = mainForm;
        medicoDAO = new MedicoDAO();
        especialidadDAO = new EspecialidadDAO(); // Inicializa EspecialidadDAO

        setContentPane(mainPanel);
        setModal(true);

        init(); // Llama al método 'init' para inicializar y configurar el formulario.
        pack();
        setLocationRelativeTo(mainForm);

        btnCancel.addActionListener(s -> this.dispose());
        btnOk.addActionListener(s -> ok());
    }

    private void init() {
        // Carga las especialidades en el JComboBox antes de configurar los valores de los controles
        loadEspecialidades();

        switch (this.cud) {
            case CREATE:
                setTitle("Crear Médico");
                btnOk.setText("Guardar");
                break;
            case UPDATE:
                setTitle("Modificar Médico");
                btnOk.setText("Guardar");
                break;
            case DELETE:
                setTitle("Eliminar Médico");
                btnOk.setText("Eliminar");
                break;
        }

        setValuesControls(this.en);
    }

    // Método para cargar las especialidades en el JComboBox
    private void loadEspecialidades() {
        try {
            ArrayList<Especialidad> especialidades = especialidadDAO.search(""); // Busca todas las especialidades
            cbEspecialidadId.removeAllItems(); // Limpia elementos existentes

            // Agrega un elemento por defecto si no es una actualización o eliminación
            if (this.cud == CUD.CREATE || (this.cud == CUD.UPDATE && en.getEspecialidadId() == 0)) {
                cbEspecialidadId.addItem(null); // Permite una selección "vacía" o "no seleccionada"
            }


            for (Especialidad esp : especialidades) {
                cbEspecialidadId.addItem(esp); // Agrega el objeto Especialidad completo al JComboBox
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al cargar especialidades: " + ex.getMessage(),
                    "ERROR", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void setValuesControls(Medico medico) {
        txtNombreCompleto.setText(medico.getNombreCompleto());
        txtSueldo.setText(String.valueOf(medico.getSueldo())); // Convierte double a String

        // Selecciona la especialidad correcta en el JComboBox si se está actualizando o eliminando
        if (medico.getEspecialidadId() != 0) {
            for (int i = 0; i < cbEspecialidadId.getItemCount(); i++) {
                Especialidad esp = cbEspecialidadId.getItemAt(i);
                if (esp != null && esp.getId() == medico.getEspecialidadId()) {
                    cbEspecialidadId.setSelectedItem(esp);
                    break;
                }
            }
        } else {
            cbEspecialidadId.setSelectedItem(null); // Asegura que no haya nada seleccionado si el ID es 0
        }


        if (this.cud == CUD.DELETE) {
            txtNombreCompleto.setEditable(false);
            txtSueldo.setEditable(false);
            cbEspecialidadId.setEnabled(false); // Deshabilita el JComboBox
            btnOk.setText("Eliminar"); // Ajustar el texto del botón si no se hizo en init()
            setTitle("Eliminar Médico"); // Ajustar el título si no se hizo en init()
        }
    }

    private boolean getValuesControls() {
        // 1. Validación de Nombre Completo
        if (txtNombreCompleto.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El campo 'Nombre Completo' es obligatorio.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 2. Validación de Especialidad
        Especialidad selectedEspecialidad = (Especialidad) cbEspecialidadId.getSelectedItem();
        if (selectedEspecialidad == null) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar una 'Especialidad'.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // 3. Validación y conversión de Sueldo (double)
        String sueldoText = txtSueldo.getText().trim();
        if (sueldoText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El campo 'Sueldo' es obligatorio.",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        double sueldo;
        try {
            sueldo = Double.parseDouble(sueldoText);
            if (sueldo < 0) { // Opcional: Validar que el sueldo no sea negativo
                JOptionPane.showMessageDialog(this,
                        "El 'Sueldo' no puede ser un valor negativo.",
                        "Validación", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "El 'Sueldo' debe ser un valor numérico válido (ej. 1500.50).",
                    "Validación", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        // Si todas las validaciones pasan, actualiza el objeto Medico.
        this.en.setNombreCompleto(txtNombreCompleto.getText().trim());
        this.en.setEspecialidadId(selectedEspecialidad.getId()); // Obtiene el ID de la especialidad seleccionada
        this.en.setSueldo(sueldo);

        return true;
    }

    private void ok() {
        try {
            boolean res = getValuesControls();

            if (res) {
                boolean r = false;

                switch (this.cud) {
                    case CREATE:
                        Medico medicoCreado = medicoDAO.create(this.en);
                        if (medicoCreado != null && medicoCreado.getId() > 0) {
                            this.en.setId(medicoCreado.getId());
                            r = true;
                        }
                        break;
                    case UPDATE:
                        r = medicoDAO.update(this.en);
                        break;
                    case DELETE:
                        r = medicoDAO.delete(this.en);
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
package ale2025.presentacion;

import javax.swing.*;
import ale2025.dominio.User;
import java.awt.*;
import java.net.URL;

public class MainForm extends JFrame {
    private User userAutenticate;
    private JPanel mainPanel;   // <-- Solo la declaración de la variable
    private JLabel imageLabel;  // <-- Solo la declaración de la variable

    public User getUserAutenticate() {
        return userAutenticate;
    }

    public void setUserAutenticate(User userAutenticate) {
        this.userAutenticate = userAutenticate;
    }

    public MainForm(){
        // Configuración básica del JFrame
        setContentPane(mainPanel); // ¡IMPORTANTE! Establece el panel raíz del formulario generado por el UI Designer.
        // Sin esta línea, tu JFrame estaría vacío al inicio.
        setTitle("Clinica Salud Total"); // Establece el título de la ventana principal (JFrame).
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Configura la operación por defecto al cerrar la ventana para que la aplicación se termine.
        setLocationRelativeTo(null); // Centra la ventana principal en la pantalla.

        // Establece el tamaño preferido del panel principal para que el formulario tenga un tamaño aceptable
        // antes de maximizarlo. Esto ayuda a la disposición inicial de los componentes.
        mainPanel.setPreferredSize(new Dimension(900, 650)); // Un tamaño medio de ejemplo (Ancho, Alto)
        pack(); // Ajusta el tamaño de la ventana a sus contenidos preferidos.
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Inicializa la ventana principal en estado maximizado, ocupando toda la pantalla.

        createMenu(); // Llama al método 'createMenu()' para crear y agregar la barra de menú a la ventana principal.

        // --- Carga de la imagen en el JLabel ---
        loadImage();
    }

    /**
     * Carga la imagen 'clinicaimagen2.png' en el imageLabel.
     * La imagen debe estar en el classpath, por ejemplo, en src/main/resources/images/.
     */
    private void loadImage() {
        // La ruta de la imagen debe ser relativa al classpath.
        // Si tu imagen está en src/main/resources/images/clinicaimagen2.png,
        // la ruta en getResource() será /images/clinicaimagen2.png
        URL imageUrl = getClass().getResource("images/clinicaimagen2.png");

        if (imageUrl != null) {
            ImageIcon icon = new ImageIcon(imageUrl);

            // Opcional: Escalar la imagen si es demasiado grande para el JLabel.
            // Es preferible que la imagen ya tenga un tamaño adecuado para el diseño.
            // int desiredWidth = imageLabel.getWidth() > 0 ? imageLabel.getWidth() : 800; // Ancho deseado, o un valor por defecto
            // int desiredHeight = imageLabel.getHeight() > 0 ? imageLabel.getHeight() : 500; // Alto deseado, o un valor por defecto
            // Image img = icon.getImage().getScaledInstance(desiredWidth, desiredHeight, Image.SCALE_SMOOTH);
            // imageLabel.setIcon(new ImageIcon(img));

            imageLabel.setIcon(icon); // Asigna el icono al JLabel
            imageLabel.setText(""); // Asegúrate de que el JLabel no tenga texto que pueda ocultar la imagen
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER); // Centra la imagen horizontalmente en el JLabel
            imageLabel.setVerticalAlignment(SwingConstants.CENTER);   // Centra la imagen verticalmente en el JLabel
        }
    }

    private void createMenu() {
        // Barra de menú
        JMenuBar menuBar = new JMenuBar(); // Crea una nueva barra de menú.
        setJMenuBar(menuBar); // Establece la barra de menú creada como la barra de menú de este JFrame (MainForm).

        JMenu menuPerfil = new JMenu("Opciones de perfil"); // Crea un nuevo menú llamado "Perfil".
        menuBar.add(menuPerfil); // Agrega el menú "Perfil" a la barra de menú.

        JMenuItem itemChangePassword = new JMenuItem("Cambiar contraseña"); // Crea un nuevo elemento de menú llamado "Cambiar contraseña".
        menuPerfil.add(itemChangePassword); // Agrega el elemento "Cambiar contraseña" al menú "Perfil".
        itemChangePassword.addActionListener(e -> { // Agrega un ActionListener al elemento "Cambiar contraseña".
            ChangePasswordForm changePassword = new ChangePasswordForm(this); // Cuando se hace clic, crea una nueva instancia de ChangePasswordForm, pasándole la instancia actual de MainForm como padre.
            changePassword.setVisible(true); // Hace visible la ventana de cambio de contraseña.

        });


        JMenuItem itemChangeUser = new JMenuItem("Cambiar de usuario"); // Crea un nuevo elemento de menú llamado "Cambiar de usuario".
        menuPerfil.add(itemChangeUser); // Agrega el elemento "Cambiar de usuario" al menú "Perfil".
        itemChangeUser.addActionListener(e -> { // Agrega un ActionListener al elemento "Cambiar de usuario".
            LoginForm loginForm = new LoginForm(this); // Cuando se hace clic, crea una nueva instancia de LoginForm (ventana de inicio de sesión), pasándole la instancia actual de MainForm como padre.
            loginForm.setVisible(true); // Hace visible la ventana de inicio de sesión.
        });


        JMenuItem itemSalir = new JMenuItem("Salir"); // Crea un nuevo elemento de menú llamado "Salir".
        menuPerfil.add(itemSalir); // Agrega el elemento "Salir" al menú "Perfil".
        itemSalir.addActionListener(e -> System.exit(0)); // Agrega un ActionListener al elemento "Salir". Cuando se hace clic, termina la ejecución de la aplicación (cierra la JVM).


        // Menú "Matenimiento"
        JMenu menuMantenimiento = new JMenu("Menu de la clinica"); // Crea un nuevo menú llamado "Mantenimientos".
        menuBar.add(menuMantenimiento); // Agrega el menú "Mantenimientos" a la barra de menú.

        JMenuItem itemUsers = new JMenuItem("Usuarios"); // Crea un nuevo elemento de menú llamado "Usuarios".
        menuMantenimiento.add(itemUsers); // Agrega el elemento "Usuarios" al menú "Mantenimientos".
        itemUsers.addActionListener(e -> { // Agrega un ActionListener al elemento "Usuarios".
            UserReadingForm userReadingForm=new UserReadingForm(this); // Cuando se hace clic, crea una nueva instancia de UserReadingForm (formulario para leer/listar usuarios), pasándole la instancia actual de MainForm como padre.
            userReadingForm.setVisible(true); // Hace visible el formulario de lectura de usuarios.

        });


        JMenuItem itemPacientes = new JMenuItem("Pacientes"); // Crea un nuevo elemento de menú llamado "Pacientes".
        menuMantenimiento.add(itemPacientes); // Asume que 'menuMantenimiento' es el JMenu donde quieres agregarlo.
        itemPacientes.addActionListener(e -> {
            PacienteReadingForm pacienteReadingForm = new PacienteReadingForm(this);
            pacienteReadingForm.setVisible(true);
        });


        JMenuItem itemEspecialidades = new JMenuItem("Especialidades"); // Crea un nuevo elemento de menú.
        menuMantenimiento.add(itemEspecialidades); // Agrega al menú "Mantenimientos".
        itemEspecialidades.addActionListener(e -> {
            EspecialidadReadingForm especialidadReadingForm = new EspecialidadReadingForm(this);// Hace visible el formulario de lectura de especialidades.
            especialidadReadingForm.setVisible(true);
        });


        JMenuItem itemMedicos = new JMenuItem("Médicos"); // Crea un nuevo elemento de menú.
        menuMantenimiento.add(itemMedicos); // Agrega al menú "Mantenimientos" (ajusta si tu menú tiene otro nombre).
        itemMedicos.addActionListener(e -> {
            MedicoReadingForm medicoReadingForm = new MedicoReadingForm(this);
            medicoReadingForm.setVisible(true);
        });



        JMenuItem itemHorarios = new JMenuItem("Horarios"); // Crea un nuevo elemento de menú.
        menuMantenimiento.add(itemHorarios); // Agrega al menú "Mantenimientos" (ajusta si tu menú tiene otro nombre).
        itemHorarios.addActionListener(e -> {
            HorarioReadingForm horarioReadingForm = new HorarioReadingForm(this);
            horarioReadingForm.setVisible(true);
        });

    }
}

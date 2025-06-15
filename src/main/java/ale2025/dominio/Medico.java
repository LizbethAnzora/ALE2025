package ale2025.dominio;

// No se requieren importaciones especiales para los tipos de datos simples (int, String, double)
public class Medico {
    private int id;
    private String nombreCompleto;
    private int especialidadId; // Clave foránea que referencia a Especialidades
    private double sueldo;

    public Medico() {
    }

    public Medico(int id, String nombreCompleto, int especialidadId, double sueldo) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.especialidadId = especialidadId;
        this.sueldo = sueldo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public int getEspecialidadId() {
        return especialidadId;
    }

    public void setEspecialidadId(int especialidadId) {
        this.especialidadId = especialidadId;
    }

    public double getSueldo() {
        return sueldo;
    }

    public void setSueldo(double sueldo) {
        this.sueldo = sueldo;
    }

    @Override
    public String toString() {
        return nombreCompleto; // Retorna solo el nombre completo del médico para mostrar en la UI.
    }
}
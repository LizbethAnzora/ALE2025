package ale2025.dominio;

import java.sql.Date; // Necesario para el tipo DATE de SQL

public class Cita {
    private int id;
    private int pacienteId;
    private int medicoId;
    private Date fechaCita;
    private double costoConsulta;

    public Cita() {
    }

    public Cita(int id, int pacienteId, int medicoId, Date fechaCita, double costoConsulta) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.medicoId = medicoId;
        this.fechaCita = fechaCita;
        this.costoConsulta = costoConsulta;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(int pacienteId) {
        this.pacienteId = pacienteId;
    }

    public int getMedicoId() {
        return medicoId;
    }

    public void setMedicoId(int medicoId) {
        this.medicoId = medicoId;
    }

    public Date getFechaCita() {
        return fechaCita;
    }

    public void setFechaCita(Date fechaCita) {
        this.fechaCita = fechaCita;
    }

    public double getCostoConsulta() {
        return costoConsulta;
    }

    public void setCostoConsulta(double costoConsulta) {
        this.costoConsulta = costoConsulta;
    }
}
package ale2025.dominio;

public class Usuario {
    private int id;
    private String nombre;
    private String passwordHash;
    private String correoElectronico ;
    private byte estado;

    public Usuario() {
    }

    public Usuario(int id, String nombre, String passwordHash, String correoElectronico, byte estado) {
        this.id = id;
        this.nombre = nombre;
        this.passwordHash = passwordHash;
        this.correoElectronico = correoElectronico;
        estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getCorreoElectronico() {
        return correoElectronico;
    }

    public void setCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public byte getEstado() {
        return estado;
    }

    public void setEstado(byte estado) {
        estado = estado;
    }

    public String getStrEstado(){
        String str="";
        switch (estado){
            case 1:
                str = "ACTIVO";
                break;
            case 2:
                str = "INACTIVO";
                break;
            default:
                str = "";
        }
        return str;
    }
}


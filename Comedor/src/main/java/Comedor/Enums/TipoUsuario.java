package comedor.enums;
 
public enum TipoUsuario {
    ESTUDIANTE("Estudiante"),
    PROFESOR("Profesor"),
    PERSONAL_ADMINISTRATIVO("Personal Administrativo");
 
    private final String descripcion;
 
    TipoUsuario(String descripcion) {
        this.descripcion = descripcion;
    }
 
    public String getDescripcion() {
        return descripcion;
    }
 
    @Override
    public String toString() {
        return descripcion;
    }
}
 
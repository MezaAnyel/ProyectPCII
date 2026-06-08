package comedor.enums;
 
public enum EstadoTicket {
    ACTIVO("Activo"),
    CANJEADO("Canjeado"),
    CANCELADO("Cancelado"),
    VENCIDO("Vencido");
 
    private final String descripcion;
 
    EstadoTicket(String descripcion) {
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
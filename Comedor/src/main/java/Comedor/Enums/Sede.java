package comedor.enums;
 
public enum Sede {
    SEDE_CENTRAL("Sede Central"),
    SEDE_NORTE("Sede Norte"),
    SEDE_SUR("Sede Sur");
 
    private final String nombre;
 
    Sede(String nombre) {
        this.nombre = nombre;
    }
 
    public String getNombre() {
        return nombre;
    }
 
    @Override
    public String toString() {
        return nombre;
    }
}
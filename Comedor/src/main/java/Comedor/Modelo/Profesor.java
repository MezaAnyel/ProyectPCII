package comedor.modelo;

import comedor.enums.Sede;
import comedor.enums.TipoUsuario;

public class Profesor extends Usuario {
    public Profesor(String codigo, String nombreCompleto, String email,
                    String facultad, Sede sede) {
        super(codigo, nombreCompleto, email, facultad, TipoUsuario.PROFESOR, sede);
    }
}
package comedor.modelo;

import comedor.enums.Sede;
import comedor.enums.TipoUsuario;

public class Estudiante extends Usuario {
    public Estudiante(String codigo, String nombreCompleto, String email,
                      String facultad, Sede sede) {
        super(codigo, nombreCompleto, email, facultad, TipoUsuario.ESTUDIANTE, sede);
    }
}
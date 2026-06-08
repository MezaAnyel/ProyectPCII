package comedor.modelo;

import comedor.enums.Sede;
import comedor.enums.TipoUsuario;

public class PersonalAdministrativo extends Usuario {
    public PersonalAdministrativo(String codigo, String nombreCompleto, String email,
                                   String facultad, Sede sede) {
        super(codigo, nombreCompleto, email, facultad, TipoUsuario.PERSONAL_ADMINISTRATIVO, sede);
    }
}
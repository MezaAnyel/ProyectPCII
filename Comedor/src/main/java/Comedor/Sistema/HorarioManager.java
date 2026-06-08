package comedor.sistema;

import comedor.enums.TipoComida;
import comedor.enums.TipoUsuario;
import comedor.modelo.RangoHorario;
import java.time.LocalTime;

/**
 * Centraliza todos los horarios del comedor.
 *
 * Ventanas de ENTREGA de ticket (por tipo de usuario):
 *   DESAYUNO : Estudiante 07:00-07:20 | Profesor 07:20-07:40 | Personal 07:40-08:00
 *   ALMUERZO : Estudiante 11:00-11:20 | Profesor 11:20-11:40 | Personal 11:40-12:00
 *   CENA     : Estudiante 18:00-18:20 | Profesor 18:20-18:40 | Personal 18:40-19:00
 *
 * Ventanas de COMIDA (iguales para todos):
 *   DESAYUNO : 08:00-09:00
 *   ALMUERZO : 12:00-13:00
 *   CENA     : 19:00-20:00
 */
public class HorarioManager {

    private HorarioManager() {}

    public static RangoHorario getVentanaEntrega(TipoComida comida, TipoUsuario tipo) {
        return switch (comida) {
            case DESAYUNO -> switch (tipo) {
                case ESTUDIANTE              -> new RangoHorario(LocalTime.of(7, 0),  LocalTime.of(7, 20));
                case PROFESOR                -> new RangoHorario(LocalTime.of(7, 20), LocalTime.of(7, 40));
                case PERSONAL_ADMINISTRATIVO -> new RangoHorario(LocalTime.of(7, 40), LocalTime.of(8, 0));
            };
            case ALMUERZO -> switch (tipo) {
                case ESTUDIANTE              -> new RangoHorario(LocalTime.of(11, 0),  LocalTime.of(11, 20));
                case PROFESOR                -> new RangoHorario(LocalTime.of(11, 20), LocalTime.of(11, 40));
                case PERSONAL_ADMINISTRATIVO -> new RangoHorario(LocalTime.of(11, 40), LocalTime.of(12, 0));
            };
            case CENA -> switch (tipo) {
                case ESTUDIANTE              -> new RangoHorario(LocalTime.of(18, 0),  LocalTime.of(18, 20));
                case PROFESOR                -> new RangoHorario(LocalTime.of(18, 20), LocalTime.of(18, 40));
                case PERSONAL_ADMINISTRATIVO -> new RangoHorario(LocalTime.of(18, 40), LocalTime.of(19, 0));
            };
        };
    }

    public static RangoHorario getVentanaComida(TipoComida comida) {
        return switch (comida) {
            case DESAYUNO -> new RangoHorario(LocalTime.of(8, 0),  LocalTime.of(9, 0));
            case ALMUERZO -> new RangoHorario(LocalTime.of(12, 0), LocalTime.of(13, 0));
            case CENA     -> new RangoHorario(LocalTime.of(19, 0), LocalTime.of(20, 0));
        };
    }

    public static boolean estaEnVentanaEntrega(TipoComida comida, TipoUsuario tipo) {
        return getVentanaEntrega(comida, tipo).contiene(java.time.LocalTime.now());
    }

    public static String mostrarHorarios() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== HORARIOS DEL COMEDOR ===\n\n");
        for (TipoComida comida : TipoComida.values()) {
            sb.append("[ ").append(comida.getDescripcion().toUpperCase()).append(" ]\n");
            for (TipoUsuario tipo : TipoUsuario.values()) {
                RangoHorario entrega = getVentanaEntrega(comida, tipo);
                sb.append(String.format("  %-28s → Entrega ticket: %s\n", tipo.getDescripcion(), entrega));
            }
            sb.append(String.format("  %-28s → Hora de comer  : %s\n\n",
                    "Todos", getVentanaComida(comida)));
        }
        return sb.toString();
    }
}
package comedor.modelo;

import comedor.enums.EstadoTicket;
import comedor.enums.Sede;
import comedor.enums.TipoComida;
import comedor.enums.TipoUsuario;
import comedor.sistema.HorarioManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Ticket {
    private static int contadorGlobal = 1;

    private final int numeroTicket;
    private final String codigoTicket;
    private final LocalDateTime horarioEmision;
    private final LocalDate fechaTicket;
    private final TipoComida tipoComida;
    private final RangoHorario horarioEntregaTicket;
    private final RangoHorario horarioComida;
    private EstadoTicket estado;
    private final double precio;
    private final Usuario usuario;
    private final Sede sede;
    private LocalDateTime horarioCanje;

    public Ticket(TipoComida tipoComida, Usuario usuario, Sede sede) {
        this.numeroTicket   = contadorGlobal++;
        this.codigoTicket   = generarCodigo(tipoComida, usuario.getTipoUsuario());
        this.horarioEmision = LocalDateTime.now();
        this.fechaTicket    = LocalDate.now();
        this.tipoComida     = tipoComida;
        this.usuario        = usuario;
        this.sede           = sede;
        this.estado         = EstadoTicket.ACTIVO;
        this.precio         = calcularPrecio(tipoComida, usuario.getTipoUsuario());
        this.horarioEntregaTicket = HorarioManager.getVentanaEntrega(tipoComida, usuario.getTipoUsuario());
        this.horarioComida        = HorarioManager.getVentanaComida(tipoComida);
    }

    // Constructor para carga desde Excel (restaurar estado)
    public Ticket(int numero, String codigo, LocalDateTime emision, LocalDate fecha,
                  TipoComida tipoComida, RangoHorario entrega, RangoHorario comida,
                  EstadoTicket estado, double precio, Usuario usuario, Sede sede,
                  LocalDateTime horarioCanje) {
        this.numeroTicket         = numero;
        this.codigoTicket         = codigo;
        this.horarioEmision       = emision;
        this.fechaTicket          = fecha;
        this.tipoComida           = tipoComida;
        this.horarioEntregaTicket = entrega;
        this.horarioComida        = comida;
        this.estado               = estado;
        this.precio               = precio;
        this.usuario              = usuario;
        this.sede                 = sede;
        this.horarioCanje         = horarioCanje;
        if (numero >= contadorGlobal) contadorGlobal = numero + 1;
    }

    private String generarCodigo(TipoComida comida, TipoUsuario tipo) {
        String prefixComida = comida.name().substring(0, 1);
        String prefixTipo   = switch (tipo) {
            case ESTUDIANTE            -> "E";
            case PROFESOR              -> "P";
            case PERSONAL_ADMINISTRATIVO -> "A";
        };
        return String.format("%s%s-%05d", prefixComida, prefixTipo, contadorGlobal);
    }

    private double calcularPrecio(TipoComida comida, TipoUsuario tipo) {
        return switch (comida) {
            case DESAYUNO -> switch (tipo) {
                case ESTUDIANTE            -> 2.50;
                case PROFESOR              -> 3.50;
                case PERSONAL_ADMINISTRATIVO -> 3.00;
            };
            case ALMUERZO -> switch (tipo) {
                case ESTUDIANTE            -> 4.00;
                case PROFESOR              -> 6.00;
                case PERSONAL_ADMINISTRATIVO -> 5.00;
            };
            case CENA -> switch (tipo) {
                case ESTUDIANTE            -> 3.00;
                case PROFESOR              -> 5.00;
                case PERSONAL_ADMINISTRATIVO -> 4.00;
            };
        };
    }

    // ── Validaciones ──────────────────────────────────────────────────────────

    /** Verifica si el ticket sigue vigente (no vencido, no cancelado, no canjeado). */
    public boolean esValido() {
        actualizarVencimiento();
        return estado == EstadoTicket.ACTIVO;
    }

    /** Verifica si el ticket puede canjearse ahora (dentro del horario de comida). */
    public boolean puedeCanjearse() {
        if (!esValido()) return false;
        if (!fechaTicket.equals(LocalDate.now())) return false;
        LocalTime ahora = LocalTime.now();
        return horarioComida.contiene(ahora);
    }

    /** Actualiza estado a VENCIDO si la fecha o el horario de canje ya pasaron. */
    public void actualizarVencimiento() {
        if (estado != EstadoTicket.ACTIVO) return;
        LocalDate hoy   = LocalDate.now();
        LocalTime ahora = LocalTime.now();
        // Vence si la fecha ya pasó o si el horario de comida ya terminó en el mismo día
        if (fechaTicket.isBefore(hoy)) {
            estado = EstadoTicket.VENCIDO;
        } else if (fechaTicket.equals(hoy) && ahora.isAfter(horarioComida.getFin())) {
            estado = EstadoTicket.VENCIDO;
        }
    }

    /** Marca el ticket como CANJEADO. */
    public boolean canjear() {
        if (!puedeCanjearse()) return false;
        estado       = EstadoTicket.CANJEADO;
        horarioCanje = LocalDateTime.now();
        return true;
    }

    /** Cancela el ticket si está activo. */
    public boolean cancelar() {
        if (estado != EstadoTicket.ACTIVO) return false;
        estado = EstadoTicket.CANCELADO;
        return true;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public int           getNumeroTicket()         { return numeroTicket; }
    public String        getCodigoTicket()          { return codigoTicket; }
    public LocalDateTime getHorarioEmision()        { return horarioEmision; }
    public LocalDate     getFechaTicket()           { return fechaTicket; }
    public TipoComida    getTipoComida()             { return tipoComida; }
    public RangoHorario  getHorarioEntregaTicket()  { return horarioEntregaTicket; }
    public RangoHorario  getHorarioComida()         { return horarioComida; }
    public EstadoTicket  getEstado()                { return estado; }
    public double        getPrecio()                { return precio; }
    public Usuario       getUsuario()               { return usuario; }
    public Sede          getSede()                  { return sede; }
    public LocalDateTime getHorarioCanje()          { return horarioCanje; }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return String.format(
            "Ticket #%05d [%s]\n" +
            "  Codigo    : %s\n" +
            "  Comida    : %s\n" +
            "  Entrega   : %s\n" +
            "  Comida    : %s\n" +
            "  Emitido   : %s\n" +
            "  Estado    : %s\n" +
            "  Precio    : S/. %.2f\n" +
            "  Sede      : %s",
            numeroTicket, usuario.getNombreCompleto(),
            codigoTicket,
            tipoComida,
            horarioEntregaTicket,
            horarioComida,
            horarioEmision.format(fmt),
            estado,
            precio,
            sede
        );
    }

    public static void resetContador(int valor) {
        contadorGlobal = valor;
    }
}
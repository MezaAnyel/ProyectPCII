package comedor.sistema;

import comedor.enums.*;
import comedor.modelo.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Gestiona toda la lógica de negocio del comedor universitario.
 * Maneja usuarios, tickets y persistencia en Excel.
 */
public class GestorComedor {

    private final Map<String, Usuario> usuarios = new LinkedHashMap<>();
    private final List<Ticket>         tickets  = new ArrayList<>();

    public GestorComedor() {
        cargarDatos();
    }

    // ─────────────────────────────── CARGA Y GUARDADO ─────────────────────────

    public final void cargarDatos() {
        List<Usuario> listaU = ExcelManager.cargarUsuarios();
        usuarios.clear();
        for (Usuario u : listaU) {
            usuarios.put(u.getCodigoIdentificacion(), u);
        }

        tickets.clear();
        List<Ticket> listaT = ExcelManager.cargarTickets(usuarios);
        tickets.addAll(listaT);

        // Asociar tickets activos a usuarios
        for (Ticket t : tickets) {
            if (t.getEstado() == EstadoTicket.ACTIVO) {
                t.getUsuario().getTicketsActivos().clear();
            }
        }
        for (Ticket t : tickets) {
            if (t.getEstado() == EstadoTicket.ACTIVO) {
                t.getUsuario().agregarTicket(t);
            }
        }
    }

    public void guardarDatos() {
        actualizarTodosVencimientos();
        ExcelManager.guardarUsuarios(new ArrayList<>(usuarios.values()));
        ExcelManager.guardarTickets(tickets);
    }

    // ─────────────────────────────── USUARIOS ─────────────────────────────────

    public boolean agregarUsuario(Usuario u) {
        if (usuarios.containsKey(u.getCodigoIdentificacion())) return false;
        usuarios.put(u.getCodigoIdentificacion(), u);
        guardarDatos();
        return true;
    }

    public Usuario buscarUsuario(String codigo) {
        return usuarios.get(codigo);
    }

    public List<Usuario> listarUsuarios() {
        return new ArrayList<>(usuarios.values());
    }

    // ─────────────────────────────── TICKETS ──────────────────────────────────

    /**
     * Solicita un ticket para una comida específica.
     * Validaciones:
     *   1. El usuario existe.
     *   2. Estamos dentro de la ventana de entrega de ticket para su tipo.
     *   3. El usuario no tiene ya un ticket activo del mismo tipo de comida para hoy.
     */
    public String solicitarTicket(String codigoUsuario, TipoComida tipoComida, Sede sede) {
        Usuario usuario = usuarios.get(codigoUsuario);
        if (usuario == null) {
            return "ERROR: Usuario no encontrado con código: " + codigoUsuario;
        }

        // Verificar ventana de entrega
        if (!HorarioManager.estaEnVentanaEntrega(tipoComida, usuario.getTipoUsuario())) {
            RangoHorario ventana = HorarioManager.getVentanaEntrega(tipoComida, usuario.getTipoUsuario());
            return "ERROR: Fuera de la ventana de entrega de ticket.\n" +
                   "  Tu ventana es: " + ventana + "\n" +
                   "  Hora actual  : " + LocalTime.now().getHour() + ":" +
                   String.format("%02d", LocalTime.now().getMinute());
        }

        // Verificar ticket duplicado para hoy
        boolean duplicado = tickets.stream().anyMatch(t ->
                t.getUsuario().getCodigoIdentificacion().equals(codigoUsuario) &&
                t.getTipoComida() == tipoComida &&
                t.getFechaTicket().equals(LocalDate.now()) &&
                (t.getEstado() == EstadoTicket.ACTIVO || t.getEstado() == EstadoTicket.CANJEADO)
        );
        if (duplicado) {
            return "ERROR: Ya tienes un ticket de " + tipoComida + " para hoy.";
        }

        Ticket ticket = new Ticket(tipoComida, usuario, sede);
        tickets.add(ticket);
        usuario.agregarTicket(ticket);
        guardarDatos();

        return "OK: Ticket emitido exitosamente.\n" + ticket;
    }

    /**
     * Canjea un ticket por su número.
     * Validaciones:
     *   1. El ticket existe y pertenece al usuario.
     *   2. El ticket está activo (no vencido, no cancelado, no ya canjeado).
     *   3. Estamos dentro del horario de comida del ticket.
     *   4. El ticket es del día actual.
     */
    public String canjearTicket(int numeroTicket, String codigoUsuario) {
        Ticket ticket = buscarTicketPorNumero(numeroTicket);
        if (ticket == null) {
            return "ERROR: Ticket #" + numeroTicket + " no encontrado.";
        }
        if (!ticket.getUsuario().getCodigoIdentificacion().equals(codigoUsuario)) {
            return "ERROR: El ticket no pertenece al usuario " + codigoUsuario + ".";
        }

        ticket.actualizarVencimiento();

        if (ticket.getEstado() == EstadoTicket.CANJEADO) {
            return "ERROR: El ticket ya fue canjeado.";
        }
        if (ticket.getEstado() == EstadoTicket.CANCELADO) {
            return "ERROR: El ticket está cancelado.";
        }
        if (ticket.getEstado() == EstadoTicket.VENCIDO) {
            return "ERROR: El ticket está vencido (expiró el " +
                   ticket.getFechaTicket() + " a las " +
                   ticket.getHorarioComida().getFin() + ").";
        }

        if (!ticket.puedeCanjearse()) {
            RangoHorario horarioComida = ticket.getHorarioComida();
            LocalTime ahora = LocalTime.now();
            if (ahora.isBefore(horarioComida.getInicio())) {
                return "ERROR: Aún no es hora de comer.\n" +
                       "  Horario de comida: " + horarioComida + "\n" +
                       "  Hora actual       : " + ahora.getHour() + ":" +
                       String.format("%02d", ahora.getMinute());
            }
            return "ERROR: Fuera del horario de comida para este ticket.";
        }

        boolean ok = ticket.canjear();
        if (ok) {
            ticket.getUsuario().removerTicket(ticket);
            guardarDatos();
            return "OK: Ticket canjeado exitosamente.\n" + ticket;
        }
        return "ERROR: No se pudo canjear el ticket.";
    }

    /**
     * Cancela un ticket activo.
     * Solo se puede cancelar si el ticket está activo.
     */
    public String cancelarTicket(int numeroTicket, String codigoUsuario) {
        Ticket ticket = buscarTicketPorNumero(numeroTicket);
        if (ticket == null) {
            return "ERROR: Ticket #" + numeroTicket + " no encontrado.";
        }
        if (!ticket.getUsuario().getCodigoIdentificacion().equals(codigoUsuario)) {
            return "ERROR: El ticket no pertenece al usuario " + codigoUsuario + ".";
        }

        ticket.actualizarVencimiento();

        if (ticket.getEstado() != EstadoTicket.ACTIVO) {
            return "ERROR: Solo se pueden cancelar tickets activos. Estado actual: " + ticket.getEstado();
        }

        ticket.cancelar();
        ticket.getUsuario().removerTicket(ticket);
        guardarDatos();
        return "OK: Ticket #" + numeroTicket + " cancelado exitosamente.";
    }

    // ─────────────────────────────── CONSULTAS ────────────────────────────────

    public List<Ticket> getTicketsDelDia() {
        LocalDate hoy = LocalDate.now();
        actualizarTodosVencimientos();
        return tickets.stream()
                .filter(t -> t.getFechaTicket().equals(hoy))
                .sorted(Comparator.comparingInt(Ticket::getNumeroTicket))
                .collect(Collectors.toList());
    }

    public List<Ticket> getTicketsUsuario(String codigoUsuario) {
        actualizarTodosVencimientos();
        return tickets.stream()
                .filter(t -> t.getUsuario().getCodigoIdentificacion().equals(codigoUsuario))
                .sorted(Comparator.comparing(Ticket::getFechaTicket).reversed())
                .collect(Collectors.toList());
    }

    public List<Ticket> getTodosTickets() {
        actualizarTodosVencimientos();
        return Collections.unmodifiableList(tickets);
    }

    public Ticket buscarTicketPorNumero(int numero) {
        return tickets.stream()
                .filter(t -> t.getNumeroTicket() == numero)
                .findFirst().orElse(null);
    }

    public Ticket buscarTicketPorCodigo(String codigo) {
        return tickets.stream()
                .filter(t -> t.getCodigoTicket().equalsIgnoreCase(codigo))
                .findFirst().orElse(null);
    }

    // ─────────────────────────────── REPORTES ─────────────────────────────────

    public void generarReporteVentasFacultad() {
        actualizarTodosVencimientos();
        ExcelManager.generarReporteVentasPorFacultad(tickets);
    }

    public void generarReporteVentasSede() {
        actualizarTodosVencimientos();
        ExcelManager.generarReporteVentasPorSede(tickets);
    }

    public void generarReporteTicketsDia() {
        actualizarTodosVencimientos();
        ExcelManager.generarReporteTicketsDia(tickets);
    }

    public String resumenTicketsDia() {
        List<Ticket> hoy = getTicketsDelDia();
        long activos   = hoy.stream().filter(t -> t.getEstado() == EstadoTicket.ACTIVO).count();
        long canjeados = hoy.stream().filter(t -> t.getEstado() == EstadoTicket.CANJEADO).count();
        long cancelados = hoy.stream().filter(t -> t.getEstado() == EstadoTicket.CANCELADO).count();
        long vencidos  = hoy.stream().filter(t -> t.getEstado() == EstadoTicket.VENCIDO).count();
        double total   = hoy.stream().filter(t -> t.getEstado() == EstadoTicket.CANJEADO)
                            .mapToDouble(Ticket::getPrecio).sum();

        return String.format(
            "=== RESUMEN DEL DÍA %s ===\n" +
            "  Total tickets  : %d\n" +
            "  Activos        : %d\n" +
            "  Canjeados      : %d\n" +
            "  Cancelados     : %d\n" +
            "  Vencidos       : %d\n" +
            "  Recaudado      : S/. %.2f",
            LocalDate.now(), hoy.size(), activos, canjeados, cancelados, vencidos, total
        );
    }

    private void actualizarTodosVencimientos() {
        tickets.forEach(Ticket::actualizarVencimiento);
    }
}
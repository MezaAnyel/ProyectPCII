package comedor.modelo;

import comedor.enums.Sede;
import comedor.enums.TipoUsuario;
import java.util.ArrayList;
import java.util.List;

public abstract class Usuario {
    private final String codigoIdentificacion;
    private final String nombreCompleto;
    private final String email;
    private final String facultad;
    private final TipoUsuario tipoUsuario;
    private final Sede sede;
    private final List<Ticket> ticketsActivos;

    public Usuario(String codigoIdentificacion, String nombreCompleto,
                   String email, String facultad, TipoUsuario tipoUsuario, Sede sede) {
        this.codigoIdentificacion = codigoIdentificacion;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.facultad = facultad;
        this.tipoUsuario = tipoUsuario;
        this.sede = sede;
        this.ticketsActivos = new ArrayList<>();
    }

    public String getCodigoIdentificacion() { return codigoIdentificacion; }
    public String getNombreCompleto()        { return nombreCompleto; }
    public String getEmail()                 { return email; }
    public String getFacultad()              { return facultad; }
    public TipoUsuario getTipoUsuario()      { return tipoUsuario; }
    public Sede getSede()                    { return sede; }
    public List<Ticket> getTicketsActivos()  { return ticketsActivos; }

    public void agregarTicket(Ticket ticket) {
        ticketsActivos.add(ticket);
    }

    public void removerTicket(Ticket ticket) {
        ticketsActivos.remove(ticket);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | %s | %s",
                codigoIdentificacion, nombreCompleto, tipoUsuario, facultad, sede);
    }
}
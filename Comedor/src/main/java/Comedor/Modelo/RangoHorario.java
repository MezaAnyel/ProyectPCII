package comedor.modelo;

import java.time.LocalTime;

public class RangoHorario {
    private final LocalTime inicio;
    private final LocalTime fin;

    public RangoHorario(LocalTime inicio, LocalTime fin) {
        this.inicio = inicio;
        this.fin = fin;
    }

    public LocalTime getInicio() { return inicio; }
    public LocalTime getFin()    { return fin; }

    public boolean contiene(LocalTime tiempo) {
        return !tiempo.isBefore(inicio) && tiempo.isBefore(fin);
    }

    @Override
    public String toString() {
        return String.format("%02d:%02d - %02d:%02d",
                inicio.getHour(), inicio.getMinute(),
                fin.getHour(), fin.getMinute());
    }
}
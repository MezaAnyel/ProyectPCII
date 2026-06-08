package comedor.sistema;

import comedor.enums.*;
import comedor.modelo.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Gestiona la persistencia del sistema usando archivos Excel (.xlsx).
 *
 * Archivos generados:
 *   - usuarios.xlsx    : registro de todos los usuarios
 *   - tickets.xlsx     : historial completo de tickets
 */
public class ExcelManager {

    public static final String ARCHIVO_USUARIOS = "data/usuarios.xlsx";
    public static final String ARCHIVO_TICKETS  = "data/tickets.xlsx";

    private static final DateTimeFormatter FMT_FECHA    = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_DATETIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter FMT_TIME     = DateTimeFormatter.ofPattern("HH:mm");

    static {
        new File("data").mkdirs();
    }

    // ─────────────────────────────── USUARIOS ─────────────────────────────────

    public static void guardarUsuarios(List<Usuario> usuarios) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Usuarios");

            CellStyle headerStyle = crearEstiloHeader(wb);
            Row header = sheet.createRow(0);
            String[] cols = {"Codigo", "Nombre Completo", "Tipo", "Facultad", "Email", "Sede"};
            for (int i = 0; i < cols.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }

            int row = 1;
            for (Usuario u : usuarios) {
                Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(u.getCodigoIdentificacion());
                r.createCell(1).setCellValue(u.getNombreCompleto());
                r.createCell(2).setCellValue(u.getTipoUsuario().name());
                r.createCell(3).setCellValue(u.getFacultad());
                r.createCell(4).setCellValue(u.getEmail());
                r.createCell(5).setCellValue(u.getSede().name());
            }

            autoSizeCols(sheet, cols.length);
            escribir(wb, ARCHIVO_USUARIOS);
        } catch (IOException e) {
            System.err.println("Error al guardar usuarios: " + e.getMessage());
        }
    }

    public static List<Usuario> cargarUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        if (!new File(ARCHIVO_USUARIOS).exists()) return lista;

        try (Workbook wb = new XSSFWorkbook(new FileInputStream(ARCHIVO_USUARIOS))) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row r = sheet.getRow(i);
                if (r == null) continue;
                String codigo   = cell(r, 0);
                String nombre   = cell(r, 1);
                String tipoStr  = cell(r, 2);
                String facultad = cell(r, 3);
                String email    = cell(r, 4);
                String sedeStr  = cell(r, 5);

                TipoUsuario tipo = TipoUsuario.valueOf(tipoStr);
                Sede sede        = Sede.valueOf(sedeStr);

                Usuario u = switch (tipo) {
                    case ESTUDIANTE              -> new Estudiante(codigo, nombre, email, facultad, sede);
                    case PROFESOR                -> new Profesor(codigo, nombre, email, facultad, sede);
                    case PERSONAL_ADMINISTRATIVO -> new PersonalAdministrativo(codigo, nombre, email, facultad, sede);
                };
                lista.add(u);
            }
        } catch (IOException e) {
            System.err.println("Error al cargar usuarios: " + e.getMessage());
        }
        return lista;
    }

    // ─────────────────────────────── TICKETS ──────────────────────────────────

    public static void guardarTickets(List<Ticket> tickets) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Tickets");

            CellStyle headerStyle = crearEstiloHeader(wb);
            Row header = sheet.createRow(0);
            String[] cols = {
                "Numero", "Codigo", "Fecha", "Emision",
                "Tipo Comida", "Entrega Inicio", "Entrega Fin",
                "Comida Inicio", "Comida Fin",
                "Estado", "Precio", "Sede",
                "Cod Usuario", "Nombre Usuario", "Tipo Usuario", "Facultad",
                "Hora Canje"
            };
            for (int i = 0; i < cols.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(headerStyle);
            }

            int row = 1;
            for (Ticket t : tickets) {
                t.actualizarVencimiento();
                Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(t.getNumeroTicket());
                r.createCell(1).setCellValue(t.getCodigoTicket());
                r.createCell(2).setCellValue(t.getFechaTicket().format(FMT_FECHA));
                r.createCell(3).setCellValue(t.getHorarioEmision().format(FMT_DATETIME));
                r.createCell(4).setCellValue(t.getTipoComida().name());
                r.createCell(5).setCellValue(t.getHorarioEntregaTicket().getInicio().format(FMT_TIME));
                r.createCell(6).setCellValue(t.getHorarioEntregaTicket().getFin().format(FMT_TIME));
                r.createCell(7).setCellValue(t.getHorarioComida().getInicio().format(FMT_TIME));
                r.createCell(8).setCellValue(t.getHorarioComida().getFin().format(FMT_TIME));
                r.createCell(9).setCellValue(t.getEstado().name());
                r.createCell(10).setCellValue(t.getPrecio());
                r.createCell(11).setCellValue(t.getSede().name());
                r.createCell(12).setCellValue(t.getUsuario().getCodigoIdentificacion());
                r.createCell(13).setCellValue(t.getUsuario().getNombreCompleto());
                r.createCell(14).setCellValue(t.getUsuario().getTipoUsuario().name());
                r.createCell(15).setCellValue(t.getUsuario().getFacultad());
                r.createCell(16).setCellValue(t.getHorarioCanje() != null
                        ? t.getHorarioCanje().format(FMT_DATETIME) : "");
            }

            autoSizeCols(sheet, cols.length);
            escribir(wb, ARCHIVO_TICKETS);
        } catch (IOException e) {
            System.err.println("Error al guardar tickets: " + e.getMessage());
        }
    }

    public static List<Ticket> cargarTickets(Map<String, Usuario> mapaUsuarios) {
        List<Ticket> lista = new ArrayList<>();
        if (!new File(ARCHIVO_TICKETS).exists()) return lista;

        try (Workbook wb = new XSSFWorkbook(new FileInputStream(ARCHIVO_TICKETS))) {
            Sheet sheet = wb.getSheetAt(0);
            int maxNumero = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row r = sheet.getRow(i);
                if (r == null) continue;
                try {
                    int    numero    = (int) r.getCell(0).getNumericCellValue();
                    String codigo    = cell(r, 1);
                    LocalDate fecha  = LocalDate.parse(cell(r, 2), FMT_FECHA);
                    LocalDateTime emision = LocalDateTime.parse(cell(r, 3), FMT_DATETIME);
                    TipoComida comida     = TipoComida.valueOf(cell(r, 4));
                    LocalTime entIni = LocalTime.parse(cell(r, 5), FMT_TIME);
                    LocalTime entFin = LocalTime.parse(cell(r, 6), FMT_TIME);
                    LocalTime comIni = LocalTime.parse(cell(r, 7), FMT_TIME);
                    LocalTime comFin = LocalTime.parse(cell(r, 8), FMT_TIME);
                    EstadoTicket estado = EstadoTicket.valueOf(cell(r, 9));
                    double precio = r.getCell(10).getNumericCellValue();
                    Sede sede = Sede.valueOf(cell(r, 11));
                    String codUsuario = cell(r, 12);
                    String canjeStr   = cell(r, 16);
                    LocalDateTime canje = canjeStr.isEmpty() ? null
                            : LocalDateTime.parse(canjeStr, FMT_DATETIME);

                    Usuario usuario = mapaUsuarios.get(codUsuario);
                    if (usuario == null) continue;

                    Ticket t = new Ticket(numero, codigo, emision, fecha, comida,
                            new RangoHorario(entIni, entFin),
                            new RangoHorario(comIni, comFin),
                            estado, precio, usuario, sede, canje);
                    t.actualizarVencimiento();
                    lista.add(t);
                    if (numero > maxNumero) maxNumero = numero;
                } catch (Exception ex) {
                    System.err.println("  Fila " + i + " ignorada: " + ex.getMessage());
                }
            }
            if (maxNumero > 0) Ticket.resetContador(maxNumero + 1);
        } catch (IOException e) {
            System.err.println("Error al cargar tickets: " + e.getMessage());
        }
        return lista;
    }

    // ─────────────────────────────── REPORTES ─────────────────────────────────

    public static void generarReporteVentasPorFacultad(List<Ticket> tickets) {
        Map<String, Double> totales  = new LinkedHashMap<>();
        Map<String, Integer> cantidad = new LinkedHashMap<>();

        for (Ticket t : tickets) {
            if (t.getEstado() == EstadoTicket.CANJEADO) {
                String fac = t.getUsuario().getFacultad();
                totales.merge(fac, t.getPrecio(), Double::sum);
                cantidad.merge(fac, 1, Integer::sum);
            }
        }

        String archivo = "data/reporte_ventas_facultad_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Ventas por Facultad");
            CellStyle hs = crearEstiloHeader(wb);

            Row header = sheet.createRow(0);
            for (int i = 0; i < 3; i++) header.createCell(i).setCellStyle(hs);
            header.getCell(0).setCellValue("Facultad");
            header.getCell(1).setCellValue("Tickets Canjeados");
            header.getCell(2).setCellValue("Total (S/.)");

            int row = 1;
            double gran = 0;
            for (String fac : totales.keySet()) {
                Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(fac);
                r.createCell(1).setCellValue(cantidad.get(fac));
                r.createCell(2).setCellValue(totales.get(fac));
                gran += totales.get(fac);
            }
            Row total = sheet.createRow(row);
            CellStyle ts = crearEstiloHeader(wb);
            total.createCell(0).setCellValue("TOTAL");
            total.createCell(0).setCellStyle(ts);
            total.createCell(1).setCellValue(cantidad.values().stream().mapToInt(Integer::intValue).sum());
            total.createCell(1).setCellStyle(ts);
            total.createCell(2).setCellValue(gran);
            total.createCell(2).setCellStyle(ts);

            autoSizeCols(sheet, 3);
            escribir(wb, archivo);
            System.out.println("  → Reporte guardado: " + archivo);
        } catch (IOException e) {
            System.err.println("Error generando reporte: " + e.getMessage());
        }
    }

    public static void generarReporteVentasPorSede(List<Ticket> tickets) {
        Map<String, Double> totales   = new LinkedHashMap<>();
        Map<String, Integer> cantidad = new LinkedHashMap<>();
        for (Sede s : Sede.values()) { totales.put(s.getNombre(), 0.0); cantidad.put(s.getNombre(), 0); }

        for (Ticket t : tickets) {
            if (t.getEstado() == EstadoTicket.CANJEADO) {
                String sede = t.getSede().getNombre();
                totales.merge(sede, t.getPrecio(), Double::sum);
                cantidad.merge(sede, 1, Integer::sum);
            }
        }

        String archivo = "data/reporte_ventas_sede_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Ventas por Sede");
            CellStyle hs = crearEstiloHeader(wb);

            Row header = sheet.createRow(0);
            for (int i = 0; i < 3; i++) header.createCell(i).setCellStyle(hs);
            header.getCell(0).setCellValue("Sede");
            header.getCell(1).setCellValue("Tickets Canjeados");
            header.getCell(2).setCellValue("Total (S/.)");

            int row = 1;
            double gran = 0;
            for (String sede : totales.keySet()) {
                Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(sede);
                r.createCell(1).setCellValue(cantidad.get(sede));
                r.createCell(2).setCellValue(totales.get(sede));
                gran += totales.get(sede);
            }
            Row total = sheet.createRow(row);
            CellStyle ts = crearEstiloHeader(wb);
            total.createCell(0).setCellValue("TOTAL");
            total.createCell(0).setCellStyle(ts);
            total.createCell(1).setCellValue(cantidad.values().stream().mapToInt(Integer::intValue).sum());
            total.createCell(1).setCellStyle(ts);
            total.createCell(2).setCellValue(gran);
            total.createCell(2).setCellStyle(ts);

            autoSizeCols(sheet, 3);
            escribir(wb, archivo);
            System.out.println("  → Reporte guardado: " + archivo);
        } catch (IOException e) {
            System.err.println("Error generando reporte: " + e.getMessage());
        }
    }

    public static void generarReporteTicketsDia(List<Ticket> tickets) {
        LocalDate hoy = LocalDate.now();
        String archivo = "data/reporte_tickets_dia_" +
                hoy.format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Tickets del Dia");
            CellStyle hs = crearEstiloHeader(wb);

            Row header = sheet.createRow(0);
            String[] cols = {"#", "Codigo", "Usuario", "Tipo Usuario", "Facultad",
                    "Sede", "Comida", "Estado", "Precio (S/.)", "Emision", "Canje"};
            for (int i = 0; i < cols.length; i++) {
                Cell c = header.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(hs);
            }

            int row = 1;
            for (Ticket t : tickets) {
                if (!t.getFechaTicket().equals(hoy)) continue;
                t.actualizarVencimiento();
                Row r = sheet.createRow(row++);
                r.createCell(0).setCellValue(t.getNumeroTicket());
                r.createCell(1).setCellValue(t.getCodigoTicket());
                r.createCell(2).setCellValue(t.getUsuario().getNombreCompleto());
                r.createCell(3).setCellValue(t.getUsuario().getTipoUsuario().getDescripcion());
                r.createCell(4).setCellValue(t.getUsuario().getFacultad());
                r.createCell(5).setCellValue(t.getSede().getNombre());
                r.createCell(6).setCellValue(t.getTipoComida().getDescripcion());
                r.createCell(7).setCellValue(t.getEstado().getDescripcion());
                r.createCell(8).setCellValue(t.getPrecio());
                r.createCell(9).setCellValue(t.getHorarioEmision().format(FMT_DATETIME));
                r.createCell(10).setCellValue(t.getHorarioCanje() != null
                        ? t.getHorarioCanje().format(FMT_DATETIME) : "-");
            }

            autoSizeCols(sheet, cols.length);
            escribir(wb, archivo);
            System.out.println("  → Reporte guardado: " + archivo);
        } catch (IOException e) {
            System.err.println("Error generando reporte: " + e.getMessage());
        }
    }

    // ─────────────────────────────── UTILIDADES ───────────────────────────────

    private static CellStyle crearEstiloHeader(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    private static void autoSizeCols(Sheet sheet, int n) {
        for (int i = 0; i < n; i++) {
            sheet.autoSizeColumn(i);
            sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i) + 512, 15000));
        }
    }

    private static void escribir(Workbook wb, String ruta) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(ruta)) {
            wb.write(fos);
        }
    }

    private static String cell(Row row, int col) {
        Cell c = row.getCell(col);
        if (c == null) return "";
        return switch (c.getCellType()) {
            case STRING  -> c.getStringCellValue().trim();
            case NUMERIC -> {
                double v = c.getNumericCellValue();
                yield v == (int) v ? String.valueOf((int) v) : String.valueOf(v);
            }
            default -> c.toString().trim();
        };
    }
}
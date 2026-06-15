package comedor.sistema;

import java.util.Scanner;

import comedor.enums.Sede;
import comedor.enums.TipoComida;

public class Principal {

    public static void main(String[] args) {
        // Inicializa el gestor principal de la aplicación
        GestorComedor gestor = new GestorComedor();
        Scanner scanner = new Scanner(System.in);
        int opcion = -1;

        System.out.println("=================================================");
        System.out.println("   SISTEMA DE GESTIÓN - COMEDOR UNIVERSITARIO   ");
        System.out.println("=================================================");
        
        while (opcion != 0) {
            System.out.println("\n-----------------------------------------");
            System.out.println("              MENÚ PRINCIPAL             ");
            System.out.println("-----------------------------------------");
            System.out.println("1. Mostrar Horarios y Ventanas de Entrega");
            System.out.println("2. Solicitar un Nuevo Ticket");
            System.out.println("3. Canjear un Ticket");
            System.out.println("4. Cancelar un Ticket");
            System.out.println("5. Ver Resumen Estadístico del Día");
            System.out.println("6. Generar Reportes en Excel (.xlsx)");
            System.out.println("0. Salir del Sistema");
            System.out.print("Seleccione una opción: ");

            if (scanner.hasNextInt()) {
                opcion = scanner.nextInt();
                scanner.nextLine(); // Limpiar el salto de línea del buffer
            } else {
                scanner.nextLine(); // Limpiar entrada inválida
                System.out.println("Error: Por favor, ingrese un número entero válido.");
                continue;
            }

            switch (opcion) {
                case 1:
                    System.out.println("\n" + HorarioManager.mostrarHorarios());
                    break;
                    
                case 2:
                    System.out.print("\nIngrese el código de identificación del usuario: ");
                    String codSolicitud = scanner.nextLine().trim();
                    
                    // Selección Interactiva de Tipo de Comida
                    System.out.println("Seleccione el Tipo de Comida:");
                    System.out.println("  1. Desayuno");
                    System.out.println("  2. Almuerzo");
                    System.out.println("  3. Cena");
                    System.out.print("Opción (1-3): ");
                    int opcComida = scanner.nextInt();
                    scanner.nextLine();
                    
                    TipoComida comidaSelected = switch (opcComida) {
                        case 1 -> TipoComida.DESAYUNO;
                        case 3 -> TipoComida.CENA;
                        default -> TipoComida.ALMUERZO;
                    };
                    
                    // Selección Interactiva de Sede basada en tus Enums reales
                    System.out.println("Seleccione la Sede del Comedor:");
                    System.out.println("  1. Sede Central");
                    System.out.println("  2. Sede Norte");
                    System.out.println("  3. Sede Sur");
                    System.out.print("Opción (1-3): ");
                    int opcSede = scanner.nextInt();
                    scanner.nextLine();
                    
                    Sede sedeSelected = switch (opcSede) {
                        case 2 -> Sede.SEDE_NORTE;
                        case 3 -> Sede.SEDE_SUR;
                        default -> Sede.SEDE_CENTRAL;
                    };
                    
                    System.out.println("\nProcesando solicitud...");
                    String resultadoSolicitud = gestor.solicitarTicket(codSolicitud, comidaSelected, sedeSelected);
                    System.out.println(resultadoSolicitud);
                    break;
                    
                case 3:
                    System.out.print("\nIngrese el número de ticket a canjear: ");
                    int numCanje = scanner.nextInt();
                    scanner.nextLine();
                    
                    System.out.print("Ingrese el código de identificación del usuario: ");
                    String codCanje = scanner.nextLine().trim();
                    
                    System.out.println("\nVerificando parámetros de entrega y rango horario...");
                    String resultadoCanje = gestor.canjearTicket(numCanje, codCanje);
                    System.out.println(resultadoCanje);
                    break;
                    
                case 4:
                    System.out.print("\nIngrese el número de ticket a cancelar: ");
                    int numCancel = scanner.nextInt();
                    scanner.nextLine();
                    
                    System.out.print("Ingrese el código de identificación del usuario: ");
                    String codCancel = scanner.nextLine().trim();
                    
                    System.out.println("\nProcesando cancelación de ticket...");
                    String resultadoCancel = gestor.cancelarTicket(numCancel, codCancel);
                    System.out.println(resultadoCancel);
                    break;
                    
                case 5:
                    System.out.println("\n" + gestor.resumenTicketsDia());
                    break;
                    
                case 6:
                    System.out.println("\nGenerando matrices de datos y reportes consolidados...");
                    try {
                        gestor.generarReporteVentasFacultad();
                        gestor.generarReporteVentasSede();
                        gestor.generarReporteTicketsDia();
                        System.out.println("¡Reportes generados exitosamente en la carpeta raíz del proyecto!");
                    } catch (Exception e) {
                        System.out.println("Error al escribir los archivos en disco: " + e.getMessage());
                    }
                    break;
                    
                case 0:
                    System.out.println("\nCerrando flujos y sesiones. Finalizando el programa...");
                    break;
                    
                default:
                    System.out.println("\nOpción inválida. Por favor, seleccione una opción del menú.");
            }
        }
        scanner.close();
    }
}
# Sistema de Comedor Universitario - 3 Sedes

Sistema Java completo de gestión de comedor para una universidad con tres sedes (Sede Central, Sede Norte, Sede Sur).

## Características

✅ **Gestión de Tickets de Comida**
- Solicitar tickets con horarios escalonados por tipo de usuario
- Canjear tickets en el horario de comida correspondiente
- Cancelar tickets activos
- Validación automática de vencimiento (mismo día de emisión)

✅ **Tres Tipos de Usuarios**
- Estudiantes
- Profesores
- Personal Administrativo

✅ **Horarios Escalonados**
- **Desayuno**: 07:00-09:00 (entrega escalonada, comida 08:00-09:00)
- **Almuerzo**: 11:00-13:00 (entrega escalonada, comida 12:00-13:00)
- **Cena**: 18:00-20:00 (entrega escalonada, comida 19:00-20:00)

✅ **Persistencia con Excel**
- Archivo `usuarios.xlsx` - registro de todos los usuarios
- Archivo `tickets.xlsx` - historial completo de tickets
- Reportes automáticos en Excel

✅ **Reportes de Administrador**
- Reporte de ventas por facultad
- Reporte de ventas por sede
- Reporte de tickets del día actual

## Estructura del Proyecto

```
comedor/
├── src/main/java/comedor/
│   ├── enums/
│   │   ├── TipoUsuario.java        (ESTUDIANTE, PROFESOR, PERSONAL_ADMINISTRATIVO)
│   │   ├── TipoComida.java         (DESAYUNO, ALMUERZO, CENA)
│   │   ├── EstadoTicket.java       (ACTIVO, CANJEADO, CANCELADO, VENCIDO)
│   │   └── Sede.java               (SEDE_CENTRAL, SEDE_NORTE, SEDE_SUR)
│   ├── modelo/
│   │   ├── Usuario.java            (clase abstracta)
│   │   ├── Estudiante.java
│   │   ├── Profesor.java
│   │   ├── PersonalAdministrativo.java
│   │   ├── Ticket.java             (validaciones y estados)
│   │   └── RangoHorario.java       (ventanas de tiempo)
│   └── sistema/
│       ├── Principal.java          (MAIN - interfaz de consola)
│       ├── GestorComedor.java      (lógica de negocio)
│       ├── HorarioManager.java     (gestión de horarios)
│       └── ExcelManager.java       (persistencia con Apache POI)
├── pom.xml                          (configuración Maven)
└── data/                            (archivos Excel generados)
    ├── usuarios.xlsx
    ├── tickets.xlsx
    ├── reporte_ventas_facultad_*.xlsx
    ├── reporte_ventas_sede_*.xlsx
    └── reporte_tickets_dia_*.xlsx
```

## Requisitos

- **Java 17+**
- **Maven 3.6+**

## Compilación y Ejecución

### 1. Compilar el proyecto

```bash
cd comedor
mvn clean compile
```

### 2. Ejecutar la aplicación

**Con Maven:**
```bash
mvn exec:java -Dexec.mainClass="comedor.sistema.Principal"
```

**Crear JAR ejecutable:**
```bash
mvn package
java -jar target/comedor-1.0-SNAPSHOT.jar
```

## Uso

### Inicio de Sesión

1. **Como Usuario**: Ingrese su código de identificación (ej: `E001`, `P001`, `A001`)
2. **Como Administrador**: Use contraseña `admin123`
3. **Registrar Usuario**: Opción en el menú principal

### Menú de Usuario

- Solicitar nuevo ticket
- Canjear ticket
- Cancelar ticket
- Ver mis tickets
- Ver horarios del comedor

### Menú de Administrador

- Ver resumen del día
- Generar reportes de ventas
- Ver listado de usuarios
- Ver historial de tickets

## Validaciones Implementadas

✓ **Ventana de Entrega de Ticket**: Solo se permite solicitar ticket dentro de la ventana asignada por tipo de usuario
✓ **Ventana de Comida**: Solo se puede canjear el ticket durante el horario de comida del mismo día
✓ **Vencimiento**: Los tickets expiran automáticamente al finalizar el día o el horario de comida
✓ **Ticket Único por Comida**: No se puede solicitar 2+ tickets del mismo tipo en el mismo día
✓ **Pertenencia**: Solo el usuario propietario puede canjear o cancelar su ticket

## Precios de Tickets

| Comida  | Estudiante | Profesor | Personal Adm |
|---------|-----------|----------|--------------|
| Desayuno | S/. 2.50  | S/. 3.50 | S/. 3.00     |
| Almuerzo | S/. 4.00  | S/. 6.00 | S/. 5.00     |
| Cena     | S/. 3.00  | S/. 5.00 | S/. 4.00     |

## Usuarios de Ejemplo

El sistema carga automáticamente estos usuarios si la base de datos está vacía:

**Estudiantes:**
- `E001` - Juan Pérez García (Ingeniería)
- `E002` - María López Martínez (Medicina)
- `E003` - Carlos Sánchez Ruiz (Derecho)

**Profesores:**
- `P001` - Dr. Ricardo Flores Mendoza (Ingeniería)
- `P002` - Dra. Ana García Rodríguez (Medicina)

**Personal Administrativo:**
- `A001` - Pedro Morales Acosta (Administración)
- `A002` - Lucia Ramírez Salazar (Recursos Humanos)

## Dependencias

- **Apache POI 5.2.3**: Lectura/escritura de archivos Excel
  - poi-ooxml: Soporte para archivos .xlsx

## Archivos de Datos

Los archivos se generan automáticamente en el directorio `data/`:

- `usuarios.xlsx` - Todas los usuarios registrados
- `tickets.xlsx` - Historial completo de tickets con estados
- `reporte_*.xlsx` - Reportes generados por el administrador

## Notas Técnicas

- **Contador Global de Tickets**: Mantiene secuencia única incluso después de recargar
- **Actualización Automática**: Los tickets se validan automáticamente cada vez que se cargan
- **Códigos de Ticket**: Formato `{ComidaPrefijo}{TipoUsuarioPrefijo}-{Numero}` (ej: DA-00001)
- **Horarios 24H**: Usa `java.time` para manejo robusto de fechas y horas

## Autor

Sistema desarrollado para la gestión de comedores universitarios.

## Licencia

Uso libre para fines educativos.

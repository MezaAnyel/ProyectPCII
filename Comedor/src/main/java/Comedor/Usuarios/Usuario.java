/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Comedor.Usuarios;

import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author F20LAB10E03
 */
public abstract class Usuario {
    protected String nombre;
    protected String apellido;
    protected String identificacion;
    protected String email;
    protected ArrayList tickets;
    
    public String nombreCompleto() {
        
    }
    
    public abstract Date obtenerHorarioEntregaTicket();        
    
    public abstract String obtenerTipoUsuario();
    
    public Date obtenerHorarioComida(String tipoComida) {
        return new Date();
    }
    
    public void agregarTicket() {
        
    }
    
    public ArrayList obtenerTicketsActivos() {
        
    }
    
    public ArrayList obtenerTicketsDelDia(Date fecha) {
        
    }
}

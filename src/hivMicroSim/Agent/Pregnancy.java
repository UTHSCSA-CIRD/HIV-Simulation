/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.Agent;

/**
 *
 * @author ManuelLS
 */
public class Pregnancy implements java.io.Serializable{
    private final static long serialVersionUID = 1;
    public final Agent child;
    
    private int ticks;
    public static final int duration = 40;
    
    public boolean step(){
        ticks++;
        return  ticks >=duration;
    }
    
    public Pregnancy(Agent child){
        this.child = child;
        ticks = 0;
    }
    
    
}

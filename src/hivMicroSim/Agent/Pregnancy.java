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
    //might later be used for genetics of familial tracking.
   
    private int month;
    
    public boolean step(){
        month++;
        return month >=9;
    }
    
    public Pregnancy(){
        month = 0;
    }
    
    
}

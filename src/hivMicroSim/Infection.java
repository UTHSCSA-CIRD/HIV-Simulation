/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim;

/**
 *
 * @author ManuelLS
 */
public class Infection implements java.io.Serializable{
    private static final long serialVersionUID = 1; 
    
    private final int disease;
    
    public int getDisease(){
        return disease;
    }
    public Infection(int disease){
        this.disease = disease;
    }
}

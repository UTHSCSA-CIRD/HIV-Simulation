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
public class AlloImmunity implements java.io.Serializable{
    private final static long serialVersionUID = 1; 
    
    private final int agent;
    private int resistance;
    
    public int getAgent(){
        return agent;
    }
    public int getResistance(){
        return resistance;
    }
    public int addResistance(int a){
        resistance += a;
        if(resistance > 100){
            resistance = 100;
            return 100;
        }
        if(resistance < 0){
            resistance = 0;
        }
        return resistance;
    }
    public AlloImmunity(int agent){
        this.agent = agent;
        resistance = 0;
    }
    
}

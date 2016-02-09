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
    private boolean exposed;
    private byte rL; //latency for degredation;
    
    public void degrade(){
        //degrade the alloimmunity with a half life of 3 months
        if(!exposed){
            rL++;
            if(rL == 3){//3 month half life
                resistance = resistance / 2;
                rL = 0;
            }
        }else{
            rL =0;
            exposed = false; //reset
        }
    }
    public int getAgent(){
        return agent;
    }
    public int getResistance(){
        return resistance;
    }
    public int addResistance(int a){
        if(resistance >= 100 && a >0){
            resistance += (a/java.lang.Math.log10(resistance));
        }else{
            resistance += a;
        }
        if(resistance < 0){
            resistance = 0;
        }
        exposed = true;
        return resistance;
    }
    public AlloImmunity(int agent){
        this.agent = agent;
        resistance = 0;
        exposed = true;
    }
    
}

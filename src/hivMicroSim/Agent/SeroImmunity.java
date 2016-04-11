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
public class SeroImmunity implements java.io.Serializable{
    private final int genotype;
    private int resistance;
    private byte rL; //reduction latency a number between 0 and 3(exclusive-- at this point we initiate half life reduction.)
    private boolean exposed;
    private static final long serialVersionUID = 1; 
    
    public void degrade(){
        //this method degrades the seroimmunity if not exposed.
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
    public int getGenotype(){
        return genotype;
    }
    public int getResistance(){
        return resistance;
    }
    public int expose(int a){
        resistance += a;
        if(resistance <0){
            resistance = 0;
        }
        exposed = true;
        return resistance;
    }
    public SeroImmunity(int gene){
        genotype = gene;
        resistance = 1;
    }
    public SeroImmunity(int gene, int degree){
        genotype = gene;
        if(degree>= 0 && degree <= 100){
            resistance = degree;
        }else{//if the passed degree is invalid, set to default of 1
            resistance = 1;
        }
        exposed = true;
    }
}

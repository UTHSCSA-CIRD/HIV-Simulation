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
public class SeroImmunity implements java.io.Serializable{
    private final int genotype;
    private int resistance;
    private static final long serialVersionUID = 1; 
    
    public int getGenotype(){
        return genotype;
    }
    public int getResistance(){
        return resistance;
    }
    public int expose(int a){
        resistance += a;
        if(resistance >100){
            resistance = 100;
            return 100;
        }
        if(resistance <0){
            resistance = 0;
        }
        
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
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.HIV;
                                                                                
/**
 *
 * @author ManuelLS
 */
public class Genotype implements java.io.Serializable{
    private static final long serialVersionUID = 1;
    private final int type;
    private double virulence;
    private final boolean CCR5Dependant;
    //later we can add antiviral resistance. 
    
    public int getGenotype(){return type;}
    public double getVirulence(){return virulence;}
    public double adjustVirulence(double a){
        virulence += a;
        return virulence;
    }
    public boolean getCCR5Dependance(){
        return CCR5Dependant;
    }
    
    public Genotype(int genotype, double virulence, boolean CCR5){
        type = genotype;
        this.virulence = virulence;
        CCR5Dependant = CCR5;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.Agent;

/**
 *
 * @author manuells
 */
public class Gene {
    
    public final byte ccr5a;
    public final byte ccr5b;
    
    //CCR5
    public static final double[] ccr5susceptibility = {0,-.5,.5};
    public static final double[] ccr5progression = {0,-.5,.5};
    public static final String CCR5EffectString = "R5 Suceptibility and progression";
    public static final char CCR5Effect = 'b';
    public static final byte CCR5EffectZone = 5; //R5
    public static final byte CCR5WildType = 0;
    //http://www.ncbi.nlm.nih.gov/pmc/articles/PMC3958329/table/t3-gmb-37-7/ (rates per race) HHG*2 is CCR5D32
    public static final byte CCR5D32 = 1; // 8% allele frequency in europeans ~ 1% or less in other races. 
    public static final double CCR5D32Effect = .5;
    public static final byte CCR5HHEP1 = 2;
    public static final double CCR5HHEP1Effect = 1.5; // 32% in europeanhttp://www.ncbi.nlm.nih.gov/pmc/articles/PMC3958329/
    
    //Initial Prevalences:
    //CCR5
    public static final double ccr5Delta32Prev = .08; //.00-1 - decimal percentage of population with ccr5 resistance.
    public static final double ccr5HHEPrev = .32;
    
    public double getHIVInfectionRisk(){
       return 1 + ccr5susceptibility[ccr5a] 
                + ccr5susceptibility[ccr5b];
    }
    public double getHIVProgressionFactor(){
       return 1 + ccr5susceptibility[ccr5a] 
                + ccr5susceptibility[ccr5b];
    }
    public static byte rollCCR5(double roll){
        double L = ccr5Delta32Prev;
        if(roll < L) return CCR5D32;
        L += ccr5HHEPrev;
        if(roll < L) return CCR5HHEP1;
        return CCR5WildType;
    }
    public Gene(byte a, byte b){
        ccr5a = a;
        ccr5b=b;
    }
    
}

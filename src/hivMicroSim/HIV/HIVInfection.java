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
public class HIVInfection implements Comparable<HIVInfection>, java.io.Serializable{
    private static final long serialVersionUID = 1;
    /*
    *This class exists to classify the individual class of virus within an agent. Each class of virus may be effected by
    *different factors such as antibacterial resistance, CCR5 resistance, or virulence. Each host may have resistance to
    *particular strains created by "Seroimmunity" or the bodies own immune defenses against a particular infection. 
    */
    private final int genotype;
    private final int virulence; // note that this virulence is specific to the HOST. 
    //It does NOT take seroimmunity into account as this is ADAPTABLE immunity and virulence to the host genome will be represented as 
    //FINAL. No viral adaptation to host or antivirals yet.
    private final boolean CCR5Resist;
    
    public int getGenotype(){
        return genotype;
    }
    public int getVirulence(){
        return virulence;
    }
    public boolean getCCR5Resistance(){
        return CCR5Resist;
    }
    @Override
    public int compareTo(HIVInfection a){
        //returns the infectivity comparison, this is used to organize the genotype list in the disease matrix. 
        //because the more virulent infections will spread more easily. e.g. in someone heterozygous for CCR5 delta 32 mutation
        //a strain with 900 virulence and 50% effectivness (requires CCR5) will be less likely to contaminate another than a strain at
        //600 virulence, but with no CCR5 reliance.
        
        //this allows it to sort DESCENDING by making higher values lower. 
        if(virulence > a.getVirulence())return -1;
        if(virulence < a.getVirulence()) return 1;
        return 0;
    }
    public HIVInfection(Genotype g, int virulence){
        genotype = g.getGenotype();
        this.virulence = virulence;
        CCR5Resist = g.getCCR5Dependance();
    }
    
}


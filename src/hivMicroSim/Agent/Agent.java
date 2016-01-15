/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.Agent;
import hivMicroSim.AlloImmunity;
import hivMicroSim.HIV.DiseaseMatrix;
import hivMicroSim.HIV.Genotype;
import hivMicroSim.HIV.HIVInfection;
import hivMicroSim.HIVMicroSim;
import hivMicroSim.Infection;
import hivMicroSim.Relationship;
import hivMicroSim.SeroImmunity;
import java.util.ArrayList;
import sim.portrayal.*;
import sim.engine.*;

import java.awt.*;
import sim.portrayal.simple.OvalPortrayal2D;
/**
 *
 * @author ManuelLS
 */
public abstract class Agent extends OvalPortrayal2D implements Steppable{
    private static final long serialVersionUID = 1;
    //Behavioral Factors
    public final int ID;
    protected final int faithfulness; // between 1 and 10
    protected int condomUse; //between 1 and 100
    protected final double wantLevel;
    protected double networkLevel;
    protected double lack; //lack of fulfillment in current relationships
    public boolean alive = true;
    
    //Genetic factors
    private final boolean female;
    protected final boolean ccr51, ccr52;//first and second allele ccr5 one is randomly chosen to be passed on.
    protected final double otherImmunityFactors; //Averaged between parents - value between 0 and 1
    
    //Adaptable Immunity
    private final ArrayList<SeroImmunity> seroImmunity;
    private final ArrayList<AlloImmunity> alloImmunity;
    
    //General Status
    protected boolean infected;
    protected DiseaseMatrix hiv = null;
    protected final ArrayList<Infection> infections;
    protected int age; // measured in months/ ticks. 
    protected Color col = Color.gray;
    protected Stoppable stopper;
    
    public void setStoppable(Stoppable stop){
        stopper = stop;
    }
    
    
    protected final ArrayList<Relationship> network;
    
    public Agent(int id, int faithfullness, int condomUse, double wantLevel, double lack, boolean ccr51, boolean ccr52, double immuneFactors, boolean female,
            int age){
        ID = id;
        faithfulness = faithfullness;// because the programmer can't spell... 
        this.condomUse = condomUse;
        this.wantLevel = wantLevel;
        this.lack = lack;
        this.ccr51 = ccr51;
        this.ccr52 = ccr52;
        this.otherImmunityFactors = immuneFactors;
        this.female = female;
        
        seroImmunity = new ArrayList<>();
        alloImmunity = new ArrayList<>();
        infections = new ArrayList<>();
        this.age = age;
        
        infected = false;
        network = new ArrayList<>();
        networkLevel = 0;
    }
    public Agent(int id, int faithfullness, int condomUse, double wantLevel, double lack, boolean ccr51, boolean ccr52, double immuneFactors, boolean female,
            ArrayList<SeroImmunity> sero, ArrayList<AlloImmunity> allo, int age, ArrayList<Infection> coinfections ){
        ID = id;
        faithfulness = faithfullness;// because the programmer can't spell... 
        this.condomUse = condomUse;
        this.wantLevel = wantLevel;
        this.lack = lack;
        this.ccr51 = ccr51;
        this.ccr52 = ccr52;
        this.otherImmunityFactors = immuneFactors;
        this.female = female;
        
        seroImmunity = sero;
        alloImmunity = allo;
        this.age = age;
        infections = coinfections;
        
        infected = false;
        network = new ArrayList<>();
        networkLevel = 0;
    }
    public Agent(int id, int faithfullness, int condomUse, double wantLevel, double lack, boolean ccr51, boolean ccr52, double immuneFactors, boolean female,
            ArrayList<SeroImmunity> sero, ArrayList<AlloImmunity> allo, int age, ArrayList<Infection> coinfections, DiseaseMatrix disease){
        
        ID = id;
        faithfulness = faithfullness;// because the programmer can't spell... 
        this.condomUse = condomUse;
        this.wantLevel = wantLevel;
        this.lack = lack;
        this.ccr51 = ccr51;
        this.ccr52 = ccr52;
        this.otherImmunityFactors = immuneFactors;
        this.female = female;
        
        seroImmunity = sero;
        alloImmunity = allo;
        this.age = age;
        infections = coinfections;
        
        hiv = disease;
        
        infected = true;
        network = new ArrayList<>();
        networkLevel = 0;
        switch(disease.getStage()){
            case 1:
                col = Color.red;
                break;
            case 2:
                col = Color.GREEN;
                break;
            case 3:
                col = Color.ORANGE;
                break;
            case 4:
                col = Color.black;//should not be passed a dead agent, but... *shrug*              
        }
    }
    
    
    public int getFaithfulness(){
        return faithfulness;
    }
    public int getCondomUse(){return condomUse;}
    public void setCondomUse(int a){
        if(a >= 0 && a <= 100){
            condomUse = a;
        }
    }
    public double getWantLevel(){return wantLevel;}
    public double getLack(){
        return lack;
    }
    public double adjustLack(double a){
        lack += a;
        if(lack > 10){
            lack = 10;
            return lack;
        }
        if(lack < 0){
            lack = 0;
        }
        return lack;
    }
    
    public boolean isFemale(){return female;}
    public int getCCR5Resistance(){
        int ret = 0;
        if(ccr51)
            ret += 50;
        if(ccr52)
            ret += 50;
        return ret;
    }
    public double getImmunityFactors(){return otherImmunityFactors;}
    public ArrayList<SeroImmunity> getSeroImmunity(){return seroImmunity;}
    public ArrayList<AlloImmunity> getAlloImmunity(){return alloImmunity;}
    public int addSeroImmunity(int genoType, int degree){
        SeroImmunity a;
        for (SeroImmunity seroImmunity1 : seroImmunity) {
            a = seroImmunity1;
            if(a.getGenotype() == genoType){
                return a.expose(degree);
            }
        }
        //didn't find that genotype
        a = new SeroImmunity(genoType, degree);
        seroImmunity.add(a);
        return a.getResistance();
    }
    public int addAlloImmunity(int agentID, int degree){
        AlloImmunity a;
        for (AlloImmunity alloImmunity1 : alloImmunity) {
            a = alloImmunity1;
            if(a.getAgent() == agentID){
                if(alloImmunity.size() > 10){
                    return a.addResistance(degree/2);
                }else{
                    return a.addResistance(degree);
                }
            }
        }
        //didn't find that genotype
        a = new AlloImmunity(agentID);
        alloImmunity.add(a);
        return a.getResistance();
    }
    
    public int getSeroImmunity(int genoType){
        for(SeroImmunity a : seroImmunity){
            if(a.getGenotype() == genoType)
                return a.getResistance();
        }
        return 0;
    }
    public int getAlloImmunity(int agentID){
        for(AlloImmunity a : alloImmunity){
            if(a.getAgent() == agentID)
                return a.getResistance();
        }
        return 0;
    }
    
    public boolean isInfected(){
        return infected;
    }
    public boolean infect(Genotype a){
        if(a.getCCR5Dependance()){
            if(ccr51 && ccr52) return false; // in case this was called from the main method
        }
        int type = a.getGenotype();
        if(infected){
            //make sure I don't already have that genotype -- basic error checking, should never happen. 
            ArrayList<HIVInfection> mine = hiv.getGenotypes();
            if (!mine.stream().noneMatch((inf) -> (inf.getGenotype() == type))) {
                return false;
            }
        }
///////////////////Enhancment necessary- basic formula////////////////////
        double infectivity = a.getVirulence();
        double sero = (double)getSeroImmunity(type);
        if(sero > 0){
            infectivity = (infectivity+(infectivity*(sero/100.0)))/2;
        }
        if(otherImmunityFactors >0){
            infectivity = (infectivity+(infectivity*otherImmunityFactors))/2;
        }
        if(a.getCCR5Dependance() && (ccr51 || ccr52)){
            infectivity = infectivity/2;
        }
        if(infectivity <1) infectivity = 1;
        if(infected){
            hiv.addGenoType(new HIVInfection(a, (int)infectivity));
        }else{
            infected = true;
            col = Color.red;
            hiv = new DiseaseMatrix(new HIVInfection(a, (int)infectivity));
        }
        return true;
    }
    public DiseaseMatrix getDiseaseMatrix(){
        return hiv;
    }
    public ArrayList<Infection> getInfections(){
        return infections;
    }
    public boolean addInfection(Infection a){
        if (!infections.stream().noneMatch((s) -> (s.getDisease() == a.getDisease()))) {
            return false;
        }
        infections.add(a);
        return true;
    }
    public boolean removeInfection(int a){
        for(int i = 0;i<infections.size(); i++){
            if(infections.get(i).getDisease() == a){
                infections.remove(i);
                return true;
            }
        }
        return false;
    }    
    public int getAge(){
        return age;
    }
    protected Color getColor(){
        return col;
    }
    
    public ArrayList<Relationship> getNetwork(){return network;}
    public double getNetworkLevel(){
        return networkLevel;
    }
    public boolean wantsConnection(){
        return networkLevel < wantLevel;
    }
    public int getNetworkSize(){
        return network.size();
    }
    public abstract boolean addEdge(Relationship a);
    public abstract boolean removeEdge(Relationship a);
    @Override
    public abstract void step(SimState state);
    
    @Override
    public abstract void draw(Object object, Graphics2D graphics, DrawInfo2D info);
    public boolean attemptInfection(HIVMicroSim sim, HIVInfection infection, int stage, double degree){
//////////////////////Basic infection algorithm will need refining.
        //degree could refer to alloimmunity or the effect of antiretroviral therapy.
        //CCR5 immunity
        if(infection.getCCR5Resistance()){
            int resist = getCCR5Resistance();
            if(resist == 100) return false;
            if(resist == 50) degree = degree * .5;
        }
        //SeroImmunity
        int sero = getSeroImmunity(infection.getGenotype());
        if(degree > 1){
            addSeroImmunity(infection.getGenotype(), (int)degree);
        }else{
            addSeroImmunity(infection.getGenotype(), 1);
        }
        if(sero > 10){
            degree = degree * (1/(sero*.1));
        }
        //add "other" immune factors.
        if(otherImmunityFactors > 0){
            degree = degree*(1-otherImmunityFactors);
        }
        //Infection stage and its effect on virulence. 
        if(stage == 1) degree = degree * DiseaseMatrix.ACUTEXFACTOR; //int passed by value
        else if (stage == 2) degree = degree * DiseaseMatrix.AIDSXFACTOR;
        degree = infection.getVirulence() * degree; //now this is the infectivity of this contact.
        degree = degree * sim.perInteractionLikelihood;
        double roll = sim.random.nextDouble(); // next double between 0 and 1 (noninclusive)
        return (roll<degree); //as degree increases the chance of having a double below that increases. 
    }
}

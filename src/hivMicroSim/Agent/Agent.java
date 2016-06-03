/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.Agent;
import hivMicroSim.HIV.DiseaseMatrix;
import hivMicroSim.HIVMicroSim;
import hivMicroSim.Infection;
import hivMicroSim.Relationship;
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
    protected double networkLevel;
    protected double lack; //lack of fulfillment in current relationships
    public boolean alive = true;
    protected int relationship;
    Personality pp;
    
    //Genetic factors
    public final double hivImmunity;
    
    
    //General Status
    protected boolean infected;
    protected DiseaseMatrix hiv = null;
    protected final ArrayList<Infection> infections;
    protected int age; // measured in months/ ticks. 
    protected int life; // how long this person should live without HIV/AIDS. This should probably
    //be changed by a statistician to use Gompertz or some other algorithm/source, but for now it is assigned using a
    //gaussian distribution from 0 to life expectancy + .5(life expectancy) offset to mean of life expectancly.
    //This ignores infant morality as does Gompertz (since it seems to best model rate of death after a certain age... and potentially
    //also up to a certain age.) -- again. statistician. ^.^;; 
    protected Color col = Color.gray;
    protected double width = 1;
    protected double height = 1;
    protected Stoppable stopper;//MASON
    
    //infection modes
    public static final int MODEVI = 1;
    public static final int MODEVR = 2;
    public static final int MODEAI = 3;
    public static final int MODEAR = 4;
    public static final int MODEMOTHERCHILD = 10;
    
    public void setStoppable(Stoppable stop){
        stopper = stop;
    }
    
    public boolean isMarried(){return (relationship == Relationship.MARRIAGE);}
    protected final ArrayList<Relationship> network;
    
    public Agent(int id, Personality personality, double resistance, int age, int life){
        ID = id;
        pp = personality;
        lack = 0;
        hivImmunity = resistance;
        
        infections = new ArrayList<>();
        this.age = age;
        if(age >= 216){
            width = 1.5;
            height = 1.5;
        }else{
            width = 1;
            height = 1;
        }
        this.life = life;
        
        infected = false;
        network = new ArrayList<>();
        networkLevel = 0;
    }
    
    
    public int getFaithfulness(){
        return pp.faithfulness;
    }
    public double getCondomUse(){return pp.condomUse;}
    
    public int getLifeSpan(){
        return life;
    }
    public double getWantLevel(){return pp.want;}
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
    
    public abstract boolean isFemale();
    
       
    public boolean isInfected(){
        return infected;
    }
    public boolean infect(){
        if(infected){ // already infected
            return false;
        }
///////////////////Enhancment necessary- basic formula////////////////////
        double infectivity = a.getVirulence();
        infected = true;
        col = Color.red;
        hiv = new DiseaseMatrix(new HIVInfection(a, (int)infectivity));
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
    public boolean wantsConnection(HIVMicroSim sim){
        if(age < HIVMicroSim.networkEntranceAge) return false;
        if(pp.want == Personality.wantMin) return false;
        if(networkLevel == 0)return true;
        //handle extremely low faithfulness. 
        if(pp.faithfulness == Personality.faithfulnessMin) return true;
        //if all needs are met return
        if(networkLevel >= pp.want) return false;
        if(pp.faithfulness == Personality.faithfulnessMax) return false;
        //failing all else we roll from - faithfulnessMax to positive faithfulness max. 
        int roll = sim.random.nextInt(Personality.faithfulnessMax*2)-Personality.faithfulnessMax;
        if(relationship ==  Relationship.MARRIAGE){
            return (roll+(pp.faithfulness *2)) < lack;
        }else{
            if(relationship == Relationship.RELATIONSHIP){
                return((roll+pp.faithfulness) < lack);
            }else{
                return(roll < lack);
            }
        }
    }
    public int getNetworkSize(){
        return network.size();
    }
    public void removeOneShots(SimState state);
    public boolean addEdge(Relationship a);
    public boolean removeEdge(Relationship a);
    public void calculateNetworkLevel(){
        //used to calculate the new network level after a coital frequency change.
        networkLevel = 0;
        network.stream().forEach((r) -> {
            networkLevel += r.getCoitalFrequency();
        });
    }
    @Override
    public abstract void step(SimState state);
    
    @Override
    public abstract void draw(Object object, Graphics2D graphics, DrawInfo2D info);
    public boolean attemptInfection(HIVMicroSim sim, HIVInfection infection, int stage, double degree, int mode){
//////////////////////Basic infection algorithm will need refining.

        //moving this over sero immunity because the number of virons will effect the sero immunity. 
        //Infection stage and its effect on virulence. 
        if(stage == DiseaseMatrix.StageAcute) degree = degree * DiseaseMatrix.ACUTEXFACTOR; //int passed by value
        else if (stage == DiseaseMatrix.StageAIDS) degree = degree * DiseaseMatrix.AIDSXFACTOR;
        degree = infection.getVirulence() * degree; //now this is the infectivity of this contact.
        //SeroImmunity -- starter algorithm. In sex workers this appears to be as important a factor as genomics. 
        int sero = getSeroImmunity(infection.getGenotype());
        //reducing degree prior to adding additional immunity - law of diminishing returns. 
        if(sero > 10){
            degree = degree * (1/(sero*.1));
        }
        if(degree >= 2000){
            addSeroImmunity(infection.getGenotype(), (int)(degree/1000));
        }else{
            if(degree < 100){
                addSeroImmunity(infection.getGenotype(), 0); // continued exposure, but no more additional immune response. 
            }else{
                addSeroImmunity(infection.getGenotype(), 1);
            }
        }
        
        //These factors should not have an effect on seroimmunity as they look at whether or not the viron can enter the cell, seroimmunity
        //refers to the recognition of the virus particles. 
        //CCR5 immunity
        if(infection.getCCR5Resistance()){
            if(ccr51 == Gene.CCR5D32 && ccr52 == Gene.CCR5D32) return false;
            degree = degree * getCCR5SusceptibilityFactor();
        }
        degree = degree*getHLAImmuneFactor();
        
        switch(mode){
            case MODEHETEROCOITIS:
                degree = degree * sim.perInteractionLikelihood;
                break;
            case MODEMOTHERCHILD:
                degree = degree * sim.motherToChildInfection;
                break;        
        }
        double roll = sim.random.nextDouble(); // next double between 0 and 1 (noninclusive)
        return (roll<degree); //as degree increases the chance of having a double below that increases. 
    }
    public abstract void deathFromOtherCauses(SimState state);
}

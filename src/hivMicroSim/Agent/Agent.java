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
import java.util.Iterator;
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
    protected double hinderance = 1;
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
    
    protected final ArrayList<Relationship> network;
    
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
    public boolean infect(HIVMicroSim sim){
        if(infected){ // already infected
            return false;
        }
///////////////////Enhancment necessary- basic formula////////////////////
        double latencyHazard = sim.getGaussianRangeDouble(DiseaseMatrix.wellnessHazardMinLatency, 
                0,DiseaseMatrix.wellnessHazardAvgLatency, true);
        double aidsHazard = sim.getGaussianRangeDouble(DiseaseMatrix.wellnessHazardMinAIDS, 
                0,DiseaseMatrix.wellnessHazardAvgAIDS, true);
        infected = true;
        col = Color.red;
        hiv = new DiseaseMatrix(DiseaseMatrix.normalWellness, DiseaseMatrix.normalInfectivity, 
                latencyHazard, aidsHazard);
        return true;
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
        if(age < sim.networkEntranceAge) return false;
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
    public void removeOneShots(SimState state){
        HIVMicroSim sim = (HIVMicroSim) state;
        Iterator<Relationship> itr = network.iterator();
        Relationship r;
        while(itr.hasNext()){
            r = itr.next();
            if(r.getLevel() == Relationship.ONETIME){
                sim.network.removeEdge(r);
                Agent a = r.getPartner(this);
                a.removeEdge(r);
                itr.remove();
            }
        }
    }
    public boolean addEdge(Relationship a){
        if(network.add(a)){
            networkLevel += a.getCoitalFrequency();
            if(relationship < a.getLevel())relationship = a.getLevel();
            return true;
        }
        return false;
    }
    public boolean removeEdge(Relationship a){
        if(network.remove(a)){
            networkLevel -=a.getCoitalFrequency();
            if(relationship == a.getLevel()){
                calculateRelationship();
            }
            return true;
        }
        return false;
    }
    public void calculateRelationship(){
        relationship = 0;
        for(Relationship r :network){ // can't use stream and filter since relationship is not stateless.
            if(relationship< r.getLevel()) relationship = r.getLevel();
        }
    }
    public void calculateNetworkLevel(){
        //used to calculate the new network level after a coital frequency change.
        networkLevel = 0;
        network.stream().forEach((r) -> {
            networkLevel += r.getCoitalFrequency();
        });
    }
    public void hinderanceChange(){
        //if there has been a change to the hinderance.
        pp.hinderWant(hinderance);
        int coitalLevel;
        network.stream().forEach((r) -> {
            r.setCoitalFrequency((int)Math.abs(pp.want + r.getPartner(this).getWantLevel())/2, ID);
        });
        calculateNetworkLevel();
    }
    @Override
    public void step(SimState state){
        HIVMicroSim sim = (HIVMicroSim) state;
        age++;
        if(age > life){
            death(sim, true);
            return;
        }
        if(age == 216){
            width = 1.5;
            height = 1.5;
            sim.network.addNode(this);
        }
        if(infected){
            if(hiv.progress(sim)){
                //something has changed (hinderance or stage)
                int stage = hiv.getStage();
                switch(stage){
                    case 1:
                        col = Color.red;
                        break;
                    case 2:
                        col = Color.GREEN;
                        sim.logger.insertProgression(ID, stage);
                        break;
                    case 3: 
                        col = Color.orange;
                        sim.logger.insertProgression(ID, stage);
                        break;
                    case 4: 
                        col = Color.black;
                        death(sim,false);
                }
                if(hiv.getHinderence() != hinderance)hinderanceChange();
            }
        }
        lack += pp.want -networkLevel;
    }
    
    @Override
    public abstract void draw(Object object, Graphics2D graphics, DrawInfo2D info);
    public boolean attemptInfection(HIVMicroSim sim, double degree, int mode){
        switch(mode){
            case MODEVI: // vaginal insertive - baseline
                degree *= sim.perInteractionLikelihood * sim.likelinessFactorVI;
                break;
            case MODEVR:
                degree *= sim.perInteractionLikelihood *sim.likelinessFactorVR;
                break;
            case MODEAI:
                degree *= sim.perInteractionLikelihood * sim.likelinessFactorAI;
                break;
            case MODEAR:
                degree *= sim.perInteractionLikelihood * sim.likelinessFactorAR;
                break;
            case MODEMOTHERCHILD:
                degree *= sim.motherToChildInfection;
                break;        
        }
        double roll = sim.random.nextDouble(); // next double between 0 and 1 (noninclusive)
        return (roll<degree); //as degree increases the chance of having a double below that increases. 
    }
    public void death(HIVMicroSim sim, boolean natural){
        sim.logger.insertDeath(ID, natural, infected);
        //remove all relationships.
        for(Relationship r : network){//start with the last element and work down to empty out the list
            r.getPartner(this).removeEdge(r);
            sim.network.removeEdge(r);
        }
        sim.network.removeNode(this);
        networkLevel = 0;
        network.clear();
        alive = false;
        stopper.stop();
    }
}

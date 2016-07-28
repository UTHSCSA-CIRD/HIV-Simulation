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
    public boolean alive = true;
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
    protected int attemptsToInfect = 0;
    protected final ArrayList<Relationship> network;
    
    
    //infection modes
    public static final int MODEVI = 1;
    public static final int MODEVR = 2;
    public static final int MODEAI = 3;
    public static final int MODEAR = 4;
    
    public void setStoppable(Stoppable stop){
        stopper = stop;
    }
    
    public Agent(int id, Personality personality, double resistance, int age, int life){
        ID = id;
        pp = personality;
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
    
    public abstract boolean acceptsGender(boolean isFemale);
    public int getCommitment(){
        return pp.commitment;
    }
    public int getMonogamous(){
        return pp.monogamous;
    }
    public double getCondomUse(){
        return pp.condomUse;
    }
    
    public int getLifeSpan(){
        return life;
    }
    public double getLibido(){
        return pp.libido;
    }
    public int getAttemptsToInfect(){return attemptsToInfect;}
    public double getHIVImmunity() {return hivImmunity;}
    public double getHinderance(){ return hinderance;}
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
    public boolean attemptInfection(HIVMicroSim sim, double degree){
        attemptsToInfect++;
        degree *= sim.perInteractionLikelihood * hivImmunity;
        double roll = sim.random.nextDouble(); // next double between 0 and 1 (noninclusive)
        if(roll<degree){
            infect(sim);
            return true;
        }
        return false; //as degree increases the chance of having a double below that increases. 
        
    }
    public boolean attemptCoitalInfection(HIVMicroSim sim, int frequency, double degree, int mode){
        switch(mode){
            case Agent.MODEAI:
                degree *= sim.likelinessFactorAI;
                break;
            case Agent.MODEAR:
                degree *= sim.likelinessFactorAR;
                break;
            case Agent.MODEVR:
                degree *= sim.likelinessFactorVR;
                break;
            case Agent.MODEVI:
                degree *= sim.likelinessFactorVI;
                break;
        }
        for(int i = 0; i< frequency; i++){
            if(attemptInfection(sim, degree)) return true;
        }
        return false;
    }
    public double getInfectivity(){
        if(hiv == null) return 0; //shouldn't be called if it is null, but this stops potential null exception pointer errors
        return hiv.getInfectivity();
    }
    public ArrayList<Infection> getInfections(){
        return infections;
    }
    public boolean addInfection(Infection a){
        /**
         * This is for adding NON-HIV infections.
         * @param a The infection to add.
         * @return Returns true for added the infection, false for could not add the infection. (e.g. already has infection a)
         */
        if (!infections.stream().noneMatch((s) -> (s.getDisease() == a.getDisease()))) {
            return false;
        }
        infections.add(a);
        return true;
    }
    public boolean removeInfection(int a){
        /**
         * This is for removing NON-HIV infections.
         * @param a The infection to remove.
         * @return Returns true for removed the infection, false for could not remove the infection. (e.g. does not have infection a to remove)
         */
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
    public int getWellness(){
        if(hiv == null) return 1000;
        return(hiv.getWellness());
    }
    public int getInfectionDuration(){
        if(hiv == null) return -1;
        return(hiv.getDuration());
    }
    public double getHIVHazard(){
        if(hiv == null) return 0;
        if(hiv.getStage() == DiseaseMatrix.StageAcute) return 0;
        if(hiv.getStage() == DiseaseMatrix.StageLatency) return hiv.getWellnessHazardLatency();
        return hiv.getWellnessHazardAIDS();
    }
    public ArrayList<Relationship> getNetwork(){return network;}
    public double getNetworkLevel(){
        return networkLevel;
    }
    public boolean wantsConnection(HIVMicroSim sim){
        if(networkLevel == 0)return true;
        //are their current needs met?
        if(networkLevel >= pp.libido) return false;
        //handle those at the extreme of polygamy. 
        if(pp.monogamous == Personality.monogamousMin) return true;
        //extremes of monogamy- they will not be with more than 1 person at the same time.
        if(pp.monogamous == Personality.monogamousMax) return false;
        //failing all else we roll from - monogamousMax to positive monogamousMax. 
        int roll = sim.random.nextInt(Personality.monogamousMax*2)-Personality.monogamousMax;
        return (roll+(pp.monogamous)) < networkLevel - pp.libido;
    }
    public int getNetworkSize(){
        return network.size();
    }
    public boolean hasAsPartner(Agent a){
        int id = a.ID;
        return network.stream().anyMatch((network1) -> (network1.getPartner(this).ID == id));
    }
    public boolean addEdge(Relationship a){
        if(network.add(a)){
            networkLevel += a.getCoitalFrequency();
            return true;
        }
        return false;
    }
    public boolean removeEdge(Relationship a){
        if(network.remove(a)){
            networkLevel -=a.getCoitalFrequency();
            return true;
        }
        return false;
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
        pp.hinderLibido(hinderance);
        network.stream().forEach((r) -> {
            r.setCoitalFrequency((int)Math.abs(pp.libido + r.getPartner(this).getLibido())/2, ID);
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
    }
    @Override
    public abstract void draw(Object object, Graphics2D graphics, DrawInfo2D info);
    
    public void death(HIVMicroSim sim, boolean natural){
        sim.logger.insertDeath(ID, natural, infected);
        //remove all relationships.
        for(Relationship r : network){//start with the last element and work down to empty out the list
            r.getPartner(this).removeEdge(r);
            sim.network.removeEdge(r);
        }
        sim.network.removeNode(this);
        if(natural) sim.agents.remove(this);
        networkLevel = 0;
        network.clear();
        alive = false;
        stopper.stop();
    }
}

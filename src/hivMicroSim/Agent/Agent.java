/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.Agent;
import hivMicroSim.Disease.DiseaseMatrix;
import hivMicroSim.HIVMicroSim;
import hivMicroSim.Infection;
import hivMicroSim.CoitalInteraction;
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
    public Personality pp;
    
    //Genetic factors
    public final double hivImmunity;
    
    //General Status
    protected boolean infected;
    public DiseaseMatrix hiv = null;
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
    protected double lack = 0;
    protected final ArrayList<CoitalInteraction> network;
    
    
    //infection modes
    public static final int MODEVI = 1;
    public static final int MODEVR = 2;
    public static final int MODEAI = 3;
    public static final int MODEAR = 4;
    
    //Testing
    private int lastTest; //records the step of the last test.
    public void adjustLack (double a){
        lack += a;
        if(lack < 0) lack = 0;
        else if (lack > Personality.libidoMax) lack = Personality.libidoMax;
    }
    public double getLack(){
        return lack;
    }
    public int getLastTest(){
        return lastTest;
    }
    public double getTestingLikelihood(){
        return pp.testingLikelihood;
    }
    
    public void setStoppable(Stoppable stop){
        stopper = stop;
    }
    /**
     * Create a new agent.
     * @param id The ID of the agent. This should be a unique identification number.
     * @param personality Personality matrix of the agent.
     * @param resistance The agent's resistance/susceptibility to HIV infection. 1 means no resistance or susceptibility.
     * @param age The agent's starting age.
     * @param life The agent's age at death.
     * @param gene The agent's genetic information.
     */
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
    /**
     * Abstract method as it is only important or males.
     * @param isFemale The male or female status of the other agent.
     * @return Whether or not this agent accepts the other agent's gender.
     */
    public abstract boolean acceptsGender(boolean isFemale);
    //** NOTE THAT THE FOLLOWING PERSONALITY GET METHODS ARE FOR THE MASON GUI INTERFACE
    /**
     * Returns the current coitalLongevity level of the agent.
     * @return How likely the agent is to remain with another agent in a long term relationship.
     */
    public int getCoitalLongevity(){
        return pp.coitalLongevity;
    }
    /**
     * Returns the current monogamy rating of the agent. 
     * @return How likely the agent is to prefer a single partner. 
     */
    public int getMonogamous(){
        return pp.monogamous;
    }
    /**
     * Returns the current condom usage of the agent. 
     * @return How likely the agent is to use a condom. 
     */
    public double getCondomUse(){
        return pp.condomUse;
    }
    /**
     * How many times a month the agent current wants to have some form of intercourse. 
     * @return The libido of the agent.
     */
    public double getLibido(){
        return pp.libido;
    }
    /**
     * Get how long the agent will live.
     * @return The age at which the agent will pass from non-AIDs related death.
     */
    public double getLifeSpan(){
        return life/HIVMicroSim.ticksPerYear;
    }
    
    public int getAttemptsToInfect(){return attemptsToInfect;}
    public double getHIVImmunity() {return hivImmunity;}
    public double getHindrance(){ 
        if(hiv == null) return 1;
        return hiv.getHindrance();
    }
    public abstract boolean isFemale();
     
    public boolean isInfected(){
        return infected;
    }
    public boolean isTreated(){
        if(hiv == null) return false;
        return hiv.isTreated();
    }
    /*Removed
    public boolean isSuppressed(){
        if(hiv == null) return false;
        return hiv.isSuppressed();
    }
    */
    public boolean infect(HIVMicroSim sim, int clusterID){
        if(infected){ // already infected
            return false;
        }
///////////////////Enhancment necessary- basic formula////////////////////
        
        infected = true;
        col = Color.red;
        hiv = new DiseaseMatrix(DiseaseMatrix.normalInfectivity, this, clusterID);
        return true;
    }
    public boolean infect(HIVMicroSim sim, double wellness, int clusterID){
        if(infected){ // already infected
            return false;
        }
///////////////////Enhancment necessary- basic formula////////////////////
        
        infected = true;
        col = Color.red;
        hiv = new DiseaseMatrix(DiseaseMatrix.normalInfectivity, wellness, this, clusterID);
        hindranceChange();
        return true;
    }
    public boolean attemptInfection(HIVMicroSim sim, double degree, int clusterID){
        attemptsToInfect++;
        degree *= sim.perInteractionLikelihood * hivImmunity;
        double roll = sim.random.nextDouble(); // next double between 0 and 1 (noninclusive)
        if(roll<degree){
            infect(sim, clusterID);
            return true;
        }
        return false; //as degree increases the chance of having a double below that increases. 
        
    }
    public boolean attemptCoitalInfection(HIVMicroSim sim, int frequency, double degree, int mode, int clusterID){
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
            if(attemptInfection(sim, degree, clusterID)) return true;
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
    public double getAge(){
        return age/HIVMicroSim.ticksPerYear;
    }
    public int getTickAge(){ //age in ticks.
        return age;
    }
    protected Color getColor(){
        return col;
    }
    public double getWellness(){
        if(hiv == null) return 1000;
        return(hiv.getWellness());
    }
    public int getInfectionDuration(){
        if(hiv == null) return -1;
        return(hiv.getDuration());
    }
    
    public ArrayList<CoitalInteraction> getNetwork(){return network;}
    public double getNetworkLevel(){
        return networkLevel;
    }
    
    public int getNetworkSize(){
        return network.size();
    }
    public boolean hasAsPartner(Agent a){
        int id = a.ID;
        return network.stream().anyMatch((network1) -> (network1.getPartner(this).ID == id));
    }
    public boolean addEdge(CoitalInteraction a){
        if(network.add(a)){
            networkLevel += a.getCoitalFrequency();
            return true;
        }
        return false;
    }
    public boolean removeEdge(CoitalInteraction a){
        if(network.remove(a)){
            if(network.isEmpty()) networkLevel = 0; //because doubles might not revert this completely to 0
            else networkLevel -=a.getCoitalFrequency();
            
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
    /**
     * 
     * Processes the change of a wellness or other hindrance change on all of 
     * the agent's relationships and recalculates the network level.
     */
    public void hindranceChange(){
        //if there has been a change to the hindrance.
        pp.hinderLibido(hiv.getHindrance());
        network.stream().forEach((r) -> {
            r.setCoitalFrequency((int)Math.abs(pp.libido + r.getPartner(this).getLibido())/2, ID);
        });
        calculateNetworkLevel();
    }
    /**
     * Will return the base libido prior to any changes caused by disease or other influences on their personality.
     * @return base libido
     */
    public double getBaseLibido(){
        return pp.baseLibido;
    }
    /**
     * Does the agent know that they are infected with HIV? 
     * @return Will return true if known, false if not infected or unknown.
     */
    public boolean isKnown(){
        
        if(hiv != null) return hiv.isKnown();
        return false;
    }
    @Override
    public void step(SimState state){
        HIVMicroSim sim = (HIVMicroSim) state;
        age++;
        if(age > life){
            if(alive){
                death(sim, true);
            }else{
                //for agents who died of HIV when RemoveTheDead is false. They 
                //remain active for population growth and to record the effects of 
                //the disease, but they should be removed when they would normally die.
                sim.agents.remove(this);
                stopper.stop();
            }
            return;
        }
        if(!alive) return;
        if(age == sim.networkEntranceAge){
            width = 1.5;
            height = 1.5;
            if(isFemale()){ //males handle this in their own step function
                sim.networkMF.addNode(this);
            }
        }
        if(age >= sim.networkEntranceAge){
            adjustLack(pp.libido-networkLevel);
        }
        double roll;
        if(infected){
            if(hiv.isKnown()){
                roll = sim.random.nextDouble();
                if(hiv.isTreated()){
                    if(roll < sim.lossToFollowup) hiv.setTreatment(false);
                        //suppressed, roll to see if we maintain suppression
                     
                }else{
                 //we are not yet treating, test to see if we treat
                    if(sim.treatAIDS && hiv.getStage() == DiseaseMatrix.StageAIDS) hiv.treat();
                    if(roll < sim.treatmentLikelihood) hiv.treat();
                }
            }else{
                //it's not known, are we testing? 
                if(pp.testingLikelihood > 0){
                    //we are testing
                    double test = sim.random.nextDouble();
                    if(test < pp.testingLikelihood){
                        lastTest = (int)sim.schedule.getSteps();
                        if(hiv.getDuration() > sim.testTicks){//is it within the detectable range? 
                            test = sim.random.nextDouble();
                            if(test < sim.testAccuracy)hiv.discover(); //that's a bad day. 
                            discoverHIV(sim);
                        }
                    }
                }
            }
            int change = hiv.progress(sim);
            int stage = hiv.getStage();
            switch(stage){
                case 1:
                    if(hiv.isKnown()){
                        col = new Color(153,0,0);
                    }else{
                        col = Color.red;
                    }
                    break;
                case 2:
                    if(hiv.isKnown()){
                        if(hiv.isTreated()){
                            if(hiv.isSuppressed()){
                                col = new Color(0,255,127);
                            }else{
                                col = new Color(127,255,0);
                            }
                        }else{
                            col = new Color(50,205,50);
                        }
                    }else{
                        col = new Color(34,139,34);
                    }
                    if(change < 0) sim.logger.insertProgression(ID, stage, 2);
                    break;
                case 3: 
                    if(hiv.isKnown()){
                        if(hiv.isTreated()){
                            if(hiv.isSuppressed()){
                                col = new Color(255,255,0);
                            }else{
                                col = new Color(255,215,0);
                            }
                        }else{
                            col = new Color(255,165,0);
                        }
                    }else{
                        col = new Color(184,134,11);
                    }
                    if(change < 0) sim.logger.insertProgression(ID, stage, hiv.getDuration()-2);
                    break;
                case 4: 
                    //should only get to this once... 
                    col = Color.black;
                    death(sim,false);
                    if(networkLevel != 0){// DEBUG
                        System.err.println("Fail.... ");
                    }
                    break;
            }
            if(change%2 == 1){
                hindranceChange();
            }
        }
    }
    @Override
    public abstract void draw(Object object, Graphics2D graphics, DrawInfo2D info);
    
    public void death(HIVMicroSim sim, boolean natural){
        int ticks;
        if(!natural){
            ticks = hiv.getAIDsTick();
        }else{
            ticks = -1;
        }
        sim.logger.insertDeath(ID, natural, infected, ticks);
        //remove all relationships.
        for(CoitalInteraction r : network){//start with the last element and work down to empty out the list
            r.getPartner(this).removeEdge(r);
            if(r.network == CoitalInteraction.networkMF){
                sim.networkMF.removeEdge(r);
            }else{//later this might become a switch statement, but there are only 2 networks. At some point there might
                //be relationship subclasses that remove themselves to simplify this part.
                sim.networkM.removeEdge(r);
            }
        }
        if(isFemale()){//males will handle this on their own as removing a node from the network is a cpu/access intensive process
            sim.networkMF.removeNode(this);
            networkLevel = 0;
            network.clear();
        }
        if(natural || sim.removeTheDead){ 
            sim.agents.remove(this);
            stopper.stop();
        }
        alive = false;
    }
    public void discoverHIV(HIVMicroSim sim){
        //breaking this out so that it's easier to find and adjust later. -Thank you self
        int mono, longevity;
        double libido,condom, seroRoll, seroKnown;
        mono = sim.knownHIVMonogamous;
        longevity = sim.knownHIVCoitalLongevity;
        libido = sim.knownHIVLibido;
        condom = sim.knownHIVCondom;
        seroRoll = sim.random.nextDouble();
        seroKnown = sim.knownHIVSeroSort;
        //determine if the values are stratified
        if(sim.knownHIVStratify){
            //need to set the std because making it (Max -(-Max))/6 makes the stratification too large. Should be within
            //the allowable range. i.e. (Max - min)/6
               //int min, int max, boolean reroll, boolean inclusive, double mean, double std, int offset
            mono = sim.nextGaussianRange(-Personality.monogamousMax, 
                    Personality.monogamousMax, true, true, mono,
                    (Personality.monogamousMax-Personality.monogamousMin)/6,0);
            longevity = sim.nextGaussianRange(-Personality.coitalLongevityMax, 
                    Personality.coitalLongevityMax, true, true, longevity,
                    (Personality.coitalLongevityMax-Personality.coitalLongevityMin)/6, 0);
              //double min, double max, boolean reroll, double mean, double std, double offset
            libido = sim.nextGaussianRangeDouble(-Personality.libidoMax, 
                    Personality.libidoMax, true, libido,
                    (Personality.libidoMax-Personality.libidoMin)/6, 0 );
            condom = sim.nextGaussianRangeDouble(-Personality.condomMax, 
                    Personality.condomMax, true, condom,
                    (Personality.condomMax-Personality.condomMin)/6, 0);
            seroKnown = sim.nextGaussianRangeDouble(Personality.knownSeroSortMin, Personality.knownSeroSortMax, sim.knownHIVSeroSort, false);
        }
        //if the roll is less than the population level, they serosort
        if(seroRoll < seroKnown) pp.seroSort();
        //change personality. int mono, int commit, double lib, double condom, 
              //double testing (not currently using this, but it's there for the future)
        pp.changePersonality(mono, longevity, libido, condom, 0);
        //report known status; 
        sim.logger.insertDiscovery(ID, hiv.getStage(), hiv.getDuration());
    }
    public boolean wantsConnection(HIVMicroSim sim){
        return pp.wantsConnection(sim);
    }
    public boolean wantsConnection(HIVMicroSim sim, Agent a){
        return pp.wantsConnection(sim, a);
    }
            
}

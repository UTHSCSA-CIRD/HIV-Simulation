/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim;

import hivMicroSim.Agent.Agent;
import hivMicroSim.Agent.Male;
import hivMicroSim.Agent.Personality;
import sim.engine.*;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import java.io.IOException;
import sim.util.Interval;
/**
 *
 * @author ManuelLS
 */

/* Example code from MASON to be deleted once integrated into the model:
    public double getRandomActionProbability() { return randomActionProbability; }
    public void setRandomActionProbability(double val) {if (val >= 0 && val <= 1.0) randomActionProbability = val; }
    public Object domRandomActionProbability() { return new Interval(0.0, 1.0); }
*/


public class HIVMicroSim extends SimState{
    public SparseGrid2D agents;
    public ListNetwork networkMF; // male female sexual network
    public ListNetwork networkM; // male to male sexual network
    public boolean MSMnetwork = true;
    public int numAgents = 1000;
    //records the current growth of new agents. Is a double because growth is by month and new
    //agents might only appear ever x months.
    public double agentGrowth = 0; //internal variable
    //Population growth per month
    public double populationGrowth = .0005;
    public boolean removeTheDead = false;
    
    public int newAgents = 0;
    public int numInfect = 10; //initial number of infected agents. 
    public int currentID = 0;
    public int gridWidth = 100;
    public int gridHeight = 100;
    public int networkEntranceAge = 936;//18 years
    
    //global agent descriptors- gaussian distribution between 1 and 10 with these as the means
    public double percentCondomUse = .5; //0-1 inclusive
    public Object domPercentCondomUse() { return new Interval(0.0, 1.0); }
    public int maleMonogamous = 5;      //0-10
    public Object domMaleMonogamous() { return new Interval(0, 10); }
    public int femaleMonogamous = 5;    
    public Object domFemaleMonogamous() { return new Interval(0, 10); }
    public int maleCoitalLongevity = 5;   
    public Object domMaleCoitalLongevity() { return new Interval(0, 10); }
    public int femaleCoitalLongevity = 5; 
    public Object domFemaleCoitalLongevity() { return new Interval(0, 10); }
    public int maleLibido = 2;
    public Object domMaleLibido() { return new Interval(0, 7); }
    public int femaleLibido= 2; 
    public Object domFemaleLibido() { return new Interval(0, 7); }
    public boolean allowExtremes = true;
    
    //natural resistance -- replacing genes until further science is available 
    public double resistanceMax = 2;  //maximum resistance
    public double resistanceMin = 0;  //minimum resistance
    public double resistanceAvg = 1;  //average resistance.
    
    //population percentages 
    public double percentMsMW = .02;   //0-1 (combined with MsM cannot exceed 1)
    public double percentMsM = .02;   // 0-1(combined with MsMW cannot exceed 1)
    public double percentCircum = .5; //percent circumcised
    public Object domPercentCircum() { return new Interval(0.0, 1.0); }
    
    //testing likelihoods and range between tests
    public double testingLikelihood = .00481; //The likelihood of the average agent to get tested in any given tick. 
                                        //Assuming 50% of the population gets tested every 2 years
                                            //1/24/2
    public double testAccuracy = 1; //The likelihood of testing positive when positive
        //Currently disabled by setting it to 1 (all tests are 
    public double testTicks = 4; //number of ticks afer which the agent may test positive. 
   //Behavioral changes due to known status.
    public boolean knownHIVStratify = false;
    public double knownHIVCondom = .25; //increases the likelihood of using condoms
    public Object domKnownHIVCondom() { return new Interval(-1.0, 1.0); }
    public double knownHIVLibido = 0; 
    public Object domKnownHIVLibido() { return new Interval(-7.0, 7.0); }
    public int knownHIVMonogamous = 0; 
    public Object domKnownHIVMonogamous() { return new Interval(-10, 10); }
    public int knownHIVCoitalLongevity = 0; 
    public Object domKnownHIVCoitalLongevity() { return new Interval(-10, 10); }
    
    //Treatment
    public double treatmentLikelihood = .00481;
    public boolean treatAIDS = true;
    public double lossToFollowup = .00481; 
    
   
    /* Likeliness factors
    http://www.cdc.gov/hiv/risk/estimates/riskbehaviors.html
    '~Risk per 10,000 exposures
    Needle Sharing (injection drug use)- 63
    Percutaneous needle-stick - 23
    receptive anal - 138
    insertive anal - 11
    receptive vaginal - 8
    insertive vaginal - 4~'
    http://www.who.int/hiv/topics/mtct/en/
    "In the absence of any intervention, transmission rates range from 15% to 45%. 
    This rate can be reduced to below 5% with effective interventions "
    */
    //mode
    public double likelinessFactorVR = 8;
    public double likelinessFactorVI = 4;
    public double likelinessFactorAR = 138;
    public double likelinessFactorAI = 11;
    //preventative methods
    public double likelinessFactorCircumcision = .49;
    //interaction type (mother to child vs everything else.)
    public double perInteractionLikelihood = 0.0001;
    
    //Population statistics
    public static final int ticksPerYear = 52;
    public int averageAge = 1300; //in ticks
    public int averageLifeSpan = 3380;
    public HIVLogger logger;
    public int logLevel = HIVLogger.LOG_DEATH_NATURAL; 
    public DebugLogger debugLog;
    private final String simDebugFile = "simDebug.txt";
    private final int simDebugLevel = DebugLogger.LOG_ALL;
    public int initializationOnTick = 52; // the tick on which we will initiate infection. 
    public boolean initializeHighRiskPop = true;
    public boolean stratifyInitInfected = true;
    
    public int getStartInfected(){
        return numInfect;
    }
    public void setStartInfected(int a){
        if(a > 0 && a < numAgents){
            numInfect = a;
        }
    }
    public double getPopulationGrowth(){
        return populationGrowth;
    }
    public void setPopulationGrowth(double a){
        if(a >= 0){
            populationGrowth = a;
        }
    }
    public boolean isRemoveTheDead(){return removeTheDead;}
    public void setRemoveTheDead(boolean a) {removeTheDead = a;}
    public int getInitializationTick(){
        return initializationOnTick;
    }
    public void setInitializationTick(int a){
        if(a >0){
            initializationOnTick = a;
        }
    }
    public boolean isStratifiedInitInfected(){return stratifyInitInfected;}
    public void setStratifiedInitInfected(boolean a){stratifyInitInfected = a;}
    public boolean isInitializeHighRiskPop(){return initializeHighRiskPop;}
    public void setInitializeHighRiskPop(boolean a){initializeHighRiskPop = a;}
    
    public int getAverageAge(){
        return (int)averageAge/ticksPerYear;
    }
    public int getAverageLifeSpan(){
        return (int)averageLifeSpan/ticksPerYear;
    }
    public void setAverageAge(int a){
        a *= ticksPerYear;
        if(a > 1 && a < averageLifeSpan){
            averageAge = a;
        }
    }
    public void setAverageLifeSpan(int a){
        a *= ticksPerYear;
        if(a > 20 && a> averageAge){
            averageLifeSpan = a;
        }
    }
    public double getPercentMsMW(){return percentMsMW;}
    public void setPercentMsMW(double a){
        if(a + percentMsM <=1){
            percentMsMW = a;
        }
    }
    public int getNetworkEntranceAge(){return networkEntranceAge/ticksPerYear;}
    public void setNetworkEntranceAge(int a){
        a *= ticksPerYear;
        if(a >0){
            networkEntranceAge = a;
        }
    }
    public boolean getMSMNewtork(){
        return MSMnetwork;
    }
    public void setMSMNetwork(boolean a){
        MSMnetwork = a;
    }
    public double getPercentMsM(){return percentMsM;}
    public void setPercentMsM(double a){
        if(a + percentMsMW <=1){
            percentMsM = a;
        }
    }
    
    public double getPercentCircum(){return percentCircum;}
    public void setPercentCircum(double a){
        if(percentCircum <=1 && percentCircum >=0){
            percentCircum = a;
        }
    }
    public double getTestingLikelihood(){
        return testingLikelihood;
    }
    public void setTestingLikelihood(double a){
        if(a >= Personality.testingMin && a <= Personality.testingMax) testingLikelihood = a;
    }
//    public double treatmentLikelihood = .00481;
//    public boolean treatAIDS = true;
//    public double viralSuppressionLikelihood = .26; // rolling below this starts viral suppression
//    public double viralFailureLikelihood = .15; //rolling below this loses viral suppression
    public double getTreatmentLikelihood() {return treatmentLikelihood;}
    public boolean isTreatAIDS(){return treatAIDS;}
    public double getLossToFollowup(){return lossToFollowup;}
    
    public void setTreatmentLikelihood(double a){
        if(a >= 0 && a <= 1) treatmentLikelihood = a; 
    }
    public void setTreatAIDS(boolean a){
        treatAIDS = a;
    }
    public void setLossToFollowup(double a){
        if(a >= 0 && a <= 1) lossToFollowup = a;
    }
    
    //Beans for known HIV status behavioral changes
    public boolean isKnownHIVStratify() {return knownHIVStratify;}
    public void setKnownHIVStratify(boolean a) {knownHIVStratify = a;}
    public double getKnownHIVCondom(){return knownHIVCondom;}
    public void setKnownHIVCondom(double a) { 
        if(a < 1 && a > -1){
            knownHIVCondom = a;
        }
    }
    public double getKnownHIVLibido(){return knownHIVLibido;} 
    public void setKnownHIVLibido(double a){ 
        if(a <7 && a > -7){
             knownHIVLibido = a;
        }
    }
    public int getKnownHIVMonogamous() {return knownHIVMonogamous;} 
    public void setKnownHIVMonogamous(int a) { 
        if(a>-10 &&a<10){
            knownHIVMonogamous = a;
        }
    }
    public int getKnownHIVCoitalLongevity(){return knownHIVCoitalLongevity; }
    public void setKnownHIVCoitalLongevity(int a) {
        if(a>-10 && a<10){
            knownHIVCoitalLongevity = a;
        } 
    }
    
    /**
     * This selects a random gaussian number and converts it to the range (min, max) and average (avg) 
     * indicated by the calling method. Min and max will be set 3 standard deviations from the mean and then
     * offset to the avg. 
     * @param min - Minimum value allowable.
     * @param max - Maximum value allowable.
     * @param avg - Average number- this allows for an offset greater than or less than mean.
     * @param reroll - Whether or not to re-roll values that are outside the min and max range. W
     * When false the value is truncated to the min/max, when true the value is re-rolled until it falls
     * within the acceptable range.
     * @param inclusive - If the min and max are inclusive values. e.g. min 0 max 10 true makes 0 and 10 valid
     * numbers. False makes 0 and 10 invalid numbers.
     * @return - return the next random gaussian within the specifications.
     */
    public int nextGaussianRange(int min, int max, int avg, boolean reroll, boolean inclusive){
        if(min == max) return min;
        if(min > max){
            int tmp = max;
            max = min;
            min = tmp;
        }
        double mean = (max+min)/2;
        if(avg > max || avg < min){
            System.err.println("Average is out of range. Setting average to the mean.");
            //TODO: Remove before production release
            //only for debug: java.lang.Thread.dumpStack(); //debugging command because this shouldn't happen.
            avg = (int)mean;
        }
        int offset = avg - (int)mean;
        double std = (max-mean)/3; //most (95%) numbers will be within 3 standard devaitions.
        return nextGaussianRange(min, max, reroll, inclusive, mean, std, offset);
    }
    public int nextGaussianRange(int min, int max, boolean reroll, boolean inclusive, double mean, double std, int offset){
        double rand;
        if(reroll){
            if(inclusive){
                do{
                    rand= (random.nextGaussian()*std)+mean+ offset;
                }while(rand <= min || rand >= max);
            }else{
                do{
                    rand= (random.nextGaussian()*std)+mean+ offset;
                }while(rand < min || rand > max);
            }
        }else{
            rand= (random.nextGaussian()*std)+mean+ offset;
            if(inclusive){
                if(rand < min) rand = min;
                if(rand > max) rand = max;
            }else{
                if(rand <= min) rand = min + 1;
                if(rand >= max) rand = max - 1; 
            }
        }
        return (int)rand;
    }
    /**
     * This selects a random gaussian number and converts it to the range (min, max) and average (avg) 
     * indicated by the calling method. Min and max will be set 3 standard deviations from the mean and then
     * offset to the avg. 
     * @param min - Minimum value allowable.
     * @param max - Maximum value allowable.
     * @param avg - Average number- this allows for an offset greater than or less than mean.
     * @param reroll - Whether or not to re-roll values that are outside the min and max range. W
     * When false the value is truncated to the min/max, when true the value is re-rolled until it falls
     * within the acceptable range. Inclusive is not added to double because the values less than or greater than the min and max
     * are infinitesimally small and there is no way to create a standard fall back that is not inclusive. 
     * (int is +/- 1. should double be 0.1? 0.0000000000000001? 
     * @return - return the next random gaussian double within the specifications.
     */
    public double nextGaussianRangeDouble(double min, double max, double avg, boolean reroll){
        if(min == max) return min;
        if(min > max){
            double tmp = max;
            max = min;
            min = tmp;
        }         
        double mean = (max+min)/2;
        if(avg > max || avg < min){
            System.err.println("Average is out of range. Setting average to the mean.");
            //TODO: Remove before production release
            //only for debug: java.lang.Thread.dumpStack();//debugging command because this shouldn't happen.
            avg = mean;
        }
        double offset = (avg - mean);
        double std = (max-mean)/3; //most (95%) numbers will be within 3 standard devaitions.
        return (nextGaussianRangeDouble(min, max, reroll, mean, std, offset));
    }
    public double nextGaussianRangeDouble(double min, double max, boolean reroll, double mean, double std, double offset){
        double rand;
        if(reroll){
            do{
                rand= (random.nextGaussian()*std)+ mean + offset;
            }while(rand <= min || rand >= max);
        }else{
            rand= (random.nextGaussian()*std)+mean+ offset;
            if(rand < min) rand = min;
            if(rand > max) rand = max;
        }
        return rand;
    }
    
    public boolean getAllowExtremes(){return allowExtremes;}
    public void setAllowExtremes(boolean allow){allowExtremes = allow;}
    public int getMaleMonogamous(){return maleMonogamous;}
    public int getFemaleMonogamous(){return femaleMonogamous;}
    public int getMaleCoitalLongevity(){return maleCoitalLongevity;}
    public int getFemaleCoitalLongevity(){return femaleCoitalLongevity;}
    public int getMaleLibido(){return maleLibido;}
    public int getFemaleLibido(){return femaleLibido;}
    public void setMaleMonogamous(int a){
        if(a >=Personality.monogamousMin && a <= Personality.monogamousMax){
            maleMonogamous = a;
        }
    }
    public void setFemaleMonogamous(int a){
        if(a >=Personality.monogamousMin && a <= Personality.monogamousMax){
            femaleMonogamous = a;
        }
    }
    public void setMaleCoitalLongevity(int a){
        if(a >=Personality.coitalLongevityMin && a <= Personality.coitalLongevityMax){
            maleCoitalLongevity = a;
        }
    }
    public void setFemaleCoitalLongevity(int a){
        if(a >=Personality.coitalLongevityMin && a <= Personality.coitalLongevityMax){
            femaleCoitalLongevity = a;
        }
    }
    public void setMaleLibido(int a){
        if(a >=Personality.libidoMin && a <= Personality.libidoMax){
            maleLibido = a;
        }
    }
    public void setFemaleLibido(int a){
        if(a >=Personality.libidoMin && a <= Personality.libidoMax){
            femaleLibido = a;
        }
    }
    public double getModelCondomUse(){
        return percentCondomUse;
    }
    public void setModelCondomUse(double a){
        if(a >= Personality.condomMin && a<=Personality.condomMax){
            percentCondomUse = a;
        }
    }
    
    public int getNumAgents(){
        return numAgents;
    }
    public void setNumAgents(int a){
        if(a > 0){
            numAgents  = a;
        }
    }
    public int getGridWidth(){return gridWidth;}
    public int getGridHeight(){return gridHeight;}
    public void setGridWidth(int a){
        if(a > 0) gridWidth = a;
    }
    public void setGridHeight(int a){
        if(a>0) gridHeight = a;
    }
    
    public HIVMicroSim(long seed){
        super(seed);
        agents = new SparseGrid2D(100, 100);
        networkMF = new ListNetwork();
        networkM = new ListNetwork();
        logger = new HIVLogger();
    }
    
    @Override
    public void start(){
        super.start();
        currentID = 0;
        logger = new HIVLogger();
        try{
            debugLog = new DebugLogger(simDebugLevel, simDebugFile);
            schedule.scheduleRepeating(Schedule.EPOCH, 0, debugLog);
        }catch(IOException e){
            System.err.println("Exception when creating logger!! " + e.getLocalizedMessage());
            debugLog = new DebugLogger();
        }
        agents = new SparseGrid2D(gridWidth, gridHeight);
        networkMF = new ListNetwork(false);
        networkM = new ListNetwork(false);
        /* Order
            1 Agent Processes Self
            2 Relationships Processed
                2.1 Relationships Dissolved
                2.2 New Relationships Created 
        */
        
        //generate starter agents, add them to the network and object location.
        Stoppable stopper;
        Agent agent;
        for(int i = 0; i < numAgents; i++){
            agent = Generator.generateAgent(this, true);
            agents.setObjectLocation(agent,random.nextInt(gridWidth), random.nextInt(gridHeight));
            if(agent.getTickAge() >= networkEntranceAge) {
                if(agent.isFemale()){
                    networkMF.addNode(agent);
                }else{
                    Male m = (Male)agent;
                    //if they have sex with other men and the msm network is active they will be added to that network
                    if(m.getMSM() && MSMnetwork){ 
                        networkM.addNode(m);
                    }
                    //if they have sex with women OR the msm network is not active, add them to the mf network.
                    if(m.getMSW() || !MSMnetwork){
                        networkMF.addNode(m);
                    }
                }
            }
            stopper = schedule.scheduleRepeating(Schedule.EPOCH, 1, agent);
            agent.setStoppable(stopper);
        }
        //generate initial network.
        Generator.generateInitialNetwork(this);
        //add network stepable- break & create networks
        Steppable processInteractions = new Steppable(){
            private static final long serialVersionUID = 1;
            @Override
            public void step(SimState state) {
                HIVMicroSim sim = (HIVMicroSim) state;
                //remove oneshots and process relationships created last round or during
                //initialization.
                HandlerInteraction.processCoitalInteractions(sim);
                Agent agent;
                //this has to be changed to the agents object and a check for age must be added because there 
                //may be multiple networks in play. In the future this method can handle walking over all the networks
                Bag allAgents = agents.allObjects;
                for(Object o : allAgents){
                    agent = (Agent) o;
                    if(agent.getTickAge() < networkEntranceAge || !agent.alive)continue;
                    //set up new networks
                    if(agent.wantsConnection(sim)){
                        HandlerInteraction.findConnection(agent, sim);
                    }
                }//end for
            }//end step
        };
        schedule.scheduleRepeating(Schedule.EPOCH, 2, processInteractions);
        
        //create and schedule the infect timer. 
        Infector infectTimer = new Infector(initializationOnTick, numInfect, initializeHighRiskPop);
        infectTimer.setStopper(schedule.scheduleRepeating(Schedule.EPOCH, 4, infectTimer));
        
        Steppable agentGenerator = new Steppable(){
            private static final long serialVersionUID = 1;
            @Override
            public void step(SimState state) {
                HIVMicroSim sim = (HIVMicroSim) state;
                sim.agentGrowth += sim.populationGrowth*sim.agents.allObjects.size();
                if(sim.agentGrowth>=1){
                    int i;
                    Agent agent;
                    Stoppable stopper;
                    for(i = 0; i<sim.agentGrowth; i++){
                        agent = Generator.generateAgent(sim, false);
                        logger.insertNewAgent(agent);
                        agents.setObjectLocation(agent,random.nextInt(gridWidth), random.nextInt(gridHeight));
                        if(agent.getTickAge() >=sim.networkEntranceAge){
                            if(agent.isFemale()){
                                networkMF.addNode(agent);
                            }else{
                                Male m = (Male)agent;
                                //if they have sex with other men and the msm network is active they will be added to that network
                                if(m.getMSM() && MSMnetwork){ 
                                    networkM.addNode(m);
                                }
                                //if they have sex with women OR the msm network is not active, add them to the mf network.
                                if(m.getMSW() || !MSMnetwork){
                                    networkMF.addNode(m);
                                }
                            }
                        }
                        stopper = schedule.scheduleRepeating(sim.schedule.getSteps(), 1, agent);
                        agent.setStoppable(stopper);
                    }
                    agentGrowth -=i;
                }
            }//end step
        };
        schedule.scheduleRepeating(Schedule.EPOCH, 5, agentGenerator);
    }
    public static void main(String[] args){
        doLoop(HIVMicroSim.class, args);
        System.exit(0);
    }
    @Override
    public void finish(){
        logger.close();
        debugLog.close();
        super.finish();
    } 
} 
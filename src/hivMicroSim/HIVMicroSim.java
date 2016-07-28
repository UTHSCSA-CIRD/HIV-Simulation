/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim;

import hivMicroSim.Agent.Agent;
import hivMicroSim.Agent.Personality;
import sim.engine.*;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import java.io.IOException;
/**
 *
 * @author ManuelLS
 */
public class HIVMicroSim extends SimState{
    public SparseGrid2D agents;
    public ListNetwork network;
    public int numAgents = 100;
    //records the current growth of new agents. Is a double because growth is by month and new
    //agents might only appear ever x months.
    public double agentGrowth = 0; 
    //Population growth per month
    public double populationGrowth = .002;
    
    public int newAgents = 0;
    public int numInfect = 2; //initial number of infected agents. 
    public int currentID = 0;
    public int gridWidth = 100;
    public int gridHeight = 100;
    public int networkEntranceAge = 216;//18 years
    
    //global agent descriptors- gaussian distribution between 1 and 10 with these as the means
    public double percentCondomUse = .5; //0-1 inclusive
    public int maleMonogamous = 5;      //0-10
    public int femaleMonogamous = 5;    // 0-10
    public int maleCommittedness = 5;   //0-10
    public int femaleCommittedness = 5; // 0-10
    public int maleLibido = 15;  
    public int femaleLibido= 15; 
    public boolean allowExtremes = true;
    public double commitmentChange = 0.05;
    
    //natural resistance -- replacing genes until further science is available 
    public double resistanceMax = 2;  //maximum resistance
    public double resistanceMin = 0;  //minimum resistance
    public double resistanceAvg = 1;  //average resistance.
    
    //population percentages 
    public double percentMsMW = .1;   //0-1 (combined with MsM cannot exceed 1)
    public double percentMsM = .02;   // 0-1(combined with MsMW cannot exceed 1)
    public double percentCircum = .5; //percent circumcised
    
    //testing likelihoods and range between tests
    public double testingLikelihood = .5; //the percent of the population likely to get tested.
    public int testingRangeMin = 12; //The minimum time period between tests
    public int testingRangeMax = 120; //the maximum time period between tests
    public int testingRangeAverage = 24; //the average amount of time between tests
    public double testAccuracy = .98; //The likelihood of testing positive when positive
    public double testTicks = 1; //number of ticks afer which the agent may test positive. 
    
   
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
    public int averageAge = 300;//in months
    public int averageLifeSpan = 780;
    public double pregnancyChance = .008;
    public HIVLogger logger;
    public DebugLogger debugLog;
    private String simDebugFile = "simDebug.txt";
    private int simDebugLevel = DebugLogger.LOG_ALL;
    public int initializationOnTick = 10; // the tick on which we will initiate infection. 
    
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
    public int getInitializationTick(){
        return initializationOnTick;
    }
    public void setInitializationTick(int a){
        if(a >0){
            initializationOnTick = a;
        }
    }
    
    public int getAverageAge(){
        return averageAge;
    }
    public int getAverageLifeSpan(){
        return averageLifeSpan;
    }
    public void setAverageAge(int a){
        if(a > 1 && a < averageLifeSpan){
            averageAge = a;
        }
    }
    public void setAverageLifeSpan(int a){
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
    public double getCommitmentChange(){return commitmentChange;}
    public void setCommitmentChange(double a){
        if(commitmentChange <=.1 && commitmentChange >=0){
            commitmentChange = a;
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
    public int getGaussianRange(int min, int max, int avg, boolean reroll, boolean inclusive){
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
    public double getGaussianRangeDouble(double min, double max, double avg, boolean reroll){
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
        double rand;
        if(reroll){
            do{
                rand= (random.nextGaussian()*std)+mean+ offset;
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
    public int getMaleCommitted(){return maleCommittedness;}
    public int getFemaleCommitted(){return femaleCommittedness;}
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
    public void setMaleCommitted(int a){
        if(a >=Personality.commitmentMin && a <= Personality.commitmentMax){
            maleCommittedness = a;
        }
    }
    public void setFemaleCommitted(int a){
        if(a >=Personality.commitmentMin && a <= Personality.commitmentMax){
            femaleCommittedness = a;
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
        network = new ListNetwork();
        logger = new HIVLogger();
        
    }
    
    @Override
    public void start(){
        super.start();
        logger = new HIVLogger();
        try{
            debugLog = new DebugLogger(simDebugLevel, simDebugFile);
            schedule.scheduleRepeating(Schedule.EPOCH, 0, debugLog);
        }catch(IOException e){
            System.err.println("Exception when creating logger!! " + e.getLocalizedMessage());
            debugLog = new DebugLogger();
        }
        agents = new SparseGrid2D(gridWidth, gridHeight);
        network = new ListNetwork(false);
        currentID = numAgents;
        
        //generate starter agents, add them to the network and object location.
        Agent[] s = new Agent[numAgents];
        Stoppable stopper;
        Agent agent;
        for(int i = 0; i < numAgents; i++){
            agent = Generator.generateAgent(this, true);
            agents.setObjectLocation(agent,random.nextInt(gridWidth), random.nextInt(gridHeight));
            if(agent.getAge() >= networkEntranceAge) network.addNode(agent);
            stopper = schedule.scheduleRepeating(Schedule.EPOCH, 1, agent);
            agent.setStoppable(stopper);
            s[i] = agent;
        }
        //generate initial network.
        Generator.generateInitialNetwork(this);
        //add network stepable- break & create networks
        Steppable n = new Steppable(){
            private static final long serialVersionUID = 1;
            @Override
            public void step(SimState state) {
                HIVMicroSim sim = (HIVMicroSim) state;
                //remove oneshots and process relationships created last round or during
                //initialization.
                HandlerRelationship.processRelationships(sim);
                Agent agent;
                Bag allAgents = network.getAllNodes();
                for(Object o : allAgents){
                    agent = (Agent) o;
                    //set up new networks
                    if(agent.wantsConnection(sim)){
                        HandlerRelationship.findConnection(agent, sim);
                    }
                }//end for
            }//end step
        };
        schedule.scheduleRepeating(Schedule.EPOCH, 2, n);
        
        //create and schedule the infect timer. 
        Infector infectTimer = new Infector(initializationOnTick, numInfect);
        infectTimer.setStopper(schedule.scheduleRepeating(Schedule.EPOCH, 4, infectTimer));
        
        Steppable agentGenerator = new Steppable(){
            private static final long serialVersionUID = 1;
            @Override
            public void step(SimState state) {
                HIVMicroSim sim = (HIVMicroSim) state;
                sim.agentGrowth += sim.populationGrowth*sim.network.allNodes.size();
                if(sim.agentGrowth>=1){
                    int i;
                    Agent agent;
                    Stoppable stopper;
                    for(i = 0; i<sim.agentGrowth; i++){
                        agent = Generator.generateAgent(sim, false);
                        agents.setObjectLocation(agent,random.nextInt(gridWidth), random.nextInt(gridHeight));
                        if(agent.getAge() >=sim.networkEntranceAge) network.addNode(agent);
                        stopper = schedule.scheduleRepeating(sim.schedule.getSteps(), 1, agent);
                        agent.setStoppable(stopper);
                    }
                    agentGrowth -=i;
                }
            }//end step
        };
        schedule.scheduleRepeating(Schedule.EPOCH, 2, agentGenerator);
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

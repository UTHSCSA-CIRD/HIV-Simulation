/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim;
import hivMicroSim.Agent.Male;
import hivMicroSim.Agent.Female;
import hivMicroSim.Agent.Agent;
import hivMicroSim.Agent.Personality;
import hivMicroSim.Agent.Pregnancy;
import sim.engine.*;
import sim.field.network.*;
import java.util.ArrayList;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import java.io.IOException;
/**
 *
 * @author ManuelLS
 */
public class HIVMicroSim extends SimState{
    public SparseGrid2D agents;
    public Network network;
    public int numAgents = 100;
    public int numInfect = 2;//initial number of infected agents. 
    private int currentID = 0;
    public int gridWidth = 100;
    public int gridHeight = 100;
    public int networkEntranceAge = 216;//18 years
    
    //global agent descriptors- gaussian distribution between 1 and 10 with these as the means
    public double percentCondomUse = .5; //0-1 inclusive
    public int maleFaithfulness = 5; //0-10
    public int femaleFaithfulness = 6; // 0-10
    public int maleWant = 6; // 0-10
    public int femaleWant = 5; // 0-10
    
    //natural resistance -- replacing genes until further science is available 
    public double resistanceMax = 2; //maximum resistance
    public double resistanceMin = 0;  //minimum resistance
    public double resistanceAvg = 1;  //average resistance.
    
    //population percentages 
    public double percentMsMW = .1; //0-1 (combined with MsM cannot exceed 1)
    public double percentMsM = .02; // 0-1(combined with MsMW cannot exceed 1)
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
    public double likelinessFactorCircumcision = .5;
    //interaction type (mother to child vs everything else.)
    public double perInteractionLikelihood = 0.0001;
    public double motherToChildInfection = .3;
    
    //Population statistics
    public int averageAge = 300;//in months
    public int averageLifeSpan = 780;
    public double pregnancyChance = .008;
    public HIVLogger logger;
    public DebugLogger debugLog;
    private String simDebugFile = "simDebug.txt";
    private int simDebugLevel = DebugLogger.LOG_ALL;
    public int initializationOnTick = 300; // the tick on which we will initiate infection. 
    
    public int getStartInfected(){
        return numInfect;
    }
    public int getInitializationTick(){
        return initializationOnTick;
    }
    public void setInitializationTick(int a){
        if(a >0){
            initializationOnTick = a;
        }
    }
    
    public double getMotherToChildInfection(){
        return motherToChildInfection;
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
    public double getPerInteractionLikelihood(){
        return perInteractionLikelihood;
    }
    public void setPerInteractionLikelihood(double a){
        if(a < .1){
            perInteractionLikelihood = a;
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
        if(percentCircum <=1){
            percentCircum = a;
        }
    }
    
    public int getGaussianRange(int min, int max, int avg, boolean reroll, boolean inclusive){
        if(min == max) return min;
        if(min > max){
            int tmp = max;
            max = min;
            min = tmp;
        }
        double mean = ((max-min)/2)+ min;
        if(avg > max || avg < min){
            System.err.println("Average is out of range. Setting average to the mean.");
            avg = (int)mean;
        }
        int offset = (int)mean - avg;
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
    public double getGaussianRangeDouble(double min, double max, double avg, boolean reroll){
        if(min == max) return min;
        if(min > max){
            double tmp = max;
            max = min;
            min = tmp;
        }
         
        double mean = ((max-min)/2)+ min;
        if(avg > max || avg < min){
            System.err.println("Average is out of range. Setting average to the mean.");
            avg = mean;
        }
        double offset = mean - avg;
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
        return (int)rand;
    }
    
    
    public int getMaleFaithfulness(){return maleFaithfulness;}
    public int getFemaleFaithfulness(){return femaleFaithfulness;}
    public int getMaleWant(){return maleWant;}
    public int getFemaleWant(){return femaleWant;}
    public void setMaleFaithfulness(int a){
        if(a >=0 && a <= 10){
            maleFaithfulness = a;
        }
    }
    public void setFemaleFaithfulness(int a){
        if(a >=0 && a <= 10){
            femaleFaithfulness = a;
        }
    }
    public void setMaleWant(int a){
        if(a >=0 && a <= 10){
            maleWant = a;
        }
    }
    public void setFemaleWant(int a){
        if(a >=0 && a <= 10){
            femaleWant = a;
        }
    }
    public double getModelCondomUse(){
        return percentCondomUse;
    }
    public void setModelCondomUse(double a){
        if(a >=0 && a<=1){
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
        network = new Network();
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
        network = new Network(false);
        currentID = numAgents;
                
        Agent[] s = new Agent[numAgents];
        //create the agents
        int maxLife = averageLifeSpan + (int)(averageLifeSpan*.5);
        
        //currently used variables
        int faithfulness;
        double condomUse;
        double want;
        boolean female;
        double geneRoll; // to hold the roll
        byte ccr51;
        byte ccr52;
        byte ccr21;
        byte ccr22;
        byte HLA_A1;
        byte HLA_A2;
        byte HLA_B1;
        byte HLA_B2;
        byte HLA_C1;
        byte HLA_C2;
        
        double lack;
        int age;
        int ageOffset = -((maxLife/2) - averageAge); //should be negative. 
        int life;
        int offsetLife;
        
        Stoppable stopper;
        for(int i=0; i<numAgents; i++){
            //build agent.
            female = random.nextBoolean();
            try{
                if(female){
                    faithfulness = getGaussianRange(0, 10,  false);
                }else{
                    faithfulness = getGaussianRange(offsetMF, 0, 10, false);
                }
            }catch(OffSetOutOfRangeException e){
                System.err.println("OffsetFaithfulness");
                faithfulness = getGaussianRange(0, 10, false);
            }
            try{
                if(female){
                    want = getGaussianRangeDouble(offsetFW, 0, 10, false);
                }else{
                    want = getGaussianRangeDouble(offsetMW, 0, 10, false);
                }
            }catch(OffSetOutOfRangeException e){
                System.err.println("OffsetWant");
                want = getGaussianRangeDouble(0, 10, false);
            }
            try{
                condomUse = getGaussianRangeDouble(offsetCondom, 0, 1, false);
            }catch(OffSetOutOfRangeException e){
                System.err.println("CondomUse offset");
                condomUse = getGaussianRangeDouble(0,1, false);
            }
            try{
  ///////////////////Note that this is a bad fit 2* averageLifeSpan is not a good range and may result in an older population than
                //desired. 
                age = getGaussianRange(ageOffset, 0 , maxLife, true);
                //System.out.println("DEBUG: Age: " + age);
            }catch(OffSetOutOfRangeException e){
                System.err.println("Age offset out of bounds");
                age = getGaussianRange(0, maxLife, true);
            }
            if(age>=averageLifeSpan){
                offsetLife = -(((maxLife-age)/2)-1);
            }else{
                offsetLife = -((((maxLife-age)/2)+age)-averageLifeSpan);
            }
            
            try{
                life = getGaussianRange(offsetLife, age, maxLife, true);
            }catch(OffSetOutOfRangeException e){
                System.err.println("Life Expectancy offset out of bounds!");
                life = getGaussianRange(age, maxLife, true);
            }
            
            lack = random.nextDouble()*10;//random number from 0-10 (inclusive)
            
            Agent agent; 
            if(female){
                agent = new Female(i, faithfulness, condomUse, want, lack, age, life);
            }else{
                agent = new Male(i, faithfulness, condomUse, want, lack, ccr51, ccr52, ccr21, ccr22, HLA_A1, HLA_A2, HLA_B1, HLA_B2, HLA_C1, HLA_C2, age, life);
            }
            agents.setObjectLocation(agent,random.nextInt(gridWidth), random.nextInt(gridHeight));
            if(agent.getAge() >=216) network.addNode(agent);
            stopper = schedule.scheduleRepeating(Schedule.EPOCH, 1, agent);
            agent.setStoppable(stopper);
            s[i] = agent;
        }
        //generate initial network.
        int roll;
        double rollD;
        int connectID;
        Agent connect;
        
        Relationship edge;
        int edgeVal;
        for (Agent me : s) {
            if(me.getAge()<216)continue;//18 years * 12 months
            rollD = random.nextDouble()*10;
            if(rollD < me.getLack()){
                do{//repeat until we find a connection of suitable age and gender.
                    connectID = random.nextInt(numAgents); 
                    connect = s[connectID];
                }while((connect.isFemale() == me.isFemale()) || (connect.getAge() < 216));
                //now that we have an agent, see if their network has space for another edge.
                if(connect.wantsConnection(this)){ //using this function so we can add more advanced code in there later
                    //this one is selected.
                    if(connect.getWantLevel() > me.getWantLevel()){
                        edgeVal = (int)(((connect.getWantLevel()-me.getWantLevel())/2) + me.getWantLevel());
                    }else{
                        edgeVal = (int)(((me.getWantLevel()-connect.getWantLevel())/2) + connect.getWantLevel());
                    }
                    int relationship = Relationship.RELATIONSHIP;
                    int ii;
                    if(!me.isMarried() && !connect.isMarried()){
                         ii = random.nextInt(10);
                        if(ii < me.getFaithfulness() && ii < connect.getFaithfulness()){
                            relationship =Relationship.MARRIAGE;
                        }else{
                            if(ii > me.getFaithfulness() && ii > connect.getFaithfulness()){
                                relationship = Relationship.ONETIME;
                                edgeVal = 1;
                            }
                        }
                    }else{
                        ii = random.nextInt(10);
                        if(ii > me.getFaithfulness() && ii > connect.getFaithfulness()){
                            relationship = Relationship.ONETIME;
                            edgeVal = 1;
                        }
                    }
                    if(me.isFemale()){
                        edge = new Relationship(relationship, connect, me, edgeVal);
                    }else{
                        edge = new Relationship(relationship, me, connect, edgeVal);
                    }
                    connect.addEdge(edge);
                    me.addEdge(edge);
                    network.addEdge(edge);
                }
            }
        }
       
        //add network stepable- break & create networks
        Steppable n = new Steppable(){
            private static final long serialVersionUID = 1;
            @Override
            public void step(SimState state) {
                HIVMicroSim sim = (HIVMicroSim) state;
                double roll;
                double diff;
                int ii;
                Agent connect;
                Agent agent;
                Relationship e;
                Bag allAgents = network.getAllNodes();
                for(Object o : allAgents){
                    agent = (Agent) o;
                    if(!agent.alive) continue;
                    if(agent.getAge() < networkEntranceAge) continue;
                    agent.removeOneShots(state);
                    if(agent.getNetwork().size()>0){// if < =0 no network.
                        diff = Math.abs(agent.getNetworkLevel()- agent.getWantLevel());// getting the difference between their wants and what's provided.
                        ii = random.nextInt(agent.getNetwork().size());
                        e = agent.getNetwork().get(ii);
                        try{
                            roll = getGaussianRangeDouble(agent.getFaithfulness()-5, 0,10, true)*e.getLevel();
                        }catch(OffSetOutOfRangeException except){
                            System.err.println("Network Step, Agent Faithfulness");
                            roll = getGaussianRangeDouble(0,10, true) * e.getType();
                        }
                        if(roll < diff){
                            //dissolve
                            e.getMale().removeEdge(e);
                            e.getFemale().removeEdge(e);
                            if(network.removeEdge(e) == null){
                                System.err.println("COULD NOT REMOVE EDGE!!!");
                            }
                        }
                    }
                    //set up new networks
                    try{
                        roll = getGaussianRangeDouble(agent.getFaithfulness()-5, 0,10, true);
                    }catch(OffSetOutOfRangeException except){
                        System.err.println("Network Setup - get Faithfulness");
                        roll = getGaussianRangeDouble(0,10, true);
                    }
                    if(roll < agent.getLack()){
                        //create a relationship.
                        int tries = 0; //prevent infinite look in case of small pool of agents.
                        do{
                            ii = random.nextInt(allAgents.size());
                            connect = (Agent)allAgents.get(ii);
                            tries ++;
                        }while(((agent.isFemale() == connect.isFemale()) || (connect.getAge() < 216) || (!connect.alive) )&& tries <10);
                        if(tries ==10)continue;
                        if(connect.wantsConnection(sim)){
                            int edgeVal;
                            if(connect.getWantLevel() > agent.getWantLevel()){
                                edgeVal = (int)(((connect.getWantLevel()-agent.getWantLevel())/2) + agent.getWantLevel());
                            }else{
                                edgeVal = (int)(((agent.getWantLevel()-connect.getWantLevel())/2) + connect.getWantLevel());
                            }
                            int relationship = Relationship.RELATIONSHIP;
                            if(!agent.isMarried() && !connect.isMarried()){
                                ii = random.nextInt(Personality.faithfulnessMax);
                                if(ii < agent.getFaithfulness() && ii < connect.getFaithfulness()){
                                    relationship =Relationship.MARRIAGE;
                                }else{
                                    if(ii > agent.getFaithfulness() && ii > connect.getFaithfulness()){
                                        relationship = Relationship.ONETIME;
                                        edgeVal = 1;
                                    }
                                }
                            }else{
                                ii = random.nextInt(Personality.faithfulnessMax);
                                if(ii > agent.getFaithfulness() && ii > connect.getFaithfulness()){
                                    relationship = Relationship.ONETIME;
                                    edgeVal = 1;
                                }
                            }
                            if(agent.isFemale()){
                                e = new Relationship(relationship, connect, agent, edgeVal);
                            }else{
                                e = new Relationship(relationship, agent, connect, edgeVal);
                            }
                            agent.addEdge(e);
                            connect.addEdge(e);
                            network.addEdge(e);
                        }
                    }
                }//end for
            }//end step
        };
        schedule.scheduleRepeating(Schedule.EPOCH, 2, n);
        
        //create and schedule the infect timer. 
        Infector infectTimer = new Infector(initializationOnTick, numInfect);
        infectTimer.setStopper(schedule.scheduleRepeating(Schedule.EPOCH, 4, infectTimer));
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim;
import hivMicroSim.HIV.Genotype;
import hivMicroSim.Agent.Male;
import hivMicroSim.Agent.Female;
import hivMicroSim.Agent.Agent;
import hivMicroSim.Agent.Pregnancy;
import sim.engine.*;
import sim.field.network.*;
import java.util.ArrayList;
import sim.field.grid.SparseGrid2D;
/**
 *
 * @author ManuelLS
 */
public class HIVMicroSim extends SimState{
    public SparseGrid2D agents;
    public Network network;
    public int numAgents = 100;
    public double initialInfectedPercent = .02;
    public int gridWidth = 100;
    public int gridHeight = 100;
    public int infected = 0;
    
    //global agent descriptors- gaussian distribution between 1 and 10 with these as the means
    public int percentCondomUse = 50; //0-100
    public int maleFaithfulness = 5; //0-10
    public int femaleFaithfulness = 6; // 0-10
    public int maleWant = 6; // 0-10
    public int femaleWant = 5; // 0-10
    public double ccr5Resistance = .01; //.00-1 - decimal percentage of population with ccr5 resistance.
    /*http://www.k-state.edu/parasitology/biology198/answers1.html 
    * Hardy-Weinberg Law - p^2 + 2pq + q^2 = 1 and p + q = 1; 
    */
    public double femaleLikelinessFactor = 1.5; //the increased likelihood of females to aquire the virus. 
    public double circumcisionLikelinessFactor = .5;
    /*
    * For a score of 1000 (No resistance, viral load 1000, male, uncircumcised) give a 1 in 10 chance per interaction.
    */
    public double perInteractionLikelihood = 0.00001; //we'll say this per 100 in viral load..? -- Obviously we'll need some adjustments here 
    public double motherToChildInfection = .1;
    //Population statistics
    public int averageAge = 300;//in months
    public int averageLifeSpan = 780;
    public double pregnancyChance = .008;
    
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
            averageAge = a;
        }
    }
    public int getInfected(){
        return infected;
    }
    public double getPerInteractionLikelihood(){
        return perInteractionLikelihood;
    }
    public void setPerInteractionLikelihood(double a){
        if(a < .1){
            perInteractionLikelihood = a;
        }
    }
    public final ArrayList<Genotype> genotypeList = new ArrayList<>();
    
    public void setupGenoTypeList(){
        //this simply sets up the basic genotypes for the virus. Some will be based on actual strains, others will be
        //randomly created. Might later add the ability for each model to create recombinant forms of the virus.
        //hard coded in the order of add- ONLY the first virus (genotype 0) will be added at the start of the model
        //all additional genotypes have the chance to be added later when immigration is added to the model. 
        //The lower the numerical value the more common the genotype, thus, genotype 16 is less likely to migrate in than genotype 15.
        //Note that this does not change how quickly the disease spreads among the susceptible population and recombinant forms
        //added later are already in the population and don't need to migrate in. 
        genotypeList.clear();
        genotypeList.add(new Genotype(0, 600, true));
        genotypeList.add(new Genotype(1, 900, true));
        genotypeList.add(new Genotype(2, 600, false));
    }
    
    public int getGaussianRange(int min, int max){
        if(min > max){
            int tmp = max;
            max = min;
            min = tmp;
        }
        double mean = ((max-min)/2)+ min;
        double std = (max-mean)/3; //most numbers will be within 3 standard devaitions.
        double rand;
        do{
            rand= (random.nextGaussian()*std)+mean;
        }while(rand <= min || rand >= max);
        return (int)rand;
    }
    public int getGaussianRange(int offset, int min, int max) throws OffSetOutOfRangeException{
        if(min > max){
            int tmp = max;
            max = min;
            min = tmp;
        }
        double mean = ((max-min)/2)+ min;
        if(mean + offset > max || mean + offset < min){
            System.err.println("Offset of: " + offset + " will push the mean: " + mean + " out of range: " + min + " - " + max +".");
            throw new OffSetOutOfRangeException();
        }
        double std = (max-mean)/3; //most numbers will be within 3 standard devaitions.
        double rand;
        do{
            rand= (random.nextGaussian()*std)+mean;
        }while(rand <= min || rand >= max);
        return (int)rand;
    }
    public double getGaussianRangeDouble(double min, double max){
        if(min > max){
            double tmp = max;
            max = min;
            min = tmp;
        }
        double mean = ((max-min)/2)+ min;
        double std = (max-mean)/3; //most numbers will be within 3 standard devaitions.
        double rand;
        do{
            rand = (random.nextGaussian()*std)+mean;
        }while(rand <= min || rand >= max);
        return rand;
    }
    public double getGaussianRangeDouble(double offset, double min, double max) throws OffSetOutOfRangeException{
        if(min > max){
            double tmp = max;
            max = min;
            min = tmp;
        }
        double mean = ((max-min)/2)+ min;
        if(mean + offset > max || mean + offset < min){
            System.err.println("Offset of: " + offset + " will push the mean: " + mean + " out of range: " + min + " - " + max +".");
            throw new OffSetOutOfRangeException();
        }
        double std = (max-mean)/3; //most numbers will be within 3 standard devaitions.
        double rand;
        do{
            rand = (random.nextGaussian()*std)+mean+offset;
        }while(rand <= min || rand >= max);
        return rand;
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
    public double getFemaleLikelinessFactor(){
        return femaleLikelinessFactor;
    }
    public void setFemaleLikelinessFactor(int a){
        if(femaleLikelinessFactor > 1){
            femaleLikelinessFactor = a;
        }
    }
    public double getCircumcisionLikelinessFactor(){
        return circumcisionLikelinessFactor;
    }
    public void setCircumcisionLikelinessFactor(int a){
        if(circumcisionLikelinessFactor < 1 && circumcisionLikelinessFactor > 0){
            circumcisionLikelinessFactor = a;
        }
    }
    public double getCCR5Resistance(){return ccr5Resistance;}
    public void setCCR5Resistance(double a){
        if(a >=0 && a <=1){
            ccr5Resistance = a; 
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
        infected = 0;
    }
    public Agent createNewAgent(Pregnancy p){
        boolean female;
        ArrayList<Infection> infections = new ArrayList<>();
        ArrayList<AlloImmunity> allo = new ArrayList<>();
        ArrayList<SeroImmunity> sero = new ArrayList<>();
        int offsetF;
        int offsetW;
        int condomUse;
        double want;
        int offsetCondom = percentCondomUse - 50;
        int faithfulness;
        
        Stoppable stopper;
        female = random.nextBoolean();
        if(female){
            offsetF = femaleFaithfulness -5;
            offsetW = femaleWant - 5;
        }else{
            offsetF = maleFaithfulness -5;
            offsetW = maleWant - 5;
        }
        
        try{

            faithfulness = getGaussianRange(offsetF, 0, 10);
        }catch(OffSetOutOfRangeException e){
            System.err.println("OffsetFaithfulness");
            faithfulness = getGaussianRange(0, 10);
        }
        try{
            want = getGaussianRangeDouble(offsetW, 0, 10);
        }catch(OffSetOutOfRangeException e){
            System.err.println("OffsetWant");
            want = getGaussianRangeDouble(0, 10);
        }
        try{
            condomUse = getGaussianRange(offsetCondom, 0, 100);
        }catch(OffSetOutOfRangeException e){
            System.err.println("CondomUse offset");
            condomUse = getGaussianRange(0,100);
        }
        Agent agent;
        int i = agents.allObjects.size();
        if(female){
            agent = new Female(i, faithfulness, condomUse, want, 0, p.getCCR51(), p.getCCR52(), p.getImmuneFactors(), 0);
        }else{
            agent = new Male(i, faithfulness, condomUse, want, 0, p.getCCR51(), p.getCCR52(), p.getImmuneFactors(), 0);
        }
        //add to grids
        agents.setObjectLocation(agent,random.nextInt(gridWidth), random.nextInt(gridHeight));
        network.addNode(agent); // handles the network only! Display of nodes handled by continuous 2D
        //add to schedule
        stopper = schedule.scheduleRepeating(agent);
        //set stoppable
        agent.setStoppable(stopper);
        return agent;
    }
    @Override
    public void start(){
        super.start();
        setupGenoTypeList();
        agents = new SparseGrid2D(gridWidth, gridHeight);
        network = new Network(false);
        infected = 0;
        
        
        Agent[] s = new Agent[numAgents];
        //create the agents
        //set offsets
        int offsetMF = maleFaithfulness -5;
        int offsetFF = femaleFaithfulness -5;
        int offsetMW = maleWant - 5;
        int offsetFW = femaleWant - 5;
        int offsetCondom = percentCondomUse - 50;
        double ccr5GenePrevalence = Math.sqrt(ccr5Resistance);
        
        //currently used variables
        int faithfulness;
        int condomUse;
        double want;
        boolean female;
        double ccr5; // to hold the roll
        boolean ccr51;
        boolean ccr52;
        double immuneFactors;
        double lack;
        int age;
        int ageOffset = averageAge - averageLifeSpan; //should be negative. 
        //currently fudged variables
        
        ArrayList<Infection> infections = new ArrayList<>();
        ArrayList<AlloImmunity> allo = new ArrayList<>();
        ArrayList<SeroImmunity> sero = new ArrayList<>();
        Stoppable stopper;
        for(int i=0; i<numAgents; i++){
            //build agent.
            female = random.nextBoolean();
            try{
                if(female){
                    faithfulness = getGaussianRange(offsetFF, 0, 10);
                }else{
                    faithfulness = getGaussianRange(offsetMF, 0, 10);
                }
            }catch(OffSetOutOfRangeException e){
                System.err.println("OffsetFaithfulness");
                faithfulness = getGaussianRange(0, 10);
            }
            try{
                if(female){
                    want = getGaussianRangeDouble(offsetFW, 0, 10);
                }else{
                    want = getGaussianRangeDouble(offsetMW, 0, 10);
                }
            }catch(OffSetOutOfRangeException e){
                System.err.println("OffsetWant");
                want = getGaussianRangeDouble(0, 10);
            }
            try{
                condomUse = getGaussianRange(offsetCondom, 0, 100);
            }catch(OffSetOutOfRangeException e){
                System.err.println("CondomUse offset");
                condomUse = getGaussianRange(0,100);
            }
            try{
  ///////////////////Note that this is a bad fit 2* averageLifeSpan is not a good range and may result in an older population than
                //desired. 
                age = getGaussianRange(ageOffset, 0, (2*averageLifeSpan));
            }catch(OffSetOutOfRangeException e){
                System.err.println("Age offset out of bounds");
                age = getGaussianRange(1, averageLifeSpan);
            }
            ccr5 = random.nextDouble(); // next double between 0 and 1(exclusive)
            if(ccr5 < ccr5GenePrevalence){
                if(ccr5 < ccr5Resistance){
                    ccr51 = true;
                    ccr52 = true;
                }else{
                    ccr51 = true;
                    ccr52 = false;
                }
            }else{
                ccr51 = false;
                ccr52 = false;
            }
            lack = random.nextDouble()*10;//random number from 0-10 (inclusive)
            immuneFactors = Math.abs(random.nextGaussian()/3); // centers at 0- sets the first 3 standard deviations between 0 and 1. 
            if(immuneFactors > 1) immuneFactors = 0; // if not between 0 and 1, set to 0. 
            Agent agent; 
            if(female){
                agent = new Female(i, faithfulness, condomUse, want, lack, ccr51, ccr52, immuneFactors, age);
            }else{
                agent = new Male(i, faithfulness, condomUse, want, lack, ccr51, ccr52, immuneFactors, age);
            }
            agents.setObjectLocation(agent,random.nextInt(gridWidth), random.nextInt(gridHeight));
            network.addNode(agent); // handles the network only! Display of nodes handled by continuous 2D
            stopper = schedule.scheduleRepeating(agent);
            agent.setStoppable(stopper);
            s[i] = agent;
        }
        //generate initial network.
        int roll;
        int connectID;
        Agent connect;
        
        Relationship edge;
        int edgeVal;
        for (Agent me : s) {
            if(me.getAge()<216)continue;//18 years * 12 months
            roll = random.nextInt(11);
            if(roll < me.getLack()){
                do{//repeat until we find a
                    connectID = random.nextInt(numAgents); 
                    connect = s[connectID];
                }while((connect.isFemale() == me.isFemale()) || (connect.getAge() < 216));
                //now that we have an agent, see if their network has space for another edge.
                if(connect.getNetworkLevel() < connect.getWantLevel()){
                    //this one is selected.
                    if(connect.getWantLevel() > me.getWantLevel()){
                        edgeVal = (int)(((connect.getWantLevel()-me.getWantLevel())/2) + me.getWantLevel());
                    }else{
                        edgeVal = (int)(((me.getWantLevel()-connect.getWantLevel())/2) + connect.getWantLevel());
                    }
                    if(me.isFemale()){
                        edge = new Relationship(Relationship.RELATIONSHIP,connect, me, edgeVal);
                    }else{
                        edge = new Relationship(Relationship.RELATIONSHIP,me, connect, edgeVal);
                    }
                    connect.addEdge(edge);
                    me.addEdge(edge);
                    network.addEdge(edge);
                }
            }
        }
        //Setup initial infection
        int numInfected = (int)(numAgents * initialInfectedPercent);
        if(numInfected <1)numInfected = 1;
        for(int i = 0; i<numInfected;i++){
            boolean inf = false;
            do{
                roll = random.nextInt(numAgents);
                if(s[roll].getAge() >= 216){//lets assume children aren't doing things to become patient 0.
                    inf = s[roll].infect(genotypeList.get(0));
                }
            }while(inf == false); //so that if we find someone resistant or if they are too young.
            infected++;
        }
        //add network stepable- break & create networks
        Steppable n = new Steppable(){
            private static final long serialVersionUID = 1;
            @Override
            public void step(SimState state) {
                double roll;
                double diff;
                int ii;
                Agent connect;
                Agent agent;
                Relationship e;
                for(Object o : agents.allObjects){
                    agent = (Agent) o;
                    if(!agent.alive) continue;
                    if(agent.getAge() < 216) continue;
                    if(agent.getNetworkSize()>0){// if < =0 no network.
                        diff = Math.abs(agent.getNetworkLevel()- agent.getWantLevel());// getting the difference between their wants and what's provided.
                        ii = random.nextInt(agent.getNetwork().size());
                        e = agent.getNetwork().get(ii);
                        try{
                            roll = getGaussianRangeDouble(agent.getFaithfulness()-5, 0,10)*e.getType();
                        }catch(OffSetOutOfRangeException except){
                            System.err.println("Network Step, Agent Faithfulness");
                            roll = getGaussianRangeDouble(0,10) * e.getType();
                        }
                        if(roll < diff){
                            //disolve
                            e.getMale().removeEdge(e);
                            e.getFemale().removeEdge(e);
                           
                            Edge removeEdge = network.removeEdge(e);
                            if(removeEdge == null){
                                System.err.println("COULD NOT REMOVE EDGE!!!");
                            }
                        }
                    }
                    //set up new networks
                    try{
                        roll = getGaussianRangeDouble(agent.getFaithfulness()-5, 0,10);
                    }catch(OffSetOutOfRangeException except){
                        System.err.println("Network Setup - get Faithfulness");
                        roll = getGaussianRangeDouble(0,10);
                    }
                    if(roll < agent.getLack()){
                        //create a relationship.
                        do{
                            ii = random.nextInt(s.length);
                            connect = s[ii];
                        }while((agent.isFemale() == connect.isFemale()) || (connect.getAge() < 18) || (!connect.alive));
////////////////////////// Should probably create a "Wants" or something algorithm into each agent and use that instead of something like
                        //////////this-- currently the recipient's "lack" is not being considered and all relationships are just relationships.
                        if(connect.getNetworkLevel() < connect.getWantLevel()){
                            int edgeVal;
                            if(connect.getWantLevel() > agent.getWantLevel()){
                                edgeVal = (int)(((connect.getWantLevel()-agent.getWantLevel())/2) + agent.getWantLevel());
                            }else{
                                edgeVal = (int)(((agent.getWantLevel()-connect.getWantLevel())/2) + connect.getWantLevel());
                            }
                            if(agent.isFemale()){
                                e = new Relationship(Relationship.RELATIONSHIP, connect, agent, edgeVal);
                            }else{
                                e = new Relationship(Relationship.RELATIONSHIP, agent, connect, edgeVal);
                            }
                            agent.addEdge(e);
                            connect.addEdge(e);
                            network.addEdge(e);
                        }
                    }
                }//end for
            }//end step
        };
        schedule.scheduleRepeating(Schedule.EPOCH, 1, n, 2);
    }
    public static void main(String[] args){
        doLoop(HIVMicroSim.class, args);
        System.exit(0);
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim;
import Neighborhoods.Neighborhood;
import Neighborhoods.NeighborhoodTemplates;
import hivMicroSim.HIV.Genotype;
import hivMicroSim.Agent.Male;
import hivMicroSim.Agent.Female;
import hivMicroSim.Agent.Agent;
import hivMicroSim.Agent.AgentProfile;
import hivMicroSim.Agent.Gene;
import hivMicroSim.Agent.GeneProfile;
import hivMicroSim.Agent.Pregnancy;
import hivMicroSim.HIV.HIVInfection;
import sim.engine.*;
import java.util.ArrayList;
import java.util.LinkedList;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import java.io.IOException;
/**
 *
 * @author ManuelLS
 */
public class HIVMicroSim extends SimState{
    public SparseGrid2D agents;
    public RelationshipNetwork network;
    public int numAgents = 100;
    public int numInfect = 2;//initial number of infected agents. 
    private int currentID = 0;
    public int gridWidth = 100;
    public int gridHeight = 100;
    
    //global agent descriptors- gaussian distribution between 1 and 10 with these as the means
    public double percentCondomUse = .5; //0-1 inclusive
    public int maleFaithfulness = 10; 
    public int femaleFaithfulness = 20; 
    public static final int faithfulnessMax = 30;
    public int maleWant = 20; 
    public int femaleWant = 15; 
    public static final int wantMax = 30;
    public static final int lackMax = 30;
    public int oneShotValue = 5;
    public int femaleSelectivity = 30;
    public int maleSelectivity = 20; 
    public int selectivityMax = 40;//race(10) + religion(10) + age (10) + other(10) = 40 + 40- personal selectiveness 
    public int selectivityRoll = 50;//because race/religion/other selectivity @ max should not be overridden. 
    //homosexuality and bisexuality will go here... right now.... I really can't say how this is best coded... 
    //Gene prevelence = the prevelence of the alleles- this makes it a little easier to assign alleles. so 0-1 the sum of all versions of the alleles can't be > 1
    
    
    /*http://www.k-state.edu/parasitology/biology198/answers1.html 
    * Hardy-Weinberg Law - p^2 + 2pq + q^2 = 1 and p + q = 1; 
    */
    //////****Transmission LIKEINESS FACTORS****//////
    //http://www.cdc.gov/hiv/policies/law/risk.html last updated 11-16-2015
    public double femaleLikelinessFactor = 1.5; //the increased likelihood of females to aquire the virus. 
    public double circumcisionLikelinessFactor = .7;
    public double femaleToFemaleLikelinessFactor = .01;
    public double insertiveAnalLikelinessFactor = 2.75;
    public double receptiveAnalLikelinessFactor = 34.5;
    public double needleSharingLikelinessFactor = 15.75;
    public double perInteractionLikelihood = 0.00001;
    public double motherToChildInfection = .0005;
    //public double bloodTransfusion ~ 93% chance
    
    //////Population statistics////
    public int averageAge = 300;//in months
    public int averageLifeSpan = 780;
    public double pregnancyChance = .01;
    
    //neighborhoods
    public final Neighborhood race_Caucassian = NeighborhoodTemplates.getRaceCaucasian();
    public final Neighborhood race_Hispanic = NeighborhoodTemplates.getRaceHispanic();
    public final Neighborhood race_Black = NeighborhoodTemplates.getRaceBlack();
    public final Neighborhood race_Other = NeighborhoodTemplates.getRaceOther();
    public final Neighborhood religion_Catholic = NeighborhoodTemplates.getReligionCatholic();
    public final Neighborhood religion_Buddist = NeighborhoodTemplates.getReligionBuddist();
    public final Neighborhood other_Swinger = NeighborhoodTemplates.getOtherSwinger();
    
    //Logging////
    public HIVLogger logger;
    public DebugLogger debugLog;
    private final String simDebugFile = "simDebug.txt";
    private final int simDebugLevel = DebugLogger.LOG_ALL;
    
    
    //Initialization tick - this allows for the network and simulation to be "primed" prior to release of the infection.
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
        genotypeList.add(new Genotype(0, 1000, true));
        genotypeList.add(new Genotype(1, 600, true));
        genotypeList.add(new Genotype(2, 600, false));
    }
    
    public int getGaussianRange(int min, int max, boolean inclusive){
        if(min == max) return min;
        if(min > max){
            int tmp = max;
            max = min;
            min = tmp;
        }
        double mean = ((max-min)/2)+ min;
        double std = (max-mean)/3; //most numbers will be within 3 standard devaitions.
        double rand;
        if(inclusive){
            do{
                rand= (random.nextGaussian()*std)+mean;
            }while(rand <= min || rand >= max);
        }else{
            do{
                rand= (random.nextGaussian()*std)+mean;
            }while(rand < min || rand > max);
        }
        return (int)rand;
    }
    public int getGaussianRange(int offset, int min, int max, boolean inclusive) throws OffSetOutOfRangeException{
        if(min == max) return min;
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
        if(inclusive){
            do{
                rand= (random.nextGaussian()*std)+mean+ offset;
            }while(rand <= min || rand >= max);
        }else{
            do{
                rand= (random.nextGaussian()*std)+mean+ offset;
            }while(rand < min || rand > max);
        }
        return (int)rand;
    }
    public double getGaussianRangeDouble(double min, double max, boolean inclusive){
        if(min == max) return min;
        if(min > max){
            double tmp = max;
            max = min;
            min = tmp;
        }
        double mean = ((max-min)/2)+ min;
        double std = (max-mean)/3; //most numbers will be within 3 standard devaitions.
        double rand;
        if(inclusive){
            do{
                rand = (random.nextGaussian()*std)+mean;
            }while(rand <= min || rand >= max);
        }else{
            do{
                rand = (random.nextGaussian()*std)+mean;
            }while(rand < min || rand > max);
        }
        return rand;
    }
    public double getGaussianRangeDouble(double offset, double min, double max, boolean inclusive) throws OffSetOutOfRangeException{
        if(min == max) return min;
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
        if(inclusive){
            do{
                rand = (random.nextGaussian()*std)+mean+offset;
            }while(rand <= min || rand >= max);
        }else{
            do{
                rand = (random.nextGaussian()*std)+mean+offset;
            }while(rand < min || rand > max);
        }
        return rand;
    }
    
    public int getMaleFaithfulness(){return maleFaithfulness;}
    public int getFemaleFaithfulness(){return femaleFaithfulness;}
    public int getMaleWant(){return maleWant;}
    public int getFemaleWant(){return femaleWant;}
    public int getFemaleSelectivity(){return femaleSelectivity;}
    public int getMaleSelectivity(){return maleSelectivity;}
    public void setFemaleSelectivity(int a){
        if(a >=0 && a <= selectivityMax){
            femaleSelectivity = a;
        }
    }
    public void setMaleSelectivity(int a){
        if(a >=0 && a <= selectivityMax){
            maleSelectivity = a;
        }
    }
    public void setMaleFaithfulness(int a){
        if(a >=0 && a <= faithfulnessMax){
            maleFaithfulness = a;
        }
    }
    public void setFemaleFaithfulness(int a){
        if(a >=0 && a <= faithfulnessMax){
            femaleFaithfulness = a;
        }
    }
    public void setMaleWant(int a){
        if(a >=0 && a <= wantMax){
            maleWant = a;
        }
    }
    public void setFemaleWant(int a){
        if(a >=0 && a <= wantMax){
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
        network = new RelationshipNetwork();
        logger = new HIVLogger();
    }
    private AgentProfile getPersonalityProfile(){
        boolean female;
        int offsetF;
        int offsetW;
        int offsetS;
        double condomUse;
        int want;
        int offsetCondom = (int)((percentCondomUse * 100)- 50);
        int faithfulness;
        int selectivity;
        byte orientation;
        
        female = random.nextBoolean();
        if(female){
            offsetF = femaleFaithfulness -(faithfulnessMax/2);
            offsetW = femaleWant - (wantMax/2);
            offsetS = femaleSelectivity - (selectivityMax/2);
        }else{
            offsetF = maleFaithfulness -(faithfulnessMax/2);
            offsetW = maleWant - (wantMax/2);
            offsetS = maleSelectivity - (selectivityMax/2);
        }
        try{
            faithfulness = getGaussianRange(offsetF, 0, faithfulnessMax, true);// non-inclusive of max and min
        }catch(OffSetOutOfRangeException e){
            System.err.println("OffsetFaithfulness");
            faithfulness = getGaussianRange(0, faithfulnessMax, true);
        }
        try{
            want = getGaussianRange(offsetW,0, wantMax, true);// non-inclusive of max and min
        }catch(OffSetOutOfRangeException e){
            System.err.println("OffsetWant");
            want = getGaussianRange(0, wantMax, true);
        }
        try{
            condomUse = getGaussianRange(offsetCondom, 0, 100, true)/100.0;
        }catch(OffSetOutOfRangeException e){
            System.err.println("CondomUse offset");
            condomUse = getGaussianRange(0,100, true)/100.0;
        }
        try{
            selectivity = getGaussianRange(offsetS, 0, selectivityMax, true);
        }catch(OffSetOutOfRangeException e){
            System.err.println("Selectivity offset");
            selectivity = getGaussianRange(0,selectivityMax, true);
        }
        orientation = NeighborhoodTemplates.getSexuality(this);
        return(new AgentProfile(female, faithfulness, condomUse, want, orientation, selectivity));
    }
    
    public Agent createNewAgent(Pregnancy p){
        AgentProfile profile = getPersonalityProfile();
        int offsetLife = averageLifeSpan -(int)((averageLifeSpan +(.5*averageLifeSpan))/2); //consider setting this in start.
        Stoppable stopper;
        int life;
        Agent agent;
        try{
            life = getGaussianRange(offsetLife, 0, (int)((averageLifeSpan +(.5*averageLifeSpan))), false);
        }catch(OffSetOutOfRangeException e){
            System.err.println("Life Expectancy offset out of bounds!");
            life = averageLifeSpan;
        }
        if(profile.female){
            agent = new Female(currentID, profile.faithfulness, profile.condomUse, profile.want, 0, p.getCCR51(), p.getCCR52(),p.getCCR21(), p.getCCR22(),
                    p.getHLAA1(), p.getHLAA2(), p.getHLAB1(), p.getHLAB2(), p.getHLAC1(),p.getHLAC2(), 0, life, 
                    profile.orientation, p.getMother(), p.getFather(), p.getRace(), p.getReligion(), p.getOther(), profile.selectivity);
        }else{
            agent = new Male(currentID, profile.faithfulness, profile.condomUse, profile.want, 0, p.getCCR51(), p.getCCR52(),p.getCCR21(), p.getCCR22(),
                    p.getHLAA1(), p.getHLAA2(), p.getHLAB1(), p.getHLAB2(), p.getHLAC1(),p.getHLAC2(), 0, life, 
                    profile.orientation, p.getMother(), p.getFather(), random.nextBoolean(), random.nextBoolean(), p.getRace(), 
                    p.getReligion(), p.getOther(), profile.selectivity);
        }
        logger.insertBirth(agent);
        currentID++;
        //add to grids
        agents.setObjectLocation(agent,random.nextInt(gridWidth), random.nextInt(gridHeight));
        network.network.addNode(agent); // handles the network only! Display of nodes handled by continuous 2D
        //add to schedule
        stopper = schedule.scheduleRepeating(agent);
        //set stoppable
        agent.setStoppable(stopper);
        return agent;
    }
    
    public Agent[] createNewAgents(int n){
        Agent[] s = new Agent[numAgents];
        //create the agents
        //set offsets
        int maxLife = averageLifeSpan + (int)(averageLifeSpan*.5);
        
        //currently used variables
        AgentProfile profile;
        GeneProfile gprofile;
                
        double lack;
        int age;
        int ageOffset = -((maxLife/2) - averageAge); //should be negative. 
        int life;
        int offsetLife;
        
        Stoppable stopper;
        for(int i=0; i<numAgents; i++){
            //build agent.
            profile = getPersonalityProfile();
            
            try{
  ///////////////////Note that this is a bad fit 2* averageLifeSpan is not a good range and may result in an older population than
                //desired. 
                age = getGaussianRange(ageOffset, 0 , maxLife, false);
                //System.out.println("DEBUG: Age: " + age);
            }catch(OffSetOutOfRangeException e){
                System.err.println("Age offset out of bounds");
                age = getGaussianRange(0, maxLife, false);
            }
            
            if(age>=averageLifeSpan){
                offsetLife = -(((maxLife-age)/2)-1);
            }else{
                offsetLife = -((((maxLife-age)/2)+age)-averageLifeSpan);
            }
            
            try{
                life = getGaussianRange(offsetLife, age, maxLife, false);
            }catch(OffSetOutOfRangeException e){
                System.err.println("Life Expectancy offset out of bounds!");
                life = getGaussianRange(age, maxLife, false);
            }
                                   
            lack = random.nextDouble()*10;//random number from 0-10 (inclusive)
            Agent agent; 
            Neighborhood race = NeighborhoodTemplates.getRace(this);
            gprofile = Gene.getGeneProfile(this, race.ID);
            if(profile.female){
                agent = new Female(i, profile.faithfulness, profile.condomUse, profile.want, lack, gprofile.ccr51, gprofile.ccr52,
                        gprofile.ccr21, gprofile.ccr22, gprofile.HLA_A1, gprofile.HLA_A2, 
                        gprofile.HLA_B1, gprofile.HLA_B2, gprofile.HLA_C1, gprofile.HLA_C2, age, life, profile.orientation, -1, -1,
                        race, NeighborhoodTemplates.getReligion(this), 
                        NeighborhoodTemplates.getOther(this), profile.selectivity);
            }else{
                agent = new Male(i, profile.faithfulness, profile.condomUse, profile.want, lack, gprofile.ccr51, gprofile.ccr52,
                        gprofile.ccr21, gprofile.ccr22, gprofile.HLA_A1, gprofile.HLA_A2, 
                        gprofile.HLA_B1, gprofile.HLA_B2, gprofile.HLA_C1, gprofile.HLA_C2, age, life, profile.orientation, 
                        -1, -1, random.nextBoolean(), random.nextBoolean(), race, 
                        NeighborhoodTemplates.getReligion(this), NeighborhoodTemplates.getOther(this), profile.selectivity);
            }
            agents.setObjectLocation(agent,random.nextInt(gridWidth), random.nextInt(gridHeight));
            if(age >= 216){
                //only adding agents that are old enough, agents will add themselves as they age. 
               network.network.addNode(agent); 
            }
            stopper = schedule.scheduleRepeating(Schedule.EPOCH, 1, agent);
            agent.setStoppable(stopper);
            s[i] = agent;
        }
        return s;
    }
    public int attemptFindConnection(Agent me, Bag s){
        double rollD;
        int connectID;
        Agent connect;
        Relationship edge;
        int bagSize = s.numObjs;
        int edgeVal;
        rollD = random.nextDouble()*10;
        int tries = 0;
        do{//repeat until we find a connection of suitable age and gender.
            connectID = random.nextInt(bagSize);
            connect = (Agent)s.objs[connectID];
            tries++; //is this the cause of the program freezing...? 
        }while((!connect.acceptGender(me.isFemale()) || !me.acceptGender(connect.isFemale()) || me.isRelated(connect) || me.hasEdge(connect.ID)) && tries <= 10);
        if(tries == 11) return 0;
        //-- newly added-- just because they are "seeking" doesn't mean that they don't need to "want" the other. 
        if(!me.wantsConnection(getGaussianRangeDouble(-faithfulnessMax,faithfulnessMax, false),random.nextInt(selectivityRoll), connect))return 0;
        if(connect.wantsConnection(getGaussianRangeDouble(-faithfulnessMax,faithfulnessMax, false),random.nextInt(selectivityRoll), me)){ //using this function so we can add more advanced code in there later
            //this one is selected.
            //make sure this relationship doesn't already exist...
            if(connect.getWantLevel() > me.getWantLevel()){
                edgeVal = ((connect.getWantLevel()-me.getWantLevel())/2) + me.getWantLevel();
            }else{
                edgeVal = ((me.getWantLevel()-connect.getWantLevel())/2) + connect.getWantLevel();
            }
            int relationship = Relationship.RELATIONSHIP;
            int ii;
            ii = getGaussianRange(0,faithfulnessMax,false);
            if(ii > me.getFaithfulness() || ii > connect.getFaithfulness()){
                relationship = Relationship.ONETIME;
                edgeVal = oneShotValue;
            }

            if(me.isFemale() ^ connect.isFemale()){
                edge = new Relationship(relationship, me, connect, edgeVal);
            }else{
                if(me.isFemale()){ //F2F
                    edge = new Relationship(relationship, me, connect, edgeVal, Relationship.F2F);
                }else{//M2M
                    //DEFINE ONTOP
                    Male m = (Male)me;
                    Male o = (Male)connect;
                    boolean meOT; //since I am a and the last variable is "me ontop"
                    if(m.onTop){
                        if(o.onTop){
                            meOT = random.nextBoolean();
                        }else{
                            meOT = true;
                        }
                    }else{
                        if(o.onTop){
                            meOT = false;
                        }else{
                            meOT = random.nextBoolean();
                        }
                    }
                    edge = new Relationship(relationship, me, connect, edgeVal, Relationship.M2M, meOT);
                }
            }
            
            connect.addEdge(edge);
            me.addEdge(edge);
            network.wrapperAddEdge(edge);
            return edge.getType();
        }
        return 0;
    }
    public void processNetworks(){
        //double adj = wantLevel;
        Agent a, b;
        Relationship r;
        int PFC; //protection-free coitis
        double PFCRoll;
        LinkedList relationships = network.relationshipList;
        forEachRelationship:
        for (Object o : relationships) {
            r = (Relationship)o;
            a = r.getA();
            b = r.getB();
            if (r.getType() == Relationship.RELATIONSHIP && !a.isMarried() && !b.isMarried()){
                //give the relationship a chance to advance. 
                int ii = getGaussianRange(0, faithfulnessMax, false);
                    if(ii < a.getFaithfulness() && ii < b.getFaithfulness()){
                        r.setType(Relationship.MARRIAGE);
                        a.setRelationshipLevel((byte)Relationship.MARRIAGE);
                        b.setRelationshipLevel((byte)Relationship.MARRIAGE);
                    }
            }
            
            ///***Find PFC Unprotected - Coital Frequency - Maybe this should have been UFC..?***////
            
            //Condom use section is currently in "bandaid mode". I might add some additional code for marriage where 
            //they are trying to get pregnant or something. For now die hard supporters or non-supporters of condom use
            //win out or (if in the same partnership) war it out. 50-50. Marriage reduces the likelihood of condom usage, but
            //this ignores things like trying to get pregnant in which case they wouldn't use them at all
            //it also does not take into account knowledge of their disease status (not yet integrated into the system).
            if(a.getCondomUse() == 0 || b.getCondomUse() == 0){
                //at least one is die hard against condom use...
                if(a.getCondomUse() == 1 || b.getCondomUse() == 1){
                    //if one of them is die hard on condom use -- might need to check this before starting the relationship.
                    //giving them an all or nothing for this encounter.
                    //adding to lack for "unhappiness factor" for the losing side.
                   if(random.nextBoolean()){
                       PFC = r.getCoitalFrequency();
                       if(a.getCondomUse() == 1){
                           a.adjustLack(1);
                       }else{
                           b.adjustLack(1);
                       }
                   } else{
                       if(a.getCondomUse() == 0){
                           a.adjustLack(1);
                       }else{
                           b.adjustLack(1);
                       }
                       PFC = 0;
                   }
                }else{
                    PFC = r.getCoitalFrequency();
                }
            }else{
                if(a.getCondomUse() == 1 || b.getCondomUse() == 1){
                    PFC = 0;
                }else{
                    PFCRoll = ((a.getCondomUse()+b.getCondomUse())/2);
                    switch(r.getType()){
                        case Relationship.MARRIAGE: // less likely to use condoms
                            PFCRoll += getGaussianRangeDouble(-.25, 0, true) ;//average of partners + a random + or - between .25;
                            if(PFCRoll < 0) PFCRoll = 0;
                            if(PFCRoll > 1) PFCRoll = 1;
                            PFCRoll = 1-PFCRoll; // switch this around to likelihood of NOT using vs Using condoms. 
                            PFC = (int)(PFCRoll * r.getCoitalFrequency());//PFCRoll is the percentage of time using condoms PFC is the number of times without using condoms - Java rounds down.
                            break;
                        case Relationship.RELATIONSHIP:
                            PFCRoll += getGaussianRangeDouble(-.25, .25, true);//average of partners + a random + or - between .25;
                            if(PFCRoll < 0) PFCRoll = 0;
                            if(PFCRoll > 1) PFCRoll = 1;
                            PFCRoll = 1-PFCRoll; // switch this around to likelihood of NOT using vs Using condoms. 
                            PFC = (int)(PFCRoll * r.getCoitalFrequency());//PFCRoll is the percentage of time using condoms PFC is the number of times without using condoms - Java rounds down.
                            break;
                        default: //one shot - more likely to use condoms
                            PFCRoll += getGaussianRangeDouble(0, .25, true);//average of partners + a random + or - between .25;
                            //because java rounds down x<1 results in 0 PFC, thus we use the halfway mark and simply assign the single action. 
                            if(PFCRoll < .5) PFC = 1;
                            else PFC = 0; 
                    }
                }
            }
            if(PFC >0){
                //pregnancy
                a.addAlloImmunity(b, PFC);
                b.addAlloImmunity(a, PFC);
                if(r.orientation == Relationship.HETERO){
                    if(a.isFemale() && a.getAge() <= 480){
                        Female c = (Female)a;
                        c.attemptPregnancy(PFC, b, this);
                    }else{
                        if(b.isFemale() && b.getAge() <= 480){
                            Female c = (Female)b;
                            c.attemptPregnancy(PFC, a, this);
                        }
                    }
                }
                if(a.isInfected()){//a attempts to infect b.
                    infectHelper(a, b, PFC, r);
                }
                if(b.isInfected()){//b attempts to infect a.
                    infectHelper(b, a, PFC, r);
                }

            }
            
        }
    }
    private void infectHelper(Agent a, Agent b, int PFC, Relationship r){
        //A is the infected agent. 
        boolean newInfect = true;
                    int roll;
                    ArrayList<HIVInfection> otherInfections = a.getDiseaseMatrix().getGenotypes(); 
                    HIVInfection infection;
                    //if the other has more than one genotype, select one, otherwise use that one. 
                    if(otherInfections.size() >1){
                        //set mean of 0 with max range of list size. This makes you most likely to select an item closer to 0 or with larger virulence. 
                        roll = Math.abs(getGaussianRange(-(otherInfections.size()-1), (otherInfections.size()-1), true));
                        infection = otherInfections.get(roll);
                    }else{
                        infection = otherInfections.get(0);
                    }
                    
                    //attempt infection 
                    if(b.attemptCoitalInfection(this, infection, a.getDiseaseMatrix().getStage(), PFC, a, 1.0, r)){
                        //We've been infected!
                        if(b.isInfected())newInfect = false;
                        if(b.infect(genotypeList.get(infection.getGenotype()))) {
                            if(a.isFemale() == b.isFemale()){
                                logger.insertInfection(HIVLogger.INFECT_HOMO, b.ID, a.ID, infection.getGenotype(), newInfect, r.getType());
                            }else{
                                logger.insertInfection(HIVLogger.INFECT_HETERO, b.ID, a.ID, infection.getGenotype(), newInfect, r.getType());
                            }
                        }
                    }
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
        
        setupGenoTypeList();
        agents = new SparseGrid2D(gridWidth, gridHeight);
        network = new RelationshipNetwork();
        currentID = numAgents;
                
        Agent[] s = createNewAgents(numAgents);
        Bag na = network.network.getAllNodes();
        Agent me;
        //generate initial network.
        for (Object ome : na) {
            me = (Agent) ome;
            attemptFindConnection(me, na);
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
                Bag allAgents = network.network.allNodes; // all sexual active not dead agents. 
                for(Object o : allAgents){
                    agent = (Agent) o;
                    //remove links
                    agent.removeOneShots(state);
                    if(agent.getNetwork().size()>0 && agent.getFaithfulness() != 10){// if < =0 no network.
                        diff = Math.abs(agent.getNetworkLevel()- agent.getWantLevel());// getting the difference between their wants and what's provided.
                        diff +=(agent.getLack()/agent.getFaithfulness());
                        ii = random.nextInt(agent.getNetwork().size());
                        e = agent.getNetwork().get(ii);
                        try{
                            roll = getGaussianRangeDouble(agent.getFaithfulness()-(faithfulnessMax/2), 0,faithfulnessMax, true)*e.getType();
                        }catch(OffSetOutOfRangeException except){
                            System.err.println("Network Step, Agent Faithfulness");
                            roll = getGaussianRangeDouble(0,faithfulnessMax, true) * e.getType();
                        }
                        if(roll < diff){
                            //disolve
                            e.getA().removeEdge(e);
                            e.getB().removeEdge(e);
                            network.wrapperRemoveEdge(e);
                        }
                    }
                    //set up new relationships
                    //if they are in a relationship already (note that one shots are gone) they must WANT a new relationship, otherwise
                    //if they have no relationship let them try to find one. 
                    if((agent.getNetworkSize() > 0 && agent.wantsConnection(getGaussianRangeDouble(-faithfulnessMax,faithfulnessMax, false))) 
                            || agent.getNetworkSize() == 0){
                        int get;
                        if(agent.getFaithfulness() < 2) get = (int)agent.getLack();
                        else get = (int)agent.getLack()/(agent.getFaithfulness()/2);
                        for( int i = 0; i< get; i++){
                            if(attemptFindConnection(agent, allAgents) == 2) break;
                        }
                    }
                }//end for
            }//end step
        };
        schedule.scheduleRepeating(Schedule.EPOCH, 3, n);
        /*this handles what used to be handled by each agent-- it steps through all relationships and handles attempting to infect them
        *also handles pregnancy -- this was somehow a little simpler than going through the agent (because of pregnancy)
        *This keeps us from having to duplicate relationship changes between males and females -- which was causing issues... >.>;
        */
        n = new Steppable(){
             private static final long serialVersionUID = 1;
             @Override
             public void step(SimState state) {
                 processNetworks(); // to keep the "start" method cleaner. 
             }
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

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
import hivMicroSim.Agent.Gene;
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
    public int maleFaithfulness = 5; //0-10
    public int femaleFaithfulness = 6; // 0-10
    public int maleWant = 6; // 0-10
    public int femaleWant = 5; // 0-10
    //homosexuality and bisexuality will go here... right now.... I really can't say how this is best coded... 
    //Gene prevelence = the prevelence of the alleles- this makes it a little easier to assign alleles. so 0-1 the sum of all versions of the alleles can't be > 1
    
    
    /*http://www.k-state.edu/parasitology/biology198/answers1.html 
    * Hardy-Weinberg Law - p^2 + 2pq + q^2 = 1 and p + q = 1; 
    */
    //////****Transmission LIKEINESS FACTORS****//////
    //http://www.cdc.gov/hiv/policies/law/risk.html last updated 11-16-2015
    public double femaleLikelinessFactor = 1.5; //the increased likelihood of females to aquire the virus. 
    public double circumcisionLikelinessFactor = .7;
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
            }while(rand < min || rand > max);
        }else{
            do{
                rand= (random.nextGaussian()*std)+mean;
            }while(rand <= min || rand >= max);
        }
        return (int)rand;
    }
    public int getGaussianRange(int offset, int min, int max, boolean inclusive) throws OffSetOutOfRangeException{
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
            }while(rand < min || rand > max);
        }else{
            do{
                rand= (random.nextGaussian()*std)+mean+ offset;
            }while(rand <= min || rand >= max);
        }
        return (int)rand;
    }
    public double getGaussianRangeDouble(double min, double max, boolean inclusive){
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
            }while(rand < min || rand > max);
        }else{
            do{
                rand = (random.nextGaussian()*std)+mean;
            }while(rand <= min || rand >= max);
        }
        return rand;
    }
    public double getGaussianRangeDouble(double offset, double min, double max, boolean inclusive) throws OffSetOutOfRangeException{
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
            }while(rand < min || rand > max);
        }else{
            do{
                rand = (random.nextGaussian()*std)+mean+offset;
            }while(rand <= min || rand >= max);
        }
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
    
    public Agent createNewAgent(Pregnancy p){
        boolean female;
        int offsetF;
        int offsetW;
        double condomUse;
        int want;
        int offsetCondom = (int)((percentCondomUse * 100)- 50);
        int faithfulness;
        int life;
        
        int offsetLife = averageLifeSpan -(int)((averageLifeSpan +(.5*averageLifeSpan))/2); //consider setting this in start.
        
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

            faithfulness = getGaussianRange(offsetF, 0, 10, true);// non-inclusive of max and min
        }catch(OffSetOutOfRangeException e){
            System.err.println("OffsetFaithfulness");
            faithfulness = getGaussianRange(0, 10, true);
        }
        try{
            want = getGaussianRange(offsetW,0, 10, true);// non-inclusive of max and min
        }catch(OffSetOutOfRangeException e){
            System.err.println("OffsetWant");
            want = getGaussianRange(0, 10, true);
        }
        try{
            condomUse = getGaussianRange(offsetCondom, 0, 100, true)/100.0;
        }catch(OffSetOutOfRangeException e){
            System.err.println("CondomUse offset");
            condomUse = getGaussianRange(0,100, true)/100.0;
        }
        try{
            life = getGaussianRange(offsetLife, 0, (int)((averageLifeSpan +(.5*averageLifeSpan))), false);
        }catch(OffSetOutOfRangeException e){
            System.err.println("Life Expectancy offset out of bounds!");
            life = averageLifeSpan;
        }
        Agent agent;
        //currently orientation will be coded to straight only, all men listed as "onTop" and circumsized for now.
        if(female){
            agent = new Female(currentID, faithfulness, condomUse, want, 0, p.getCCR51(), p.getCCR52(),p.getCCR21(), p.getCCR22(), p.getHLAA1(), p.getHLAA2(), p.getHLAB1(), p.getHLAB2(), p.getHLAC1(),p.getHLAC2(), 0, life, Agent.ORIENTATION_HETEROSEXUAL, p.getMother(), p.getFather());
        }else{
            agent = new Male(currentID, faithfulness, condomUse, want, 0, p.getCCR51(), p.getCCR52(),p.getCCR21(), p.getCCR22(), p.getHLAA1(), p.getHLAA2(), p.getHLAB1(), p.getHLAB2(), p.getHLAC1(),p.getHLAC2(), 0, life, Agent.ORIENTATION_HETEROSEXUAL, p.getMother(), p.getFather(), true, true);
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
        int offsetMF = maleFaithfulness -5;
        int offsetFF = femaleFaithfulness -5;
        int offsetMW = maleWant - 5;
        int offsetFW = femaleWant - 5;
        int maxLife = averageLifeSpan + (int)(averageLifeSpan*.5);
        int offsetCondom = (int)((percentCondomUse * 100)- 50);
        
        //currently used variables
        int faithfulness;
        double condomUse;
        int want;
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
                    faithfulness = getGaussianRange(offsetFF, 0, 10, true);
                }else{
                    faithfulness = getGaussianRange(offsetMF, 0, 10, true);
                }
            }catch(OffSetOutOfRangeException e){
                System.err.println("OffsetFaithfulness");
                faithfulness = getGaussianRange(0, 10, true);
            }
            try{
                if(female){
                    want = getGaussianRange(offsetFW, 0, 10, true);
                }else{
                    want = getGaussianRange(offsetMW, 0, 10, true);
                }
            }catch(OffSetOutOfRangeException e){
                System.err.println("OffsetWant");
                want = getGaussianRange(0, 10, true);
            }
            try{
                condomUse = getGaussianRange(offsetCondom, 0, 100, true)/100.0;
            }catch(OffSetOutOfRangeException e){
                System.err.println("CondomUse offset");
                condomUse = getGaussianRange(0,100, true)/100.0;
            }
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
            geneRoll = random.nextDouble(); // next double between 0 and 1(exclusive)
            ccr51 = Gene.getCCR5(geneRoll);
            geneRoll = random.nextDouble(); // next double between 0 and 1(exclusive)
            ccr52 = Gene.getCCR5(geneRoll);
            geneRoll = random.nextDouble(); // next double between 0 and 1(exclusive)
            ccr21 = Gene.getCCR2(geneRoll);
            geneRoll = random.nextDouble(); // next double between 0 and 1(exclusive)
            ccr22 = Gene.getCCR2(geneRoll);
            geneRoll = random.nextDouble(); // next double between 0 and 1(exclusive)
            HLA_A1 = Gene.getHLA_A(geneRoll);
            geneRoll = random.nextDouble(); // next double between 0 and 1(exclusive)
            HLA_A2 = Gene.getHLA_A(geneRoll);
            geneRoll = random.nextDouble(); // next double between 0 and 1(exclusive)
            HLA_B1 = Gene.getHLA_B(geneRoll);
            geneRoll = random.nextDouble(); // next double between 0 and 1(exclusive)
            HLA_B2 = Gene.getHLA_B(geneRoll);
            geneRoll = random.nextDouble(); // next double between 0 and 1(exclusive)
            HLA_C1 = Gene.getHLA_C(geneRoll);
            geneRoll = random.nextDouble(); // next double between 0 and 1(exclusive)
            HLA_C2 = Gene.getHLA_C(geneRoll);
            
            lack = random.nextDouble()*10;//random number from 0-10 (inclusive)
            
            Agent agent; 
            if(female){
                agent = new Female(i, faithfulness, condomUse, want, lack, ccr51, ccr52, ccr21, ccr22, HLA_A1, HLA_A2, HLA_B1, HLA_B2, HLA_C1, HLA_C2, age, life, Agent.ORIENTATION_HETEROSEXUAL, -1, -1);
            }else{
                agent = new Male(i, faithfulness, condomUse, want, lack, ccr51, ccr52, ccr21, ccr22, HLA_A1, HLA_A2, HLA_B1, HLA_B2, HLA_C1, HLA_C2, age, life, Agent.ORIENTATION_HETEROSEXUAL, -1, -1, false, true);
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
    public void attemptFindConnection(Agent me, Bag s){
        double rollD;
        int connectID;
        Agent connect;
        Relationship edge;
        int bagSize = s.numObjs;
        int edgeVal;
        rollD = random.nextDouble()*10;
        do{//repeat until we find a connection of suitable age and gender.
            connectID = random.nextInt(bagSize);
            connect = (Agent)s.objs[connectID];
        }while(!connect.acceptGender(me.isFemale()) || !me.acceptGender(connect.isFemale()) || me.isRelated(connect));
        //now that we have an agent, see if their network has space for another edge.
        if(me.hasEdge(connect.ID)) return;
        if(connect.wantsConnection(getGaussianRangeDouble(-10,10, false))){ //using this function so we can add more advanced code in there later
            //this one is selected.
            //make sure this relationship doesn't already exist...
            if(connect.getWantLevel() > me.getWantLevel()){
                edgeVal = ((connect.getWantLevel()-me.getWantLevel())/2) + me.getWantLevel();
            }else{
                edgeVal = ((me.getWantLevel()-connect.getWantLevel())/2) + connect.getWantLevel();
            }
            int relationship = Relationship.RELATIONSHIP;
            int ii;
            ii = random.nextInt(10);
            if(ii > me.getFaithfulness() || ii > connect.getFaithfulness()){
                relationship = Relationship.ONETIME;
                edgeVal = 1;
            }
//            if(!me.isMarried() && !connect.isMarried()){
//                 ii = random.nextInt(10);
//                if(ii < me.getFaithfulness() && ii < connect.getFaithfulness()){
//                    relationship =Relationship.MARRIAGE;
//                }else{
//                    if(ii > me.getFaithfulness() || ii > connect.getFaithfulness()){
//                        relationship = Relationship.ONETIME;
//                        edgeVal = 1;
//                    }
//                }
//            }else{
//                ii = random.nextInt(10);
//                if(ii > me.getFaithfulness() && ii > connect.getFaithfulness()){
//                    relationship = Relationship.ONETIME;
//                    edgeVal = 1;
//                }
//            }
            edge = new Relationship(relationship, me, connect, edgeVal);
            connect.addEdge(edge);
            me.addEdge(edge);
            network.wrapperAddEdge(edge);
        }
        
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
            a.adjustLack(-r.getCoitalFrequency());
            b.adjustLack(-r.getCoitalFrequency());
            if (r.getType() == Relationship.RELATIONSHIP && !a.isMarried() && !b.isMarried()){
                //give the relationship a chance to advance. 
                int ii = random.nextInt(10) + 1;
                    if(ii < a.getFaithfulness() && ii < b.getFaithfulness()){
                        r.setType(Relationship.MARRIAGE);
                        a.setMarried(true);
                        b.setMarried(true);
                    }
            }
            if(a.isFemale() && b.isFemale()) continue;
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
                
                if(a.isFemale() && a.getAge() <= 480){
                    Female c = (Female)a;
                    c.attemptPregnancy(PFC, b, this);
                }
                if(b.isFemale() && b.getAge() <= 480){
                    Female c = (Female)b;
                    c.attemptPregnancy(PFC, a, this);
                }
                if(a.isInfected()){//a attempts to infect b.
                    //select a genotype from the other 
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
                    if(b.attemptCoitalInfection(this, infection, a.getDiseaseMatrix().getStage(), PFC, a, 1.0)){
                        //We've been infected!
                        if(b.isInfected())newInfect = false;
                        if(b.infect(genotypeList.get(infection.getGenotype()))) {
                            logger.insertInfection(HIVLogger.INFECT_HETERO, b.ID, a.ID, infection.getGenotype(), newInfect);
                        }
                    }
                }
                if(b.isInfected()){//b attempts to infect a.
                    //select a genotype from the other 
                    boolean newInfect = true;
                    int roll;
                    ArrayList<HIVInfection> otherInfections = b.getDiseaseMatrix().getGenotypes(); 
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
                    if(a.attemptCoitalInfection(this, infection, b.getDiseaseMatrix().getStage(), PFC, b, 1.0)){
                        //We've been infected!
                        if(a.isInfected())newInfect = false;
                        if(a.infect(genotypeList.get(infection.getGenotype()))) {
                            logger.insertInfection(HIVLogger.INFECT_HETERO, a.ID, b.ID, infection.getGenotype(), newInfect);
                        }
                    }
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
                        ii = random.nextInt(agent.getNetwork().size());
                        e = agent.getNetwork().get(ii);
                        try{
                            roll = getGaussianRangeDouble(agent.getFaithfulness()-5, 0,10, true)*e.getType();
                        }catch(OffSetOutOfRangeException except){
                            System.err.println("Network Step, Agent Faithfulness");
                            roll = getGaussianRangeDouble(0,10, true) * e.getType();
                        }
                        if(roll < diff){
                            //disolve
                            e.getA().removeEdge(e);
                            e.getB().removeEdge(e);
                            network.wrapperRemoveEdge(e);
                        }
                    }
                    //set up new relationships
                    if(agent.wantsConnection(getGaussianRangeDouble(-10,10, false))){
                        attemptFindConnection(agent, allAgents);
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

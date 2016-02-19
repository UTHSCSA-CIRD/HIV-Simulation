/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.Agent;
import hivMicroSim.AlloImmunity;
import hivMicroSim.HIV.DiseaseMatrix;
import hivMicroSim.HIV.Genotype;
import hivMicroSim.HIV.HIVInfection;
import hivMicroSim.HIVLogger;
import hivMicroSim.HIVMicroSim;
import hivMicroSim.Infection;
import hivMicroSim.Relationship;
import hivMicroSim.SeroImmunity;
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
    protected final int faithfulness; // between 1 and 10
    protected double condomUse; //between 0 and 1 inclusive
    protected final int wantLevel;
    protected double networkLevel;
    protected double lack; //lack of fulfillment in current relationships
    public boolean alive = true;
    protected boolean married;
    //public final int race;
        public static final int RACE_CAUCASIAN = 0;
        public static final int RACE_HISPANIC = 1;
        public static final int RACE_BLACK = 2;
        public static final int RACE_ASIAN = 3;
        public static final int RACE_MIXED = 4;
    private final byte orientation;
        public static final byte ORIENTATION_HETEROSEXUAL = 0;
        public static final byte ORIENTATION_BISEXUAL = 1;
        public static final byte ORIENTATION_HOMOSEXUAL = 2;
    private final int mother;
    private final int father;
    
    //Genetic factors
    public final byte ccr51, ccr52;//first and second allele ccr5 one is randomly chosen to be passed on.
    public final byte ccr21, ccr22;
    public final byte HLA_A1, HLA_A2;
    public final byte HLA_B1, HLA_B2;
    public final byte HLA_C1, HLA_C2;
    
    //Adaptable Immunity
    private final ArrayList<SeroImmunity> seroImmunity;
    private final ArrayList<AlloImmunity> alloImmunity;
    
    //General Status
    protected boolean infected;
    protected DiseaseMatrix hiv = null;
    protected final ArrayList<Infection> infections;
    protected int age; // measured in months/ ticks. 
    protected int life; // cheap way to say- how long this person should live without HIV/AIDS. This should probably
    //be changed by a statistician to use Gompertz or some other algorithm/source, but for now it is assigned using a
    //gaussian distribution from 0 to life expectancy + .5(life expectancy) offset to mean of life expectancly.
    //This ignores infant morality as does Gompertz (since it seems to best model rate of death after a certain age... and potentially
    //also up to a certain age.) -- again. statistician. ^.^;; 
    protected Color col = Color.gray;
    protected double width = 1;
    protected double height = 1;
    protected Stoppable stopper;
    
    //infection modes
    public static final int MODEHETEROCOITIS = 1;
    public static final int MODEMOTHERCHILD = 2;
    
    public void setStoppable(Stoppable stop){
        stopper = stop;
    }
    
    public boolean acceptGender(boolean otherFemale){
        if(orientation == ORIENTATION_BISEXUAL) return true;
        if(isFemale()){
            if(otherFemale){
                if(orientation == ORIENTATION_HOMOSEXUAL)return true;
            }else{
                if(orientation == ORIENTATION_HETEROSEXUAL)return true;
            }
        }else{
            if(otherFemale){
                if(orientation == ORIENTATION_HETEROSEXUAL)return true;
            }else{
                if(orientation == ORIENTATION_HOMOSEXUAL)return true;
            }
        }
        return false;
    }
    public boolean isMarried(){return married;}
    protected final ArrayList<Relationship> network;
    
    public Agent(int id, int faithfullness, double condomUse, int wantLevel, double lack, byte ccr51, byte ccr52, byte ccr21, byte ccr22,byte HLAA1, byte HLAA2,
            byte HLAB1, byte HLAB2, byte HLAC1, byte HLAC2, int age, int life, byte orientation, int mother, int father){
        ID = id;
        faithfulness = faithfullness;// because the programmer can't spell... 
        this.condomUse = condomUse;
        this.wantLevel = wantLevel;
        this.lack = lack;
        this.ccr51 = ccr51;
        this.ccr52 = ccr52;
        this.ccr21 = ccr21;
        this.ccr22 = ccr22;
        this.orientation = orientation;
        this.mother = mother;
        this.father = father; 
        HLA_A1 = HLAA1; HLA_A2 = HLAA2; HLA_B1 = HLAB1; HLA_B2 = HLAB2; HLA_C1 = HLAC1; HLA_C2 = HLAC2;
        
        seroImmunity = new ArrayList<>();
        alloImmunity = new ArrayList<>();
        infections = new ArrayList<>();
        this.age = age;
        this.life = life;
        
        infected = false;
        network = new ArrayList<>();
        networkLevel = 0;
    }
    
    public boolean isRelated(Agent a){
        //only checking for immediate family. cousins, aunts and uncles would require the maintaining of a geneology mapping table. 
        if(mother == -1 || father == -1) return false; //for simplicity initial agents are not related.
        if(a.isFemale()){
            if(a.ID == mother) return true;
        }else{
            if(a.ID == father) return true;
        }
        return a.mother == mother || a.father == father;
    }
    public int getFaithfulness(){
        return faithfulness;
    }
    public double getCondomUse(){return condomUse;}
    public void setCondomUse(double a){
        if(a >= 0 && a <= 1){
            condomUse = a;
        }
    }
    public int getLifeSpan(){
        return life;
    }
    public int getWantLevel(){return wantLevel;}
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
    public abstract boolean isPregnant();
    public double getCCR5SusceptibilityFactor(){
        double ret = 1;
        //delta 32
        
        if(ccr51 == Gene.CCR5D32){
            if(ccr52 == Gene.CCR5D32) return 0;
            ret *= Gene.CCR5D32Effect;
        }else if(ccr51 == Gene.CCR5HHEP1)
            ret *= Gene.CCR5HHEP1Effect;
        if(ccr52 == Gene.CCR5D32)
            ret *= Gene.CCR5D32Effect;
        else if(ccr52 == Gene.CCR5HHEP1)
            ret *= Gene.CCR5HHEP1Effect;
        
        //CCR2 - increased CCR5 resistance 
        if(ccr21 == Gene.CCR2V64I)
            ret *= Gene.CCR2V64IEffect;
        if(ccr22 == Gene.CCR2V64I)
            ret *= Gene.CCR2V64IEffect;
        return ret;
    }
    public double getHLAImmuneFactor(){
        double ret = 1;
        //look for impactful HLAs
        if(HLA_A1 == Gene.HLAA02){
            ret = ret*Gene.HLA6802Effect;
        }
        if(HLA_A2 == Gene.HLAA02){
            ret = ret*Gene.HLA6802Effect;
        }
        if(HLA_B1 == Gene.HLAB07){
            ret = ret*Gene.HLAB07Effect;
        }else if(HLA_B1 == Gene.HLAB27){
            ret = ret*Gene.HLAB27Effect;
        }else if(HLA_B1 == Gene.HLAB58){
            ret = ret*Gene.HLAB58Effect;
        }
        if(HLA_B2 == Gene.HLAB07){
            ret = ret*Gene.HLAB07Effect;
        }else if(HLA_B2 == Gene.HLAB27){
            ret = ret*Gene.HLAB27Effect;
        }else if(HLA_B2 == Gene.HLAB58){
            ret = ret*Gene.HLAB58Effect;
        }
        if(HLA_C1 == Gene.HLACw0303){
            ret = ret*Gene.HLACw0303Effect;
        }
        if(HLA_C2 == Gene.HLACw0303){
            ret = ret*Gene.HLACw0303Effect;
        }
        if(HLA_A1 == HLA_A2){
            ret *= 1.4;
        }
        if(HLA_B1 == HLA_B2){
            ret *= 1.4;
        }
        if(HLA_C1 == HLA_C2){
            ret *= 1.4;
        }
        return ret;
    }
    
    public ArrayList<SeroImmunity> getSeroImmunity(){return seroImmunity;}
    public ArrayList<AlloImmunity> getAlloImmunity(){return alloImmunity;}
    public int addSeroImmunity(int genoType, int degree){
        SeroImmunity a;
        for (SeroImmunity seroImmunity1 : seroImmunity) {
            a = seroImmunity1;
            if(a.getGenotype() == genoType){
                return a.expose(degree);
            }
        }
        //didn't find that genotype
        a = new SeroImmunity(genoType, degree);
        seroImmunity.add(a);
        return a.getResistance();
    }
    public int addAlloImmunity(Agent agent, int degree){
        //note that at some point we should work on a method to gradually reduce alloimmunity over time. 
        AlloImmunity a = null;//just doing this so it doesn't complain about initilaizing.. 
        boolean found = false;
        int missmatch = 0;
        for (int i = 0; i< alloImmunity.size(); i++) {
            a = alloImmunity.get(i);
            if(a.getAgent() == agent.ID){
                found = true;
                break;
            }
        }
        if(!found){ 
            a = new AlloImmunity(agent.ID);
            alloImmunity.add(a);
        }
        if(alloImmunity.size() > 10){ // currently an arbitrarily chosen number. Some research may be needed to see what is considered "high" and could reduce the immune response as is seen in sex workers. 
            degree = degree/2;
        }
        //now that we have HLA factors they will greatly effect the degree.
        if(HLA_A1 != agent.HLA_A1 && HLA_A1 != agent.HLA_A2) missmatch++;
        if(HLA_A2 != agent.HLA_A1 && HLA_A2 != agent.HLA_A2) missmatch++;
        if(HLA_B1 != agent.HLA_B1 && HLA_B1 != agent.HLA_B2) missmatch++;
        if(HLA_B2 != agent.HLA_B1 && HLA_B2 != agent.HLA_B2) missmatch++;
        if(HLA_C1 != agent.HLA_C1 && HLA_C1 != agent.HLA_C2) missmatch++;
        if(HLA_C2 != agent.HLA_C1 && HLA_C2 != agent.HLA_C2) missmatch++;
        //returning the degree * missmatch- higher missmatch, higher immune response. missmatch of 0 - no immune response. 
        return a.addResistance(degree * missmatch);
    }
    public int getSeroImmunity(int genoType){
        for(SeroImmunity a : seroImmunity){
            if(a.getGenotype() == genoType)
                return a.getResistance();
        }
        return 0;
    }
    public int getAlloImmunity(int agentID){
        for(AlloImmunity a : alloImmunity){
            if(a.getAgent() == agentID)
                return a.getResistance();
        }
        return 0;
    }
    
    public boolean isInfected(){
        return infected;
    }
    public boolean infect(Genotype a){
        if(a.getCCR5Dependance()){
            if(ccr51 == Gene.CCR5D32 && ccr52 == Gene.CCR5D32) return false; // in case this was called from the main method
        }
        int type = a.getGenotype();
        if(infected){
            //make sure I don't already have that genotype -- basic error checking, should never happen. 
            ArrayList<HIVInfection> mine = hiv.getGenotypes();
            if (!mine.stream().noneMatch((inf) -> (inf.getGenotype() == type))) {
                return false;
            }
        }
///////////////////Enhancment necessary- basic formula////////////////////
        double infectivity = a.getVirulence();
        double sero = (double)getSeroImmunity(type);
        if(sero > 0){
            infectivity = (infectivity+(infectivity*(sero/100.0)))/2;
        }
        
        infectivity = (infectivity+(infectivity*getHLAImmuneFactor()))/2;
        
        if(a.getCCR5Dependance()){
            infectivity = infectivity * getCCR5SusceptibilityFactor();
        }
        if(infectivity <1) infectivity = 1;
        if(infected){
            hiv.addGenoType(new HIVInfection(a, (int)infectivity));
        }else{
            infected = true;
            col = Color.red;
            hiv = new DiseaseMatrix(new HIVInfection(a, (int)infectivity));
        }
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
    public boolean wantsConnection(double roll){
        if(age <216) return false;
        if(networkLevel >= wantLevel) return false;
        if(married){
            return (roll+faithfulness) < (lack-networkLevel);
        }else{
            return((roll+faithfulness) < lack);
        }
    }
    public int getNetworkSize(){
        return network.size();
    }
    public abstract void removeOneShots(SimState state);
    public abstract boolean addEdge(Relationship a);
    public abstract boolean removeEdge(Relationship a);
    @Override
    public void step(SimState state){
        HIVMicroSim sim = (HIVMicroSim) state;
        age++;
        if(age > life){
            deathFromOtherCauses(state);
            return;
        }
        if(infected){
            if(hiv.progress(age<216?2:1)){//if young, the disease progresses more rapidly. 
                //we have progressed in the infection. 
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
                        sim.logger.insertDeath(ID, false, true);
                        //remove all relationships.
                        for(Relationship r : network){
                            r.getPartner(ID).removeEdge(r);
                            sim.network.removeEdge(r);
                        }
                        sim.network.removeNode(this);
                        networkLevel = 0;
                        network.clear();
                        alive = false;
                        stopper.stop();
                }
            }
        }
        if(age < 216){
            width = 1;
            height = 1;
            return;
        }
        width = 1.5;
        height = 1.5;
        if(age == 216){
            //add self to network.
            sim.network.addNode(this);
        }
        //adjust for network edges (note, this does not change the edges, just adds their effect and potential infection. 
        double adj = wantLevel;
        Agent other;
        int PFC; //protection-free coitis
        double PFCRoll;
        //not sure if this should be handled seperately like this-- I might end up creating another 
        //class to handle this before or after the agents have made their move and work through each
        //Relationship itself to make condom use, etc choices for that month for the couple.
        //for now it's handled by the agent. 
        forEachRelationship:
        for (Relationship network1 : network) {
            adj -= network1.getCoitalFrequency();
            other = network1.getPartner(ID);
            if(isFemale() && other.isFemale()) continue;
            //Condom use section is currently in "bandaid mode". I might add some additional code for marriage where 
            //they are trying to get pregnant or something. For now die hard supporters or non-supporters of condom use
            //win out or (if in the same partnership) war it out. 50-50. Marriage reduces the likelihood of condom usage, but
            //this ignores things like trying to get pregnant in which case they wouldn't use them at all
            //it also does not take into account knowledge of their disease status (not yet integrated into the system).
            if(condomUse == 0 || other.condomUse == 0){
                //at least one is die hard against condom use...
                if(condomUse == 10 || other.condomUse == 10){
                    //if one of them is die hard on condom use -- might need to check this before starting the relationship.
                    //giving them an all or nothing for this encounter.
                   if(sim.random.nextBoolean()){
                       PFC = network1.getCoitalFrequency();
                   } else{
                       PFC = 0;
                   }
                }else{
                    PFC = network1.getCoitalFrequency();
                }
            }else{
                if(condomUse == 10 || other.condomUse == 10){
                    PFC = 0;
                }else{
                    
                    switch(network1.getType()){
                        case Relationship.MARRIAGE: // less likely to use condoms
                            PFCRoll = (sim.getGaussianRangeDouble(-.25, 0, true) + ((other.condomUse+condomUse)/2));//average of partners + a random + or - between .25;
                            if(PFCRoll < 0) PFCRoll = 0;
                            if(PFCRoll > 1) PFCRoll = 1;
                            PFCRoll = 1-PFCRoll; // switch this around to likelihood of NOT using vs Using condoms. 
                            PFC = (int)(PFCRoll * network1.getCoitalFrequency());//PFCRoll is the percentage of time using condoms PFC is the number of times without using condoms - Java rounds down.
                            break;
                        case Relationship.RELATIONSHIP:
                            PFCRoll = (sim.getGaussianRangeDouble(-.25, .25, true) + ((other.condomUse+condomUse)/2));//average of partners + a random + or - between .25;
                            if(PFCRoll < 0) PFCRoll = 0;
                            if(PFCRoll > 1) PFCRoll = 1;
                            PFCRoll = 1-PFCRoll; // switch this around to likelihood of NOT using vs Using condoms. 
                            PFC = (int)(PFCRoll * network1.getCoitalFrequency());//PFCRoll is the percentage of time using condoms PFC is the number of times without using condoms - Java rounds down.
                            break;
                        default: //one shot - more likely to use condoms
                            PFCRoll = (sim.getGaussianRangeDouble(0, .25, true) + ((other.condomUse+condomUse)/2));//average of partners + a random + or - between .25;
                            //because java rounds down x<1 results in 0 PFC, thus we use the halfway mark and simply assign the single action. 
                            if(PFCRoll < .5) PFC = 1;
                            else PFC = 0; 
                    }
                }
            }
            if(PFC >0){
                if(!pregnant && age <= 480){
                    //attempt to become so! 
                    for(int i = 0; i< PFC; i++){
                       double pg = sim.random.nextDouble();
                        if(pg < sim.pregnancyChance){
                        //she's pregnant! 
                            sim.logger.insertConception(ID, other.ID);
                            pregnant = true;
                            boolean rand;
                            byte infCCR51;
                            byte infCCR52;
                            rand = sim.random.nextBoolean();
                            //CCR5
                            if(rand){
                                infCCR51 = ccr51;
                            }else{
                                infCCR51 = ccr52;
                            }
                            rand = sim.random.nextBoolean();
                            if(rand){
                                infCCR52 = other.ccr51;
                            }else{
                                infCCR52 = other.ccr52;
                            }
                            //CCR2
                            byte infCCR21;
                            byte infCCR22;
                            rand = sim.random.nextBoolean();
                            if(rand){
                                infCCR21 = ccr21;
                            }else{
                                infCCR21 = ccr22;
                            }
                            rand = sim.random.nextBoolean();
                            if(rand){
                                infCCR22 = other.ccr21;
                            }else{
                                infCCR22 = other.ccr22;
                            }
                            //HLA_A
                            byte infHLAA1;
                            byte infHLAA2;
                            rand = sim.random.nextBoolean();
                            if(rand){
                                infHLAA1 = HLA_A1;
                            }else{
                                infHLAA1 = HLA_A2;
                            }
                            rand = sim.random.nextBoolean();
                            if(rand){
                                infHLAA2 = other.HLA_A1;
                            }else{
                                infHLAA2 = other.HLA_A2;
                            }
                            //HLA_B
                            byte infHLAB1;
                            byte infHLAB2;
                            rand = sim.random.nextBoolean();
                            if(rand){
                                infHLAB1 = HLA_B1;
                            }else{
                                infHLAB1 = HLA_B2;
                            }
                            rand = sim.random.nextBoolean();
                            if(rand){
                                infHLAB2 = other.HLA_B1;
                            }else{
                                infHLAB2 = other.HLA_B2;
                            }
                            //HLA_C
                            byte infHLAC1;
                            byte infHLAC2;
                            rand = sim.random.nextBoolean();
                            if(rand){
                                infHLAC1 = HLA_C1;
                            }else{
                                infHLAC1 = HLA_C2;
                            }
                            rand = sim.random.nextBoolean();
                            if(rand){
                                infHLAC2 = other.HLA_C1;
                            }else{
                                infHLAC2 = other.HLA_C2;
                            }
                            pregnancy = new Pregnancy(ID, other.ID, infCCR51, infCCR52, infCCR21, infCCR22, infHLAA1, infHLAA2, infHLAB1, infHLAB2, infHLAC1, infHLAC2);
                        } 
                    }

                }
                addAlloImmunity(other, PFC);
                if(other.infected){
                    //select a genotype from the other 
                    ArrayList<HIVInfection> otherInfections = other.getDiseaseMatrix().getGenotypes(); 
                    HIVInfection infection;
                    //if the other has more than one genotype, select one, otherwise use that one. 
                    if(otherInfections.size() >1){
                        //set mean of 0 with max range of list size. This makes you most likely to select an item closer to 0 or with larger virulence. 
                        roll = Math.abs(sim.getGaussianRange(-(otherInfections.size()-1), (otherInfections.size()-1), true));
                        infection = otherInfections.get(roll);
                    }else{
                        infection = otherInfections.get(0);
                    }
                    if(infected){
                        for (HIVInfection mine : hiv.getGenotypes()) {
                            if(mine.getGenotype() == infection.getGenotype())continue forEachRelationship;
                        }
                    }
                    //attempt infection
    ///////////////////Calculated frequency of unprotected coitus. 
                    if(attemptCoitalInfection(sim, infection, other.getDiseaseMatrix().getStage(), PFC, other.ID, 1.0)){
                        //We've been infected!
                        boolean pre = !infected;
                        if(infect(sim.genotypeList.get(infection.getGenotype()))) {
                            sim.logger.insertInfection(HIVLogger.INFECT_HETERO, ID, other.ID, infection.getGenotype(), pre);
                        }
                    }
                }
            }
            
        }
        //System.out.print("DEBUG: Lack: " + lack + " want: " + wantLevel + " Network Level: " + networkLevel + " of size " + network.size() + " produced: " );
        adjustLack((adj/12));
        degradeImmunity();
        
    }
    public void degradeImmunity(){
        alloImmunity.stream().forEach((allo) -> {
            allo.degrade();
        });
        seroImmunity.stream().forEach((sero) -> {
            sero.degrade();
        });
    }
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

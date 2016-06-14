/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.Agent;
import hivMicroSim.Generator;
import hivMicroSim.HIVLogger;
import hivMicroSim.HIVMicroSim;
import hivMicroSim.Relationship;
import java.util.ArrayList;
import sim.portrayal.*;
import sim.engine.*;

import java.awt.*;
/**
 *
 * @author ManuelLS
 */
public class Female extends Agent implements Steppable{
    private static final long serialVersionUID = 1;
    private boolean pregnant;
    private Pregnancy pregnancy;
    
    public Female(int id, Personality personality, double resistance, int age, int life){
        super(id, personality, resistance, age, life);
    }
    
    public boolean makePregnant(Pregnancy p){
        if(pregnant) return false;
        pregnant = true;
        pregnancy = p;
        return true;
    }
    
    @Override
    public boolean isFemale(){return true;}
     
    @Override
    public void step(SimState state){
        HIVMicroSim sim = (HIVMicroSim) state;
        if(age >= sim.networkEntranceAge){
            if(pregnant){
                if(pregnancy.step()){
                    //a little one is born! Now.... if mom is HIV positive... 
                    Agent littleOne = Generator.generateAgent(sim, true);
                    pregnant = false;
                    pregnancy = null;
                    if(infected){
                        if(littleOne.attemptInfection(sim, hiv.getInfectivity(), Agent.MODEMOTHERCHILD)){
                            //Poor little guy was infected :( 
                            sim.logger.insertInfection(HIVLogger.INFECT_MOTHERTOCHILD, ID, littleOne.ID, 
                                    true);
                            littleOne.infect(sim);
                        }
                    }
                }
            }
        }
        
        double adj = wantLevel;
        Agent other;
        int PFC; //protection-free coitis
        double PFCRoll;
        forEachRelationship:
        for (Relationship network1 : network) {
            adj -= network1.getCoitalFrequency();
            other = network1.getMale();
            switch(network1.getType()){
                case Relationship.MARRIAGE: // less likely to use condoms
                    PFCRoll = (sim.getGaussianRangeDouble(-.25, 0) + ((other.condomUse+condomUse)/2));//average of partners + a random + or - between .25;
                    if(PFCRoll < 0) PFCRoll = 0;
                    if(PFCRoll > 1) PFCRoll = 1;
                    PFCRoll = 1-PFCRoll; // switch this around to likelihood of NOT using vs Using condoms. 
                    PFC = (int)(PFCRoll * network1.getCoitalFrequency());//PFCRoll is the percentage of time using condoms PFC is the number of times without using condoms - Java rounds down.
                    break;
                case Relationship.RELATIONSHIP:
                    PFCRoll = (sim.getGaussianRangeDouble(-.25, .25) + ((other.condomUse+condomUse)/2));//average of partners + a random + or - between .25;
                    if(PFCRoll < 0) PFCRoll = 0;
                    if(PFCRoll > 1) PFCRoll = 1;
                    PFCRoll = 1-PFCRoll; // switch this around to likelihood of NOT using vs Using condoms. 
                    PFC = (int)(PFCRoll * network1.getCoitalFrequency());//PFCRoll is the percentage of time using condoms PFC is the number of times without using condoms - Java rounds down.
                    break;
                default: //one shot - more likely to use condoms
                    PFCRoll = (sim.getGaussianRangeDouble(0, .25) + ((other.condomUse+condomUse)/2));//average of partners + a random + or - between .25;
                    //because java rounds down x<1 results in 0 PFC, thus we use the halfway mark and simply assign the single action. 
                    if(PFCRoll < .5) PFC = 1;
                    else PFC = 0; 
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
                            pregnancy = new Pregnancy(infCCR51, infCCR52, infCCR21, infCCR22, infHLAA1, infHLAA2, infHLAB1, infHLAB2, infHLAC1, infHLAC2);
                        } 
                    }

                }
                if(other.infected){
                    //select a genotype from the other 
                    ArrayList<HIVInfection> otherInfections = other.getDiseaseMatrix().getGenotypes(); 
                    HIVInfection infection;
                    //if the other has more than one genotype, select one, otherwise use that one. 
                    if(otherInfections.size() >1){
                        //set mean of 0 with max range of list size. This makes you most likely to select an item closer to 0 or with larger virulence. 
                        roll = Math.abs(sim.getGaussianRange(-(otherInfections.size()-1), (otherInfections.size()-1)));
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
        //System.out.print(" new lack: " + lack + "\n");
        super.step(state);
    }
    
    public boolean attemptCoitalInfection(HIVMicroSim sim, HIVInfection infection, int stage, int frequency, int agent, double degree){
        //this calculates the potential reduction from alloimmunity, then passes it on to attemptInfection. 
        
        //Since we're in the female class, we'll add the "female factor" to the degree since the risk of infection is higher
        //for the female in heterosexual coitus. -- later additional factors may be added for homosexual coitus for males. 
        degree = degree*sim.femaleLikelinessFactor;
////////////////////////This needs refining.
        for(int i = 0; i< frequency; i++){
            if(attemptInfection(sim, infection, stage, degree, Agent.MODEHETEROCOITIS)) return true;
        }
        return false;
    }
    
    
    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info){
        graphics.setColor(col);
        int x = (int)(info.draw.x - (info.draw.width *width) / 2.0);
        int y = (int)(info.draw.y - (info.draw.height*height) / 2.0);
        int w = (int)((info.draw.width) * width);
        int h = (int)((info.draw.height) * height);
        
        graphics.fillOval(x,y,w, h);
    }
}

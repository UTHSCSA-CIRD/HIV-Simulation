/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.Agent;
import Neighborhoods.Neighborhood;
import Neighborhoods.NeighborhoodTemplates;
import hivMicroSim.HIV.HIVInfection;
import hivMicroSim.HIVLogger;
import hivMicroSim.HIVMicroSim;
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
    
    public Female(int id, int faithfullness, double condomUse, int wantLevel, double lack, byte ccr51, 
            byte ccr52, byte ccr21, byte ccr22,byte HLAA1, byte HLAA2,
            byte HLAB1, byte HLAB2, byte HLAC1, byte HLAC2, int age, int life,
            byte orientation, int mother, int father, Neighborhood agentRace, Neighborhood agentReligion, Neighborhood otherNetwork, int selectivity){
        super(id, faithfullness, condomUse, wantLevel, lack, ccr51, ccr52, ccr21, ccr22, HLAA1, HLAA2,
            HLAB1, HLAB2, HLAC1, HLAC2, age, life, orientation, mother, father, agentRace, agentReligion, otherNetwork, selectivity);
    }
    public boolean makePregnant(Pregnancy p){
        if(pregnant) return false;
        pregnant = true;
        pregnancy = p;
        return true;
    }
    @Override
    public boolean isFemale(){return true;}
    
    public void attemptPregnancy(int PFC, Agent other, HIVMicroSim sim){
        if(pregnant) return;
        double pg;
        for(int i = 0; i< PFC; i++){
            pg = sim.random.nextDouble();
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
                //child neighborhoods... for now other and religion will be assigned randomly from one parent or the other if they
                //have one. 
                Neighborhood childRace, childReligion, childOther;
                if(race.ID == other.race.ID){
                    childRace = race;
                }else{
                    if(race.ID == NeighborhoodTemplates.Race_Other) childRace = other.race;
                    else if(other.race.ID == NeighborhoodTemplates.Race_Other) childRace = race;
                    else childRace = sim.race_Other;
                }
                int randInt  = sim.random.nextInt(3);
                if(randInt == 0){
                    childReligion = religion;
                }else{
                    if(randInt == 1){
                        childReligion = other.religion;
                    }else{
                        childReligion = NeighborhoodTemplates.getReligion(sim);
                    }
                }
                randInt  = sim.random.nextInt(3);
                if(randInt == 0){
                    childOther = this.other;
                }else{
                    if(randInt == 1){
                        childOther = other.other;
                    }else{
                        childOther = NeighborhoodTemplates.getOther(sim);
                    }
                }
                pregnancy = new Pregnancy(ID, other.ID, infCCR51, infCCR52, infCCR21, infCCR22, infHLAA1, infHLAA2, 
                        infHLAB1, infHLAB2, infHLAC1, infHLAC2, childRace, childReligion, childOther);
                return;
            } 
        }
    }
    @Override
    public void step(SimState state){
        super.step(state); //most of this has been moved into the 
        HIVMicroSim sim = (HIVMicroSim) state;
        
        if(age <216) return;
        if(alive && pregnant) {
            int roll;
            if(pregnancy.step()){
                //a little one is born! Now.... if mom is HIV positive... 
                Agent littleOne = sim.createNewAgent(pregnancy);
                pregnant = false;
                pregnancy = null;
                if(infected){
                    ArrayList<HIVInfection> infs = hiv.getGenotypes();
                    //attempt to infect the little one
                    if(infs.size() > 1){
                        roll = Math.abs(sim.getGaussianRange(-(infs.size()-1), (infs.size()-1), false));
                    }else{
                        roll = 0;
                    }
                    if(littleOne.attemptInfection(sim, infs.get(roll), hiv.getStage(), 1, Agent.MODEMOTHERCHILD)){
                        //Poor little guy was infected :( 
                        sim.logger.insertInfection(HIVLogger.INFECT_MOTHERTOCHILD, ID, littleOne.ID, 
                                infs.get(roll).getGenotype(), true);
                        littleOne.infect(sim.genotypeList.get(infs.get(roll).getGenotype()));
                    }
                }
            }
        }
    }
    
    @Override
    public boolean attemptCoitalInfection(HIVMicroSim sim, HIVInfection infection, int stage, int frequency, Agent agent, double degree){
        if(infected && hiv.hasGenoType(infection.getGenotype()))return false;
        //this calculates the potential reduction from alloimmunity, then passes it on to attemptInfection. 
        int alloImmunity = getAlloImmunity(agent.ID);
        if(alloImmunity > 100){
            degree *= 1/(alloImmunity *.01);
        }
        //Since we're in the female class, we'll add the "female factor" to the degree since the risk of infection is higher
        //for the female in heterosexual coitus. -- later additional factors may be added for homosexual coitus for males. 
        degree = degree*sim.femaleLikelinessFactor;
////////////////////////This needs refining.
        for(int i = 0; i< frequency; i++){
            if(attemptInfection(sim, infection, stage, degree, Agent.MODECOITIS)) return true;
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

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
/**
 *
 * @author ManuelLS
 */
public class Male extends Agent implements Steppable{
    private boolean circumcised = false; //this will be implemented later, but for now we'll just say all men are uncirumcised. 
    
    public Male(int id, int faithfullness, double condomUse, int wantLevel, double lack, byte ccr51, 
            byte ccr52, byte ccr21, byte ccr22,byte HLAA1, byte HLAA2,
            byte HLAB1, byte HLAB2, byte HLAC1, byte HLAC2, int age, int life,
            byte orientation, int mother, int father){
        super(id, faithfullness, condomUse, wantLevel, lack, ccr51, ccr52, ccr21, ccr22, HLAA1, HLAA2,
            HLAB1, HLAB2, HLAC1, HLAC2, age, life, orientation, mother, father);
    }
    @Override
    public boolean isFemale(){return false;}
    public boolean isPregnant(){return false;}
    @Override
    public boolean addEdge(Relationship a){
        
        if(a.getMale() != this){
            return false;
        }
        //make sure we're not duplicating a relationship
        int o = a.getFemale().ID;
        if (!network.stream().noneMatch((f) -> (f.getFemale().ID == o))) {
            return false;
        }
        
        network.add(a);
        if(a.getType() == Relationship.MARRIAGE){
            married = true;
        }
        //System.out.print("DEBUG: Add Relationship. Network Level: " + networkLevel + " added: " + a.getCoitalFrequency());
        networkLevel += a.getCoitalFrequency();
        //System.out.print(" new Network Level: " + networkLevel + "\n");
        return true;
    }
    @Override
    public boolean removeEdge(Relationship a){
        
        int o = a.getFemale().ID;
        for(int i = 0; i<network.size();i++){
            Relationship r = network.get(i);
            if(r.getFemale().ID == o){
                //System.out.print("DEBUG: Delete Relationship: " + networkLevel + " removed " + network.get(i).getCoitalFrequency());
                networkLevel -= r.getCoitalFrequency();
                if(r.getType() == Relationship.MARRIAGE){
                    married = false;
                }
                network.remove(i);
                //System.out.print(" new Network Level: " + networkLevel + "\n");
                return true;
            }
        }
        
        return false;// could not find the edge! 
    }
    @Override
    public void step(SimState state){
        HIVMicroSim sim = (HIVMicroSim) state;
        age++;
        if(age > life){
            deathFromOtherCauses(state);
            return;
        }
        if(age >= 216){
            width = 2;
            height = 2;
        }else{
            width = 1;
            height = 1;
        }
        //adjust disease
        if(infected){
            if(hiv.progress(age<216?2:1)){
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
                        for(Relationship r :network){//start with the last element and work down to empty out the list
                            r.getFemale().removeEdge(r);
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
        if(age<216)return;
        //adjust for network edges (note, this does not change the edges, just adds their effect and potential infection. 
        double adj = wantLevel;
        Agent other;
        int PFC; //protection-free coitis
        double PFCRoll = 0;
        forEachRelationship:
        for (Relationship network1 : network) {
            adj -= network1.getCoitalFrequency();
            other = network1.getFemale();
            //currently condoms are considered 100% effective in both pregnancy and viral transfer prevention. 
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
                    PFC=0;
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
            if(PFC > 0){
                addAlloImmunity(other, PFC);

                if(other.infected){
                    //select a genotype from the other 
                    ArrayList<HIVInfection> otherInfections = other.getDiseaseMatrix().getGenotypes(); 
                    HIVInfection infection;
                    //if the other has more than one genotype, select one, otherwise use that one. 
                    if(otherInfections.size() >1){
                        //set mean of 0 with max range of list size. This makes you most likely to 
                        //select an item closer to 0 or with larger virulence. 
                        int roll = Math.abs(sim.getGaussianRange(-(otherInfections.size()-1), (otherInfections.size()-1), true));
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
    ///////////////////Calculate frequency of unprotected coitus. 
                    if(attemptCoitalInfection(sim, infection, other.getDiseaseMatrix().getStage(), 
                            network1.getCoitalFrequency(), other, 1.0)){
                        //We've been infected!
                        boolean pre = !infected;
                        if(infect(sim.genotypeList.get(infection.getGenotype()))) {
                            sim.logger.insertInfection(HIVLogger.INFECT_HETERO, ID, other.ID, infection.getGenotype(), pre);
                        }
                    }
                }
            }
        }
        adjustLack((adj/12));
        degradeImmunity();
    }
     public boolean attemptCoitalInfection(HIVMicroSim sim, HIVInfection infection, 
             int stage, int frequency, Agent agent, double degree){
        //this calculates the potential reduction from alloimmunity, then passes it on to attemptInfection. 
        int alloImmunity = getAlloImmunity(agent.ID);
        addAlloImmunity(agent, frequency);
        if(alloImmunity > 100){
            degree*= 1/(alloImmunity *.01);
        }
        //add circumcision factor later additional factors may be added for homosexual coitus. 
        if(circumcised){
            degree = degree*sim.circumcisionLikelinessFactor;
        }
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
        
        graphics.fillRect(x,y,w, h);
    }
    @Override
    public void removeOneShots(SimState state){
        HIVMicroSim sim = (HIVMicroSim) state;
        for(int i = network.size()-1; i >=0; i--){
            Relationship r = network.get(i);
            if(r.getType() == Relationship.ONETIME){
                r.getFemale().removeEdge(r);
                sim.network.removeEdge(r);
                networkLevel -= r.getCoitalFrequency();
                network.remove(i);
            }
        }
    }
    @Override
    public void deathFromOtherCauses(SimState state){
        //3 steps
        HIVMicroSim sim = (HIVMicroSim)state;
        sim.logger.insertDeath(ID, true, infected);
        //1- remove networks
        Agent other;
        for(Relationship r : network){
            other = r.getFemale();
            other.removeEdge(r);
            sim.network.removeEdge(r);
        }
        sim.network.removeNode(this);
        //note- no need to remove this agent's network edges right now because it will be garbabe collected. 
        //-also no reason to change it to dead, but just in case something funky happens, lets debug it to color yellow or something.
        col = Color.CYAN;
        //2- remove from sparse plot
        sim.agents.remove(this);
        //3- Remove from schedule
        stopper.stop();
    }
}

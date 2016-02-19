/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.Agent;
import hivMicroSim.AlloImmunity;
import hivMicroSim.HIV.DiseaseMatrix;
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
public class Female extends Agent implements Steppable{
    private static final long serialVersionUID = 1;
    private boolean pregnant;
    private Pregnancy pregnancy;
    
    public Female(int id, int faithfullness, double condomUse, int wantLevel, double lack, byte ccr51, 
            byte ccr52, byte ccr21, byte ccr22,byte HLAA1, byte HLAA2,
            byte HLAB1, byte HLAB2, byte HLAC1, byte HLAC2, int age, int life,
            byte orientation, int mother, int father){
        super(id, faithfullness, condomUse, wantLevel, lack, ccr51, ccr52, ccr21, ccr22, HLAA1, HLAA2,
            HLAB1, HLAB2, HLAC1, HLAC2, age, life, orientation, mother, father);
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
    public boolean addEdge(Relationship a){
        
        if(a.getFemale()!= this){
            return false; //invalid edge! 
        }
        //make sure we're not duplicating a relationship
        int o = a.getMale().ID;
        if (!network.stream().noneMatch((f) -> (f.getMale().ID == o))) return false;
        network.add(a);
        
        //System.out.print("DEBUG: Add Relationship. Network Level: " + networkLevel + " added: " + a.getCoitalFrequency());
        networkLevel += a.getCoitalFrequency();
        if(a.getType() == Relationship.MARRIAGE){
            married = true;
        }
        //System.out.print(" new Network Level: " + networkLevel + "\n");
        return true;
    }
    @Override
    public boolean removeEdge(Relationship a){
        int o = a.getMale().ID;
        for(int i = 0; i<network.size();i++){
            Relationship r = network.get(i);
            if(r.getMale().ID == o){
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
    public boolean attemptPregnancy(int freq, Agent other){
        
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
    
    public boolean attemptCoitalInfection(HIVMicroSim sim, HIVInfection infection, int stage, int frequency, int agent, double degree){
        //this calculates the potential reduction from alloimmunity, then passes it on to attemptInfection. 
        int alloImmunity = getAlloImmunity(agent);
        if(alloImmunity > 100){
            degree *= 1/(alloImmunity *.01);
        }
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
    @Override
    public void removeOneShots(SimState state){
        HIVMicroSim sim = (HIVMicroSim) state;
        for(int i = network.size()-1; i >=0; i--){
            Relationship r = network.get(i);
            if(r.getType() == Relationship.ONETIME){
                r.getMale().removeEdge(r);
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
            other = r.getMale();
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

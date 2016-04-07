/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.Agent;
import Neighborhoods.Neighborhood;
import hivMicroSim.HIV.HIVInfection;
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
public class Male extends Agent implements Steppable{
    private boolean circumcised; 
    public final boolean onTop; 
    public Male(int id, int faithfullness, double condomUse, int wantLevel, double lack, byte ccr51, 
            byte ccr52, byte ccr21, byte ccr22,byte HLAA1, byte HLAA2,
            byte HLAB1, byte HLAB2, byte HLAC1, byte HLAC2, int age, int life,
            byte orientation, int mother, int father, boolean circumcised, boolean onTop,
            Neighborhood agentRace, Neighborhood agentReligion, Neighborhood otherNetwork, int selectivity){
        super(id, faithfullness, condomUse, wantLevel, lack, ccr51, ccr52, ccr21, ccr22, HLAA1, HLAA2,
            HLAB1, HLAB2, HLAC1, HLAC2, age, life, orientation, mother, father, agentRace, agentReligion, otherNetwork, selectivity);
        this.circumcised = circumcised;
        this.onTop = onTop;
    }
    @Override
    public boolean isFemale(){return false;}
    @Override
    public void step(SimState state){
        super.step(state);
    }
    @Override
    public boolean attemptCoitalInfection(HIVMicroSim sim, HIVInfection infection, 
             int stage, int frequency, Agent agent, double degree, Relationship r){
        if(infected && hiv.hasGenoType(infection.getGenotype()))return false;
        //this calculates the potential reduction from alloimmunity, then passes it on to attemptInfection. 
        int alloImmunity = getAlloImmunity(agent.ID);
        if(alloImmunity > 100){
            degree*= 1/(alloImmunity *.01);
        }
        if(!agent.isFemale()){
            Male m = (Male)agent;
            //at some point I should probably remove this --- since I've started using Netbeans tasks- I'm going to add it there! ^.^
            if(r.amOntop(this)){
                if(circumcised){
                    degree *= sim.circumcisionLikelinessFactor;
                }
                degree *= sim.insertiveAnalLikelinessFactor;
            }else{
                degree *= sim.receptiveAnalLikelinessFactor;
            }
        }else{
            if(circumcised){
                degree *= sim.circumcisionLikelinessFactor;
            }
        }
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
        
        graphics.fillRect(x,y,w, h);
    }
    
}

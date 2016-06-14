/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.Agent;
import hivMicroSim.HIV.DiseaseMatrix;
import hivMicroSim.HIVLogger;
import hivMicroSim.HIVMicroSim;
import hivMicroSim.Infection;
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
    private boolean circumcised = false; //this will be implemented later, but for now we'll just say all men are uncirumcised. 
    
    public Male(int id, int faithfullness, double condomUse, double wantLevel, double lack, byte ccr51, 
            byte ccr52, byte ccr21, byte ccr22,byte HLAA1, byte HLAA2,
            byte HLAB1, byte HLAB2, byte HLAC1, byte HLAC2, int age, int life){
        super(id, faithfullness, condomUse, wantLevel, lack, ccr51, ccr52, ccr21, ccr22, HLAA1, HLAA2,
            HLAB1, HLAB2, HLAC1, HLAC2, age, life);
    }
    @Override
    public boolean isFemale(){return false;}
    
    public void step(SimState state){
        super.step(state);
        HIVMicroSim sim = (HIVMicroSim) state;
        
        Agent other;
        int PFC; //protection-free coitis
        double PFCRoll = 0;
        forEachRelationship:
        for (Relationship network1 : network) {
            adj -= network1.getCoitalFrequency();
            other = network1.getFemale();
            //currently condoms are considered 100% effective in both pregnancy and viral transfer prevention. 
            switch(network1.getType()){
                case Relationship.MARRIAGE: // less likely to use condoms
                    PFCRoll = (sim.getGaussianRangeDouble(-.25, 0) + ((other.condomUse+condomUse)/2));
                    //average of partners + a random + or - between .25;
                    if(PFCRoll < 0) PFCRoll = 0;
                    if(PFCRoll > 1) PFCRoll = 1;
                    PFCRoll = 1-PFCRoll; // switch this around to likelihood of NOT using vs Using condoms. 
                    PFC = (int)(PFCRoll * network1.getCoitalFrequency());
                    //PFCRoll is the percentage of time using condoms PFC is the number of times without using condoms 
                    //- Java rounds down.
                    break;
                case Relationship.RELATIONSHIP:
                    PFCRoll = (sim.getGaussianRangeDouble(-.25, .25) + ((other.condomUse+condomUse)/2));
                    //average of partners + a random + or - between .25;
                    if(PFCRoll < 0) PFCRoll = 0;
                    if(PFCRoll > 1) PFCRoll = 1;
                    PFCRoll = 1-PFCRoll; // switch this around to likelihood of NOT using vs Using condoms. 
                    PFC = (int)(PFCRoll * network1.getCoitalFrequency());
                    //PFCRoll is the percentage of time using condoms PFC is the number of times without using condoms - 
                    //Java rounds down.
                    break;
                default: //one shot - more likely to use condoms
                    PFCRoll = (sim.getGaussianRangeDouble(0, .25) + ((other.condomUse+condomUse)/2));
                    //average of partners + a random + or - between .25;
                    //because java rounds down x<1 results in 0 PFC, thus we use the halfway mark and simply assign the single action. 
                    if(PFCRoll < .5) PFC = 1;
                    else PFC = 0; 
            }
            
            if(other.infected){
////////////////////////// TODO -- handle condom use! Least likely to use condoms with spouse, most likely to use them with one shot.            
                
                //select a genotype from the other 
                ArrayList<HIVInfection> otherInfections = other.getDiseaseMatrix().getGenotypes(); 
                HIVInfection infection;
                //if the other has more than one genotype, select one, otherwise use that one. 
                if(otherInfections.size() >1){
                    //set mean of 0 with max range of list size. This makes you most likely to 
                    //select an item closer to 0 or with larger virulence. 
                    int roll = Math.abs(sim.getGaussianRange(-(otherInfections.size()-1), (otherInfections.size()-1)));
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
     public boolean attemptCoitalInfection(HIVMicroSim sim, HIVInfection infection, 
             int stage, int frequency, Agent agent, double degree){
        //this calculates the potential reduction from alloimmunity, then passes it on to attemptInfection. 
        
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
}

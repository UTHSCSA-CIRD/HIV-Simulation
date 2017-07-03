/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.Agent;
import hivMicroSim.HIVMicroSim;
import sim.portrayal.*;
import sim.engine.*;

import java.awt.*;
/**
 *
 * @author ManuelLS
 */
public class Male extends Agent implements Steppable{
    private boolean circumcised = false; //this will be implemented later, but for now we'll just say all men are uncirumcised. 
    private boolean MSM, MSW;
    
    @Override
    public boolean acceptsGender(boolean isFemale){
        if(isFemale && MSW) return true;
        return !isFemale && MSM;
    }
    
    public Male(int id, Personality personality, Gene gene, double resistance, int age, int life, boolean circumsized, boolean MSM, boolean MSW){
        super(id, personality, gene, resistance, age, life);
        this.MSM = MSM;
        this.MSW = MSW; 
    }
    public boolean isCircumcised(){
        return circumcised;
    }
    public void circumcise(){
        circumcised = true;
    }
    public boolean getMSM(){
        return MSM;
    }
    public boolean getMSW(){
        return MSW;
    }
    public void switchMSM(){
        MSM = !MSM;
    }
    public void switchMSW(){
        MSW = !MSW;
    }
    
    @Override
    public boolean isFemale(){return false;}
    
    @Override
    public void step(SimState state){
        //All male specific factors have been removed for the moment...
        super.step(state);
        HIVMicroSim sim = (HIVMicroSim)state;
        if(age == sim.networkEntranceAge && alive){
            if(MSM && sim.MSMnetwork){
                sim.networkM.addNode(this);
            }
            if(MSW || !sim.MSMnetwork){
                sim.networkMF.addNode(this);
            }
        }
    }
    @Override
    public void death(HIVMicroSim sim, boolean natural){
        super.death(sim, natural);
        if(MSM && sim.MSMnetwork) sim.networkM.removeNode(this);
        if(MSW || !sim.MSMnetwork) sim.networkMF.removeNode(this);
        networkLevel = 0;
        network.clear();
    }
    @Override
    public boolean attemptCoitalInfection(HIVMicroSim sim, int frequency, double degree, int mode){
        //add circumcision factor later additional factors may be added for homosexual coitus. 
        if(circumcised && (mode == Agent.MODEAI || mode == Agent.MODEVI)){
            degree *= sim.likelinessFactorCircumcision;
        }
        return(super.attemptCoitalInfection(sim, frequency, degree, mode));
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

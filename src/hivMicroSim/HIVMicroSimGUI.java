/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim;
import sim.portrayal.network.*;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.engine.*;
import sim.display.*;
import javax.swing.*;
import java.awt.Color;
/**
 *
 * @author ManuelLS
 */
public class HIVMicroSimGUI extends GUIState{
    public Display2D display;
    public JFrame displayFrame;
    
    NetworkPortrayal2D edgePortrayal = new NetworkPortrayal2D();
    SparseGridPortrayal2D nodePortrayal = new SparseGridPortrayal2D();
    
    public static void main(String[] args){
        new HIVMicroSimGUI().createController();
    }
    public HIVMicroSimGUI(){
        super(new HIVMicroSim(System.currentTimeMillis()));
    }
    public HIVMicroSimGUI(SimState state){super(state);}
    
    public static String getName(){return "HIV Micro Simulation";}
    
    @Override
    public Object getSimulationInspectedObject(){return state;}
    
    @Override
    public void start(){
        super.start();
        setupPortrayals();
    }
    @Override
    public void load(SimState state){
        super.load(state);
        setupPortrayals();
    }
    public void setupPortrayals(){
        HIVMicroSim sim = (HIVMicroSim) state;
        
        edgePortrayal.setField( new SpatialNetwork2D(sim.agents, sim.network));
        edgePortrayal.setPortrayalForAll(new SimpleEdgePortrayal2D());
        nodePortrayal.setField(sim.agents);
        
        display.reset();
        display.setBackground(Color.white);
        
        display.repaint();
    }
    @Override
    public void init(Controller c){
        super.init(c);

        // make the displayer
        display = new Display2D(600,600,this);
        // turn off clipping
        display.setClipping(false);

        displayFrame = display.createFrame();
        displayFrame.setTitle("HIV Simulation");
        
        c.registerFrame(displayFrame);
        
        displayFrame.setVisible(true);
        display.attach(edgePortrayal, "Relationships");
        display.attach(nodePortrayal, "Agents");
    }
    
    @Override
    public void quit(){
        super.quit();
        if(displayFrame!=null) displayFrame.dispose();
        displayFrame = null;
        display = null;
    }
    
}

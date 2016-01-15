/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulation;
import sim.engine.*;
import sim.display.*;
import sim.portrayal.grid.*;
import java.awt.*;
import javax.swing.*;
import sim.app.tutorial3.Tutorial3WithUI;
/**
 *
 * @author ManuelLS
 */
public class InfectoryGUI extends GUIState{
    private static final long serialVersionUID = 1;
    public Display2D display;
    public JFrame displayFrame;

    SparseGridPortrayal2D entityPortrayal = new SparseGridPortrayal2D();
    FastValueGridPortrayal2D infectivityPortrayal = new FastValueGridPortrayal2D("Trail");
    public static void main(String[] args){
        new InfectoryGUI().createController();
    }
    
    public InfectoryGUI(){
        super(new Infectory(System.currentTimeMillis()));
    }
    public InfectoryGUI(SimState state){super(state);};
    public static String getName(){
        return "Infectory";
    }
    public void quit()
        {
        super.quit();
        
        if (displayFrame!=null) displayFrame.dispose();
        displayFrame = null;  // let gc
        display = null;       // let gc
        }

    public void start()
        {
        super.start();
        // set up our portrayals
        setupPortrayals();
        }
    
    public void load(SimState state)
        {
        super.load(state);
        // we now have new grids.  Set up the portrayals to reflect that
        setupPortrayals();
        }
    public void setupPortrayals(){
        infectivityPortrayal.setField(((Infectory)state).infectivity);
        infectivityPortrayal.setMap( new sim.util.gui.SimpleColorMap(
        0.0, 1.0, Color.black, Color.white));
        entityPortrayal.setField(((Infectory)state).entities);
        entityPortrayal.setPortrayalForAll( new sim.portrayal.simple.OvalPortrayal2D(Color.green));
        
        display.reset();
        display.repaint();
    }
    public void init(Controller c){
        super.init(c);
        
        // Make the Display2D.  We'll have it display stuff later.
        display = new Display2D(400,400,this); // at 400x400, we've got 4x4 per array position
        displayFrame = display.createFrame();
        c.registerFrame(displayFrame);   // register the frame so it appears in the "Display" list
        displayFrame.setVisible(true);

        // specify the backdrop color  -- what gets painted behind the displays
        display.setBackdrop(Color.black);

        // attach the portrayals
        display.attach(infectivityPortrayal,"Trails");
        display.attach(entityPortrayal,"Particles");
    }
}

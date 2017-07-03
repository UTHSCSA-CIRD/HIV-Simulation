package hivMicroSim.Agent;
import sim.portrayal.*;
import sim.engine.*;

import java.awt.*;
/**
 *
 * @author ManuelLS
 */
public class Female extends Agent implements Steppable{
    private static final long serialVersionUID = 1;
    private Pregnancy pregnancy;
    
    public boolean isPregnant(){
        return pregnancy != null;
    }
    public boolean setPregnancy(Pregnancy p){
        if(isPregnant()) return false;
        pregnancy = p;
        return true;
    }
    
    public Female(int id, Personality personality, Gene gene, double resistance, int age, int life){
        super(id, personality,gene, resistance, age, life);
    }
    
    @Override
    public boolean isFemale(){return true;}
     
    @Override
    public void step(SimState state){
        //No female specific step at the moment. 
        super.step(state);
        if (super.alive && pregnancy != null){
            if(pregnancy.step()){
                hivMicroSim.HIVMicroSim sim = (hivMicroSim.HIVMicroSim) state;
                sim.logger.insertNewAgent(pregnancy.child);
                sim.agents.setObjectLocation(pregnancy.child,sim.random.nextInt(sim.gridWidth), sim.random.nextInt(sim.gridHeight));
                Stoppable stop = sim.schedule.scheduleRepeating(sim.schedule.getSteps(), 1, pregnancy.child);
                pregnancy.child.setStoppable(stop);
                pregnancy = null;
            }
        }
    }
    @Override
    public boolean acceptsGender(boolean isFemale){
        return !isFemale;
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

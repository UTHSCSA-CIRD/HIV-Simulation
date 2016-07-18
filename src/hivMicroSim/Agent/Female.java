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
    
    public Female(int id, Personality personality, double resistance, int age, int life){
        super(id, personality, resistance, age, life);
    }
    
    @Override
    public boolean isFemale(){return true;}
     
    @Override
    public void step(SimState state){
        //No female specific step at the moment. 
        super.step(state);
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

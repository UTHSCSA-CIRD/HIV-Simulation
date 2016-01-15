/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulation;

import sim.field.grid.*;
import sim.portrayal.*;
import sim.portrayal.simple.*;
import sim.util.*;
import sim.engine.*;
import java.awt.*;

/**
 *
 * @author ManuelLS
 */
public class Entity extends OvalPortrayal2D implements Steppable{
    //entity represents a creature that may become infected over a course of time.
    private static final long serialVersionUID = 1;
    
    //describe the person
    private short immuneStrength; //1-10
    private boolean infected = false;
    private boolean contagious = false;
    private int infectionDay = -1;
    private int hour = 0;
    private short xdir;
    private short ydir;
    private short moveCount = 0;
    
    public Entity(short is, boolean infected, short xdir, short ydir){
        immuneStrength = is;
        this.xdir = xdir;
        this.ydir = ydir;
        if(infected){
            infectionDay = 0;
            this.infected = true;
        }
    }
    
    public boolean isInfected(){
        return infected;
    }
    public void setInfected(boolean infected){
        if(infected){
            infectionDay = 0;
            this.infected = true;
        }
    }
    public boolean isContagious(){
        return contagious;
    }
    public int getInfectionDay(){
        return infectionDay;
    }
    public boolean exposed(int a){
        if(infected) return false;
        if(infectionDay > -1) return false; // immune- previously infected. 
        if(a >= immuneStrength){
            infected = true;
            infectionDay = 0;
            return true;
        }
        return false;
    }
    public void move(SimState state){
        //I cheat... it's what I do.
        Infectory inf = (Infectory)state;
        Int2D location = inf.entities.getObjectLocation(this);
        int rand = inf.random.nextInt(10);
        moveCount++;
        if(rand < moveCount){
           xdir = (short)(inf.random.nextInt(3) - 1);
           ydir = (short)(inf.random.nextInt(3) - 1);
           moveCount = 0;
        }
        int newx = location.x + xdir;
        int newy = location.y + ydir;
        //x within range
        if (newx < 0) { 
            newx++; 
            xdir = (short)-xdir; 
        }
        else if (newx >= inf.infectivity.getWidth()) {
            newx--;
            xdir = (short)-xdir; 
        }
        //y within range
        if (newy < 0) { 
            newy++ ; 
            ydir = (short)-ydir; 
        }
        else if (newy >= inf.infectivity.getHeight()) {
            newy--; 
            ydir = (short)-ydir; 
        }
        Int2D newloc = new Int2D(newx,newy);
        inf.entities.setObjectLocation(this,newloc);
    }

    @Override
    public void step(SimState state){
        Infectory inf = (Infectory) state;
        hour++;
        if(hour > 24){
            if(infected){
                int roll = inf.random.nextInt(5) - (int)((inf.random.nextInt(immuneStrength)-5)*.5);
                infectionDay++;
                if(roll < infectionDay){
                    if(contagious){
                        contagious = false;
                        infected = false;
                    }else{
                        contagious = true;
                        infectionDay = 0;
                    }
                }
            }
            hour -= 24;
        }
        Int2D location = inf.entities.getObjectLocation(this);
        if(contagious){
            //fomite infection.
            inf.infectivity.field[location.x][location.y] = 1.0;
            //infect - airborne
        }
        if(infectionDay <0){
            //get fomite infection.
            double locInf = inf.infectivity.field[location.x][location.y];
            if(locInf > .1){
                
                //apply a chance for exposure of
                int rand = inf.random.nextInt((int)(locInf*10)+1);
                exposed(rand); 
            }
        }
    }
}

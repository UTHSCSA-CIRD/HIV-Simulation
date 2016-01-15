/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulation;
import sim.engine.*;
import sim.field.grid.*;
import sim.util.*;

/**
 *
 * @author ManuelLS
 */
public class Infectory extends SimState{
    
    public DoubleGrid2D infectivity;
    public SparseGrid2D entities;
    
    public int gridWidth = 100;
    public int gridHeight = 100;
    public int numEntities = 100;
    
    public int getNumEntities() { return numEntities; }
    public void setNumEntities(int val) { if (val >= 0) numEntities = val; }
    
    public Infectory(long seed){
        super(seed);
    }
    @Override
    public void start(){
        super.start();
        infectivity = new DoubleGrid2D(gridWidth, gridHeight);
        entities = new SparseGrid2D(gridWidth, gridHeight);
        
        Entity e;
        int is;
        int xdir;
        int ydir;
        for(int i=0; i< numEntities; i++){
            is = random.nextInt(10)+1;
            xdir = random.nextInt(3)-1;
            ydir = random.nextInt(3)-1;
            if(i == 0){
                e = new Entity((short)is, true, (short) xdir, (short) ydir);
            }else{
                
                e = new Entity((short)is, false, (short) xdir, (short) ydir);
            }
            schedule.scheduleRepeating(e);
            entities.setObjectLocation(e, new Int2D(random.nextInt(gridWidth),random.nextInt(gridHeight)));
        }
        Steppable mover = new Steppable(){
            @Override
            public void step(SimState state){
                Infectory inf = (Infectory) state;
                Bag b = inf.entities.getAllObjects();
                //my way around the only one type of step... >.>; 
                for(int i = 0; i < b.numObjs; i++){
                    ((Entity)b.get(i)).move(state);
                }
            }
            private static final long serialVersionUID = 1;
        };
        schedule.scheduleRepeating(Schedule.EPOCH,2,mover,1);
        Steppable decreaser = new Steppable(){
            @Override
            public void step(SimState state){
                Infectory inf = (Infectory) state;
                inf.infectivity.multiply(.98);
            }
            private static final long serialVersionUID = 1;
        };
        schedule.scheduleRepeating(Schedule.EPOCH,3, decreaser,1);
    }
    
    public static void main(String[] args){
        doLoop(Infectory.class, args);
        System.exit(0);
    }
}

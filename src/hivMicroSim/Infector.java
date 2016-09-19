/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim;
import sim.engine.SimState;
import sim.engine.Stoppable;
import sim.util.Bag;
import hivMicroSim.Agent.Agent;
import java.io.IOException;
import sim.engine.Schedule;
/**
 *
 * @author manuells
 */
public class Infector implements sim.engine.Steppable{
    /*
    *This class exists to initiate infection and start up the logger on a specified tick. 
    */
    private final static long serialVersionUID = 1;
    private int tick = 0;
    private final int initiateTick;
    private int infectNumber;
    private Stoppable stopper;
    
    public void setStopper(Stoppable a){
        stopper = a;
    }
    public Infector(int initiateOn, int infectNum){
        initiateTick = initiateOn;
        infectNumber = infectNum;
    }
    
    @Override
    public void step(SimState state){
        tick++;
        if(tick < initiateTick){
            return;
        }
        HIVMicroSim sim = (HIVMicroSim) state;
        Bag b = sim.agents.allObjects;
        int rand;
        Agent a;
        if(infectNumber > (b.size()*.5)){
            System.err.println("Warning, number to infect is greater than half the population, setting to 1%");
            infectNumber = (int)Math.ceil(.1* b.size());
        }
        for(int i = 0; i<infectNumber; i++){
            do{
                rand = sim.random.nextInt(b.size());
                a = (Agent)b.get(rand);
                if(a.getTickAge()< sim.networkEntranceAge || a.isInfected()) a = null;//We're not infecting children
                //continue to pull new random agents until you find one that is both old enough and not yet infected.
            }while(a == null);
            a.infect(sim);
        }
        try{
            sim.logger = new HIVLogger(sim.logLevel, "eventLog.txt", "yearLog.txt","agentLog.txt", sim.agents.size(), infectNumber);
            sim.schedule.scheduleRepeating(sim.schedule.getSteps(), 0, sim.logger);
            sim.logger.firstSet(b);
        }catch(IOException e){
            System.err.println("Exception when creating logger!! ");
            sim.logger = new HIVLogger();
        }
        stopper.stop();
    }
}

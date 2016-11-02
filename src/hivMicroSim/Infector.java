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
import hivMicroSim.Agent.Male;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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
    private boolean highRisk;
    
    //high risk infection attributes
        //gender and sexual preference
        private static final int notMSM = 10;
        private static final int condomUseMult = 10; 
    //end high risk infection attributes
    
    
    public void setStopper(Stoppable a){
        stopper = a;
    }
    public Infector(int initiateOn, int infectNum, boolean highRiskInfection){
        initiateTick = initiateOn;
        infectNumber = infectNum;
        highRisk = highRiskInfection;
    }
    /**
     * Step function that will infect X number of agents after Y number of ticks defined in the HIVMicroSim class. 
     * @param state 
     */
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
            System.err.println("Warning, number to infect is greater than half the population, setting to 10%");
            infectNumber = (int)Math.ceil(.1* b.size());
        }
        if(highRisk){
            infectHighRisk(sim, infectNumber);
        }else{
            for(int i = 0; i<infectNumber; i++){
                do{
                    rand = sim.random.nextInt(b.size());
                    a = (Agent)b.get(rand);
                    if(a.getTickAge()< sim.networkEntranceAge || a.isInfected()) a = null;//We're not infecting children
                    //continue to pull new random agents until you find one that is both old enough and not yet infected.
                }while(a == null);
                if(sim.stratifyInitInfected){
                    a.infect(sim, (int)(sim.random.nextDouble()*1000));
                }else{
                    a.infect(sim);
                }
            }
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
    /**
     * This method exists to target high risk populations. 
     * Note that the LOWER the weight the greater the risk.
     * (This is just to simplify programming so that we don't have to "flip" things like Monogamous and condom usage.)
     * @param sim 
     */
    private void infectHighRisk(HIVMicroSim sim, int infectNumber){
        Agent a;
        int risk, roll;
        ArrayList<RiskRating> agents = new ArrayList<>();
        Bag bagged = sim.agents.getAllObjects();
        for(int i = 0; i < bagged.size(); i++){
            a = (Agent)bagged.get(i);
            risk = a.getMonogamous() + a.getCoitalLongevity() + (int)(a.getCondomUse() * condomUseMult);
            if(!a.isFemale()){
                Male m = (Male)a;
                if(!m.getMSM()) risk += notMSM;
            }else risk += notMSM;
            if(a.getTickAge()>= sim.networkEntranceAge)agents.add(new RiskRating(a, risk));
        }
        //we sort the agents
        Collections.sort(agents);
        for(int i = 0; i < infectNumber; i++){
            roll = sim.nextGaussianRange(0, agents.size()-1, 0, false, true); //select an agent, riskier agents close to 0 are more likely to be chosen
            if(sim.stratifyInitInfected){
                agents.get(roll).agent.infect(sim, (int)(sim.random.nextDouble()*1000));
            }else{
                agents.get(roll).agent.infect(sim);
            }
            agents.remove(roll); // remove them from the list so that they can't be selected again. 
        }
        
    }
}

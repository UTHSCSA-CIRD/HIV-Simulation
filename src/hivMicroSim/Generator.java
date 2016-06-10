/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim;
import hivMicroSim.Agent.*;
import sim.engine.*;
/**
 *This is a generator class made to clean up the HIV Micro Sim code and keep all of the generation and relationship code together. 
 * @author ManuelLS
 */
public abstract class Generator{
    public static Agent generateAgent(HIVMicroSim sim, boolean born){
        /**
         * Generates a new agent.
         * @param sim The simulation this is a part of. This is needed for the random.
         * @param born Whether or not this is a newly born agent.
         */
        boolean female;
        int life;
        double hivImmunity;
        int age;
        Personality personality;
        Agent agent;
        
        if(born){
            age = 0;
        }else{
            age = sim.getGaussianRange(0, (int)(sim.averageLifeSpan * 1.5), sim.averageAge, true, true);
        }
        
        female = sim.random.nextBoolean();
        life = sim.getGaussianRange(age, (int)(sim.averageLifeSpan * 1.5), sim.averageLifeSpan, true, true);
        personality = generatePersonality(sim, female);
        if(female){
            agent = new Female();
        }else{
            agent = new Male();
        }
    }
    
    public static Personality generatePersonality(HIVMicroSim sim, boolean female){
        double condomUse;
        int want;
        int faithfulness;
        Personality ret;
        
        faithfulness = sim.getGaussianRange(Personality.faithfulnessMin, Personality.faithfulnessMax,
                female? sim.femaleFaithfulness : sim.maleFaithfulness , false, true);
        want = sim.getGaussianRange(Personality.wantMin, Personality.wantMax,
                female? sim.femaleWant : sim.maleWant , false, true);
        condomUse = sim.getGaussianRangeDouble(Personality.condomMin, Personality.condomMax,
                sim.percentCondomUse , false);
        ret = new Personality(faithfulness, want, condomUse);
        return ret;
    }
    /*
    public Agent createNewAgent(Pregnancy p){
        
    
        
        Agent agent;
        if(female){
            agent = new Female(currentID, faithfulness, condomUse, want, 0, p.getCCR51(), p.getCCR52(),p.getCCR21(), p.getCCR22(), p.getHLAA1(), p.getHLAA2(), p.getHLAB1(), p.getHLAB2(), p.getHLAC1(),p.getHLAC2(), 0, life);
        }else{
            agent = new Male(currentID, faithfulness, condomUse, want, 0, p.getCCR51(), p.getCCR52(),p.getCCR21(), p.getCCR22(), p.getHLAA1(), p.getHLAA2(), p.getHLAB1(), p.getHLAB2(), p.getHLAC1(),p.getHLAC2(), 0, life);
        }
        logger.insertBirth(agent);
        currentID++;
        //add to grids
        agents.setObjectLocation(agent,random.nextInt(gridWidth), random.nextInt(gridHeight));
        network.addNode(agent); // handles the network only! Display of nodes handled by continuous 2D
        //add to schedule
        stopper = schedule.scheduleRepeating(agent);
        //set stoppable
        agent.setStoppable(stopper);
        return agent;
    }
    */
    public static int getRelationship(Agent a, HIVMicroSim sim){
        
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim;
import hivMicroSim.Agent.*;
import sim.util.Bag;
/**
 *This is a generator class made to clean up the HIV Micro Sim code and keep all of the generation and relationship code together. 
 * @author ManuelLS
 */
public abstract class Generator{
    public static Agent generateAgent(HIVMicroSim sim, boolean init){
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
        //if this is the initial run of the generation we will use the average age, otherwise we will use the network entrance age as the mean. 
        //Meaning that agents can join the model at an older age, but that in general they are around the network entrance age. 
        age = sim.nextGaussianRange(0, (int)(sim.averageLifeSpan * 1.5), init ? sim.averageAge : sim.networkEntranceAge, true, true);
        
        //If we want to map only MSM then there is no reason to create females. 
        if(sim.percentMsM == 1) female = false;
        else female = sim.random.nextBoolean();
        if(age > sim.averageLifeSpan){
            life = sim.nextGaussianRange(age, (int)(sim.averageLifeSpan * 1.5), age, true, true);
        }else{
            life = sim.nextGaussianRange(age, (int)(sim.averageLifeSpan * 1.5), sim.averageLifeSpan, true, true);
        }
        personality = generatePersonality(sim, female);
        hivImmunity = sim.nextGaussianRangeDouble(sim.susceptibilityMin, sim.susceptibilityMax, sim.susceptibilityAvg, true);
        if(female){
            agent = new Female(sim.currentID, personality, hivImmunity, age, life);
        }else{
            boolean circ;
            circ = sim.random.nextDouble() < sim.percentCircum;
            double rand = sim.random.nextDouble();
            boolean msm = false, msw = false;
            if(rand < (sim.percentMsMW+sim.percentMsM)) msm = true;
            if(rand > sim.percentMsM) msw = true;
            agent = new Male(sim.currentID, personality, hivImmunity, age, life, circ, msm, msw);
        }
        //currently personality is generated prior to the agent. 
        agent.pp.setSelf(agent);
        sim.currentID++;
        return agent;
    }
    
   
    
    public static Personality generatePersonality(HIVMicroSim sim, boolean female){
        double condomUse;
        double libido;
        int monogamous;
        int commitment;
        Personality ret;
        //min, max, average, reroll (if outside the bounds of min and max will it truncate or re-roll?
        //inclusive (include the min and max)
        monogamous = sim.nextGaussianRange(Personality.monogamousMin, Personality.monogamousMax,
                female? sim.femaleMonogamous : sim.maleMonogamous , !sim.allowExtremes, sim.allowExtremes);
        commitment = sim.nextGaussianRange(Personality.coitalLongevityMin, Personality.coitalLongevityMax,
                female? sim.femaleCoitalLongevity : sim.maleCoitalLongevity , !sim.allowExtremes, sim.allowExtremes);
        libido = sim.nextGaussianRangeDouble(Personality.libidoMin, Personality.libidoMax,
                female? sim.femaleLibido : sim.maleLibido , true); // re-rolled no truncated- extremes in this are no longer considered because it's a double.
        condomUse = sim.nextGaussianRangeDouble(Personality.condomMin, Personality.condomMax,
                sim.percentCondomUse , !sim.allowExtremes);
        ret = new Personality(monogamous, commitment, libido, condomUse, sim.testingLikelihood);
        return ret;
    }
    public static void generateInitialNetwork(HIVMicroSim sim){
        Object o;
        Agent connector;
        Bag agents = sim.agents.allObjects;
        for(int i = 0; i < agents.size();i++){
            o = agents.get(i);
            if(o instanceof Agent){
                connector = (Agent)o;
            }else{
                System.err.println("Error. Not an agent!");
                continue;
            }
            if(connector.getTickAge() < sim.networkEntranceAge) continue;
            if(connector.wantsConnection(sim)){
                //We currently don't care about the return value, later renditions might allow the agent 
                //to attempt to find a connection multiple times.
                HandlerInteraction.findConnection(connector, sim);
            }
        }
    }
}

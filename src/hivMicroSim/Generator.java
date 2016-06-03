/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim;
import hivMicroSim.Agent.*;
/**
 *This is a generator class made to clean up the HIV Micro Sim code and keep all of the generation and relationship code together. 
 * @author ManuelLS
 */
public abstract class Generator{
    public static Agent generateAgent(HIVMicroSim sim){
        
    }
    public static Agent generateAgent(HIVMicroSim sim, Pregnancy p){
        
    }
    /*
    public Agent createNewAgent(Pregnancy p){
        boolean female;
        int offsetF;
        int offsetW;
        double condomUse;
        double want;
        double offsetCondom = percentCondomUse - .5;
        int faithfulness;
        int life;
        
        int offsetLife = averageLifeSpan -(int)((averageLifeSpan +(.5*averageLifeSpan))/2); //consider setting this in start.
        
        Stoppable stopper;
        female = random.nextBoolean();
        if(female){
            offsetF = femaleFaithfulness -5;
            offsetW = femaleWant - 5;
        }else{
            offsetF = maleFaithfulness -5;
            offsetW = maleWant - 5;
        }
        try{
            faithfulness = getGaussianRange(offsetF, 0, 10, false);
        }catch(OffSetOutOfRangeException e){
            System.err.println("OffsetFaithfulness");
            faithfulness = getGaussianRange(0, 10, false);
        }
        try{
            want = getGaussianRangeDouble(offsetW, 0, 10, false);
        }catch(OffSetOutOfRangeException e){
            System.err.println("OffsetWant");
            want = getGaussianRangeDouble(0, 10, false);
        }
        try{
            condomUse = getGaussianRangeDouble(offsetCondom, 0, 1.0, false);
        }catch(OffSetOutOfRangeException e){
            System.err.println("CondomUse offset");
            condomUse = getGaussianRangeDouble(0,1.0, false);
        }
        try{
            life = getGaussianRange(offsetLife, 0, (int)((averageLifeSpan +(.5*averageLifeSpan))), true);
        }catch(OffSetOutOfRangeException e){
            System.err.println("Life Expectancy offset out of bounds!");
            life = averageLifeSpan;
        }
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

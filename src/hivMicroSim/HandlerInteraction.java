/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim;

import hivMicroSim.Agent.Agent;
import hivMicroSim.Agent.Male;
import hivMicroSim.Agent.Personality;
import java.util.Iterator;
import sim.util.Bag;

/**
 *
 * @author ManuelLS
 */
public class HandlerInteraction {
    
    //TODO: Note to self: Agents no longer remove their own one shots. 
    
    
    /**
     * Finds a connection for a sim. Note: this should only be called if the agent wants a connection.
     * @param a - The agent that wants to connect.
     * @param sim - the HIV sim containing the network and random number generator.
     * @return 0 for no connection, otherwise the number for the default connection level. 
     */
    public static double findConnection(Agent a, HIVMicroSim sim){
        Bag nodes;
        int network = CoitalInteraction.networkMF;
        if(!a.isFemale() && sim.MSMnetwork){
            Male m = (Male)a;
            if(m.getMSM()){
                if(m.getMSW()){
                    //he can go either way, so we will randomly select a network for him to search. 
                    if(sim.random.nextBoolean()){
                        nodes = sim.networkMF.allNodes;
                    }else{
                        nodes = sim.networkM.allNodes;
                        network = CoitalInteraction.networkM;
                    }
                    
                }else{
                    nodes = sim.networkM.allNodes;
                    network = CoitalInteraction.networkM;
                }
            }else{
                nodes = sim.networkMF.allNodes;
            }
        }else{
            nodes = sim.networkMF.allNodes;
        }
        Agent other;
        int rand;
        int tries = 0;
        int triesMax = (int)a.getLack();
        if(triesMax == 0) triesMax = 1;
        
        //search up to 10 times for an acceptable agent.
        //In the future other trials can be added besides just gender. E.g. selectivity
        do{
            rand = sim.random.nextInt(nodes.size());
            other = (Agent)nodes.get(rand);
            if(a.ID == other.ID || !a.acceptsGender(other.isFemale()) || a.hasAsPartner(other)
                    || !other.wantsConnection(sim) || !other.acceptsGender(a.isFemale())) other = null;
            tries ++;
        }while(other == null && tries <= triesMax);
        if (other == null) return -1; //if could not find acceptable agent 
        
        //Yay, we have found a connection
        CoitalInteraction edge;
        double longevity,freq;
        freq = (a.getLibido() + other.getLibido())/2;
        longevity = (a.getCoitalLongevity() + other.getCoitalLongevity())/2;
        edge = new CoitalInteraction(a, other, longevity, freq, network);
        a.addEdge(edge);
        other.addEdge(edge);
        if(network == CoitalInteraction.networkMF){
            sim.networkMF.addEdge(edge);
        }else{
            sim.networkM.addEdge(edge);
        }
        return freq;
    }
    //TODO: This algorithm needs to be fixed. Right now a relationship with a commitment of 9 has a 1 in 10 chance of dissolving each week... 
    public static void processCoitalInteractions(HIVMicroSim sim){
        processCoitalNetwork(sim, sim.networkMF);
        if(sim.MSMnetwork){
            processCoitalNetwork(sim, sim.networkM);
        }//end if Network M is activated
    }
    private static void processCoitalNetwork(HIVMicroSim sim, ListNetwork network){
        Iterator iter = network.edges.iterator();
        CoitalInteraction edge;
        while(iter.hasNext()){
            edge = (CoitalInteraction)iter.next();
            //locals are faster than calling.
            sexualInteraction(sim, edge);           
            
            //R:> round(1-(pnorm(c(1,2,3,4,5,6,7,8,9), sd = 2)^52), digits = 4)
            // [1] 1.0000 0.9999 0.9726 0.6978 0.2767 0.0678 0.0120 0.0016 0.0002
            //int min, int max, boolean reroll, double mean, double std, int offset){
            double rand = sim.nextGaussianRangeDouble(Personality.coitalLongevityMin,
                    Personality.coitalLongevityMax,true, 0,
                    (Personality.coitalLongevityMax-Personality.coitalLongevityMin)/5, 0);
            if(rand > edge.getCoitalLongevity()){
                iter.remove();
                edge.getA().removeEdge(edge);
                edge.getB().removeEdge(edge);
                network.removeEdgeNetworkOnly(edge);
            }
        }
    }
    public static void sexualTransmission(HIVMicroSim sim, Agent infected, Agent nonInfected, int PFC){
        if(infected.isFemale()){
            //currently not handing possible female anal intercourse option... still need some input on that
            if(nonInfected.attemptCoitalInfection(sim, PFC, infected.getInfectivity(), Agent.MODEVI)){
                sim.logger.insertInfection(Agent.MODEVI, infected, nonInfected.ID, nonInfected.getAttemptsToInfect());
            }
            return;
        }
        if(nonInfected.isFemale()){
            if(nonInfected.attemptCoitalInfection(sim, PFC, infected.getInfectivity(), Agent.MODEVR)){
                sim.logger.insertInfection(Agent.MODEVR, infected, nonInfected.ID, nonInfected.getAttemptsToInfect());
            }
            return;
        }
        //currently randomly flipping between insertive and receptive.
        int mode = sim.random.nextBoolean() ? Agent.MODEAI : Agent.MODEAR;
        if(nonInfected.attemptCoitalInfection(sim, PFC, infected.getInfectivity(), mode)){
            sim.logger.insertInfection(mode, infected, nonInfected.ID, nonInfected.getAttemptsToInfect());
        }
    }
    public static void sexualInteraction(HIVMicroSim sim, CoitalInteraction edge){
        //This just helps simplify things and keep the infection code outside of the process CoitalInteraction code.
        int PFC = 0; //protection-free coitis
        Agent a = edge.getA();
        Agent b = edge.getB();
        //no need for processing if they aren't heterozygous for at least one of these.
        if(!(a.isInfected() ^ b.isInfected()))return;
        double ac = a.getCondomUse();
        double bc = b.getCondomUse();
        //calculate the number of acts this tick. This is because with coitalFrequency being a double we will likely need to
        //roll. 
        double roll;
        int coitis = (int)edge.getCoitalFrequency(); // note that coitis will be 0 if:  0 < coitalfrequency < 1 
        roll = sim.random.nextDouble();
        if(roll < edge.getCoitalFrequency()-coitis) coitis++; //so we roll coitalfreq - coitis  to capture the decimal value
        if (coitis == 0) return;
        
        if(ac == Personality.condomMin || bc == Personality.condomMin){
            if(ac == Personality.condomMax || bc == Personality.condomMax){
                if(sim.random.nextBoolean())
                    PFC = coitis;
                else{
                    int tmp = 0;
                    while (tmp < coitis){ //condom effectiveness 
                        roll = sim.random.nextDouble();
                        if(roll > sim.likelinessFactorCondom) PFC++;
                        tmp++;
                    }
                }
            }else{
                PFC = coitis;
            }
        }else{
            if(ac == Personality.condomMax || bc == Personality.condomMax){
                int tmp = 0;
                    while (tmp < coitis){ //condom effectiveness 
                        roll = sim.random.nextDouble();
                        if(roll > sim.likelinessFactorCondom) PFC++;
                        tmp++;
                    }
            }else{
                double avgC = (ac+bc)/2;
                for(int i = 0; i<coitis; i++){
                    roll = sim.random.nextDouble();
                    if(roll > avgC) PFC++;
                    else {
                        roll = sim.random.nextDouble();
                        if(roll > sim.likelinessFactorCondom) PFC++;
                    }
                }
                
            }
        }
        
        if(PFC <1) return;
        if(a.isInfected()) sexualTransmission(sim, a, b, PFC);
        else sexualTransmission(sim, b, a, PFC);
    }
}

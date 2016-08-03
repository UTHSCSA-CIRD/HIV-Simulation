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
public class HandlerRelationship {
    
    //TODO: Note to self: Agents no longer remove their own one shots. 
    
    
    /**
     * Finds a connection for a sim. Note: this should only be called if the agent wants a connection.
     * @param a - The agent that wants to connect.
     * @param sim - the HIV sim containing the network and random number generator.
     * @return 0 for no connection, otherwise the number for the default connection level. 
     */
    public static int findConnection(Agent a, HIVMicroSim sim){
        Bag nodes;
        int network = Relationship.networkMF;
        if(!a.isFemale() && sim.MSMnetwork){
            Male m = (Male)a;
            if(m.getMSM()){
                if(m.getMSW()){
                    //he can go either way, so we will randomly select a network for him to search. 
                    if(sim.random.nextBoolean()){
                        nodes = sim.networkMF.allNodes;
                    }else{
                        nodes = sim.networkM.allNodes;
                        network = Relationship.networkM;
                    }
                    
                }else{
                    nodes = sim.networkM.allNodes;
                    network = Relationship.networkM;
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
        int triesMax = (int)a.getLibido() - a.getNetworkSize();
        
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
        Relationship edge;
        int freq;
        if(a.getCommitment() == Personality.commitmentMin || other.getCommitment() == Personality.commitmentMin){
            rand = Relationship.commitmentDissolve;
            freq = 1;
        }else{
            rand = sim.getGaussianRange(Relationship.commitmentDissolve, Relationship.commitmentMax, 
                (a.getCommitment()+other.getCommitment())/2, false, true);
            if(rand == Relationship.commitmentDissolve)freq = 1; 
            else freq = (int)(a.getLibido() + other.getLibido())/2;
        }
        edge = new Relationship(a, other, rand, freq, network);
        a.addEdge(edge);
        other.addEdge(edge);
        if(network == Relationship.networkMF){
            sim.networkMF.addEdge(edge);
        }else{
            sim.networkM.addEdge(edge);
        }
        return (int)edge.getCommitmentLevel();
    }
    
    public static void processRelationships(HIVMicroSim sim){
        Iterator iter = sim.networkMF.edges.iterator();
        Relationship edge;
        while(iter.hasNext()){
            edge = (Relationship)iter.next();
            //locals are faster than calling.
            Agent a, b;
            a = edge.getA();
            b = edge.getB();
            if(a.isInfected() ^ b.isInfected()){ // exclusive OR
                if(a.isInfected()) sexualTransmission(sim, a, b, edge);
                else sexualTransmission(sim, b, a, edge);
            }//end infected
            if(a.getCommitment() == Personality.commitmentMin || b.getCommitment() == Personality.commitmentMin){
                //Dissolve.
                edge.adjustCommitmentLevel(-Relationship.commitmentMax);
            }else{
                //average minus the mean.
                double change = ((a.getCommitment()+b.getCommitment())/2)/(Personality.commitmentMax - Personality.commitmentMin);
                //change -= Math.abs(a.getLibido()-b.getLibido()); //reduce the change by the diff
                change = change + ((sim.random.nextDouble()*2)-1);//add the change augment to a random number between -1 and 1
                edge.adjustCommitmentLevel(change);
            }
            if(edge.getCommitmentLevel() == Relationship.commitmentDissolve){
                iter.remove();
                edge.getA().removeEdge(edge);
                edge.getB().removeEdge(edge);
                sim.networkMF.removeEdgeNetworkOnly(edge);
            }
        }//end loop. Network MF
        if(sim.MSMnetwork){
            iter = sim.networkM.edges.iterator();
            while(iter.hasNext()){
                edge = (Relationship)iter.next();
                //locals are faster than calling.
                Agent a, b;
                a = edge.getA();
                b = edge.getB();
                if(a.isInfected() ^ b.isInfected()){ // exclusive OR
                    if(a.isInfected()) sexualTransmission(sim, a, b, edge);
                    else sexualTransmission(sim, b, a, edge);
                }//end infected
                if(a.getCommitment() == Personality.commitmentMin || b.getCommitment() == Personality.commitmentMin){
                    //Dissolve.
                    edge.adjustCommitmentLevel(-Relationship.commitmentMax);
                }else{
                    //average minus the mean.
                    double change = ((a.getCommitment()+b.getCommitment())/2)/(Personality.commitmentMax - Personality.commitmentMin);
                    //change -= Math.abs(a.getLibido()-b.getLibido()); //reduce the change by the diff
                    change = change + ((sim.random.nextDouble()*2)-1);//add the change augment to a random number between -1 and 1
                    edge.adjustCommitmentLevel(change);
                }
                if(edge.getCommitmentLevel() == Relationship.commitmentDissolve){
                    iter.remove();
                    edge.getA().removeEdge(edge);
                    edge.getB().removeEdge(edge);
                    sim.networkMF.removeEdgeNetworkOnly(edge);
                }
            }//end loop. Network M
        }//end if Network M is activated
    }
    public static void sexualTransmission(HIVMicroSim sim, Agent infected, Agent nonInfected, Relationship edge){
        //This just helps simplify things and keep the infection code outside of the process Relationship code.
        int PFC = 0; //protection-free coitis
        double ac = infected.getCondomUse();
        double bc = nonInfected.getCondomUse();
        if(ac == Personality.condomMin || bc == Personality.condomMin){
            if(ac == Personality.condomMax || bc == Personality.condomMax){
                if(sim.random.nextBoolean())
                    PFC = edge.getCoitalFrequency();
                else
                    //TODO: Version 2- condom not 100%
                    return; //currently condom usage is assumed 100% effective, we know it's not, so this will be addressed
            }else{
                PFC = edge.getCoitalFrequency();
            }
        }else{
            if(ac == Personality.condomMax || bc == Personality.condomMax){
                //TODO: Version 2- condom not 100%
                return; //currently condom usage is assumed 100% effective, we know it's not, so this will be addressed
            }else{
                double avgC = (ac+bc)/2;
                double commitChange = edge.getCommitmentLevel() * sim.commitmentChange;
                if(avgC-commitChange < 0)
                    avgC = 0;
                else
                    avgC = avgC-commitChange;
                avgC = 1-avgC;
                double roll;
                for(int i = 0; i<edge.getCoitalFrequency(); i++){
                    roll = sim.random.nextDouble();
                    if(roll > avgC) PFC++;
                }
                if(PFC <1) return;
            }
        }
        if(infected.isFemale()){
            //currently not handing possible female anal intercourse option... still need some input on that
            if(nonInfected.attemptCoitalInfection(sim, PFC, infected.getInfectivity(), Agent.MODEVI)){
                sim.logger.insertInfection(Agent.MODEVI, infected.ID, nonInfected.ID, edge.getCommitmentLevel(), nonInfected.getAttemptsToInfect());
            }
            return;
        }
        if(nonInfected.isFemale()){
            if(nonInfected.attemptCoitalInfection(sim, edge.getCoitalFrequency(), infected.getInfectivity(), Agent.MODEVR)){
                sim.logger.insertInfection(Agent.MODEVR, infected.ID, nonInfected.ID, edge.getCommitmentLevel(), nonInfected.getAttemptsToInfect());
            }
            return;
        }
        //currently randomly flipping between insertive and receptive.
        int mode = sim.random.nextBoolean() ? Agent.MODEAI : Agent.MODEAR;
        if(nonInfected.attemptCoitalInfection(sim, edge.getCoitalFrequency(), infected.getInfectivity(), mode)){
            sim.logger.insertInfection(mode, infected.ID, nonInfected.ID, edge.getCommitmentLevel(), nonInfected.getAttemptsToInfect());
        }
    }
}

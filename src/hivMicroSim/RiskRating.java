/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim;

import hivMicroSim.Agent.Agent;

/**
 *  This class exists as a comparable utilized by the "high risk" method of the Infector class. 
 * It allows a sorting algorithm to quickly sort the weighted list of indexed agents. 
 * @author Laura Manuel
 */
public class RiskRating implements Comparable<RiskRating>{
    public final Agent agent;
    public final int weight;
    
    public RiskRating(Agent agent, int weight){
        this.agent = agent;
        this.weight = weight;
    }
    
    @Override
    public int compareTo(RiskRating a){
        if(weight > a.weight) return 1;
        if(weight < a.weight) return -1;
        return 0;
    }
}

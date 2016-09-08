/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim;
import hivMicroSim.Agent.Agent;
/**
 *
 * @author ManuelLS
 */
public class RelationshipHistory {
    public final Agent other;
    public final long startingTick;
    public RelationshipHistory(Agent other, long tick){
        this.other = other;
        startingTick = tick;
    }
    
}

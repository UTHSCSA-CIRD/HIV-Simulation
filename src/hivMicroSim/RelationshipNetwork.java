/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim;

import java.util.LinkedList;
import sim.field.network.Network;
import java.io.Serializable;

/**
 *
 * @author ManuelLS
 */
public class RelationshipNetwork implements Serializable{
    private static final long serialVersionUID = 1;
    public Network network;
    public LinkedList<Relationship> relationshipList;
    
    public void wrapperAddEdge(Relationship edge){
        network.addEdge(edge);
        relationshipList.add(edge);
    }
    public void wrapperRemoveEdge(Relationship edge){
        network.removeEdge(edge);
        relationshipList.remove(edge);
    }
    public RelationshipNetwork(){
        network = new Network(false);
        relationshipList = new LinkedList<>();
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim;
import sim.field.network.*;
import java.util.ArrayList;
/**
 *The purpose of this class is to provide a wrapper to the Network class. The network class does not allow us to 
 * store and step through the list of edges, edges only exist on the nodes. We still want to keep
 * the Network class for the visual aspects, but want to add an additional list that contains all of 
 * the edges in the network and allows us to walk through them. 
 * @author Laura Manuel
 */
public class ListNetwork extends Network{
    public ArrayList<CoitalInteraction> edges;
    
    public ArrayList<CoitalInteraction> getEdges(){
        return edges;
    }
    
    
    @Override
    public void addEdge(Edge a){
        edges.add((CoitalInteraction)a);
        super.addEdge(a);
    }
    @Override
    public Edge removeEdge(Edge a){
        edges.remove((CoitalInteraction)a);
        return(super.removeEdge(a));
    }
    /**
     * Turns out there's a small issue with simply removing the edge (if we're stepping through the ArrayList
     * with an iterator we should remove the edge with the iterator, so we need a method that says: 
     * 'Hey! I'm removing this with the iterator, but I still need you to remove it from the network!'
     * @param a The edge to remove from the network, not this edge should be removed from the ArrayList manually.
     */
    
    public void removeEdgeNetworkOnly(Edge a){
        super.removeEdge(a);
    }
    @Override
    public void removeAllEdges(){
        edges.clear();
        super.removeAllEdges();
    }
            
    public ListNetwork(){
        super();
        edges = new ArrayList<>();
    }    
    public ListNetwork(boolean a){
        super(a);
        edges = new ArrayList<>();
    }
}

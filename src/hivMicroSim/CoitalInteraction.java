/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim;
import hivMicroSim.Agent.Agent;
import sim.field.network.Edge;
/**
 *
 * @author ManuelLS
 */
public class CoitalInteraction extends Edge implements java.io.Serializable{
    private final static long serialVersionUID = 1;
    
    public final static int networkMF = 0;
    public final static int networkM = 1;
    
    //commitmentLevel is a double to allow for minute changes, however it should be represented as an
    //integer value to applications outside this class and should truncate down. e.g. 0.56 = 0, 1.12 = 1
    private double coitalLongevity;
    private final int type;
        public static final int MsW = 1;
        public static final int MsM = 2;
    private double coitalFrequency;
    private final Agent a,b;
    public final int network;
    
    public double getCoitalLongevity(){
        return coitalLongevity;
    }
    public double adjustCoitalLongevity(double a){
        coitalLongevity += a;
        if(coitalLongevity > 1) coitalLongevity = 1;
        if(coitalLongevity < 0) coitalLongevity = 0;
        return coitalLongevity;
    }
    public Agent getA(){return a;}
    public Agent getB(){return b;}
    public int getType(){
        return type;
    }
    public double getCoitalFrequency(){
        return coitalFrequency;
    }
    public void setCoitalFrequency(int frequency, int agentID){
        coitalFrequency = frequency;
        if(a.ID == agentID) b.calculateNetworkLevel();
        else a.calculateNetworkLevel();
    }
    
    public Agent getPartner(Agent me){
        if(a.ID == me.ID) return b;
        return a;
    }
    
    public CoitalInteraction(Agent A, Agent B, double level, double frequency, int network){
        super(A,B, level);
        coitalLongevity = level;
        a = A;
        b = B;
        coitalFrequency = frequency;
        if(a.isFemale() || b.isFemale()) type = MsW;
        else type = MsM;
        this.network = network;
    }
}

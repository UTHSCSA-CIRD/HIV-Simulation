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
public class Relationship extends Edge implements java.io.Serializable{
    private final static long serialVersionUID = 1;
    public final static int commitmentMax = 10;
    public final static int commitmentDissolve = 0;
    
    public final static int networkMF = 0;
    public final static int networkM = 1;
    
    //commitmentLevel is a double to allow for minute changes, however it should be represented as an
    //integer value to applications outside this class and should truncate down. e.g. 0.56 = 0, 1.12 = 1
    private double commitmentLevel;
    private final int type;
        public static final int MsW = 1;
        public static final int MsM = 2;
    private double coitalFrequency;
    private final Agent a,b;
    public final int network;
    
    public int getCommitmentLevel(){
        return (int)commitmentLevel;
    }
    public int adjustCommitmentLevel(double a){
        commitmentLevel += a;
        if(commitmentLevel > commitmentMax) commitmentLevel = 10;
        if(commitmentLevel < commitmentDissolve) commitmentLevel = commitmentDissolve;
        return (int)commitmentLevel;
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
    
    public Relationship(Agent A, Agent B, int level, double frequency, int network){
        super(A,B, level);
        commitmentLevel = level;
        a = A;
        b = B;
        coitalFrequency = frequency;
        if(a.isFemale() || b.isFemale()) type = MsW;
        else type = MsM;
        this.network = network;
    }
}

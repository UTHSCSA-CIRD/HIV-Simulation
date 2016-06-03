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
    
    private final int level;
        public static final int MARRIAGE = 3;
        public static final int RELATIONSHIP = 2;
        public static final int ONETIME = 1;
    private final int type;
        public static final int MsW = 1;
        public static final int MsM = 2;
    private int coitalFrequency;
    private final Agent a,b;
    
    public int getLevel(){
        return level;
    }
    public int getType(){
        return type;
    }
    public int getCoitalFrequency(){
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
    public Relationship(int level, Agent A, Agent B){
        super(A,B, level);
        this.level = level;
        a = A;
        b = B;
        coitalFrequency = 1;
        if(a.isFemale() || b.isFemale()) type = MsW;
        else type = MsM;
    }
    public Relationship(int level, Agent A, Agent B, int frequency){
        super(A,B, level);
        this.level = level;
        a = A;
        b = B;
        coitalFrequency = frequency;
        if(a.isFemale() || b.isFemale()) type = MsW;
        else type = MsM;
    }
}

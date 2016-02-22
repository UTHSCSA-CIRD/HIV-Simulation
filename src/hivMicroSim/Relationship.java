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
    
    private int type;
        public static final int MARRIAGE = 3;
        public static final int RELATIONSHIP = 2;
        public static final int ONETIME = 1;
    private final int coitalFrequency;
    private final Agent a,b;
    public Agent getA(){return a;}
    public Agent getB() {return b;}
    public int getType(){
        return type;
    }
    public void setType(int type){
        this.type = type;
    }
    public int getCoitalFrequency(){
        return coitalFrequency;
    }
    public Agent getPartner(int id){
        if(a.ID == id) return b;
        return a;
    }
    public boolean containsMe(int id){
        if(a.ID == id || b.ID == id) return true;
        return false;
    }
    public Relationship(int type, Agent A, Agent B){
        super(A,B, type);
        this.type = type;
        a = A;
        b = B;
        coitalFrequency = 1;
    }
    public Relationship(int type, Agent A, Agent B, int frequency){
        super(A,B, type);
        this.type = type;
        a = A;
        b = B;
        coitalFrequency = frequency;
    }
}

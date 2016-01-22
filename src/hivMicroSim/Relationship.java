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
    
    private final int type;
        public static final int MARRIAGE = 3;
        public static final int RELATIONSHIP = 2;
        public static final int ONETIME = 1;
    private final int coitalFrequency;
    private final Agent m,f;
    
    public int getType(){
        return type;
    }
    public int getCoitalFrequency(){
        return coitalFrequency;
    }
    public Agent getMale(){
        return m;
    }
    public Agent getFemale(){
        return f;
    }
    
    public Relationship(int type, Agent M, Agent F){
        super(M,F, type);
        this.type = type;
        m = M;
        f = F;
        coitalFrequency = 1;
    }
    public Relationship(int type, Agent M, Agent F, int frequency){
        super(M,F, type);
        this.type = type;
        m = M;
        f = F;
        coitalFrequency = frequency;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.Agent;

import Neighborhoods.Neighborhood;
import java.util.ArrayList;

/**
 *
 * @author ManuelLS
 */
public class AgentProfile {
     private static final long serialVersionUID = 1;
    //Behavioral Factors
    
    //base
    public final boolean female;
    public final int faithfulness; // between 1 and 10
    public final double condomUse; //between 0 and 1 inclusive
    public final int want;
    public final int selectivity;
    public final byte orientation;
    
    public AgentProfile(boolean female, int faithfullness, double condom, int want, byte orientation, int selectivity){
        
        this.female = female;
        this.faithfulness = faithfullness;// because the programmer can't spell... 
        condomUse = condom;
        this.want = want;
        this.selectivity = selectivity;
        this.orientation = orientation;
        }
}

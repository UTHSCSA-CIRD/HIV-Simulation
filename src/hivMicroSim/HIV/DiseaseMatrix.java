/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.HIV;
import hivMicroSim.HIVMicroSim;
import java.util.ArrayList;

/**
 * The disease matrix handles all of the global disease related variables as well as this agent's disease progression.
 * In the future it will also contain information about treatment and possibly behavioral changes. For now
 * it deals mostly with the wellness, stage, and infectivity.
 * @author ManuelLS
 */
public class DiseaseMatrix implements java.io.Serializable{
    private static final long serialVersionUID = 1;
    
    //Stage info
    public static final int StageAcute = 1;
    public static final int StageLatency = 2;
    public static final int StageAIDS = 3;
    public static final int ACUTEXFACTOR = 15;
    private static final int ACUTEMONTHS = 2;
    private static final int LATENCYWELLNESSTHRESHOLD = 200; // the threshold over which 
    public static final int AIDSXFACTOR = 20;
    
    public static final double wellnessHazardMaxLatency = 10.0;
    public static final double wellnessHazardMinLatency = -20.83;
    
    public static final double wellnessHazardMaxAIDS = 5.0;
    public static final double wellnessHazardMinAIDS = -50.0;
    public static final int[] wellnessLevels = {500,400,300,200,100};
    public static final double[] wellnessHinderance = {.1,.2,.3,.7,.8};
    
    
    //INSTANCE SPECIFIC FACTORS
    private int stage;
    private int duration;
    private int infectionWellness = 700;
    private int infectivity = 600;
    private double wellnessHazardLatency = -4.2; //this agent's average wellness decline per tick after acute
    private double wellnessHazardAIDS = -8.3; //this agent's average wellness decline per tick after acute
    private boolean known = false;
    
    
    public int getStage(){
        return stage;
    }
    public int getDuration(){
        return duration;
    }
    
    public int progress(HIVMicroSim sim){
        //an algorithm to calculate the progression of the diseas in the individual
        //returns the wellness of the individual.
        switch(stage){
            case 1://acute
                //Average viral load 600
                //up to 20X in acute stage. -using 15
                progression += ACUTEXFACTOR*(a*viralLoadFactor);
                if(progression > ACUTELEVEL){
                    stage = 2;
                    progression = 0;
                    return true;
                }
            break;
            case 2: //clinical latency
                progression += viralLoadFactor*a;
                if(progression > LATENCYLEVEL){
                    stage = 3;
                    progression = 0;
                    return true;
                }
            break;
            case 3:
                progression += AIDSXFACTOR*(a*viralLoadFactor);
                if(progression > AIDSLEVEL){
                    stage = 4;
                    return true;
                }
            break;
            default:
                System.err.println("Cannot progress invalid/death stage");      
        }
        return false;
    }
    public DiseaseMatrix(HIVInfection a){
        infections = new ArrayList<>();
        infections.add(a);
        stage = 1;
        progression = 0;
        viralLoadFactor = a.getVirulence();
    }
    public DiseaseMatrix(HIVInfection b, int stage, int progression){
        infections = new ArrayList<>();
        infections.add(b);
        this.stage = stage;
        this.progression = progression;
        viralLoadFactor = b.getVirulence();
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.HIV;
import hivMicroSim.HIVMicroSim;

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
    public static final int StageDeath = 4; //for error checking or debugging.
    public static final int ACUTEXFACTOR = 15;
    private static final int ACUTETICKS = 7;
    public static final int AIDSXFACTOR = 20;
        
    public static final double normalInfectivity = 1;
    
    public static final int minTimeToAIDS = 52;
    public static final int averageTimeToAIDS = 520;
    public static final int minTimeToDeath = 12;
    public static final int averageTimeToDeath = 156;
    
    
    //INSTANCE SPECIFIC FACTORS
    private int stage;
    private int duration;
    private int aidsTick = -1; //the number of ticks since the agent converted to aids
    private double infectivity = 1;
    private final int timeToAIDS;
    private final int timeToDeath;
    private boolean known = false;
    
    public int getAIDsTick(){return aidsTick;}
    public DiseaseMatrix() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public double getInfectivity(){
        /**
         * This method combines infectivity with the stage to get the current infectivity. 
         */
        double degree = infectivity;
        if(stage == StageAcute){
            degree *= ACUTEXFACTOR;
        }else{
            if(stage == StageAIDS){
                degree *= AIDSXFACTOR;
            }
        }
        return degree;
    }
    
    public double getBaseInfectivity(){
        /**
         * This method disregards stage and only gives the base infectivity
         */
        return infectivity;
    }
    public int getStage(){
        return stage;
    }
    public int getDuration(){
        return duration;
    }
    
    public boolean progress(HIVMicroSim sim){
        //an algorithm to calculate the progression of the disease in the individual
        //returns the wellness of the individual.
        //0 - no change
        //- stage changed
        //odd- hindrance changed
        duration++;
        switch(stage){
            case StageAcute://acute
                if(duration == ACUTETICKS){
                    stage = StageLatency;
                    return true;
                }
            break;
            case StageLatency: //clinical latency
                if(duration==timeToAIDS){
                    stage = StageAIDS;
                    aidsTick = 0;
                    return true;
                }
            break;
            case StageAIDS:
                aidsTick++;
                if(aidsTick == timeToDeath){
                    //progress to Death
                    stage = StageDeath;
                    return true;
                }
            break;
            default:
                System.err.println("Cannot progress invalid/death stage");
        }
        return false;
    }
    public void discover(){
        known = true;
    }
    public boolean isKnown(){
        return known;
    }
    public DiseaseMatrix(double infectivity, int toAidsTime, int aidsTime){
        this.infectivity = infectivity;
        timeToAIDS = toAidsTime;
        timeToDeath = aidsTime;
        known = false;
        stage = StageAcute;
        duration = 0;
    }
}

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
    private static final int ACUTEMONTHS = 2;
    private static final int LATENCYWELLNESSTHRESHOLD = 200; // the threshold over which 
    public static final int AIDSXFACTOR = 20;
    private static final int WELLNESSDEATHTHRESHOLD = 0;
    
    public static final int normalWellness = 700;
    public static final double normalInfectivity = 1;
    
    public static final double wellnessHazardMaxLatency = 41.6;
    public static final double wellnessHazardAvgLatency = -4.2;
    public static final double wellnessHazardMinLatency = -41.6;
    
    public static final double wellnessHazardMaxAIDS = 50.0;
    public static final double wellnessHazardAvgAIDS = -8.3;
    public static final double wellnessHazardMinAIDS = -50.0;
    public static final int[] wellnessLevels = {500,400,300,200,100};
    public static final double[] wellnessHindrance = {.9,.8,.7,.3,.2};
    
    
    //INSTANCE SPECIFIC FACTORS
    private int stage;
    private int duration;
    private int aidsTick = -1; //the number of ticks since the agent converted to aids
    private int infectionWellness = 700;
    private double hindrance;
    private double infectivity = 1;
    private double wellnessHazardLatency = -4.2; //this agent's average wellness decline per tick after acute
    private double wellnessHazardAIDS = -5.6; //this agent's average wellness decline per tick after acute
    private boolean known = false;
    
    public int getAIDsTick(){return aidsTick;}
    public DiseaseMatrix() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    public int getWellness(){
        return infectionWellness;
    }
    public double getWellnessHazardLatency(){
        return wellnessHazardLatency;
    }
    public double getWellnessHazardAIDS(){
        return wellnessHazardAIDS;
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
    public double getHindrance(){
        return hindrance;
    }
    public boolean updateHindrance(){
        /**
         * Updates the hindrance value of the disease.
         * @return Returns true if the hindrance changed or false if the hindrance did not change. 
         */
        double hind = 0;
        int level = 0;
        while(level < 5 && infectionWellness < wellnessLevels[level]){
            hind = wellnessHindrance[level];
            level ++;
        }
        if(hind == hindrance) return false;
        hindrance = hind;
        return true;
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
    
    public int progress(HIVMicroSim sim){
        //an algorithm to calculate the progression of the disease in the individual
        //returns the wellness of the individual.
        //0 - no change
        //- stage changed
        //odd- hindrance changed
        int change = 0;
        duration++;
        double rand;
        switch(stage){
            case StageAcute://acute
                if(duration == ACUTEMONTHS){
                    stage = StageLatency;
                    change -=2;
                }
            break;
            case StageLatency: //clinical latency
                rand = sim.getGaussianRangeDouble(wellnessHazardMinLatency, wellnessHazardMaxLatency, wellnessHazardLatency, true);
                infectionWellness +=rand;
                if(infectionWellness < LATENCYWELLNESSTHRESHOLD){
                    //progress to AIDS
                    stage = StageAIDS;
                    change -=2;
                    aidsTick = 0;
                }
            break;
            case StageAIDS:
                aidsTick++;
                rand = sim.getGaussianRangeDouble(wellnessHazardMinAIDS, wellnessHazardMaxAIDS, wellnessHazardAIDS, true);
                infectionWellness +=rand;
                if(infectionWellness <= WELLNESSDEATHTHRESHOLD){
                    //progress to Death
                    stage = StageDeath;
                    change -=2;
                }
            break;
            default:
                System.err.println("Cannot progress invalid/death stage");      
        }
        if(updateHindrance())change += 1;
        return change;
    }
    public void discover(){
        known = true;
    }
    public boolean isKnown(){
        return known;
    }
    public DiseaseMatrix(int wellness, double infectivity, double latencyHazard, double aidsHazard){
        infectionWellness = wellness;
        this.infectivity = infectivity;
        wellnessHazardLatency = latencyHazard;
        wellnessHazardAIDS = aidsHazard;
        hindrance = 0;
        known = false;
        stage = StageAcute;
        duration = 0;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.HIV;
import java.util.ArrayList;
import java.util.Collections;
/**
 *
 * @author ManuelLS
 */
public class DiseaseMatrix implements java.io.Serializable{
    private static final long serialVersionUID = 1;
    
    public static final int ACUTEXFACTOR = 15;
    private static final int ACUTEMONTHS = 3;
    private static final int AVERAGEVIRALLOAD = 600;
    private static final int LATENCYMONTHS = 120; //10 years 
    public static final int AIDSXFACTOR = 4;
    private static final int AIDSMONTHS = 24;
    private static final int AIDSLEVEL = AIDSMONTHS *(AIDSXFACTOR * AVERAGEVIRALLOAD);
    private static final int ACUTELEVEL = ACUTEMONTHS * (ACUTEXFACTOR * AVERAGEVIRALLOAD);
    private static final int LATENCYLEVEL = LATENCYMONTHS * AVERAGEVIRALLOAD;
    
    
    
    private ArrayList<HIVInfection> infections; //allows for multiple genotype infection.
    private int stage;
    private int progression;
    private int viralLoadFactor;//1 being the smallest viral load factor and indicates those that tend to live 25+ years without treatment.
    
    public ArrayList<HIVInfection> getGenotypes(){
        return infections;
    }
    public int getStage(){
        return stage;
    }
    public int getProgression(){
        return progression;
    }
    public int getViralLoadFactor(){
        return viralLoadFactor;
    }
    public boolean progress(int a){
        //an algorithm to calculate the progression from one stage to the next, 
        //returns true if progresses to the next stage -> 1- acute ->2- latent ->3- AIDs -> 4- Death
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
    public void setViralLoadFactor(int a){
        if(a > 0){
            viralLoadFactor = a;
        }
    }
    public void adjustViralLoadFactor(int a){
        viralLoadFactor += a;
        if(viralLoadFactor<1){viralLoadFactor = 1;}
    }
    public boolean addGenoType(HIVInfection a){
        if (!infections.stream().noneMatch((b) -> (b.getGenotype() == a.getGenotype()))) {
            return false;
        }
        viralLoadFactor += a.getVirulence();
        infections.add(a);
        Collections.sort(infections);
        return true;
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

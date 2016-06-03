package hivMicroSim.Agent;

/**
 *This class contains the base personality of the agent.
 * @author Laura Manuel
 */
public class Personality {
    //Static quantifying variables for extremes:
    public static final int faithfulnessMin = 0;
    public static final int faithfulnessMax = 10;
    public static final int condomMax = 1;
    public static final int condomMin = 0;
    public static final int wantMin = 0;
    public static final int wantMax = 10;
    //not stored in here yet, but this is the best place to keep the max and min.
    public static final int lackMin = 0;
    public static final int lackMax = 10;
    
    //current levels
    protected final int faithfulness; 
    protected final double condomUse; 
    protected final int want;
    
    //base levels
    protected final int baseFaithfulness; 
    protected final double baseCondomUse; 
    protected final int baseWant;
    
    public Personality(int faith, int want, double condom){
        baseFaithfulness = faithfulness = faith;
        baseWant = this.want = want;
        baseCondomUse = condomUse = condom;
    }
}

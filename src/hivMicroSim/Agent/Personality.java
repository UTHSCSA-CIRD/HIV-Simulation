package hivMicroSim.Agent;

/**
 *This class contains the base personality of the agent.
 * @author Laura Manuel
 */
public class Personality {
    //Static quantifying variables for extremes:
    public static final int faithfulnessMin = 0;
    public static final int faithfulnessMax = 10;
    public static final double condomMax = 1.0;
    public static final double condomMin = 0;
    public static final int wantMin = 0;
    public static final int wantMax = 10;
    //not stored in here yet, but this is the best place to keep the max and min.
    public static final int lackMin = 0;
    public static final int lackMax = 10;
    
    //current levels
    protected int faithfulness; 
    protected double condomUse; 
    protected int want;
    
    //base levels
    protected final int baseFaithfulness; 
    protected final double baseCondomUse; 
    protected final int baseWant;
    
    public void hinderWant(double a){
        want *= a;
    }
    
    public Personality(int faith, int want, double condom){
        baseFaithfulness = faithfulness = faith;
        baseWant = this.want = want;
        baseCondomUse = condomUse = condom;
    }
}

package hivMicroSim.Agent;

/**
 *This class contains the base personality of the agent.
 * @author Laura Manuel
 */
public class Personality {
    //Static quantifying variables for extremes:
    public static final int monogamousMin = 0;
    public static final int monogamousMax = 10;
    public static final int coitalLongevityMin = 0;
    public static final int coitalLongevityMax = 10;
    public static final double condomMax = 1.0;
    public static final double condomMin = 0;
    public static final int libidoMin = 0;
    public static final int libidoMax = 7;
    public static final double testingMin = 0;
    public static final double testingMax = 1.0;
    
    
    //current levels
    protected int monogamous;
    protected int coitalLongevity;
    protected double condomUse;
    protected double libido;
    protected double testingLikelihood;
    
    
    //base levels
    protected final int baseMonogamous; 
    protected final int baseCoitalLongevity; 
    protected final double baseCondomUse; 
    protected final double baseLibido;
    protected final double baseTestingLikelihood;
    
    public void hinderLibido(double a){
        libido = (baseLibido * a);
    }
    public void changePersonality(int mono, int commit, double lib, double condom, double testing){
        monogamous += mono;
        if (monogamous > monogamousMax)monogamous = monogamousMax;
        if (monogamous < monogamousMin)monogamous = monogamousMin;
        
        coitalLongevity += commit;
        if (coitalLongevity > coitalLongevityMax)coitalLongevity = coitalLongevityMax;
        if (coitalLongevity < coitalLongevityMin)coitalLongevity = coitalLongevityMin;
        
        libido += lib;
        if (libido > libidoMax)libido = libidoMax;
        if (libido < libidoMin)libido = libidoMin;
        
        condomUse += condom;
        if (condomUse > condomMax)condomUse = condomMax;
        if (condomUse < condomMin)condomUse = condomMin;
        
        testingLikelihood += testing;
        if (testingLikelihood > testingMax)testingLikelihood = testingMax;
        if (testingLikelihood < testingMin)testingLikelihood = testingMin;
    }
    
    public Personality(int mono, int commit, double libido, double condom, double testing){
        baseMonogamous = monogamous = mono;
        baseCoitalLongevity = coitalLongevity = commit;
        baseLibido = this.libido = libido;
        baseCondomUse = condomUse = condom;
        baseTestingLikelihood = testingLikelihood = testing;
    }
}

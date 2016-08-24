package hivMicroSim.Agent;

/**
 *This class contains the base personality of the agent.
 * @author Laura Manuel
 */
public class Personality {
    //Static quantifying variables for extremes:
    public static final int monogamousMin = 0;
    public static final int monogamousMax = 10;
    public static final int commitmentMin = 0;
    public static final int commitmentMax = 10;
    public static final double condomMax = 1.0;
    public static final double condomMin = 0;
    public static final int libidoMin = 0;
    public static final int libidoMax = 30;
    public static final double testingMin = 0;
    public static final double testingMax = 1.0;
    
    
    //current levels
    protected int monogamous;
    protected int commitment;
    protected double condomUse;
    protected int libido;
    protected double testingLikelihood;
    
    
    //base levels
    protected final int baseMonogamous; 
    protected final int baseCommitment; 
    protected final double baseCondomUse; 
    protected final int baseLibido;
    protected final double baseTestingLikelihood;
    
    public void hinderLibido(double a){
        libido = (int)(baseLibido * a);
    }
    public void changePersonality(int mono, int commit, int lib, double condom, double testing){
        monogamous += mono;
        if (monogamous > monogamousMax)monogamous = monogamousMax;
        if (monogamous < monogamousMin)monogamous = monogamousMin;
        
        commitment += commit;
        if (commitment > commitmentMax)commitment = commitmentMax;
        if (commitment < commitmentMin)commitment = commitmentMin;
        
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
    
    public Personality(int mono, int commit, int libido, double condom, double testing){
        baseMonogamous = monogamous = mono;
        baseCommitment = commitment = commit;
        baseLibido = this.libido = libido;
        baseCondomUse = condomUse = condom;
        baseTestingLikelihood = testingLikelihood = testing;
    }
}

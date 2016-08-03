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
    
    //current levels
    protected int monogamous;
    protected int commitment;
    protected double condomUse;
    protected int libido;
    
    //base levels
    protected final int baseMonogamous; 
    protected final int baseCommitment; 
    protected final double baseCondomUse; 
    protected final int baseLibido;
    
    public void hinderLibido(double a){
        libido = (int)(baseLibido * a);
    }
    
    public Personality(int mono, int commit, int libido, double condom){
        baseMonogamous = monogamous = mono;
        baseCommitment = commitment = commit;
        baseLibido = this.libido = libido;
        baseCondomUse = condomUse = condom;
    }
}

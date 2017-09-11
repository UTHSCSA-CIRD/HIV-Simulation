package hivMicroSim.Agent;

import hivMicroSim.HIVMicroSim;

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
    public static final double knownSeroSortMin = 0.0;
    public static final double knownSeroSortMax = 1.0;
    
    
    //current levels
    protected int monogamous;
    protected int coitalLongevity;
    protected double condomUse;
    protected double libido;
    protected double testingLikelihood;
    protected boolean seroSort = false; //default because not infected. 
    
    //base levels
    protected final int baseMonogamous; 
    protected final int baseCoitalLongevity; 
    protected final double baseCondomUse; 
    protected final double baseLibido;
    protected final double baseTestingLikelihood;
    private Agent self = null;
    
    public void setSelf(Agent a){
        //really nothing should try to overwrite the original self... but.... 
        //currently personality is generated prior to the agent. 
        if(self == null) self = a;
    }
    
    /**
     * Tests whether or not the agent wants a connection. 
     * @param sim Provides a random number generator and access to system variables. 
     * @return True if the agent currently desires a new connection. False if they do not. 
     */
    public boolean wantsConnection(HIVMicroSim sim){
        if(self.networkLevel == 0)return true;
        //are their current needs met?
        if(self.networkLevel >= libido) return false;
        //handle those at the extreme of polygamy. 
        if(monogamous == monogamousMin) return true;
        //extremes of monogamy- they will not be with more than 1 person at the same time.
        if(monogamous == monogamousMax) return false;
        //failing all else we roll their monogamy score on the same scale as commitment with non-monogamy being increasingly
        //unlikely the higher your score. Note that, like longevity the sheer number of tests make a random number from
        //0 to 10 a bad idea because in 1 year (52 ticks) a rank 10 will have been rolled approximately 5 times. 
        //public int nextGaussianRange(int min, int max, boolean reroll, boolean inclusive, double mean, double std, int offset){
        //Ran some calculations in R. at 2.5 the chances of a level 5 agent engaging after 1 year was around 70%
        //utilizing 2 ((10-0)/5) you get the probabilities below. 
        //R:> round(1-(pnorm(c(1,2,3,4,5,6,7,8,9), sd = 2)^52), digits = 4)
            // [1] 1.0000 0.9999 0.9726 0.6978 0.2767 0.0678 0.0120 0.0016 0.0002
        int roll = sim.nextGaussianRange(monogamousMin,
                monogamousMax,false, true ,0, 
                ((monogamousMax-monogamousMin)/5), 0);
        return roll > monogamous;
    }
    /**
     * Tests whether or not the agent defined by this personality desires a connection with Agent a
     * @param sim The Sim. Provides access to system variables and random number generation.
     * @param a The Agent to test the connection with. This allows for comparisons between personality and preferences.
     * @return True if they want the connection false if they do not. 
     */
    public boolean wantsConnection(HIVMicroSim sim, Agent a){
        if(!self.acceptsGender(a.isFemale()))return false;
        if(self.pp.seroSort){
            //doesn't matter if they are infected, they need to know they are infected.
            if(!a.isKnown()) return false;
        }
        int sorting = 0;
        int sortVal = 0;
        if(sim.ageSorting){
            sorting += 10;
            /*
                Every 5 years difference results in an additional point.
            */
            sortVal += (int)(Math.abs(a.getAge()-self.getAge())/5);
        }
        if(sim.personalitySorting){
            sorting += (Personality.libidoMax + (Personality.coitalLongevityMax/2) + (Personality.monogamousMax/2) + (Personality.condomMax/2));
            //One point per 1 point difference, up to libidoMax (7 pt scale)
            sortVal += (int) (Math.abs(libido - a.getLibido()));
            //One point per 2 point difference (currently 10 pt scale)
            sortVal += (int) (Math.abs(coitalLongevity - a.getCoitalLongevity())/2);
            //One point per 2 point difference (currenlty 10 pt scale)
            sortVal += (int) (Math.abs(monogamous - a.getMonogamous())/2);
            //One point per 0.2 point difference. (currently 0-1 scale)
            sortVal += (int) (Math.abs(condomUse - a.getCondomUse())*5);
        }
        //random number from a normal distribution. 
        //Uniform distribution had some issues with the personality sorting being so large.
        return  sim.nextGaussianRange(0, sorting, 0, false, true) >= sortVal;
    }
    
    public void hinderLibido(double a){
        libido = (baseLibido * a);
    }
    public void seroSort(){
        seroSort = true;
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.Agent;

import Neighborhoods.NeighborhoodTemplates;
import hivMicroSim.HIVMicroSim;

/**
 *
 * @author manuells
 */
public abstract class Gene {
    //this is a "static" class used to store gene variants. This might later
    //be integrated into the Agent class for more speed, but for now it creates a static
    //area where gene types are stored.
    
    //effect Zone = X'4', R'5'
    //effect - s - susceptibility p-progrnosis b- both
    
    //CCR5
    public static final String CCR5EffectString = "R5 Suceptibility and progression";
    public static final char CCR5Effect = 'b';
    public static final byte CCR5EffectZone = 5; //R5
    public static final byte CCR5WildType = 0;
    //http://www.ncbi.nlm.nih.gov/pmc/articles/PMC3958329/table/t3-gmb-37-7/ (rates per race) HHG*2 is CCR5D32
    public static final byte CCR5D32 = 1; // 8% allele frequency in europeans ~ 1% or less in other races. 
    public static final double CCR5D32Effect = .5;
    public static final byte CCR5HHEP1 = 2;
    public static final double CCR5HHEP1Effect = 1.5; // 32% in european http://www.ncbi.nlm.nih.gov/pmc/articles/PMC3958329/
    
    //CCR2 --- http://www.ncbi.nlm.nih.gov/pmc/articles/PMC3958329/
    /*
    CCR2- V64I - Linked with CCR5 SNP -- 13%
    */
    public static final String CCR2EffectString = "R5 Suceptibility and progression";
    public static final char CCR2Effect = 'b';
    public static final byte CCR2EffectZone = 5; //R5
    public static final byte CCR2WildType = 0;
    public static final byte CCR2V64I = 1;
    public static final double CCR2V64IEffect = .8;
    
    //SDF1-3A --X 4 resistance .289 in asians
    //http://www.ncbi.nlm.nih.gov/pubmed/15192272
    //HLA
    /*
    http://ghr.nlm.nih.gov/geneFamily/hla
    In humans, the MHC complex consists of more than 200 genes located close together on chromosome 6. 
    Genes in this complex are categorized into three basic groups: class I, class II, and class III.
    http://jid.oxfordjournals.org/content/191/Supplement_1/S68.long
    Class 1 (HLA A, HLA B, HLA C)HIV. Homozygous class 1 alleles with rapid disease progression.
    Concordance in HLA antigens between partners increases seroconversion risk 
    http://www.ebi.ac.uk/ipd/imgt/hla/stats.html
     --- There are a few too many of these to really map out in a model..... 
    --For the moment lets go ahead and give A B and C 100 variations and mostly differentiate the variations that research
    has shown effects HIV. 
    http://jid.oxfordjournals.org/content/188/6/856.full
    B22 Sergroup RH 1.76 (heterozygous)
    B27 or 57 - RH 0.5
    B35 or 53 - RH 1.53
    Class I homozygous - RH: 1.31
    HLA Cw0303 - 1.42
    
    
    http://bmcimmunol.biomedcentral.com/articles/10.1186/1471-2172-9-1
    Supertypes!! Thank goodness I found this... For now thi will be MUCH easier to program than trying to do a subset of subtypes. 
    */
    //A     
        //Subtype6802
    public static final String HLA6802SiteEffectString = "Mostly beneficial against susceptibility and MTC transmission reduced progression."
            + " May have increased progression with subtype C.";
    public static final char HLA6802SiteEffect = 'b';
    public static final double HLA6802Effect = .45;
    public static final byte HLAA02 = 2;//supertype HLA A02 contains HLA A2-6802
    public static final byte HLAA01 = 1;
    public static final byte HLAA01A03 = 13;
    public static final byte HLAA01A24 = 124;
    public static final byte HLAA03 = 3;
    public static final byte HLAA24 = 24;
    
    //B
    //http://www.ncbi.nlm.nih.gov/pubmed/22139851
    //The age-adjusted US prevalence of B27 was 6.1% (95% confidence interval [95% CI] 4.6-8.2). By race/ethnicity, the prevalence of B27 was 7.5% (95% CI 5.3-10.4) among non-Hispanic whites and 3.5% (95% CI 2.5-4.8) among all other US races/ethnicities combined. In Mexican Americans, the prevalence was 4.6% (95% CI 3.4-6.1). 
    //http://www.ncbi.nlm.nih.gov/pubmed/20375757
    /*
     The overall estimate of HLA-B*5701 prevalence in Europe was 4.98%, with country-specific estimates ranging from 1.53 to 7.75%. HLA-B*5701 prevalence was highest in the self-reported white population (6.49%) and lowest in the black population (0.39%). 
    http://jid.oxfordjournals.org/content/188/6/856/F3.expansion.html
    */
    public static final byte HLAB07 = 7;
    public static final double HLAB07Effect = 1.72;
    public static final byte HLAB8 = 8;
    public static final byte HLAB27 = 27; 
    public static final double HLAB27Effect = .5;
    public static final byte HLAB44 = 44;
    public static final byte HLAB58 = 58; //contains B57
    public static final double HLAB58Effect = .5;
    public static final byte HLAB62 = 62;
    
    //C
    public static final byte HLACw0303 = 3;
    public static final double HLACw0303Effect = 1.42;
    //no supertypes listed for c... adding 5 other random ones
    public static final byte HLAC1 = 1;
    public static final byte HLAC2 = 2;
    public static final byte HLAC4 = 4;
    public static final byte HLAC5 = 5;
    public static final byte HLAC6 = 6;
    
    //Prevalences:
    //allele frequency CCR5
    //https://www.researchgate.net/profile/Srinivas_Mummidi/publication/12781086_Race-specific_HIV-1_disease-modifying_effects_associated_with_CCR5_haplotypes/links/00b49521f635b4d0b1000000.pdf
    //0-CCR5- HHE, 1-CCR5 D32, 2- CCR2V61I, 3- HLA-A02, 4- , 5- , 6- , 7- 
    public static final double[] prevBlack = {.19,.03, .308, .16, .04, .061, .047, .04};
    public static final double[] prevCaucassian = {.32, .08, .13, .16, .04, .061, .047, .04};
    public static final double[] prevHispanic = {.25, .057, .1812, .16, .04, .061, .047, .04};
    public static final double[] prevAsian = {.25, .01, .11, .16, .04, .061, .047, .04};
    /*
    //hla factors
    //HLA A - need other race factors for these
    public static final double hlaA02Prev = .16;
    //HLA B
    public static final double hlaB7Prev = .04;
    public static final double hlaB27Prev = .061;
    public static final double hlaB58Prev = .047;
    //HLA C
    public static final double hlaCw0303Prev = .04; //need data for this 
    
    //I'm creating these methods to simplify initial gene creation in the main method. 
    //I don't want to have to program all of these into the main method, easier to call it from here and keep 
    //all of the gene stuff in one place.
    */
    private static double[] getRaceSet(int race){
        switch(race){
            case NeighborhoodTemplates.Race_Caucasian:
                return prevCaucassian;
            case NeighborhoodTemplates.Race_Black:
                return prevBlack;
            case NeighborhoodTemplates.Race_Hispanic:
                return prevHispanic;
            default:
                //for now
                return prevAsian;
        }
    }
    public static GeneProfile getGeneProfile(HIVMicroSim sim, int race){
        double[] prevs = getRaceSet(race);
        double roll;
        double b;
        byte CCR51, CCR52, CCR21, CCR22, HLAA1, HLAA2, HLAB1, HLAB2, HLA_C1, HLA_C2;
        //CCR5
        roll = sim.random.nextDouble();
        double L = prevs[0];
        if(roll < L) CCR51 = CCR5HHEP1;
        else{
            L += prevs[1];
            if(roll < L) CCR51 = CCR5D32;
            else CCR51 = CCR5WildType;
        }
        
        roll = sim.random.nextDouble();
        L = prevs[0];
        if(roll < L) CCR52 = CCR5HHEP1;
        else{
            L += prevs[1];
            if(roll < L) CCR52 = CCR5D32;
            else CCR52 = CCR5WildType;
        }
        
        //CCR2
        roll = sim.random.nextDouble();
        if(roll < prevs[2]) CCR21 = CCR2V64I;
        else CCR21 = CCR2WildType;
        
        roll = sim.random.nextDouble();
        if(roll < prevs[2]) CCR22 = CCR2V64I;
        else CCR22 = CCR2WildType;
        
        //HLAA
        roll = sim.random.nextDouble();
        L = prevs[3];
        if(roll < L) HLAA1 = HLAA02;
        else{
            b = (1-L)/5;
            L += b;
            if(roll < L) HLAA1 = HLAA01;
            else{
                L += b;
                if(roll < L) HLAA1 = HLAA01A03;
                else{
                    L += b;
                    if(roll < L) HLAA1 = HLAA01A24;
                    else{
                        L += b;
                        if(roll < L) HLAA1 = HLAA03;
                        else HLAA1 = HLAA24;
                    }
                }
            }
        }
        
        roll = sim.random.nextDouble();
        L = prevs[3];
        if(roll < L) HLAA2 = HLAA02;
        else{
            b = (1-L)/5;
            L += b;
            if(roll < L) HLAA2 = HLAA01;
            else{
                L += b;
                if(roll < L) HLAA2 = HLAA01A03;
                else{
                    L += b;
                    if(roll < L) HLAA2 = HLAA01A24;
                    else{
                        L += b;
                        if(roll < L) HLAA2 = HLAA03;
                        else HLAA2 = HLAA24;
                    }
                }
            }
        }
        
        //HLAB
        roll = sim.random.nextDouble();
        L = prevs[4];
        if(roll < L) HLAB1 = HLAB07;
        else{
            L += prevs[5];
            if(roll < L) HLAB1 = HLAB27;
            else{
                L += prevs[6];
                if(roll < L) HLAB1 = HLAB58;
                else{
                    b = (1-L)/3;
                    L += b;
                    if(roll < L) HLAB1 = HLAB8;
                    else{
                        L += b;
                        if(roll < L) HLAB1 = HLAB44;
                        else HLAB1 = HLAB62;
                    }
                }
            }
        }
        roll = sim.random.nextDouble();
        L = prevs[4];
        if(roll < L) HLAB2 = HLAB07;
        else{
            L += prevs[5];
            if(roll < L) HLAB2 = HLAB27;
            else{
                L += prevs[6];
                if(roll < L) HLAB2 = HLAB58;
                else{
                    b = (1-L)/3;
                    L += b;
                    if(roll < L) HLAB2 = HLAB8;
                    else{
                        L += b;
                        if(roll < L) HLAB2 = HLAB44;
                        else HLAB2 = HLAB62;
                    }
                }
            }
        }
        //HLA C
        roll = sim.random.nextDouble();
        L = prevs[7];
        if(roll < L) HLA_C1 = HLACw0303;
        else{
            b = (1-L)/5;
            L += b;
            if(roll < L) HLA_C1 = HLAC1;
            else{
                L += b;
                if(roll < L) HLA_C1 = HLAC2;
                else{
                    L += b;
                    if(roll < L) HLA_C1 = HLAC4;
                    else{
                        L += b;
                        if(roll < L) HLA_C1 = HLAC5;
                        else HLA_C1 = HLAC6;
                    }
                }
            }
        }
        roll = sim.random.nextDouble();
        L = prevs[7];
        if(roll < L) HLA_C2 = HLACw0303;
        else{
            b = (1-L)/5;
            L += b;
            if(roll < L) HLA_C2 = HLAC1;
            else{
                L += b;
                if(roll < L) HLA_C2 = HLAC2;
                else{
                    L += b;
                    if(roll < L) HLA_C2 = HLAC4;
                    else{
                        L += b;
                        if(roll < L) HLA_C2 = HLAC5;
                        else HLA_C2 = HLAC6;
                    }
                }
            }
        }
        return(new GeneProfile(CCR51, CCR52, CCR21, CCR22, HLAA1, HLAA2, HLAB1, HLAB2, HLA_C1, HLA_C2));
    }
    
    public static String getCCR5(byte val){
        switch(val){
            case CCR5D32:
                return "CCR5 Delta 32";
            case CCR5HHEP1:
                return "CCR5 Haplotype HHE";
            default:
                return "CCR5 Wild Type";
        }
    }
    public static String getCCR2(byte val){
        switch(val){
            case CCR2V64I:
                return "CCR2 V64I";
            
            default:
                return "CCR2 Wild Type";
        }
    }
    public static String getHLA_A(byte val){
        switch(val){
            case HLAA01:
                return "HLA A01";
            case HLAA02:
                return "HLA A02";
            case HLAA03:
                return "HLA A03";
            case HLAA24:
                return "HLA A24";
            case HLAA01A03:
                return "HLA A01 A03";
            case HLAA01A24:
                return "HLA A01 A24";
            default:
                return "Error " + val + " not valid for HLA A!";
        }
    }
    public static String getHLA_B(byte val){
        switch(val){
            case HLAB07:
                return "HLA B07";
            case HLAB27:
                return "HLA B27";
            case HLAB44:
                return "HLA B44";
            case HLAB58:
                return "HLA B58";
            case HLAB62:
                return "HLA B62";
            case HLAB8:
                return "HLA B8";
            default:
                return "Error " + val + " not valid for HLA B!";
        }
    }
    public static String getHLA_C(byte val){
        switch(val){
            case HLAC1:
                return "HLA C1";
            case HLAC2:
                return "HLA C2";
            case HLACw0303:
                return "HLA Cw0303";
            case HLAC4:
                return "HLA C4";
            case HLAC5:
                return "HLA C5";
            case HLAC6:
                return "HLA C6";
            default:
                return "Error " + val + " not valid for HLA C!";
        }
    }
}

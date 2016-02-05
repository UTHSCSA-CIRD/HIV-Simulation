/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.Agent;

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
    public static final double CCR5HHEP1Effect = 1.5; // 32% in europeanhttp://www.ncbi.nlm.nih.gov/pmc/articles/PMC3958329/
    
    //CCR2 --- http://www.ncbi.nlm.nih.gov/pmc/articles/PMC3958329/
    /*
    CCR2- V64I - Linked with CCR5 SNP -- 13%
    */
    
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
    public static final byte HLA02 = 2;//supertype HLA A02 contains HLA A2-6802
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
    public static final double HLA58Effect = .5;
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
    
}

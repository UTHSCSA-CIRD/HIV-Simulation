/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.Agent;

import Neighborhoods.Neighborhood;

/**
 *
 * @author ManuelLS
 */
public class GeneProfile {
    //Genetic factors
    public final byte ccr51, ccr52;//first and second allele ccr5 one is randomly chosen to be passed on.
    public final byte ccr21, ccr22;
    public final byte HLA_A1, HLA_A2;
    public final byte HLA_B1, HLA_B2;
    public final byte HLA_C1, HLA_C2;
    
    public GeneProfile(byte ccr51, byte ccr52, byte ccr21, byte ccr22,byte HLAA1, byte HLAA2, byte HLAB1, 
            byte HLAB2, byte HLAC1, byte HLAC2){
        
        this.ccr51 = ccr51;
        this.ccr52 = ccr52;
        this.ccr21 = ccr21;
        this.ccr22 = ccr22;
        HLA_A1 = HLAA1; HLA_A2 = HLAA2; HLA_B1 = HLAB1; HLA_B2 = HLAB2; HLA_C1 = HLAC1; HLA_C2 = HLAC2;
    }
}

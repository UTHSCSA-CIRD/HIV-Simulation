/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.Agent;

/**
 *
 * @author ManuelLS
 */
public class Pregnancy implements java.io.Serializable{
    private final static long serialVersionUID = 1;
 
    protected final int mother, father;
    private final byte ccr51, ccr52;
    protected final byte ccr21, ccr22;
    protected final byte HLA_A1, HLA_A2;
    protected final byte HLA_B1, HLA_B2;
    protected final byte HLA_C1, HLA_C2;
    private int month;
    
    public boolean step(){
        month++;
        return month >=9;
    }
    public int getMother(){return mother;}
    public int getFather() {return father;}
    public byte getCCR51(){
        return ccr51;
    }
    public byte getCCR52(){
        return ccr52;
    }
    public byte getCCR21(){
        return ccr21;
    }
    public byte getCCR22(){
        return ccr22;
    }
    public byte getHLAA1(){
        return HLA_A1;
    }
    public byte getHLAA2(){
        return HLA_A2;
    }
    public byte getHLAB1(){
        return HLA_B1;
    }
    public byte getHLAB2(){
        return HLA_B2;
    }
    public byte getHLAC1(){
        return HLA_C1;
    }
    public byte getHLAC2(){
        return HLA_C2;
    }
    public Pregnancy(int mother, int father, byte ccr51, byte ccr52, byte ccr21, byte ccr22,byte HLAA1, byte HLAA2, byte HLAB1, byte HLAB2, 
            byte HLAC1, byte HLAC2){
        this.ccr51 = ccr51;
        this.ccr52 = ccr52;
        this.ccr21 = ccr21;
        this.ccr22 = ccr22;
        this.mother = mother;
        this.father = father;
        HLA_A1 = HLAA1; HLA_A2 = HLAA2; HLA_B1 = HLAB1; HLA_B2 = HLAB2; HLA_C1 = HLAC1; HLA_C2 = HLAC2;
        month = 0;
    }
    
    
}

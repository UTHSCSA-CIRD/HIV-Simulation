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
    
    private final double immuneFactors;
    private final boolean ccr51, ccr52;
    private int month;
    public boolean step(){
        month++;
        return month >=9;
    }
    public double getImmuneFactors(){
        return immuneFactors;
    }
    public boolean getCCR51(){
        return ccr51;
    }
    public boolean getCCR52(){
        return ccr52;
    }
    public Pregnancy(double immuneFactors, boolean ccr51, boolean ccr52){
        this.immuneFactors = immuneFactors;
        this.ccr51 = ccr51;
        this.ccr52 = ccr52;
        month = 0;
    }
    
    
}

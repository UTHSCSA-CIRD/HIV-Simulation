/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.HIV;

import hivMicroSim.DebugLogger;
import hivMicroSim.HIVMicroSim;
import java.nio.ByteBuffer;

/**
 *
 * @author ManuelLS
 */
public abstract class Antigen {
    //The Antigen class is going to be mostly like the "gene" class
    //It exists to store global values and methods... mostly methods.
    /*I know-- I'm a terrible programmer. I should have the bitcodes memorized. 
        0
        0000
        1
        0001
        2
        0010
        3
        0011
        4
        0100
        5
        0101
        6
        0110
        7
        0111
        8
        1000
        9
        1001
        A
        1010
        B
        1011
        C
        1100
        D
        1101
        E
        1110
        F
        1111
    */
    public static int getDiff(int a,int b){
        int c = a^b;
        return Integer.bitCount(c);
    }
    public static double getSimilarityP(int a, int b){
        //returns the percent similarity between the two strains.
        return (32.0-getDiff(a,b))/32.0;
    }
    public static int changeSingleByte(int a, HIVMicroSim sim){
        //accepts an integer to change a byte on and a sim from which to pull a random number and post to debug
       int rand = sim.random.nextInt(32);
       byte[] buff = ByteBuffer.allocate(4).putInt(a).array();
       String debug = "HIV Antigen code: " + Integer.toBinaryString(a) + " converted to ";
       buff[rand] += 1;//0->1 1->0
       sim.debugLog.insertDebug(debug + ByteBuffer.wrap(buff).toString(), DebugLogger.LOG_ALL);
       return ByteBuffer.wrap(buff).getInt();
    }
    public static int changeSingleByte(int a, int rand){
        //accepts an integer to change a byte on and a random number between 0 and 32 (noninclusive)
        //which is to be changed. 
       byte[] buff = ByteBuffer.allocate(4).putInt(a).array();
       buff[rand] += 1;//0->1 1->0
       return ByteBuffer.wrap(buff).getInt();
    }
}

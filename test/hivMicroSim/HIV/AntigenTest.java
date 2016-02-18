/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hivMicroSim.HIV;

import hivMicroSim.HIVMicroSim;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ManuelLS
 */
public class AntigenTest {
    
    public AntigenTest() {
    }

    /**
     * Test of getDiff method, of class Antigen.
     */
    @Test
    public void testGetDiff() {
        System.out.println("getDiff");
        int a = 0;
        int b = 0;
        int expResult = 0;
        int result = Antigen.getDiff(a, b);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSimilarityP method, of class Antigen.
     */
    @Test
    public void testGetSimilarityP() {
        System.out.println("getSimilarityP");
        int a = 0;
        int b = 0;
        double expResult = 1;
        double result = Antigen.getSimilarityP(a, b);
        assertEquals(expResult, result, 0.0);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    
}

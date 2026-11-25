package com.example.firewall.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SimulatorTest {

    @Test
    public void testAttackObjectCreation() {
        Simulator.Attack attack = new Simulator.Attack("SQLi Test", "http://localhost:7080/auth", "POST", "{\"id\":1}");
        assertEquals("SQLi Test", attack.name);
        assertEquals("http://localhost:7080/auth", attack.url);
        assertEquals("POST", attack.method);
        assertEquals("{\"id\":1}", attack.payload);
    }

    @Test
    public void testEncodedAttackConstants() {
        assertNotNull(ConstantsInUse.ENCODEDQUERY);
        assertTrue(ConstantsInUse.ENCODEDQUERY.contains("OR"));

        assertNotNull(ConstantsInUse.ENCODEDXSS);
        assertTrue(ConstantsInUse.ENCODEDXSS.contains("script"));

        assertNotNull(ConstantsInUse.ENCODEDPATH);
        assertTrue(ConstantsInUse.ENCODEDPATH.contains("etc"));
    }

    @Test
    public void testSimulationResultHandling() {
        Simulator.SimulationResult resBlocked = new Simulator.SimulationResult("SQLi", "http://localhost:7080/auth", 403, true, null);
        assertEquals(403, resBlocked.responseCode);
        assertTrue(resBlocked.isBlocked);
        assertNull(resBlocked.errorMessage);

        Simulator.SimulationResult resAllowed = new Simulator.SimulationResult("Clean", "http://localhost:7080/auth", 200, false, null);
        assertEquals(200, resAllowed.responseCode);
        assertFalse(resAllowed.isBlocked);
    }
}

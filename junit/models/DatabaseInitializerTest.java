// File: junit/models/DatabaseInitializerTest.java
package models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseInitializerTest {
    @Test
    public void testInitializeRuns() {
        assertDoesNotThrow(() -> DatabaseInitializer.initialize());
    }
}

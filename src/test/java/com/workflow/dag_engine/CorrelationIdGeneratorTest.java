package com.workflow.dag_engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.workflow.dag_engine.helpers.CorrelationIdGenerator;

public class CorrelationIdGeneratorTest {

    @Test
    public void testCorrelationIdFormat() {
        String id1 = CorrelationIdGenerator.generateCorrelationId();
        String id2 = CorrelationIdGenerator.generateCorrelationId();

        assertEquals(27, id1.length(), "Correlation ID must be exactly 27 digits long");
        assertEquals(27, id2.length(), "Correlation ID must be exactly 27 digits long");

        assertTrue(id1.matches("^\\d{27}$"), "Correlation ID must be only numeric digits");
        assertTrue(id2.matches("^\\d{27}$"), "Correlation ID must be only numeric digits");

        assertTrue(id2.compareTo(id1) >= 0, "Correlation ID should be sequential");
    }

}

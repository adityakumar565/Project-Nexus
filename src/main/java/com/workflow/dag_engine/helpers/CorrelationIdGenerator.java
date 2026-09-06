package com.workflow.dag_engine.helpers;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class CorrelationIdGenerator {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final AtomicLong SEQUENCE = new AtomicLong(1);
    private static final long MAX_SEQUENCE = 10_000_000_000L; // 10 digits max

    /**
     * Generates a 27-digit sequential correlation ID.
     * Consists of:
     * - 17 digits: Timestamp in 'yyyyMMddHHmmssSSS' format
     * - 10 digits: Zero-padded atomic sequential counter
     */
    public static String generateCorrelationId() {
        String timestampPart = LocalDateTime.now().format(TIMESTAMP_FORMATTER); // 17 digits
        long seq = SEQUENCE.getAndIncrement() % MAX_SEQUENCE;
        String sequencePart = String.format("%010d", seq); // 10 digits
        return timestampPart + sequencePart; // exactly 27 digits
    }

}

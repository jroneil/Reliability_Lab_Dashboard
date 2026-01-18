package com.lab.reliability.lab;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.LongAdder;

public class TimeBucket {
    private final int tSec;
    private final LongAdder successCount = new LongAdder();
    private final LongAdder errorCount = new LongAdder();
    private final List<Long> latencies = Collections.synchronizedList(new ArrayList<>());

    public TimeBucket(int tSec) {
        this.tSec = tSec;
    }

    public void recordSuccess(long latencyMs) {
        successCount.increment();
        latencies.add(latencyMs);
    }

    public void recordError() {
        errorCount.increment();
    }

    public int getTSec() {
        return tSec;
    }

    public long getSuccesses() {
        return successCount.sum();
    }

    public long getErrors() {
        return errorCount.sum();
    }

    public long getTotal() {
        return getSuccesses() + getErrors();
    }

    public double getPercentile(double percentile) {
        synchronized (latencies) {
            if (latencies.isEmpty())
                return 0;
            List<Long> sorted = new ArrayList<>(latencies);
            Collections.sort(sorted);
            int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
            return sorted.get(Math.max(0, index));
        }
    }
}

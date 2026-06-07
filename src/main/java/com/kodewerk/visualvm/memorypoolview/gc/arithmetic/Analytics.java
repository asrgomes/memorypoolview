/*
 * Copyright (c) 2011-2026, Kirk Pepperdine.
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License (the "License").
 * You may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the license at http://www.opensource.org/licenses/CDDL-1.0.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL HEADER in each
 * file and include the License file.
 * If applicable, add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your own identifying
 * information: Portions Copyright [yyyy] [name of copyright owner]
 */
package com.kodewerk.visualvm.memorypoolview.gc.arithmetic;

/// Fixed-size rolling analytics for chart indicator series.
public final class Analytics {

    private final int period;
    private final long[] samples;

    private long sum = 0;
    private int currentIndex;

    /// Creates a rolling analytics buffer with the supplied sample period.
    public Analytics(int period) {
        if (period <= 0) {
            throw new IllegalArgumentException("period must be greater than zero");
        }
        this.period = period;
        this.samples = new long[period];
        this.currentIndex = period - 1;
    }

    /// Returns the sample at a circular index relative to the buffer.
    public long getSampleAt(int index) {
        return samples[(index + period) % period];
    }

    /// Adds a new sample and evicts the oldest sample from the rolling window.
    public void add(long newSample) {
        var nextIndex = (currentIndex + 1) % period;
        sum = sum - samples[nextIndex] + newSample;
        currentIndex = nextIndex;
        samples[currentIndex] = newSample;
    }

    /// Returns the latest sample as a percentage of the previous sample.
    public double getSimpleMomentum() {
        var divisor = getSampleAt(currentIndex - 1);
        if (divisor == 0) {
            return 0.0d;
        }
        return ((double) getSampleAt(currentIndex) / (double) divisor) * 100.0d;
    }

    /// Returns the arithmetic mean of the current rolling window.
    public long getMovingAverage() {
        return sum / period;
    }
}

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
package com.kodewerk.visualvm.memorypoolview;

/// Memory-pool chart panel that adds the analytic indicator series.
public class MemoryPoolAnalyticPanel extends MemoryPoolPanel implements MemoryPoolModelListener {

    /// Creates a memory-pool chart with an indicator line.
    public MemoryPoolAnalyticPanel() {
        super("Indicator (sample size: " + MemoryPoolAnalyticModel.MOVING_AVERAGE_PERIOD + ")", "Indicator");
    }

    /// Adds the latest memory-pool and indicator samples to the chart.
    @Override
    public void memoryPoolUpdated(MemoryPoolModel model) {
        if (!(model instanceof MemoryPoolAnalyticModel analyticModel)) {
            super.memoryPoolUpdated(model);
            return;
        }

        var movingAverage = analyticModel.getMovingAverage();
        var dataPoints = new long[]{model.getCommitted(), model.getUsed(), movingAverage};
        var details = new String[]{
                formatBytes(model.getCommitted()),
                formatBytes(model.getUsed()),
                formatBytes(model.getMax()),
                formatBytes(movingAverage)
        };
        updateChart(dataPoints, details);
    }
}

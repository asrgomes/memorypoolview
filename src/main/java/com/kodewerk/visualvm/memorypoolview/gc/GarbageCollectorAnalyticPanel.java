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
package com.kodewerk.visualvm.memorypoolview.gc;

import org.graalvm.visualvm.charts.SimpleXYChartDescriptor;

import static com.kodewerk.visualvm.memorypoolview.gc.GarbageCollectorDurationPanel.ONE_MEGABYTE_SIZE;

/// GC duration chart panel that adds the analytic indicator series.
public class GarbageCollectorAnalyticPanel extends GarbageCollectorDurationPanel {

    /// Creates a duration chart with an indicator line.
    public GarbageCollectorAnalyticPanel() {
        super(createAnalyticDescriptor());
    }

    private static SimpleXYChartDescriptor createAnalyticDescriptor() {
        var description = SimpleXYChartDescriptor.decimal(ONE_MEGABYTE_SIZE, false, 1000);

        description.addLineItems("Last GC duration");
        description.addLineItems("Indicator (sample size: " + GarbageCollectorAnalyticModel.MOVING_AVERAGE_PERIOD + ")");
        description.setDetailsItems(new String[]{"Last duration", "Number of collections", "Total time in GC",
                "Indicator"});
        return description;
    }

    /// Adds the latest duration and indicator samples to the chart.
    @Override
    public void garbageCollectorUpdated(GarbageCollectionModel model) {
        if (!(model instanceof GarbageCollectorAnalyticModel analyticModel)) {
            super.garbageCollectorUpdated(model);
            return;
        }

        var movingAverage = analyticModel.getMovingAverage();
        var dataPoints = new long[]{model.getLastDuration(), movingAverage};
        super.getChart().addValues(System.currentTimeMillis(), dataPoints);

        var details = new String[]{
                Collection.formatNumber(model.getLastDuration()) + " ms",
                String.valueOf(model.getCount()),
                Collection.formatNumber(model.getTotalDuration()) + " ms",
                String.valueOf(movingAverage)
        };
        getChart().updateDetails(details);
    }
}

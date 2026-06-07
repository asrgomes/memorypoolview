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

import org.graalvm.visualvm.charts.ChartFactory;
import org.graalvm.visualvm.charts.SimpleXYChartDescriptor;
import org.graalvm.visualvm.charts.SimpleXYChartSupport;

import java.awt.BorderLayout;
import javax.swing.JPanel;

/// Swing chart panel for the latest duration of one garbage collector.
public class GarbageCollectorDurationPanel extends JPanel implements GarbageCollectorModelListener {
    static final long ONE_MEGABYTE_SIZE = 1048576;

    private final SimpleXYChartSupport chart;

    /// Creates a chart with last duration, collection count, and total duration details.
    public GarbageCollectorDurationPanel() {
        this(createDurationDescriptor());
    }

    protected GarbageCollectorDurationPanel(SimpleXYChartDescriptor description) {
        setLayout(new BorderLayout());
        chart = ChartFactory.createSimpleXYChart(description);
        add(chart.getChart(), BorderLayout.CENTER);
    }

    private static SimpleXYChartDescriptor createDurationDescriptor() {
        var description = SimpleXYChartDescriptor.decimal(ONE_MEGABYTE_SIZE, false, 1000);

        description.addLineItems("Last GC duration");
        description.setDetailsItems(new String[]{"Last duration", "Number of collections", "Total time in GC"});
        return description;
    }

    protected SimpleXYChartSupport getChart() {
        return chart;
    }

    /// Adds the latest garbage-collection duration sample to the chart.
    @Override
    public void garbageCollectorUpdated(GarbageCollectionModel model) {
        var dataPoints = new long[]{model.getLastDuration()};
        chart.addValues(System.currentTimeMillis(), dataPoints);

        var details = new String[]{
                Collection.formatNumber(model.getLastDuration()) + " ms",
                String.valueOf(model.getCount()),
                Collection.formatNumber(model.getTotalDuration()) + " ms"
        };
        getChart().updateDetails(details);
    }
}

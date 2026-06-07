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

/// Swing chart panel for observed garbage-collection frequency.
class GarbageCollectionFrequencyPanel extends JPanel implements GarbageCollectorModelListener {

    private static final long ONE_MEGABYTE_SIZE = 1048576;

    private final SimpleXYChartSupport chart;

    GarbageCollectionFrequencyPanel() {
        setLayout(new BorderLayout());
        var description = SimpleXYChartDescriptor.decimal(ONE_MEGABYTE_SIZE, false, 1000);

        description.addLineItems("GC Frequency");
        description.setDetailsItems(new String[]{"Current Frequency"});

        chart = ChartFactory.createSimpleXYChart(description);
        add(chart.getChart(), BorderLayout.CENTER);
    }

    /// Adds the latest garbage-collection frequency sample to the chart.
    @Override
    public void garbageCollectorUpdated(GarbageCollectionModel model) {
        var frequency = model.getFrequency();
        var dataPoints = new long[]{frequency};
        chart.addValues(System.currentTimeMillis(), dataPoints);

        var details = new String[]{Collection.formatNumber(frequency) + " collections per second"};
        chart.updateDetails(details);
    }
}

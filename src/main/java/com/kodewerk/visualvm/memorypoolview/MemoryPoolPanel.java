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

import org.graalvm.visualvm.charts.ChartFactory;
import org.graalvm.visualvm.charts.SimpleXYChartDescriptor;
import org.graalvm.visualvm.charts.SimpleXYChartSupport;

import javax.swing.JPanel;
import java.awt.BorderLayout;

/// Swing chart panel for a single memory pool.
public class MemoryPoolPanel extends JPanel implements MemoryPoolModelListener {
    private static final long ONE_MEGABYTE_SIZE = 1048576;

    private final SimpleXYChartSupport chart;

    /// Creates a chart with committed and used memory series.
    public MemoryPoolPanel() {
        this(new String[]{"Memory Pool Size", "Memory Pool Used"}, new String[]{"Size", "Used", "Max"});
    }

    /// Creates a chart with an additional line item and details field.
    public MemoryPoolPanel( String additionalLineItem, String additionalDetails) {
        this(new String[]{"Memory Pool Size", "Memory Pool Used", additionalLineItem}, new String[]{"Size", "Used", "Max", additionalDetails});
    }

    MemoryPoolPanel( String[] lineItems, String[] details) {
        setLayout(new BorderLayout());
        var description = SimpleXYChartDescriptor.bytes(ONE_MEGABYTE_SIZE, false, 1000);

        for (var lineItem : lineItems) {
            description.addLineItems(lineItem);
        }

        description.setDetailsItems(details);
        chart = ChartFactory.createSimpleXYChart(description);
        add(chart.getChart(), BorderLayout.CENTER);
    }

    /// Formats a byte count using the same units as the chart.
    public String formatBytes(long value) {
        return chart.formatBytes(value);
    }

    protected void updateChart(long[] values, String[] details) {
        chart.addValues(System.currentTimeMillis(), values);
        chart.updateDetails(details);
    }

    /// Adds the latest memory-pool sample to the chart.
    @Override
    public void memoryPoolUpdated(MemoryPoolModel model) {
        var dataPoints = new long[]{model.getCommitted(), model.getUsed()};
        var details = new String[]{
                formatBytes(model.getCommitted()),
                formatBytes(model.getUsed()),
                formatBytes(model.getMax())
        };
        updateChart(dataPoints, details);
    }
}

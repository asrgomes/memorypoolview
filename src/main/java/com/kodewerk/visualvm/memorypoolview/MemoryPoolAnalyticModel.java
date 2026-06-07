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

import com.kodewerk.visualvm.memorypoolview.gc.arithmetic.Analytics;
import org.graalvm.visualvm.tools.jmx.JmxModel;

import java.io.IOException;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/// Memory-pool model that adds a moving indicator for the used-byte series.
public class MemoryPoolAnalyticModel extends MemoryPoolModel {

    /// Number of samples used for the analytic indicator.
    public static final int MOVING_AVERAGE_PERIOD = 10;

    private final Analytics movingAverage;

    /// Creates an analytic model for one memory-pool MBean.
    public MemoryPoolAnalyticModel(final ObjectName mbeanName, final JmxModel model, final MBeanServerConnection mbeanServerConnection) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException {
        super(mbeanName, model, mbeanServerConnection);
        movingAverage = new Analytics(MOVING_AVERAGE_PERIOD);
    }

    /// Returns the current percentage momentum indicator for used memory.
    public long getMovingAverage() {
        return (long) movingAverage.getSimpleMomentum();
    }

    @Override
    protected void tickleListeners() {
        movingAverage.add(getUsed());
        super.tickleListeners();
    }
}

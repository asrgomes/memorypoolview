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

import org.graalvm.visualvm.application.Application;
import org.graalvm.visualvm.core.ui.DataSourceView;
import org.graalvm.visualvm.core.ui.DataSourceViewProvider;
import org.graalvm.visualvm.core.ui.DataSourceViewsManager;
import org.graalvm.visualvm.tools.jmx.JmxModel;
import org.graalvm.visualvm.tools.jmx.JmxModelFactory;
import org.openide.util.Exceptions;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.io.IOException;

/// Registers the Garbage Collector Activity view for connected VisualVM applications.
public class GCViewProvider extends DataSourceViewProvider<Application> {

    private static final DataSourceViewProvider<Application> INSTANCE = new GCViewProvider();
    private static final ObjectName GARBAGE_COLLECTOR_PATTERN = createGarbageCollectorPattern();

    /// Returns whether the application has a connected JMX model with collector MBeans.
    @Override
    public boolean supportsViewFor(final Application application) {
        var jmx = JmxModelFactory.getJmxModelFor(application);
        if (jmx != null && jmx.getConnectionState() == JmxModel.ConnectionState.CONNECTED) {
            MBeanServerConnection connection = jmx.getMBeanServerConnection();
            if (connection == null) {
                return false;
            }
            try {
                var objectNames = connection.queryNames(GARBAGE_COLLECTOR_PATTERN, null);
                return !objectNames.isEmpty();
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return false;
    }

    /// Creates the view instance for the selected application.
    @Override
    public synchronized DataSourceView createView(final Application application) {
        return new GCView(application);

    }

    /// Registers this provider with VisualVM.
    public static void initialize() {
        DataSourceViewsManager.sharedInstance().addViewProvider(INSTANCE, Application.class);
    }

    /// Unregisters this provider from VisualVM.
    public static void unregister() {
        DataSourceViewsManager.sharedInstance().removeViewProvider(INSTANCE);
    }

    private static ObjectName createGarbageCollectorPattern() {
        try {
            return new ObjectName("java.lang:type=GarbageCollector,name=*");
        } catch (Exception e) {
            return ObjectName.WILDCARD;
        }
    }
}

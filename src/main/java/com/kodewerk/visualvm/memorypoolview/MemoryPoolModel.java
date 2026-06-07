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

import org.graalvm.visualvm.tools.jmx.CachedMBeanServerConnectionFactory;
import org.graalvm.visualvm.tools.jmx.JmxModel;
import org.graalvm.visualvm.tools.jmx.MBeanCacheListener;
import org.openide.util.Exceptions;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.lang.management.MemoryUsage;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/// Live model for one `MemoryPoolMXBean`.
///
/// Instances subscribe to VisualVM's cached JMX connection and notify registered
/// chart panels whenever fresh memory-usage data is available.
public class MemoryPoolModel implements MBeanCacheListener {

    private static final MemoryUsage UNKNOWN_MEMORY_USAGE = new MemoryUsage(0, 0, 0, -1);

    private final Set<MemoryPoolModelListener> listeners = new HashSet<>();
    private final String name;
    private final String type;
    private final ObjectName mbeanName;
    private final MBeanServerConnection mbeanServerConnection;

    private MemoryUsage memoryUsage = UNKNOWN_MEMORY_USAGE;

    /// Creates a model for the supplied memory-pool MBean and registers it for
    /// cached JMX refresh notifications.
    public MemoryPoolModel(final ObjectName mbeanName, final JmxModel model, final MBeanServerConnection mbeanServerConnection) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException {
        this.mbeanName = mbeanName;
        this.mbeanServerConnection = mbeanServerConnection;
        CachedMBeanServerConnectionFactory.getCachedMBeanServerConnection(model, 2000).addMBeanCacheListener(this);
        name = attributeText("Name", fallbackName(mbeanName));
        type = attributeText("Type", "unknown");
        memoryUsage = readMemoryUsage();
    }

    /// Returns the display name reported by the target JVM.
    public String getName() {
        return name;
    }

    /// Returns the memory-pool type reported by the target JVM.
    public String getType() {
        return type;
    }

    /// Returns the currently committed byte count.
    public long getCommitted() {
        return memoryUsage.getCommitted();
    }

    /// Returns the configured maximum byte count, or `-1` when undefined.
    public long getMax() {
        return memoryUsage.getMax();
    }

    /// Returns the currently used byte count.
    public long getUsed() {
        return memoryUsage.getUsed();
    }

    /// Registers a listener that should receive model refresh events.
    public void registerView(MemoryPoolModelListener listener) {
        listeners.add(listener);
    }

    /// Returns an iterator over the currently registered listeners.
    public Iterator<MemoryPoolModelListener> views() {
        return listeners.iterator();
    }

    protected void tickleListeners() {
        for (MemoryPoolModelListener listener : listeners) {
            listener.memoryPoolUpdated(this);
        }
    }

    /// Refreshes memory usage from JMX and notifies listeners.
    @Override
    public void flushed() {
        try {
            memoryUsage = readMemoryUsage();
            tickleListeners();
        } catch (Throwable t) {
            Exceptions.printStackTrace(Exceptions.attachMessage(t, "Exception recovering data from MemoryPoolMXBean"));
        }
    }

    private MemoryUsage readMemoryUsage() throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException {
        if (mbeanServerConnection.getAttribute(mbeanName, "Usage") instanceof CompositeData poolStatistics) {
            return MemoryUsage.from(poolStatistics);
        }
        return UNKNOWN_MEMORY_USAGE;
    }

    private String attributeText(String attributeName, String fallback) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException {
        var value = mbeanServerConnection.getAttribute(mbeanName, attributeName);
        return value == null ? fallback : value.toString();
    }

    private static String fallbackName(ObjectName mbeanName) {
        var keyPropertyName = mbeanName.getKeyProperty("name");
        return keyPropertyName == null ? mbeanName.getCanonicalName() : keyPropertyName;
    }

    /// Returns a concise diagnostic string with name, type, used, and committed bytes.
    @Override
    public String toString() {
        var buffer = new StringBuilder(getName());
        buffer.append(" : ").append(this.getType());
        buffer.append(" : ").append(this.getUsed()).append(" : ").append(this.getCommitted());
        return buffer.toString();
    }
}

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

import org.graalvm.visualvm.tools.jmx.CachedMBeanServerConnectionFactory;
import org.graalvm.visualvm.tools.jmx.JmxModel;
import org.graalvm.visualvm.tools.jmx.MBeanCacheListener;
import org.jspecify.annotations.Nullable;
import org.openide.util.Exceptions;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/// Live model for one `GarbageCollectorMXBean`.
///
/// The model tracks cumulative and latest collection durations, derives
/// collection frequency, and notifies registered chart panels after each
/// VisualVM JMX cache refresh.
public class GarbageCollectionModel implements MBeanCacheListener {

    private final Set<GarbageCollectorModelListener> listeners = new HashSet<>();
    private final ObjectName mbeanName;
    private final MBeanServerConnection mbeanServerConnection;
    private final String name;

    private Collection currentCollection = new Collection();
    private Collection previousCollection = new Collection();
    private long timeOfPreviousObservation = System.currentTimeMillis();
    private long timeOfCurrentObservation = System.currentTimeMillis() + 1;

    /// Creates a GC model for the supplied collector MBean and registers it for
    /// cached JMX refresh notifications.
    public GarbageCollectionModel(final ObjectName mbeanName, final JmxModel model, final MBeanServerConnection mbeanServerConnection) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException {
        this.mbeanName = mbeanName;
        this.mbeanServerConnection = mbeanServerConnection;
        this.name = attributeText("Name", fallbackName(mbeanName));
        CachedMBeanServerConnectionFactory.getCachedMBeanServerConnection(model, 2000).addMBeanCacheListener(this);
    }

    /// Refreshes counters from JMX and notifies listeners.
    @Override
    public void flushed() {
        try {
            var collectionTime = attributeAsLong("CollectionTime");
            var collectionCount = attributeAsLong("CollectionCount");
            var lastDuration = extractLastDuration();

            previousCollection = currentCollection;
            timeOfPreviousObservation = timeOfCurrentObservation;

            if (collectionCount != null && collectionTime != null && collectionCount != currentCollection.getCount()) {
                currentCollection = new Collection(collectionCount, collectionTime, lastDuration);
            }
            timeOfCurrentObservation = System.currentTimeMillis();

            beforeListeners();
            tickleListeners();
        } catch (Throwable t) {
            Exceptions.printStackTrace(Exceptions.attachMessage(t, "Exception recovering data from GarbageCollectorMXBean"));
        }
    }

    private long extractLastDuration() throws AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException, IOException {
        try {
            return extractLastDurationFromHotspot();
        } catch (AttributeNotFoundException e) {
            return extractLastDurationFromJ9();
        }
    }

    private long extractLastDurationFromHotspot() throws AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException, IOException {
        if (mbeanServerConnection.getAttribute(mbeanName, "LastGcInfo") instanceof CompositeData lastGcInfo
                && lastGcInfo.get("duration") instanceof Long duration) {
            return duration;
        }
        return 0L;
    }

    private long extractLastDurationFromJ9() throws AttributeNotFoundException, MBeanException, ReflectionException, InstanceNotFoundException, IOException {
        var lastCollectionStartTime = attributeAsLong("LastCollectionStartTime");
        var lastCollectionEndTime = attributeAsLong("LastCollectionEndTime");
        if (lastCollectionStartTime == null || lastCollectionEndTime == null) {
            throw new AttributeNotFoundException("Last collection duration could not be retrieved. " +
                    "lastCollectionStartTime=[" + lastCollectionStartTime +
                    "], lastCollectionEndTime=[" + lastCollectionEndTime +
                    "]");
        } else {
            return lastCollectionEndTime - lastCollectionStartTime;
        }
    }

    private @Nullable Long attributeAsLong(String attributeName) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException {
        return mbeanServerConnection.getAttribute(mbeanName, attributeName) instanceof Long value ? value : null;
    }

    private String attributeText(String attributeName, String fallback) throws MBeanException, AttributeNotFoundException, InstanceNotFoundException, ReflectionException, IOException {
        var value = mbeanServerConnection.getAttribute(mbeanName, attributeName);
        return value == null ? fallback : value.toString();
    }

    private static String fallbackName(ObjectName mbeanName) {
        var keyPropertyName = mbeanName.getKeyProperty("name");
        return keyPropertyName == null ? mbeanName.getCanonicalName() : keyPropertyName;
    }

    private void tickleListeners() {
        for (var listener : listeners) {
            listener.garbageCollectorUpdated(this);
        }
    }

    /// Hook for subclasses that need to update derived state before listeners run.
    protected void beforeListeners() {
    }

    /// Returns the total number of collections observed.
    public long getCount() {
        return currentCollection.getCount();
    }

    /// Returns the cumulative collection duration in milliseconds.
    public long getTotalDuration() {
        return currentCollection.getTotalDuration();
    }

    /// Returns the latest collection duration in milliseconds.
    public long getLastDuration() {
        return currentCollection.getLastDuration();
    }

    /// Returns the observed collections per second since the previous refresh.
    public long getFrequency() {
        var elapsedMillis = timeOfCurrentObservation - timeOfPreviousObservation;
        if (elapsedMillis <= 0) {
            return 0;
        }
        return 1000 * (currentCollection.getCount() - previousCollection.getCount()) / elapsedMillis;
    }

    /// Registers a listener that should receive model refresh events.
    public void registerView(GarbageCollectorModelListener listener) {
        listeners.add(listener);
    }

    /// Returns the display name reported by the target JVM.
    public String getName() {
        return name;
    }
}

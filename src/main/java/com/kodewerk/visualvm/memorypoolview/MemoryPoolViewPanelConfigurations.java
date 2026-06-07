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

import org.graalvm.visualvm.core.ui.components.DataViewComponent;
import org.jspecify.annotations.Nullable;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

/// Placement and analytic-series policy for known JVM memory pools and collectors.
public class MemoryPoolViewPanelConfigurations {

    private static final int[] FALLBACK_CORNERS = {
            DataViewComponent.TOP_LEFT,
            DataViewComponent.TOP_RIGHT,
            DataViewComponent.BOTTOM_LEFT,
            DataViewComponent.BOTTOM_RIGHT
    };

    private final Map<String, Point> memoryPoolPanelPositions = new HashMap<>();
    private final Map<String, Boolean> memoryPoolAnalytics = new HashMap<>();
    private final Map<String, Point> garbageCollectionFrequencyPanelPositions = new HashMap<>();
    private final Map<String, Point> garbageCollectorPanelPositions = new HashMap<>();
    private final Map<String, Boolean> garbageCollectorAnalytics = new HashMap<>();

    private int fallbackCornerIndex;
    private int fallbackOrder = 1;

    /// Builds the default placement table for common HotSpot and IBM JVM names.
    public MemoryPoolViewPanelConfigurations() {
        // Hotspot Memory Pool Names
        // Young generation
        memoryPoolPanelPositions.put("Par Eden Space", new Point(DataViewComponent.TOP_LEFT, 10));
        memoryPoolPanelPositions.put("PS Eden Space", new Point(DataViewComponent.TOP_LEFT, 10));
        memoryPoolPanelPositions.put("Eden Space", new Point(DataViewComponent.TOP_LEFT, 10));
        memoryPoolPanelPositions.put("G1 Eden", new Point(DataViewComponent.TOP_LEFT, 10));
        memoryPoolPanelPositions.put("G1 Eden Space", new Point(DataViewComponent.TOP_LEFT, 10));
        memoryPoolPanelPositions.put("Par Survivor Space", new Point(DataViewComponent.TOP_LEFT, 20));
        memoryPoolPanelPositions.put("PS Survivor Space", new Point(DataViewComponent.TOP_LEFT, 20));
        memoryPoolPanelPositions.put("Survivor Space", new Point(DataViewComponent.TOP_LEFT, 20));
        memoryPoolPanelPositions.put("G1 Survivor", new Point(DataViewComponent.TOP_LEFT, 20));
        memoryPoolPanelPositions.put("G1 Survivor Space", new Point(DataViewComponent.TOP_LEFT, 20));

        // Old generation
        memoryPoolPanelPositions.put("CMS Old Gen", new Point(DataViewComponent.TOP_RIGHT, 10));
        memoryPoolPanelPositions.put("PS Old Gen", new Point(DataViewComponent.TOP_RIGHT, 10));
        memoryPoolPanelPositions.put("Tenured Gen", new Point(DataViewComponent.TOP_RIGHT, 10));
        memoryPoolPanelPositions.put("G1 Old Gen", new Point(DataViewComponent.TOP_RIGHT, 10));

        // Permanent generation
        memoryPoolPanelPositions.put("CMS Perm Gen", new Point(DataViewComponent.BOTTOM_LEFT, 15));
        memoryPoolPanelPositions.put("Perm Gen", new Point(DataViewComponent.BOTTOM_LEFT, 15));
        memoryPoolPanelPositions.put("PS Perm Gen", new Point(DataViewComponent.BOTTOM_LEFT, 15));
        memoryPoolPanelPositions.put("G1 Perm Gen", new Point(DataViewComponent.BOTTOM_LEFT, 15));

        // Code cache
        memoryPoolPanelPositions.put("Code Cache", new Point(DataViewComponent.BOTTOM_RIGHT, 10));

        //IBM Memory Pool Names
        memoryPoolPanelPositions.put("Java heap", new Point(DataViewComponent.TOP_LEFT, 10));

        memoryPoolPanelPositions.put("class storage", new Point(DataViewComponent.TOP_RIGHT, 10));
        memoryPoolPanelPositions.put("miscellaneous non-heap storage", new Point(DataViewComponent.TOP_RIGHT, 15));

        memoryPoolPanelPositions.put("JIT code cache", new Point(DataViewComponent.BOTTOM_LEFT, 10));
        memoryPoolPanelPositions.put("JIT data cache", new Point(DataViewComponent.BOTTOM_LEFT, 20));
        
        // Hotspot Memory Pool Names
        // Do we track with memoryPoolAnalytics
        // Young generation
        memoryPoolAnalytics.put("Par Eden Space", Boolean.FALSE);
        memoryPoolAnalytics.put("PS Eden Space", Boolean.FALSE);
        memoryPoolAnalytics.put("Eden Space", Boolean.FALSE);
        memoryPoolAnalytics.put("G1 Eden", Boolean.FALSE);
        memoryPoolAnalytics.put("G1 Eden Space", Boolean.FALSE);
        memoryPoolAnalytics.put("Par Survivor Space", Boolean.FALSE);
        memoryPoolAnalytics.put("PS Survivor Space", Boolean.FALSE);
        memoryPoolAnalytics.put("Survivor Space", Boolean.FALSE);
        memoryPoolAnalytics.put("G1 Survivor", Boolean.FALSE);
        memoryPoolAnalytics.put("G1 Survivor Space", Boolean.FALSE);

        // Old generation
        memoryPoolAnalytics.put("CMS Old Gen", Boolean.TRUE);
        memoryPoolAnalytics.put("PS Old Gen", Boolean.TRUE);
        memoryPoolAnalytics.put("Tenured Gen", Boolean.TRUE);
        memoryPoolAnalytics.put("G1 Old Gen", Boolean.TRUE);

        // Permanent generation
        memoryPoolAnalytics.put("CMS Perm Gen", Boolean.TRUE);
        memoryPoolAnalytics.put("Perm Gen", Boolean.TRUE);
        memoryPoolAnalytics.put("PS Perm Gen", Boolean.TRUE);
        memoryPoolAnalytics.put("G1 Perm Gen", Boolean.TRUE);

        // Code cache
        memoryPoolAnalytics.put("Code Cache", Boolean.TRUE);

        //IBM Memory Pool Names
        memoryPoolAnalytics.put("Java heap", Boolean.TRUE);

        memoryPoolAnalytics.put("class storage", Boolean.TRUE);
        memoryPoolAnalytics.put("miscellaneous non-heap storage", Boolean.TRUE);

        memoryPoolAnalytics.put("JIT code cache", Boolean.TRUE);
        memoryPoolAnalytics.put("JIT data cache", Boolean.TRUE);
        
        /***********
         Garbage Collector JPanel Configurations
         ***********/
        
        //Position is either top left or top right
        garbageCollectorPanelPositions.put("Copy", new Point(DataViewComponent.TOP_LEFT, 1));
        garbageCollectorPanelPositions.put("PS Scavenge", new Point(DataViewComponent.TOP_LEFT, 1));
        garbageCollectorPanelPositions.put("ParNew", new Point(DataViewComponent.TOP_LEFT, 1));
        garbageCollectorPanelPositions.put("DefNew", new Point(DataViewComponent.TOP_LEFT, 1));
        garbageCollectorPanelPositions.put("G1 Young Generation", new Point(DataViewComponent.TOP_LEFT, 1));

        garbageCollectorPanelPositions.put("MarkSweepCompact", new Point(DataViewComponent.TOP_RIGHT, 1));
        garbageCollectorPanelPositions.put("PS MarkSweep", new Point(DataViewComponent.TOP_RIGHT, 1));
        garbageCollectorPanelPositions.put("ConcurrentMarkSweep", new Point(DataViewComponent.TOP_RIGHT, 1));
        garbageCollectorPanelPositions.put("G1 Old Generation", new Point(DataViewComponent.TOP_RIGHT, 1));

        garbageCollectionFrequencyPanelPositions.put("Copy", new Point(DataViewComponent.BOTTOM_LEFT, 1));
        garbageCollectionFrequencyPanelPositions.put("PS Scavenge", new Point(DataViewComponent.BOTTOM_LEFT, 1));
        garbageCollectionFrequencyPanelPositions.put("ParNew", new Point(DataViewComponent.BOTTOM_LEFT, 1));
        garbageCollectionFrequencyPanelPositions.put("DefNew", new Point(DataViewComponent.BOTTOM_LEFT, 1));
        garbageCollectionFrequencyPanelPositions.put("G1 Young Generation", new Point(DataViewComponent.BOTTOM_LEFT, 1));

        garbageCollectionFrequencyPanelPositions.put("MarkSweepCompact", new Point(DataViewComponent.BOTTOM_RIGHT, 1));
        garbageCollectionFrequencyPanelPositions.put("PS MarkSweep", new Point(DataViewComponent.BOTTOM_RIGHT, 1));
        garbageCollectionFrequencyPanelPositions.put("ConcurrentMarkSweep", new Point(DataViewComponent.BOTTOM_RIGHT, 1));
        garbageCollectionFrequencyPanelPositions.put("G1 Old Generation", new Point(DataViewComponent.BOTTOM_RIGHT, 1));

        //Do we track analytics for this view?
        garbageCollectorAnalytics.put("Copy", false);
        garbageCollectorAnalytics.put("PS Scavenge", false);
        garbageCollectorAnalytics.put("ParNew", false);
        garbageCollectorAnalytics.put("DefNew", false);
        garbageCollectorAnalytics.put("G1 Young Generation", false);

        garbageCollectorAnalytics.put("MarkSweepCompact", true);
        garbageCollectorAnalytics.put("PS MarkSweep", true);
        garbageCollectorAnalytics.put("ConcurrentMarkSweep", true);
        garbageCollectorAnalytics.put("G1 Old Generation", true);

    }

    /**
     * For the current Oracle JVM 5 charts are displayed. Names seem to be
     * fixed for the moment but can change. They are also different for other
     * JVM OEMs. So if a name cannot be found in memoryPoolPanelPositions the solution is to stack
     * the unknown charts  in the top left. This is under the assumption that
     * if one is unknown, the lot is likely to be unknown and any attempt
     * to come up with a semantically reasonable ordering will most likely
     * fail.
     *
     * @param name the name of the GC
     * @return the associated position
     */

    private Point panelPosition(Map<String, Point> panelPositions, @Nullable String name) {
        var point = panelPositions.get(name);
        if (point == null) {
            point = new Point(FALLBACK_CORNERS[fallbackCornerIndex], fallbackOrder++);
            fallbackCornerIndex = (fallbackCornerIndex + 1) % FALLBACK_CORNERS.length;
        }
        return new Point(point);
    }

    private boolean analytic(Map<String, Boolean> analytics, @Nullable String name) {
        return Boolean.TRUE.equals(analytics.get(name));
    }

    /// Returns the preferred details-area position for a memory-pool chart.
    public Point memoryPoolPanelPosition(@Nullable String name) {
        return panelPosition(memoryPoolPanelPositions, name);
    }

    /// Returns whether the named memory pool should display an analytic indicator.
    public boolean memoryPoolAnalytic(@Nullable String name) {
        return analytic(memoryPoolAnalytics, name);
    }

    /// Returns the preferred details-area position for a GC duration chart.
    public Point garbageCollectorPanelPosition(@Nullable String name) {
        return panelPosition(garbageCollectorPanelPositions, name);
    }

    /// Returns whether the named collector should display an analytic indicator.
    public boolean garbageCollectorAnalytic(@Nullable String name) {
        return analytic(garbageCollectorAnalytics, name);
    }

    /// Returns the preferred details-area position for a GC frequency chart.
    public Point garbageCollectionFrequencyPanelPosition(@Nullable String name) {
        return panelPosition(garbageCollectionFrequencyPanelPositions, name);
    }

    /// Returns whether the named collector's frequency chart should display an analytic indicator.
    public boolean garbageCollectionFrequencyAnalytic(@Nullable String name) {
        return false;
    }
}

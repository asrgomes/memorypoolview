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

import org.graalvm.visualvm.application.Application;
import org.graalvm.visualvm.core.ui.DataSourceView;
import org.graalvm.visualvm.core.ui.components.DataViewComponent;
import org.graalvm.visualvm.tools.jmx.JmxModel;
import org.graalvm.visualvm.tools.jmx.JmxModelFactory;
import org.jspecify.annotations.Nullable;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.io.IOException;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/// Displays live charts for every memory pool exposed by the selected JVM.
class MemoryPoolView extends DataSourceView {

    private static final ObjectName MEMORY_POOL_WILDCARD_PATTERN = createMemoryPoolWildcardPattern();
    private static final String IMAGE_PATH = "com/kodewerk/visualvm/memorypoolview/memory.png";
    private static final int GRID_COLUMNS = 3;

    private static final Comparator<MemoryPoolDescriptor> BY_MAX_SIZE_DESCENDING =
            Comparator.comparingLong(MemoryPoolDescriptor::maxSize)
                    .reversed()
                    .thenComparing(MemoryPoolDescriptor::displayName);

    private final List<MemoryPoolModel> memoryPoolModels = new ArrayList<>();

    static @Nullable MBeanServerConnection getMBeanServerConnection(Application application) {
        var jmx = JmxModelFactory.getJmxModelFor(application);
        return jmx == null ? null : jmx.getMBeanServerConnection();
    }

    private static ObjectName createMemoryPoolWildcardPattern() {
        try {
            return new ObjectName("java.lang:type=MemoryPool,name=*");
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static Image loadViewImage() {
        var image = ImageUtilities.loadImage(IMAGE_PATH, true);
        if (image == null) {
            throw new IllegalStateException("Missing image resource: " + IMAGE_PATH);
        }
        return image;
    }

    MemoryPoolView(Application application) {
        super(application, "Memory Pools", loadViewImage(), 60, false);
    }

    @Override
    protected DataViewComponent createComponent() {
        var memoryPoolGrid = createMemoryPoolGrid();

        var masterPanel = new JPanel();
        masterPanel.setOpaque(false);
        var masterView = new DataViewComponent.MasterView("Memory Pools", "View of Memory Pools", masterPanel);

        var masterConfiguration = new DataViewComponent.MasterViewConfiguration(false);

        var dvc = new DataViewComponent(masterView, masterConfiguration);
        dvc.configureDetailsArea(
                new DataViewComponent.DetailsAreaConfiguration("Memory Pools", false),
                DataViewComponent.TOP_LEFT);
        dvc.addDetailsView(new DataViewComponent.DetailsView(
                "All Pools", "memory pool metrics", 1, memoryPoolGrid, null),
                DataViewComponent.TOP_LEFT);
        dvc.configureDetailsView(new DataViewComponent.DetailsViewConfiguration(
                1.0d, 1.0d, -1.0d, -1.0d, 1.0d, 1.0d));

        findMemoryPoolsAndCreatePanels(memoryPoolGrid);

        return dvc;
    }

    private JPanel createMemoryPoolGrid() {
        var grid = new JPanel();
        grid.setLayout(new GridLayout(0, GRID_COLUMNS, 8, 8));
        grid.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        grid.setOpaque(false);
        grid.setMinimumSize(new Dimension(0, 0));
        return grid;
    }

    private void findMemoryPoolsAndCreatePanels(JPanel memoryPoolGrid) {
        try {
            var application = (Application) super.getDataSource();
            var jmxModel = JmxModelFactory.getJmxModelFor(application);
            if (jmxModel == null) {
                return;
            }

            var conn = jmxModel.getMBeanServerConnection();
            if (conn == null) {
                return;
            }

            conn.queryNames(MEMORY_POOL_WILDCARD_PATTERN, null).stream()
                    .map(name -> new MemoryPoolDescriptor(name, displayName(name), normalizedMax(conn, name)))
                    .sorted(BY_MAX_SIZE_DESCENDING)
                    .forEach(pool -> createPanelFor(memoryPoolGrid, pool, jmxModel, conn));
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    private long normalizedMax(MBeanServerConnection conn, ObjectName name) {
        try {
            if (conn.getAttribute(name, "Usage") instanceof CompositeData usageData) {
                var max = MemoryUsage.from(usageData).getMax();
                return max >= 0 ? max : Long.MIN_VALUE;
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(Exceptions.attachMessage(e, "Exception reading memory pool maximum size"));
        }
        return Long.MIN_VALUE;
    }

    private String displayName(ObjectName name) {
        var keyPropertyName = name.getKeyProperty("name");
        return keyPropertyName == null ? name.getCanonicalName() : keyPropertyName;
    }

    private void createPanelFor(JPanel memoryPoolGrid, MemoryPoolDescriptor descriptor, JmxModel jmxModel, MBeanServerConnection conn) {
        var model = initializeMemoryPoolModel(descriptor.objectName(), jmxModel, conn);
        if (model != null) {
            configureMemoryPoolPanelFor(memoryPoolGrid, new MemoryPoolPanel(), model);
        }
    }

    private void configureMemoryPoolPanelFor(JPanel memoryPoolGrid, MemoryPoolPanel panel, MemoryPoolModel model) {
        model.registerView(panel);
        memoryPoolGrid.add(createMemoryPoolGraph(model.getName(), panel));
    }

    private JComponent createMemoryPoolGraph(String name, MemoryPoolPanel panel) {
        var graph = new JPanel(new BorderLayout());
        graph.setOpaque(false);
        graph.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(name),
                BorderFactory.createEmptyBorder(2, 4, 4, 4)));
        graph.setMinimumSize(new Dimension(0, 0));
        panel.setMinimumSize(new Dimension(0, 0));
        graph.add(panel, BorderLayout.CENTER);
        return graph;
    }

    private @Nullable MemoryPoolModel initializeMemoryPoolModel(ObjectName mbeanName, JmxModel jmxModel, MBeanServerConnection conn) {
        MemoryPoolModel model = null;
        try {
            model = new MemoryPoolModel(mbeanName, jmxModel, conn);
            memoryPoolModels.add(model);
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return model;
    }

    private record MemoryPoolDescriptor(ObjectName objectName, String displayName, long maxSize) {
    }
}

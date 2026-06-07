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

import com.kodewerk.visualvm.memorypoolview.MemoryPoolViewPanelConfigurations;
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
import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import java.awt.Image;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/// Displays live charts for every garbage collector exposed by the selected JVM.
public class GCView extends DataSourceView {

    private static final ObjectName GARBAGE_COLLECTOR_WILDCARD_PATTERN = createGarbageCollectorWildcardPattern();
    private static final String IMAGE_PATH = "com/kodewerk/visualvm/memorypoolview/bin.png";

    private final Set<GarbageCollectionModel> garbageCollectorModels = new HashSet<>();

    static @Nullable MBeanServerConnection getMBeanServerConnection(Application application) {
        var jmx = JmxModelFactory.getJmxModelFor(application);
        return jmx == null ? null : jmx.getMBeanServerConnection();
    }

    private static ObjectName createGarbageCollectorWildcardPattern() {
        try {
            return new ObjectName("java.lang:type=GarbageCollector,name=*");
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

    GCView(Application application) {
        super(application, "Garbage Collector Activity", loadViewImage(), 60, false);
    }

    @Override
    protected DataViewComponent createComponent() {
        var generalDataArea = new JEditorPane();
        generalDataArea.setEditable(false);
        generalDataArea.setBorder(BorderFactory.createEmptyBorder(7, 8, 7, 8));

        var masterView = new DataViewComponent.MasterView("Garbage Collector statistics", "View of statistics", generalDataArea);

        var masterConfiguration = new DataViewComponent.MasterViewConfiguration(false);

        var dvc = new DataViewComponent(masterView, masterConfiguration);

        findGarbageCollectorsAndCreatePanels(dvc);

        return dvc;
    }

    private void findGarbageCollectorsAndCreatePanels(DataViewComponent dvc) {
        var configuration = new MemoryPoolViewPanelConfigurations();
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

            for (var name : conn.queryNames(GARBAGE_COLLECTOR_WILDCARD_PATTERN, null)) {
                var collectorName = name.getKeyProperty("name");
                GarbageCollectionModel model;
                GarbageCollectorDurationPanel durationPanel;
                if (configuration.garbageCollectorAnalytic(collectorName)) {
                    model = initializeGarbageCollectorAnalyticModel(name, jmxModel, conn);
                    durationPanel = new GarbageCollectorAnalyticPanel();
                } else {
                    model = initializeGarbageCollectorModel(name, jmxModel, conn);
                    durationPanel = new GarbageCollectorDurationPanel();
                }

                if (model != null) {
                    configureGarbageCollectorDurationPanelFor(dvc, durationPanel, model, configuration);
                    configureGarbageCollectionFrequencyPanelFor(dvc, new GarbageCollectionFrequencyPanel(), model, configuration);
                }
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    private @Nullable GarbageCollectorAnalyticModel initializeGarbageCollectorAnalyticModel(ObjectName mbeanName, JmxModel jmxModel, MBeanServerConnection conn) {
        GarbageCollectorAnalyticModel model = null;
        try {
            model = new GarbageCollectorAnalyticModel(mbeanName, jmxModel, conn);
            garbageCollectorModels.add(model);
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return model;
    }

    private @Nullable GarbageCollectionModel initializeGarbageCollectorModel(ObjectName mbeanName, JmxModel jmxModel, MBeanServerConnection conn) {
        GarbageCollectionModel model = null;
        try {
            model = new GarbageCollectionModel(mbeanName, jmxModel, conn);
            garbageCollectorModels.add(model);
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
        return model;
    }

    private void configureGarbageCollectorDurationPanelFor(DataViewComponent dvc, GarbageCollectorDurationPanel panel, GarbageCollectionModel model, MemoryPoolViewPanelConfigurations configuration) {
        model.registerView(panel);
        var position = configuration.garbageCollectorPanelPosition(model.getName());
        var detailsView = new DataViewComponent.DetailsView(
                model.getName(), "garbage collector metrics", position.y, panel, null);
        dvc.addDetailsView(detailsView, position.x);
    }

    private void configureGarbageCollectionFrequencyPanelFor(DataViewComponent dvc, GarbageCollectionFrequencyPanel panel, GarbageCollectionModel model, MemoryPoolViewPanelConfigurations configuration) {
        model.registerView(panel);
        var position = configuration.garbageCollectionFrequencyPanelPosition(model.getName());
        var detailsView = new DataViewComponent.DetailsView(
                model.getName(), "GC Frequency", position.y, panel, null);
        dvc.addDetailsView(detailsView, position.x);
    }
}

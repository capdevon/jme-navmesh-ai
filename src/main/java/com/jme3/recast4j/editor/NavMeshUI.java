package com.jme3.recast4j.editor;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.recast4j.builder.NavMeshBuildSettings;
import com.jme3.scene.Node;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.RollupPanel;
import com.simsilica.lemur.props.PropertyPanel;
import com.simsilica.lemur.style.BaseStyles;

/**
 * 
 * @author capdevon
 */
public class NavMeshUI extends BaseAppState {

    private Container container;
    private NavMeshGeneratorState navMeshState;

    @Override
    protected void initialize(Application app) {
        this.navMeshState = getState(NavMeshGeneratorState.class, true);

        // initialize lemur
        GuiGlobals.initialize(app);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        initComponents();
    }
    
    private Container initComponents() {

        NavMeshBuildSettings settings = new NavMeshBuildSettings();

        container = new Container();
        //container.addChild(new Label("Properties", new ElementId("title")));
        container.setLocalTranslation(10, getApplication().getCamera().getHeight() - 10, 1);

        //PropertyPanel propertyPanel = container.addChild(new PropertyPanel("glass"));
        PropertyPanel propertyPanel = new PropertyPanel("glass");

        // Rasterization
        propertyPanel.addFloatField("Cell Size", settings, "cellSize", 0.1f, 1, 0.1f);
        propertyPanel.addFloatField("Cell Height", settings, "cellHeight", 0.1f, 1, 0.1f);

        // Agent
        propertyPanel.addFloatField("Agent Height", settings, "agentHeight", 0.1f, 5f, 0.1f);
        propertyPanel.addFloatField("Agent Radius", settings, "agentRadius", 0, 5, 0.1f);
        propertyPanel.addFloatField("Agent Max Climb", settings, "agentMaxClimb", 0.1f, 5f, 0.1f);
        propertyPanel.addFloatField("Agent Max Slope", settings, "agentMaxSlope", 0, 90, 0.1f);

        // Region
        propertyPanel.addIntField("Min Region Size", settings, "regionMinSize", 0, 150, 1);
        propertyPanel.addIntField("Merge Region Size", settings, "regionMergeSize", 0, 150, 1);

        // Partitioning
        propertyPanel.addEnumField("Partitionig", settings, "partitionType");

        // Filtering
        propertyPanel.addBooleanField("Low Hanging Obstacles", settings, "filterLowHangingObstacles");
        propertyPanel.addBooleanField("Ledge Spans", settings, "filterLedgeSpans");
        propertyPanel.addBooleanField("Walkable Low Height Spans", settings, "filterWalkableLowHeightSpans");

        // Polygonization
        propertyPanel.addFloatField("Max Edge Length", settings, "edgeMaxLen", 0.0f, 50f, 0.1f);
        propertyPanel.addFloatField("Max Edge Error", settings, "edgeMaxError", 0.1f, 3, 0.1f);
        propertyPanel.addIntField("Vert Per Poly", settings, "vertsPerPoly", 3, 12, 1);

        // Detail Mesh
        propertyPanel.addFloatField("Sample Distance", settings, "detailSampleDist", 0.0f, 16f, 0.1f);
        propertyPanel.addFloatField("Max Sample Error", settings, "detailSampleMaxError", 0.0f, 16f, 0.1f);

        propertyPanel.addBooleanField("Tiling", settings, "tiled");
        propertyPanel.addIntField("Tile Size", settings, "tileSize", 16, 1024, 16);


        RollupPanel rollup = new RollupPanel("NavMesh Settings", propertyPanel, "glass");
        rollup.setAlpha(0, false);
        //		rollup.setOpen(false);
        container.addChild(rollup);

        Button refreshButton = container.addChild(new Button("Generate NavMesh"));
        refreshButton.addClickCommands(source -> {
            navMeshState.generateNavMesh(settings);
        });

        return container;
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
        getGuiNode().attachChild(container);
    }

    @Override
    protected void onDisable() {
        getGuiNode().detachChild(container);
    }

    private Node getGuiNode() {
        return ((SimpleApplication) getApplication()).getGuiNode();
    }

}

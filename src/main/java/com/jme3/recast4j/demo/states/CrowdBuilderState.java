/*
 * The MIT License
 *
 * Copyright 2019 .
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * MODELS/DUNE.J3O:
 * Converted from http://quadropolis.us/node/2584 [Public Domain according to the Tags of this Map]
 */

package com.jme3.recast4j.demo.states;

import static com.jme3.recast4j.demo.SimpleAreaMod.POLYFLAGS_ALL;
import static com.jme3.recast4j.demo.SimpleAreaMod.POLYFLAGS_DISABLED;
import static com.jme3.recast4j.demo.SimpleAreaMod.POLYFLAGS_DOOR;
import static com.jme3.recast4j.demo.SimpleAreaMod.POLYFLAGS_JUMP;
import static com.jme3.recast4j.demo.SimpleAreaMod.POLYFLAGS_SWIM;
import static com.jme3.recast4j.demo.SimpleAreaMod.POLYFLAGS_WALK;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;

import org.recast4j.detour.DefaultQueryFilter;
import org.recast4j.detour.NavMesh;
import org.recast4j.detour.NavMeshQuery;
import org.recast4j.detour.QueryFilter;
import org.recast4j.detour.crowd.CrowdAgent;
import org.recast4j.detour.crowd.ObstacleAvoidanceQuery.ObstacleAvoidanceParams;
import org.recast4j.detour.io.MeshSetReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.event.MouseButtonEvent;
import com.jme3.math.Vector3f;
import com.jme3.recast4j.Detour.BetterDefaultQueryFilter;
import com.jme3.recast4j.Detour.Crowd.CircleFormationHandler;
import com.jme3.recast4j.Detour.Crowd.Crowd;
import com.jme3.recast4j.Detour.Crowd.CrowdManager;
import com.jme3.recast4j.Detour.Crowd.MovementApplicationType;
import com.jme3.recast4j.Detour.Crowd.Impl.CrowdManagerAppState;
import com.jme3.recast4j.demo.controls.CrowdChangeControl;
import com.jme3.recast4j.demo.controls.CrowdDebugControl;
import com.jme3.recast4j.demo.controls.PhysicsAgentControl;
import com.jme3.recast4j.demo.layout.MigLayout;
import com.jme3.recast4j.demo.states.AgentGridState.Grid;
import com.jme3.recast4j.demo.states.AgentGridState.GridAgent;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.ActionButton;
import com.simsilica.lemur.CallMethodAction;
import com.simsilica.lemur.Checkbox;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.RollupPanel;
import com.simsilica.lemur.TabbedPanel;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.event.CursorEventControl;
import com.simsilica.lemur.event.DefaultMouseListener;
import com.simsilica.lemur.event.DragHandler;
import com.simsilica.lemur.event.MouseEventControl;
import com.simsilica.lemur.event.PopupState;
import com.simsilica.lemur.list.DefaultCellRenderer;
import com.simsilica.lemur.style.ElementId;
import com.simsilica.lemur.text.DocumentModelFilter;
import com.simsilica.lemur.text.TextFilters;

/**
 * Gui builder for crowd data. 
 * 
 * @author Robert
 */
public class CrowdBuilderState extends BaseAppState {

    private static final Logger LOG = LoggerFactory.getLogger(CrowdBuilderState.class.getName());

    private Container contTabs;
    private DocumentModelFilter doc;
    private TextField fieldCostGround;
    private TextField fieldCostRoad;
    private TextField fieldCostGrass;
    private TextField fieldCostDoor;
    private TextField fieldCostJump;
    private TextField fieldCostWater;
    private TextField fieldNavMesh;
    private TextField fieldMaxAgents;
    private TextField fieldMaxAgentRadius;
    private TextField fieldVelocityBias;
    private TextField fieldWeightDesVel;
    private TextField fieldWeightCurVel;
    private TextField fieldWeightSide;
    private TextField fieldWeightToi;
    private TextField fieldHorizTime;
    private TextField fieldGridSize;
    private TextField fieldAdaptiveRings;
    private TextField fieldAdaptiveDivs;
    private TextField fieldAdaptiveDepth;    
    private ListBox<ObstacleAvoidanceParams> listBoxAvoidance;  
    private ListBox<String> listBoxMoveType;
    private ListBox<Crowd> listBoxActiveCrowds;
    private ListBox<String> listBoxActiveGrids;
    private ListBox<DefaultQueryFilter> listBoxFilters;
    // Keep track selection changes
    //Crowd changes.
    private VersionedReference<Set<Integer>> crowdSelectRef; 
    private VersionedReference<List<Crowd>> crowdModelRef;
    //Filter changes
    private VersionedReference<Boolean> editActiveSelRef;
    private VersionedReference<Set<Integer>> filtersSelectRef; 
    private VersionedReference<List<DefaultQueryFilter>> filtersModelRef;
    private String defaultActiveGridText;
    private Checkbox checkIncludeNone;
    private Checkbox checkIncludeDisabled;
    private Checkbox checkIncludeAll;
    private Checkbox checkIncludeWalk;
    private Checkbox checkIncludeSwim;
    private Checkbox checkIncludeDoor;
    private Checkbox checkIncludeJump;
    private Checkbox checkExcludeAll;
    private Checkbox checkExcludeNone;
    private Checkbox checkExcludeDisabled;
    private Checkbox checkExcludeWalk;
    private Checkbox checkExcludeSwim;
    private Checkbox checkExcludeDoor;
    private Checkbox checkExcludeJump;
    private Checkbox checkEditActive;
    private HashMap<Crowd, NavMeshQuery> mapCrowds;
    public static final int DT_CROWD_MAX_OBSTAVOIDANCE_PARAMS = 8;
    public static final int DT_CROWD_MAX_QUERY_FILTER_TYPE = 16;
    //Currently, only six area modifications in SampleAreaModifications.
    private static final int MAX_AREAMOD = 6;
    @Override
    protected void initialize(Application app) {
        
        //Holds a paired Crowd and NavMeshQuery object with Crowd key and Query
        //value.
        mapCrowds = new HashMap<>();
        
        //Displays when crowdSelectRef and crowdModelRef has changed and the model or 
        //selection is empty. Used as default for startup of listBoxActiveCrowds.  
        defaultActiveGridText = "No Active Grids";
    }

    @Override
    protected void cleanup(Application app) {
        //Removing will also cleanup the AgentGridState and AgentParamState
        //lemur objects.
        ((SimpleApplication) getApplication()).getGuiNode().detachChild(contTabs);

        Iterator<Crowd> iterator = mapCrowds.keySet().iterator();
        while (iterator.hasNext()) {
            Crowd crowd = iterator.next();
            getState(CrowdManagerAppState.class).getCrowdManager().removeCrowd(crowd);
            iterator.remove();
        }
    }

    /**
     * Called by AgentParamState(onEnable). CrowdBuilderState needs 
     * AgentGridState and AgentParamState to build its gui. This is the end of 
     * the attachment chain. 
     * AgentGridState(onEnable)=>AgentParamState(onEnable)=>CrowdBuilderState(onEnable)
     */
    @Override
    protected void onEnable() {
        
        //The top container for the crowd builder panel.
        Container contCrowd = new Container(new MigLayout("align center"));
        contCrowd.setName("CrowdBuilderState contCrowd");
        contCrowd.setAlpha(0, false);        
        
        
        
        Container contOAP = new Container(new MigLayout("wrap", "[grow]"));
        contOAP.setName("CrowdBuilderState contOAP");
        contOAP.setAlpha(1, false);
        contOAP.addChild(new Label("Obstacle Avoidance Parameters")); 
        contCrowd.addChild(contOAP, "growx, growy");
                
        //Velocity Bias.
        contOAP.addChild(new Label("velBias"), "split 2, growx"); 
        fieldVelocityBias = contOAP.addChild(new TextField(""));
        fieldVelocityBias.setSingleLine(true);
        fieldVelocityBias.setPreferredWidth(50);
        
        //Weight desired velocity.
        contOAP.addChild(new Label("weightDesVel"), "split 2, growx"); 
        fieldWeightDesVel = contOAP.addChild(new TextField(""));
        fieldWeightDesVel.setSingleLine(true);
        fieldWeightDesVel.setPreferredWidth(50);
        
        //Weight current velocity.
        contOAP.addChild(new Label("weightCurVel"), "split 2, growx"); 
        fieldWeightCurVel = contOAP.addChild(new TextField(""));
        fieldWeightCurVel.setSingleLine(true);
        fieldWeightCurVel.setPreferredWidth(50);
        
        //Weight side.
        contOAP.addChild(new Label("weightSide"), "split 2, growx"); 
        fieldWeightSide = contOAP.addChild(new TextField(""));
        fieldWeightSide.setSingleLine(true);
        fieldWeightSide.setPreferredWidth(50);        
        
        //Weight to impact.
        contOAP.addChild(new Label("weightToi"), "split 2, growx"); 
        fieldWeightToi = contOAP.addChild(new TextField(""));
        fieldWeightToi.setSingleLine(true);
        fieldWeightToi.setPreferredWidth(50);  
        
        //Horrizon time.
        contOAP.addChild(new Label("horizTime"), "split 2, growx"); 
        fieldHorizTime = contOAP.addChild(new TextField(""));
        fieldHorizTime.setSingleLine(true);
        fieldHorizTime.setPreferredWidth(50);                
                
        //Grid Size.
        contOAP.addChild(new Label("gridSize"), "split 2, growx"); 
        doc = new DocumentModelFilter();
        doc.setInputTransform(TextFilters.numeric());
        doc.setText("");
        fieldGridSize = contOAP.addChild(new TextField(doc));
        fieldGridSize.setSingleLine(true);
        fieldGridSize.setPreferredWidth(50);
        
        //Adaptive Divisions.
        contOAP.addChild(new Label("adaptiveDivs"), "split 2, growx"); 
        doc = new DocumentModelFilter();
        doc.setInputTransform(TextFilters.numeric());
        doc.setText("");
        fieldAdaptiveDivs = contOAP.addChild(new TextField(doc));
        fieldAdaptiveDivs.setSingleLine(true);
        fieldAdaptiveDivs.setPreferredWidth(50);
        
        //Adaptive Rings.
        contOAP.addChild(new Label("adaptiveRings"), "split 2, growx"); 
        doc = new DocumentModelFilter();
        doc.setInputTransform(TextFilters.numeric());
        doc.setText("");
        fieldAdaptiveRings = contOAP.addChild(new TextField(doc));
        fieldAdaptiveRings.setSingleLine(true);
        fieldAdaptiveRings.setPreferredWidth(50);
        
        //Adaptive Depth.
        contOAP.addChild(new Label("adaptiveDepth"), "split 2, growx"); 
        doc = new DocumentModelFilter();
        doc.setInputTransform(TextFilters.numeric());
        doc.setText("");
        fieldAdaptiveDepth = contOAP.addChild(new TextField(doc));
        fieldAdaptiveDepth.setSingleLine(true);
        fieldAdaptiveDepth.setPreferredWidth(50);        
        
        
        
        //Parameters list.
        listBoxAvoidance = contOAP.addChild(new ListBox<>(), "growx");
        listBoxAvoidance.setName("listBoxAvoidance");
        listBoxAvoidance.setVisibleItems(1);
        listBoxAvoidance.setCellRenderer(new DefaultCellRenderer<ObstacleAvoidanceParams>(new ElementId("list.item"), null) {
            @Override
            protected String valueToString(ObstacleAvoidanceParams param ) {
                return formatOAP(param);
            }
        });

        //Update a parameter button.
        contOAP.addChild(new ActionButton(new CallMethodAction("Update Parameter", this, "updateOAP")));   
        
        
        
        //Filters.
        Container contFilters = new Container(new MigLayout("wrap 2"));
        contFilters.setName("CrowdBuilderState contAbilityFlags");
        contFilters.setAlpha(1, false);
        contFilters.addChild(new Label("Query Filters"), "span 2"); 
        contFilters.addChild(new Label("Ability Flags"), "span 2");
        contFilters.addChild(new Label("Include"));
        contFilters.addChild(new Label("Exclude"));
        contCrowd.addChild(contFilters, "growx, growy,top");
        
        //Include-exclude
        /**
        * The purpose of adding MouseEventControl here is to only update the the 
        * toggled Checkbox view, not the filter, in cases when they are just 
        * toggling all or none for filters. 
        * 
        * We don't use a reference listener because there are other Versioned 
        * References that set Checkboxes based on updating filters. These would 
        * fire off again every time those references updated.
        * 
        * Preferred use would be to addClickCommands, but unfortunately the lemur 
        * library is still outdated and uses type arguments that cause 
        * "unchecked generic array creation for varargs" warnings. 
        */        
        checkIncludeAll = contFilters.addChild(new Checkbox("ALL")); 
        MouseEventControl.addListenersToSpatial(checkIncludeAll, new CheckboxListen());
        checkExcludeAll = contFilters.addChild(new Checkbox("ALL"));
        MouseEventControl.addListenersToSpatial(checkExcludeAll, new CheckboxListen());
        checkIncludeNone = contFilters.addChild(new Checkbox("NONE"));
        MouseEventControl.addListenersToSpatial(checkIncludeNone, new CheckboxListen());
        checkExcludeNone = contFilters.addChild(new Checkbox("NONE"));
        MouseEventControl.addListenersToSpatial(checkExcludeNone, new CheckboxListen());
        
        checkIncludeDisabled = contFilters.addChild(new Checkbox("DISABLED"));
        checkExcludeDisabled = contFilters.addChild(new Checkbox("DISABLED"));
        checkIncludeWalk = contFilters.addChild(new Checkbox("WALK"));
        checkExcludeWalk = contFilters.addChild(new Checkbox("WALK"));
        checkIncludeSwim = contFilters.addChild(new Checkbox("SWIM"));
        checkExcludeSwim = contFilters.addChild(new Checkbox("SWIM"));
        checkIncludeDoor = contFilters.addChild(new Checkbox("DOOR"));
        checkExcludeDoor = contFilters.addChild(new Checkbox("DOOR"));
        checkIncludeJump = contFilters.addChild(new Checkbox("JUMP"));
        checkExcludeJump = contFilters.addChild(new Checkbox("JUMP"));
        
        //Area type.
        contFilters.addChild(new Label("Area Type"));
        contFilters.addChild(new Label("Filter")); 
        contFilters.addChild(new Label("Type"), "split 2, growx");
        contFilters.addChild(new Label("Cost"));
        checkEditActive = contFilters.addChild(new Checkbox("Edit Active"), "wrap");
        editActiveSelRef = checkEditActive.getModel().createReference();
        
        //Ground cost.
        contFilters.addChild(new Label("Ground"), "split 2, growx"); 
        fieldCostGround = contFilters.addChild(new TextField(""));
        fieldCostGround.setSingleLine(true);
        fieldCostGround.setPreferredWidth(50);

        
        
        //Filters listBox.
        listBoxFilters = contFilters.addChild(new ListBox<>(), "center, span 1 4");
        listBoxFilters.setName("listBoxActiveGrid");
        listBoxFilters.setCellRenderer(new DefaultCellRenderer<DefaultQueryFilter>(new ElementId("list.item"), null) {
            @Override
            protected String valueToString(DefaultQueryFilter param ) {
                return ((Integer) listBoxFilters.getModel().indexOf(param)).toString();
            }
        });
        
        for (int i = 0; i < DT_CROWD_MAX_QUERY_FILTER_TYPE; i++) {
            listBoxFilters.getModel().add(new BetterDefaultQueryFilter());
        }
        
        filtersModelRef = listBoxFilters.getModel().createReference();
        filtersSelectRef = listBoxFilters.getSelectionModel().createReference();
        listBoxFilters.getSelectionModel().setSelection(0);
        
        //Water cost.
        contFilters.addChild(new Label("Water"), "split 2, growx"); 
        fieldCostWater = contFilters.addChild(new TextField(""));
        fieldCostWater.setSingleLine(true);
        fieldCostWater.setPreferredWidth(50);
        
        //Road cost.
        contFilters.addChild(new Label("Road"), "split 2, growx"); 
        fieldCostRoad = contFilters.addChild(new TextField(""));
        fieldCostRoad.setSingleLine(true);
        fieldCostRoad.setPreferredWidth(50);
        
        //Grass cost.
        contFilters.addChild(new Label("Grass"), "split 2, growx"); 
        fieldCostGrass = contFilters.addChild(new TextField(""));
        fieldCostGrass.setSingleLine(true);
        fieldCostGrass.setPreferredWidth(50);
        
        //Door cost.
        contFilters.addChild(new Label("Door"), "split 2, growx"); 
        fieldCostDoor = contFilters.addChild(new TextField(""));
        fieldCostDoor.setSingleLine(true);
        fieldCostDoor.setPreferredWidth(50);
        
        
        //Update filter button.
        contFilters.addChild(new ActionButton(new CallMethodAction("Update Filter", this, "updateFilter")), ", center, span 1 2");
        
        //Jump cost.
        contFilters.addChild(new Label("Jump"), "split 2, growx"); 
        fieldCostJump = contFilters.addChild(new TextField(""));
        fieldCostJump.setSingleLine(true);
        fieldCostJump.setPreferredWidth(50);
         
        

        //Container that holds the obstacle avoidance parameters for CrowdAgents.
        Container contCrowdParams = new Container(new MigLayout("wrap 2", "[grow]"));
        contCrowdParams.setName("CrowdBuilderState contCrowdParams");
        contCrowdParams.setAlpha(1, false);
        contCrowd.addChild(contCrowdParams, "growx, growy");
        contCrowdParams.addChild(new Label("Crowd Parameters"));
        contCrowdParams.addChild(new Label("Movement Type"));    
        contCrowdParams.addChild(new Label("NavMesh"), "split 2"); 
        
        
        //NavMesh field.
        fieldNavMesh = contCrowdParams.addChild(new TextField("test.nm"), "growx");
        fieldNavMesh.setSingleLine(true);
        
        
        
        //Movement types for the crowd.
        listBoxMoveType = contCrowdParams.addChild(new ListBox<>(), "growx, growy, span 1 3");
        listBoxMoveType.setName("listBoxMoveType");
        listBoxMoveType.getSelectionModel().setSelection(0);
        listBoxMoveType.getModel().add("BETTER_CHARACTER_CONTROL");
        listBoxMoveType.getModel().add("DIRECT");
        listBoxMoveType.getModel().add("CUSTOM");
        listBoxMoveType.getModel().add("NONE");
        
        
        
        //Max CrowdAgents for the crowd.
        contCrowdParams.addChild(new Label("Max Agents"), "split 2, growx"); 
        doc = new DocumentModelFilter();
        doc.setInputTransform(TextFilters.numeric());
        doc.setText("100");
        fieldMaxAgents = contCrowdParams.addChild(new TextField(doc));
        fieldMaxAgents.setSingleLine(true);
        fieldMaxAgents.setPreferredWidth(50);
        
        //Max CrowdAgent radius for an agent in the crowd.
        contCrowdParams.addChild(new Label("Max Agent Radius"), "split 2, growx, top");
        fieldMaxAgentRadius = contCrowdParams.addChild(new TextField("0.6"), "top");
        fieldMaxAgentRadius.setSingleLine(true);
        fieldMaxAgentRadius.setPreferredWidth(50);                            
        
        
        
        //The Active Grids listBox.
        contCrowdParams.addChild(new Label("Active Grids"), "wrap"); 
        listBoxActiveGrids = contCrowdParams.addChild(new ListBox<>(), "growx, span");
        listBoxActiveGrids.setName("listBoxActiveGrid");
        listBoxActiveGrids.getModel().add(defaultActiveGridText);
        
        //Active crowds list.
        contCrowdParams.addChild(new Label("Active Crowds"), "wrap");
        listBoxActiveCrowds = contCrowdParams.addChild(new ListBox<>(), "growx, growy, span"); 
        listBoxActiveCrowds.setName("listBoxActiveCrowds");
        crowdSelectRef = listBoxActiveCrowds.getSelectionModel().createReference();  
        crowdModelRef = listBoxActiveCrowds.getModel().createReference();
        listBoxActiveCrowds.setCellRenderer(new DefaultCellRenderer<Crowd>(new ElementId("list.item"), null) {
            @Override
            protected String valueToString( Crowd crowd ) {
                String txt = "Crowd [ ";
                CrowdManager crowdManager = getState(CrowdManagerAppState.class).getCrowdManager();
                int numberOfCrowds = crowdManager.getNumberOfCrowds();
                for (int i = 0; i < numberOfCrowds; i++) {
                    if (crowdManager.getCrowd(i).equals(crowd)) {
                        txt += i + " ] Size [ ";
                        break;
                    }
                }
                
                txt += crowd.getAgentCount() + " ] ";
                txt += crowd.getApplicationType();
                return txt;
            }
        });

        //Button to stop the Crowd.
        contCrowdParams.addChild(new ActionButton(new CallMethodAction("Shutdown Crowd", this, "shutdown")), "top, wrap");
        
        

        //Legend and Setup buttons.
        Container contButton = new Container(new MigLayout(null, // Layout Constraints
                "[]push[][]")); // Column constraints [min][pref][max]
        contButton.setName("CrowdBuilderState contButton");
        contButton.setAlpha(1, false);
        contCrowdParams.addChild(contButton, "growx, span 2"); //cell col row span w h
        //Help
        contButton.addChild(new ActionButton(new CallMethodAction("Help", this, "showHelp")));
        //Button to start the Crowd.
        contButton.addChild(new ActionButton(new CallMethodAction("Start Crowd", this, "startCrowd")));
        
        
        
        //Container that will hold the tab panel for BuildGridGui and BuildParamGui gui.
        contTabs = new Container(new MigLayout("wrap"));
        contTabs.setName("CrowdBuilderState contTabs");
        //Make it dragable.
        DragHandler dragHandler = new DragHandler();
        CursorEventControl.addListenersToSpatial(contTabs, dragHandler);
        contTabs.addChild(new Label("Crowd Builder"));
        
        
        
        //Create the tabbed panel.
        TabbedPanel tabPanel = contTabs.addChild(new TabbedPanel());
        
        
        
        //Modify tabs to stretch collapse button. MigLayout(null, "[grow, fill]"))
        //Add a rollup panel for the crowd settings panel.
        RollupPanel rollCrowd = new RollupPanel("Expand / Collapse", 
                contCrowd, "glass");
        rollCrowd.getTitleContainer().setLayout(new MigLayout(null, "[grow, fill]"));
        rollCrowd.getTitleElement().removeFromParent();
        rollCrowd.getTitleContainer().addChild(rollCrowd.getTitleElement());
        rollCrowd.getTitleElement().setTextHAlignment(HAlignment.Center);
        rollCrowd.setAlpha(0, false); 
        rollCrowd.setOpen(false);
        tabPanel.addTab("Crowd", rollCrowd);
        
        
        
        //Add a rollup panel so can hide agent grid.
        RollupPanel rollAgentGrid = new RollupPanel("Expand / Collapse", 
                getState(AgentGridState.class).getContAgentGrid(), "glass");
        rollAgentGrid.getTitleContainer().setLayout(new MigLayout(null, "[grow, fill]"));
        rollAgentGrid.getTitleElement().removeFromParent();
        rollAgentGrid.getTitleContainer().addChild(rollAgentGrid.getTitleElement());
        rollAgentGrid.getTitleElement().setTextHAlignment(HAlignment.Center);
        rollAgentGrid.setAlpha(0, false);
        rollAgentGrid.setOpen(false);
        tabPanel.addTab("Agent Grid", rollAgentGrid);
        
        
        
        //Add a rollup panel so can hide CrowdAgent parameters.
        RollupPanel rollAgentParam = new RollupPanel("Expand / Collapse", 
                getState(AgentParamState.class).getContAgentParams(), "glass");
        rollAgentParam.getTitleContainer().setLayout(new MigLayout(null, "[grow, fill]"));
        rollAgentParam.getTitleElement().removeFromParent();
        rollAgentParam.getTitleContainer().addChild(rollAgentParam.getTitleElement());
        rollAgentParam.getTitleElement().setTextHAlignment(HAlignment.Center);
        rollAgentParam.setAlpha(0, false); 
        rollAgentParam.setOpen(false);
        tabPanel.addTab("Agent Parameters", rollAgentParam);
        
        
        
        int height = getApplication().getCamera().getHeight();
        contTabs.setLocalTranslation(new Vector3f(0, height, 0));
        
        ((SimpleApplication) getApplication()).getGuiNode().attachChild(contTabs);
    }

    /**
     * Called by the DemoApplication F1 button ActionListener as part of a chain 
     * detachment of states. This is the start of the detachment chain.
     * CrowdBuilderState(onDisable)=>AgentParamState(onDisable)=>AgentGridState(onDisable)
     */
    @Override
    protected void onDisable() {
        if (getStateManager().hasState(getState(AgentParamState.class))) {
            getStateManager().getState(AgentParamState.class).setEnabled(false);
        }
        getStateManager().detach(this);
    }
    
    @Override
    public void update(float tpf) {

        //Have to check for both crowdSelectRef(the selections themselves) and 
        //modelRef(the list of crowds) updates to fully know whether the 
        //reference has been updated.
        if( crowdSelectRef.update() || crowdModelRef.update()) {
            // Selection has changed and the model or selection is empty so set 
            //defaults.
            if ( crowdSelectRef.get().isEmpty() ||  crowdModelRef.get().isEmpty()) {
                                
                //Clear the Avoidance Param window.
                listBoxAvoidance.getModel().clear();
                
                //Clear all OAP fields.
                clearOAPFields();

                //Clear the Active Grids window.
                listBoxActiveGrids.getModel().clear();
                listBoxActiveGrids.getModel().add(defaultActiveGridText);
                
            } else {
                //Look for the selectedCrowd in listBoxCrowds rather than pulling 
                //directly from CrowdManager in case CrowdState is running at 
                //the same time as gui.
                Crowd crowd = getSelectedCrowd();

                if (crowd != null) {
                    
                    //If listBoxAvoidance has objects, clear them to get ready 
                    //for update.
                    if (listBoxAvoidance.getModel().size() > 0) {
                        listBoxAvoidance.getModel().clear();
                    }
                    
                    //Populate listBoxAvoidance for the selected crowd.
                    for (int i = 0; i < DT_CROWD_MAX_OBSTAVOIDANCE_PARAMS; i++) {
                        ObstacleAvoidanceParams oap = crowd.getObstacleAvoidanceParams(i);
                        listBoxAvoidance.getModel().add(oap);
                    }
                    
                    //Look for the gridAgents CrowdAgent in the selected crowd.
                    List<Grid> listGrids = getState(AgentGridState.class).getGrids(); 

                    //Remove any existing text from listBoxActiveGrids to prepare 
                    //for update.
                    listBoxActiveGrids.getModel().clear();
                    
                    for (Grid grid: listGrids) {
                        //We only need one agent to know if the Crowd contains
                        //this grid, get first.
                        CrowdAgent gridAgent = grid.getListGridAgent().get(0).getCrowdAgent();
                        if (crowd.getActiveAgents().contains(gridAgent)) {
                            String txt = grid.getGridName() 
                                    + " < " + grid.getListGridAgent().size() 
                                    + "/" + crowd.getActiveAgents().size() 
                                    + "/" + crowd.getAgentCount() + " >";
                            listBoxActiveGrids.getModel().add(txt);
                        }
                    }
                    
                    //Empty so set default text.
                    if (listBoxActiveGrids.getModel().isEmpty()) {
                        listBoxActiveGrids.getModel().add(defaultActiveGridText);
                    }
                    
                    /**
                     * Populates listBoxFilters with the selected crowds query 
                     * filters whenever a new crowd is selected.
                     */
                    if (checkEditActive.isChecked() ) {
                        updateFiltersList();
                    }
                } 
            }
        }
        
        /**
         * Updates when listBoxFilters selections are made. If for any reason 
         * the versioned reference returns empty, the listBoxFilters will 
         * re-populate with BetterDefaultQueryFilters if no crowd is selected, 
         * or with the selected crowds filters if a crowd is selected.
         * 
         * Selection changes update the include/exclude checkboxes based off the
         * listBoxFilters objects.
         */
        if (filtersSelectRef.update() || filtersModelRef.update()) {
            if (filtersSelectRef.get().isEmpty() || filtersModelRef.get().isEmpty()) {
                updateFiltersList(); 
            } else {
                //Update include/exclude checkBoxes.
                updateIncludeExclude();
            }
        }
        
        /**
         * Updates when checkBoxEditActive is toggled.
         * 
         * If unchecked: When a new crowd is created then the current filters in 
         * listBoxFilters are used for crowd constructor.
         * 
         * If checked: When a new crowd is created with a crowd already selected, 
         * then listBoxFilters is ignored and the default constructor for crowd 
         * creation is used where the first filter is BetterDefaultQueryFilter 
         * and the remaining are DefaultQueryFilters. All area costs are set to 
         * one. If no crowd is selected, then the current filters in 
         * listBoxFilters are used for crowd constructor as well as current area 
         * costs.
         * 
         * If a crowd is selected: When toggled to checked, listBoxFilters will 
         * clear and re-populate using the selected crowd. When toggled to 
         * unchecked, all filters are reset to new BetterDefaultQueryFilters.
         * 
         * If no crowd is selected: No changes to the list will be made. 
         */
        if (editActiveSelRef.update()) {
            if(getSelectedCrowd() == null) {
                //Do nothing when no crowds selected. Allows pre-fill of filters
                //prior to starting crowd.
            } else {
                //Update listBoxFilters.
                updateFiltersList();
            }
        }

    }
    
    //Explains the Crowd parameters.
    private void showHelp() {
                
        String[] msg = { 
        "Obstacle Avoidance Parameters - The shared avoidance configuration for an Agent inside the ",
        "crowd. When first instantiating the crowd, you are allotted eight parameter objects in total. All ",
        "eight slots are filled with the defaults listed below.", 
        " ",
        "[Defaults]",
        "velBias             = 0.4f \t    horizTime         = 2.5f",
        "weightDesVel = 2.0f \t    gridSize             = 33",
        "weightCurVel  = 0.75f \t adaptiveDivs     = 7",
        "weightSide      = 0.75f \t adaptiveRings   = 2",
        "weightToi         = 2.5f \t   adaptiveDepth  = 5",
        " ",
        "To change any parameter, select the crowd to be updated from the Active Crowds window, ",
        "select any parameter from the list, set the parameters you desire in the Obstacle Avoidance ",
        "Parameters section, click the [ Update Parameter ] button.",
        " ",
        "* velBias - The velocity bias describes how the sampling patterns is offset from the (0,0) based ",
        "on the desired velocity. This allows to tighten the sampling area and cull a lot of samples. ",
        "[Limit: 0-1]",
        " ",
        "* weightDesVel - How much deviation from desired velocity is penalized, the more penalty ",
        "applied to this, the more \"goal oriented\" the avoidance is, at the cost of getting more easily ",
        "stuck at local minima. [Limit: >= 0]",
        " ",
        "* weightCurVel - How much deviation from current velocity is penalized, the more penalty ",
        "applied to this, the more stubborn the agent is. This basically is a low pass filter, and very ",
        "important part of making things work.",
        " ",
        "* weightSide - In order to avoid reciprocal dance, the agents prefer to pass from right, this ",
        "weight applies penalty to velocities which try to take over from the wrong side.",
        " ",
        "* weightToi - How much penalty is added based on time to impact. Too much penalty", 
        "and the agents are shy, too little and they avoid too late.",
        " ",
        "* horizTime - Time horizon, this affects how early the agents start to avoid each other. Too long ",
        "horizon and the agents are scared of going through tight spots, and too small, and they avoid ",
        "too late (closely related to weightToi).",
        " ",
        "* gridSize - ???",
        " ",
        "* adaptiveDivs - Number of divisions per ring. [Limit: 1-32]",
        " ",
        "* adaptiveRings - Number of rings. [Limit: 1-4]",
        " ",
        "* adaptiveDepth - Number of iterations at best velocity.",
        " ",
        "Query Filters - Upon GUI startup, the filters list will be populated with BetterDefaultQuery ",
        "filters and display the selected filter include/exclude flags. Each filter in the list has \"All\" include ",
        "flags, no exclude flags and all area costs are 1.0f. You use this panel to create new filters or ",
        "update existing crowd filters. These Area Modifications are broken down into Area Types and ",
        "Ability Flags.",
        " ",
        "* Area Type - Specifies the cost to travel across the polygon.",
        "* Ability Flag - Specifies if the agent can travel through the area at all.",
        " ",
        "There are 64 Area Types and 16 Ability Flags that can be created. The polygon flag filter works ",
        "so that you can specify certain flags (abilities), which must be on (included), and certain flags ", 
        "which must not be on (excluded) for a polygon to be valid for the path. Polygons have a travel ", 
        "cost set when generating the NavMesh which is the Area Type. Agents prefer paths that have a ", 
        "lower cost. You set the filter for the agent in the [ Agent Parameters ] tab.",
        " ",
        "* Filter - Each Crowd has 16 filters available for path finding. You can set these filters, their ",
        "include/exclude flags and costs prior to starting any crowd, but only flags may be updated after ",
        "startup. Selecting any filter in the list will also display that filters include/exclude flags but not ",
        "Area Costs. They will never update because the getCost() method is expected to be used only ",
        "internally after creation.",
        " ",
        "* Edit Active - If unchecked: When a new crowd is created, the current filters in the Filters list ",
        "are used for the crowd constructor that accepts filters.",
        " ",
        "If checked: When a new crowd is created and no crowd is selected, then the current filters in ",
        "the Filters list are used for the crowd constructor that accepts filters. With a crowd already ",
        "selected, then the Filters list is ignored and the default constructor for crowd creation is used ",
        "where the first filter is BetterDefaultQueryFilter and the remaining are DefaultQueryFilters. ",
        "Each filter has \"All\" include flags, no exclude flags and all area costs are 1.0f. ",
        " ",
        "If a crowd is selected: When toggled to checked, the Filters list will clear and re-populate using ",
        "the selected crowd. When toggled to unchecked, all filters are reset to new ",
        "BetterDefaultQueryFilters.",
        " ",
        "If no crowd is selected: No changes to the list will be made.",
        " ",
        
        "* Update Filter - This will create a new filter, replacing the selected filter, using any ",
        "include/exclude flags and area costs. Any cost not filled or improperly filled will be set to one ",
        "upon using the update button.", 
        " ",
        "NavMesh - The navigation mesh to use for planning.",
        " ",
        "Max Agents - The maximum number of agents the crowd can manage. [Limit: >= 1]",
        "  ",
        "Max Agent Radius - The maximum radius of any agent that will be added to the crowd.",
        " ",
        "Movement Type - Each type determines how movement is applied to an agent.",
        " ",
        "* BETTER_CHARACTER_CONTROL - Use physics and the BetterCharacterControl to set move ",
        "and view direction. Grids containing Physics agents are the only type allowed for this Crowd.",
        " ",
        "* DIRECT - Direct setting of the agents translation and view direction. No controls are needed ",
        "for movement.",
        " ",
        "* CUSTOM - With custom, you implement the applyMovement() method from the ",
        "ApplyFunction interface. This will give you access to the agents CrowdAgent object, the new ",
        "position and the velocity of the agent. It's use is not supported by this demo.",
        " ",
        "* NONE - No movement is implemented. You can freely interact with the crowd and implement ",
        "your own movement soloutions. It's use is not supported by this demo.",
        " ",
        "Active Crowds - The list of active crowds. One crowd must be selected before any agents can ",
        "be added or targets for agents can be set.",
        " ",
        "* Format: Name [Crowd Number] [Active Agents Limit] Movement Type",  
        " ",
        "Active Grids - Displays information about active grids residing in the selected crowd.",
        " ",
        "* Format: Name <Grid Size/Crowd Active Agents Size/Crowd Active Agents Limit>."
        };
        
        Container window = new Container(new MigLayout("wrap"));
        ListBox<String> listScroll = window.addChild(new ListBox<>());
        listScroll.getModel().addAll(Arrays.asList(msg));
        listScroll.setPreferredSize(new Vector3f(500, 400, 0));
        listScroll.setVisibleItems(20);
        window.addChild(new ActionButton(new CallMethodAction("Close", window, "removeFromParent")), "align 50%");
        getState(UtilState.class).centerComp(window);
        //This assures clicking outside of the message should close the window 
        //but not activate underlying UI components.
        GuiGlobals.getInstance().getPopupState().showPopup(window, PopupState.ClickMode.ConsumeAndClose, null, null);
    }
    
    /**
     * Shuts down crowds listed in the Active Crowds list. When CrowdState is
     * enabled this will not be affected due to using the crowd of mapCrowds
     * to remove the Crowd from the CrowdManager. 
     */
    private void shutdown() {

        //Get the crowds name from the listBoxActiveCrowds selectedParam. Could 
        //use the getter for this but would need to check for null again and we 
        //went thorugh the trouble of creating selection variable so use it.
        Crowd selectedCrowd = getSelectedCrowd();

        //We check mapCrowds to see if the key exists. If not, go no further.
        if (selectedCrowd == null) {
            displayMessage("No crowd found.", 0);
            return;
        }
        
        //We have a valid crowd so remove it from the map, the CrowdManager and 
        //the listBoxActiveCrowds.
        Iterator<Crowd> iterator = mapCrowds.keySet().iterator();        
        while (iterator.hasNext()) {
            Crowd crowd = iterator.next();
            if (crowd.equals(selectedCrowd)) {
                List<CrowdAgent> activeAgents = crowd.getActiveAgents();
                List<Grid> listGrids = getState(AgentGridState.class).getGrids();
                boolean found = false;
                //Check the crowdAgents against the gridAgents corwdAgent so we
                //csn remove any Crowdspecific controls and reset movement.
                for (CrowdAgent ca: activeAgents) {
                    //Grab the grid to check from the list.
                    for (Grid grid: listGrids) {
                        List<GridAgent> listGridAgent = grid.getListGridAgent();
                        //Check for any CrowdAgents that are in the grid and 
                        //crowd. 
                        for (GridAgent ga: listGridAgent) {
                            if (ga.getCrowdAgent() != null) {
                                //We have a CrowdAgent and the GridAgents 
                                //CrowdAgent match so check for Crowd specific 
                                //control to manipulate or remove.
                                if (ga.getCrowdAgent().equals(ca)) {
                                    //Physics agents need to have their BCC reset.
                                    if (ga.getSpatialForAgent().getControl(PhysicsAgentControl.class) != null) {
                                        LOG.info("Resetting Move [{}] idx [{}].", ga.getSpatialForAgent(), ga.getCrowdAgent().idx);
                                        ga.getSpatialForAgent().getControl(PhysicsAgentControl.class).stopFollowing();
                                    }

                                    //CrowdDebugControl is crowd specific so remove 
                                    //if found.
                                    if (ga.getSpatialForAgent().getControl(CrowdDebugControl.class) != null) {
                                        LOG.info("Removing CrowdDebugControl [{}] idx [{}].", ga.getSpatialForAgent(), ga.getCrowdAgent().idx);
                                        ga.getSpatialForAgent().removeControl(CrowdDebugControl.class);
                                    }

                                    //CrowdChangeControl is crowd specific so remove
                                    //if found.
                                    LOG.info("Removing CrowdChangeControl [{}] idx [{}].", ga.getSpatialForAgent(), ga.getCrowdAgent().idx);
                                    ga.getSpatialForAgent().removeControl(CrowdChangeControl.class);
                                    //CrowdAgent is crowd specific so null out 
                                    //the Crowdagent for this gridAgent.
                                    ga.setCrowdAgent(null);
                                    //We have a GridAgent so notify outter loop it 
                                    //was found and break out.
                                    found = true;
                                    break;
                                }
                            }
                        }

                        //We found a GridAgent so this loop is done.
                        if (found) {
                           break; 
                        }
                    }
                    //Remove CrowdAgent from crowd.
                    LOG.info("Removing idx [{}] crowd [{}]", ca.idx, crowd);
                    crowd.removeAgent(ca);
                }
                //To fully remove the crowd we have to remove it from the 
                //CrowdManager, mapCrowds (removes the query object also), and 
                //the listBoxActiveCrowds.
                getState(CrowdManagerAppState.class).getCrowdManager().removeCrowd(crowd);
                listBoxActiveCrowds.getModel().remove(crowd);
                //Lemur getSelected() does not update if you remove or insert 
                //into a listBox. Best to set it to -1 (unselected) and force
                //user to reselect.
                listBoxActiveCrowds.getSelectionModel().setSelection(-1);
                iterator.remove();
                //Refresh Agent Parameter panel.
                getState(AgentParamState.class).updateParams();
                //Refresh filters panel if checkEditActive.
                if (checkEditActive.isChecked()) {
                    updateFiltersList();
                }
                break;
            }
        } 

    }
    
    /**
     * Starts and adds a crowd to the CrowdManager.
     */
    private void startCrowd() {
        int maxAgents;
        float maxAgentRadius;
        String mesh;

        //The name of the mesh to load.                
        if (fieldNavMesh.getText().isEmpty()) {
            displayMessage("You must enter a [ NavMesh ] name.", 0);
            return;
        } else {
            mesh = fieldNavMesh.getText();
        }
        
        //The max CrowdAgents for the crowd. Uses numeric doc filter to prevent bad data.
        if (fieldMaxAgents.getText().isEmpty()) {
            displayMessage("[ Max Agents ] requires a valid int value.", 0);
            return;
        } else {
            maxAgents = new Integer(fieldMaxAgents.getText());
            //Stop useless input.
            if (maxAgents < 1) {
                displayMessage("[ Max Agents ] requires a int value >= 1", 0);
                return;
            }
        }
        
        //The max CrowdAgent radius for an agent in the crowd.
        if (!getState(UtilState.class).isNumeric(fieldMaxAgentRadius.getText()) 
        ||  fieldMaxAgentRadius.getText().isEmpty()) {
            displayMessage("[ Max Agent Radius ] requires a valid float value.", 0);
            return;
        } else {
            maxAgentRadius = new Float(fieldMaxAgentRadius.getText());
            //Stop negative input.
            if (maxAgentRadius <= 0.0f ) {
                displayMessage("[ Max Agent Radius ] requires a float value between > 0.", 0);
                return;
            }
        }
        
        MovementApplicationType applicationType = null;
        
        switch (listBoxMoveType.getSelectionModel().getSelection()) {
            case 0:
                applicationType = MovementApplicationType.BETTER_CHARACTER_CONTROL;
                break;
            case 1: 
                applicationType = MovementApplicationType.DIRECT;
                break;
            case 2:
                applicationType = MovementApplicationType.CUSTOM;
                break;
            case 3:
                applicationType = MovementApplicationType.NONE;
        }
        
        MeshSetReader msr = new MeshSetReader();
        try {
            //Read in the saved navMesh with same maxVertPerPoly(3) saved. Will 
            //be added to mapCrowds as a key using the text returned by 
            //fieldCrowdName.
            NavMesh navMesh = msr.read(new FileInputStream(mesh), 3);
            //Create the query object for pathfinding in this Crowd. Will be 
            //added to the mapCrowds as a crowd so each query object is referenced.  
            NavMeshQuery query = new NavMeshQuery(navMesh);
                            
            Crowd crowd;
            
            if (checkEditActive.isChecked() && getSelectedCrowd() != null) {
                /**
                 * Create crowd using default filters where first filter will be
                 * BetterDefaultQueryFilter and rest DefaultQueryFilter when 
                 * checkeditActive and there is a crowd selected.
                 */
                crowd = new Crowd(applicationType, maxAgents, maxAgentRadius, navMesh);
            } else {
                //Create crowd using filters from list.
                IntFunction<QueryFilter> filters = (int i) -> listBoxFilters.getModel().get(i);
                //Create the crowd.
                crowd = new Crowd(applicationType, maxAgents, maxAgentRadius, navMesh, filters);
                /**
                 * Clear and re-populate the listBoxFilters list with new 
                 * filters if checkedEditActive is unchecked or there is no 
                 * currently selected crowd and checkEditActive is checked .
                 * Failure to update will leave referenced filters in the list.
                 */
                updateFiltersList();
            }

            crowd.setFormationHandler(new CircleFormationHandler(maxAgents, crowd, 2f));
            
            //Add to CrowdManager, mapCrowds, and listBoxActiveCrowds.
            getState(CrowdManagerAppState.class).getCrowdManager().addCrowd(crowd);
            mapCrowds.put(crowd, query);
            listBoxActiveCrowds.getModel().add(crowd); 
        } catch (IOException | InstantiationException ex) {
            LOG.error("{} {}", CrowdBuilderState.class.getName(), ex);
        }

    }
    
    /**
     * Takes a string and looks for the token '$' to format a new string based 
     * on the char following the token. Builds the new ObstacleAvoidanceParams 
     * object for the selected Crowd and inserts it into the appropriate slot. 
     * It is expected the Crowd is running.
     * 
     * @param format The string to be formatted.
     */
    private void updateOAP() {
        float velBias;
        float weightDesVel;
        float weightCurVel;
        float weightSide;
        float weightToi;
        float horizTime;
        int gridSize;
        int adaptiveDivs;
        int adaptiveDepth;
        int adaptiveRings;
        Integer selectedParam;
                
        //The velocity bias settings.
        if (!getState(UtilState.class).isNumeric(fieldVelocityBias.getText()) 
        ||  fieldVelocityBias.getText().isEmpty()) {
            displayMessage("[ velocityBias ] requires a valid float value.", 0);
            return;
        } else {
            velBias = new Float(fieldVelocityBias.getText());
            //Stop negative input.
            if (velBias < 0.0f || velBias > 1) {
                displayMessage("[ velocityBias ] requires a float value between 0 and 1 inclusive.", 0);
                return;
            }
        }
        
        //The weighted desired velocity settings.
        if (!getState(UtilState.class).isNumeric(fieldWeightDesVel.getText()) 
        ||  fieldWeightDesVel.getText().isEmpty()) {
            displayMessage("[ weightDesVel ] requires a valid float value.", 0);
            return;
        } else {
            weightDesVel = new Float(fieldWeightDesVel.getText());
            //Stop negative input.
            if (weightDesVel < 0.0f) {
                displayMessage("[ weightDesVel ] requires a float value >= 0.", 0);
                return;
            }
        }

        //The weighted current velocity settings.
        if (!getState(UtilState.class).isNumeric(fieldWeightCurVel.getText()) 
        ||  fieldWeightCurVel.getText().isEmpty()) {
            displayMessage("[ weightCurVel ] requires a valid float value.", 0);
            return;
        } else {
            weightCurVel = new Float(fieldWeightCurVel.getText());
            //Stop negative input.
            if (weightCurVel < 0.0f) {
                displayMessage("[ weightCurVel ] requires a float value >= 0.", 0);
                return;
            }
        }
        
        //The weighted side settings.
        if (!getState(UtilState.class).isNumeric(fieldWeightSide.getText()) 
        ||  fieldWeightSide.getText().isEmpty()) {
            displayMessage("[ weightSide ] requires a valid float value.", 0);
            return;
        } else {
            weightSide = new Float(fieldWeightSide.getText());
            //Stop negative input.
            if (weightSide < 0.0f) {
                displayMessage("[ weightSide ] requires a float value >= 0.", 0);
                return;
            }
        }
        
        //The weight to impact settings.
        if (!getState(UtilState.class).isNumeric(fieldWeightToi.getText()) 
        ||  fieldWeightToi.getText().isEmpty()) {
            displayMessage("[ weightToi ] requires a valid float value.", 0);
            return;
        } else {
            weightToi = new Float(fieldWeightToi.getText());
            //Stop negative input.
            if (weightToi < 0.0f) {
                displayMessage("[ weightToi ] requires a float value >= 0.", 0);
                return;
            }
        }        
                
        //The horizon settings.
        if (!getState(UtilState.class).isNumeric(fieldHorizTime.getText()) 
        ||  fieldHorizTime.getText().isEmpty()) {
            displayMessage("[ horizTime ] requires a valid float value.", 0);
            return;
        } else {
            horizTime = new Float(fieldHorizTime.getText());
            //Stop negative input.
            if (weightToi < 0.0f) {
                displayMessage("[ horizTime ] requires a float value >= 0.", 0);
                return;
            }
        }       
        
        //The grid size settings. Uses numeric doc filter to prevent bad data.
        if (fieldGridSize.getText().isEmpty()) {
            displayMessage("[ gridSize ] requires a valid int value.", 0);
            return;
        } else {
            gridSize = new Integer(fieldGridSize.getText());
            //Stop useless input.
            if (gridSize < 1) {
                displayMessage("[ gridSize ] requires a int value >= 1.", 0);
                return;
            }
        }
        
        //The adaptive divisions settings. Uses numeric doc filter to prevent
        //bad data.
        if (fieldAdaptiveDivs.getText().isEmpty()) {
            displayMessage("[ adaptiveDivs ] requires a valid int value.", 0);
            return;
        } else {
            adaptiveDivs = new Integer(fieldAdaptiveDivs.getText());
            //Stop useless input.
            if (adaptiveDivs < 1 || adaptiveDivs > 32) {
                displayMessage("[ adaptiveDivs ] requires a int value between 1 and 32 inclusive.", 0);
                return;
            }
        }
        
         //The adaptive depth settings. Uses numeric doc filter to prevent bad data.
        if (fieldAdaptiveDepth.getText().isEmpty()) {
            displayMessage("[ adaptiveDepth ] requires a valid int value.", 0);
            return;
        } else {
            adaptiveDepth = new Integer(fieldAdaptiveDepth.getText());
            //Stop useless input.
            if (adaptiveDepth < 1 ) {
                displayMessage("[ adaptiveDepth ] requires a int value >= 1.", 0);
                return;
            }
        }
        
        //The adaptive depth settings. Uses numeric doc filter to prevent bad data.
        if (fieldAdaptiveRings.getText().isEmpty()) {
            displayMessage("[ adaptiveRings ] requires a valid int value.", 0);
            return;
        } else {
            adaptiveRings = new Integer(fieldAdaptiveRings.getText());
            //Stop negative input.
            if (adaptiveRings < 1 || adaptiveRings > 4) {
                displayMessage("[ adaptiveRings ] requires a int value between 1 and 4 inclusive.", 0);
                return;
            }
        }
        
        //Get the selectedParam from listBoxAvoidance.
        selectedParam = listBoxAvoidance.getSelectionModel().getSelection();
        
        //Check to make sure a an avoidance parmeter has been selected.
        if (selectedParam == null) {
            displayMessage("You must select a [ Parameter ] from the list before it can be updated.", 0);
            return;
        }

        ObstacleAvoidanceParams params  = new ObstacleAvoidanceParams();
        params.velBias          = velBias;
        params.weightDesVel     = weightDesVel;
        params.weightCurVel     = weightCurVel;
        params.weightSide       = weightSide;
        params.weightToi        = weightToi;
        params.horizTime        = horizTime;
        params.gridSize         = gridSize;
        params.adaptiveDivs     = adaptiveDivs;
        params.adaptiveDepth    = adaptiveDepth;
        params.adaptiveRings    = adaptiveRings;

        //Inject the new parameter into the crowd. Check for the crowd in 
        //the crowds list and if exists, update OAP params. Pulls the crowd 
        //reference from listBoxCrowds rather than the CrowdManager in case 
        //CrowdState is running. This will keep our crowd lookups in sync.
        if (getSelectedCrowd() != null) {
            getSelectedCrowd().setObstacleAvoidanceParams(selectedParam, params);
            //Remove selected parameter from listBoxAvoidance. Lemur does not 
            //currently update the selection when items are removed or inserted.
            //If this changes, this must be reworked to account for changes.
            listBoxAvoidance.getModel().clear();
            
            //Force a selctor reference update to repopulate the params.
            updateVersRef();
            
        } else {
            LOG.error("Failed to find the selected crowd in listBoxCrowds [{}]", getSelectedCrowd());
            displayMessage("You must select a [ Active Crowd ] before a parameter can be updated.", 0);
        }
        
    }
    
    /**
     * Formats a Obstacle Avoidance Parameter into string form for insertion into  
     * a listBoxAvoidance selection.
     * Parameters are case sensitive and are as follows:
     *          'b' sets the Velocity Bias
     *          'd' sets the Adaptive Divisions
     *          'D' sets the Adaptive Depth
     *          'g' sets the Grid Size
     *          'h' sets the Horizon Time
     *          'i' sets the Weight To Impact
     *          'r' sets the Adaptive Rings
     *          's' sets the Weight Side
     *          'v' sets the Weight Desired Velocity
     *          'V' sets the Weight Current Velocity
     * 
     * @param oap The Obstacle Avoidance Parameter that needs string formating.
     * @param idx The index of the listboxAvoidance parameter.
     * @return The formated string.
     */
    private String formatOAP(ObstacleAvoidanceParams oap) {
        //If run into threading problems use StringBuffer.
        StringBuilder buf   = new StringBuilder();
        String i            = null;
        Integer idx         = null;
        Crowd selectedCrowd = getSelectedCrowd();
        
        if (selectedCrowd != null) {
            for (int k = 0; k < DT_CROWD_MAX_OBSTAVOIDANCE_PARAMS; k++) {
                if (selectedCrowd.getObstacleAvoidanceParams(k).equals(oap)) {
                    idx = k;
                    break;
                }
            }
        }

        String str = 
          "<=====    " + idx + "    =====>\n"  
        + "velBias              = $b\n"
        + "weightDesVel  = $v\n"
        + "weightCurVel   = $V\n"
        + "weightSide       = $s\n"
        + "weightToi         = $i\n"
        + "horizTime         = $h\n"
        + "gridSize            = $g\n"
        + "adaptiveDivs    = $d\n"               
        + "adaptiveRings  = $r\n"
        + "adaptiveDepth = $D";
        
        LOG.info("<========== BEGIN CrowdBuilderState formatOAP [{}]==========>", idx);
        for ( int j = 0; j < str.length(); j++ ) {
            if ( str.charAt(j) != '$' ) {
                buf.append(str.charAt(j));
                continue;
            }
            
            char charAt = str.charAt(++j);

            switch (charAt) {
                case 'b': //Velocity Bias
                    i = "" + oap.velBias;
                    fieldVelocityBias.setText(i);
                    LOG.info("velBias       [{}]", oap.velBias);
                    break;
                case 'd': //Adaptive Divisions
                    i = "" + oap.adaptiveDivs;
                    fieldAdaptiveDivs.setText(i);
                    LOG.info("adaptiveDivs  [{}]", oap.adaptiveDivs);
                    break;
                case 'D': //Adaptive Depth
                    i = "" + oap.adaptiveDepth;
                    fieldAdaptiveDepth.setText(i);
                    LOG.info("adaptiveDepth [{}]", oap.adaptiveDepth);
                    break;
                case 'g': //Grid Size
                    i = "" + oap.gridSize;
                    fieldGridSize.setText(i);
                    LOG.info("gridSize      [{}]", oap.gridSize);
                    break;
                case 'h': //Horizon Time
                    i = "" + oap.horizTime;
                    fieldHorizTime.setText(i);
                    LOG.info("horizTime     [{}]", oap.horizTime);
                    break;
                case 'i': //Weight To Impact
                    i = "" + oap.weightToi;
                    fieldWeightToi.setText(i);
                    LOG.info("weightToi     [{}]", oap.weightToi);
                    break;
                case 'r': //Adaptive Rings
                    i = "" + oap.adaptiveRings;
                    fieldAdaptiveRings.setText(i);
                    LOG.info("adaptiveRings [{}]", oap.adaptiveRings);
                    break;
                case 's': //Weight Side
                    i = "" + oap.weightSide;
                    fieldWeightSide.setText(i);
                    LOG.info("weightSide    [{}]", oap.weightSide);
                    break;
                case 'v': //Weight Desired Velocity
                    i = "" + oap.weightDesVel;
                    fieldWeightDesVel.setText(i);
                    LOG.info("weightDesVel  [{}]", oap.weightDesVel);
                    break;
                case 'V': //Weight Current Velocity
                    i = "" + oap.weightCurVel;
                    fieldWeightCurVel.setText(i);
                    LOG.info("weightCurVel  [{}]", oap.weightCurVel);
                    break;
            }
            buf.append(i);
        } 
        LOG.info("<========== END CrowdBuilderState formatOAP [{}] ==========>", idx);
        return buf.toString();
        
    }
    
    /**
     * Updates a single query filter. If a crowd is selected and checkEditActive 
     * is checked then only the selected crowds selected filter will be updated 
     * and then only the include/exclude flags.
     * 
     * For all other situations, a new filter will be created using the 
     * includes/excludes flags and any valid area cost above 1.0f. Any cost 
     * field that is not valid or < 1.0f will be set to one.
     */
    private void updateFilter() {

        Integer selected = listBoxFilters.getSelectionModel().getSelection();
        DefaultQueryFilter filter;

        //We are editing an existing crowd filter so only set filters.
        if (checkEditActive.isChecked() && getSelectedCrowd() != null) {
            filter = listBoxFilters.getModel().get(selected);
            filter.setIncludeFlags(getIncludes());
            filter.setExcludeFlags(getExcludes());
        } else {
            //We are editing a new filter so we can create and replace any 
            //filters we choose using areaCosts.
            float[] areaCost = new float[MAX_AREAMOD];
        
            //Prefill array in case invalid data is in any of the following 
            //textfields.
            for (int i = 0; i < MAX_AREAMOD; i++) {
                areaCost[i] = 1.0f;
            }

            //All types must be in same order as those in SampleAreaModifications.
            if (!fieldCostGround.getText().isEmpty() 
            &&   getState(UtilState.class).isNumeric(fieldCostGround.getText()) ) {
                float cost = new Float(fieldCostGround.getText());
                if (cost > 1.0f) {
                    areaCost[0] = cost;
                }
            }

            if (!fieldCostWater.getText().isEmpty()
            &&   getState(UtilState.class).isNumeric(fieldCostWater.getText()) ) {
                float cost = new Float(fieldCostWater.getText());
                if (cost > 1.0f) {
                    areaCost[1] = cost;
                }
            }

            if (!fieldCostRoad.getText().isEmpty()
            &&   getState(UtilState.class).isNumeric(fieldCostRoad.getText()) ) {
                float cost = new Float(fieldCostRoad.getText());
                if (cost > 1.0f) {
                    areaCost[2] = cost;
                }
            }

            if (!fieldCostGrass.getText().isEmpty()
            &&   getState(UtilState.class).isNumeric(fieldCostGrass.getText()) ) {
                float cost = new Float(fieldCostGrass.getText());
                if (cost > 1.0f) {
                    areaCost[3] = cost;
                }
            }

            if (!fieldCostDoor.getText().isEmpty()
            &&   getState(UtilState.class).isNumeric(fieldCostDoor.getText()) ) {
                float cost = new Float(fieldCostDoor.getText());
                if (cost > 1.0f) {
                    areaCost[4] = cost;
                }
            }

            if (!fieldCostJump.getText().isEmpty()
            &&   getState(UtilState.class).isNumeric(fieldCostJump.getText()) ) {
                float cost = new Float(fieldCostJump.getText());
                if (cost > 1.0f) {
                    areaCost[5] = cost;
                }
            } 
            
            filter = new BetterDefaultQueryFilter(getIncludes(), getExcludes(), areaCost);
            listBoxFilters.getModel().remove((int) selected);
            listBoxFilters.getModel().add((int) selected, filter);
        }

        setCheckedFlags(listBoxFilters.getModel().get(selected).getIncludeFlags(), true);
        setCheckedFlags(listBoxFilters.getModel().get(selected).getExcludeFlags(), false);
    }
    
    /**
     * Updates listBoxFilters and resets any include or exclude checkBoxes.
     * 
     * If checkEditActive is checked: Updates by clearing the list then 
     * re-populating using the selected crowd query filters or if no crowd is 
     * selected, re-populates by creating all new BetterDefaultQueryFilter.
     * 
     * If checkEditActive is unchecked: Updates by clearing the list then 
     * re-populating using all new BetterDefaultQueryFilter.
     */
    private void updateFiltersList() {
        listBoxFilters.getModel().clear();
        
        if (checkEditActive.isChecked() && getSelectedCrowd() != null) {
            Crowd crowd = getSelectedCrowd();
            for (int i = 0; i < DT_CROWD_MAX_QUERY_FILTER_TYPE; i++) {
                listBoxFilters.getModel().add(i, (DefaultQueryFilter) crowd.getFilter(i));
            }
        } else {
            for (int i = 0; i < DT_CROWD_MAX_QUERY_FILTER_TYPE; i++) {
                listBoxFilters.getModel().add(new BetterDefaultQueryFilter());
            }
        }
        
        //Update include/exclude checkBoxes.
        updateIncludeExclude();
    }    
    
    /**
     * Updates all include/exclude query filter checkBoxes.
     */
    private void updateIncludeExclude() {
        Integer selection = listBoxFilters.getSelectionModel().getSelection();
        setCheckedFlags(listBoxFilters.getModel().get(selection).getIncludeFlags(), true);
        setCheckedFlags(listBoxFilters.getModel().get(selection).getExcludeFlags(), false);
    }
    
    /**
     * Sets checkBoxes for filter include/exclude.
     * 
     * @param filterFlags The filter flags to set.
     * @param includes If true, set include flags, false set exclude flags.
     */
    public void setCheckedFlags(int filterFlags, boolean includes) {
        
        if (includes) {
            this.checkIncludeNone.setChecked(filterFlags == 0);
            this.checkIncludeDisabled.setChecked(isBitSet(POLYFLAGS_DISABLED, filterFlags));
            this.checkIncludeDoor.setChecked(isBitSet(POLYFLAGS_DOOR, filterFlags));
            this.checkIncludeJump.setChecked(isBitSet(POLYFLAGS_JUMP, filterFlags));
            this.checkIncludeSwim.setChecked(isBitSet(POLYFLAGS_SWIM, filterFlags));
            this.checkIncludeWalk.setChecked(isBitSet(POLYFLAGS_WALK, filterFlags));
            this.checkIncludeAll.setChecked(isBitSet(POLYFLAGS_ALL, filterFlags));
        } else {
            this.checkExcludeNone.setChecked(filterFlags == 0);
            this.checkExcludeDisabled.setChecked(isBitSet(POLYFLAGS_DISABLED, filterFlags));
            this.checkExcludeDoor.setChecked(isBitSet(POLYFLAGS_DOOR, filterFlags));
            this.checkExcludeJump.setChecked(isBitSet(POLYFLAGS_JUMP, filterFlags));
            this.checkExcludeSwim.setChecked(isBitSet(POLYFLAGS_SWIM, filterFlags));
            this.checkExcludeWalk.setChecked(isBitSet(POLYFLAGS_WALK, filterFlags));
            this.checkExcludeAll.setChecked(isBitSet(POLYFLAGS_ALL, filterFlags));
        }
    }
    
    /**
     * Gets all checked query filter includes.
     * 
     * @return The checked includes. 
     */
    private int getIncludes() {
        
        int includes = 0;
        
        if (checkIncludeAll.isChecked()) {
            includes |= POLYFLAGS_ALL;
        } 
        
        if (checkIncludeDisabled.isChecked()) {
            includes |= POLYFLAGS_DISABLED;
        }
        
        if (checkIncludeWalk.isChecked()) {
            includes |= POLYFLAGS_WALK;
        }
        
        if (checkIncludeSwim.isChecked()) {
            includes |= POLYFLAGS_SWIM;
        }
        
        if (checkIncludeDoor.isChecked()) {
            includes |= POLYFLAGS_DOOR;
        }
        
        if (checkIncludeJump.isChecked()) {
            includes |= POLYFLAGS_JUMP;
        }
        return includes;
    }
    
    /**
     * Gets all checked query filter excludes.
     * 
     * @return The checked excludes. 
     */
    private int getExcludes() {
        
        int excludes = 0;
        
        if (checkExcludeAll.isChecked()) {
            excludes |= POLYFLAGS_ALL;
        }

        if (checkExcludeDisabled.isChecked()) {
            excludes |= POLYFLAGS_DISABLED;
        }
        
        if (checkExcludeWalk.isChecked()) {
            excludes |= POLYFLAGS_WALK;
        }
        
        if (checkExcludeSwim.isChecked()) {
            excludes |= POLYFLAGS_SWIM;
        }
        
        if (checkExcludeDoor.isChecked()) {
            excludes |= POLYFLAGS_DOOR;
        }
        
        if (checkExcludeJump.isChecked()) {
            excludes |= POLYFLAGS_JUMP;
        }
        
        return excludes;
    }
    
    /**
     * Checks whether a bit flag is set.
     * 
     * @param flag The flag to check for.
     * @param flags The flags to check for the supplied flag.
     * @return True if the supplied flag is set for the given flags.
     */
    private boolean isBitSet(int flag, int flags) {
        return (flags & flag) == flag;
    }
    
    /**
     * Displays a modal popup message.
     * 
     * @param txt The text for the popup.
     * @param width The maximum width for wrap. 
     */
    private void displayMessage(String txt, float width) {
        GuiGlobals.getInstance().getPopupState()
                    .showModalPopup(getState(UtilState.class)
                            .buildPopup(txt, width));
    }
    
    /**
     * Clears all OAP fields.
     */
    private void clearOAPFields() {
        fieldVelocityBias.setText("");
        fieldWeightDesVel.setText("");
        fieldWeightCurVel.setText("");
        fieldWeightSide.setText("");
        fieldWeightToi.setText("");
        fieldHorizTime.setText("");
        fieldGridSize.setText("");
        fieldAdaptiveRings.setText("");
        fieldAdaptiveDivs.setText("");
        fieldAdaptiveDepth.setText("");    
    }
    
    /**
     * Gets the query object for any selected Crowd.
     * 
     * @return The query object for a selected Crowd or null if the Crowd has 
     * not been selected in the Active Crowds list or if the Crowd object 
     * doesn't exist in the mapCrowds list.
     */
    public NavMeshQuery getQuery() {
        
        Crowd crowd = getSelectedCrowd();
        
        //Check to make sure a crowd has been selected.
        if (crowd == null || !mapCrowds.containsKey(crowd)) {
            return null;
        } 
        
        return mapCrowds.get(crowd);
    }    
    
    /**
     * Gets the currently selected crowd from the Active Crowds list as a Crowd 
     * object.
     * 
     * @return The Crowd for the selection.
     */    
    public Crowd getSelectedCrowd() {
        //Get the crowd from listBoxActiveCrowds.
        Integer selectedCrowd = listBoxActiveCrowds.getSelectionModel().getSelection();

        //Check to make sure a crowd has been selected.
        if (selectedCrowd == null) {
            return null;
        }
        
        return listBoxActiveCrowds.getModel().get(selectedCrowd);
    }
    
    /**
     * Forces a selection update for Crowd Versioned references.
     */
    public void updateVersRef() {
        Integer crowdSelection = listBoxActiveCrowds.getSelectionModel().getSelection();
        if (crowdSelection != null) {
            listBoxActiveCrowds.getSelectionModel().setSelection(-1);
            listBoxActiveCrowds.getSelectionModel().setSelection(crowdSelection);
        }
    }
    
    /**
     * Gets a crowd number from the CrowdManager by checking for the crowd 
     * object. Not really useful for anything other than logging.
     * 
     * @param crowd The crowd to lookup.
     * @return The crowd number for the given crowd.
     */
    public int getCrowdNumber(Crowd crowd) {
        CrowdManager crowdManager = getState(CrowdManagerAppState.class).getCrowdManager();
        int numberOfCrowds = crowdManager.getNumberOfCrowds();
        for (int i = 0; i < numberOfCrowds; i++) {
            if (crowdManager.getCrowd(i).equals(crowd)) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * The purpose here is to only update the the toggled Checkbox view, not 
     * the filter, in cases when they are just toggling all or none for filters. 
     * 
     * We don't use a reference listener because there are other Versioned 
     * References that set Checkboxes based on updating filters. These would 
     * fire off again every time those references updated.
     * 
     * Preferred use would be to addClickCommands, but unfortunately the lemur 
     * library is still outdated and uses type arguments that cause 
     * "unchecked generic array creation for varargs" warnings. 
     */    
    private class CheckboxListen extends DefaultMouseListener {
        @Override
        protected void click(MouseButtonEvent event, Spatial target, Spatial capture) {
            if (target.equals(checkIncludeAll) ){
                if (((Checkbox) target).isChecked()) {
                    setCheckedFlags(POLYFLAGS_ALL, true);
                } 
            } else if (target.equals(checkExcludeAll)) {
                if (((Checkbox) target).isChecked()) {
                    setCheckedFlags(POLYFLAGS_ALL, false);
                }                 
            } else if (target.equals(checkIncludeNone)) {
                if (((Checkbox) target).isChecked()) {
                    setCheckedFlags(0, true);
                }                
            } else if (target.equals(checkExcludeNone)) {
                if (((Checkbox) target).isChecked()) {
                    setCheckedFlags(0, false);
                }               
            }
        }
    }
}

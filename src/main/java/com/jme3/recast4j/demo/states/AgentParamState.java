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

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.recast4j.Detour.BetterDefaultQueryFilter;
import com.jme3.recast4j.Detour.Crowd.Crowd;
import com.jme3.recast4j.Detour.Crowd.MovementApplicationType;
import com.jme3.recast4j.Detour.DetourUtils;
import com.jme3.recast4j.demo.controls.CrowdBCC;
import com.jme3.recast4j.demo.controls.CrowdChangeControl;
import com.jme3.recast4j.demo.controls.CrowdDebugControl;
import com.jme3.recast4j.demo.layout.MigLayout;
import com.jme3.recast4j.demo.states.AgentGridState.Grid;
import com.jme3.recast4j.demo.states.AgentGridState.GridAgent;
import static com.jme3.recast4j.demo.states.CrowdBuilderState.DT_CROWD_MAX_OBSTAVOIDANCE_PARAMS;
import static com.jme3.recast4j.demo.states.CrowdBuilderState.DT_CROWD_MAX_QUERY_FILTER_TYPE;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Torus;
import com.simsilica.lemur.ActionButton;
import com.simsilica.lemur.CallMethodAction;
import com.simsilica.lemur.Checkbox;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.event.PopupState;
import java.util.Arrays;
import java.util.List;
import org.recast4j.detour.FindNearestPolyResult;
import org.recast4j.detour.NavMeshQuery;
import org.recast4j.detour.Result;
import org.recast4j.detour.crowd.CrowdAgent;
import org.recast4j.detour.crowd.CrowdAgentParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds the CrowdAgent parameter panel components.
 * 
 * @author Robert
 */
public class AgentParamState extends BaseAppState {

    private static final Logger LOG = LoggerFactory.getLogger(AgentParamState.class.getName());
    
    private Container contAgentParams;
    private TextField fieldColQueryRange;
    private TextField fieldHeight;
    private TextField fieldMaxAccel;
    private TextField fieldMaxSpeed;
    private TextField fieldPathOptimizeRange;
    private TextField fieldRadius;
    private TextField fieldSeparationWeight;
    private TextField fieldTargetX;
    private TextField fieldTargetY;
    private TextField fieldTargetZ;
    private ListBox<Integer> listBoxAvoidance;
    private ListBox<Integer> listBoxQuery;
    private Checkbox checkAvoid;
    private Checkbox checkSep;
    private Checkbox checkTopo;
    private Checkbox checkTurns;
    private Checkbox checkVis;
    private Checkbox checkRadius;
    private Checkbox checkHeight;
    private Checkbox checkDebugVisual;
    private Checkbox checkDebugVerbose;

    @Override
    protected void initialize(Application app) {
        
        //The top container for this gui.
        contAgentParams = new Container(new MigLayout("align center"));
        contAgentParams.setName("AgentParamState contAgentParams");
        contAgentParams.setAlpha(0, false);
        
        //Panel one.
        Container contPanel1 = new Container(new MigLayout("wrap 2", "[grow]"));
        contPanel1.setName("AgentParamState contPanel1");
        contPanel1.setAlpha(1, false);
        contAgentParams.addChild(contPanel1, "top, growx");

        //Begin the CrowdAgent parameters section.
        contPanel1.addChild(new Label("Crowd Agent Parameters"), "wrap");
        
        //The auto-generate radius checkbox.
        checkRadius = contPanel1.addChild(new Checkbox("Agent Radius"));
        fieldRadius = contPanel1.addChild(new TextField(""));
        fieldRadius.setSingleLine(true);
        fieldRadius.setPreferredWidth(50);
        
        //The auto-generate height checkbox.
        checkHeight = contPanel1.addChild(new Checkbox("Agent Height"));
        fieldHeight = contPanel1.addChild(new TextField(""));
        fieldHeight.setSingleLine(true);
        fieldHeight.setPreferredWidth(50);
        
        //The max acceleration field.
        contPanel1.addChild(new Label("Max Acceleration"));
        fieldMaxAccel = contPanel1.addChild(new TextField(""));
        fieldMaxAccel.setSingleLine(true);
        fieldMaxAccel.setPreferredWidth(50);
        
        //The max speed field.
        contPanel1.addChild(new Label("Max Speed"));
        fieldMaxSpeed = contPanel1.addChild(new TextField(""));
        fieldMaxSpeed.setSingleLine(true);
        fieldMaxSpeed.setPreferredWidth(50);
        
        //The collision query range.
        contPanel1.addChild(new Label("Collision Query Range"));
        fieldColQueryRange = contPanel1.addChild(new TextField(""));
        fieldColQueryRange.setSingleLine(true);
        fieldColQueryRange.setPreferredWidth(50);
        
        //The path optimization range.
        contPanel1.addChild(new Label("Path Optimize Range"));
        fieldPathOptimizeRange = contPanel1.addChild(new TextField(""));
        fieldPathOptimizeRange.setSingleLine(true);
        fieldPathOptimizeRange.setPreferredWidth(50);
        
        //The separation weight.
        contPanel1.addChild(new Label("Separation Weight"));
        fieldSeparationWeight = contPanel1.addChild(new TextField(""));
        fieldSeparationWeight.setSingleLine(true);
        fieldSeparationWeight.setPreferredWidth(50);
        
        
        //Obstacle avoidance.
        contPanel1.addChild(new Label("Avoidance Type"), "align center");
        //Query filter.
        contPanel1.addChild(new Label("Query Filter"), "align center");

        
        
        //Obstacle avoidance list
        listBoxAvoidance = contPanel1.addChild(new ListBox<>(), "align center");
        //Have to set this here since Crowd has package-private access the to 
        //the DT_CROWD_MAX_OBSTAVOIDANCE_PARAMS variable. Currently this is eight.
        for (int i = 0; i < DT_CROWD_MAX_OBSTAVOIDANCE_PARAMS; i++) {
            listBoxAvoidance.getModel().add(i);
        }
        
        listBoxAvoidance.getSelectionModel().setSelection(0);
        
        
        
        //Obstacle avoidance list
        listBoxQuery = contPanel1.addChild(new ListBox<>(), "align center");
        //Have to set this here since Crowd has package-private access the to 
        //the DT_CROWD_MAX_OBSTAVOIDANCE_PARAMS variable. Currently this is eight.
        for (int i = 0; i < DT_CROWD_MAX_QUERY_FILTER_TYPE; i++) {
            listBoxQuery.getModel().add(i);
        }
        
        listBoxQuery.getSelectionModel().setSelection(0);
        
        
        
        //Panel two.
        Container contPanel2 = new Container(new MigLayout("wrap 2", "[grow]"));
        contPanel2.setName("AgentParamState contPanel2");
        contPanel2.setAlpha(1, false);
        contAgentParams.addChild(contPanel2, "top, growy");
              
        //Update flags.
        contPanel2.addChild(new Label("Update Flags"));
        //Debug.
        contPanel2.addChild(new Label("Debug Movement"));

        //Update flags.
        checkTurns = contPanel2.addChild(new Checkbox("ANTICIPATE_TURNS"));    
        
        //Debug.
        checkDebugVisual = contPanel2.addChild(new Checkbox("Visual"));
        checkDebugVisual.getModel().setChecked(true);

        //Update flags.
        checkAvoid = contPanel2.addChild(new Checkbox("OBSTACLE_AVOIDANCE"));
        
        //Debug.
        checkDebugVerbose = contPanel2.addChild(new Checkbox("Verbose")); 

        //Update flags.
        checkTopo = contPanel2.addChild(new Checkbox("OPTIMIZE_TOPO"), "wrap");
        checkVis = contPanel2.addChild(new Checkbox("OPTIMIZE_VIS"), "wrap");
        checkSep = contPanel2.addChild(new Checkbox("SEPARATION"), "wrap");

        
        
        //Container that holds the start position components.
        Container contTarget = new Container(new MigLayout(null, "[grow]"));
        contTarget.setName("CrowdBuilderState contTarget");
        contTarget.setAlpha(0, false);
        contPanel2.addChild(contTarget, "pushy, span");
        
        //The start postion field.
        contTarget.addChild(new Label("Target Position"), "wrap"); 
        //X
        contTarget.addChild(new Label("X:"), "split 6");
        fieldTargetX = contTarget.addChild(new TextField("0.0"));
        fieldTargetX.setSingleLine(true);
        fieldTargetX.setPreferredWidth(75);
        //Y
        contTarget.addChild(new Label("Y:"));
        fieldTargetY = contTarget.addChild(new TextField("0.0"));
        fieldTargetY.setSingleLine(true);
        fieldTargetY.setPreferredWidth(75);
        //Z        
        contTarget.addChild(new Label("Z:"));
        fieldTargetZ = contTarget.addChild(new TextField("0.0"), "wrap");
        fieldTargetZ.setSingleLine(true);
        fieldTargetZ.setPreferredWidth(75);
        
        //Set the target for the crowd.
        contTarget.addChild(new ActionButton(new CallMethodAction("Set Target", this, "setTarget")));

        
        
        //Holds the Help and Setup buttons.
        Container contButton = new Container(new MigLayout(null, // Layout Constraints
                "[]push[][]")); // Column constraints [min][pref][max]
        contButton.setName("AgentParamState contButton");
        contButton.setAlpha(1, false);
        contPanel2.addChild(contButton, "growx, span");
        
        //Buttons.
        contButton.addChild(new ActionButton(new CallMethodAction("Help", this, "showHelp")));
        contButton.addChild(new ActionButton(new CallMethodAction("Add Grid Crowd", this, "addGridCrowd")));  

    }

    @Override
    protected void cleanup(Application app) {
        //The removal of the gui components is a by product of the removal of 
        //CrowdBuilderState where this gui lives.
    }

    /**
     * Called by AgentGridState(onEnable). CrowdBuilderState needs 
     * AgentGridState and AgentParamState to build its gui. This is the middle 
     * of the attachment chain. 
     * AgentGridState(onEnable)=>AgentParamState(onEnable)=>CrowdBuilderState(onEnable)
     */
    @Override
    protected void onEnable() {
        getStateManager().attach(new CrowdBuilderState());
    }

    /**
     * Called by CrowdBuilderState(onDisable) as part of a chain detachment of states. 
     * This is the middle of the detachment chain. Lemur cleanup for all states 
     * is done from CrowdBuilderState.
     * CrowdBuilderState(onDisable)=>AgentParamState(onDisable)=>AgentGridState(onDisable)
     */
    @Override
    protected void onDisable() {
        if (getStateManager().hasState(getState(AgentGridState.class))) {
            getStateManager().getState(AgentGridState.class).setEnabled(false);
        }
        getStateManager().detach(this);
    }
    
    @Override
    public void update(float tpf) {
        //TODO: implement behavior during runtime
    }
    
    /**
     * Explains the CrowdAgent parameters.
     */
    private void showHelp() {

        String[] msg = {
        "Agent Parameters - These are crowd specific parameters for the Grid you select in the ",
        "[ Agent Grid ] [ Active Grids ] window. If a Grid is selected but has not yet been added to a ",
        "Crowd, the settings will populate with defaults but have no effect. After the Grid has been ",
        "added to the crowd, they will populate with the Grids current settings. To update any selected ",
        "[ Active Grid ] simply make your changes and [ Add Grid Crowd ].",
        " ",
        "Agent Radius - The radius of the agent. When presented with an opening  they are to large to ",
        "enter, pathFinding will try to navigate around it. If checked, the given value will be used for the ",
        "radius of Crowd navigation. Left unchecked, and the radius assigned to the agent during the grid ",
        "creation process will be used instead if this is a Physics agent. For Direct agents, the value will ",
        "be taken from the world bounds of the spatial and is the smallest value in the x or z ",
        "direction / 2. [Limit: >= 0]",
        " ",
        "Agent Height - The height of the agent. Obstacles with a height less than this (value - radius) will ",
        "cause pathFinding to try and find a navigable path around the obstacle. If checked, the given ",
        "value will be used for the height of Crowd navigation. Left unchecked, and the height assigned ",
        "to the agent during the grid creation process will be used instead if this is a Physics agent. For ",
        "Direct agents, the value will be taken from the world bounds of the spatial and is the Y ",
        "value * 2. [Limit: > 0]",
        " ",
        "Max Acceleration - When an agent lags behind in the path, this is the maximum burst of speed ",
        "the agent will move at when trying to catch up to their expected position. [Limit: >= 0]",
        " ",
        "Max Speed - The maximum speed the agent will travel along the path when unobstructed. ",
        "[Limit: >= 0]",
        " ",
        "Collision Query Range - Defines how close a collision element must be before it's considered ",
        "for steering behaviors. [Limits: > 0]",
        " ",
        "Path Optimization Range - The path visibility optimization range. [Limit: > 0]",
        " ",
        "Separation Weight - How aggressive  the agent manager should be at avoiding collisions with ",
        "this agent. [Limit: >= 0]",
        " ",
        "Avoidance Type - This is the Obstacle Avoidance configuration to be applied to this agent. ",
        "Currently, the max number of avoidance types that can be configured for the Crowd is eight. ",
        "See [ Crowd ] [ Obstacle Avoidance Parameters ]. [Limits: 0 <= value < 8]",
        " ",
        "Query Filter - The path finding filter to be used by this grids agents. Currently, the max number ",
        "of Query filters that can be configured for the Crowd is sixteen. See [ Crowd ] [ Query Filters ]. ",
        "[Limits: 0 <= value < 16]",
        " ",
        "Update Flags - Flags that impact steering behavior.",
        " ",
        "* ANTICIPATE_TURNS",
        " ",
        "* OBSTACLE_AVOIDANCE",
        " ",
        "* OPTIMIZE_TOPO - Attempts to optimize the path using a local area search. Inaccurate ",
        "locomotion or dynamic obstacle avoidance can force the agent position significantly outside the ",
        "original corridor. Over time this can result in the formation of a non-optimal corridor. This ",
        "function will use a local area path search to try to re-optimize the corridor.",
        " ",
        "* OPTIMIZE_VIS - Attempts to optimize the path if the specified point is visible from the ",
        "current position. Inaccurate locomotion or dynamic obstacle avoidance can force the agent ",
        "position significantly outside the original corridor. Over time this can result in the formation of a ",
        "non-optimal corridor. Non-optimal paths can also form near the corners of tiles. This ",
        "is not suitable for long distance searches.",
        " ",
        "* SEPARATION",
        " ",
        "Debug Movement - Adds a debug control if [ Visual ], [ Verbose ], or both are checked and no ",
        "existing control is found when adding the agent to the crowd. To remove the control, deselect ",
        "both options and [ Add Agents Crowd ]. To update the visual or verbose state of the control, ",
        "select one or both and [ Add Agents Crowd ].",
        " " ,
        "* Visual - Display a visual representation of an agents MoveRequestState while inside the ",
        "selected crowd.",
        " ",
        "\tWhite   = Forming",
        "\tMagenta = Moving / MoveRequestState.DT_CROWDAGENT_TARGET_VALID",
        "\tCyan    = NoTarget / MoveRequestState.DT_CROWDAGENT_TARGET_NONE",
        "\tBlack   = none of the above",
        "",
        "* Verbose - Logs the information to the console.",
        " ",
        "Target Position - This is the target for the crowd you have selected in the [ Crowd ] panel. You ",
        "can set it manually or by hovering your mouse pointer over the desiredn target and selecting ",
        "the [ Shift ] key.",
        " ",
        "Add Agents Crowd - Add the active grid selected in the [ Add Grid ] [ Active Grids ] window to ",
        "the crowd selected in the [ Crowd ] [ Active Crowd ] window. It will also update any active grid ",
        "Agent Parameters as explained above.",
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
     * Adds an CrowdAgent to the specified crowd but does not set the target. 
     * Updates CrowdAgents if they already exist in the crowd.
     */
    private void addGridCrowd() {

        float radius;
        float height;
        float maxAccel;
        float maxSpeed;   
        float colQueryRange;
        float pathOptimizeRange;
        float separationWeight;
        int updateFlags;

        //Get the selected crowd from the CrowdBuilderState where all crowds live.
        Crowd crowd = getState(CrowdBuilderState.class).getSelectedCrowd();
        
        //Must select a crowd before anything else.
        if (crowd == null) {
            displayMessage("You must select a [ Active Crowd ] from the [ Crowd ] tab.", 0); 
            return;
        }        

        //Must select a agent grid.
        Integer gridSelection = getState(AgentGridState.class).getGridSelection();

        //Check to make sure a grid has been selected.
        if (gridSelection == null) {
            displayMessage("You must select a [ Active Grid ] from the [ Agent Grid ] tab.", 0);
            return;
        }

        //Get the grid name from the listBoxGrid gridSelection. gridSelection
        //returning non null guarentees success so no checks needed for null.
        Grid grid = getState(AgentGridState.class).getGrid(gridSelection);
        
        //The CrowdAgent radius. 
        if (checkRadius.isChecked()) {
            if (fieldRadius.getText().isEmpty()
            || !getState(UtilState.class).isNumeric(fieldRadius.getText())) {
                displayMessage("[ Agent Radius ] requires a valid float value.", 0);
                return;
            } 
        } 

        //The CrowdAgent height. If empty we will use auto generated settings 
        //gathered when the CrowdAgent was added to its grid in the Add Grid tab.
        if (checkHeight.isChecked()) {
            if (fieldHeight.getText().isEmpty()
            || !getState(UtilState.class).isNumeric(fieldHeight.getText())) {
                displayMessage("[ Agent Height ] requires a valid float value.", 0);
                return;
            } 
        }

        //The max acceleration settings.
        if (!getState(UtilState.class).isNumeric(fieldMaxAccel.getText()) 
        ||  fieldMaxAccel.getText().isEmpty()) {
            displayMessage("[ Max Acceleration ] requires a valid float value.", 0);
            return;
        } else {
            maxAccel = new Float(fieldMaxAccel.getText());
            //Stop negative input.
            if (maxAccel < 0.0f) {
                displayMessage("[ Max Acceleration ] requires a float value >= 0.0f.", 0);
                return;
            }
        }

        //The max speed settings.
        if (!getState(UtilState.class).isNumeric(fieldMaxSpeed.getText()) 
        ||  fieldMaxSpeed.getText().isEmpty()) {
            displayMessage("[ Max Speed ] requires a valid float value.", 0);
            return;
        } else {
            maxSpeed = new Float(fieldMaxSpeed.getText());
            //Stop negative input.
            if (maxSpeed < 0.0f) {
                displayMessage("[ Max Speed ] requires a float value >= 0.0f.", 0);
                return;
            }
        }

        //The collision query range.
        if (!getState(UtilState.class).isNumeric(fieldColQueryRange.getText()) 
        ||  fieldColQueryRange.getText().isEmpty()) {
            displayMessage("[ Collision Query Range ] requires a valid float value.", 0);
            return;
        } else {
            colQueryRange = new Float(fieldColQueryRange.getText());
            //Stop negative input.
            if (colQueryRange <= 0.0f) {
                displayMessage("[ Collision Query Range ] requires a float value > 0.0f.", 0);
                return;
            }
        }

        //The path optimize range.
        if (!getState(UtilState.class).isNumeric(fieldPathOptimizeRange.getText()) 
        ||  fieldPathOptimizeRange.getText().isEmpty()) {
            displayMessage("[ Path Optimize Range ] requires a valid float value.", 0);
            return;
        } else {
            pathOptimizeRange = new Float(fieldPathOptimizeRange.getText());
            //Stop negative input.
            if (pathOptimizeRange <= 0.0f) {
                displayMessage("[ Path Optimize Range ] requires a float value > 0.0f.", 0);
                return;
            }
        }

        //The separation weight settings.
        if (!getState(UtilState.class).isNumeric(fieldSeparationWeight.getText()) 
        ||  fieldSeparationWeight.getText().isEmpty()) {
            GuiGlobals.getInstance().getPopupState()
                    .showModalPopup(getState(UtilState.class)
                            .buildPopup("[ Separation Weight ] requires a valid float value.", 0));
            return;
        } else {
            separationWeight = new Float(fieldSeparationWeight.getText());
            //Stop negative input.
            if (separationWeight < 0.0f) {
                GuiGlobals.getInstance().getPopupState()
                        .showModalPopup(getState(UtilState.class)
                                .buildPopup("[ Separation Weight ] requires a float value >= 0.0f.", 0));
                return;
            }
        }

        updateFlags = 0;
        if (checkTurns.isChecked()) {
            updateFlags |= CrowdAgentParams.DT_CROWD_ANTICIPATE_TURNS;
        }

        if (checkAvoid.isChecked()) {
            updateFlags |= CrowdAgentParams.DT_CROWD_OBSTACLE_AVOIDANCE;
        }

        if (checkTopo.isChecked()) {
            updateFlags |= CrowdAgentParams.DT_CROWD_OPTIMIZE_TOPO;
        }

        if (checkVis.isChecked()) {
            updateFlags |= CrowdAgentParams.DT_CROWD_OPTIMIZE_VIS;
        }

        if (checkSep.isChecked()) {
            updateFlags |= CrowdAgentParams.DT_CROWD_SEPARATION;
        }      
        
        //Everything checks out so far so grab the selected list of agents for 
        //the grid.
        List<GridAgent> listGridAgents = grid.getListGridAgent();
                
        //If checked, we use the fieldRadius for the radius.
        if (checkRadius.isChecked()) {
            radius = new Float(fieldRadius.getText());
            //Stop negative input.
            if (radius < 0.0f) {
                displayMessage("[ Agent Radius ] requires a float value >= 0.", 0);
                return;
            }
        } else {
            // All gridAgents are the same so grab first one.
            Node spatialForAgent = listGridAgents.get(0).getSpatialForAgent();
            //Calculate based off BCC.
            if (spatialForAgent.getControl(CrowdBCC.class) != null) {
                CrowdBCC control = spatialForAgent.getControl(CrowdBCC.class);
                radius = control.getRadius();
            } else {
                //Auto calculate based on bounds.
                BoundingBox bounds = (BoundingBox) spatialForAgent.getChild("spatial").getWorldBound();
                float x = bounds.getXExtent();
                float z = bounds.getZExtent();

                float xz = x < z ? x:z;
                radius = xz/2;
            }
            
        }

        //If checked, we use the fieldHeight for height.
        if (checkHeight.isChecked()) {
            height = new Float(fieldHeight.getText());
            //Stop negative input.
            if (height <= 0.0f) {
                displayMessage("[ Agent Height ] requires a float value > 0.", 0);
                return;
            }
        } else {
            // All gridAgents are the same so grab first one.
            Node spatialForAgent = listGridAgents.get(0).getSpatialForAgent();
            //Calculates based off BCC.
            if (spatialForAgent.getControl(CrowdBCC.class) != null) {
                CrowdBCC control = spatialForAgent.getControl(CrowdBCC.class);
                height = control.getHeight();
            } else {
                //Auto calculate based on bounds. All gridAgents are the same so 
                //grab first one.
                BoundingBox bounds = (BoundingBox) spatialForAgent.getChild("spatial").getWorldBound();
                float y = bounds.getYExtent();
                height = y*2;
            }
        }     
        
        LOG.info("<===== BEGIN AgentParamState addAgentCrowd =====>");        
        LOG.info("Crowd                 [{}]", getState(CrowdBuilderState.class).getCrowdNumber(crowd));
        LOG.info("Active Agents         [{}]", crowd.getActiveAgents().size());

        //Build the params object.
        CrowdAgentParams ap         = new CrowdAgentParams();
        ap.radius                   = radius;
        ap.height                   = height;
        ap.maxAcceleration          = maxAccel;
        ap.maxSpeed                 = maxSpeed;
        ap.collisionQueryRange      = colQueryRange;
        ap.pathOptimizationRange    = pathOptimizeRange;
        ap.separationWeight         = separationWeight;
        ap.updateFlags              = updateFlags;
        ap.obstacleAvoidanceType    = listBoxAvoidance.getSelectionModel().getSelection(); 
        ap.queryFilterType          = listBoxQuery.getSelectionModel().getSelection();
        
        LOG.info("radius                [{}]", ap.radius);
        LOG.info("height                [{}]", ap.height);
        LOG.info("maxAcceleration       [{}]", ap.maxAcceleration);
        LOG.info("maxSpeed              [{}]", ap.maxSpeed);
        LOG.info("colQueryRange         [{}]", ap.collisionQueryRange);
        LOG.info("pathOptimizationRange [{}]", ap.pathOptimizationRange);
        LOG.info("separationWeight      [{}]", ap.separationWeight);
        LOG.info("obstacleAvoidanceType [{}]", ap.obstacleAvoidanceType);
        LOG.info("queryFilterType       [{}]", ap.queryFilterType);
        LOG.info("updateFlags           [{}]", ap.updateFlags);
        LOG.info("Agents Grid           [{}]", listGridAgents);
        
        /**
         * Update an existing CrowdAgent or add a new CrowdAgent to the crowd. 
         * This loop checks listGridAgents against the active CrowdAgents for 
         * the selected crowd. If the crowd contains the CrowdAgent, update the 
         * parameters rather than creating new ones. If we update, we don't 
         * create a new CrowdAgent. 
         */        
        for (GridAgent ga: listGridAgents) {
            
            if (crowd.getActiveAgents().contains(ga.getCrowdAgent())) {
                //Update the parameters for the CrowdAgent.
                crowd.updateAgentParameters(ga.getCrowdAgent().idx, ap);
                
                //check the CrowdDebugControl if it exists.
                checkDebugMove(ga.getSpatialForAgent(), crowd, ga.getCrowdAgent());
                
                LOG.info("<========== Update CAP CrowdAgent [{}] ==========>", ga.getSpatialForAgent().getName());
                
            } else {   
                
                //Sanity check intentions of crowd use.
                if (crowd.getApplicationType() == MovementApplicationType.BETTER_CHARACTER_CONTROL
                &&  ga.getSpatialForAgent().getControl(BetterCharacterControl.class) == null) {
                    displayMessage("Selected crowd uses PHYSICS movement. Select a different crowd or grid type and try again.", 0);
                    return;
                } else if (crowd.getApplicationType() == MovementApplicationType.DIRECT
                        && ga.getSpatialForAgent().getControl(BetterCharacterControl.class) != null) {
                    displayMessage("Selected crowd uses DIRECT movement. Select a different crowd or grid type and try again.", 0);
                    return;
                } else if (crowd.getApplicationType() == MovementApplicationType.CUSTOM) {
                    displayMessage("Selected crowd uses CUSTOM movement and is beyond the scope of this demo.", 0);
                    return;
                } else if (crowd.getApplicationType() == MovementApplicationType.NONE) {
                    displayMessage("Selected crowd uses NONE movement and is beyond the scope of this demo.", 0);
                    return;
                }
                
                //Stop overcrowding of the selected crowd.
                if (listGridAgents.size() > crowd.getAgentCount()) {
                    displayMessage("Agent grid size of [" + listGridAgents.size() 
                            + "] excedes the crowd size ["
                            + crowd.getAgentCount() + "].", 0);
                    return;
                //Grid size to small if active agents + current GridSize - current GridAgent to small.
                } else if ((listGridAgents.size() + crowd.getActiveAgents().size() - listGridAgents.lastIndexOf(ga)) > crowd.getAgentCount()) {
                    displayMessage("Agent grid size of [" + listGridAgents.size() 
                            + "] plus active agents of [" 
                            + crowd.getActiveAgents().size() 
                            + "] excedes the crowd size ["
                            + crowd.getAgentCount() + "].", 0);
                    return;
                }
                //Add CrowdAgents to the crowd.
                CrowdAgent createAgent = crowd.createAgent(ga.getSpatialForAgent().getWorldTranslation(), ap);
                crowd.setSpatialForAgent(createAgent, ga.getSpatialForAgent());
                
                //Set the CrowdAgent for the GridAgent.
                ga.setCrowdAgent(createAgent);
                
                //No CrowdChangeControl then add one.
                if (ga.getSpatialForAgent().getControl(CrowdChangeControl.class) == null) {
                    ga.getSpatialForAgent().addControl(new CrowdChangeControl(crowd, createAgent));
                    LOG.info("Adding CrowdChangeControl to [{}].", ga.getSpatialForAgent().getName());
                } else {
                    //Existing control so update.
                    LOG.info("Updating CrowdChangeControl to [{}].", ga.getSpatialForAgent().getName());
                    ga.getSpatialForAgent().getControl(CrowdChangeControl.class).setCrowd(crowd, createAgent);
                }
                
                //Check for CrowdDebugControl.
                checkDebugMove(ga.getSpatialForAgent(), crowd, createAgent);

                //Force versionedRef update so the Active Grid list will populate.
                getState(CrowdBuilderState.class).updateVersRef();
                
                LOG.info("<========== New CrowdAgent [{}] ==========>", ga.getSpatialForAgent().getName());
            }
            
            //Refresh Agent Parameters panel.
            this.updateParams();
        }

        LOG.info("Crowd                 [{}]", getState(CrowdBuilderState.class).getCrowdNumber(crowd));
        LOG.info("Active Agents         [{}]", crowd.getActiveAgents().size());
        LOG.info("<===== END AgentParamState addAgentCrowd =====>");
    }
    
    /**
     * Adds a debug control if checkDebugVisual, checkDebugVerbose, or both are 
     * checked and no existing control is found.
     * 
     * Removes the control if one exists and both checkDebugVisual and 
     * checkDebugVerbose are not checked. 
     * 
     * The control will update the visual or verbose state if either or both 
     * checkDebugVisual or checkDebugVerbose are selected and the control exists.
     */
    private void checkDebugMove(Node spatialForAgent, Crowd crowd, CrowdAgent crowdAgent) {
        if (checkDebugVisual.isChecked() || checkDebugVerbose.isChecked()) {
            if (spatialForAgent.getControl(CrowdDebugControl.class) == null) {
                LOG.info("Adding CrowdDebugControl to [{}].", spatialForAgent.getName());
                //Create the geometry for the halo
                Torus halo = new Torus(16, 16, 0.1f, 0.3f);
                Geometry haloGeom = new Geometry("halo", halo);
                Material haloMat = new Material(getApplication().getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                haloGeom.setMaterial(haloMat);
                haloGeom.setLocalTranslation(0, crowdAgent.params.height + 0.5f, 0);
                Quaternion pitch90 = new Quaternion();
                pitch90.fromAngleAxis(FastMath.PI/2, new Vector3f(1,0,0));
                haloGeom.setLocalRotation(pitch90);
                //Add the control and set its visual and verbose state.
                CrowdDebugControl dmc = new CrowdDebugControl(crowd, crowdAgent, haloGeom);
                dmc.setVisual(checkDebugVisual.isChecked()); 
                dmc.setVerbose(checkDebugVerbose.isChecked());                    
                spatialForAgent.addControl(dmc);
            } else {
                //Set the control to display selected option.
                LOG.info("Updating CrowdDebugControl [{}].", spatialForAgent.getName());
                spatialForAgent.getControl(CrowdDebugControl.class).setVisual(checkDebugVisual.isChecked()); 
                spatialForAgent.getControl(CrowdDebugControl.class).setVerbose(checkDebugVerbose.isChecked());
                spatialForAgent.getControl(CrowdDebugControl.class).setCrowd(crowd);
                spatialForAgent.getControl(CrowdDebugControl.class).setAgent(crowdAgent);
            }
        } else {
            //Nothing checked for debug, remove control if exists.
            LOG.info("Removing CrowdDebugControl [{}].", spatialForAgent.getName());
            if (spatialForAgent.getControl(CrowdDebugControl.class) != null) {
                spatialForAgent.removeControl(CrowdDebugControl.class);
            } 
        }
    }

    /**
     * Set the target for the selected crowd.
     */
    private void setTarget() {

        //Get the selected crowd from the CrowdBuilderState where all crowds live.
        Crowd crowd = getState(CrowdBuilderState.class).getSelectedCrowd();
        
        //Check to make sure a crowd has been selected.
        if (crowd == null) {
            displayMessage("Select an [ Active Crowd ] from the [ Crowd ] tab to set the target for.", 0); 
            return;
        } 
        
        //The target position of the grid. Sanity check.
        if (!getState(UtilState.class).isNumeric(fieldTargetX.getText()) || fieldTargetX.getText().isEmpty() 
        ||  !getState(UtilState.class).isNumeric(fieldTargetY.getText()) || fieldTargetY.getText().isEmpty() 
        ||  !getState(UtilState.class).isNumeric(fieldTargetZ.getText()) || fieldTargetZ.getText().isEmpty()) {
            displayMessage("[ Start Position ] requires a valid float value.", 0);
        } else {
            Float x = new Float(fieldTargetX.getText());
            Float y = new Float(fieldTargetY.getText());
            Float z = new Float(fieldTargetZ.getText());
            Vector3f target = new Vector3f(x, y, z);

            //@TODO: Remove all the findNearestPoly stuff.
            
            //Get the query extent for this crowd.
            float[] ext = crowd.getQueryExtents();
            
            //Get the query object.
            NavMeshQuery query = getState(CrowdBuilderState.class).getQuery();
        
            if (query == null) {
               displayMessage("Query object not found. Select an "
                       + "[ Active Crowd ] from the [ Crowd ] tab first.", 0);  
               return;
            }

            LOG.info("<========== BEGIN AgentParamState setTarget ==========>");
            LOG.info("queryExt              {}", ext);
            LOG.info("setTarget             {}", target);
            
            //Locate the nearest poly ref/pos.
            Result<FindNearestPolyResult> nearest = query.findNearestPoly(DetourUtils.toFloatArray(target), ext, new BetterDefaultQueryFilter());
            LOG.info("nearestPos            [{}] nearestRef {}", nearest.result.getNearestPos(), nearest.result.getNearestRef());
            if (!nearest.status.isSuccess() || nearest.result.getNearestRef() == 0) {
                LOG.info("getNearestRef() can't be 0. ref [{}]", nearest.result.getNearestRef());
            } else {
                //Sets all CrowdAgent targets at same time.
                boolean requestMoveToTarget = crowd.requestMoveToTarget(target);//DetourUtils.createVector3f(nearest.result.getNearestPos()), nearest.result.getNearestRef());
                LOG.info("requestMoveToTarget   [{}]", requestMoveToTarget);
            }
            
            LOG.info("<========== END AgentParamState setTarget ==========>");
        }
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
     * Toggles the checkBoxes on/off for crowd agent parameters. 
     * 
     * @param updateFlags The update flags for the CrowdAgentParameter object.
     */
    private void checkUpdateFlags(int updateFlags) {
        this.checkTurns.setChecked(isBitSet(CrowdAgentParams.DT_CROWD_ANTICIPATE_TURNS, updateFlags));
        
        this.checkAvoid.setChecked(isBitSet(CrowdAgentParams.DT_CROWD_OBSTACLE_AVOIDANCE, updateFlags));

        this.checkTopo.setChecked(isBitSet(CrowdAgentParams.DT_CROWD_OPTIMIZE_TOPO, updateFlags));

        this.checkVis.setChecked(isBitSet(CrowdAgentParams.DT_CROWD_OPTIMIZE_VIS, updateFlags));

        this.checkSep.setChecked(isBitSet(CrowdAgentParams.DT_CROWD_SEPARATION, updateFlags));
    }
    
    /**
     * Sets or clears the default parameters. 
     * 
     * @param reset True will clear all fields, false will set default settings.
     */
    private void loadDefaultParams(boolean reset) {
        this.checkRadius.setChecked(false);
        this.fieldRadius.setText(reset ? "" : "0.6");
        this.checkHeight.setChecked(false);
        this.fieldHeight.setText(reset ? "" : "2.0");
        this.fieldMaxAccel.setText(reset ? "" : "8.0");
        this.fieldMaxSpeed.setText(reset ? "" : "3.5");
        this.fieldColQueryRange.setText(reset ? "" : "12.0");
        this.fieldPathOptimizeRange.setText(reset ? "" : "30.0");
        this.fieldSeparationWeight.setText(reset ? "" : "2.0");
        this.listBoxAvoidance.getSelectionModel().setSelection(0);
        this.listBoxQuery.getSelectionModel().setSelection(0);
        this.checkDebugVisual.setChecked(true);
        this.checkTurns.setChecked(false);
        this.checkAvoid.setChecked(false);
        this.checkTopo.setChecked(false);
        this.checkVis.setChecked(false);
        this.checkSep.setChecked(false);
    }
    
    /**
     * Updates the Agent Parameters panel fields and selections based off the 
     * current selected grid or if no grids selected will clear all settings.
     */
    public void updateParams() {
        Integer gridSelection = getState(AgentGridState.class).getGridSelection();
        
        //null selection so clear all fields.
        if (gridSelection == null) {
            loadDefaultParams(true);
        } else {
            //Get the selected grid.
            Grid grid = getState(AgentGridState.class).getGrid(gridSelection);
            //We only need to get the first agent of a grid to know all agent 
            //parameters.
            CrowdAgent crowdAgent = grid.getListGridAgent().get(0).getCrowdAgent();
            //null CrowdAgent means the Grid exists but has not been added to a
            //crowd yet so set default params.
            if (crowdAgent == null) {
                loadDefaultParams(false);
            } else {
                CrowdAgentParams params = crowdAgent.params;
                this.checkRadius.setChecked(true);
                this.fieldRadius.setText("" + params.radius);
                this.checkHeight.setChecked(true);
                this.fieldHeight.setText("" + params.height);
                this.fieldMaxAccel.setText("" + params.maxAcceleration);
                this.fieldMaxSpeed.setText("" + params.maxSpeed);
                this.fieldColQueryRange.setText("" + params.collisionQueryRange);
                this.fieldPathOptimizeRange.setText("" + params.pathOptimizationRange);
                this.fieldSeparationWeight.setText("" + params.separationWeight);
                this.listBoxAvoidance.getSelectionModel().setSelection(params.obstacleAvoidanceType);
                this.listBoxQuery.getSelectionModel().setSelection(params.queryFilterType);
                //Clear any selections for debug.
                this.checkDebugVisual.setChecked(false);
                this.checkDebugVerbose.setChecked(false);
                //Look for the CrowdDebugControl and if found set the appropriate
                //checkBoxes.
                if (grid.getListGridAgent().get(0).getSpatialForAgent().getControl(CrowdDebugControl.class) != null) {
                    CrowdDebugControl control = grid.getListGridAgent().get(0).getSpatialForAgent().getControl(CrowdDebugControl.class);
                    this.checkDebugVisual.setChecked(control.isVisual());
                    this.checkDebugVerbose.setChecked(control.isVerbose());
                } 

                //Look for any flags to turn off/on.
                checkUpdateFlags(params.updateFlags);
            }
        }
    }
    
    /**
     * @return The contAgentParams.
     */
    public Container getContAgentParams() {
        return contAgentParams;
    }
    
    /**
     * Sets the target by converting vector3f to string.
     * 
     * @param target The requested target value to set.
     */
    public void setFieldTargetXYZ(Vector3f target) {
        String x = "" + target.x;
        String y = "" + target.y;
        String z = "" + target.z;
        this.fieldTargetX.setText(x);
        this.fieldTargetY.setText(y);
        this.fieldTargetZ.setText(z);
    }   
    
}

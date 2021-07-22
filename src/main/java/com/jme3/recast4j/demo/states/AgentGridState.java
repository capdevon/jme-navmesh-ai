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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.recast4j.detour.crowd.CrowdAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.bounding.BoundingBox;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.math.Vector3f;
import com.jme3.recast4j.Detour.Crowd.Impl.CrowdManagerAppState;
import com.jme3.recast4j.demo.controls.CrowdBCC;
import com.jme3.recast4j.demo.controls.CrowdChangeControl;
import com.jme3.recast4j.demo.controls.CrowdDebugControl;
import com.jme3.recast4j.demo.layout.MigLayout;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.simsilica.lemur.ActionButton;
import com.simsilica.lemur.CallMethodAction;
import com.simsilica.lemur.Checkbox;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.event.PopupState;
import com.simsilica.lemur.list.DefaultCellRenderer;
import com.simsilica.lemur.style.ElementId;

/**
 * Holds the GridAgent panel components.
 * 
 * @author Robert
 */
public class AgentGridState extends BaseAppState {

    private static final Logger LOG = LoggerFactory.getLogger(AgentGridState.class.getName());

    private Container contAgentGrid;
    private Checkbox checkHeight;
    private Checkbox checkPhysics;
    private Checkbox checkRadius;
    private Checkbox checkWeight;
    private ListBox<String> listBoxAgent;
    private ListBox<Grid> listBoxGrid;
    private ListBox<Integer> listBoxSize;
    private TextField fieldDistance;
    private TextField fieldGridName;
    private TextField fieldHeight;
    private TextField fieldWeight;
    private TextField fieldPosX;
    private TextField fieldPosY;
    private TextField fieldPosZ;
    private TextField fieldRadius;
    private boolean newGrid;
    private boolean checkGrids;
    private float maxPopupSize;
    private VersionedReference<Set<Integer>> gridSelectRef; 
    private VersionedReference<List<Grid>> gridModelRef;
    
    @Override
    protected void initialize(Application app) {
        
        //Set the maximum size a popup can be.
        maxPopupSize = 500.0f;
        
        //The top container for this gui.
        contAgentGrid = new Container(new MigLayout("align center"));
        contAgentGrid.setName("AgentGridState contBuildGridGui");
        contAgentGrid.setAlpha(0, false);

        //The container that holds GridAgent stat selections.
        Container contStats = new Container(new MigLayout("wrap", "[grow, fill]"));
        contStats.setName("AgentGridState contStats");
        contStats.setAlpha(0, false);
        contAgentGrid.addChild(contStats, "split 2, growy");

        //The grid name field. 
        contStats.addChild(new Label("Grid Name"), "split 2");
        fieldGridName = contStats.addChild(new TextField("Grid"), "growx");
        fieldGridName.setSingleLine(true);
        
        //The use physics checkbox.
        checkPhysics = contStats.addChild(new Checkbox("Use Physics"));
        checkPhysics.getModel().setChecked(true);

        //The Radius checkbox.
        checkRadius = contStats.addChild(new Checkbox("Agent Radius"), "split 2, growx");     
        fieldRadius = contStats.addChild(new TextField("0.6"));
        fieldRadius.setSingleLine(true);
        fieldRadius.setPreferredWidth(50);
                
        //The Height checkbox.
        checkHeight = contStats.addChild(new Checkbox("Agent Height"), "split 2, growx");
        fieldHeight = contStats.addChild(new TextField("2.0"));
        fieldHeight.setSingleLine(true);
        fieldHeight.setPreferredWidth(50);
        
        //The weight checkbox.
        checkWeight = contStats.addChild(new Checkbox("Agent Weight"), "split 2, growx");
        fieldWeight = contStats.addChild(new TextField("1.0"));
        fieldWeight.setSingleLine(true);
        fieldWeight.setPreferredWidth(50);
        
        //The grid separation field.
        contStats.addChild(new Label("Agent Separation"), "split 2, growx");
        fieldDistance = contStats.addChild(new TextField("1.0"));
        fieldDistance.setSingleLine(true);
        fieldDistance.setPreferredWidth(50);
        


        //Container that holds the listbox for the models/agents.
        Container contAgent = new Container(new MigLayout("wrap", "[grow, fill]"));
        contAgent.setName("AgentGridState contAgent");
        contAgent.setAlpha(0, false);
        contAgentGrid.addChild(contAgent, "wrap, wmin 100");
        
        //The agentPath listbox. Add new model name here. Add the asset path 
        //to addGrid # MODELS.
        contAgent.addChild(new Label("Agent"));
        //models in butAddAgentGrid listener under the #Model Section.
        listBoxAgent = contAgent.addChild(new ListBox<>());
        listBoxAgent.getModel().add("Jamie");
        listBoxAgent.getModel().add("Fish");
        listBoxAgent.setVisibleItems(7);
        listBoxAgent.getSelectionModel().setSelection(0);
        
        
        
        //Container for the grid size listbox.
        Container contGridSize = new Container(new MigLayout("wrap"));
        contGridSize.setName("AgentGridState contGridSize");
        contGridSize.setAlpha(0, false);
        contAgentGrid.addChild(contGridSize, "split 2, growy");        
        
        //The grid size listbox. 
        contGridSize.addChild(new Label("Grid Size"));
        listBoxSize = contGridSize.addChild(new ListBox<>());
        listBoxSize.setVisibleItems(7);
        int size = 1;
        for (int i = 0; i < 15; i++) {
            listBoxSize.getModel().add(size);
            size++;
        }
        listBoxSize.getSelectionModel().setSelection(0);        

        
        
        //Container that holds the active grid components.
        Container contGrid = new Container(new MigLayout("wrap", "[grow]"));
        contGrid.setName("AgentGridState contGrid");
        contGrid.setAlpha(0, false);
        contAgentGrid.addChild(contGrid, "wrap, growx, growy");
        
        //The active grids listbox. Displays the grid name, grid size, grid type
        //when a grid is selected.
        contGrid.addChild(new Label("Grids"));
        listBoxGrid = contGrid.addChild(new ListBox<>(), "growx");
        gridSelectRef = listBoxGrid.getSelectionModel().createReference();
        gridModelRef = listBoxGrid.getModel().createReference();
        listBoxGrid.setCellRenderer(new DefaultCellRenderer<Grid>(new ElementId("list.item"), null) {
            @Override
            protected String valueToString(Grid grid ) {
                String txt = grid.getGridName() + " [ " + grid.getListGridAgent().size() + " ] ";
                txt += grid.getListGridAgent().get(0).getSpatialForAgent().getControl(BetterCharacterControl.class) != null ? "[ PHYSICS ]": "[ DIRECT ]";
                return txt;
            }
        });
        //Use the method call to set the checkGrids boolean to true.
        contGrid.addChild(new ActionButton(new CallMethodAction("Remove Grid", this, "removeGrid")));
                        


        //Holds all the underlying components for this gui.
        Container contStartPos = new Container(new MigLayout(null)); 
        contStartPos.setName("AgentGridState contStartPos");
        contStartPos.setAlpha(0, false);
        contAgentGrid.addChild(contStartPos, "wrap");
        
        //The start postion field.
        contStartPos.addChild(new Label("Start Position"), "wrap"); 
        //X
        contStartPos.addChild(new Label("X:"), "split 6");
        fieldPosX = contStartPos.addChild(new TextField("0.0"));
        fieldPosX.setSingleLine(true);
        fieldPosX.setPreferredWidth(50);
        //Y
        contStartPos.addChild(new Label("Y:"));
        fieldPosY = contStartPos.addChild(new TextField("0.0"));
        fieldPosY.setSingleLine(true);
        fieldPosY.setPreferredWidth(50);
        //Z        
        contStartPos.addChild(new Label("Z:"));
        fieldPosZ = contStartPos.addChild(new TextField("0.0"));
        fieldPosZ.setSingleLine(true);
        fieldPosZ.setPreferredWidth(50);
        
        
        
        //Holds the Add Agent and Legend buttons.
        Container contButton = new Container(new MigLayout(null, // Layout Constraints
                "[]push[][]")); // Column constraints [min][pref][max]
        contButton.setName("AgentGridState contButton");
        contButton.setAlpha(1, false);
        contAgentGrid.addChild(contButton, "growx");
        
        //Buttons.
        contButton.addChild(new ActionButton(new CallMethodAction("Help", this, "showHelp")));
        contButton.addChild(new ActionButton(new CallMethodAction("Add Agent Grid", this, "addGrid")));
        
    }

    /**
     * Removing this state from StateManager will clear all gridAgents from the 
     * physics space and gui node. The removal of the gui components is a by
     * product of the removal of CrowdBuilderState where this gui lives. 
     * CrowdBuilderState will have already removed crowds so only need Grid 
     * related removal.
     * @param app
     */
    @Override
    protected void cleanup(Application app) {
        Iterator<Grid> it = getGrids().iterator(); 
        while (it.hasNext()) {
            Grid grid = it.next();
            List<GridAgent> listGridAgent = grid.getListGridAgent();
            for (GridAgent gridAgent: listGridAgent) {
                //Convolouted crap just to get a PhysicsRigidBody from BCC.
                if (gridAgent.getSpatialForAgent().getControl(BetterCharacterControl.class) != null) {
                    PhysicsRigidBody prb = gridAgent.getSpatialForAgent().getControl(CrowdBCC.class).getPhysicsRigidBody();
                    if (getStateManager().getState(BulletAppState.class).getPhysicsSpace().getRigidBodyList().contains(prb)) {
                        LOG.info("Removing BCC from [{}]", gridAgent.getSpatialForAgent().getName());
                        getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(gridAgent.getSpatialForAgent());
                    }
                }

                //Crowd specific control so remove.
                if (gridAgent.getSpatialForAgent().getControl(CrowdDebugControl.class) != null) {
                    LOG.info("Removing CrowdDebugControl from [{}]", gridAgent.getSpatialForAgent().getName());
                    gridAgent.getSpatialForAgent().removeControl(CrowdDebugControl.class);
                }

                //Crowd specific control so remove.
                if (gridAgent.getSpatialForAgent().getControl(CrowdChangeControl.class) != null) {
                    LOG.info("Removing CrowdChangeControl from [{}]", gridAgent.getSpatialForAgent().getName());
                    gridAgent.getSpatialForAgent().removeControl(CrowdChangeControl.class);
                }
                ((SimpleApplication) getApplication()).getRootNode().detachChild(gridAgent.getSpatialForAgent());
            }
            it.remove();
        }

    }

    /**
     * Called by the DemoApplication F1 button ActionListener. CrowdBuilderState needs 
     * AgentGridState and AgentParamState to build its gui. This is the start of 
     * the attachment chain. 
     * AgentGridState(onEnable)=>AgentParamState(onEnable)=>CrowdBuilderState(onEnable)
     */
    @Override
    protected void onEnable() {
        getStateManager().attach(new AgentParamState());
    }

    /**
     * Called by AgentParamState(onDisable) as part of a chain detachment of 
     * states. This is the end of the detachment chain. Lemur cleanup for all 
     * states is done from CrowdBuilderState.
     * CrowdBuilderState(onDisable)=>AgentParamState(onDisable)=>AgentGridState(onDisable)
     */
    @Override
    protected void onDisable() {
        getStateManager().detach(this);
    }
    
    @Override
    public void update(float tpf) {

        //Look for incactive grid to activate. Loads the gridAgents into the physics
        //space and attaches them to the rootNode.
        if (newGrid) {
            List<Grid> grids = getGrids();
            for (Grid grid: grids) {
                //All inactive grids need activation.
                if (!grid.isActiveGrid()) {
                    List<GridAgent> listGridAgent = grid.getListGridAgent();
                    for (GridAgent gridAgent: listGridAgent) {
                        //Physics GridAgent so add to physics space.
                        if (gridAgent.getSpatialForAgent().getControl(BetterCharacterControl.class) != null) {
                            getStateManager().getState(BulletAppState.class).getPhysicsSpace().add(gridAgent.getSpatialForAgent());
                        }
                        ((SimpleApplication) getApplication()).getRootNode().attachChild(gridAgent.getSpatialForAgent());
                    }
                    //Stop the the newGrid check from adding this again.
                    grid.setActiveGrid(true);
                }
            }
            //All grids are activated so stop looking.
            newGrid = false;
        }

        //Look for grids to remove. Removes the gridAgents from the root node and 
        //physics space. Use iterator to avoid ConcurrentModificationException.
        if (checkGrids) {
            Iterator<Grid> it = getGrids().iterator(); 
            while (it.hasNext()) {
                Grid grid = it.next();
                //Flaged for removal.
                if (grid.isRemoveGrid()) {
                    List<GridAgent> listGridAgent = grid.getListGridAgent();
                    for (GridAgent ga: listGridAgent) {
                        //Convolouted crap just to get a PhysicsRigidBody from BCC.
                        if (ga.getSpatialForAgent().getControl(BetterCharacterControl.class) != null) {
                            //Look for the physicsRigidBody for this listGridAgent.
                            PhysicsRigidBody prb = ga.getSpatialForAgent().getControl(CrowdBCC.class).getPhysicsRigidBody();

                            if (getStateManager().getState(BulletAppState.class).getPhysicsSpace().getRigidBodyList().contains(prb)) {
                                LOG.info("Removing BCC from [{}]", ga.getSpatialForAgent().getName());
                                getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(ga.getSpatialForAgent());
                            }                            
                        }

                        //Remove CrowdDebugControl if exits.
                        if (ga.getSpatialForAgent().getControl(CrowdDebugControl.class) != null) {
                            LOG.info("Removing CrowdDebugControl from [{}]", ga.getSpatialForAgent().getName());
                            ga.getSpatialForAgent().removeControl(CrowdDebugControl.class);
                        }

                        //Remove CrowdChangeControl if exists.
                        if (ga.getSpatialForAgent().getControl(CrowdChangeControl.class) != null) {
                            LOG.info("Removing CrowdChangeControl from [{}]", ga.getSpatialForAgent().getName());
                            ga.getSpatialForAgent().removeControl(CrowdChangeControl.class);
                        }

                        //Remove from crowd if CrowdAgent.
                        if (ga.getCrowdAgent() != null) {
                            //Check all crowds.
                            int numberOfCrowds = getState(CrowdManagerAppState.class).getCrowdManager().getNumberOfCrowds();

                            for (int i = 0; i < numberOfCrowds; i++) {
                                //If the crowd has this CrowdAgent remove.
                                if (getState(CrowdManagerAppState.class).getCrowdManager().getCrowd(i).getActiveAgents().contains(ga.getCrowdAgent())) {
                                    LOG.info("Removing [{}] from crowd [{}].", ga.getSpatialForAgent(), i);
                                    getState(CrowdManagerAppState.class).getCrowdManager().getCrowd(i).removeAgent(ga.crowdAgent);
                                    break;
                                }
                            } 
                        }

                        ((SimpleApplication) getApplication()).getRootNode().detachChild(ga.getSpatialForAgent());
                    }
                    //Remove the grid.
                    it.remove();
                    //Clear the selection so user hase to make a new one.
                    listBoxGrid.getSelectionModel().setSelection(-1);

                    //Force versioned reference update to update the Active 
                    //Grids window of the Crowd panel.
                    getState(CrowdBuilderState.class).updateVersRef();
                }
            }
            checkGrids = false;
        }
        
        if (gridSelectRef.update() || gridModelRef.update()) {
            getState(AgentParamState.class).updateParams();
        }
    }
    
    /**
     * Explains the settings for the Grid Generator.
     */
    private void showHelp() {
        String[] msg = {
        "Grid Name - The name used for the grid. The agents used in the grid will all be named according ",
        "to their insertion point into the grid of agents. This is a required setting.",
        " ",
        "Example: In a size 3 grid (3x3 = 9 agents), the naming is as follows.",
        " ",
        "r = row, c = column",
        "Row1 = gridName_r0_c0, gridName_r0_c1, gridName_r0_c2",
        "Row2 = gridname_r1_c0, gridName_r1_c1, gridName_r1_c2", 
        "Row2 = gridname_r2_c0, gridName_r2_c1, gridName_r2_c2", 
        " ",
        "Use Physics - If [ Use Physics ] is checked, it's expected that physics is to be used for ",
        "navigation movement and a PhysicsAgentControl and BetterCharacterControl will be added to ",
        "the Agent of choice. Radius, height and weight will be set as explained below.", 
        " ",
        "Agent Radius - The radius of the agent. Left unchecked, the value will be taken from the world ",
        "bounds of the spatial and is the smallest value in the x or z direction / 2. Applies to physics ",
        "agents exclusively.",
        " ",
        "Agent Height - The height of the agent. Left unchecked, the value will be taken from the world ",
        "bounds of the spatial and is the Y value * 2. Applies to physics agents exclusively.",
        " ",
        "Agent Weight - The weight of the agent. Left unchecked, a default weight of 1.0f will be ",
        "assigned. Applies to physics agents exclusively.",
        " ",
        "Agent Separation - Spacing between agents in the grid. Must be a setting of 0.1f or larger.",
        " ",
        "Agent - Model to use for the grid.",
        " ",
        "Grid Size - Number of agents to place into the grid in rows and columns. Limit = 15 which is 225 ",
        "agents.",
        " ",
        "Example: 2 = 2 rows, 2 columns = a grid of 4 agents.",
        " ",
        "Grids - The list of all the current grids. Select any grid from the list and press the [Remove Grid] ",
        "button to remove the grid and all active agents related to that grid. Each grid must have a ",
        "unique name. Spaces count in the naming so if there is added space after a grid name, that grid ",
        "will be considered unique. Selecting any grid will populate the [ Agent Parameters ] panel with ",
        "the grids settings. If the grid has been added to a crowd, the [ Crowd ] [ Active Grids ] panel will ",
        "display information about this grid as it relates to the Crowd it's a member of.",
        " ",
        "* Format: Name [Grid Size] [Grid Type]",
        " ",
        "Start Position - Starting position the agents will spread out evenly from to form the grid. This is ",
        "only used to generate the agents for the grid so you can drop your agents from above the ",
        "navMesh if you wish. The actual starting point of the agent is determined by its final position on ",
        "the navMesh prior to being added to the crowd."
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
     * Adds an GridAgent grid to the world.
     */
    private void addGrid() {
        String gridName;
        float separation;
        String agentPath;
        Vector3f startPos;

        //Radius
        if (checkRadius.isChecked()) {
            //The GridAgent radius. 
            if (fieldRadius.getText().isEmpty()
            || !getState(UtilState.class).isNumeric(fieldRadius.getText())) {
                displayMessage("[ Agent Radius ] requires a valid float value.", 0);
                return;
            } 
        }
            
        //Height
        if (checkHeight.isChecked()) {
            //The GridAgent height. 
            if (fieldHeight.getText().isEmpty()
            || !getState(UtilState.class).isNumeric(fieldHeight.getText())) {
                displayMessage("[ Agent Height ] requires a valid float value.", 0);
                return;
            }
        }
        
        //Weight
        if (checkPhysics.isChecked() 
        &&  checkWeight.isChecked()) {
            //The GridAgent weight. 
            if (fieldWeight.getText().isEmpty()
            || !getState(UtilState.class).isNumeric(fieldWeight.getText())) {
                displayMessage("[ Agent Weight ] requires a valid float value.", 0);
                return;
            }
        }
         
        //Garbage in?, no grid.
        if (!getState(UtilState.class).isNumeric(fieldDistance.getText()) 
        ||  fieldDistance.getText().isEmpty()) {
            displayMessage("[ Agent Separation ] requires a valid float value.", 0);
            return;
        } else {
            separation = new Float(fieldDistance.getText());
            //Stop negative separation input for agentPath grid.
            if (separation < 0.1f) {
                displayMessage("[ Agent Separation ] requires a float value >= 0.1f.", 0);
                return;
            }
        }

        //###### MODELS Section #####
        //Model to use for the grid. Selection is set to 0 when creating 
        //the listBoxAgent so shouldn't need to check for null.
        switch(listBoxAgent.getSelectionModel().getSelection()) {
            case 0: 
                agentPath = "Models/Jaime/Jaime.j3o";
                break;
            case 1:
                agentPath = "Models/Fish/Fish1.j3o";
                break;
            default: agentPath = null;
        }

        //Number of gridAgents to place into the grid in rows and columns.
        //Selection is set when creating the listBoxSize so shouldn't need to 
        //check for null.
        int size = listBoxSize.getSelectionModel().getSelection() + 1;

        //The starting position of the grid. Sanity check.
        if (!getState(UtilState.class).isNumeric(fieldPosX.getText()) || fieldPosX.getText().isEmpty() 
        ||  !getState(UtilState.class).isNumeric(fieldPosY.getText()) || fieldPosY.getText().isEmpty() 
        ||  !getState(UtilState.class).isNumeric(fieldPosZ.getText()) || fieldPosZ.getText().isEmpty()) {
            displayMessage("[ Start Position ] requires a valid float value.", 0);
            return;
        } else {
            Float x = new Float(fieldPosX.getText());
            Float y = new Float(fieldPosY.getText());
            Float z = new Float(fieldPosZ.getText());
            startPos = new Vector3f(x, y, z);
        }

        //The name of this grid.                
        if (fieldGridName.getText().isEmpty()) {
            displayMessage("You must enter a grid name.", 0);
            return;
        } else {
            List<Grid> grids = getGrids();
            for (Grid grid: grids) {
                if (grid.getGridName().equals(fieldGridName.getText())) {
                    displayMessage(
                      "[ " + fieldGridName.getText() 
                    + " ] has already been activated. "
                    + "Change the grid name or remove the existing grid before proceeding.", maxPopupSize);
                    return;
                }
            }
            gridName = fieldGridName.getText();
        }
                 
        addAgentGrid(agentPath, size, separation, startPos, gridName);
            
    }
    
    /**
     * Set the grid parameters for this grid. 
     * 
     * If checkPhysics is checked, it's expected that physics is to be used for 
     * navigation movement and a PhysicsAgentControl and BetterCharacterControl 
     * will be added to the node of choice. 
     * 
     * If either checkRadius or checkHeight is left unchecked, the value will be 
     * taken from the world bounds of the spatial for that attribute. Auto 
     * generation of radius and height is based off model bounds. For radius, 
     * this is the smallest value in the x or z direction / 2. For height, this 
     * would be the Y value * 2.
     * 
     * If weight is left unchecked, a default weight of 1.0f will be assigned.
     * 
     * If checkPhysics is left unchecked, no control will be used. The Radius 
     * and height of the spatial is determined as noted above except weight is 
     * ignored.
     * 
     * @param agentPath The Path of the GridAgent to be used for this grid.
     * @param size The size of the grid to be created.
     * @param distance The spacing between agents in the grid.
     * @param startPos The start position of the GridAgent. This has no 
     * other use outside of initial grid generation. 
     * @param gridName The name for this grid. This will also used when applying 
     * Obstacle Avoidance Parameters to the crowd.
     */          
    private void addAgentGrid(String agentPath, int size, float distance, Vector3f startPos, String gridName) {

        //Anything over 2 arguments creates a new object so split this up.
        LOG.info("<===== BEGIN AgentGridState addAgentGrid =====>");
        LOG.info("agentPath          [{}]", agentPath);
        LOG.info("size               [{}]", size);
        LOG.info("separation         [{}]", distance);
        LOG.info("startPos           [{}]", startPos);
        LOG.info("gridName           [{}]", gridName);


        List<GridAgent> listGridAgent = new ArrayList<>(size*size);
        
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                
                LOG.info("<<<<<<<<<<     >>>>>>>>>>");

                Node agent = new Node();
                
                //Add spatial to agent node.
                Spatial spatial = getApplication().getAssetManager().loadModel(agentPath);
                spatial.setName("spatial");
                agent.attachChild(spatial);
                
                //Agent name.
                agent.setName(gridName + "_r" + i + "_c"+ j);
                LOG.info("Agent Name        [{}]", agent.getName());              
                
                //Set the start position for each node
                float startX = startPos.getX() + i * distance;
                float startY = startPos.getY();
                float startZ = startPos.getZ() + j * distance;
                Vector3f start = new Vector3f(startX, startY, startZ);
                agent.setLocalTranslation(start);
                LOG.info("Position World     [{}]", agent.getWorldTranslation());
                LOG.info("Position Local     [{}]", agent.getLocalTranslation());
                
                //If checkPhysics, we use BCC, PhysicsAgentControl, radius, 
                //height, weight.
                if (checkPhysics.isChecked()) {
                    //The GridAgent radius, height, weight.
                    float weight;
                    float radius;
                    float height;

                    //If checked, we use the fieldRadius for the radius.
                    if (checkRadius.isChecked()) {
                        radius = new Float(fieldRadius.getText());
                        //Stop negative radius input.
                        if (radius < 0.0f) {
                            displayMessage("[ Agent Radius ] requires a float value >= 0.", 0);
                            listGridAgent = null;
                            return;
                        }
                    } else {
                        //Auto calculate based on bounds.
                        BoundingBox bounds = (BoundingBox) spatial.getWorldBound();
                        float x = bounds.getXExtent();
                        float z = bounds.getZExtent();

                        float xz = x < z ? x:z;
                        radius = xz/2;
                    }
                    
                    LOG.info("radius             [{}]", radius);
                    
                    //If checked, we use the fieldHeight for height.
                    if (checkHeight.isChecked()) {
                        height = new Float(fieldHeight.getText());
                        //Stop negative height input.
                        if (height <= 0.0f) {
                            displayMessage("[ Agent Height ] requires a float value > 0.", 0);
                            listGridAgent = null;
                            return;
                        }
                    } else {
                        //Auto calculate based on bounds.
                        BoundingBox bounds = (BoundingBox) spatial.getWorldBound();
                        float y = bounds.getYExtent();
                        height = y*2;
                    } 
                    
                    LOG.info("height             [{}]", height);
                    
                    //If checked, we use the fieldWeight for weight.
                    if (checkWeight.isChecked()) {
                        weight = new Float(fieldWeight.getText());
                        //Stop negative weight input.
                        if (weight <= 0.0f) {
                            displayMessage("[ Agent Weight ] requires a float value > 0.", 0);
                            listGridAgent = null;
                            return;
                        }
                    } else {
                        weight = 1.0f;
                    }
                    
                    LOG.info("weight             [{}]", weight);

                    //Give the GridAgent physics controls controls. Will be added to
                    //physics space from update loop.
                    //PhysicsRigidBody from CrowdBCC is detectable for cleanup
                    //so had to extend it just to add the getter.
//                    node.addControl(new BetterCharacterControl(radius, height, weight));
                    agent.addControl(new CrowdBCC(radius, height, weight));
                } 
                
                //Add to agents list.
                GridAgent gridAgent = new GridAgent(agent);
                listGridAgent.add(gridAgent);
            }
        }
        //Create grid and add to the Grids list. Informs the update loop to 
        //check for new grids to activate.
        LOG.info("listGridAgent size [{}]", listGridAgent.size());
        Grid grid = new Grid(gridName, listGridAgent);
        listBoxGrid.getModel().add(grid);
        newGrid = true;
        LOG.info("<===== END AgentGridState addAgentGrid =====>");
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
    
    //Removes a grid from mapGrids.
    private void removeGrid() {
        
        //Get the selectedGrid from listBoxGrid.
        Integer selectedGrid = getGridSelection();
        
        //Check to make sure a grid has been selected.
        if (selectedGrid == null) {
            displayMessage("You must select a grid before it can be removed.", 0);
            return;
        }
        
        //Get the grids from the listBoxGrid selectedGrid.
        Grid grid = getGrid(selectedGrid);
        
        //We have a valid grid so set the grid to be removed and tell the update
        //loop to look for grids to be removed.
        grid.setRemoveGrid(true);
        checkGrids = true;
    }

    /**
     * @return the contAgentGrid.
     */
    public Container getContAgentGrid() {
        return contAgentGrid;
    }
    
    /**
     * Gets the selected grid from the active grids list.
     * 
     * @return The selected grid or null. 
     */
    public Integer getGridSelection() {
        return listBoxGrid.getSelectionModel().getSelection();
    }
    
    /**
     * Gets the Grid from the active grids list.
     * 
     * @param index The index of the Grid to retrieve.
     * @return The Grid for the given index.
     */
    public Grid getGrid(int index) {
        return listBoxGrid.getModel().get(index);
    }
    
    /**
     * Gets the list of active grids.
     * 
     * @return The list of active grids.
     */
    public List<Grid> getGrids() {
        return listBoxGrid.getModel().getObject();
    }
    
    /**
     * The grid object for storing grid agents.
     */
    public class Grid {

        private final String gridName;
        private final List<GridAgent> listGridAgent;
        private boolean activeGrid;
        private boolean removeGrid;
        
        public Grid(String gridName, List<GridAgent> listGridAgent) {
            this.gridName = gridName;
            this.listGridAgent = listGridAgent;
        }
        
        /**
         * If true, this grid is scheduled for removal in the next update.
         * 
         * @return the checkGrids
         */
        public boolean isRemoveGrid() {
            return removeGrid;
        }

        /**
         * A setting of true will trigger the removal of the grid. All gridAgents 
         * associated with the grid will be removed from the physics space and
         * rootNode. Removal takes place in the next pass of the update loop.
         * 
         * @param removeGrid the checkGrids to set
         */
        public void setRemoveGrid(boolean removeGrid) {
            this.removeGrid = removeGrid;
        }
        
        /**
         * If grid is inactive it will be activated on the next update and all 
         * gridAgents loaded into the rootNode and physics space from the update loop.
         * 
         * @return the activeGrid
         */
        public boolean isActiveGrid() {
            return activeGrid;
        }

        /**
         * A setting of true will keep the grid active in the mapGrid and prevent
         * the loading of this grids agents.
         * 
         * @param activeGrid the activeGrid to set
         */
        public void setActiveGrid(boolean activeGrid) {
            this.activeGrid = activeGrid;
        }

        /**
         * The name of this grid.
         * 
         * @return the gridName
         */
        public String getGridName() {
            return gridName;
        }

        /**
         * The node list used for this agent grid.
         * 
         * @return The list of Grid Agents
         */
        public List<GridAgent> getListGridAgent() {
            return listGridAgent;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 11 * hash + Objects.hashCode(this.gridName);
            hash = 11 * hash + Objects.hashCode(this.listGridAgent);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Grid other = (Grid) obj;
            if (!Objects.equals(this.gridName, other.gridName)) {
                return false;
            }
            if (!Objects.equals(this.listGridAgent, other.listGridAgent)) {
                return false;
            }
            return true;
        }
        
        @Override
        public String toString() {
            return "Grid [name = "+ gridName + "] " + "AGENTS " + getListGridAgent(); 
        }

    }
    
    /**
     * Represents an agent for a grid. Uses a node for attaching and detaching of 
     * controls.
     */
    public class GridAgent {

        private CrowdAgent crowdAgent;
        private final Node node;
        
        public GridAgent(Node node) {
            this.node = node;
        }

        /**
         * @return The CrowdAgent for this node.
         */
        public CrowdAgent getCrowdAgent() {
            return crowdAgent;
        }

        /**
         * @param crowdAgent the CrowdAgent to set for this node.
         */
        public void setCrowdAgent(CrowdAgent crowdAgent) {
            this.crowdAgent = crowdAgent;
        }

        /**
         *
         * @return The node for this CrowdAgent.
         */
        public Node getSpatialForAgent() {
            return node;
        }
        
        @Override
        public String toString() {
            return node.getName();
        }

    } 

}

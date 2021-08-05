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
package com.jme3.recast4j.demo.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.Display;

import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.simsilica.lemur.Button;
import com.simsilica.lemur.Checkbox;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.ListBox;
import com.simsilica.lemur.Panel;
import com.simsilica.lemur.ProgressBar;
import com.simsilica.lemur.Slider;
import com.simsilica.lemur.TabbedPanel;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.AbstractGuiComponent;
import com.simsilica.lemur.core.GuiControl;
import com.simsilica.lemur.core.GuiLayout;
import net.miginfocom.layout.AC;
import net.miginfocom.layout.CC;
import net.miginfocom.layout.ComponentWrapper;
import net.miginfocom.layout.ConstraintParser;
import net.miginfocom.layout.ContainerWrapper;
import net.miginfocom.layout.Grid;
import net.miginfocom.layout.LC;
import net.miginfocom.layout.LayoutUtil;
import net.miginfocom.layout.PlatformDefaults;

/**
 * @author Zissis Trabaris You have the right to freely use, modify, and
 *         redistribute this code for any purpose royalty free.
 */
public class MigLayout extends AbstractGuiComponent implements GuiLayout, Cloneable {

    private static class LemurComponentWrapper implements net.miginfocom.layout.ComponentWrapper {

        private Node component;

        protected LemurComponentWrapper(Node component) {
            this.component = component;
        }

        @Override
        public Object getComponent() {
            return component;
        }

        @Override
        public final boolean equals(Object o) {
            if (o instanceof LemurComponentWrapper == false)
                return false;

            return component.equals(((LemurComponentWrapper) o).getComponent());
        }

        @Override
        public final int hashCode() {
            return component.hashCode();
        }

        @Override
        public int getX() {
            return (int) Math.floor(component.getLocalTranslation().x);
        }

        @Override
        public int getY() {
            return (int) Math.floor(component.getLocalTranslation().y * -1);
        }

        @Override
        public int getWidth() {
            return (int) component.getControl(GuiControl.class).getSize().x;
        }

        @Override
        public int getHeight() {
            return (int) component.getControl(GuiControl.class).getSize().y;
        }

        @Override
        public int getScreenLocationX() {
            return (int) GuiGlobals.getInstance().getScreenCoordinates(component, component.getLocalTranslation()).x;
        }

        @Override
        public int getScreenLocationY() {
            return (int) GuiGlobals.getInstance().getScreenCoordinates(component, component.getLocalTranslation()).y;
        }

        @Override
        public int getMinimumWidth(int hHint) {
            return getPreferredWidth(hHint);
        }

        @Override
        public int getMinimumHeight(int wHint) {
            return getPreferredHeight(wHint);
        }

        @Override
        public int getPreferredWidth(int hHint) {
            return (int) Math.ceil(component.getControl(GuiControl.class).getPreferredSize().x);
        }

        @Override
        public int getPreferredHeight(int wHint) {
            return (int) Math.ceil(component.getControl(GuiControl.class).getPreferredSize().y);
        }

        @Override
        public int getMaximumWidth(int hHint) {
            return Display.getWidth();
        }

        @Override
        public int getMaximumHeight(int wHint) {
            return Display.getHeight();
        }

        @Override
        public void setBounds(int x, int y, int width, int height) {
            component.setLocalTranslation(new Vector3f(x, y * -1, component.getLocalTranslation().z));
            Vector3f size = component.getControl(GuiControl.class).getPreferredSize().clone();
            size.set(width < 0 ? (width * -1) + size.getX() : width, height < 0 ? (height * -1) + size.getY() : height, size.z);
            component.getControl(GuiControl.class).setSize(size);
        }

        @Override
        public boolean isVisible() {
            return component.getCullHint() != CullHint.Always;
            // return true;
        }

        @Override
        public int getBaseline(int width, int height) {

            return -1;
        }

        @Override
        public boolean hasBaseline() {
            return false;
        }

        @Override
        public ContainerWrapper getParent() {
            return new LemurContainerWrapper(component.getParent());
        }

        @Override
        public float getPixelUnitFactor(boolean isHor) {
            return 1;
        }

        @Override
        public int getHorizontalScreenDPI() {
            return PlatformDefaults.getDefaultDPI();
        }

        @Override
        public int getVerticalScreenDPI() {
            return PlatformDefaults.getDefaultDPI();
        }

        @Override
        public int getScreenWidth() {
            return GuiGlobals.getInstance().getCollisionViewPort(component).getCamera().getWidth();
        }

        @Override
        public int getScreenHeight() {
            return GuiGlobals.getInstance().getCollisionViewPort(component).getCamera().getHeight();
        }

        @Override
        public String getLinkId() {

            return component.getName();
        }

        @Override
        public int getLayoutHashCode() {

            int hash = getWidth() + (getHeight() << 5);

            Vector3f size = component.getLocalTranslation();
            hash += (((int) size.x) << 10) + (((int) size.y) << 15);

            if (isVisible())
                hash += 1324511;

            String id = getLinkId();
            if (id != null)
                hash += id.hashCode();

            return hash;
        }

        @Override
        public int[] getVisualPadding() {
            return null;
        }

        @Override
        public void paintDebugOutline(boolean showVisualPadding) {

        }

        private int compType = TYPE_UNSET;

        @Override
        public int getComponentType(boolean disregardScrollPane) {
            if (compType == TYPE_UNSET)
                compType = checkType(disregardScrollPane);

            return compType;
        }

        private int checkType(boolean disregardScrollPane) {
            Node c = component;

            if (c instanceof TextField) {
                return TYPE_TEXT_FIELD;
            } else if (c instanceof Checkbox) {
                return TYPE_CHECK_BOX;
            } else if (c instanceof Button) {
                return TYPE_BUTTON;
            } else if (c instanceof Label) {
                return TYPE_LABEL;
            } else if (c instanceof ListBox) {
                return TYPE_LIST;
            } else if (c instanceof ProgressBar) {
                return TYPE_PROGRESS_BAR;
            } else if (c instanceof Slider) {
                return TYPE_SCROLL_BAR;
            } else if (c instanceof TabbedPanel) {
                return TYPE_TABBED_PANE;
            } else if (c instanceof Container) {
                return TYPE_CONTAINER;
            } else if (c instanceof Panel) {
                return TYPE_PANEL;
            }
            return TYPE_UNKNOWN;
        }

        @Override
        public int getContentBias() {
            return LayoutUtil.HORIZONTAL;
        }

    }

    private static class LemurContainerWrapper extends LemurComponentWrapper implements ContainerWrapper {

        protected LemurContainerWrapper(Node component) {
            super(component);
        }

        @Override
        public ComponentWrapper[] getComponents() {
            Container c = (Container) getComponent();
            Node[] children = c.getLayout().getChildren().toArray(new Node[0]);
            LemurComponentWrapper[] lcw = new LemurComponentWrapper[children.length];
            for (int i = 0; i < lcw.length; i++)
                lcw[i] = new LemurComponentWrapper((Panel) children[i]);
            return lcw;
        }

        @Override
        public int getComponentCount() {
            Container c = (Container) getComponent();
            return c.getLayout().getChildren().size();
        }

        @Override
        public Object getLayout() {
            return ((Container) getComponent()).getLayout();
        }

        @Override
        public boolean isLeftToRight() {
            return true;
        }

        @Override
        public void paintDebugCell(int x, int y, int width, int height) {
        }

    }

    private transient LC lc = null;
    private AC colSpecs = null, rowSpecs = null;
    private Grid grid = null;
    private final Map < LemurComponentWrapper, CC > ccMap = new HashMap < > (8);
    /**
     * The component to string constraints mappings.
     */
    private final Map < Panel, Object > scrConstrMap = new IdentityHashMap < > (8);
    /**
     * Hold the serializable text representation of the constraints.
     */
    private Object layoutConstraints = "", colConstraints = "", rowConstraints = ""; // Should never be null!
    private GuiControl parent;
    private List<Node> children = new ArrayList<>();
    private LemurContainerWrapper containerWrapper = null;

    /**
     * Constructor.
     * 
     * @param layoutConstraints The constraints that concern the whole layout.
     *                          <code>null</code> will be treated as "".
     */
    public MigLayout(String layoutConstraints) {
        this(layoutConstraints, "", "");
    }

    /**
     * Constructor.
     * 
     * @param layoutConstraints The constraints that concern the whole layout.
     *                          <code>null</code> will be treated as "".
     * @param colConstraints    The constraints for the columns in the grid.
     *                          <code>null</code> will be treated as "".
     */
    public MigLayout(String layoutConstraints, String colConstraints) {
        this(layoutConstraints, colConstraints, "");
    }

    /**
     * Constructor.
     * 
     * @param layoutConstraints The constraints that concern the whole layout.
     *                          <code>null</code> will be treated as "".
     * @param colConstraints    The constraints for the columns in the grid.
     *                          <code>null</code> will be treated as "".
     * @param rowConstraints    The constraints for the rows in the grid.
     *                          <code>null</code> will be treated as "".
     */
    public MigLayout(String layoutConstraints, String colConstraints, String rowConstraints) {
        setLayoutConstraints(layoutConstraints);
        setColumnConstraints(colConstraints);
        setRowConstraints(rowConstraints);
    }

    /**
     * Sets the layout constraints for the layout manager instance as a String.
     * <p>
     * See the class JavaDocs for information on how this string is formatted.
     * 
     * @param constr The layout constraints as a String or
     *               {@link net.miginfocom.layout.LC} representation.
     *               <code>null</code> is converted to <code>""</code> for storage.
     * @throws RuntimeException if the constraint was not valid.
     */
    public void setLayoutConstraints(Object constr) {
        if (constr == null || constr instanceof String) {
            constr = ConstraintParser.prepare((String) constr);
            lc = ConstraintParser.parseLayoutConstraint((String) constr);
        } else if (constr instanceof LC) {
            lc = (LC) constr;
        } else {
            throw new IllegalArgumentException("Illegal constraint type: " + constr.getClass().toString());
        }
        layoutConstraints = constr;
        invalidate();
    }

    /**
     * Returns layout constraints either as a <code>String</code> or
     * {@link net.miginfocom.layout.LC} depending what was sent in to the
     * constructor or set with {@link #setLayoutConstraints(Object)}.
     * 
     * @return The layout constraints either as a <code>String</code> or
     *         {@link net.miginfocom.layout.LC} depending what was sent in to the
     *         constructor or set with {@link #setLayoutConstraints(Object)}. Never
     *         <code>null</code>.
     */
    public Object getLayoutConstraints() {
        return layoutConstraints;
    }

    /**
     * Returns the column layout constraints either as a <code>String</code> or
     * {@link net.miginfocom.layout.AC}.
     * 
     * @return The column constraints either as a <code>String</code> or
     *         {@link net.miginfocom.layout.AC} depending what was sent in to the
     *         constructor or set with {@link #setColumnConstraints(Object)}. Never
     *         <code>null</code>.
     */
    public Object getColumnConstraints() {
        return colConstraints;
    }

    /**
     * Sets the column layout constraints for the layout manager instance as a
     * String.
     * <p>
     * See the class JavaDocs for information on how this string is formatted.
     * 
     * @param constr The column layout constraints as a String or
     *               {@link net.miginfocom.layout.AC} representation.
     *               <code>null</code> is converted to <code>""</code> for storage.
     * @throws RuntimeException if the constraint was not valid.
     */
    public void setColumnConstraints(Object constr) {
        if (constr == null || constr instanceof String) {
            constr = ConstraintParser.prepare((String) constr);
            colSpecs = ConstraintParser.parseColumnConstraints((String) constr);
        } else if (constr instanceof AC) {
            colSpecs = (AC) constr;
        } else {
            throw new IllegalArgumentException("Illegal constraint type: " + constr.getClass().toString());
        }
        colConstraints = constr;
        invalidate();
    }

    /**
     * Returns the row layout constraints either as a <code>String</code> or
     * {@link net.miginfocom.layout.AC}.
     * 
     * @return The row constraints either as a <code>String</code> or
     *         {@link net.miginfocom.layout.AC} depending what was sent in to the
     *         constructor or set with {@link #setRowConstraints(Object)}. Never
     *         <code>null</code>.
     */
    public Object getRowConstraints() {
        return rowConstraints;
    }

    /**
     * Sets the row layout constraints for the layout manager instance as a String.
     * <p>
     * See the class JavaDocs for information on how this string is formatted.
     * 
     * @param constr The row layout constraints as a String or
     *               {@link net.miginfocom.layout.AC} representation.
     *               <code>null</code> is converted to <code>""</code> for storage.
     * @throws RuntimeException if the constraint was not valid.
     */
    public void setRowConstraints(Object constr) {
        if (constr == null || constr instanceof String) {
            constr = ConstraintParser.prepare((String) constr);
            rowSpecs = ConstraintParser.parseRowConstraints((String) constr);
        } else if (constr instanceof AC) {
            rowSpecs = (AC) constr;
        } else {
            throw new IllegalArgumentException("Illegal constraint type: " + constr.getClass().toString());
        }
        rowConstraints = constr;
        invalidate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.simsilica.lemur.core.GuiComponent#calculatePreferredSize(com.jme3.
     * math.Vector3f)
     */
    @Override
    public void calculatePreferredSize(Vector3f size) {
        if (parent == null)
            return;

        Grid grid = new Grid(containerWrapper, lc, rowSpecs, colSpecs, ccMap, null);
        int[] b = new int[] {
            0,
            0,
            (int) parent.getSize().x,
            (int) parent.getSize().y
        };

        if (grid.layout(b, lc.getAlignX(), lc.getAlignY(), false)) {
            grid = new Grid(containerWrapper, lc, rowSpecs, colSpecs, ccMap, null);
            grid.layout(b, lc.getAlignX(), lc.getAlignY(), false);
        }
        
        int w = LayoutUtil.getSizeSafe(grid.getWidth(), LayoutUtil.PREF);
        int h = LayoutUtil.getSizeSafe(grid.getHeight(), LayoutUtil.PREF);
        size.set(w, h, parent.getSize().z);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.simsilica.lemur.core.GuiComponent#reshape(com.jme3.math.Vector3f,
     * com.jme3.math.Vector3f)
     */
    @Override
    public void reshape(Vector3f pos, Vector3f size) {
        if (parent == null)
            return;
        // for(Node n: children)
        // n.getControl(GuiControl.class).getPreferredSize();

        int[] b = new int[] {
            (int) Math.floor(pos.x), (int) Math.floor(pos.y * -1), (int) Math.ceil(size.x), (int) Math.ceil(size.y)
        };
        Grid grid = new Grid(containerWrapper, lc, rowSpecs, colSpecs, ccMap, null);
        if (grid.layout(b, lc.getAlignX(), lc.getAlignY(), false)) {
            grid = new Grid(containerWrapper, lc, rowSpecs, colSpecs, ccMap, null);
            grid.layout(b, lc.getAlignX(), lc.getAlignY(), false);
        }
        for (Node n: children)
            n.setLocalTranslation(n.getLocalTranslation().clone().setZ(pos.z));
        // parent.getNode().setLocalTranslation(pos);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.simsilica.lemur.core.GuiLayout#addChild(com.jme3.scene.Node,
     * java.lang.Object[])
     */
    @Override
    public < T extends Node > T addChild(T n, Object...constraints) {
        
    	if (n.getControl(GuiControl.class) == null)
            throw new IllegalArgumentException("Child is not GUI element.");
        
        LemurComponentWrapper componentWrapper = new LemurComponentWrapper((Panel) n);
        String constraintString = constraints.length > 0 && constraints[0] instanceof String ? (String) constraints[0] : null;
        String cStr = ConstraintParser.prepare(constraintString);

        scrConstrMap.put((Panel) n, constraintString);
        ccMap.put(componentWrapper, ConstraintParser.parseComponentConstraint(cStr));

        children.add(n);

        if (parent != null) {
            // We are attached
            parent.getNode().attachChild(n);
        }

        invalidate();
        return n;
    }

    @Override
    protected void invalidate() {
        if (parent != null) {
            parent.invalidate();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.simsilica.lemur.core.GuiLayout#removeChild(com.jme3.scene.Node)
     */
    @Override
    public void removeChild(Node n) {
        scrConstrMap.remove(n);
        ccMap.remove(new LemurComponentWrapper((Panel) n));
        grid = null; // To clear references

        if (children.remove(n)) {
            if (parent != null) {
                parent.getNode().detachChild(n);
            }
        }
        invalidate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.simsilica.lemur.core.GuiLayout#getChildren()
     */
    @Override
    public Collection < Node > getChildren() {
        return Collections.unmodifiableList(children);
    }

    @Override
    public void attach(GuiControl parent) {
        this.parent = parent;
        this.containerWrapper = new LemurContainerWrapper((Panel) parent.getNode());
        Node self = parent.getNode();
        for (Node n: children) {
            self.attachChild(n);
        }
    }

    @Override
    public void detach(GuiControl parent) {
        this.parent = null;
        this.containerWrapper = null;
        // Have to make a copy to avoid concurrent mod exceptions
        // now that the containers are smart enough to call remove
        // when detachChild() is called. A small side-effect.
        // Possibly a better way to do this? Disable loop-back removal
        // somehow?
        Collection < Node > copy = new ArrayList < > (children);
        for (Node n: copy) {
            n.removeFromParent();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.simsilica.lemur.core.GuiLayout#clearChildren()
     */
    @Override
    public void clearChildren() {
        for (Node n: children.toArray(new Node[children.size()])) {
            removeChild(n);
        }
        invalidate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.simsilica.lemur.core.GuiLayout#clone()
     */
    @Override
    public GuiLayout clone() {
        // TODO Auto-generated method stub
        return null;
    }

}

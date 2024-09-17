package com.jme3.recast4j.demo.states;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.KeyInput;
import com.jme3.math.ColorRGBA;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.TextField;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.TbtQuadBackgroundComponent;
import com.simsilica.lemur.focus.FocusNavigationFunctions;
import com.simsilica.lemur.style.Attributes;
import com.simsilica.lemur.style.BaseStyles;
import com.simsilica.lemur.style.Styles;

/**
 *
 * @author Robert
 */
public class LemurConfigState extends BaseAppState {

    @Override
    protected void initialize(Application app) {
        GuiGlobals.initialize(app);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        // Block Lemur from mapping input.
        GuiGlobals.getInstance().getInputMapper().map(FocusNavigationFunctions.F_X_AXIS, KeyInput.KEY_RIGHT);
        GuiGlobals.getInstance().getInputMapper().map(FocusNavigationFunctions.F_Y_AXIS, KeyInput.KEY_RIGHT);

        // Make container panels solid.
        Styles styles = GuiGlobals.getInstance().getStyles();
        Attributes attrs = styles.getSelector(Container.ELEMENT_ID, "glass");
        TbtQuadBackgroundComponent bg = attrs.get("background");
        bg.setColor(new ColorRGBA(0.25f, 0.5f, 0.5f, 1.0f));

        // Set the rollup button colors
        // Default is pink with alpha .85.
        attrs = styles.getSelector("title", "glass");
        attrs.set("highlightColor", new ColorRGBA(ColorRGBA.Pink));
        attrs.set("focusColor", new ColorRGBA(ColorRGBA.Magenta));

        // Set the default font size
        attrs = styles.getSelector("glass");
        attrs.set("fontSize", 12);

        // Change textfield background from defaults.
        attrs = styles.getSelector(TextField.ELEMENT_ID, "glass");
        attrs.set("background", new QuadBackgroundComponent(new ColorRGBA(ColorRGBA.DarkGray)), false);
    }

    @Override
    protected void cleanup(Application app) {
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

}

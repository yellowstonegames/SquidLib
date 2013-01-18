package squidpony.squidgrid.gui.swing;

import java.beans.*;

/**
 * The information class for the Swing implementation of SGPane.
 *
 * @author Eben Howard - http://squidpony.com - howard@squidpony.com
 */
public class SwingPaneBeanInfo extends SimpleBeanInfo {

    // Bean descriptor//GEN-FIRST:BeanDescriptor
    /*lazy BeanDescriptor*/
    private static BeanDescriptor getBdescriptor(){
        BeanDescriptor beanDescriptor = new BeanDescriptor  ( squidpony.squidgrid.gui.swing.SwingPane.class , null ); // NOI18N//GEN-HEADEREND:BeanDescriptor

        // Here you can add code for customizing the BeanDescriptor.

        return beanDescriptor;     }//GEN-LAST:BeanDescriptor
    // Property identifiers//GEN-FIRST:Properties
    private static final int PROPERTY_accessibleContext = 0;
    private static final int PROPERTY_actionMap = 1;
    private static final int PROPERTY_alignmentX = 2;
    private static final int PROPERTY_alignmentY = 3;
    private static final int PROPERTY_ancestorListeners = 4;
    private static final int PROPERTY_autoscrolls = 5;
    private static final int PROPERTY_background = 6;
    private static final int PROPERTY_backgroundSet = 7;
    private static final int PROPERTY_baselineResizeBehavior = 8;
    private static final int PROPERTY_border = 9;
    private static final int PROPERTY_bounds = 10;
    private static final int PROPERTY_cellDimension = 11;
    private static final int PROPERTY_cellHeight = 12;
    private static final int PROPERTY_cellWidth = 13;
    private static final int PROPERTY_colorModel = 14;
    private static final int PROPERTY_component = 15;
    private static final int PROPERTY_componentCount = 16;
    private static final int PROPERTY_componentCountInLayer = 17;
    private static final int PROPERTY_componentListeners = 18;
    private static final int PROPERTY_componentOrientation = 19;
    private static final int PROPERTY_componentPopupMenu = 20;
    private static final int PROPERTY_components = 21;
    private static final int PROPERTY_componentsInLayer = 22;
    private static final int PROPERTY_containerListeners = 23;
    private static final int PROPERTY_cursor = 24;
    private static final int PROPERTY_cursorSet = 25;
    private static final int PROPERTY_debugGraphicsOptions = 26;
    private static final int PROPERTY_defaultBackground = 27;
    private static final int PROPERTY_defaultForeground = 28;
    private static final int PROPERTY_displayable = 29;
    private static final int PROPERTY_doubleBuffered = 30;
    private static final int PROPERTY_dropTarget = 31;
    private static final int PROPERTY_enabled = 32;
    private static final int PROPERTY_focusable = 33;
    private static final int PROPERTY_focusCycleRoot = 34;
    private static final int PROPERTY_focusCycleRootAncestor = 35;
    private static final int PROPERTY_focusListeners = 36;
    private static final int PROPERTY_focusOwner = 37;
    private static final int PROPERTY_focusTraversable = 38;
    private static final int PROPERTY_focusTraversalKeys = 39;
    private static final int PROPERTY_focusTraversalKeysEnabled = 40;
    private static final int PROPERTY_focusTraversalPolicy = 41;
    private static final int PROPERTY_focusTraversalPolicyProvider = 42;
    private static final int PROPERTY_focusTraversalPolicySet = 43;
    private static final int PROPERTY_font = 44;
    private static final int PROPERTY_fontSet = 45;
    private static final int PROPERTY_foreground = 46;
    private static final int PROPERTY_foregroundSet = 47;
    private static final int PROPERTY_graphics = 48;
    private static final int PROPERTY_graphicsConfiguration = 49;
    private static final int PROPERTY_gridHeight = 50;
    private static final int PROPERTY_gridWidth = 51;
    private static final int PROPERTY_height = 52;
    private static final int PROPERTY_hierarchyBoundsListeners = 53;
    private static final int PROPERTY_hierarchyListeners = 54;
    private static final int PROPERTY_ignoreRepaint = 55;
    private static final int PROPERTY_imageCellMap = 56;
    private static final int PROPERTY_inheritsPopupMenu = 57;
    private static final int PROPERTY_inputContext = 58;
    private static final int PROPERTY_inputMap = 59;
    private static final int PROPERTY_inputMethodListeners = 60;
    private static final int PROPERTY_inputMethodRequests = 61;
    private static final int PROPERTY_inputVerifier = 62;
    private static final int PROPERTY_insets = 63;
    private static final int PROPERTY_keyListeners = 64;
    private static final int PROPERTY_layout = 65;
    private static final int PROPERTY_lightweight = 66;
    private static final int PROPERTY_locale = 67;
    private static final int PROPERTY_location = 68;
    private static final int PROPERTY_locationOnScreen = 69;
    private static final int PROPERTY_managingFocus = 70;
    private static final int PROPERTY_maximumSize = 71;
    private static final int PROPERTY_maximumSizeSet = 72;
    private static final int PROPERTY_minimumSize = 73;
    private static final int PROPERTY_minimumSizeSet = 74;
    private static final int PROPERTY_mouseListeners = 75;
    private static final int PROPERTY_mouseMotionListeners = 76;
    private static final int PROPERTY_mousePosition = 77;
    private static final int PROPERTY_mouseWheelListeners = 78;
    private static final int PROPERTY_name = 79;
    private static final int PROPERTY_nextFocusableComponent = 80;
    private static final int PROPERTY_opaque = 81;
    private static final int PROPERTY_optimizedDrawingEnabled = 82;
    private static final int PROPERTY_paintingForPrint = 83;
    private static final int PROPERTY_paintingTile = 84;
    private static final int PROPERTY_parent = 85;
    private static final int PROPERTY_peer = 86;
    private static final int PROPERTY_preferredSize = 87;
    private static final int PROPERTY_preferredSizeSet = 88;
    private static final int PROPERTY_propertyChangeListeners = 89;
    private static final int PROPERTY_registeredKeyStrokes = 90;
    private static final int PROPERTY_requestFocusEnabled = 91;
    private static final int PROPERTY_rootPane = 92;
    private static final int PROPERTY_showing = 93;
    private static final int PROPERTY_size = 94;
    private static final int PROPERTY_text = 95;
    private static final int PROPERTY_textCellFactory = 96;
    private static final int PROPERTY_textFactory = 97;
    private static final int PROPERTY_toolkit = 98;
    private static final int PROPERTY_toolTipText = 99;
    private static final int PROPERTY_topLevelAncestor = 100;
    private static final int PROPERTY_transferHandler = 101;
    private static final int PROPERTY_treeLock = 102;
    private static final int PROPERTY_UIClassID = 103;
    private static final int PROPERTY_valid = 104;
    private static final int PROPERTY_validateRoot = 105;
    private static final int PROPERTY_verifyInputWhenFocusTarget = 106;
    private static final int PROPERTY_vetoableChangeListeners = 107;
    private static final int PROPERTY_visible = 108;
    private static final int PROPERTY_visibleRect = 109;
    private static final int PROPERTY_width = 110;
    private static final int PROPERTY_x = 111;
    private static final int PROPERTY_y = 112;

    // Property array 
    /*lazy PropertyDescriptor*/
    private static PropertyDescriptor[] getPdescriptor(){
        PropertyDescriptor[] properties = new PropertyDescriptor[113];
    
        try {
            properties[PROPERTY_accessibleContext] = new PropertyDescriptor ( "accessibleContext", squidpony.squidgrid.gui.swing.SwingPane.class, "getAccessibleContext", null ); // NOI18N
            properties[PROPERTY_actionMap] = new PropertyDescriptor ( "actionMap", squidpony.squidgrid.gui.swing.SwingPane.class, "getActionMap", "setActionMap" ); // NOI18N
            properties[PROPERTY_alignmentX] = new PropertyDescriptor ( "alignmentX", squidpony.squidgrid.gui.swing.SwingPane.class, "getAlignmentX", "setAlignmentX" ); // NOI18N
            properties[PROPERTY_alignmentY] = new PropertyDescriptor ( "alignmentY", squidpony.squidgrid.gui.swing.SwingPane.class, "getAlignmentY", "setAlignmentY" ); // NOI18N
            properties[PROPERTY_ancestorListeners] = new PropertyDescriptor ( "ancestorListeners", squidpony.squidgrid.gui.swing.SwingPane.class, "getAncestorListeners", null ); // NOI18N
            properties[PROPERTY_autoscrolls] = new PropertyDescriptor ( "autoscrolls", squidpony.squidgrid.gui.swing.SwingPane.class, "getAutoscrolls", "setAutoscrolls" ); // NOI18N
            properties[PROPERTY_background] = new PropertyDescriptor ( "background", squidpony.squidgrid.gui.swing.SwingPane.class, "getBackground", "setBackground" ); // NOI18N
            properties[PROPERTY_backgroundSet] = new PropertyDescriptor ( "backgroundSet", squidpony.squidgrid.gui.swing.SwingPane.class, "isBackgroundSet", null ); // NOI18N
            properties[PROPERTY_baselineResizeBehavior] = new PropertyDescriptor ( "baselineResizeBehavior", squidpony.squidgrid.gui.swing.SwingPane.class, "getBaselineResizeBehavior", null ); // NOI18N
            properties[PROPERTY_border] = new PropertyDescriptor ( "border", squidpony.squidgrid.gui.swing.SwingPane.class, "getBorder", "setBorder" ); // NOI18N
            properties[PROPERTY_bounds] = new PropertyDescriptor ( "bounds", squidpony.squidgrid.gui.swing.SwingPane.class, "getBounds", "setBounds" ); // NOI18N
            properties[PROPERTY_cellDimension] = new PropertyDescriptor ( "cellDimension", squidpony.squidgrid.gui.swing.SwingPane.class, "getCellDimension", null ); // NOI18N
            properties[PROPERTY_cellHeight] = new PropertyDescriptor ( "cellHeight", squidpony.squidgrid.gui.swing.SwingPane.class, "getCellHeight", null ); // NOI18N
            properties[PROPERTY_cellWidth] = new PropertyDescriptor ( "cellWidth", squidpony.squidgrid.gui.swing.SwingPane.class, "getCellWidth", null ); // NOI18N
            properties[PROPERTY_colorModel] = new PropertyDescriptor ( "colorModel", squidpony.squidgrid.gui.swing.SwingPane.class, "getColorModel", null ); // NOI18N
            properties[PROPERTY_component] = new IndexedPropertyDescriptor ( "component", squidpony.squidgrid.gui.swing.SwingPane.class, null, null, "getComponent", null ); // NOI18N
            properties[PROPERTY_componentCount] = new PropertyDescriptor ( "componentCount", squidpony.squidgrid.gui.swing.SwingPane.class, "getComponentCount", null ); // NOI18N
            properties[PROPERTY_componentCountInLayer] = new IndexedPropertyDescriptor ( "componentCountInLayer", squidpony.squidgrid.gui.swing.SwingPane.class, null, null, "getComponentCountInLayer", null ); // NOI18N
            properties[PROPERTY_componentListeners] = new PropertyDescriptor ( "componentListeners", squidpony.squidgrid.gui.swing.SwingPane.class, "getComponentListeners", null ); // NOI18N
            properties[PROPERTY_componentOrientation] = new PropertyDescriptor ( "componentOrientation", squidpony.squidgrid.gui.swing.SwingPane.class, "getComponentOrientation", "setComponentOrientation" ); // NOI18N
            properties[PROPERTY_componentPopupMenu] = new PropertyDescriptor ( "componentPopupMenu", squidpony.squidgrid.gui.swing.SwingPane.class, "getComponentPopupMenu", "setComponentPopupMenu" ); // NOI18N
            properties[PROPERTY_components] = new PropertyDescriptor ( "components", squidpony.squidgrid.gui.swing.SwingPane.class, "getComponents", null ); // NOI18N
            properties[PROPERTY_componentsInLayer] = new IndexedPropertyDescriptor ( "componentsInLayer", squidpony.squidgrid.gui.swing.SwingPane.class, null, null, "getComponentsInLayer", null ); // NOI18N
            properties[PROPERTY_containerListeners] = new PropertyDescriptor ( "containerListeners", squidpony.squidgrid.gui.swing.SwingPane.class, "getContainerListeners", null ); // NOI18N
            properties[PROPERTY_cursor] = new PropertyDescriptor ( "cursor", squidpony.squidgrid.gui.swing.SwingPane.class, "getCursor", "setCursor" ); // NOI18N
            properties[PROPERTY_cursorSet] = new PropertyDescriptor ( "cursorSet", squidpony.squidgrid.gui.swing.SwingPane.class, "isCursorSet", null ); // NOI18N
            properties[PROPERTY_debugGraphicsOptions] = new PropertyDescriptor ( "debugGraphicsOptions", squidpony.squidgrid.gui.swing.SwingPane.class, "getDebugGraphicsOptions", "setDebugGraphicsOptions" ); // NOI18N
            properties[PROPERTY_defaultBackground] = new PropertyDescriptor ( "defaultBackground", squidpony.squidgrid.gui.swing.SwingPane.class, null, "setDefaultBackground" ); // NOI18N
            properties[PROPERTY_defaultForeground] = new PropertyDescriptor ( "defaultForeground", squidpony.squidgrid.gui.swing.SwingPane.class, null, "setDefaultForeground" ); // NOI18N
            properties[PROPERTY_displayable] = new PropertyDescriptor ( "displayable", squidpony.squidgrid.gui.swing.SwingPane.class, "isDisplayable", null ); // NOI18N
            properties[PROPERTY_doubleBuffered] = new PropertyDescriptor ( "doubleBuffered", squidpony.squidgrid.gui.swing.SwingPane.class, "isDoubleBuffered", "setDoubleBuffered" ); // NOI18N
            properties[PROPERTY_dropTarget] = new PropertyDescriptor ( "dropTarget", squidpony.squidgrid.gui.swing.SwingPane.class, "getDropTarget", "setDropTarget" ); // NOI18N
            properties[PROPERTY_enabled] = new PropertyDescriptor ( "enabled", squidpony.squidgrid.gui.swing.SwingPane.class, "isEnabled", "setEnabled" ); // NOI18N
            properties[PROPERTY_focusable] = new PropertyDescriptor ( "focusable", squidpony.squidgrid.gui.swing.SwingPane.class, "isFocusable", "setFocusable" ); // NOI18N
            properties[PROPERTY_focusCycleRoot] = new PropertyDescriptor ( "focusCycleRoot", squidpony.squidgrid.gui.swing.SwingPane.class, "isFocusCycleRoot", "setFocusCycleRoot" ); // NOI18N
            properties[PROPERTY_focusCycleRootAncestor] = new PropertyDescriptor ( "focusCycleRootAncestor", squidpony.squidgrid.gui.swing.SwingPane.class, "getFocusCycleRootAncestor", null ); // NOI18N
            properties[PROPERTY_focusListeners] = new PropertyDescriptor ( "focusListeners", squidpony.squidgrid.gui.swing.SwingPane.class, "getFocusListeners", null ); // NOI18N
            properties[PROPERTY_focusOwner] = new PropertyDescriptor ( "focusOwner", squidpony.squidgrid.gui.swing.SwingPane.class, "isFocusOwner", null ); // NOI18N
            properties[PROPERTY_focusTraversable] = new PropertyDescriptor ( "focusTraversable", squidpony.squidgrid.gui.swing.SwingPane.class, "isFocusTraversable", null ); // NOI18N
            properties[PROPERTY_focusTraversalKeys] = new IndexedPropertyDescriptor ( "focusTraversalKeys", squidpony.squidgrid.gui.swing.SwingPane.class, null, null, null, "setFocusTraversalKeys" ); // NOI18N
            properties[PROPERTY_focusTraversalKeysEnabled] = new PropertyDescriptor ( "focusTraversalKeysEnabled", squidpony.squidgrid.gui.swing.SwingPane.class, "getFocusTraversalKeysEnabled", "setFocusTraversalKeysEnabled" ); // NOI18N
            properties[PROPERTY_focusTraversalPolicy] = new PropertyDescriptor ( "focusTraversalPolicy", squidpony.squidgrid.gui.swing.SwingPane.class, "getFocusTraversalPolicy", "setFocusTraversalPolicy" ); // NOI18N
            properties[PROPERTY_focusTraversalPolicyProvider] = new PropertyDescriptor ( "focusTraversalPolicyProvider", squidpony.squidgrid.gui.swing.SwingPane.class, "isFocusTraversalPolicyProvider", "setFocusTraversalPolicyProvider" ); // NOI18N
            properties[PROPERTY_focusTraversalPolicySet] = new PropertyDescriptor ( "focusTraversalPolicySet", squidpony.squidgrid.gui.swing.SwingPane.class, "isFocusTraversalPolicySet", null ); // NOI18N
            properties[PROPERTY_font] = new PropertyDescriptor ( "font", squidpony.squidgrid.gui.swing.SwingPane.class, "getFont", "setFont" ); // NOI18N
            properties[PROPERTY_fontSet] = new PropertyDescriptor ( "fontSet", squidpony.squidgrid.gui.swing.SwingPane.class, "isFontSet", null ); // NOI18N
            properties[PROPERTY_foreground] = new PropertyDescriptor ( "foreground", squidpony.squidgrid.gui.swing.SwingPane.class, "getForeground", "setForeground" ); // NOI18N
            properties[PROPERTY_foregroundSet] = new PropertyDescriptor ( "foregroundSet", squidpony.squidgrid.gui.swing.SwingPane.class, "isForegroundSet", null ); // NOI18N
            properties[PROPERTY_graphics] = new PropertyDescriptor ( "graphics", squidpony.squidgrid.gui.swing.SwingPane.class, "getGraphics", null ); // NOI18N
            properties[PROPERTY_graphicsConfiguration] = new PropertyDescriptor ( "graphicsConfiguration", squidpony.squidgrid.gui.swing.SwingPane.class, "getGraphicsConfiguration", null ); // NOI18N
            properties[PROPERTY_gridHeight] = new PropertyDescriptor ( "gridHeight", squidpony.squidgrid.gui.swing.SwingPane.class, "getGridHeight", null ); // NOI18N
            properties[PROPERTY_gridWidth] = new PropertyDescriptor ( "gridWidth", squidpony.squidgrid.gui.swing.SwingPane.class, "getGridWidth", null ); // NOI18N
            properties[PROPERTY_height] = new PropertyDescriptor ( "height", squidpony.squidgrid.gui.swing.SwingPane.class, "getHeight", null ); // NOI18N
            properties[PROPERTY_hierarchyBoundsListeners] = new PropertyDescriptor ( "hierarchyBoundsListeners", squidpony.squidgrid.gui.swing.SwingPane.class, "getHierarchyBoundsListeners", null ); // NOI18N
            properties[PROPERTY_hierarchyListeners] = new PropertyDescriptor ( "hierarchyListeners", squidpony.squidgrid.gui.swing.SwingPane.class, "getHierarchyListeners", null ); // NOI18N
            properties[PROPERTY_ignoreRepaint] = new PropertyDescriptor ( "ignoreRepaint", squidpony.squidgrid.gui.swing.SwingPane.class, "getIgnoreRepaint", "setIgnoreRepaint" ); // NOI18N
            properties[PROPERTY_imageCellMap] = new PropertyDescriptor ( "imageCellMap", squidpony.squidgrid.gui.swing.SwingPane.class, "getImageCellMap", "setImageCellMap" ); // NOI18N
            properties[PROPERTY_inheritsPopupMenu] = new PropertyDescriptor ( "inheritsPopupMenu", squidpony.squidgrid.gui.swing.SwingPane.class, "getInheritsPopupMenu", "setInheritsPopupMenu" ); // NOI18N
            properties[PROPERTY_inputContext] = new PropertyDescriptor ( "inputContext", squidpony.squidgrid.gui.swing.SwingPane.class, "getInputContext", null ); // NOI18N
            properties[PROPERTY_inputMap] = new PropertyDescriptor ( "inputMap", squidpony.squidgrid.gui.swing.SwingPane.class, "getInputMap", null ); // NOI18N
            properties[PROPERTY_inputMethodListeners] = new PropertyDescriptor ( "inputMethodListeners", squidpony.squidgrid.gui.swing.SwingPane.class, "getInputMethodListeners", null ); // NOI18N
            properties[PROPERTY_inputMethodRequests] = new PropertyDescriptor ( "inputMethodRequests", squidpony.squidgrid.gui.swing.SwingPane.class, "getInputMethodRequests", null ); // NOI18N
            properties[PROPERTY_inputVerifier] = new PropertyDescriptor ( "inputVerifier", squidpony.squidgrid.gui.swing.SwingPane.class, "getInputVerifier", "setInputVerifier" ); // NOI18N
            properties[PROPERTY_insets] = new PropertyDescriptor ( "insets", squidpony.squidgrid.gui.swing.SwingPane.class, "getInsets", null ); // NOI18N
            properties[PROPERTY_keyListeners] = new PropertyDescriptor ( "keyListeners", squidpony.squidgrid.gui.swing.SwingPane.class, "getKeyListeners", null ); // NOI18N
            properties[PROPERTY_layout] = new PropertyDescriptor ( "layout", squidpony.squidgrid.gui.swing.SwingPane.class, "getLayout", "setLayout" ); // NOI18N
            properties[PROPERTY_lightweight] = new PropertyDescriptor ( "lightweight", squidpony.squidgrid.gui.swing.SwingPane.class, "isLightweight", null ); // NOI18N
            properties[PROPERTY_locale] = new PropertyDescriptor ( "locale", squidpony.squidgrid.gui.swing.SwingPane.class, "getLocale", "setLocale" ); // NOI18N
            properties[PROPERTY_location] = new PropertyDescriptor ( "location", squidpony.squidgrid.gui.swing.SwingPane.class, "getLocation", "setLocation" ); // NOI18N
            properties[PROPERTY_locationOnScreen] = new PropertyDescriptor ( "locationOnScreen", squidpony.squidgrid.gui.swing.SwingPane.class, "getLocationOnScreen", null ); // NOI18N
            properties[PROPERTY_managingFocus] = new PropertyDescriptor ( "managingFocus", squidpony.squidgrid.gui.swing.SwingPane.class, "isManagingFocus", null ); // NOI18N
            properties[PROPERTY_maximumSize] = new PropertyDescriptor ( "maximumSize", squidpony.squidgrid.gui.swing.SwingPane.class, "getMaximumSize", "setMaximumSize" ); // NOI18N
            properties[PROPERTY_maximumSizeSet] = new PropertyDescriptor ( "maximumSizeSet", squidpony.squidgrid.gui.swing.SwingPane.class, "isMaximumSizeSet", null ); // NOI18N
            properties[PROPERTY_minimumSize] = new PropertyDescriptor ( "minimumSize", squidpony.squidgrid.gui.swing.SwingPane.class, "getMinimumSize", "setMinimumSize" ); // NOI18N
            properties[PROPERTY_minimumSizeSet] = new PropertyDescriptor ( "minimumSizeSet", squidpony.squidgrid.gui.swing.SwingPane.class, "isMinimumSizeSet", null ); // NOI18N
            properties[PROPERTY_mouseListeners] = new PropertyDescriptor ( "mouseListeners", squidpony.squidgrid.gui.swing.SwingPane.class, "getMouseListeners", null ); // NOI18N
            properties[PROPERTY_mouseMotionListeners] = new PropertyDescriptor ( "mouseMotionListeners", squidpony.squidgrid.gui.swing.SwingPane.class, "getMouseMotionListeners", null ); // NOI18N
            properties[PROPERTY_mousePosition] = new PropertyDescriptor ( "mousePosition", squidpony.squidgrid.gui.swing.SwingPane.class, "getMousePosition", null ); // NOI18N
            properties[PROPERTY_mouseWheelListeners] = new PropertyDescriptor ( "mouseWheelListeners", squidpony.squidgrid.gui.swing.SwingPane.class, "getMouseWheelListeners", null ); // NOI18N
            properties[PROPERTY_name] = new PropertyDescriptor ( "name", squidpony.squidgrid.gui.swing.SwingPane.class, "getName", "setName" ); // NOI18N
            properties[PROPERTY_nextFocusableComponent] = new PropertyDescriptor ( "nextFocusableComponent", squidpony.squidgrid.gui.swing.SwingPane.class, "getNextFocusableComponent", "setNextFocusableComponent" ); // NOI18N
            properties[PROPERTY_opaque] = new PropertyDescriptor ( "opaque", squidpony.squidgrid.gui.swing.SwingPane.class, "isOpaque", "setOpaque" ); // NOI18N
            properties[PROPERTY_optimizedDrawingEnabled] = new PropertyDescriptor ( "optimizedDrawingEnabled", squidpony.squidgrid.gui.swing.SwingPane.class, "isOptimizedDrawingEnabled", null ); // NOI18N
            properties[PROPERTY_paintingForPrint] = new PropertyDescriptor ( "paintingForPrint", squidpony.squidgrid.gui.swing.SwingPane.class, "isPaintingForPrint", null ); // NOI18N
            properties[PROPERTY_paintingTile] = new PropertyDescriptor ( "paintingTile", squidpony.squidgrid.gui.swing.SwingPane.class, "isPaintingTile", null ); // NOI18N
            properties[PROPERTY_parent] = new PropertyDescriptor ( "parent", squidpony.squidgrid.gui.swing.SwingPane.class, "getParent", null ); // NOI18N
            properties[PROPERTY_peer] = new PropertyDescriptor ( "peer", squidpony.squidgrid.gui.swing.SwingPane.class, "getPeer", null ); // NOI18N
            properties[PROPERTY_preferredSize] = new PropertyDescriptor ( "preferredSize", squidpony.squidgrid.gui.swing.SwingPane.class, "getPreferredSize", "setPreferredSize" ); // NOI18N
            properties[PROPERTY_preferredSizeSet] = new PropertyDescriptor ( "preferredSizeSet", squidpony.squidgrid.gui.swing.SwingPane.class, "isPreferredSizeSet", null ); // NOI18N
            properties[PROPERTY_propertyChangeListeners] = new PropertyDescriptor ( "propertyChangeListeners", squidpony.squidgrid.gui.swing.SwingPane.class, "getPropertyChangeListeners", null ); // NOI18N
            properties[PROPERTY_registeredKeyStrokes] = new PropertyDescriptor ( "registeredKeyStrokes", squidpony.squidgrid.gui.swing.SwingPane.class, "getRegisteredKeyStrokes", null ); // NOI18N
            properties[PROPERTY_requestFocusEnabled] = new PropertyDescriptor ( "requestFocusEnabled", squidpony.squidgrid.gui.swing.SwingPane.class, "isRequestFocusEnabled", "setRequestFocusEnabled" ); // NOI18N
            properties[PROPERTY_rootPane] = new PropertyDescriptor ( "rootPane", squidpony.squidgrid.gui.swing.SwingPane.class, "getRootPane", null ); // NOI18N
            properties[PROPERTY_showing] = new PropertyDescriptor ( "showing", squidpony.squidgrid.gui.swing.SwingPane.class, "isShowing", null ); // NOI18N
            properties[PROPERTY_size] = new PropertyDescriptor ( "size", squidpony.squidgrid.gui.swing.SwingPane.class, "getSize", "setSize" ); // NOI18N
            properties[PROPERTY_text] = new PropertyDescriptor ( "text", squidpony.squidgrid.gui.swing.SwingPane.class, null, "setText" ); // NOI18N
            properties[PROPERTY_textCellFactory] = new PropertyDescriptor ( "textCellFactory", squidpony.squidgrid.gui.swing.SwingPane.class, null, "setTextCellFactory" ); // NOI18N
            properties[PROPERTY_textFactory] = new PropertyDescriptor ( "textFactory", squidpony.squidgrid.gui.swing.SwingPane.class, "getTextFactory", null ); // NOI18N
            properties[PROPERTY_toolkit] = new PropertyDescriptor ( "toolkit", squidpony.squidgrid.gui.swing.SwingPane.class, "getToolkit", null ); // NOI18N
            properties[PROPERTY_toolTipText] = new PropertyDescriptor ( "toolTipText", squidpony.squidgrid.gui.swing.SwingPane.class, "getToolTipText", "setToolTipText" ); // NOI18N
            properties[PROPERTY_topLevelAncestor] = new PropertyDescriptor ( "topLevelAncestor", squidpony.squidgrid.gui.swing.SwingPane.class, "getTopLevelAncestor", null ); // NOI18N
            properties[PROPERTY_transferHandler] = new PropertyDescriptor ( "transferHandler", squidpony.squidgrid.gui.swing.SwingPane.class, "getTransferHandler", "setTransferHandler" ); // NOI18N
            properties[PROPERTY_treeLock] = new PropertyDescriptor ( "treeLock", squidpony.squidgrid.gui.swing.SwingPane.class, "getTreeLock", null ); // NOI18N
            properties[PROPERTY_UIClassID] = new PropertyDescriptor ( "UIClassID", squidpony.squidgrid.gui.swing.SwingPane.class, "getUIClassID", null ); // NOI18N
            properties[PROPERTY_valid] = new PropertyDescriptor ( "valid", squidpony.squidgrid.gui.swing.SwingPane.class, "isValid", null ); // NOI18N
            properties[PROPERTY_validateRoot] = new PropertyDescriptor ( "validateRoot", squidpony.squidgrid.gui.swing.SwingPane.class, "isValidateRoot", null ); // NOI18N
            properties[PROPERTY_verifyInputWhenFocusTarget] = new PropertyDescriptor ( "verifyInputWhenFocusTarget", squidpony.squidgrid.gui.swing.SwingPane.class, "getVerifyInputWhenFocusTarget", "setVerifyInputWhenFocusTarget" ); // NOI18N
            properties[PROPERTY_vetoableChangeListeners] = new PropertyDescriptor ( "vetoableChangeListeners", squidpony.squidgrid.gui.swing.SwingPane.class, "getVetoableChangeListeners", null ); // NOI18N
            properties[PROPERTY_visible] = new PropertyDescriptor ( "visible", squidpony.squidgrid.gui.swing.SwingPane.class, "isVisible", "setVisible" ); // NOI18N
            properties[PROPERTY_visibleRect] = new PropertyDescriptor ( "visibleRect", squidpony.squidgrid.gui.swing.SwingPane.class, "getVisibleRect", null ); // NOI18N
            properties[PROPERTY_width] = new PropertyDescriptor ( "width", squidpony.squidgrid.gui.swing.SwingPane.class, "getWidth", null ); // NOI18N
            properties[PROPERTY_x] = new PropertyDescriptor ( "x", squidpony.squidgrid.gui.swing.SwingPane.class, "getX", null ); // NOI18N
            properties[PROPERTY_y] = new PropertyDescriptor ( "y", squidpony.squidgrid.gui.swing.SwingPane.class, "getY", null ); // NOI18N
        }
        catch(IntrospectionException e) {
            e.printStackTrace();
        }//GEN-HEADEREND:Properties

        // Here you can add code for customizing the properties array.

        return properties;     }//GEN-LAST:Properties
    // EventSet identifiers//GEN-FIRST:Events
    private static final int EVENT_ancestorListener = 0;
    private static final int EVENT_componentListener = 1;
    private static final int EVENT_containerListener = 2;
    private static final int EVENT_focusListener = 3;
    private static final int EVENT_hierarchyBoundsListener = 4;
    private static final int EVENT_hierarchyListener = 5;
    private static final int EVENT_inputMethodListener = 6;
    private static final int EVENT_keyListener = 7;
    private static final int EVENT_mouseListener = 8;
    private static final int EVENT_mouseMotionListener = 9;
    private static final int EVENT_mouseWheelListener = 10;
    private static final int EVENT_propertyChangeListener = 11;
    private static final int EVENT_vetoableChangeListener = 12;

    // EventSet array
    /*lazy EventSetDescriptor*/
    private static EventSetDescriptor[] getEdescriptor(){
        EventSetDescriptor[] eventSets = new EventSetDescriptor[13];
    
        try {
            eventSets[EVENT_ancestorListener] = new EventSetDescriptor ( squidpony.squidgrid.gui.swing.SwingPane.class, "ancestorListener", javax.swing.event.AncestorListener.class, new String[] {"ancestorAdded", "ancestorRemoved", "ancestorMoved"}, "addAncestorListener", "removeAncestorListener" ); // NOI18N
            eventSets[EVENT_componentListener] = new EventSetDescriptor ( squidpony.squidgrid.gui.swing.SwingPane.class, "componentListener", java.awt.event.ComponentListener.class, new String[] {"componentResized", "componentMoved", "componentShown", "componentHidden"}, "addComponentListener", "removeComponentListener" ); // NOI18N
            eventSets[EVENT_containerListener] = new EventSetDescriptor ( squidpony.squidgrid.gui.swing.SwingPane.class, "containerListener", java.awt.event.ContainerListener.class, new String[] {"componentAdded", "componentRemoved"}, "addContainerListener", "removeContainerListener" ); // NOI18N
            eventSets[EVENT_focusListener] = new EventSetDescriptor ( squidpony.squidgrid.gui.swing.SwingPane.class, "focusListener", java.awt.event.FocusListener.class, new String[] {"focusGained", "focusLost"}, "addFocusListener", "removeFocusListener" ); // NOI18N
            eventSets[EVENT_hierarchyBoundsListener] = new EventSetDescriptor ( squidpony.squidgrid.gui.swing.SwingPane.class, "hierarchyBoundsListener", java.awt.event.HierarchyBoundsListener.class, new String[] {"ancestorMoved", "ancestorResized"}, "addHierarchyBoundsListener", "removeHierarchyBoundsListener" ); // NOI18N
            eventSets[EVENT_hierarchyListener] = new EventSetDescriptor ( squidpony.squidgrid.gui.swing.SwingPane.class, "hierarchyListener", java.awt.event.HierarchyListener.class, new String[] {"hierarchyChanged"}, "addHierarchyListener", "removeHierarchyListener" ); // NOI18N
            eventSets[EVENT_inputMethodListener] = new EventSetDescriptor ( squidpony.squidgrid.gui.swing.SwingPane.class, "inputMethodListener", java.awt.event.InputMethodListener.class, new String[] {"inputMethodTextChanged", "caretPositionChanged"}, "addInputMethodListener", "removeInputMethodListener" ); // NOI18N
            eventSets[EVENT_keyListener] = new EventSetDescriptor ( squidpony.squidgrid.gui.swing.SwingPane.class, "keyListener", java.awt.event.KeyListener.class, new String[] {"keyTyped", "keyPressed", "keyReleased"}, "addKeyListener", "removeKeyListener" ); // NOI18N
            eventSets[EVENT_mouseListener] = new EventSetDescriptor ( squidpony.squidgrid.gui.swing.SwingPane.class, "mouseListener", java.awt.event.MouseListener.class, new String[] {"mouseClicked", "mousePressed", "mouseReleased", "mouseEntered", "mouseExited"}, "addMouseListener", "removeMouseListener" ); // NOI18N
            eventSets[EVENT_mouseMotionListener] = new EventSetDescriptor ( squidpony.squidgrid.gui.swing.SwingPane.class, "mouseMotionListener", java.awt.event.MouseMotionListener.class, new String[] {"mouseDragged", "mouseMoved"}, "addMouseMotionListener", "removeMouseMotionListener" ); // NOI18N
            eventSets[EVENT_mouseWheelListener] = new EventSetDescriptor ( squidpony.squidgrid.gui.swing.SwingPane.class, "mouseWheelListener", java.awt.event.MouseWheelListener.class, new String[] {"mouseWheelMoved"}, "addMouseWheelListener", "removeMouseWheelListener" ); // NOI18N
            eventSets[EVENT_propertyChangeListener] = new EventSetDescriptor ( squidpony.squidgrid.gui.swing.SwingPane.class, "propertyChangeListener", java.beans.PropertyChangeListener.class, new String[] {"propertyChange"}, "addPropertyChangeListener", "removePropertyChangeListener" ); // NOI18N
            eventSets[EVENT_vetoableChangeListener] = new EventSetDescriptor ( squidpony.squidgrid.gui.swing.SwingPane.class, "vetoableChangeListener", java.beans.VetoableChangeListener.class, new String[] {"vetoableChange"}, "addVetoableChangeListener", "removeVetoableChangeListener" ); // NOI18N
        }
        catch(IntrospectionException e) {
            e.printStackTrace();
        }//GEN-HEADEREND:Events

        // Here you can add code for customizing the event sets array.

        return eventSets;     }//GEN-LAST:Events
    // Method identifiers//GEN-FIRST:Methods
    private static final int METHOD_action0 = 0;
    private static final int METHOD_add1 = 1;
    private static final int METHOD_add2 = 2;
    private static final int METHOD_add3 = 3;
    private static final int METHOD_add4 = 4;
    private static final int METHOD_add5 = 5;
    private static final int METHOD_add6 = 6;
    private static final int METHOD_addNotify7 = 7;
    private static final int METHOD_addPropertyChangeListener8 = 8;
    private static final int METHOD_applyComponentOrientation9 = 9;
    private static final int METHOD_areFocusTraversalKeysSet10 = 10;
    private static final int METHOD_bounds11 = 11;
    private static final int METHOD_bump12 = 12;
    private static final int METHOD_bump13 = 13;
    private static final int METHOD_checkImage14 = 14;
    private static final int METHOD_checkImage15 = 15;
    private static final int METHOD_clearCell16 = 16;
    private static final int METHOD_clearCell17 = 17;
    private static final int METHOD_computeVisibleRect18 = 18;
    private static final int METHOD_contains19 = 19;
    private static final int METHOD_contains20 = 20;
    private static final int METHOD_countComponents21 = 21;
    private static final int METHOD_createImage22 = 22;
    private static final int METHOD_createImage23 = 23;
    private static final int METHOD_createToolTip24 = 24;
    private static final int METHOD_createVolatileImage25 = 25;
    private static final int METHOD_createVolatileImage26 = 26;
    private static final int METHOD_deliverEvent27 = 27;
    private static final int METHOD_disable28 = 28;
    private static final int METHOD_dispatchEvent29 = 29;
    private static final int METHOD_doLayout30 = 30;
    private static final int METHOD_enable31 = 31;
    private static final int METHOD_enable32 = 32;
    private static final int METHOD_enableInputMethods33 = 33;
    private static final int METHOD_ensureFits34 = 34;
    private static final int METHOD_findComponentAt35 = 35;
    private static final int METHOD_findComponentAt36 = 36;
    private static final int METHOD_firePropertyChange37 = 37;
    private static final int METHOD_firePropertyChange38 = 38;
    private static final int METHOD_firePropertyChange39 = 39;
    private static final int METHOD_firePropertyChange40 = 40;
    private static final int METHOD_firePropertyChange41 = 41;
    private static final int METHOD_firePropertyChange42 = 42;
    private static final int METHOD_firePropertyChange43 = 43;
    private static final int METHOD_firePropertyChange44 = 44;
    private static final int METHOD_getActionForKeyStroke45 = 45;
    private static final int METHOD_getBaseline46 = 46;
    private static final int METHOD_getBounds47 = 47;
    private static final int METHOD_getClientProperty48 = 48;
    private static final int METHOD_getComponentAt49 = 49;
    private static final int METHOD_getComponentAt50 = 50;
    private static final int METHOD_getComponentZOrder51 = 51;
    private static final int METHOD_getConditionForKeyStroke52 = 52;
    private static final int METHOD_getDefaultLocale53 = 53;
    private static final int METHOD_getFocusTraversalKeys54 = 54;
    private static final int METHOD_getFontMetrics55 = 55;
    private static final int METHOD_getIndexOf56 = 56;
    private static final int METHOD_getInsets57 = 57;
    private static final int METHOD_getLayer58 = 58;
    private static final int METHOD_getLayer59 = 59;
    private static final int METHOD_getLayeredPaneAbove60 = 60;
    private static final int METHOD_getListeners61 = 61;
    private static final int METHOD_getLocation62 = 62;
    private static final int METHOD_getMousePosition63 = 63;
    private static final int METHOD_getPopupLocation64 = 64;
    private static final int METHOD_getPosition65 = 65;
    private static final int METHOD_getPropertyChangeListeners66 = 66;
    private static final int METHOD_getSize67 = 67;
    private static final int METHOD_getToolTipLocation68 = 68;
    private static final int METHOD_getToolTipText69 = 69;
    private static final int METHOD_gotFocus70 = 70;
    private static final int METHOD_grabFocus71 = 71;
    private static final int METHOD_handleEvent72 = 72;
    private static final int METHOD_hasFocus73 = 73;
    private static final int METHOD_hide74 = 74;
    private static final int METHOD_highestLayer75 = 75;
    private static final int METHOD_imageUpdate76 = 76;
    private static final int METHOD_initialize77 = 77;
    private static final int METHOD_initialize78 = 78;
    private static final int METHOD_insets79 = 79;
    private static final int METHOD_inside80 = 80;
    private static final int METHOD_invalidate81 = 81;
    private static final int METHOD_isAncestorOf82 = 82;
    private static final int METHOD_isFocusCycleRoot83 = 83;
    private static final int METHOD_isLightweightComponent84 = 84;
    private static final int METHOD_keyDown85 = 85;
    private static final int METHOD_keyUp86 = 86;
    private static final int METHOD_layout87 = 87;
    private static final int METHOD_list88 = 88;
    private static final int METHOD_list89 = 89;
    private static final int METHOD_list90 = 90;
    private static final int METHOD_list91 = 91;
    private static final int METHOD_list92 = 92;
    private static final int METHOD_locate93 = 93;
    private static final int METHOD_location94 = 94;
    private static final int METHOD_lostFocus95 = 95;
    private static final int METHOD_lowestLayer96 = 96;
    private static final int METHOD_minimumSize97 = 97;
    private static final int METHOD_mouseDown98 = 98;
    private static final int METHOD_mouseDrag99 = 99;
    private static final int METHOD_mouseEnter100 = 100;
    private static final int METHOD_mouseExit101 = 101;
    private static final int METHOD_mouseMove102 = 102;
    private static final int METHOD_mouseUp103 = 103;
    private static final int METHOD_move104 = 104;
    private static final int METHOD_moveToBack105 = 105;
    private static final int METHOD_moveToFront106 = 106;
    private static final int METHOD_nextFocus107 = 107;
    private static final int METHOD_paint108 = 108;
    private static final int METHOD_paintAll109 = 109;
    private static final int METHOD_paintComponent110 = 110;
    private static final int METHOD_paintComponents111 = 111;
    private static final int METHOD_paintImmediately112 = 112;
    private static final int METHOD_paintImmediately113 = 113;
    private static final int METHOD_placeCharacter114 = 114;
    private static final int METHOD_placeCharacter115 = 115;
    private static final int METHOD_placeCharacter116 = 116;
    private static final int METHOD_placeHorizontalString117 = 117;
    private static final int METHOD_placeHorizontalString118 = 118;
    private static final int METHOD_placeImage119 = 119;
    private static final int METHOD_placeImage120 = 120;
    private static final int METHOD_placeText121 = 121;
    private static final int METHOD_placeText122 = 122;
    private static final int METHOD_placeVerticalString123 = 123;
    private static final int METHOD_placeVerticalString124 = 124;
    private static final int METHOD_postEvent125 = 125;
    private static final int METHOD_preferredSize126 = 126;
    private static final int METHOD_prepareImage127 = 127;
    private static final int METHOD_prepareImage128 = 128;
    private static final int METHOD_print129 = 129;
    private static final int METHOD_printAll130 = 130;
    private static final int METHOD_printComponents131 = 131;
    private static final int METHOD_putClientProperty132 = 132;
    private static final int METHOD_putLayer133 = 133;
    private static final int METHOD_refresh134 = 134;
    private static final int METHOD_registerKeyboardAction135 = 135;
    private static final int METHOD_registerKeyboardAction136 = 136;
    private static final int METHOD_remove137 = 137;
    private static final int METHOD_remove138 = 138;
    private static final int METHOD_remove139 = 139;
    private static final int METHOD_removeAll140 = 140;
    private static final int METHOD_removeNotify141 = 141;
    private static final int METHOD_removePropertyChangeListener142 = 142;
    private static final int METHOD_repaint143 = 143;
    private static final int METHOD_repaint144 = 144;
    private static final int METHOD_repaint145 = 145;
    private static final int METHOD_repaint146 = 146;
    private static final int METHOD_repaint147 = 147;
    private static final int METHOD_requestDefaultFocus148 = 148;
    private static final int METHOD_requestFocus149 = 149;
    private static final int METHOD_requestFocus150 = 150;
    private static final int METHOD_requestFocusInWindow151 = 151;
    private static final int METHOD_resetKeyboardActions152 = 152;
    private static final int METHOD_reshape153 = 153;
    private static final int METHOD_resize154 = 154;
    private static final int METHOD_resize155 = 155;
    private static final int METHOD_revalidate156 = 156;
    private static final int METHOD_scrollRectToVisible157 = 157;
    private static final int METHOD_setBounds158 = 158;
    private static final int METHOD_setComponentZOrder159 = 159;
    private static final int METHOD_setDefaultLocale160 = 160;
    private static final int METHOD_setLayer161 = 161;
    private static final int METHOD_setLayer162 = 162;
    private static final int METHOD_setPosition163 = 163;
    private static final int METHOD_show164 = 164;
    private static final int METHOD_show165 = 165;
    private static final int METHOD_size166 = 166;
    private static final int METHOD_slide167 = 167;
    private static final int METHOD_slide168 = 168;
    private static final int METHOD_slide169 = 169;
    private static final int METHOD_toString170 = 170;
    private static final int METHOD_transferFocus171 = 171;
    private static final int METHOD_transferFocusBackward172 = 172;
    private static final int METHOD_transferFocusDownCycle173 = 173;
    private static final int METHOD_transferFocusUpCycle174 = 174;
    private static final int METHOD_unregisterKeyboardAction175 = 175;
    private static final int METHOD_update176 = 176;
    private static final int METHOD_updateUI177 = 177;
    private static final int METHOD_validate178 = 178;
    private static final int METHOD_waitForAnimations179 = 179;
    private static final int METHOD_wiggle180 = 180;
    private static final int METHOD_willFit181 = 181;

    // Method array 
    /*lazy MethodDescriptor*/
    private static MethodDescriptor[] getMdescriptor(){
        MethodDescriptor[] methods = new MethodDescriptor[182];
    
        try {
            methods[METHOD_action0] = new MethodDescriptor(java.awt.Component.class.getMethod("action", new Class[] {java.awt.Event.class, java.lang.Object.class})); // NOI18N
            methods[METHOD_action0].setHidden ( true );
            methods[METHOD_action0].setDisplayName ( "" );
            methods[METHOD_add1] = new MethodDescriptor(java.awt.Component.class.getMethod("add", new Class[] {java.awt.PopupMenu.class})); // NOI18N
            methods[METHOD_add1].setHidden ( true );
            methods[METHOD_add1].setDisplayName ( "" );
            methods[METHOD_add2] = new MethodDescriptor(java.awt.Container.class.getMethod("add", new Class[] {java.awt.Component.class})); // NOI18N
            methods[METHOD_add2].setHidden ( true );
            methods[METHOD_add2].setDisplayName ( "" );
            methods[METHOD_add3] = new MethodDescriptor(java.awt.Container.class.getMethod("add", new Class[] {java.lang.String.class, java.awt.Component.class})); // NOI18N
            methods[METHOD_add3].setHidden ( true );
            methods[METHOD_add3].setDisplayName ( "" );
            methods[METHOD_add4] = new MethodDescriptor(java.awt.Container.class.getMethod("add", new Class[] {java.awt.Component.class, int.class})); // NOI18N
            methods[METHOD_add4].setHidden ( true );
            methods[METHOD_add4].setDisplayName ( "" );
            methods[METHOD_add5] = new MethodDescriptor(java.awt.Container.class.getMethod("add", new Class[] {java.awt.Component.class, java.lang.Object.class})); // NOI18N
            methods[METHOD_add5].setHidden ( true );
            methods[METHOD_add5].setDisplayName ( "" );
            methods[METHOD_add6] = new MethodDescriptor(java.awt.Container.class.getMethod("add", new Class[] {java.awt.Component.class, java.lang.Object.class, int.class})); // NOI18N
            methods[METHOD_add6].setHidden ( true );
            methods[METHOD_add6].setDisplayName ( "" );
            methods[METHOD_addNotify7] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("addNotify", new Class[] {})); // NOI18N
            methods[METHOD_addNotify7].setHidden ( true );
            methods[METHOD_addNotify7].setDisplayName ( "" );
            methods[METHOD_addPropertyChangeListener8] = new MethodDescriptor(java.awt.Container.class.getMethod("addPropertyChangeListener", new Class[] {java.lang.String.class, java.beans.PropertyChangeListener.class})); // NOI18N
            methods[METHOD_addPropertyChangeListener8].setHidden ( true );
            methods[METHOD_addPropertyChangeListener8].setDisplayName ( "" );
            methods[METHOD_applyComponentOrientation9] = new MethodDescriptor(java.awt.Container.class.getMethod("applyComponentOrientation", new Class[] {java.awt.ComponentOrientation.class})); // NOI18N
            methods[METHOD_applyComponentOrientation9].setHidden ( true );
            methods[METHOD_applyComponentOrientation9].setDisplayName ( "" );
            methods[METHOD_areFocusTraversalKeysSet10] = new MethodDescriptor(java.awt.Container.class.getMethod("areFocusTraversalKeysSet", new Class[] {int.class})); // NOI18N
            methods[METHOD_areFocusTraversalKeysSet10].setHidden ( true );
            methods[METHOD_areFocusTraversalKeysSet10].setDisplayName ( "" );
            methods[METHOD_bounds11] = new MethodDescriptor(java.awt.Component.class.getMethod("bounds", new Class[] {})); // NOI18N
            methods[METHOD_bounds11].setDisplayName ( "" );
            methods[METHOD_bump12] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("bump", new Class[] {java.awt.Point.class, squidpony.squidgrid.util.Direction.class})); // NOI18N
            methods[METHOD_bump12].setDisplayName ( "" );
            methods[METHOD_bump13] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("bump", new Class[] {java.awt.Point.class, java.awt.Point.class})); // NOI18N
            methods[METHOD_bump13].setDisplayName ( "" );
            methods[METHOD_checkImage14] = new MethodDescriptor(java.awt.Component.class.getMethod("checkImage", new Class[] {java.awt.Image.class, java.awt.image.ImageObserver.class})); // NOI18N
            methods[METHOD_checkImage14].setHidden ( true );
            methods[METHOD_checkImage14].setDisplayName ( "" );
            methods[METHOD_checkImage15] = new MethodDescriptor(java.awt.Component.class.getMethod("checkImage", new Class[] {java.awt.Image.class, int.class, int.class, java.awt.image.ImageObserver.class})); // NOI18N
            methods[METHOD_checkImage15].setHidden ( true );
            methods[METHOD_checkImage15].setDisplayName ( "" );
            methods[METHOD_clearCell16] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("clearCell", new Class[] {int.class, int.class})); // NOI18N
            methods[METHOD_clearCell16].setDisplayName ( "" );
            methods[METHOD_clearCell17] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("clearCell", new Class[] {int.class, int.class, java.awt.Color.class})); // NOI18N
            methods[METHOD_clearCell17].setDisplayName ( "" );
            methods[METHOD_computeVisibleRect18] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("computeVisibleRect", new Class[] {java.awt.Rectangle.class})); // NOI18N
            methods[METHOD_computeVisibleRect18].setDisplayName ( "" );
            methods[METHOD_contains19] = new MethodDescriptor(java.awt.Component.class.getMethod("contains", new Class[] {java.awt.Point.class})); // NOI18N
            methods[METHOD_contains19].setDisplayName ( "" );
            methods[METHOD_contains20] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("contains", new Class[] {int.class, int.class})); // NOI18N
            methods[METHOD_contains20].setDisplayName ( "" );
            methods[METHOD_countComponents21] = new MethodDescriptor(java.awt.Container.class.getMethod("countComponents", new Class[] {})); // NOI18N
            methods[METHOD_countComponents21].setDisplayName ( "" );
            methods[METHOD_createImage22] = new MethodDescriptor(java.awt.Component.class.getMethod("createImage", new Class[] {java.awt.image.ImageProducer.class})); // NOI18N
            methods[METHOD_createImage22].setDisplayName ( "" );
            methods[METHOD_createImage23] = new MethodDescriptor(java.awt.Component.class.getMethod("createImage", new Class[] {int.class, int.class})); // NOI18N
            methods[METHOD_createImage23].setDisplayName ( "" );
            methods[METHOD_createToolTip24] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("createToolTip", new Class[] {})); // NOI18N
            methods[METHOD_createToolTip24].setDisplayName ( "" );
            methods[METHOD_createVolatileImage25] = new MethodDescriptor(java.awt.Component.class.getMethod("createVolatileImage", new Class[] {int.class, int.class})); // NOI18N
            methods[METHOD_createVolatileImage25].setDisplayName ( "" );
            methods[METHOD_createVolatileImage26] = new MethodDescriptor(java.awt.Component.class.getMethod("createVolatileImage", new Class[] {int.class, int.class, java.awt.ImageCapabilities.class})); // NOI18N
            methods[METHOD_createVolatileImage26].setDisplayName ( "" );
            methods[METHOD_deliverEvent27] = new MethodDescriptor(java.awt.Container.class.getMethod("deliverEvent", new Class[] {java.awt.Event.class})); // NOI18N
            methods[METHOD_deliverEvent27].setDisplayName ( "" );
            methods[METHOD_disable28] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("disable", new Class[] {})); // NOI18N
            methods[METHOD_disable28].setDisplayName ( "" );
            methods[METHOD_dispatchEvent29] = new MethodDescriptor(java.awt.Component.class.getMethod("dispatchEvent", new Class[] {java.awt.AWTEvent.class})); // NOI18N
            methods[METHOD_dispatchEvent29].setDisplayName ( "" );
            methods[METHOD_doLayout30] = new MethodDescriptor(java.awt.Container.class.getMethod("doLayout", new Class[] {})); // NOI18N
            methods[METHOD_doLayout30].setDisplayName ( "" );
            methods[METHOD_enable31] = new MethodDescriptor(java.awt.Component.class.getMethod("enable", new Class[] {boolean.class})); // NOI18N
            methods[METHOD_enable31].setDisplayName ( "" );
            methods[METHOD_enable32] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("enable", new Class[] {})); // NOI18N
            methods[METHOD_enable32].setDisplayName ( "" );
            methods[METHOD_enableInputMethods33] = new MethodDescriptor(java.awt.Component.class.getMethod("enableInputMethods", new Class[] {boolean.class})); // NOI18N
            methods[METHOD_enableInputMethods33].setDisplayName ( "" );
            methods[METHOD_ensureFits34] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("ensureFits", new Class[] {char[].class})); // NOI18N
            methods[METHOD_ensureFits34].setDisplayName ( "" );
            methods[METHOD_findComponentAt35] = new MethodDescriptor(java.awt.Container.class.getMethod("findComponentAt", new Class[] {int.class, int.class})); // NOI18N
            methods[METHOD_findComponentAt35].setDisplayName ( "" );
            methods[METHOD_findComponentAt36] = new MethodDescriptor(java.awt.Container.class.getMethod("findComponentAt", new Class[] {java.awt.Point.class})); // NOI18N
            methods[METHOD_findComponentAt36].setDisplayName ( "" );
            methods[METHOD_firePropertyChange37] = new MethodDescriptor(java.awt.Component.class.getMethod("firePropertyChange", new Class[] {java.lang.String.class, byte.class, byte.class})); // NOI18N
            methods[METHOD_firePropertyChange37].setDisplayName ( "" );
            methods[METHOD_firePropertyChange38] = new MethodDescriptor(java.awt.Component.class.getMethod("firePropertyChange", new Class[] {java.lang.String.class, short.class, short.class})); // NOI18N
            methods[METHOD_firePropertyChange38].setDisplayName ( "" );
            methods[METHOD_firePropertyChange39] = new MethodDescriptor(java.awt.Component.class.getMethod("firePropertyChange", new Class[] {java.lang.String.class, long.class, long.class})); // NOI18N
            methods[METHOD_firePropertyChange39].setDisplayName ( "" );
            methods[METHOD_firePropertyChange40] = new MethodDescriptor(java.awt.Component.class.getMethod("firePropertyChange", new Class[] {java.lang.String.class, float.class, float.class})); // NOI18N
            methods[METHOD_firePropertyChange40].setDisplayName ( "" );
            methods[METHOD_firePropertyChange41] = new MethodDescriptor(java.awt.Component.class.getMethod("firePropertyChange", new Class[] {java.lang.String.class, double.class, double.class})); // NOI18N
            methods[METHOD_firePropertyChange41].setDisplayName ( "" );
            methods[METHOD_firePropertyChange42] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("firePropertyChange", new Class[] {java.lang.String.class, boolean.class, boolean.class})); // NOI18N
            methods[METHOD_firePropertyChange42].setDisplayName ( "" );
            methods[METHOD_firePropertyChange43] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("firePropertyChange", new Class[] {java.lang.String.class, int.class, int.class})); // NOI18N
            methods[METHOD_firePropertyChange43].setDisplayName ( "" );
            methods[METHOD_firePropertyChange44] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("firePropertyChange", new Class[] {java.lang.String.class, char.class, char.class})); // NOI18N
            methods[METHOD_firePropertyChange44].setDisplayName ( "" );
            methods[METHOD_getActionForKeyStroke45] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getActionForKeyStroke", new Class[] {javax.swing.KeyStroke.class})); // NOI18N
            methods[METHOD_getActionForKeyStroke45].setDisplayName ( "" );
            methods[METHOD_getBaseline46] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getBaseline", new Class[] {int.class, int.class})); // NOI18N
            methods[METHOD_getBaseline46].setDisplayName ( "" );
            methods[METHOD_getBounds47] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getBounds", new Class[] {java.awt.Rectangle.class})); // NOI18N
            methods[METHOD_getBounds47].setDisplayName ( "" );
            methods[METHOD_getClientProperty48] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getClientProperty", new Class[] {java.lang.Object.class})); // NOI18N
            methods[METHOD_getClientProperty48].setDisplayName ( "" );
            methods[METHOD_getComponentAt49] = new MethodDescriptor(java.awt.Container.class.getMethod("getComponentAt", new Class[] {int.class, int.class})); // NOI18N
            methods[METHOD_getComponentAt49].setDisplayName ( "" );
            methods[METHOD_getComponentAt50] = new MethodDescriptor(java.awt.Container.class.getMethod("getComponentAt", new Class[] {java.awt.Point.class})); // NOI18N
            methods[METHOD_getComponentAt50].setDisplayName ( "" );
            methods[METHOD_getComponentZOrder51] = new MethodDescriptor(java.awt.Container.class.getMethod("getComponentZOrder", new Class[] {java.awt.Component.class})); // NOI18N
            methods[METHOD_getComponentZOrder51].setDisplayName ( "" );
            methods[METHOD_getConditionForKeyStroke52] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getConditionForKeyStroke", new Class[] {javax.swing.KeyStroke.class})); // NOI18N
            methods[METHOD_getConditionForKeyStroke52].setDisplayName ( "" );
            methods[METHOD_getDefaultLocale53] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getDefaultLocale", new Class[] {})); // NOI18N
            methods[METHOD_getDefaultLocale53].setDisplayName ( "" );
            methods[METHOD_getFocusTraversalKeys54] = new MethodDescriptor(java.awt.Container.class.getMethod("getFocusTraversalKeys", new Class[] {int.class})); // NOI18N
            methods[METHOD_getFocusTraversalKeys54].setDisplayName ( "" );
            methods[METHOD_getFontMetrics55] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getFontMetrics", new Class[] {java.awt.Font.class})); // NOI18N
            methods[METHOD_getFontMetrics55].setDisplayName ( "" );
            methods[METHOD_getIndexOf56] = new MethodDescriptor(javax.swing.JLayeredPane.class.getMethod("getIndexOf", new Class[] {java.awt.Component.class})); // NOI18N
            methods[METHOD_getIndexOf56].setDisplayName ( "" );
            methods[METHOD_getInsets57] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getInsets", new Class[] {java.awt.Insets.class})); // NOI18N
            methods[METHOD_getInsets57].setDisplayName ( "" );
            methods[METHOD_getLayer58] = new MethodDescriptor(javax.swing.JLayeredPane.class.getMethod("getLayer", new Class[] {javax.swing.JComponent.class})); // NOI18N
            methods[METHOD_getLayer58].setDisplayName ( "" );
            methods[METHOD_getLayer59] = new MethodDescriptor(javax.swing.JLayeredPane.class.getMethod("getLayer", new Class[] {java.awt.Component.class})); // NOI18N
            methods[METHOD_getLayer59].setDisplayName ( "" );
            methods[METHOD_getLayeredPaneAbove60] = new MethodDescriptor(javax.swing.JLayeredPane.class.getMethod("getLayeredPaneAbove", new Class[] {java.awt.Component.class})); // NOI18N
            methods[METHOD_getLayeredPaneAbove60].setDisplayName ( "" );
            methods[METHOD_getListeners61] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getListeners", new Class[] {java.lang.Class.class})); // NOI18N
            methods[METHOD_getListeners61].setDisplayName ( "" );
            methods[METHOD_getLocation62] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getLocation", new Class[] {java.awt.Point.class})); // NOI18N
            methods[METHOD_getLocation62].setDisplayName ( "" );
            methods[METHOD_getMousePosition63] = new MethodDescriptor(java.awt.Container.class.getMethod("getMousePosition", new Class[] {boolean.class})); // NOI18N
            methods[METHOD_getMousePosition63].setDisplayName ( "" );
            methods[METHOD_getPopupLocation64] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getPopupLocation", new Class[] {java.awt.event.MouseEvent.class})); // NOI18N
            methods[METHOD_getPopupLocation64].setDisplayName ( "" );
            methods[METHOD_getPosition65] = new MethodDescriptor(javax.swing.JLayeredPane.class.getMethod("getPosition", new Class[] {java.awt.Component.class})); // NOI18N
            methods[METHOD_getPosition65].setDisplayName ( "" );
            methods[METHOD_getPropertyChangeListeners66] = new MethodDescriptor(java.awt.Component.class.getMethod("getPropertyChangeListeners", new Class[] {java.lang.String.class})); // NOI18N
            methods[METHOD_getPropertyChangeListeners66].setDisplayName ( "" );
            methods[METHOD_getSize67] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getSize", new Class[] {java.awt.Dimension.class})); // NOI18N
            methods[METHOD_getSize67].setDisplayName ( "" );
            methods[METHOD_getToolTipLocation68] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getToolTipLocation", new Class[] {java.awt.event.MouseEvent.class})); // NOI18N
            methods[METHOD_getToolTipLocation68].setDisplayName ( "" );
            methods[METHOD_getToolTipText69] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("getToolTipText", new Class[] {java.awt.event.MouseEvent.class})); // NOI18N
            methods[METHOD_getToolTipText69].setDisplayName ( "" );
            methods[METHOD_gotFocus70] = new MethodDescriptor(java.awt.Component.class.getMethod("gotFocus", new Class[] {java.awt.Event.class, java.lang.Object.class})); // NOI18N
            methods[METHOD_gotFocus70].setDisplayName ( "" );
            methods[METHOD_grabFocus71] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("grabFocus", new Class[] {})); // NOI18N
            methods[METHOD_grabFocus71].setDisplayName ( "" );
            methods[METHOD_handleEvent72] = new MethodDescriptor(java.awt.Component.class.getMethod("handleEvent", new Class[] {java.awt.Event.class})); // NOI18N
            methods[METHOD_handleEvent72].setDisplayName ( "" );
            methods[METHOD_hasFocus73] = new MethodDescriptor(java.awt.Component.class.getMethod("hasFocus", new Class[] {})); // NOI18N
            methods[METHOD_hasFocus73].setDisplayName ( "" );
            methods[METHOD_hide74] = new MethodDescriptor(java.awt.Component.class.getMethod("hide", new Class[] {})); // NOI18N
            methods[METHOD_hide74].setDisplayName ( "" );
            methods[METHOD_highestLayer75] = new MethodDescriptor(javax.swing.JLayeredPane.class.getMethod("highestLayer", new Class[] {})); // NOI18N
            methods[METHOD_highestLayer75].setDisplayName ( "" );
            methods[METHOD_imageUpdate76] = new MethodDescriptor(java.awt.Component.class.getMethod("imageUpdate", new Class[] {java.awt.Image.class, int.class, int.class, int.class, int.class, int.class})); // NOI18N
            methods[METHOD_imageUpdate76].setDisplayName ( "" );
            methods[METHOD_initialize77] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("initialize", new Class[] {int.class, int.class, int.class, int.class, java.awt.Font.class})); // NOI18N
            methods[METHOD_initialize77].setDisplayName ( "" );
            methods[METHOD_initialize78] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("initialize", new Class[] {int.class, int.class, java.awt.Font.class})); // NOI18N
            methods[METHOD_initialize78].setDisplayName ( "" );
            methods[METHOD_insets79] = new MethodDescriptor(java.awt.Container.class.getMethod("insets", new Class[] {})); // NOI18N
            methods[METHOD_insets79].setDisplayName ( "" );
            methods[METHOD_inside80] = new MethodDescriptor(java.awt.Component.class.getMethod("inside", new Class[] {int.class, int.class})); // NOI18N
            methods[METHOD_inside80].setDisplayName ( "" );
            methods[METHOD_invalidate81] = new MethodDescriptor(java.awt.Container.class.getMethod("invalidate", new Class[] {})); // NOI18N
            methods[METHOD_invalidate81].setDisplayName ( "" );
            methods[METHOD_isAncestorOf82] = new MethodDescriptor(java.awt.Container.class.getMethod("isAncestorOf", new Class[] {java.awt.Component.class})); // NOI18N
            methods[METHOD_isAncestorOf82].setDisplayName ( "" );
            methods[METHOD_isFocusCycleRoot83] = new MethodDescriptor(java.awt.Container.class.getMethod("isFocusCycleRoot", new Class[] {java.awt.Container.class})); // NOI18N
            methods[METHOD_isFocusCycleRoot83].setDisplayName ( "" );
            methods[METHOD_isLightweightComponent84] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("isLightweightComponent", new Class[] {java.awt.Component.class})); // NOI18N
            methods[METHOD_isLightweightComponent84].setDisplayName ( "" );
            methods[METHOD_keyDown85] = new MethodDescriptor(java.awt.Component.class.getMethod("keyDown", new Class[] {java.awt.Event.class, int.class})); // NOI18N
            methods[METHOD_keyDown85].setDisplayName ( "" );
            methods[METHOD_keyUp86] = new MethodDescriptor(java.awt.Component.class.getMethod("keyUp", new Class[] {java.awt.Event.class, int.class})); // NOI18N
            methods[METHOD_keyUp86].setDisplayName ( "" );
            methods[METHOD_layout87] = new MethodDescriptor(java.awt.Container.class.getMethod("layout", new Class[] {})); // NOI18N
            methods[METHOD_layout87].setDisplayName ( "" );
            methods[METHOD_list88] = new MethodDescriptor(java.awt.Component.class.getMethod("list", new Class[] {})); // NOI18N
            methods[METHOD_list88].setDisplayName ( "" );
            methods[METHOD_list89] = new MethodDescriptor(java.awt.Component.class.getMethod("list", new Class[] {java.io.PrintStream.class})); // NOI18N
            methods[METHOD_list89].setDisplayName ( "" );
            methods[METHOD_list90] = new MethodDescriptor(java.awt.Component.class.getMethod("list", new Class[] {java.io.PrintWriter.class})); // NOI18N
            methods[METHOD_list90].setDisplayName ( "" );
            methods[METHOD_list91] = new MethodDescriptor(java.awt.Container.class.getMethod("list", new Class[] {java.io.PrintStream.class, int.class})); // NOI18N
            methods[METHOD_list91].setDisplayName ( "" );
            methods[METHOD_list92] = new MethodDescriptor(java.awt.Container.class.getMethod("list", new Class[] {java.io.PrintWriter.class, int.class})); // NOI18N
            methods[METHOD_list92].setDisplayName ( "" );
            methods[METHOD_locate93] = new MethodDescriptor(java.awt.Container.class.getMethod("locate", new Class[] {int.class, int.class})); // NOI18N
            methods[METHOD_locate93].setDisplayName ( "" );
            methods[METHOD_location94] = new MethodDescriptor(java.awt.Component.class.getMethod("location", new Class[] {})); // NOI18N
            methods[METHOD_location94].setDisplayName ( "" );
            methods[METHOD_lostFocus95] = new MethodDescriptor(java.awt.Component.class.getMethod("lostFocus", new Class[] {java.awt.Event.class, java.lang.Object.class})); // NOI18N
            methods[METHOD_lostFocus95].setDisplayName ( "" );
            methods[METHOD_lowestLayer96] = new MethodDescriptor(javax.swing.JLayeredPane.class.getMethod("lowestLayer", new Class[] {})); // NOI18N
            methods[METHOD_lowestLayer96].setDisplayName ( "" );
            methods[METHOD_minimumSize97] = new MethodDescriptor(java.awt.Container.class.getMethod("minimumSize", new Class[] {})); // NOI18N
            methods[METHOD_minimumSize97].setDisplayName ( "" );
            methods[METHOD_mouseDown98] = new MethodDescriptor(java.awt.Component.class.getMethod("mouseDown", new Class[] {java.awt.Event.class, int.class, int.class})); // NOI18N
            methods[METHOD_mouseDown98].setDisplayName ( "" );
            methods[METHOD_mouseDrag99] = new MethodDescriptor(java.awt.Component.class.getMethod("mouseDrag", new Class[] {java.awt.Event.class, int.class, int.class})); // NOI18N
            methods[METHOD_mouseDrag99].setDisplayName ( "" );
            methods[METHOD_mouseEnter100] = new MethodDescriptor(java.awt.Component.class.getMethod("mouseEnter", new Class[] {java.awt.Event.class, int.class, int.class})); // NOI18N
            methods[METHOD_mouseEnter100].setDisplayName ( "" );
            methods[METHOD_mouseExit101] = new MethodDescriptor(java.awt.Component.class.getMethod("mouseExit", new Class[] {java.awt.Event.class, int.class, int.class})); // NOI18N
            methods[METHOD_mouseExit101].setDisplayName ( "" );
            methods[METHOD_mouseMove102] = new MethodDescriptor(java.awt.Component.class.getMethod("mouseMove", new Class[] {java.awt.Event.class, int.class, int.class})); // NOI18N
            methods[METHOD_mouseMove102].setDisplayName ( "" );
            methods[METHOD_mouseUp103] = new MethodDescriptor(java.awt.Component.class.getMethod("mouseUp", new Class[] {java.awt.Event.class, int.class, int.class})); // NOI18N
            methods[METHOD_mouseUp103].setDisplayName ( "" );
            methods[METHOD_move104] = new MethodDescriptor(java.awt.Component.class.getMethod("move", new Class[] {int.class, int.class})); // NOI18N
            methods[METHOD_move104].setDisplayName ( "" );
            methods[METHOD_moveToBack105] = new MethodDescriptor(javax.swing.JLayeredPane.class.getMethod("moveToBack", new Class[] {java.awt.Component.class})); // NOI18N
            methods[METHOD_moveToBack105].setDisplayName ( "" );
            methods[METHOD_moveToFront106] = new MethodDescriptor(javax.swing.JLayeredPane.class.getMethod("moveToFront", new Class[] {java.awt.Component.class})); // NOI18N
            methods[METHOD_moveToFront106].setDisplayName ( "" );
            methods[METHOD_nextFocus107] = new MethodDescriptor(java.awt.Component.class.getMethod("nextFocus", new Class[] {})); // NOI18N
            methods[METHOD_nextFocus107].setDisplayName ( "" );
            methods[METHOD_paint108] = new MethodDescriptor(javax.swing.JLayeredPane.class.getMethod("paint", new Class[] {java.awt.Graphics.class})); // NOI18N
            methods[METHOD_paint108].setDisplayName ( "" );
            methods[METHOD_paintAll109] = new MethodDescriptor(java.awt.Component.class.getMethod("paintAll", new Class[] {java.awt.Graphics.class})); // NOI18N
            methods[METHOD_paintAll109].setDisplayName ( "" );
            methods[METHOD_paintComponent110] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("paintComponent", new Class[] {java.awt.Graphics.class})); // NOI18N
            methods[METHOD_paintComponent110].setDisplayName ( "" );
            methods[METHOD_paintComponents111] = new MethodDescriptor(java.awt.Container.class.getMethod("paintComponents", new Class[] {java.awt.Graphics.class})); // NOI18N
            methods[METHOD_paintComponents111].setDisplayName ( "" );
            methods[METHOD_paintImmediately112] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("paintImmediately", new Class[] {int.class, int.class, int.class, int.class})); // NOI18N
            methods[METHOD_paintImmediately112].setDisplayName ( "" );
            methods[METHOD_paintImmediately113] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("paintImmediately", new Class[] {java.awt.Rectangle.class})); // NOI18N
            methods[METHOD_paintImmediately113].setDisplayName ( "" );
            methods[METHOD_placeCharacter114] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("placeCharacter", new Class[] {int.class, int.class, char.class})); // NOI18N
            methods[METHOD_placeCharacter114].setDisplayName ( "" );
            methods[METHOD_placeCharacter115] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("placeCharacter", new Class[] {int.class, int.class, char.class, java.awt.Color.class, java.awt.Color.class})); // NOI18N
            methods[METHOD_placeCharacter115].setDisplayName ( "" );
            methods[METHOD_placeCharacter116] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("placeCharacter", new Class[] {int.class, int.class, char.class, java.awt.Color.class})); // NOI18N
            methods[METHOD_placeCharacter116].setDisplayName ( "" );
            methods[METHOD_placeHorizontalString117] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("placeHorizontalString", new Class[] {int.class, int.class, java.lang.String.class})); // NOI18N
            methods[METHOD_placeHorizontalString117].setDisplayName ( "" );
            methods[METHOD_placeHorizontalString118] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("placeHorizontalString", new Class[] {int.class, int.class, java.lang.String.class, java.awt.Color.class, java.awt.Color.class})); // NOI18N
            methods[METHOD_placeHorizontalString118].setDisplayName ( "" );
            methods[METHOD_placeImage119] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("placeImage", new Class[] {int.class, int.class, java.lang.String.class})); // NOI18N
            methods[METHOD_placeImage119].setDisplayName ( "" );
            methods[METHOD_placeImage120] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("placeImage", new Class[] {int.class, int.class, java.lang.String.class, java.awt.Color.class})); // NOI18N
            methods[METHOD_placeImage120].setDisplayName ( "" );
            methods[METHOD_placeText121] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("placeText", new Class[] {int.class, int.class, char[][].class})); // NOI18N
            methods[METHOD_placeText121].setDisplayName ( "" );
            methods[METHOD_placeText122] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("placeText", new Class[] {int.class, int.class, char[][].class, java.awt.Color.class, java.awt.Color.class})); // NOI18N
            methods[METHOD_placeText122].setDisplayName ( "" );
            methods[METHOD_placeVerticalString123] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("placeVerticalString", new Class[] {int.class, int.class, java.lang.String.class, java.awt.Color.class, java.awt.Color.class})); // NOI18N
            methods[METHOD_placeVerticalString123].setDisplayName ( "" );
            methods[METHOD_placeVerticalString124] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("placeVerticalString", new Class[] {int.class, int.class, java.lang.String.class})); // NOI18N
            methods[METHOD_placeVerticalString124].setDisplayName ( "" );
            methods[METHOD_postEvent125] = new MethodDescriptor(java.awt.Component.class.getMethod("postEvent", new Class[] {java.awt.Event.class})); // NOI18N
            methods[METHOD_postEvent125].setDisplayName ( "" );
            methods[METHOD_preferredSize126] = new MethodDescriptor(java.awt.Container.class.getMethod("preferredSize", new Class[] {})); // NOI18N
            methods[METHOD_preferredSize126].setDisplayName ( "" );
            methods[METHOD_prepareImage127] = new MethodDescriptor(java.awt.Component.class.getMethod("prepareImage", new Class[] {java.awt.Image.class, java.awt.image.ImageObserver.class})); // NOI18N
            methods[METHOD_prepareImage127].setDisplayName ( "" );
            methods[METHOD_prepareImage128] = new MethodDescriptor(java.awt.Component.class.getMethod("prepareImage", new Class[] {java.awt.Image.class, int.class, int.class, java.awt.image.ImageObserver.class})); // NOI18N
            methods[METHOD_prepareImage128].setDisplayName ( "" );
            methods[METHOD_print129] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("print", new Class[] {java.awt.Graphics.class})); // NOI18N
            methods[METHOD_print129].setDisplayName ( "" );
            methods[METHOD_printAll130] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("printAll", new Class[] {java.awt.Graphics.class})); // NOI18N
            methods[METHOD_printAll130].setDisplayName ( "" );
            methods[METHOD_printComponents131] = new MethodDescriptor(java.awt.Container.class.getMethod("printComponents", new Class[] {java.awt.Graphics.class})); // NOI18N
            methods[METHOD_printComponents131].setDisplayName ( "" );
            methods[METHOD_putClientProperty132] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("putClientProperty", new Class[] {java.lang.Object.class, java.lang.Object.class})); // NOI18N
            methods[METHOD_putClientProperty132].setDisplayName ( "" );
            methods[METHOD_putLayer133] = new MethodDescriptor(javax.swing.JLayeredPane.class.getMethod("putLayer", new Class[] {javax.swing.JComponent.class, int.class})); // NOI18N
            methods[METHOD_putLayer133].setDisplayName ( "" );
            methods[METHOD_refresh134] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("refresh", new Class[] {})); // NOI18N
            methods[METHOD_refresh134].setDisplayName ( "" );
            methods[METHOD_registerKeyboardAction135] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("registerKeyboardAction", new Class[] {java.awt.event.ActionListener.class, java.lang.String.class, javax.swing.KeyStroke.class, int.class})); // NOI18N
            methods[METHOD_registerKeyboardAction135].setDisplayName ( "" );
            methods[METHOD_registerKeyboardAction136] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("registerKeyboardAction", new Class[] {java.awt.event.ActionListener.class, javax.swing.KeyStroke.class, int.class})); // NOI18N
            methods[METHOD_registerKeyboardAction136].setDisplayName ( "" );
            methods[METHOD_remove137] = new MethodDescriptor(java.awt.Component.class.getMethod("remove", new Class[] {java.awt.MenuComponent.class})); // NOI18N
            methods[METHOD_remove137].setDisplayName ( "" );
            methods[METHOD_remove138] = new MethodDescriptor(java.awt.Container.class.getMethod("remove", new Class[] {java.awt.Component.class})); // NOI18N
            methods[METHOD_remove138].setDisplayName ( "" );
            methods[METHOD_remove139] = new MethodDescriptor(javax.swing.JLayeredPane.class.getMethod("remove", new Class[] {int.class})); // NOI18N
            methods[METHOD_remove139].setDisplayName ( "" );
            methods[METHOD_removeAll140] = new MethodDescriptor(javax.swing.JLayeredPane.class.getMethod("removeAll", new Class[] {})); // NOI18N
            methods[METHOD_removeAll140].setDisplayName ( "" );
            methods[METHOD_removeNotify141] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("removeNotify", new Class[] {})); // NOI18N
            methods[METHOD_removeNotify141].setDisplayName ( "" );
            methods[METHOD_removePropertyChangeListener142] = new MethodDescriptor(java.awt.Component.class.getMethod("removePropertyChangeListener", new Class[] {java.lang.String.class, java.beans.PropertyChangeListener.class})); // NOI18N
            methods[METHOD_removePropertyChangeListener142].setDisplayName ( "" );
            methods[METHOD_repaint143] = new MethodDescriptor(java.awt.Component.class.getMethod("repaint", new Class[] {})); // NOI18N
            methods[METHOD_repaint143].setDisplayName ( "" );
            methods[METHOD_repaint144] = new MethodDescriptor(java.awt.Component.class.getMethod("repaint", new Class[] {long.class})); // NOI18N
            methods[METHOD_repaint144].setDisplayName ( "" );
            methods[METHOD_repaint145] = new MethodDescriptor(java.awt.Component.class.getMethod("repaint", new Class[] {int.class, int.class, int.class, int.class})); // NOI18N
            methods[METHOD_repaint145].setDisplayName ( "" );
            methods[METHOD_repaint146] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("repaint", new Class[] {long.class, int.class, int.class, int.class, int.class})); // NOI18N
            methods[METHOD_repaint146].setDisplayName ( "" );
            methods[METHOD_repaint147] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("repaint", new Class[] {java.awt.Rectangle.class})); // NOI18N
            methods[METHOD_repaint147].setDisplayName ( "" );
            methods[METHOD_requestDefaultFocus148] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("requestDefaultFocus", new Class[] {})); // NOI18N
            methods[METHOD_requestDefaultFocus148].setDisplayName ( "" );
            methods[METHOD_requestFocus149] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("requestFocus", new Class[] {})); // NOI18N
            methods[METHOD_requestFocus149].setDisplayName ( "" );
            methods[METHOD_requestFocus150] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("requestFocus", new Class[] {boolean.class})); // NOI18N
            methods[METHOD_requestFocus150].setDisplayName ( "" );
            methods[METHOD_requestFocusInWindow151] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("requestFocusInWindow", new Class[] {})); // NOI18N
            methods[METHOD_requestFocusInWindow151].setDisplayName ( "" );
            methods[METHOD_resetKeyboardActions152] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("resetKeyboardActions", new Class[] {})); // NOI18N
            methods[METHOD_resetKeyboardActions152].setDisplayName ( "" );
            methods[METHOD_reshape153] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("reshape", new Class[] {int.class, int.class, int.class, int.class})); // NOI18N
            methods[METHOD_reshape153].setDisplayName ( "" );
            methods[METHOD_resize154] = new MethodDescriptor(java.awt.Component.class.getMethod("resize", new Class[] {int.class, int.class})); // NOI18N
            methods[METHOD_resize154].setDisplayName ( "" );
            methods[METHOD_resize155] = new MethodDescriptor(java.awt.Component.class.getMethod("resize", new Class[] {java.awt.Dimension.class})); // NOI18N
            methods[METHOD_resize155].setDisplayName ( "" );
            methods[METHOD_revalidate156] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("revalidate", new Class[] {})); // NOI18N
            methods[METHOD_revalidate156].setDisplayName ( "" );
            methods[METHOD_scrollRectToVisible157] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("scrollRectToVisible", new Class[] {java.awt.Rectangle.class})); // NOI18N
            methods[METHOD_scrollRectToVisible157].setDisplayName ( "" );
            methods[METHOD_setBounds158] = new MethodDescriptor(java.awt.Component.class.getMethod("setBounds", new Class[] {int.class, int.class, int.class, int.class})); // NOI18N
            methods[METHOD_setBounds158].setDisplayName ( "" );
            methods[METHOD_setComponentZOrder159] = new MethodDescriptor(java.awt.Container.class.getMethod("setComponentZOrder", new Class[] {java.awt.Component.class, int.class})); // NOI18N
            methods[METHOD_setComponentZOrder159].setDisplayName ( "" );
            methods[METHOD_setDefaultLocale160] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("setDefaultLocale", new Class[] {java.util.Locale.class})); // NOI18N
            methods[METHOD_setDefaultLocale160].setDisplayName ( "" );
            methods[METHOD_setLayer161] = new MethodDescriptor(javax.swing.JLayeredPane.class.getMethod("setLayer", new Class[] {java.awt.Component.class, int.class})); // NOI18N
            methods[METHOD_setLayer161].setDisplayName ( "" );
            methods[METHOD_setLayer162] = new MethodDescriptor(javax.swing.JLayeredPane.class.getMethod("setLayer", new Class[] {java.awt.Component.class, int.class, int.class})); // NOI18N
            methods[METHOD_setLayer162].setDisplayName ( "" );
            methods[METHOD_setPosition163] = new MethodDescriptor(javax.swing.JLayeredPane.class.getMethod("setPosition", new Class[] {java.awt.Component.class, int.class})); // NOI18N
            methods[METHOD_setPosition163].setDisplayName ( "" );
            methods[METHOD_show164] = new MethodDescriptor(java.awt.Component.class.getMethod("show", new Class[] {})); // NOI18N
            methods[METHOD_show164].setDisplayName ( "" );
            methods[METHOD_show165] = new MethodDescriptor(java.awt.Component.class.getMethod("show", new Class[] {boolean.class})); // NOI18N
            methods[METHOD_show165].setDisplayName ( "" );
            methods[METHOD_size166] = new MethodDescriptor(java.awt.Component.class.getMethod("size", new Class[] {})); // NOI18N
            methods[METHOD_size166].setDisplayName ( "" );
            methods[METHOD_slide167] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("slide", new Class[] {java.awt.Point.class, java.awt.Point.class})); // NOI18N
            methods[METHOD_slide167].setDisplayName ( "" );
            methods[METHOD_slide168] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("slide", new Class[] {java.awt.Point.class, squidpony.squidgrid.util.Direction.class})); // NOI18N
            methods[METHOD_slide168].setDisplayName ( "" );
            methods[METHOD_slide169] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("slide", new Class[] {java.awt.Point.class, java.awt.Point.class, int.class})); // NOI18N
            methods[METHOD_slide169].setDisplayName ( "" );
            methods[METHOD_toString170] = new MethodDescriptor(java.awt.Component.class.getMethod("toString", new Class[] {})); // NOI18N
            methods[METHOD_toString170].setDisplayName ( "" );
            methods[METHOD_transferFocus171] = new MethodDescriptor(java.awt.Component.class.getMethod("transferFocus", new Class[] {})); // NOI18N
            methods[METHOD_transferFocus171].setDisplayName ( "" );
            methods[METHOD_transferFocusBackward172] = new MethodDescriptor(java.awt.Component.class.getMethod("transferFocusBackward", new Class[] {})); // NOI18N
            methods[METHOD_transferFocusBackward172].setDisplayName ( "" );
            methods[METHOD_transferFocusDownCycle173] = new MethodDescriptor(java.awt.Container.class.getMethod("transferFocusDownCycle", new Class[] {})); // NOI18N
            methods[METHOD_transferFocusDownCycle173].setDisplayName ( "" );
            methods[METHOD_transferFocusUpCycle174] = new MethodDescriptor(java.awt.Component.class.getMethod("transferFocusUpCycle", new Class[] {})); // NOI18N
            methods[METHOD_transferFocusUpCycle174].setDisplayName ( "" );
            methods[METHOD_unregisterKeyboardAction175] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("unregisterKeyboardAction", new Class[] {javax.swing.KeyStroke.class})); // NOI18N
            methods[METHOD_unregisterKeyboardAction175].setDisplayName ( "" );
            methods[METHOD_update176] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("update", new Class[] {java.awt.Graphics.class})); // NOI18N
            methods[METHOD_update176].setDisplayName ( "" );
            methods[METHOD_updateUI177] = new MethodDescriptor(javax.swing.JComponent.class.getMethod("updateUI", new Class[] {})); // NOI18N
            methods[METHOD_updateUI177].setDisplayName ( "" );
            methods[METHOD_validate178] = new MethodDescriptor(java.awt.Container.class.getMethod("validate", new Class[] {})); // NOI18N
            methods[METHOD_validate178].setDisplayName ( "" );
            methods[METHOD_waitForAnimations179] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("waitForAnimations", new Class[] {})); // NOI18N
            methods[METHOD_waitForAnimations179].setDisplayName ( "" );
            methods[METHOD_wiggle180] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("wiggle", new Class[] {java.awt.Point.class})); // NOI18N
            methods[METHOD_wiggle180].setDisplayName ( "" );
            methods[METHOD_willFit181] = new MethodDescriptor(squidpony.squidgrid.gui.swing.SwingPane.class.getMethod("willFit", new Class[] {char.class})); // NOI18N
            methods[METHOD_willFit181].setDisplayName ( "" );
        }
        catch( Exception e) {}//GEN-HEADEREND:Methods

        // Here you can add code for customizing the methods array.
        
        return methods;     }//GEN-LAST:Methods
    private static java.awt.Image iconColor16 = null;//GEN-BEGIN:IconsDef
    private static java.awt.Image iconColor32 = null;
    private static java.awt.Image iconMono16 = null;
    private static java.awt.Image iconMono32 = null;//GEN-END:IconsDef
    private static String iconNameC16 = null;//GEN-BEGIN:Icons
    private static String iconNameC32 = null;
    private static String iconNameM16 = null;
    private static String iconNameM32 = null;//GEN-END:Icons
    private static final int defaultPropertyIndex = -1;//GEN-BEGIN:Idx
    private static final int defaultEventIndex = -1;//GEN-END:Idx

//GEN-FIRST:Superclass
    // Here you can add code for customizing the Superclass BeanInfo.
//GEN-LAST:Superclass
    /**
     * Gets the bean's
     * <code>BeanDescriptor</code>s.
     *
     * @return BeanDescriptor describing the editable properties of this bean.
     * May return null if the information should be obtained by automatic
     * analysis.
     */
    @Override
    public BeanDescriptor getBeanDescriptor() {
        return getBdescriptor();
    }

    /**
     * Gets the bean's
     * <code>PropertyDescriptor</code>s.
     *
     * @return An array of PropertyDescriptors describing the editable
     * properties supported by this bean. May return null if the information
     * should be obtained by automatic analysis. <p> If a property is indexed,
     * then its entry in the result array will belong to the
     * IndexedPropertyDescriptor subclass of PropertyDescriptor. A client of
     * getPropertyDescriptors can use "instanceof" to check if a given
     * PropertyDescriptor is an IndexedPropertyDescriptor.
     */
    @Override
    public PropertyDescriptor[] getPropertyDescriptors() {
        return getPdescriptor();
    }

    /**
     * Gets the bean's
     * <code>EventSetDescriptor</code>s.
     *
     * @return An array of EventSetDescriptors describing the kinds of events
     * fired by this bean. May return null if the information should be obtained
     * by automatic analysis.
     */
    @Override
    public EventSetDescriptor[] getEventSetDescriptors() {
        return getEdescriptor();
    }

    /**
     * Gets the bean's
     * <code>MethodDescriptor</code>s.
     *
     * @return An array of MethodDescriptors describing the methods implemented
     * by this bean. May return null if the information should be obtained by
     * automatic analysis.
     */
    @Override
    public MethodDescriptor[] getMethodDescriptors() {
        return getMdescriptor();
    }

    /**
     * A bean may have a "default" property that is the property that will
     * mostly commonly be initially chosen for update by human's who are
     * customizing the bean.
     *
     * @return Index of default property in the PropertyDescriptor array
     * returned by getPropertyDescriptors. <P>	Returns -1 if there is no default
     * property.
     */
    @Override
    public int getDefaultPropertyIndex() {
        return defaultPropertyIndex;
    }

    /**
     * A bean may have a "default" event that is the event that will mostly
     * commonly be used by human's when using the bean.
     *
     * @return Index of default event in the EventSetDescriptor array returned
     * by getEventSetDescriptors. <P>	Returns -1 if there is no default event.
     */
    @Override
    public int getDefaultEventIndex() {
        return defaultEventIndex;
    }

    /**
     * This method returns an image object that can be used to represent the
     * bean in toolboxes, toolbars, etc. Icon images will typically be GIFs, but
     * may in future include other formats. <p> Beans aren't required to provide
     * icons and may return null from this method. <p> There are four possible
     * flavors of icons (16x16 color, 32x32 color, 16x16 mono, 32x32 mono). If a
     * bean choses to only support a single icon we recommend supporting 16x16
     * color. <p> We recommend that icons have a "transparent" background so
     * they can be rendered onto an existing background.
     *
     * @param iconKind The kind of icon requested. This should be one of the
     * constant values ICON_COLOR_16x16, ICON_COLOR_32x32, ICON_MONO_16x16, or
     * ICON_MONO_32x32.
     * @return An image object representing the requested icon. May return null
     * if no suitable icon is available.
     */
    @Override
    public java.awt.Image getIcon(int iconKind) {
        switch (iconKind) {
            case ICON_COLOR_16x16:
                if (iconNameC16 == null) {
                    return null;
                } else {
                    if (iconColor16 == null) {
                        iconColor16 = loadImage(iconNameC16);
                    }
                    return iconColor16;
                }
            case ICON_COLOR_32x32:
                if (iconNameC32 == null) {
                    return null;
                } else {
                    if (iconColor32 == null) {
                        iconColor32 = loadImage(iconNameC32);
                    }
                    return iconColor32;
                }
            case ICON_MONO_16x16:
                if (iconNameM16 == null) {
                    return null;
                } else {
                    if (iconMono16 == null) {
                        iconMono16 = loadImage(iconNameM16);
                    }
                    return iconMono16;
                }
            case ICON_MONO_32x32:
                if (iconNameM32 == null) {
                    return null;
                } else {
                    if (iconMono32 == null) {
                        iconMono32 = loadImage(iconNameM32);
                    }
                    return iconMono32;
                }
            default:
                return null;
        }
    }
}

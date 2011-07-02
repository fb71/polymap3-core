/* 
 * polymap.org
 * Copyright 2009, 2011 Falko Br�utigam. All rights reserved.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.polymap.core.mapeditor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.geotools.geometry.jts.ReferencedEnvelope;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;

import org.polymap.core.mapeditor.RenderManager.RenderLayerDescriptor;
import org.polymap.core.project.ILayer;
import org.polymap.core.project.IMap;
import org.polymap.openlayers.rap.widget.OpenLayersWidget;
import org.polymap.openlayers.rap.widget.base.OpenLayersEventListener;
import org.polymap.openlayers.rap.widget.base.OpenLayersObject;
import org.polymap.openlayers.rap.widget.base_types.Bounds;
import org.polymap.openlayers.rap.widget.base_types.OpenLayersMap;
import org.polymap.openlayers.rap.widget.base_types.Projection;
import org.polymap.openlayers.rap.widget.base_types.Size;
import org.polymap.openlayers.rap.widget.controls.Control;
import org.polymap.openlayers.rap.widget.controls.KeyboardDefaultsControl;
import org.polymap.openlayers.rap.widget.controls.LayerSwitcherControl;
import org.polymap.openlayers.rap.widget.controls.LoadingPanelControl;
import org.polymap.openlayers.rap.widget.controls.MousePositionControl;
import org.polymap.openlayers.rap.widget.controls.NavigationHistoryControl;
import org.polymap.openlayers.rap.widget.controls.PanZoomBarControl;
import org.polymap.openlayers.rap.widget.controls.ScaleControl;
import org.polymap.openlayers.rap.widget.controls.ScaleLineControl;
import org.polymap.openlayers.rap.widget.layers.Layer;
import org.polymap.openlayers.rap.widget.layers.VectorLayer;
import org.polymap.openlayers.rap.widget.layers.WMSLayer;

/**
 * A map editor based on {@link OpenLayersWidget}.
 *
 * @author <a href="http://www.polymap.de">Falko Braeutigam</a>
 * @since 3.0
 */
public class MapEditor
        extends EditorPart
        implements IEditorPart, IAdaptable, OpenLayersEventListener {

    static Log log = LogFactory.getLog( MapEditor.class );

    private MapEditorInput          input;
    
    private IMap                    map;
    
    private Composite               composite;

    protected OpenLayersWidget      olwidget;

    private Point                   displaySize;
    
    private RenderManager           renderManager;
    
    /** The currently displayed layers. */
    protected Map<RenderLayerDescriptor,Layer> layers = new HashMap();
    
    /** The exclusivly activ edit support interface. */
    private IMapEditorSupport       activeSupport;
    
    private List<IMapEditorSupport> supports = new LinkedList();
    
    private ListenerList            supportListeners = new ListenerList();

    private MapEditorOverview       overview;
    

    public void init( IEditorSite _site, IEditorInput _input )
            throws PartInitException {
        setSite( _site );
        setInput( _input );
        this.map = ((MapEditorInput)_input).getMap();
        setPartName( map.getLabel() );
        log.debug( "input= " + input );
    }


    public void dispose() {
        log.debug( "dispose: ..." );
        if (overview != null) {
            overview.dispose();
            overview = null;
        }
        if (renderManager != null) {
            renderManager.dispose();
            renderManager = null;
        }
        if (olwidget != null) {
            olwidget.dispose();
            olwidget = null;
        }
        supportListeners.clear();
    }


    public void createPartControl( Composite parent ) {
        composite = new Composite( parent, SWT.NONE /*_p3:SWT.EMBEDDED | SWT.NO_BACKGROUND*/ );
        GridLayout compositeLayout = new GridLayout();
        compositeLayout.marginHeight = 0;
        compositeLayout.marginWidth = 0;
        compositeLayout.numColumns = 1;
        composite.setLayout( compositeLayout );
        composite.setBackground( Display.getDefault().getSystemColor( SWT.COLOR_INFO_BACKGROUND ) );

        // the widget (use internally provided OpenLayers lib)
        olwidget = new OpenLayersWidget( composite, SWT.MULTI | SWT.WRAP, "openlayers/full/OpenLayers.js" );
        olwidget.setLayoutData( new GridData( GridData.FILL_BOTH ) );

        // projection
        String crsCode = map.getCRSCode();
        log.info( "### CRS: " + crsCode );
        Projection proj = new Projection( crsCode );
        String units = crsCode.equals( "EPSG:4326" ) ? "degrees" : "m";
        float maxResolution = crsCode.equals( "EPSG:4326" ) ? (360/256) : 500000;
        ReferencedEnvelope bbox = map.getMaxExtent();
        log.info( "### maxExtent: " + bbox );
        Bounds maxExtent = bbox != null
                ? new Bounds( bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY() )
                : null;
        olwidget.createMap( proj, proj, units, maxExtent, maxResolution );

        // XXX use OpenLayers events
        displaySize = olwidget.getSize();
        olwidget.addControlListener( new ControlListener() {
            public void controlResized( ControlEvent ev ) {
                displaySize = olwidget.getSize();
                //log.debug( "resized! size= " + displaySize );
            }
            public void controlMoved( ControlEvent ev ) {
                //
            }
        });

        // add some controls to the map
        OpenLayersMap olmap = olwidget.getMap();
        olmap.setMaxScale( 1000 );
        
        olmap.addControl( new LoadingPanelControl() );
        
        olmap.addControl( new LayerSwitcherControl() );
        olmap.addControl( new PanZoomBarControl() );
        olmap.addControl( new MousePositionControl() );
        olmap.addControl( new NavigationHistoryControl() );
        olmap.addControl( new KeyboardDefaultsControl() );

        olmap.addControl( new ScaleLineControl() );
        olmap.addControl( new ScaleControl() );

        // map events
        HashMap<String, String> payload = new HashMap<String, String>();
        payload.put( "left", "event.object.getExtent().toArray()[0]" );
        payload.put( "bottom", "event.object.getExtent().toArray()[1]" );
        payload.put( "right", "event.object.getExtent().toArray()[2]" );
        payload.put( "top", "event.object.getExtent().toArray()[3]" );
        olmap.events.register( this, OpenLayersMap.EVENT_MOUSE_OVER, payload );
        olmap.events.register( this, OpenLayersMap.EVENT_MOVEEND, payload );
        
        // empty base layer
        VectorLayer baseLayer = new VectorLayer( "[Base]" );
        baseLayer.setIsBaseLayer( true );
        olmap.addLayer( baseLayer );
        
        olmap.zoomToExtent( maxExtent, false );

        overview = new MapEditorOverview( this );
        
        // renderManager
        renderManager = new RenderManager( map, this );
        renderManager.updatePipelines();
    }


    /*
     * Processes events triggered by the OpenLayers map. 
     */
    public void process_event( OpenLayersObject obj, String name, HashMap<String, String> payload ) {
//        log.info( "event: " + name + ", from: " + obj );
//        for (Map.Entry entry : payload.entrySet()) {
//            log.debug( "    key: " + entry.getKey() + ", value: " + entry.getValue() );
//        }
        if (olwidget.getMap() != obj) {
            return;
        }
        // mouse over
        if (OpenLayersMap.EVENT_MOUSE_OVER.equals( name )) {
            try {
                final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                if (page.getActivePart() != this) {
                    Display.getCurrent().asyncExec( new Runnable() {
                        public void run() {
                            page.activate( MapEditor.this );
                        }
                    });
                }
            }
            catch (Exception e) {
                log.error( "", e );
            }
        }
        // map zoom/pan
        String left = payload.get( "left" );
        if (left != null) {
            try {
                ReferencedEnvelope bbox = new ReferencedEnvelope(
                        Double.parseDouble( payload.get( "left" ) ),
                        Double.parseDouble( payload.get( "right" ) ),
                        Double.parseDouble( payload.get( "bottom" ) ),
                        Double.parseDouble( payload.get( "top" ) ),
                        map.getCRS() );
                log.debug( "### process_event: bbox= " + bbox );
                ReferencedEnvelope old = map.getExtent();
                if (!bbox.equals( old )) {
                    map.updateExtent( bbox );
                }
                
//                // GeoEvent
//                if (!bbox.equals( old )) {
//                    GeoEvent event = new GeoEvent( GeoEvent.Type.NAVIGATION, 
//                            map.getLabel(), null );
//                    event.setProperty( GeoEvent.PROP_NEW_MAP_EXTENT, bbox );
//                    event.setProperty( GeoEvent.PROP_OLD_MAP_EXTENT, old );
//                    GeoHub.instance().send( event );
//                }
            }
            catch (Exception e) {
                log.error( "unhandled:", e );
            }
        }
    }


    public IMap getMap() {
        return map;
    }


    public void addControl( Control control ) {
        olwidget.getMap().addControl( control );
    }

    public void removeControl( Control control ) {
        olwidget.getMap().removeControl( control );
    }

    public void addLayer( Layer olayer ) {
        olwidget.getMap().addLayer( olayer );
    }
    
    public void removeLayer( Layer olayer ) {
        olwidget.getMap().removeLayer( olayer );
    }
    
    /**
     * Fills the {@link #olwidget} with the internal servers of the
     * {@link #renderManager}.
     */
    void addLayer( RenderLayerDescriptor descriptor ) {
        assert descriptor != null;
        assert !layers.containsKey( descriptor );

        StringBuffer layerNames = new StringBuffer();
        for (ILayer layer : descriptor.layers) {
            layerNames.append( layerNames.length() > 0 ? "," : "" );
            layerNames.append( layer.getLabel() );
        }
        WMSLayer olayer = new WMSLayer( descriptor.title, 
                descriptor.servicePath, layerNames.toString() );
        olayer.setFormat( "image/png" );
        olayer.setVisibility( true );
        olayer.setIsBaseLayer( false );
        //olayer.setSingleTile( true );
        olayer.setTileSize( new Size( 400, 400 ) );
        olayer.setBuffer( 0 );
        olayer.setZIndex( descriptor.zPriority );
        olayer.setOpacity( ((double)descriptor.opacity) / 100 );
        olayer.setTransitionEffect( Layer.TRANSITION_RESIZE );
        olwidget.getMap().addLayer( olayer );
        olayer.setZIndex( descriptor.zPriority );
        layers.put( descriptor, olayer );

        //overview.addLayer( olLayer );

        if (overview != null) {
            overview.addLayer( descriptor );
        }
    }
    
    
    /**
     * 
     */
    void removeLayer( RenderLayerDescriptor descriptor ) {
        assert descriptor != null;
        assert layers.containsKey( descriptor );

        if (overview != null) {
            overview.removeLayer( descriptor );
        }

        Layer olayer = layers.remove( descriptor );
        olwidget.getMap().removeLayer( olayer );
    }
   
   
    /**
     * Fills the {@link #olwidget} with the internal servers of the
     * {@link #renderManager}.
     */
    void reloadLayer( RenderLayerDescriptor descriptor ) {
        assert descriptor != null;
        assert layers.containsKey( descriptor );

        Layer ollayer = layers.get( descriptor );
        if (ollayer instanceof WMSLayer) {
            ((WMSLayer)ollayer).redraw( true );
        }
        else {
            ollayer.redraw();
        }
    }
   

    public void setLayerOpacity( RenderLayerDescriptor descriptor, int opacity ) {
        assert descriptor != null;
        assert layers.containsKey( descriptor );
        
        Layer ollayer = layers.get( descriptor );
        ollayer.setOpacity( ((double)opacity) / 100 );
    }


    public void setLayerZPriority( RenderLayerDescriptor descriptor, int zPriority ) {
        assert descriptor != null;
        assert layers.containsKey( descriptor );
        
        Layer ollayer = layers.get( descriptor );
        ollayer.setZIndex( zPriority );
        log.debug( "ollayer=" + ollayer + ", zPriority=" + zPriority );
    }


    public void setMapExtent( ReferencedEnvelope bbox ) {
        log.debug( "mapExtent: " + bbox );
        assert bbox != null : "bbox == null";
        try {
            //log.debug( "median(0)=" + bbox.getMedian( 0 ) + ", median(1)=" + bbox.getMedian( 1 ) );
            Bounds extent = new Bounds( bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY() );
            olwidget.getMap().zoomToExtent( extent, false );
        }
        catch (Exception e) {
            log.warn( "", e );
        }
    }

    
    public void setMaxExtent( ReferencedEnvelope bbox ) {
        log.debug( "maxExtent: " + bbox );
        assert bbox != null : "bbox == null";
        try {
            //log.debug( "median(0)=" + bbox.getMedian( 0 ) + ", median(1)=" + bbox.getMedian( 1 ) );
            Bounds extent = new Bounds( bbox.getMinX(), bbox.getMinY(), bbox.getMaxX(), bbox.getMaxY() );
            olwidget.getMap().setMaxExtent( extent );
        }
        catch (Exception e) {
            log.warn( "", e );
        }
    }

    
    /**
     * @deprecated 
     */
    public ILayer getEditLayer() {
        return renderManager.getEditLayer();
    }

    
    public boolean isDirty() {
        return false;
    }

    public boolean isSaveAsAllowed() {
        return false;
    }

    public void setFocus() {
        composite.setFocus();
//        updateCRS();
//        updateScaleLabel();
    }

    public void doSave( IProgressMonitor monitor ) {
        throw new RuntimeException( "not yet implemented." );
//        try {
//            log.debug( "..." );
//            layers.get( vectorLayer );
//            FinishEditLayerOperation op = new FinishEditLayerOperation( vectorLayer, true );
//
//            OperationSupport.instance().execute( op, false, false );
//        }
//        catch (Exception e) {
//            log.error( e.getMessage(), e );
//            MessageBox mbox = new MessageBox( 
//                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
//                    SWT.OK | SWT.ICON_ERROR | SWT.APPLICATION_MODAL );
//            mbox.setMessage( "Fehler: " + e.toString() );
//            mbox.setText( "Fehler bei der Operation." );
//            mbox.open();
//        }

    }

    public void doSaveAs() {
        throw new RuntimeException( "not yet implemented." );
    }


    // support interfaces *********************************
    
    public void addSupport( IMapEditorSupport support ) {
        supports.add( support );
    }
    
    public void removeSupport( IMapEditorSupport support ) {
        supports.remove( support );

        if (support.equals( activeSupport )) {
            fireSupportEvent( support, false );
            activeSupport = null;
        }
    }
    
    /**
     * Returns the support instance of the given class.
     * 
     * @param adapter
     * @return Null, if there is no such support.
     */
    public IMapEditorSupport findSupport( Class adapter ) {
        for (IMapEditorSupport support : supports) {
            if (adapter.isAssignableFrom( support.getClass() )) {
                return support;
            }
        }
        return null;
    }

    /**
     * Returns {@link #findSupport(Class)}.
     */
    public Object getAdapter( Class adapter ) {
        return findSupport( adapter );
    }
    
    /**
     *
     * @param support
     * @param active
     */
    public void activateSupport( IMapEditorSupport support, boolean active ) {
        if (support.equals( activeSupport ) && active) {
            return;
        }
        if (activeSupport != null) {
            IMapEditorSupport deactivate = activeSupport;
            activeSupport = null;
            fireSupportEvent( deactivate, false );
        }
        if (active) {
            activeSupport = support;
            fireSupportEvent( activeSupport, true );
        }
    }
    
    public boolean isActive( IMapEditorSupport support ) {
        return support != null ? support.equals( activeSupport ) : false;
    }

    public void addSupportListener( IMapEditorSupportListener listener ) {
        supportListeners.add( listener );
    }
    
    public void removeSupportListener( IMapEditorSupportListener listener ) {
        supportListeners.remove( listener );
    }
    
    private void fireSupportEvent( IMapEditorSupport support, boolean activated ) {
        for (Object listener : supportListeners.getListeners()) {
            ((IMapEditorSupportListener)listener).supportStateChanged( this, support, activated );
        }
    }

}

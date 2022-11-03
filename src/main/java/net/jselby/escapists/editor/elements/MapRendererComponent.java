package net.jselby.escapists.editor.elements;

import net.jselby.escapists.editor.mapping.Map;
import net.jselby.escapists.editor.mapping.MapRenderer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.html.HTMLAnchorElement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * A custom component which renders maps in the map editor.
 *
 * @author j_selby
 */
public class MapRendererComponent extends JPanel {
    private float origWidth;
    private float origHeight;

    private Map mapToEdit;

    private BufferedImage render;

    private float zoomFactor = 1.0f;
    private String view = "World";
    private MapRenderer renderer;

    public MapRendererComponent(Map map, MouseListener clickListener, MouseMotionListener motionListener) {
        this.mapToEdit = map;

        addMouseListener(clickListener);
        addMouseMotionListener(motionListener);

        // Build the renderer
        renderer = new MapRenderer();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (renderer.showZones && renderer.zoneEditing) {
                    renderer.getZoning().mouseDown(mapToEdit, (int) ((float) e.getX() / (float) zoomFactor),
                            (int) ((float) e.getY() / (float) zoomFactor));
                    refresh();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (renderer.showZones && renderer.zoneEditing) {
                    renderer.getZoning().mouseDragged(mapToEdit, (int) ((float) e.getX() / (float) zoomFactor),
                            (int) ((float) e.getY() / (float) zoomFactor));
                    refresh();
                }
            }
        });

        setLayout(new BorderLayout());
        setMap(map);

    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;

        if (render != null) {
            graphics2D.scale(zoomFactor, zoomFactor);
            g.drawImage(render, 0, 0, null);
        }

        Dimension size = new Dimension((int) (origWidth * zoomFactor),(int) (origHeight * zoomFactor));
        setSize(size);
        setPreferredSize(size);
        setMaximumSize(size);
        setMinimumSize(size);

        // Re-Layout the panel
        validate();
    }

    public void refresh() {
        // Render a snapshot
        if (mapToEdit != null) {
            render = renderer.render(mapToEdit, view);
        }

        setIgnoreRepaint(false);
        repaint();
    }

    public void setZoomFactor(float newZoom) {
        this.zoomFactor = newZoom;
        refresh();
    }

    public float getZoomFactor() {
        return zoomFactor;
    }

    public void setShowZones(boolean showZones) {
        renderer.showZones = showZones;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getView() {
        return view;
    }

    public void setEditZones(boolean editZones) {
        renderer.zoneEditing = editZones;
    }

    public void setMap(Map map) {

        boolean zoneEditing = renderer.zoneEditing;
        boolean showZones = renderer.showZones;
        renderer = new MapRenderer();
        renderer.zoneEditing = zoneEditing;
        renderer.showZones = showZones;
        this.mapToEdit = map;

        if (mapToEdit != null) {
            removeAll();

            Dimension size = new Dimension((map.getHeight() - 1) * 16, (map.getWidth() - 3) * 16);
            setSize(size);
            setPreferredSize(size);
            setMaximumSize(size);
            setMinimumSize(size);

            // These are inverted, don't worry.
            origWidth = (map.getHeight() - 1) * 16;
            origHeight = (map.getWidth() - 3) * 16;
        } else {
            origHeight = 500;
            origWidth = 734;
        }

        refresh();
    }

}

package net.jselby.escapists.editor;

import net.jselby.escapists.Map;
import net.jselby.escapists.MapRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

/**
 * A custom component which renders maps in the map editor.
 *
 * @author j_selby
 */
public class MapRendererComponent extends JPanel {
    private final float origWidth;
    private final float origHeight;

    private Map mapToEdit;

    private BufferedImage render;

    private float zoomFactor = 1.0f;
    private String view = "World";
    private MapRenderer renderer;

    public MapRendererComponent(final Map map, ClickListener clickListener, MouseMotionListener motionListener) {
        this.mapToEdit = map;

        addMouseListener(clickListener);
        addMouseMotionListener(motionListener);

        // Build the renderer
        renderer = new MapRenderer();

        if (mapToEdit != null) {
            Dimension size = new Dimension((map.getHeight() - 1) * 16, (map.getWidth() - 3) * 16);
            setSize(size);
            setPreferredSize(size);
            setMaximumSize(size);
            setMinimumSize(size);

            // These are inverted, don't worry.
            origWidth = (map.getHeight() - 1) * 16;
            origHeight = (map.getWidth() - 3) * 16;
        } else {
            origHeight = 1;
            origWidth = 1;
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (renderer.showZones && renderer.zoneEditing) {
                    renderer.getZoning().mouseDown(map, (int) ((float) e.getX() / (float) zoomFactor),
                            (int) ((float) e.getY() / (float) zoomFactor));
                    refresh();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (renderer.showZones && renderer.zoneEditing) {
                    renderer.getZoning().mouseDragged(map, (int) ((float) e.getX() / (float) zoomFactor),
                            (int) ((float) e.getY() / (float) zoomFactor));
                    refresh();
                }
            }
        });

        refresh();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.scale(zoomFactor, zoomFactor);

        g.drawImage(render, 0, 0, null);

        // Change the size of the panel
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
}

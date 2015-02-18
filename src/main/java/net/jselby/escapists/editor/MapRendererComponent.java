package net.jselby.escapists.editor;

import net.jselby.escapists.Map;
import net.jselby.escapists.MapRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

/**
 * A custom component which renders maps in the map editor.
 *
 * @author j_selby
 */
public class MapRendererComponent extends JPanel {
    private Map mapToEdit;

    private BufferedImage render;
    private boolean showZones;
    private String selectedZone;

    public MapRendererComponent(Map map, ClickListener clickListener, MouseMotionListener motionListener) {
        this.mapToEdit = map;

        addMouseListener(clickListener);
        addMouseMotionListener(motionListener);

        if (mapToEdit != null) {
            Dimension size = new Dimension((map.getHeight() - 1) * 16, (map.getWidth() - 3) * 16);
            setSize(size);
            setPreferredSize(size);
            setMaximumSize(size);
            setMinimumSize(size);
        }

        refresh();
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(render, 0, 0, null);
    }

    public void refresh() {
        // Render a snapshot
        if (mapToEdit != null) {
            render = MapRenderer.render(mapToEdit, showZones, selectedZone);
        }

        setIgnoreRepaint(false);
        repaint();
    }

    public void setShowZones(boolean showZones) {
        this.showZones = showZones;
    }

    public void setSelectedZone(String selectedZone) {
        this.selectedZone = selectedZone;
    }
}

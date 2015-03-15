package net.jselby.escapists.editor;

import net.jselby.escapists.Map;
import net.jselby.escapists.MapRenderer;
import net.jselby.escapists.utils.IOUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A custom component which renders maps in the map editor.
 *
 * @author j_selby
 */
public class MapRendererComponent extends JPanel {
    private final JScrollPane panel;
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

        final JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setOpaque(false);
        area.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        area.setText("Escapists Map Editor\n" +
                "Written by jselby\nhttp://redd.it/2wacp2\n\n" +
                "You don't have a map loaded currently - \n Go to File in the top left, and press a button there!\n" +
                "\nUpdate log:\nLoading...");
        area.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        new Thread(new Runnable() {
            @Override
            public void run() {
                String txt;
                try {
                    txt = IOUtils.toString(new URL("http://escapists.jselby.net/version.txt"));
                } catch (IOException e) {
                    e.printStackTrace();
                    txt = "Failed to load update log: " + e.getClass().getSimpleName() + ": " + e.getLocalizedMessage();
                }
                txt = "    " + txt.replace("\n", "\n    ");
                area.setText(area.getText().replace("Loading...", txt));
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        panel.validate();
                    }
                });
            }
        }).start();

        panel = new JScrollPane(area);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

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
            origHeight = 300;
            origWidth = 700;

            add(panel, BorderLayout.NORTH);
        }

        refresh();
    }
}

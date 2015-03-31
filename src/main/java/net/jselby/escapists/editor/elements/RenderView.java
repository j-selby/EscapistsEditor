package net.jselby.escapists.editor.elements;

import net.jselby.escapists.EscapistsEditor;
import net.jselby.escapists.editor.ActionMode;
import net.jselby.escapists.mapping.Map;
import net.jselby.escapists.objects.Objects;
import net.jselby.escapists.objects.WorldObject;
import net.jselby.escapists.utils.IOUtils;
import net.jselby.escapists.utils.StringUtils;
import net.jselby.escapists.utils.logging.Rollbar;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * The render view shows the render in a window, and has toolbar options addiitonally.
 *
 * @author j_selby
 */
public class RenderView extends JFrame {
    private final MapRendererComponent renderer;

    private final JPanel sidebar;
    private JScrollPane tileSelect;
    private JLabel selectedTile;

    private final JPanel toolOptions;

    // Tools
    private final JComboBox id;

    private EscapistsEditor editor;
    private Map mapToEdit;

    private int x;
    private int y;

    private ActionMode mode = ActionMode.CREATE_OBJECT;
    private int bigBrush;

    private boolean showZone;
    private String currentZone = "World";
    private JPanel iconPanel;

    public RenderView(final EscapistsEditor editor, Map renderMap) throws IOException {
        this.editor = editor;
        this.mapToEdit = renderMap;

        // Configure the defaults
        // ID box
        ArrayList<Object> objectIds = new ArrayList<>();
        for (Objects objectId : Objects.values()) {
            objectIds.add(objectId.getID() + ": "
                    + StringUtils.capitalize(objectId.name().toLowerCase().replace("_", " ")));
        }
        objectIds.add("Other...");
        id = new JComboBox(objectIds.toArray());
        id.setFocusable(false);
        id.setFont(new Font(Font.DIALOG, Font.PLAIN, 10));
        id.setSelectedIndex(0);
        id.setMaximumSize(new Dimension(150, 30));

        // Build tiles
        iconPanel = new JPanel();
        tileSelect = new JScrollPane(iconPanel);
        tileSelect.getVerticalScrollBar().setUnitIncrement(16);
        updateTiling();

        // Configure the view
        setLayout(new BorderLayout());

        // Add a menu bar
        final JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.add(new AbstractAction("New") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    editor.edit(IOUtils.toByteArray(
                            getClass().getResource("/blank.decrypted.map")));
                } catch (IOException e1) {
                    EscapistsEditor.fatalError(e1);
                }
            }
        });
        fileMenu.add(new AbstractAction("Load...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Save map
                try {
                    RenderView.this.setEnabled(false);
                    new MapSelectionGUI(editor).setOldView(
                            RenderView.this);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        fileMenu.add(new AbstractAction("Save...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Save map
                // Get our target
                JFileChooser fc = new JFileChooser();
                File file = new File(System.getProperty("user.home"));
                File documents = new File(file, "Documents" + File.separator
                        + "The Escapists" + File.separator + "Custom Maps");
                if (documents.exists()) {
                    fc.setCurrentDirectory(documents);
                } else {
                    fc.setCurrentDirectory(new File(editor.escapistsPath,
                            "Data" + File.separator + "Maps"));
                }
                fc.addChoosableFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.getName().toLowerCase().endsWith(".pmap");
                    }

                    @Override
                    public String getDescription() {
                        return ".pmap Maps";
                    }
                });
                int dialog = fc.showSaveDialog(RenderView.this);
                if (JFileChooser.APPROVE_OPTION == dialog) {
                    try {
                        File file1 = fc.getSelectedFile();
                        if (!file1.getName().toLowerCase().endsWith(".pmap")) {
                            file1 = new File(file1.getParent(), file1.getName() + ".pmap");
                        }
                        mapToEdit.save(file1);
                    } catch (Exception error) {
                        error.printStackTrace();
                        editor.dialog(error.getMessage());
                    }
                }
            }
        });
        fileMenu.addSeparator();
        fileMenu.add(new AbstractAction("Exit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        menuBar.add(fileMenu);
        final JMenu propertiesMenu = new JMenu("Properties");

        propertiesMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (RenderView.this.mapToEdit != null) {
                            try {
                                new PropertiesDialog(RenderView.this, renderer, RenderView.this.mapToEdit);
                            } catch (Exception err) {
                                err.printStackTrace();
                                Rollbar.fatal(err);
                            }
                        } else {
                            editor.dialog("You must have a map loaded!");
                        }
                    }
                });

            }

            public void menuDeselected(MenuEvent e) {
            }

            public void menuCanceled(MenuEvent e) {
            }
        });
        menuBar.add(propertiesMenu);

        JMenu aboutMenu = new JMenu("About");
        aboutMenu.addMenuListener(new MenuListener() {
            @Override
            public void menuSelected(MenuEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        editor.dialog("Editor by jselby (http://jselby.net)\nGame by Mouldy Toof Studios & Team17");
                    }
                });
            }

            public void menuDeselected(MenuEvent e) {}
            public void menuCanceled(MenuEvent e) {}
        });
        menuBar.add(aboutMenu);
        menuBar.setAlignmentX(LEFT_ALIGNMENT);
        menuBar.setOpaque(false);
        menuBar.setPreferredSize(new Dimension(700, 20));
        add(menuBar, BorderLayout.BEFORE_FIRST_LINE);

        // Add a small panel at the bottom for position etc
        final JPanel mousePosition = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                // Get a object, if applicable

                WorldObject object = mapToEdit != null ? mapToEdit.getObjectAt(x, y, currentZone) : null;
                String objectName = null;
                if (object != null) {
                    if (object.asWorldDictionary() == null) {
                        objectName = object.getClass().getName();
                    } else {
                        objectName = StringUtils.capitalize(object.asWorldDictionary().name().toLowerCase().replace("_", " "));
                    }
                }
                String objectDef = (object != null ? (", Object " + object.toString() + " (" + objectName + ")") : "");

                boolean validPosition = x >= 0 && y >= 0 && ((mapToEdit != null &&
                        x < mapToEdit.getHeight() && y < mapToEdit.getWidth()) || mapToEdit == null);
                String worldElements = "X: " + x + ", Y: " + y
                        + objectDef
                        + ((mapToEdit != null && validPosition) ? (", Tile: " + mapToEdit.getTile(x, y, currentZone)) : "");

                // Render a top bar
                int width = getWidth();
                int height = 20;
                g.setColor(new Color(0f, 0f, 0f, 1f));
                g.fillRect(0, 0, width, height);
                g.setColor(Color.white);
                g.drawString(worldElements, 10, 12);
            }
        };
        mousePosition.setIgnoreRepaint(false);

        renderer = new MapRendererComponent(mapToEdit, new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Get position

                x = e.getX() / 16;
                y = e.getY() / 16;

                // Convert x & y with scaling
                x = (int) (((float) x) / ((float) renderer.getZoomFactor()));
                y = (int) (((float) y) / ((float) renderer.getZoomFactor()));

                toolHandler(x, y);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                x = -1;
                y = -1;
            }
        }, new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // Get position
                int newX = e.getX() / 16;
                int newY = e.getY() / 16;
                if (newX != x || newY != y) {
                    x = newX;
                    y = newY;
                } else {
                    return;
                }

                // Convert x & y with scaling
                x = (int) (((float) x) / ((float) renderer.getZoomFactor()));
                y = (int) (((float) y) / ((float) renderer.getZoomFactor()));

                toolHandler(x, y);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                int newX = e.getX() / 16;
                int newY = e.getY() / 16;
                if (newX != x || newY != y) {
                    x = newX;
                    y = newY;
                } else {
                    return;
                }

                // Convert x & y with scaling
                x = (int) (((float) x) / ((float) renderer.getZoomFactor()));
                y = (int) (((float) y) / ((float) renderer.getZoomFactor()));

                mousePosition.repaint();
            }
        });

        // Add scroll bars to the screen
        final JScrollPane scrollArea = new JScrollPane(renderer);
        scrollArea.setOpaque(false);
        scrollArea.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scrollArea.setWheelScrollingEnabled(true);
        scrollArea.getHorizontalScrollBar().setUnitIncrement(16);
        scrollArea.getVerticalScrollBar().setUnitIncrement(16);
        scrollArea.setPreferredSize(new Dimension(700, 580));

        // Create a wrapper for the area
        final JPanel scrollAreaWrapper = new JPanel();
        scrollAreaWrapper.setIgnoreRepaint(false);
        scrollAreaWrapper.setLayout(new BorderLayout());
        scrollAreaWrapper.setPreferredSize(new Dimension(700, 580));

        // Put it together
        mousePosition.setPreferredSize(new Dimension(700, 20));
        scrollAreaWrapper.add(scrollArea, BorderLayout.CENTER);
        scrollAreaWrapper.add(mousePosition, BorderLayout.NORTH);
        add(scrollAreaWrapper);

        // Implement controls for this scrollArea
        final int scrollIncrements = 16;
        getRootPane().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.VK_UNDEFINED), "moveRight");
        getRootPane().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.VK_UNDEFINED), "moveLeft");
        getRootPane().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.VK_UNDEFINED), "moveUp");
        getRootPane().getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.VK_UNDEFINED), "moveDown");
        getRootPane().getActionMap().put("moveRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Point position = scrollArea.getViewport().getViewPosition();
                position.translate(scrollIncrements, 0);
                if (position.getX() >= 0 && position.getY() >= 0
                        && (position.getX() + scrollArea.getViewport().getWidth())
                        < scrollArea.getHorizontalScrollBar().getMaximum()
                        && (position.getY() + scrollArea.getViewport().getHeight())
                        < scrollArea.getVerticalScrollBar().getMaximum()) {
                    scrollArea.getViewport().setViewPosition(position);
                }
            }
        });
        getRootPane().getActionMap().put("moveLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Point position = scrollArea.getViewport().getViewPosition();
                position.translate(-scrollIncrements, 0);
                if (position.getX() >= 0 && position.getY() >= 0
                        && (position.getX() + scrollArea.getViewport().getWidth())
                        < scrollArea.getHorizontalScrollBar().getMaximum()
                        && (position.getY() + scrollArea.getViewport().getHeight())
                        < scrollArea.getVerticalScrollBar().getMaximum()) {
                    scrollArea.getViewport().setViewPosition(position);
                }
            }
        });
        getRootPane().getActionMap().put("moveUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Point position = scrollArea.getViewport().getViewPosition();
                position.translate(0, -scrollIncrements);
                if (position.getX() >= 0 && position.getY() >= 0
                        && (position.getX() + scrollArea.getViewport().getWidth())
                        < scrollArea.getHorizontalScrollBar().getMaximum()
                        && (position.getY() + scrollArea.getViewport().getHeight())
                        < scrollArea.getVerticalScrollBar().getMaximum()) {
                    scrollArea.getViewport().setViewPosition(position);
                }
            }
        });
        getRootPane().getActionMap().put("moveDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Point position = scrollArea.getViewport().getViewPosition();
                position.translate(0, scrollIncrements);
                if (position.getX() >= 0 && position.getY() >= 0
                        && (position.getX() + scrollArea.getViewport().getWidth())
                        < scrollArea.getHorizontalScrollBar().getMaximum()
                        && (position.getY() + scrollArea.getViewport().getHeight())
                        < scrollArea.getVerticalScrollBar().getMaximum()) {
                    scrollArea.getViewport().setViewPosition(position);
                }
            }
        });

        // Toolbar
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        Dimension sidebarSize = new Dimension(200, 600);
        sidebar.setPreferredSize(sidebarSize);
        sidebar.setMaximumSize(sidebarSize);
        sidebar.setMinimumSize(sidebarSize);
        sidebar.setAlignmentY(TOP_ALIGNMENT);
        add(sidebar, BorderLayout.WEST);

        sidebar.add(Box.createVerticalStrut(10));

        JLabel displayLabel = new JLabel("Display");
        displayLabel.setAlignmentX(CENTER_ALIGNMENT);
        displayLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        sidebar.add(displayLabel);

        sidebar.add(Box.createVerticalStrut(10));

        // View
        final JComboBox views = new JComboBox(new String[]{
            "Underground", "World", "Vents", "Roof"
        });
        views.setFocusable(false);
        views.setSelectedIndex(1);
        views.setMaximumSize(new Dimension(150, 30));
        views.setAlignmentX(CENTER_ALIGNMENT);
        views.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentZone = (String) views.getSelectedItem();
                renderer.setView((String) views.getSelectedItem());
                renderer.refresh();
            }
        });
        JLabel viewLabel = new JLabel("Layer");
        viewLabel.setAlignmentX(CENTER_ALIGNMENT);
        sidebar.add(viewLabel);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(views);

        // Zoom
        sidebar.add(Box.createVerticalStrut(10));
        JLabel zoomLabel = new JLabel("Zoom");
        zoomLabel.setAlignmentX(CENTER_ALIGNMENT);
        sidebar.add(zoomLabel);
        final JSlider zoom = new JSlider(JSlider.HORIZONTAL,
                1, 5, 3);
        zoom.setFocusable(false);
        zoom.setMajorTickSpacing(1);
        zoom.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                float value = zoom.getValue();
                if (value < 3) {
                    if (value == 1) {
                        value = 0.5f;
                    } else if (value == 2) {
                        value = 0.75f;
                    }
                } else {
                    value -= 2;
                    if (value == 2) {
                        value = 1.5f;
                    } else if (value == 3) {
                        value = 2f;
                    }
                }
                renderer.setZoomFactor(value);
            }
        });
        zoom.setAlignmentX(CENTER_ALIGNMENT);
        sidebar.add(zoom);

        // Checkbox for rendering
        final JCheckBox showZones = new JCheckBox();
        showZones.setFocusable(false);
        showZones.setText("Show Zones");
        showZones.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showZone = showZones.isSelected();

                if (mode != ActionMode.ZONE_EDIT) {
                    renderer.setShowZones(showZone);
                    renderer.refresh();
                }
            }
        });
        showZones.setAlignmentX(CENTER_ALIGNMENT);
        sidebar.add(showZones);
        sidebar.add(Box.createVerticalStrut(30));

        JComponent seperator = (JComponent) Box.createRigidArea(new Dimension(150, 10));
        seperator.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK));
        sidebar.add(seperator);

        JLabel toolLabel = new JLabel("Tools");
        toolLabel.setAlignmentX(CENTER_ALIGNMENT);
        toolLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        sidebar.add(toolLabel);
        sidebar.add(Box.createVerticalStrut(10));

        // Tool chooser
        String[] rawModes = new String[ActionMode.values().length];
        ActionMode[] values = ActionMode.values();
        for (int i = 0; i < values.length; i++) {
            rawModes[i] = values[i].getTitle();
        }
        final JComboBox petList = new JComboBox(rawModes);
        petList.setFocusable(false);
        petList.setSelectedIndex(2);
        petList.setMaximumSize(new Dimension(150, 30));
        petList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    setMode(ActionMode.values()[petList.getSelectedIndex()]);
                } catch (Exception err) {
                    EscapistsEditor.fatalError(err);
                }
            }
        });
        sidebar.add(petList);

        // Add the options
        sidebar.add(Box.createVerticalStrut(20));

        toolOptions = new JPanel();
        toolOptions.setAlignmentX(CENTER_ALIGNMENT);
        toolOptions.setLayout(new BoxLayout(toolOptions, BoxLayout.Y_AXIS));
        sidebar.add(toolOptions);


        setMode(ActionMode.values()[petList.getSelectedIndex()]);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setSize(800 + 150, 600);
        setLocationRelativeTo(null);

        setTitle("Escapists Map Editor v" + EscapistsEditor.VERSION + " - " +
                (mapToEdit == null ? "No map loaded" : mapToEdit.getName()));
        setIconImage(ImageIO.read(getClass().getResource("/icon.png")));
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException |
                InstantiationException |
                IllegalAccessException |
                UnsupportedLookAndFeelException e) {
            EscapistsEditor.fatalError(e);
        }

        for (java.util.Map.Entry<Object, Object> entry : javax.swing.UIManager.getDefaults().entrySet()) {
            Object key = entry.getKey();
            Object value = javax.swing.UIManager.get(key);
            if (value != null && value instanceof javax.swing.plaf.FontUIResource) {
                javax.swing.plaf.FontUIResource f = new javax.swing.plaf.FontUIResource(Font.DIALOG, Font.PLAIN, 12);
                javax.swing.UIManager.put(key, f);
            }
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SwingUtilities.updateComponentTreeUI(RenderView.this);
                setVisible(true);
            }
        });
    }

    private void updateTiling() throws IOException {
        ArrayList<Object> icons = new ArrayList<>();
        icons.add(new ImageIcon(ImageIO.read(getClass().getResource("/defaulttile.png"))));
        iconPanel.removeAll();
        iconPanel.setLayout(new GridLayout(0, 4));
        tileSelect.setPreferredSize(new Dimension(150, 250));
        if (mapToEdit != null) {
            BufferedImage tiles = mapToEdit.getTilesImage();
            int tileCount = (tiles.getWidth() / 16) * (tiles.getHeight() / 16);
            int realSize = (150) / 4 - (5 * 2);
            for (int i = -1; i < tileCount; i++) {
                int tileX = (int) Math.floor(((double) i) / (tiles.getHeight() / 16));
                int tileY = i - (tileX * (tiles.getHeight() / 16));

                int absTileX = tileX * 16;
                int absTileY = tileY * 16;

                BufferedImage newImage;
                if (i > -1) {
                    BufferedImage tileRender = tiles.getSubimage(absTileX, absTileY, 16, 16);
                    // Resize
                    newImage = new BufferedImage(realSize, realSize, BufferedImage.TYPE_INT_RGB);
                    Graphics g = newImage.createGraphics();
                    g.drawImage(tileRender, 0, 0, realSize, realSize, null);
                    g.dispose();
                } else {
                    newImage = ImageIO.read(getClass().getResource("/defaulttile.png"));
                }

                ImageIcon icon = new ImageIcon(newImage, "1");
                final JLabel representation = new JLabel(icon);
                representation.setName("Tile " + (i + 1));
                representation.setMaximumSize(new Dimension(realSize, realSize));
                representation.setMinimumSize(new Dimension(realSize, realSize));
                representation.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                representation.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (selectedTile != null) {
                            selectedTile.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                        }
                        selectedTile = representation;
                        representation.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.BLUE));
                    }
                });
                if (i == -1) {
                    selectedTile = representation;
                    representation.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.BLUE));
                }
                iconPanel.add(representation);
            }
        }
    }

    private void toolHandler(int x, int y) {
        if (mapToEdit == null) {
            return;
        }

        // Get object at that position
        WorldObject clickedObject = mapToEdit.getObjectAt(x, y, currentZone);

        switch (mode) {
            case CREATE_OBJECT:
                // Get ID
                int level = 1;
                if (currentZone.equalsIgnoreCase("Underground")) {
                    level = 0;
                } else if (currentZone.equalsIgnoreCase("Vents")) {
                    level = 2;
                } else if (currentZone.equalsIgnoreCase("Roof")) {
                    level = 3;
                }
                int idToAdd;
                if (id.getSelectedItem().toString().equalsIgnoreCase("Other...")) {
                    String output = JOptionPane.showInputDialog(RenderView.this, "Enter an ID (1-150): ").trim();
                    if (StringUtils.isNumber(output)) {
                        int num = Integer.parseInt(output);
                        if (num > 0 && num < 150) {
                            if (!mapToEdit.isObjectAt(x, y, num, level)) {
                                mapToEdit.getObjects().add(editor.registry.instanceWithUnknown(num, x, y, level));
                                renderer.refresh();
                            }
                        } else {
                            editor.dialog("Invalid ID entered!");
                        }
                    } else {
                        editor.dialog("Invalid ID entered!");
                    }
                    this.x = -1;
                    this.y = -1;
                } else {
                    idToAdd = Integer.parseInt(id.getSelectedItem().toString().split(":")[0].trim());
                    if (idToAdd > 0 && idToAdd < 150) {
                        // Check for existing object
                        if (!mapToEdit.isObjectAt(x, y, idToAdd, level)) {
                            mapToEdit.getObjects().add(editor.registry.instanceWithUnknown(idToAdd, x, y, level));
                            renderer.refresh();
                        }
                    } else {
                        editor.dialog("Invalid ID for object!");
                    }
                }
                break;
            case DELETE_OBJECT:
                mapToEdit.getObjects().remove(clickedObject);
                break;
            case SET_TILE:
                // Get tile
                if (selectedTile != null) {
                    int id = Integer.parseInt(selectedTile.getName().split(" ")[1].trim());
                    for (int relativeX = -bigBrush; relativeX <= bigBrush; relativeX++) {
                        for (int relativeY = -bigBrush; relativeY <= bigBrush; relativeY++) {
                            int myX = x + relativeX;
                            int myY = y + relativeY;
                            if (myX >= 0 && myY >= 0 && myX < mapToEdit.getWidth() && myY < mapToEdit.getHeight()) {
                                try {
                                    mapToEdit.setTile(myX, myY, id, currentZone);
                                } catch (Exception e) {
                                    System.out.println("Error @ setting " + myX + ":" + myY);
                                }
                            }
                        }
                    }
                }
                break;
            case ZONE_EDIT:
                // Handled upstream
                return;
        }

        // Redraw the object
        renderer.refresh();
    }

    public void setMode(ActionMode mode) {
        System.out.println(" - Selecting tool: " + mode.name());
        toolOptions.removeAll();

        renderer.setShowZones(showZone);
        renderer.setEditZones(false);
        switch (mode) {
            case CREATE_OBJECT:
                toolOptions.add(new JLabel("ID:"));
                toolOptions.add(id);
                break;
            case DELETE_OBJECT:
                break;
            case SET_TILE:

                JLabel zoomLabel = new JLabel("Brush size");
                zoomLabel.setAlignmentX(CENTER_ALIGNMENT);
                toolOptions.add(zoomLabel);
                final JSlider zoom = new JSlider(JSlider.HORIZONTAL,
                        0, 5, 0);
                zoom.setFocusable(false);
                zoom.setMajorTickSpacing(1);
                zoom.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        bigBrush = zoom.getValue();
                    }
                });
                zoom.setAlignmentX(CENTER_ALIGNMENT);
                toolOptions.add(zoom);
                toolOptions.add(tileSelect);
                break;
            case ZONE_EDIT:
                renderer.setEditZones(true);
                renderer.setShowZones(true);
                JButton manualEdit = new JButton("Manual...");
                manualEdit.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (mapToEdit != null) {
                            new ZonesDialog(RenderView.this, renderer, mapToEdit);
                        }
                    }
                });
                manualEdit.setAlignmentX(CENTER_ALIGNMENT);
                toolOptions.add(manualEdit);
                break;
        }

        renderer.refresh();

        toolOptions.revalidate();
        toolOptions.repaint();

        this.mode = mode;
    }

    public void setMap(Map newMap) {
        mapToEdit = newMap;
        setTitle("Escapists Map Editor v" + EscapistsEditor.VERSION + " - " +
                (mapToEdit == null ? "No map loaded" : mapToEdit.getName()));
        try {
            updateTiling();
        } catch (IOException e) {
            e.printStackTrace();
        }
        validate();
        repaint();
        renderer.setMap(mapToEdit);
    }
}

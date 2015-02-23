package net.jselby.escapists.editor;

import net.jselby.escapists.EscapistsEditor;
import net.jselby.escapists.Map;
import net.jselby.escapists.MapSelectionGUI;
import net.jselby.escapists.WorldObject;
import net.jselby.escapists.objects.Light;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * The render view shows the render in a window, and has toolbar options addiitonally.
 *
 * @author j_selby
 */
public class RenderView extends JFrame {
    private static final ArrayList<Integer> KEEP_IDS = new ArrayList<>();

    static {
        KEEP_IDS.addAll(Arrays.asList(
                17, 19, 22
        ));
    }

    private final MapRendererComponent renderer;
    private final JTextArea console;
    private final JComboBox tileSelect;
    private final JPanel toolOptions;

    // Tools
    private final JTextField id = new JTextField("0");
    private final JLabel selectedZone = new JLabel("Selected Zone: None");
    private final JTextField selectedZoneEditor = new JTextField();

    private EscapistsEditor editor;
    private Map mapToEdit;

    private int x;
    private int y;

    private ActionMode mode = ActionMode.CREATE_OBJECT;

    private boolean showZone;
    private java.util.Map.Entry<String, Object> selectedZoneElement;

    public RenderView(final EscapistsEditor editor, final Map mapToEdit) throws IOException {
        this.editor = editor;
        this.mapToEdit = mapToEdit;

        // Configure the defaults
        // ID box
        id.setAlignmentX(CENTER_ALIGNMENT);
        id.setMinimumSize(new Dimension(150, 30));
        id.setMaximumSize(new Dimension(150, 30));
        ((AbstractDocument)id.getDocument()).setDocumentFilter(
                new NumberOnlyFilter());

        // Build tiles
        ArrayList<Object> icons = new ArrayList<>();
        icons.add(new ImageIcon(ImageIO.read(getClass().getResource("/defaulttile.png"))));
        if (mapToEdit != null) {
            BufferedImage tiles = mapToEdit.getTilesImage();
            int tileCount = (tiles.getWidth() / 16) * (tiles.getHeight() / 16);
            for (int i = 0; i < tileCount; i++) {
                int tileX = (int) Math.floor(((double) i) / (tiles.getHeight() / 16));
                int tileY = i - (tileX * (tiles.getHeight() / 16));

                int absTileX = tileX * 16;
                int absTileY = tileY * 16;

                BufferedImage tileRender = tiles.getSubimage(absTileX, absTileY, 16, 16);
                ImageIcon icon = new ImageIcon(tileRender, "1");
                icons.add(icon);
            }
        }
        tileSelect = new JComboBox(icons.toArray());
        tileSelect.setSelectedIndex(0);
        tileSelect.setMaximumSize(new Dimension(200, 30));

        // Zone editor
        selectedZone.setAlignmentX(CENTER_ALIGNMENT);
        selectedZoneEditor.setAlignmentX(CENTER_ALIGNMENT);
        selectedZoneEditor.setMinimumSize(new Dimension(150, 30));
        selectedZoneEditor.setMaximumSize(new Dimension(150, 30));
        selectedZoneEditor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                genericEvent(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                genericEvent(e);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                genericEvent(e);
            }

            public void genericEvent(DocumentEvent e) {
                if (selectedZoneEditor.getText().split("_").length == 4) {
                    // Make sure we have numbers
                    for (String str : selectedZoneEditor.getText().split("_")) {
                        try {
                            int number = Integer.parseInt(str);
                            if (number < 0 || number > (96 * 16)) {
                                System.out.println("Invalid number: out of range");
                                return;
                            }
                        } catch (Exception err) {
                            System.out.println("Invalid number: not a number");
                            return;
                        }
                    }
                    selectedZoneElement.setValue(selectedZoneEditor.getText());
                    renderer.refresh();
                }
            }
        });

        // Configure the view
        setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));

        renderer = new MapRendererComponent(mapToEdit, new ClickListener() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Get position

                int newX = e.getX() / 16;
                int newY = e.getY() / 16;
                if (newX != x || newY != y) {
                    x = newX;
                    y = newY;
                } else {
                    return;
                }

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

                toolHandler(x, y);
            }
        });

        JScrollPane scrollArea = new JScrollPane(renderer);
        scrollArea.setWheelScrollingEnabled(true);
        scrollArea.getHorizontalScrollBar().setUnitIncrement(16);
        scrollArea.getVerticalScrollBar().setUnitIncrement(16);
        scrollArea.setPreferredSize(new Dimension(700, 600));
        add(scrollArea);

        // Toolbar
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        Dimension sidebarSize = new Dimension(150, 600);
        sidebar.setPreferredSize(sidebarSize);
        sidebar.setMaximumSize(sidebarSize);
        sidebar.setMinimumSize(sidebarSize);
        sidebar.setAlignmentY(TOP_ALIGNMENT);
        add(sidebar);

        // Toolbar textbox
        console = new JTextArea();
        console.setLineWrap(true);
        console.setAutoscrolls(true);
        console.setEditable(false);
        // Redirect System.out
        System.setOut(new PrintStream(new TextPanePrintStream(console)));
        console.append("Ready!\n");

        JScrollPane scrollPane = new JScrollPane(console);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setMaximumSize(new Dimension(200, 200));

        sidebar.add(scrollPane);
        sidebar.add(Box.createVerticalStrut(50));

        // Toolbar options
        JButton loadMap = new JButton("Load Map");
        loadMap.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
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
        loadMap.setAlignmentX(CENTER_ALIGNMENT);
        loadMap.setMinimumSize(new Dimension(150, 50));
        loadMap.setHorizontalAlignment(SwingConstants.LEADING);
        sidebar.add(loadMap);

        JButton saveMap = new JButton("Save Map");
        saveMap.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Save map
                try {
                    // Get our target
                    JFileChooser fc = new JFileChooser();
                    int dialog = fc.showSaveDialog(RenderView.this);
                    if (JFileChooser.APPROVE_OPTION == dialog) {
                        mapToEdit.save(fc.getSelectedFile());
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        saveMap.setAlignmentX(CENTER_ALIGNMENT);
        saveMap.setMinimumSize(new Dimension(150, 50));
        saveMap.setHorizontalAlignment(SwingConstants.LEADING);
        sidebar.add(saveMap);

        JButton properties = new JButton("Properties");
        properties.setMinimumSize(new Dimension(150, 50));
        properties.setAlignmentX(CENTER_ALIGNMENT);
        properties.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new PropertiesDialog(RenderView.this, renderer, mapToEdit);
            }
        });
        sidebar.add(properties);

        sidebar.add(Box.createVerticalStrut(50));

        // Checkbox for rendering
        final JCheckBox showZones = new JCheckBox();
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

        // Tool chooser
        String[] rawModes = new String[ActionMode.values().length];
        ActionMode[] values = ActionMode.values();
        for (int i = 0; i < values.length; i++) {
            rawModes[i] = values[i].getTitle();
        }
        final JComboBox petList = new JComboBox(rawModes);
        petList.setSelectedIndex(0);
        petList.setMaximumSize(new Dimension(200, 30));
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
        sidebar.add(Box.createVerticalStrut(50));

        toolOptions = new JPanel();
        toolOptions.setAlignmentX(CENTER_ALIGNMENT);
        toolOptions.setLayout(new BoxLayout(toolOptions, BoxLayout.Y_AXIS));
        sidebar.add(toolOptions);


        setMode(ActionMode.values()[0]);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setSize(800 + 150, 600);
        setLocationRelativeTo(null);

        setTitle("Escapists Map Editor");
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
        setVisible(true);
    }

    private void toolHandler(int x, int y) {
        // Get object at that position
        WorldObject clickedObject = mapToEdit.getObjectAt(x, y);

        System.out.println(mode.name() + " @" + x + ":" + y);
        switch (mode) {
            case CREATE_OBJECT:
                // Get ID
                int idToAdd = id.getText().trim().length() == 0 ? 0 : Integer.parseInt(id.getText());
                if (idToAdd > 0 && idToAdd < 100) {
                    mapToEdit.getObjects().add(editor.registry.instanceWithUnknown(idToAdd, x, y));
                } else {
                    System.out.println("Invalid ID");
                }
                break;
            case DELETE_OBJECT:
                mapToEdit.getObjects().remove(clickedObject);
                break;
            case INFO_OBJECT:
                System.out.println(clickedObject);
                break;
            case SET_TILE:
                // Get tile
                mapToEdit.setTile(x, y,
                tileSelect.getSelectedIndex());
                break;
            case ZONE_EDIT:
                // Get the zone at this point
                int size = Integer.MAX_VALUE;
                for (java.util.Map.Entry<String, Object> values : mapToEdit.getMap("Zones").entrySet()) {
                    String value = ((String) values.getValue()).trim();
                    if (!value.contains("_")) {
                        continue;
                    }
                    String[] args = value.split("_");
                    int x1 = Integer.parseInt(args[0]);
                    int y1 = Integer.parseInt(args[1]);
                    int x2 = Integer.parseInt(args[2]);
                    int y2 = Integer.parseInt(args[3]);
                    int absX = x * 16;
                    int absY = y * 16;
                    int mySize = (x2 - x1) * (y2 - y1);
                    if (absX > x1 && absX < x2 && absY > y1 && absY < y2 && mySize < size) {
                        size = mySize;
                        selectedZoneElement = values;
                    }
                }
                selectedZoneEditor.setText(selectedZoneElement != null
                        ? selectedZoneElement.getValue().toString().trim() : null);
                selectedZoneEditor.setEditable(selectedZoneElement != null);
                selectedZone.setText("Selected Zone: " +
                        (selectedZoneElement == null ? "None" : selectedZoneElement.getKey()));
                renderer.setSelectedZone(selectedZoneElement != null ? selectedZoneElement.getKey() : null);
                renderer.refresh();
                break;
        }

        // Redraw the object
        renderer.refresh();
    }

    public void setMode(ActionMode mode) {
        toolOptions.removeAll();

        renderer.setShowZones(showZone);
        renderer.setSelectedZone(null);
        switch (mode) {
            case CREATE_OBJECT:
                toolOptions.add(new JLabel("ID:"));
                toolOptions.add(id);
                break;
            case DELETE_OBJECT:
                break;
            case INFO_OBJECT:
                break;
            case SET_TILE:
                toolOptions.add(tileSelect);
                break;
            case ZONE_EDIT:
                renderer.setShowZones(true);
                toolOptions.add(selectedZone);
                toolOptions.add(selectedZoneEditor);
                break;
            case CLEAR_ALL_TILES:
                JLabel warning = new JLabel("This will wipe out ALL tiles!");
                JLabel warning2 = new JLabel("(And objects safe to delete)");
                JButton button = new JButton("Continue anyway...");
                button.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Delete all tiles
                        for (int y = 0; y < mapToEdit.getHeight(); y++) {
                            for (int x = 0; x < mapToEdit.getWidth(); x++) {
                                mapToEdit.setTile(x, y, 0);
                            }
                        }
                        mapToEdit.setTile(0, 0, 1); // Game renderer crashs without 1 block

                        // Delete safe objects
                        java.util.List<WorldObject> worldObjects = mapToEdit.getObjects();
                        for (WorldObject object :
                                worldObjects.toArray(new WorldObject[worldObjects.size()])) {
                            if (!KEEP_IDS.contains(object.getID())) {
                                worldObjects.remove(object);
                            }
                        }

                        renderer.refresh();
                    }
                });
                warning.setAlignmentX(CENTER_ALIGNMENT);
                warning2.setAlignmentX(CENTER_ALIGNMENT);
                button.setAlignmentX(CENTER_ALIGNMENT);
                toolOptions.add(warning);
                toolOptions.add(warning2);
                toolOptions.add(button);
        }

        renderer.refresh();

        toolOptions.revalidate();
        toolOptions.repaint();

        this.mode = mode;
    }
}

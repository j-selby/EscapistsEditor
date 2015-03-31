package net.jselby.escapists.mapping;

import net.jselby.escapists.EscapistsEditor;
import net.jselby.escapists.filters.PreFilters;
import net.jselby.escapists.mapping.store.PropertiesFile;
import net.jselby.escapists.mapping.store.PropertiesSection;
import net.jselby.escapists.objects.ObjectRegistry;
import net.jselby.escapists.objects.Objects;
import net.jselby.escapists.objects.WorldObject;
import net.jselby.escapists.utils.BlowfishCompatEncryption;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * A map contains elements of a map.
 *
 * @author j_selby
 */
public class Map {
    public static final int DEFAULT_WIDTH = 96;
    public static final int DEFAULT_HEIGHT = 93;

    private int width;
    private int height;

    private PropertiesFile sections;

    private int[][] tiles;
    private int[][] vents;
    private int[][] roof;
    private int[][] underground;

    private List<WorldObject> objects = new ArrayList<>();

    private final BufferedImage tilesImage;
    private final BufferedImage backgroundImage;

    /**
     * Decodes a map.
     *
     * @param editor The editor to pull resources from
     * @param registry The registry to spawn objects with
     * @param filename The filename of this map
     * @param content The content of this map, decrypted.
     * @throws IOException
     */
    public Map(EscapistsEditor editor, ObjectRegistry registry,
               String filename, String content) throws IOException {
        System.out.println(" - Decoding map \"" + filename + "\"...");
        this.sections = new PropertiesFile(content);

        // Get the raw filename
        String rawName = new File(filename).getName().split("\\.")[0];

        // Decode tiles, if it exists
        if (sections.containsSection("Tiles")) {
            // Decode!
            PropertiesSection section = sections.getSection("Tiles");
            tiles = compileIDSection(section);

        } else {
            tiles = new int[0][0];
            System.out.println(" > Map decode warning: No tiles found.");
        }

        if (sections.containsSection("Vents")) {
            // Decode!
            PropertiesSection section = sections.getSection("Vents");
            vents = compileIDSection(section);
        } else {
            vents = new int[0][0];
            System.out.println(" > Map decode warning: No vents found.");
        }

        if (sections.containsSection("Roof")) {
            // Decode!
            PropertiesSection section = sections.getSection("Roof");
            roof = compileIDSection(section);
        } else {
            roof = new int[0][0];
            System.out.println(" > Map decode warning: No roof found.");
        }

        if (sections.containsSection("Underground")) {
            // Decode!
            PropertiesSection section = sections.getSection("Underground");
            underground = compileIDSection(section);
        } else {
            underground = new int[0][0];
            System.out.println(" > Map decode warning: No underground found.");
        }


        // Decode Object entities
        if (sections.containsSection("Objects")) {
            for (java.util.Map.Entry<String, Object> object :
                    sections.getSection("Objects").entrySet()) {
                // Find this object
                String[] args = ((String)object.getValue()).trim().split("x");

                int x = Integer.parseInt(args[0]);
                int y = Integer.parseInt(args[1]);
                int id = Integer.parseInt(args[2]);
                int level = Integer.parseInt(args[3]);

                if (id == 0) {
                    continue;
                }

                // From the Registry
                Class<? extends WorldObject> classOfObject = registry.objects.get(id);
                if (classOfObject != null) {
                    try {
                        Constructor<? extends WorldObject> constructor
                                = classOfObject.getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE);
                        WorldObject instance = constructor.newInstance(x, y, level);
                        objects.add(instance);
                    } catch (InvocationTargetException
                            | NoSuchMethodException
                            | IllegalAccessException
                            | InstantiationException e) {
                        System.err.println(" > Map decode warning: Failed to create entity: " + id + " @ "
                                + x + ":" + y + " (" + (x * 16) + ":" + (y * 16) + ")");
                        e.printStackTrace();
                    }
                } else {
                    objects.add(new WorldObject.Unknown(x, y, id, level));
                    if (EscapistsEditor.DEBUG) {
                        System.out.println(" > Map decode warning: Unknown entity: " + id + " @ "
                                + x + ":" + y + " (" + (x * 16) + ":" + (y * 16) + ")");
                    }
                }
            }
        } else {
            System.out.println(" > Map decode warning: No objects/entities found.");
        }

        // Decode tiles for this
        File tilesFile = new File(editor.escapistsPath, "Data" +
                File.separator + "images" + File.separator + "tiles_" + rawName + ".gif");
        if (!tilesFile.exists()) {
            System.out.println(" > No tiles found for \"" + rawName + "\". Using \"perks\".");
            tilesFile = new File(editor.escapistsPath, "Data" +
                    File.separator + "images" + File.separator + "tiles_perks.gif");
        }

        File background = new File(editor.escapistsPath, "Data" +
                File.separator + "images" + File.separator + "ground_" + rawName + ".gif");
        if (!background.exists()) {
            System.out.println(" > No background found for \"" + rawName + "\". Using \"perks\".");
            background = new File(editor.escapistsPath, "Data" +
                    File.separator + "images" + File.separator + "ground_perks.gif");
        }

        // Decrypt the tiles themselves
        tilesImage = ImageIO.read(new ByteArrayInputStream(BlowfishCompatEncryption.decrypt(tilesFile)));
        backgroundImage = ImageIO.read(background);

        // Pre filter me
        PreFilters.run(this);
        if (tiles.length > 0) {
            width = tiles[0].length - 1;
            height = tiles.length;
        }

        System.out.println(" > Done.");
    }

    private int[][] compileIDSection(PropertiesSection section) {
        // Get first row, so we can work out width
        String firstRow = (String) section.get("0");
        width = firstRow.split("_").length - 1;
        height = section.size();

        // This is stored in a y-x format, so that it can be more easily converted to its original format.
        // This is flipped by most get methods, so this should be abstract.
        int[][] tiles = new int[height][width];

        for (java.util.Map.Entry<String, Object> tile : section.entrySet()) {
            int heightPos = Integer.parseInt(tile.getKey());
            String row = (String) tile.getValue();
            String[] values = row.split("_");
            int[] rowDecompiled = new int[values.length];

            int widthPos = -1;
            for (String value : values) {
                widthPos++;
                if (value.trim().length() < 1) {
                    continue;
                }

                //System.out.println(widthPos);
                rowDecompiled[widthPos] = Integer.parseInt(value);
            }

            tiles[heightPos] = rowDecompiled;
        }

        return tiles;
    }

    public String getName() {
        return (String) get("Info.MapName");
    }

    public Object get(String key) {
        // Get first .
        String section = key.split("\\.")[0];
        key = key.substring(key.indexOf(section) + section.length() + 1);

        PropertiesSection discoveredSection = sections.getSection(section);
        if (discoveredSection != null) {
            return discoveredSection.get(key);
        } else {
            return null;
        }
    }

    public void setMap(String key, PropertiesSection value) {
        sections.setSection(key, value);
    }

    public PropertiesSection getMap(String key) {
        return sections.getSection(key);
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getTile(int x, int y) {
        return tiles[y][x];
    }

    public int getTile(int x, int y, String view) {
        if (x >= width || y >= height) {
            throw new IllegalArgumentException("Fetching bad location @ " + x + ":" + y);
        }
        if (view.equalsIgnoreCase("World")) {
            return tiles[y][x];
        } else if (view.equalsIgnoreCase("Underground")) {
            return underground[y][x];
        } else if (view.equalsIgnoreCase("Vents")) {
            return vents[y][x];
        } else if (view.equalsIgnoreCase("Roof")) {
            return roof[y][x];
        } else {
            return 0;
        }
    }

    public List<WorldObject> getObjects() {
        return objects;
    }

    public BufferedImage getTilesImage() {
        return tilesImage;
    }

    public BufferedImage getBackgroundImage() {
        return backgroundImage;
    }

    public void setTile(int x, int y, int value) {
        tiles[y][x] = value;
    }

    public void setTile(int x, int y, int selectedIndex, String view) {
        if (view.equalsIgnoreCase("World")) {
            tiles[y][x] = selectedIndex;
        } else if (view.equalsIgnoreCase("Underground")) {
            underground[y][x] = selectedIndex;
        } else if (view.equalsIgnoreCase("Vents")) {
            vents[y][x] = selectedIndex;
        } else if (view.equalsIgnoreCase("Roof")) {
            roof[y][x] = selectedIndex;
        }
    }

    public int count(Objects object) {
        return count(object.getID());
    }

    public int count(int id) {
        int count = 0;
        for (WorldObject object : objects) {
            if (object.getID() == id) {
                count++;
            }
        }
        return count;
    }

    public void save(File selectedFile) throws IOException {
        // Do checks
        if (count(Objects.AI_WP_GUARD_ROLLCALL) == 0) {
            throw new IOException("Compile Error: Invalid amount of rollcall guard waypoints - \n" +
                    "You need more then 1.");
        }
        if (count(Objects.AI_WP_GUARD_GENERAL) == 0) {
            throw new IOException("Compile Error: Invalid amount of general guard waypoints - \nYou need more then 1.");
        }

        // Count tiles, more then 1
        boolean foundTile = false;
        for (int[] row : tiles) {
            for (int tile : row) {
                if (tile > 0) {
                    foundTile = true;
                    break;
                }
            }
        }
        if (!foundTile) {
            throw new IOException("Compile Error: You need at least 1 tile in the world!");
        }

        System.out.println("Saving...");

        // Serialize tiles
        sections.getSection("Tiles").clear();
        for (int y = 0; y < tiles.length; y++) {
            String arrayBuild = "";
            for (int x = 0; x < tiles[y].length; x++) {
                arrayBuild += tiles[y][x] + "_";
            }
            sections.getSection("Tiles").put(y + "", arrayBuild);
        }

        sections.getSection("Underground").clear();
        for (int y = 0; y < underground.length; y++) {
            String arrayBuild = "";
            for (int x = 0; x < underground[y].length; x++) {
                arrayBuild += underground[y][x] + "_";
            }
            sections.getSection("Underground").put(y + "", arrayBuild);
        }

        sections.getSection("Vents").clear();
        for (int y = 0; y < vents.length; y++) {
            String arrayBuild = "";
            for (int x = 0; x < vents[y].length; x++) {
                arrayBuild += vents[y][x] + "_";
            }
            sections.getSection("Vents").put(y + "", arrayBuild);
        }

        sections.getSection("Roof").clear();
        for (int y = 0; y < roof.length; y++) {
            String arrayBuild = "";
            for (int x = 0; x < roof[y].length; x++) {
                arrayBuild += roof[y][x] + "_";
            }
            sections.getSection("Roof").put(y + "", arrayBuild);
        }

        // Serialize Objects
        int count = 1;
        sections.getSection("Objects").clear();
        for (WorldObject worldObject : objects) {
            int x = worldObject.getX();
            int y = worldObject.getY();
            int id = worldObject.getID();
            int argument = worldObject.getIDArgument();

            if (id != 0) {
                sections.getSection("Objects").put(count + "", x + "x" + y + "x" + id + "x" + argument);
                count++;
            }
        }

        // Build sections
        String allSections = sections.toString();

        // Save it
        byte[] encryptedBytes = BlowfishCompatEncryption.encrypt(allSections.getBytes());

        System.out.println("Writing to " + selectedFile.getPath());
        try (FileOutputStream out = new FileOutputStream(selectedFile)) {
            out.write(encryptedBytes);
            out.flush();
        }
    }

    public WorldObject getObjectAt(int x, int y, String view) {
        int level = 1;
        if (view.equalsIgnoreCase("Underground")) {
            level = 0;
        } else if (view.equalsIgnoreCase("Vents")) {
            level = 2;
        } else if (view.equalsIgnoreCase("Roof")) {
            level = 3;
        }

        for (WorldObject object : objects) {
            if (object.getX() == x && object.getY() == y && object.getIDArgument() == level) {
                return object;
            }
        }
        return null;
    }

    public void set(String key, Object value) {
        // Get first .
        String section = key.split("\\.")[0];
        key = key.substring(key.indexOf(section) + section.length() + 1);

        PropertiesSection discoveredSection = sections.getSection(section);
        if (discoveredSection != null) {
            discoveredSection.put(key, value);
        }
    }

    public boolean isObjectAt(int x, int y, int id, int layer) {
        for (WorldObject obj : objects) {
            if (obj.getID() == id && obj.getX() == x
                    && obj.getY() == y && obj.getIDArgument() == layer) {
                return true;
            }
        }
        return false;
    }

    public int[][] getTiles(String section) {
        if (section.equalsIgnoreCase("World")) {
            return tiles;
        } else if (section.equalsIgnoreCase("Underground")) {
            return underground;
        } else if (section.equalsIgnoreCase("Vents")) {
            return vents;
        } else if (section.equalsIgnoreCase("Roof")) {
            return roof;
        } else {
            return null;
        }
    }

    public void setTiles(String section, int[][] tiles) {
        if (section.equalsIgnoreCase("World")) {
            this.tiles = tiles;
        } else if (section.equalsIgnoreCase("Underground")) {
            this.underground = tiles;
        } else if (section.equalsIgnoreCase("Vents")) {
            this.vents = tiles;
        } else if (section.equalsIgnoreCase("Roof")) {
            this.roof = tiles;
        }
    }
}

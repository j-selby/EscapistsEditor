package net.jselby.escapists;

import net.jselby.escapists.utils.BlowfishCompatEncryption;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * A map contains elements of a map.
 *
 * @author j_selby
 */
public class Map {
    private static final String COMPILE_ERROR = "Compile Error: ";

    private final int[][] tiles;
    private final int[][] vents;
    private final int[][] roof;
    private final int[][] underground;

    private int width;
    private int height;

    private final BufferedImage tilesImage;
    private final BufferedImage backgroundImage;

    private java.util.Map<String, java.util.Map<String, Object>> sections = new HashMap<>();

    private List<WorldObject> objects = new ArrayList<>();
    private EscapistsEditor editor;
    private String filename;

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
        this.editor = editor;
        this.filename = filename;
        System.out.println("Decoding map \"" + filename + "\"...");

        // Get the raw filename
        String rawName = new File(filename).getName().split("\\.")[0];

        String currentSection = null;
        java.util.Map<String, Object> currentSectionMap = null;

        int lineNum = 0;

        for (String line : content.split("\n")) {
            lineNum++;

            // Discover what this is
            String command = line.trim();

            if (command.startsWith("[") && command.endsWith("]")) {
                // New section
                // Get what its name is
                currentSection = command.substring(1, command.length() - 1);

                currentSectionMap = new LinkedHashMap<>();
                sections.put(currentSection, currentSectionMap);
                continue;

            } else if (command.startsWith("[")) {
                System.out.println("Map decode warning: Invalid syntax (\"[\") @ line " + lineNum);
                continue;

            }

            // OK: Read this key
            if (line.contains("=")) {
                if (currentSection == null) {
                    System.out.println("Map decode warning: No section defined @ line " + lineNum);
                    continue;
                }

                // Yay
                int index = line.indexOf("=");
                String key = line.substring(0, index);
                String value = line.substring(index + 1);
                currentSectionMap.put(key, value);

            } else {
                if (line.trim().length() > 0) {
                    System.out.println("Map decode warning: Invalid syntax (no \"*=*\" or \"[*]\") @ line " + lineNum);
                }
            }
        }

        // Decode tiles, if it exists
        if (sections.containsKey("Tiles")) {
            // Decode!
            java.util.Map<String, Object> section = sections.get("Tiles");
            tiles = compileIDSection("Overworld", section);

        } else {
            tiles = new int[0][0];
            System.out.println("Map decode warning: No tiles found.");
        }

        if (sections.containsKey("Vents")) {
            // Decode!
            java.util.Map<String, Object> section = sections.get("Vents");
            vents = compileIDSection("Vents", section);
        } else {
            vents = new int[0][0];
            System.out.println("Map decode warning: No vents found.");
        }

        if (sections.containsKey("Roof")) {
            // Decode!
            java.util.Map<String, Object> section = sections.get("Roof");
            roof = compileIDSection("Roof", section);
        } else {
            roof = new int[0][0];
            System.out.println("Map decode warning: No roof found.");
        }

        if (sections.containsKey("Underground")) {
            // Decode!
            java.util.Map<String, Object> section = sections.get("Underground");
            underground = compileIDSection("Underground", section);
        } else {
            underground = new int[0][0];
            System.out.println("Map decode warning: No underground found.");
        }


        // Decode Object entities
        if (sections.containsKey("Objects")) {
            for (java.util.Map.Entry<String, Object> object :
                    sections.get("Objects").entrySet()) {
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
                        System.err.println("Map decode warning: Failed to create entity: " + id + " @ "
                                + x + ":" + y + " (" + (x * 16) + ":" + (y * 16) + ")");
                        e.printStackTrace();
                    }
                } else {
                    objects.add(new WorldObject.Unknown(x, y, id, level));
                    if (EscapistsEditor.DEBUG) {
                        System.out.println("Map decode warning: Unknown entity: " + id + " @ "
                                + x + ":" + y + " (" + (x * 16) + ":" + (y * 16) + ")");
                    }
                }
            }
        } else {
            System.out.println("Map decode warning: No objects/entities found.");
        }

        // Decode tiles for this
        File tilesFile = new File(editor.escapistsPath, "Data" +
                File.separator + "images" + File.separator + "tiles_" + rawName + ".gif");
        if (!tilesFile.exists()) {
            System.out.println("No tiles found for \"" + rawName + "\". Using \"perks\".");
            tilesFile = new File(editor.escapistsPath, "Data" +
                    File.separator + "images" + File.separator + "tiles_perks.gif");
        }

        File background = new File(editor.escapistsPath, "Data" +
                File.separator + "images" + File.separator + "ground_" + rawName + ".gif");
        if (!background.exists()) {
            System.out.println("No background found for \"" + rawName + "\". Using \"perks\".");
            background = new File(editor.escapistsPath, "Data" +
                    File.separator + "images" + File.separator + "ground_perks.gif");
        }

        // Decrypt the tiles themselves
        tilesImage = ImageIO.read(new ByteArrayInputStream(BlowfishCompatEncryption.decrypt(tilesFile)));
        backgroundImage = ImageIO.read(background);
    }

    private int[][] compileIDSection(String name, java.util.Map<String, Object> section) {
        // Get first row, so we can work out width
        String firstRow = (String) section.get("0");
        width = firstRow.split("_").length - 1;
        height = section.size();
        System.out.println("Section \"" + name + "\" size: " + width + " * " + height);

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

        java.util.Map<String, Object> discoveredSection = sections.get(section);
        if (discoveredSection != null) {
            return discoveredSection.get(key);
        } else {
            return null;
        }
    }

    public java.util.Map<String, Object> getMap(String key) {
        return sections.get(key);
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

    public int count(ObjectRegistry.Objects object) {
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
        if (count(ObjectRegistry.Objects.AI_WP_GUARD_ROLLCALL) == 0) {
            throw new IOException(COMPILE_ERROR + "Invalid amount of rollcall guard waypoints - \n" +
                    "You need more then 1.");
        }
        if (count(ObjectRegistry.Objects.AI_WP_GUARD_GENERAL) == 0) {
            throw new IOException(COMPILE_ERROR + "Invalid amount of general guard waypoints - \nYou need more then 1.");
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
            throw new IOException(COMPILE_ERROR + "You need at least 1 tile in the world!");
        }

        System.out.println("Saving...");

        // Serialize tiles
        String allSections = "";

        allSections += "[Tiles]\n";
        for (int y = 0; y < tiles.length; y++) {
            String arrayBuild = "";
            for (int x = 0; x < tiles[y].length; x++) {
                arrayBuild += tiles[y][x] + "_";
            }
            allSections += y + "=" + arrayBuild + "\n";
        }

        allSections += "[Underground]\n";
        for (int y = 0; y < underground.length; y++) {
            String arrayBuild = "";
            for (int x = 0; x < underground[y].length; x++) {
                arrayBuild += underground[y][x] + "_";
            }
            allSections += y + "=" + arrayBuild + "\n";
        }

        allSections += "[Vents]\n";
        for (int y = 0; y < vents.length; y++) {
            String arrayBuild = "";
            for (int x = 0; x < vents[y].length; x++) {
                arrayBuild += vents[y][x] + "_";
            }
            allSections += y + "=" + arrayBuild + "\n";
        }

        allSections += "[Roof]\n";
        for (int y = 0; y < roof.length; y++) {
            String arrayBuild = "";
            for (int x = 0; x < roof[y].length; x++) {
                arrayBuild += roof[y][x] + "_";
            }
            allSections += y + "=" + arrayBuild + "\n";
        }

        // Serialize Objects
        allSections += "[Objects]\n";
        int count = 1;
        for (WorldObject worldObject : objects) {
            int x = worldObject.getX();
            int y = worldObject.getY();
            int id = worldObject.getID();
            int argument = worldObject.getIDArgument();

            if (id != 0) {
                String idString = x + "x" + y + "x" + id + "x" + argument;
                allSections += count + "=" + idString + "\n";
                count++;
            }
        }


        // Build sections
        for (java.util.Map.Entry<String, java.util.Map<String, Object>> entry : sections.entrySet()) {
            String sectionId = entry.getKey();
            if (sectionId.equalsIgnoreCase("Tiles") || sectionId.equalsIgnoreCase("Objects")
                    || sectionId.equalsIgnoreCase("Underground") || sectionId.equalsIgnoreCase("Vents")
                    || sectionId.equalsIgnoreCase("Roof")) {
                continue;
            }
            java.util.Map<String, Object> entries = entry.getValue();

            allSections += "[" + sectionId + "]\n";
            for (java.util.Map.Entry<String, Object> subEntry : entries.entrySet()) {
                allSections += subEntry.getKey() + "=" + subEntry.getValue() + "\n";
            }

        }

        // Save it
        File temp = new File(".temp.map");
        try (FileOutputStream out = new FileOutputStream(temp)) {
            IOUtils.write(allSections, out);
        }
        byte[] encryptedBytes = BlowfishCompatEncryption.encrypt(temp);
        temp.delete();

        System.out.println("Writing to " + selectedFile.getPath());
        try (FileOutputStream out = new FileOutputStream(selectedFile)) {
            out.write(encryptedBytes);
            out.flush();
        }
    }

    public WorldObject getObjectAt(int x, int y) {
        for (WorldObject object : objects) {
            if (object.getX() == x && object.getY() == y) {
                return object;
            }
        }
        return null;
    }

    public void set(String key, Object value) {
        // Get first .
        String section = key.split("\\.")[0];
        key = key.substring(key.indexOf(section) + section.length() + 1);

        java.util.Map<String, Object> discoveredSection = sections.get(section);
        if (discoveredSection != null) {
            discoveredSection.put(key, value);
        }
    }
}

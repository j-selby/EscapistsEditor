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
        String texName = (String) get("Info.Tileset");
        File tilesFile = new File(editor.escapistsPath, "Data" +
                File.separator + "images" + File.separator + "tiles_" + texName + ".gif");
        if (!tilesFile.exists()) {
            System.out.println(" > No tiles found for \"" + texName + "\". Using \"perks\".");
            tilesFile = new File(editor.escapistsPath, "Data" +
                    File.separator + "images" + File.separator + "tiles_perks.gif");
        }

        File background = new File(editor.escapistsPath, "Data" +
                File.separator + "images" + File.separator + "ground_" + texName + ".gif");
        if (!background.exists()) {
            System.out.println(" > No background found for \"" + texName + "\". Using \"perks\".");
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

        if (!selectedFile.getName().toLowerCase().endsWith(".proj")) {
            // Do checks
            // Correct points for guards
            if (count(Objects.AI_WP_GUARD_ROLLCALL) != 3) {
                throw new IOException("Compile Error: Invalid amount of rollcall guard waypoints - \n" +
                        "You need 3.");
            }
            if (count(Objects.AI_WP_GUARD_MEALS) != 3) {
                throw new IOException("Compile Error: Invalid amount of meal guard waypoints - \n" +
                        "You need 3.");
            }
            if (count(Objects.AI_WP_GUARD_EXERCISE) != 3) {
                throw new IOException("Compile Error: Invalid amount of exercise guard waypoints - \n" +
                        "You need 3.");
            }
            if (count(Objects.AI_WP_GUARD_SHOWERS) != 3) {
                throw new IOException("Compile Error: Invalid amount of shower guard waypoints - \n" +
                        "You need 3.");
            }
            if (count(Objects.GUARD_BED) < 2) {
                throw new IOException("Compile Error: Invalid amount of guard beds - \n" +
                        "You need more than 1.");
            }
            if (count(Objects.AI_WP_GUARD_GENERAL) < 5) {
                throw new IOException("Compile Error: Invalid amount of general guard waypoints - \nYou need more than 4.");
            }

            // Workout equipment
            int count = 0;
            for (Objects object : Objects.values()) {
                if (object.name().toLowerCase().startsWith("training")) {
                    count += count(object);
                }
            }
            int required = Integer.parseInt(get("Info.Inmates").toString());
            if (count < required) {
                throw new IOException("Compile Error: Invalid amount of training objects - \nYou need more than " + (count - 1) + ".");
            }

            // Player stuff
            if (count(Objects.FORCED_PRISONER_BED) != 1) {
                throw new IOException("Compile Error: You need a single forced prisoner bed for the player!");
            }
            if (count(Objects.FORCED_PRISONER_DESK) != 1) {
                throw new IOException("Compile Error: You need a single forced prisoner desk for the player!");
            }
            if (count(Objects.SOLITARY_BED) == 0) {
                throw new IOException("Compile Error: You need at least 1 solitary bed!");
            }
            if (count(Objects.MEDICAL_BED) == 0) {
                throw new IOException("Compile Error: You need at least 1 medical bed!");
            }

            // Other desk stuff
            if (count(Objects.BED) != (required - 1)) {
                throw new IOException("Compile Error: You need " + (required - 1) + " general beds for prisoners!");
            }

            // Lights
            if (count(Objects.LIGHT) == 0) {
                throw new IOException("Compile Error: You need some Light objects!");
            }

            // Food trays
            if (count(Objects.SERVING_TABLE) < 3) {
                throw new IOException("Compile Error: You need at least 3 serving tables!");
            }
            if (count(Objects.AI_WP_PRISONER_MEALS) == 0) {
                throw new IOException("Compile Error: You need a prisoner meals waypoint!");
            }
            if (count(Objects.CHAIR) < (required - 1)) {
                throw new IOException("Compile Error: You need at least " + (required - 1) + " chairs in the canteen!");
            }

            // Warden name
            if (get("Info.Warden") == null || get("Info.Warden").toString().length() == 0) {
                throw new IOException("Compile Error: You need a warden name!");
            }

            // Other NPC prisoners
            if (count(Objects.AI_WP_PRISONER_ROLLCALL) != (required - 1)) {
                throw new IOException("Compile Error: You need " + (required - 1) + " rollcall waypoints for prisoners!");
            }
            if (count(Objects.BED) != (required - 1)) {
                throw new IOException("Compile Error: You need " + (required - 1) + " standard beds for non-player prisoners!");
            }
            if (count(Objects.PERSONAL_DESK) != (required - 1)) {
                throw new IOException("Compile Error: You need " + (required - 1) + " standard personal desks for non-player prisoners!");
            }
            if (count(Objects.AI_WP_PRISONER_GENERAL) < 5) {
                throw new IOException("Compile Error: You need at least 5 general waypoints for prisoners!");
            }
            if (count(Objects.AI_NPC_SPAWN) != 1) {
                throw new IOException("Compile Error: You need a single AI Npc Spawn point!");
            }
            if (count(Objects.AI_WP_DOCTOR_WORK) != 1) {
                throw new IOException("Compile Error: You need a single AI Doctor Work waypoint!");
            }
            if (count(Objects.AI_WP_EMPLOYMENT_OFFICER) != 1) {
                throw new IOException("Compile Error: You need a single AI Employment Officer waypoint!");
            }

            // TODO: Zones
            /**
             25) ZONES: Solitary missing
             26) ZONES: Player cell missing
             27) ZONES: Rollcall missing
             28) ZONES: Canteen missing
             29) ZONES: Showers missing
             30) ZONES: Gym missing
             31) ZONES: Cells1 missing
             */
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
        int objCount = 1;
        sections.getSection("Objects").clear();
        for (WorldObject worldObject : objects) {
            int x = worldObject.getX();
            int y = worldObject.getY();
            int id = worldObject.getID();
            int argument = worldObject.getIDArgument();

            if (id != 0) {
                sections.getSection("Objects").put(objCount + "", x + "x" + y + "x" + id + "x" + argument);
                objCount++;
            }
        }

        // If this is a project, set it up
        if (selectedFile.getName().toLowerCase().endsWith(".proj")) {
            set("Info.Custom", -1);
        } else {
            set("Info.Custom", 2);
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

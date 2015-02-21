package net.jselby.escapists;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import net.jselby.escapists.editor.RenderView;
import net.jselby.escapists.utils.BlowfishCompatEncryption;
import net.jselby.escapists.utils.SteamFinder;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

/**
 * The main entry point for the EscapistsEditor.
 *
 * @author j_selbys
 */
public class EscapistsEditor {
    public static final boolean DEBUG = false;

    // -- Arguments
    @Parameter(names = "--decrypt", description = "Decrypts the passed file.")
    private String decryptFile;

    @Parameter(names = "--encrypt", description = "Encrypts the passed file.")
    private String encryptFile;

    @Parameter(names = "--encrypt-and-install", description = "Encrypts and installs the passed file.")
    private String encryptAndInstallFile;

    @Parameter(names = "--render", description = "Renders the passed file.")
    private String renderMap;

    @Parameter(names = "--edit", description = "Edits the passed unencrypted map.")
    private String editMap;

    @Parameter(names = "--help", description = "Shows this help.", hidden = true)
    private boolean showHelp;

    @Parameter(names = "--render-all", description = "Renders all maps.")
    private boolean renderAll;


    // -- Internal vars
    /**
     * The root directory of Escapists
     */
    public File escapistsPath;

    /**
     * Create a new default registry for objects
     */
    public ObjectRegistry registry;
    private static boolean showGUI;

    private void start() {
        System.out.println("The Escapists Editor v0.1");
        System.out.println("By jselby");

        registry = new ObjectRegistry("net.jselby.escapists.objects");

        // Discover Escapists directory
        File steamPath = SteamFinder.getSteamPath();

        if (steamPath == null) {
            fatalError("Failed to discover Steam installation.");
        }

        // Check that Escapists is installed
        escapistsPath =
                new File(steamPath, "SteamApps" + File.separator + "common" + File.separator + "The Escapists");
        if (!escapistsPath.exists()) {
            fatalError("Escapists is not installed @ " + escapistsPath.getPath());
        }

        // Parse arguments
        System.out.println("=====================");
    }

    private static void fatalError(String s) {
        System.err.println(s);
        if (showGUI) {
            JOptionPane.showMessageDialog(null, s);
        }
        System.exit(1);
    }

    public void dialog(String s) {
        System.out.println(s);
        if (showGUI) {
            JOptionPane.showMessageDialog(null, s);
        }
    }

    public String[] getMaps() {
        File mapsDir = new File(escapistsPath, "Data" + File.separator + "Maps");
        ArrayList<String> maps = new ArrayList<>();
        for (File file : mapsDir.listFiles()) {
            if (file.isFile()) {
                maps.add(file.getName());
            }
        }
        for (File file : new File(escapistsPath, "Data" + File.separator +
                "Maps" + File.separator + "Custom").listFiles()) {
            if (file.isFile()) {
                maps.add("Custom" + File.separator + file.getName());
            }
        }
        System.out.println("Listing " + maps.size() + " maps.");
        return maps.toArray(new String[maps.size()]);
    }

    public void dump(String name) throws IOException {
        if (!name.endsWith(".cmap") && !name.endsWith(".map")) {
            name += ".map";
        }

        File mapPath = new File(name);
        if (!mapPath.exists()) {
            mapPath = new File(escapistsPath, "Data" + File.separator + "Maps" + File.separator + name);
            if (!mapPath.exists()) {
                dialog("Map \"" + name.trim() + "\" not found.");
                return;
            }
        }

        // Decrypt it
        String content = new String(BlowfishCompatEncryption.decrypt(mapPath));
        File decryptedMap = new File(name.split("\\.")[0] + ".decrypted.map");
        try (FileOutputStream out = new FileOutputStream(decryptedMap)) {
            IOUtils.write(content, out);
        }
    }

    public void render(String name) throws IOException {
        if (!name.endsWith(".cmap") && !name.endsWith(".map")) {
            name += ".map";
        }

        String rawName = new File(name).getName().split("\\.")[0];

        File mapPath = new File(name);
        if (!mapPath.exists()) {
            mapPath = new File(escapistsPath, "Data" + File.separator + "Maps" + File.separator + name);
            if (!mapPath.exists()) {
                dialog("Map \"" + name.trim() + "\" not found.");
                return;
            }
        }

        // Decrypt it
        String content = new String(BlowfishCompatEncryption.decrypt(mapPath));

        // Decode it
        Map map = new Map(this, registry, mapPath.getPath(), content);

        BufferedImage image = MapRenderer.render(map);

        File parent = new File("renders");
        if (!parent.exists()) {
            parent.mkdir();
        }
        File outputfile = new File(parent, rawName.toLowerCase() + ".png");
        ImageIO.write(image, "png", outputfile);

        System.out.println("Successfully rendered \"" + outputfile.getPath() + "\".");
    }

    private void encrypt(String name, boolean install) throws IOException {
        if (!name.endsWith(".cmap") && !name.endsWith(".map")) {
            name += ".map";
        }

        File mapPath = new File(name);
        if (!mapPath.exists()) {
            mapPath = new File(escapistsPath, "Data" + File.separator + "Maps" + File.separator + name);
            if (!mapPath.exists()) {
                dialog("Map \"" + name.trim() + "\" not found.");
                return;
            }
        }

        // Decrypt it
        byte[] content = BlowfishCompatEncryption.encrypt(mapPath);
        File decryptedMap;
        if (install) {
            decryptedMap = new File(escapistsPath, "Data" + File.separator + "Maps"
                    + File.separator + name.split("\\.")[0] + ".map");
        } else {
            decryptedMap = new File(name.split("\\.")[0] + ".encrypted.map");
        }
        try (FileOutputStream out = new FileOutputStream(decryptedMap)) {
            IOUtils.write(content, out);
        }
    }

    public void edit(String name, RenderView oldView) throws IOException {
        if (!name.endsWith(".cmap") && !name.endsWith(".map")) {
            name += ".map";
        }

        String rawName = new File(name).getName().split("\\.")[0];

        File mapPath = new File(name);

        if (!mapPath.exists()) {
            mapPath = new File(escapistsPath, "Data" + File.separator + "Maps" + File.separator + name);
            if (!mapPath.exists()) {
                dialog("Map \"" + name.trim() + "\" not found.");
                return;
            }
        }

        String contents  = new String(BlowfishCompatEncryption.decrypt(mapPath));

        Map map = new Map(this, registry, mapPath.getPath(), contents);

        if (map.getTilesImage() == null && showGUI) {
            JOptionPane.showMessageDialog(null, "Failed to load resources.");
            System.exit(1);
        }
        RenderView view = new RenderView(this, map);
        if (oldView != null) {
            oldView.dispose();
        }
    }

    public static void main(String[] args) {
        try {
            // Parse
            EscapistsEditor editor = new EscapistsEditor();

            JCommander commander = new JCommander(editor);
            commander.setAcceptUnknownOptions(false);
            commander.setProgramName("java -jar escapistseditor.jar");
            try {
                commander.parse(args);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                commander.usage();
                System.exit(-1);
            }
            if (editor.showHelp) {
                commander.usage();
                System.exit(0);
            }

            editor.start();

            // Check what we need to do

            if (editor.decryptFile == null &&
                    editor.encryptFile == null &&
                    editor.renderMap == null &&
                    editor.encryptAndInstallFile == null &&
                    editor.editMap == null &&
                    !editor.renderAll) {
                // Select a map through a GUI first
                editor.showGUI = true;
                RenderView view = new RenderView(editor, null);
                view.setEnabled(false);
                MapSelectionGUI gui = new MapSelectionGUI(editor);
                gui.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                gui.setOldView(view);
            }

            if (editor.decryptFile != null) {
                editor.dump(editor.decryptFile);
            }
            if (editor.encryptFile != null) {
                editor.encrypt(editor.encryptFile, false);
            }
            if (editor.encryptAndInstallFile != null) {
                editor.encrypt(editor.encryptAndInstallFile, true);
            }
            if (editor.renderMap != null) {
                editor.render(editor.renderMap);
            }
            if (editor.renderAll) {
                for (String map : editor.getMaps()) {
                    editor.render(map);
                }
            }
            if (editor.editMap != null) {
                editor.edit(editor.editMap, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fatalError("Error: " + e.getMessage());
        }
    }
}

package net.jselby.escapists;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import net.jselby.escapists.editor.RenderView;
import net.jselby.escapists.utils.BlowfishCompatEncryption;
import net.jselby.escapists.utils.IOUtils;
import net.jselby.escapists.utils.SteamFinder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * The main entry point for the EscapistsEditor.
 *
 * @author j_selbys
 */
public class EscapistsEditor {
    public static final String VERSION = "1.4.9";
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

    @Parameter(names = "--escapists-path", description = "Forces a path for The Escapists install directory.")
    private String escapistsPathUser;


    // -- Internal vars
    /**
     * The root directory of Escapists
     */
    public File escapistsPath;

    /**
     * Create a new default registry for objects
     */
    public ObjectRegistry registry;
    public static boolean showGUI;
    private RenderView view;

    public static String updateMessage;

    private void start() {
        System.out.println("The Escapists Editor v" + VERSION);
        System.out.println("By jselby");

        System.out.println("=========================");
        System.out.println("Operating system: " + System.getProperty("os.name"));
        System.out.println("Java version: " + System.getProperty("java.version"));

        Runtime runtime = Runtime.getRuntime();
        NumberFormat format = NumberFormat.getInstance();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        System.out.println("Free memory: " + format.format(freeMemory / 1024 / 1024) + " MB");
        System.out.println("Allocated memory: " + format.format(allocatedMemory / 1024 / 1024) + " MB");
        System.out.println("Used memory: " + format.format(((allocatedMemory - freeMemory)) / 1024 / 1024) + " MB");
        System.out.println("=========================");

        registry = new ObjectRegistry("net.jselby.escapists.objects");

        // Discover Escapists directory
        if (escapistsPathUser == null) {
            File steamPath = SteamFinder.getSteamPath(this);

            if (steamPath == null) {
                fatalError("Failed to discover Steam installation with Escapists.");
            }

            // Check that Escapists is installed
            escapistsPath = steamPath;
        } else {
            escapistsPath = new File(escapistsPathUser);
        }
        if (!escapistsPath.exists()) {
            fatalError("Escapists is not installed @ " + escapistsPath.getPath());
        }

        // Parse arguments
        System.out.println("Discovered Escapists @ " + escapistsPath.getPath());

        // Check for update
        Thread updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String newVersion = IOUtils.toString(new URL("http://escapists.jselby.net/version.txt")).trim();
                    String message = "";
                    if (newVersion.contains("\n")) {
                        message = newVersion.split("\n")[1];
                        newVersion = newVersion.split("\n")[0].trim();
                    }
                    if (!newVersion.equalsIgnoreCase(VERSION) && newVersion.length() != 0) {
                        updateMessage = newVersion + "\n" + message;

                        dialog("New version found (" + newVersion + "). " +
                                "Download it at http://escapists.jselby.net\n" + message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        updateThread.start();
    }

    private static void fatalError(String s) {
        System.err.println(s);
        if (showGUI) {
            JOptionPane.showMessageDialog(null, s);
        }
        System.exit(1);
    }

    public void dialog(String s) {
        System.out.println(" - Dialog: ");
        for (String split : s.split("\n")) {
            System.out.println(" > " + split);
        }

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
        File mapPath = new File(name);
        String fileExtension = mapPath.getName().contains(".") ? ("." + mapPath.getName().split("\\.")[1]) : "";

        if (!mapPath.exists()) {
            mapPath = new File(escapistsPath, "Data" + File.separator + "Maps" + File.separator + name);
            if (!mapPath.exists()) {
                dialog("Map \"" + name.trim() + "\" not found.");
                return;
            }
        }

        // Decrypt it
        byte[] content = BlowfishCompatEncryption.decrypt(mapPath);
        File decryptedMap = new File(name.split("\\.")[0] + ".decrypted" + fileExtension);
        System.out.println("Decrypting \"" + name + " to \"" + decryptedMap.getPath() + "\"...");
        try (FileOutputStream out = new FileOutputStream(decryptedMap)) {
            out.write(content);
            out.flush();
        }
    }

    public void render(String name) throws IOException {
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

        BufferedImage image = new MapRenderer().render(map, "World");

        File parent = new File("renders");
        if (!parent.exists()) {
            if (!parent.mkdir()) {
                fatalError("Failed to create output directory: \"" + parent.getPath() + "\".");
            }
        }
        File outputfile = new File(parent, rawName.toLowerCase() + ".png");
        ImageIO.write(image, "png", outputfile);

        System.out.println("Successfully rendered \"" + outputfile.getPath() + "\".");
    }

    private void encrypt(String name, boolean install) throws IOException {
        File mapPath = new File(name);
        String fileExtension = mapPath.getName().contains(".") ? ("." + mapPath.getName().split("\\.")[1]) : "";
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
            decryptedMap = new File(name.split("\\.")[0] + ".encrypted" + fileExtension);
        }
        System.out.println("Decrypting \"" + name + " to \"" + decryptedMap.getPath() + "\"...");
        try (FileOutputStream out = new FileOutputStream(decryptedMap)) {
            out.write(content);
            out.flush();
        }
    }

    public void edit(byte[] decryptedBytes) throws IOException {
        String contents = new String(decryptedBytes);

        Map map = new Map(this, registry, "", contents);

        if (map.getTilesImage() == null && showGUI) {
            JOptionPane.showMessageDialog(null, "Failed to load resources.");
            System.exit(1);
        }

        if (view != null) {
            view.setEnabled(true);
            view.setMap(map);
        } else {
            view = new RenderView(this, map);
        }
    }

    public void edit(String name) throws IOException {
        File mapPath = new File(name);

        if (!mapPath.exists()) {
            mapPath = new File(escapistsPath, "Data" + File.separator + "Maps" + File.separator + name);
            if (!mapPath.exists()) {
                dialog("Map \"" + name.trim() + "\" not found.");
                return;
            }
        }

        String contents = new String(BlowfishCompatEncryption.decrypt(mapPath));

        Map map = new Map(this, registry, mapPath.getPath(), contents);

        if (map.getTilesImage() == null && showGUI) {
            JOptionPane.showMessageDialog(null, "Failed to load resources.");
            System.exit(1);
        }
        if (view != null) {
            view.setEnabled(true);
            view.setMap(map);
        } else {
            view = new RenderView(this, map);
        }
    }

    public static void main(String[] args) {
        try {
            // Redirect SysOut
            OutputStream fileOut = new FileOutputStream(new File("escapistseditor.log"));
            System.setOut(new LoggingDebugPrintStream(fileOut, System.out));
            System.setErr(new LoggingDebugPrintStream(fileOut, System.err));

            if (args.length == 0) {
                showGUI = true;
            }

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
                showGUI = true;
                editor.view = new RenderView(editor, null);
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
                editor.edit(editor.editMap);
            }
        } catch (Exception e) {
           fatalError(e);
        }
    }

    public static void fatalError(Exception e) {
        String string = "Error: ";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream stream = new PrintStream(out);
        e.printStackTrace(stream);
        string += out.toString();
        fatalError(string);
    }
}

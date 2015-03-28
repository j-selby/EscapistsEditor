package net.jselby.escapists;

import net.jselby.escapists.editor.elements.RenderView;
import net.jselby.escapists.mapping.Map;
import net.jselby.escapists.mapping.MapRenderer;
import net.jselby.escapists.objects.ObjectRegistry;
import net.jselby.escapists.utils.BlowfishCompatEncryption;
import net.jselby.escapists.utils.IOUtils;
import net.jselby.escapists.utils.LoggingDebugPrintStream;
import net.jselby.escapists.utils.SteamFinder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.util.ArrayList;

/**
 * The main entry point for the EscapistsEditor.
 *
 * @author j_selbys
 */
public class EscapistsEditor {
    public static final String VERSION = "1.5.0";
    public static final boolean DEBUG = false;

    // -- Arguments
    private String decryptFile;

    private String encryptFile;

    private String encryptAndInstallFile;

    private String renderMap;

    private String editMap;

    private boolean showHelp;

    private boolean renderAll;

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

        // I don't support piracy, in terms of obtaining support for such.
        // This hashes the steam_api.dll, checking for bad stuff there.
        File file = new File(escapistsPath, "steam_api.dll");
        if (file.exists()) {
            try {
                System.out.println("Hash: " + IOUtils.hash(file));
            } catch (Exception ignored) {}
        } else {
            System.out.println("Warning: No Steam API in Escapists dir!");
        }

        System.out.println("=========================");

        // Parse arguments
        System.out.println("Discovered Escapists @ " + escapistsPath.getPath());

        // Check for update
        Thread updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (!showGUI) {
                    return;
                }
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
            // TODO: Replace JCommander with internal implementation to save disk space
            // Redirect SysOut
            OutputStream fileOut = new FileOutputStream(new File("escapistseditor.log"));
            System.setOut(new LoggingDebugPrintStream(fileOut, System.out));
            System.setErr(new LoggingDebugPrintStream(fileOut, System.err));

            // Parse
            EscapistsEditor editor = new EscapistsEditor();

            if (args.length == 0) {
                showGUI = true;
            } else if (args.length > 0) {
                // Check for a command
                int pos = 0;
                while(pos < args.length) {
                    if (args[pos].startsWith("-")) {
                        // Sure is. Get its name
                        String name = args[pos].substring(1).toLowerCase();
                        while (name.startsWith("-")) {
                            name = name.substring(1);
                        }

                        if (name.startsWith("help") && args.length == 1) {
                            editor.showHelp = true;
                        }
                        if (name.startsWith("render-all") && args.length == 1) {
                            editor.renderAll = true;
                        } else if (args.length > 1) {
                            String val = args[pos + 1];

                            if (args[pos + 1].startsWith("\"")) {
                                // Multi-space arg
                                val = val.substring(1);
                                for (int i = pos + 2; i < args.length; i++) {
                                    pos = i;
                                    String str = args[i];
                                    val += " " + str;
                                    if (str.endsWith("\"")) {
                                        val = val.substring(0, val.length() - 1);
                                        break;
                                    }
                                }
                            } else {
                                pos++;
                            }

                            // Parse
                            if (name.equalsIgnoreCase("decrypt")) {
                                editor.decryptFile = val;
                            } else if (name.equalsIgnoreCase("encrypt")) {
                                editor.encryptFile = val;
                            } else if (name.equalsIgnoreCase("encrypt-and-install")) {
                                editor.encryptAndInstallFile = val;
                            } else if (name.equalsIgnoreCase("render")) {
                                editor.renderMap = val;
                            } else if (name.equalsIgnoreCase("edit")) {
                                editor.editMap = val;
                            } else if (name.equalsIgnoreCase("escapists-path")) {
                                editor.escapistsPathUser = val;
                            }
                            System.out.println(name + " = \"" + val + "\"");
                        } else {
                            System.out.println("Invalid command arguments - arg.");
                            System.out.println(name + " = \"\"");
                            editor.showHelp = true;
                        }
                    }  else {
                        System.out.println("Invalid command arguments - check.");
                        System.out.println(args[pos]);
                        editor.showHelp = true;
                    }
                    pos++;
                }
            }

            if (editor.showHelp) {
                System.out.println("Usage:");
                System.out.println(" --decrypt\tDecrypts the passed file.");
                System.out.println(" --encrypt\tEncrypts the passed file.");
                System.out.println(" --encrypt-and-install\tEncrypts and installs the passed file.");
                System.out.println(" --render\tRenders the passed file.");
                System.out.println(" --edit\tEdits the passed unencrypted map.");
                System.out.println(" --render-all\tRenders all maps.");
                System.out.println(" --escapists-path\tForces a path for The Escapists install directory.");
                return;
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

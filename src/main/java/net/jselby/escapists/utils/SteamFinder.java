package net.jselby.escapists.utils;

import net.jselby.escapists.EscapistsEditor;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.File;

/**
 * The SteamFinder discovers possible Steam paths, if required.
 *
 * @author j_selby
 */
public class SteamFinder {
    /**
     * Possible Steam locations. Prefixed by a drive.
     */
    private static final String[] POSSIBLE_LOCS = new String[] {
            "Program Files (x86)" + File.separator + "Steam" + File.separator,
            "Program Files" + File.separator + "Steam" + File.separator,
            "Steam" + File.separator,
            "SteamLibrary" + File.separator,
            "Games" + File.separator,
            "Games" + File.separator + "Steam" + File.separator
    };

    /**
     * Discovers a steam installation.
     *
     * @return A steam installation, or null if one was not found.
     * @param editor The editor to use for generating prompts
     */
    public static File getSteamPath(EscapistsEditor editor) {
        // TODO: Support platforms other then Windows.
        // Discover drive letters
        File[] drives = File.listRoots();

        // Check locations
        for (File drive : drives) {
            for (String possibleLoc : POSSIBLE_LOCS) {
                File steamDir = new File(drive, possibleLoc);

                if (steamDir.exists()) {
                    System.out.println("Discovered possible Steam location @ " + steamDir.getPath());

                    // Make sure that a Steam.dll exists here.
                    File steamFile = new File(steamDir, "Steam.dll");
                    if (steamFile.exists()) {
                        // Check that the Escapists is here
                        if (new File(steamDir,
                                "SteamApps" + File.separator + "common" + File.separator + "The Escapists").exists()) {
                            return new File(steamDir,
                                    "SteamApps" + File.separator + "common" + File.separator + "The Escapists");
                        }
                        if (new File(steamDir,
                                "common" + File.separator + "The Escapists").exists()) {
                            return new File(steamDir,
                                    "common" + File.separator + "The Escapists");
                        }
                        if (new File(steamDir,
                                "The Escapists").exists()) {
                            return new File(steamDir,
                                    "The Escapists");
                        }
                    }
                }
            }
        }

        // Ask user to find directory, if we are in a GUI.
        editor.dialog("No Stream installation detected. Please locate The Escapists in the following prompt.");
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Directory";
            }
        });
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(null);
        if (JFileChooser.APPROVE_OPTION == result) {
            // Check for Escapists here
            File resultFile = chooser.getSelectedFile();
            File escapistsTest = new File(resultFile, "TheEscapists.exe");
            if (escapistsTest.exists()) {
                return resultFile;
            } else {
                EscapistsEditor.fatalError(new Exception("Invalid directory (No TheEscapists.exe)"));
            }
        }

        return null;
    }
}

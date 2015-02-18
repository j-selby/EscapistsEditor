package net.jselby.escapists.utils;

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
            "Steam" + File.separator
    };

    /**
     * Discovers a steam installation.
     *
     * @return A steam installation, or null if one was not found.
     */
    public static File getSteamPath() {
        // TODO: Support platforms other then Windows.
        // Discover drive letters
        File[] drives = File.listRoots();

        // Check locations
        for (File drive : drives) {
            for (String possibleLoc : POSSIBLE_LOCS) {
                File steamDir = new File(drive, possibleLoc);

                if (steamDir.exists()) {
                    // Make sure that a Steam.dll exists here.
                    File steamFile = new File(steamDir, "Steam.dll");
                    if (steamFile.exists()) {
                        return steamDir;
                    }
                }
            }
        }

        return null;
    }
}

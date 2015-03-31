package net.jselby.escapists.utils;

import java.io.File;

/**
 * Various operating system related tools.
 *
 * @author j_selby
 */
public class OSUtils {
    public static File getDataStore() {
        String os = System.getProperty("os.name").toLowerCase();
        String workingDirectory = System.getProperty("user.home");

        if (os.contains("win")) {
            //it is simply the location of the "AppData" folder
            workingDirectory = System.getenv("AppData");
        } else if (os.contains("mac")) {
            workingDirectory += "/Library/Application Support";
        }

        return new File(workingDirectory);
    }
}

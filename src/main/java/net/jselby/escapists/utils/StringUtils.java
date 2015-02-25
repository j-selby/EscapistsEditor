package net.jselby.escapists.utils;

/**
 * A very small set of utilities.
 *
 * @author j_selby
 */
public class StringUtils {
    public static boolean isNumber(String output) {
        try {
            Integer.parseInt(output);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String capitalize(String string) {
        if (string == null || string.trim().length() == 0) {
            return string;
        }

        String[] tokens = string.split("\\s");
        string = "";

        for (int i = 0; i < tokens.length; i++) {
            char capLetter = Character.toUpperCase(tokens[i].charAt(0));
            string += (i == 0 ? "" : " ") + capLetter + tokens[i].substring(1, tokens[i].length());
        }

        return string;
    }
}

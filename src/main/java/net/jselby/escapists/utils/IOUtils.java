package net.jselby.escapists.utils;

import java.io.*;
import java.net.URL;

/**
 * Basic IOUtils replacement.
 */
public class IOUtils {
    private final static int BUFFER_SIZE = 8192;

    public static byte[] toByteArray(URL url) throws IOException {
        InputStream in = url.openStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] buf = new byte[BUFFER_SIZE];
        int val;
        while((val = in.read(buf)) != -1) {
            out.write(buf, 0, val);
        }

        in.close();

        return out.toByteArray();
    }

    public static byte[] toByteArray(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] buf = new byte[BUFFER_SIZE];
        int val;
        while((val = in.read(buf)) != -1) {
            out.write(buf, 0, val);
        }

        in.close();

        return out.toByteArray();
    }

    public static String toString(URL url) throws IOException {
        return new String(toByteArray(url));
    }
}

package net.jselby.escapists.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Basic IOUtils replacement.
 */
public class IOUtils {
    public static byte[] toByteArray(URL url) throws IOException {
        InputStream in = url.openStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] buf = new byte[1024];
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

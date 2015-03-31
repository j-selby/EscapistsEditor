package net.jselby.escapists.utils;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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

    public static String hash(File file) throws IOException, NoSuchAlgorithmException {
        byte[] s = toByteArray(file);
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(s, 0, s.length);
        return new BigInteger(1, md5.digest()).toString(16);
    }

    public static String toString(URL url) throws IOException {
        return new String(toByteArray(url));
    }

    public static void write(File f, String s) throws IOException {
        write(f, s.getBytes());
    }

    public static void write(File f, byte[] s) throws IOException {
        try (FileOutputStream out = new FileOutputStream(f)) {
            out.write(s);
            out.flush();
        }
    }
}

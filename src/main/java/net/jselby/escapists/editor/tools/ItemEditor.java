package net.jselby.escapists.editor.tools;

import net.jselby.escapists.editor.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Allows for items to be edited.
 *
 * @author j_selby
 */
public class ItemEditor {
    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        MessageDigest instance = MessageDigest.getInstance("MD5");
        instance.update(IOUtils.toByteArray(new File("items_eng.dat")));
        String md5 = new BigInteger(1, instance.digest()).toString(16);
        System.out.println(md5);

    }
}

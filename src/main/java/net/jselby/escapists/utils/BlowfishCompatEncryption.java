package net.jselby.escapists.utils;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.IOException;

/**
 * A simple wrapper to decrypt Blowfish-compat files.
 *
 * @author j_selby
 */
public class BlowfishCompatEncryption {
    private static String host = "104.236.143.65";

    /**
     * Decrypts a byte[] payload using the Blowfish-compat format, using the specified key.
     *
     * @param contents The contents to decrypt
     * @return The decrypted bytes.
     * @throws IOException Encryption errors.
     */
    public static byte[] decrypt(File contents) throws IOException {
        // Unfortunately, Java sucks, and there is no way to decrypt Blowfish-compat natively.
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost("http://" + host + "/escapists.php");
        System.out.println("Decrypting \"" + contents.getPath() + "\"... Please wait...");

        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("fileToUpload", contents,
                        ContentType.APPLICATION_OCTET_STREAM, "hidden.map").build();
        post.setEntity(entity);

        HttpResponse response = client.execute(post);
        byte[] responseString = IOUtils.toByteArray(response.getEntity().getContent());
        client.close();

        return responseString;
    }

    public static byte[] encrypt(File contents) throws IOException {
        // Unfortunately, Java sucks, and there is no way to encrypt Blowfish-compat natively.
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost("http://" + host + "/escapists_encrypt.php");
        System.out.println("Encrypting \"" + contents.getPath() + "\"... Please wait...");

        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("fileToUpload", contents,
                        ContentType.APPLICATION_OCTET_STREAM, "hidden.map").build();
        post.setEntity(entity);

        HttpResponse response = client.execute(post);
        byte[] responseString = IOUtils.toByteArray(response.getEntity().getContent());
        client.close();

        return responseString;
    }
}

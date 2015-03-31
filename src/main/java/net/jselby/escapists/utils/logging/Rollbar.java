package net.jselby.escapists.utils.logging;

import net.jselby.escapists.EscapistsEditor;
import net.jselby.escapists.utils.IOUtils;
import net.jselby.escapists.utils.OSUtils;
import net.jselby.escapists.utils.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.util.prefs.BackingStoreException;

/**
 * Rollbar is a online logging system.
 *
 * @author j_selby
 */
public class Rollbar {
    private static final String ACCESS_TOKEN = "ab6de73cd144479da279f262d23540b2";
    private static final String ENDPOINT = "https://api.rollbar.com/api/1/item/";

    private static boolean enabled = false;

    public static void init() throws BackingStoreException {
        File f = new File(OSUtils.getDataStore(), "unofficial-editor.cfg");

        // Make sure user has accepted
        if (!f.exists()) {
            try {
                IOUtils.write(f, JOptionPane.showConfirmDialog(null,
                        "To help with development of the editor, \n" +
                                "would it be fine to enable anonymous error submission?",
                        "Escapists Map Editor", JOptionPane.YES_NO_OPTION) == 0 ? "yes" : "no");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            enabled = IOUtils.toString(f.toURI().toURL()).trim().toLowerCase().equalsIgnoreCase("yes");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Thread fatal(Exception e) {
        // Encode to JSON
        JSONObject object = new JSONObject();
        object.put("access_token", ACCESS_TOKEN);

        JSONObject trace = new JSONObject();
        JSONArray frames = new JSONArray();
        StackTraceElement[] elements = e.getStackTrace();
        for (int x = elements.length - 1; x >= 0; x--) {
            StackTraceElement element = elements[x];
            JSONObject frame = new JSONObject();
            frame.put("filename", element.getFileName());
            frame.put("lineno", element.getLineNumber());
            frame.put("method", element.getMethodName());
            frame.put("class_name", element.getClassName());
            frames.add(frame);
        }
        trace.put("frames", frames);
        JSONObject exception = new JSONObject();
        exception.put("class", e.getClass().getName());
        exception.put("message", e.getMessage());
        trace.put("exception", exception);

        JSONObject body = new JSONObject();
        body.put("trace", trace);

        JSONObject data = new JSONObject();
        data.put("environment", "production");
        data.put("body", body);
        data.put("code_version", EscapistsEditor.VERSION);
        data.put("language", "java");
        data.put("framework", "java");
        data.put("platform", "client");
        data.put("title", "client");

        JSONObject client = new JSONObject();
        client.put("java_version", System.getProperty("java.version"));
        client.put("os_version", System.getProperty("os.name"));
        data.put("client", client);

        object.put("data", data);

        String json = object.toJSONString();

        Thread thread = new Thread(() -> {
            try {
                submit(json);
            } catch (IOException ignored) {}
        });
        thread.start();
        return thread;
    }

    private static void submit(String json) throws IOException {
        if (!enabled) {
            return;
        }

        URL obj = new URL(ENDPOINT);
        HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();

        //add reuqest header
        connection.setRequestMethod("POST");

        // Send post request
        connection.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(json);
        wr.flush();
        wr.close();

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            System.out.println("Failed to upload errorlog: " + responseCode);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            //print result
            System.out.println(response.toString());
        }
    }
}

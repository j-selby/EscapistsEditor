package net.jselby.escapists.mapping.store;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A PropertiesFile stores the raw backing format used by the Escapists, a key-value store.
 *
 * @author j_selby
 */
public class PropertiesFile {
    private final Map<String, PropertiesSection> sections;

    public PropertiesFile(String contents) {
        sections = new LinkedHashMap<>();

        String[] lines = contents.split("\n");

        ReadState state = ReadState.UNDEFINED;
        PropertiesSection section = null;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmedLine = line.trim();
            int humanLineNum = i + 1;

            if (trimmedLine.length() == 0) {
                // Blank line, skip.
                continue;
            }

            if (trimmedLine.startsWith("[")) {
                // Section title

                // Check for closing bracket
                if (trimmedLine.endsWith("]")) {
                    // OK: Setup new section
                    String name = trimmedLine.substring(1, trimmedLine.length() - 1);

                    section = new PropertiesSection(name);
                    sections.put(name, section);

                    state = ReadState.KEYS;
                } else {
                    System.out.println("Parse error: Section definition not closed @ ln " + humanLineNum);
                }

                continue;
            }

            if (state == ReadState.UNDEFINED) {
                // Bad section
                System.out.println("Parse error: Section not defined @ ln " + humanLineNum);
                continue;
            }

            // OK! Lets see if we have a key-value pair here
            if (line.contains("=")) {
                // We do! Split and store!
                int index = trimmedLine.indexOf("=");

                String key = trimmedLine.substring(0, index);
                String value = trimmedLine.substring(index + 1);

                section.put(key, value);
            } else {
                // Completely unknown line
                System.out.println("Parse error: Unknown syntax @ ln " + humanLineNum);
            }
        }

        // Done!
    }

    /**
     * Returns a section from this file.
     *
     * @param name A name of a section
     * @return A PropertiesSection, or null
     */
    public PropertiesSection getSection(String name) {
        return sections.get(name);
    }

    /**
     * Returns the entire set of sections from this file.
     * @return A entire entryset
     */
    public Iterable<? extends Map.Entry<String, PropertiesSection>> entrySet() {
        return sections.entrySet();
    }

    /**
     * Checks for a existing section.
     *
     * @param name A name of a section
     * @return If it exists
     */
    public boolean containsSection(String name) {
        return sections.containsKey(name);
    }

    /**
     * Returns a string representation of this file.
     * @return A correctly formatted list.
     */
    @Override
    public String toString() {
        String build = "";
        for (PropertiesSection section : sections.values()) {
            build += section.toString();
        }
        return build;
    }

    private enum ReadState {
        UNDEFINED,
        KEYS
    }
}

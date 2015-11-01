package net.jselby.escapists.editor.mapping.store;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A PropertiesSection is a section of a file, containing related pieces of information.
 *
 * @author j_selby
 */
public class PropertiesSection {
    private final String name;

    private final Map<String, Object> contents;

    public PropertiesSection(String name) {
        this.name = name;

        contents = new LinkedHashMap<>();
    }

    /**
     * Stores a key-value pair within this section.
     *
     * @param key The key to store with
     * @param value The value to insert
     */
    public void put(String key, Object value) {
        contents.put(key, value);
    }

    /**
     * Fetches the value for a RELATIVE key for this section.
     *
     * @param key The RELATIVE key to use.
     */
    public Object get(String key) {
        return contents.get(key);
    }

    /**
     * Returns the name of this section
     * @return A name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns an entire set of values for this section.
     * @return A entryset
     */
    public Iterable<? extends Map.Entry<String, Object>> entrySet() {
        return contents.entrySet();
    }

    /**
     * Returns the amount of values within this section.
     * @return A count of key-value pairs in this section.
     */
    public int size() {
        return contents.size();
    }

    /**
     * Removes all entries from this section.
     */
    public void clear() {
        contents.clear();
    }

    /**
     * Returns a String representation of this section, matching style.
     * @return A string representation.
     */
    @Override
    public String toString() {
        String rep = "[" + getName() + "]\n";
        for (Map.Entry<String, Object> val : contents.entrySet()) {
            rep += val.getKey() + "=" + val.getValue() + "\n";
        }

        return rep;
    }
}

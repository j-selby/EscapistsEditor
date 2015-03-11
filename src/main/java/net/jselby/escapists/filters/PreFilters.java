package net.jselby.escapists.filters;

import net.jselby.escapists.Map;
import net.jselby.escapists.WorldObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Filters the map at load time.
 *
 * @author j_selby
 */
public class PreFilters {
    public static void run(Map map) {
        // We need to remove all duplicate objects
        List<String> currentObjects = new ArrayList<>();
        for (WorldObject obj :
                map.getObjects().toArray(new WorldObject[map.getObjects().size()])) {
            String str = obj.getID() + ":" + obj.getX() + ":" + obj.getY();
            if (currentObjects.contains(str)) {
                map.getObjects().remove(obj);
            } else {
                currentObjects.add(str);
            }
        }
    }
}

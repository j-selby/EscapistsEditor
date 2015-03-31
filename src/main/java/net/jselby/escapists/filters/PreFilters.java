package net.jselby.escapists.filters;

import net.jselby.escapists.mapping.Map;
import net.jselby.escapists.mapping.store.PropertiesSection;
import net.jselby.escapists.objects.WorldObject;

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

        if (map.getWidth() != Map.DEFAULT_WIDTH || map.getHeight() != Map.DEFAULT_HEIGHT) {
            // Assuming array sizing is broken.
            System.out.println(" > Map decode warning: Bad width/height of tiles. Repairing array...");

            String[] sections = new String[] {
                    "World", "Underground", "Vents", "Roof"
            };
            for (String section : sections) {
                int[][] newArray = new int[Map.DEFAULT_HEIGHT][Map.DEFAULT_WIDTH + 1];
                int[][] oldArray = map.getTiles(section);
                for (int y = 0; y < oldArray.length; y++) {
                    for (int x = 0; x < oldArray[y].length; x++) {
                        if (newArray.length > y && newArray[y].length > x) {
                            newArray[y][x] = oldArray[y][x];
                        }
                    }
                }
                map.setTiles(section, newArray);
            }
        }

        // Escapists Update: Custom map tags
        /*
        [Perim]
            Top=16
            Bottom=16
            Left=16
            Right=16
        [Info]
            Rdy=1
            Custom=2
         */
        // Search for "Perim" section
        if (map.getMap("Perim") == null) {
            // Outdated version.
            System.out.println(" > Old-style Escapists map - upgrading...");
            PropertiesSection section = new PropertiesSection("Perim");
            section.put("Top", 16);
            section.put("Bottom", 1696);
            section.put("Left", 16);
            section.put("Right", 1696);
            map.setMap("Perim", section);
            map.set("Info.Custom", 2);
            map.set("Info.Rdy", 1);
            map.set("Info.Warden", "Bob");
        }
    }
}

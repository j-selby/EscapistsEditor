package net.jselby.escapists.objects;

import net.jselby.escapists.objects.impl.Light;
import net.jselby.escapists.objects.impl.VentMarker;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The ObjectRegistry discovers and creates WorldObjects, according to their implementations.
 *
 * @author j_selby
 */
public class ObjectRegistry {

    private static final Class<? extends WorldObject>[] CUSTOM_CLASSES = new Class[] {
            Light.class,
            VentMarker.class
    };

    public final ConcurrentHashMap<Integer, Class<? extends WorldObject>> objects = new ConcurrentHashMap<>();

    /**
     * Creates a new ObjectRegistry
     * @param packageName The package to discover WorldObjects under
     */
    public ObjectRegistry(String packageName) {
        for (Class<? extends WorldObject> type : CUSTOM_CLASSES) {
            // Create a instance to work out what ID this has
            try {
                // X:Y
                Constructor<? extends WorldObject> constructor
                        = type.getConstructor(new Class[]{Integer.TYPE, Integer.TYPE, Integer.TYPE});
                WorldObject instance = constructor.newInstance(-1, -1, 0);
                objects.put(instance.getID(), type);
            } catch (InstantiationException |
                    InvocationTargetException |
                    NoSuchMethodException |
                    IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public WorldObject instanceWithUnknown(int idToAdd, int x, int y, int level) {
        Class<? extends WorldObject> gameObjectClass = objects.get(idToAdd);
        if (gameObjectClass == null) {
            return new WorldObject.Unknown(x, y, idToAdd, level);
        }

        try {
            // X:Y;iD
            Constructor<? extends WorldObject> constructor
                    = gameObjectClass.getConstructor(new Class[]{Integer.TYPE, Integer.TYPE, Integer.TYPE});
            return constructor.newInstance(x, y, level);
        } catch (InstantiationException |
                InvocationTargetException |
                NoSuchMethodException |
                IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }
}

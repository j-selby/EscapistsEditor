package net.jselby.escapists;

import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The ObjectRegistry discovers and creates WorldObjects, according to their implementations.
 *
 * @author j_selby
 */
public class ObjectRegistry {
    public java.util.Map<Integer, Class<? extends WorldObject>> objects = new ConcurrentHashMap<>();

    /**
     * Creates a new ObjectRegistry
     * @param packageName The package to discover WorldObjects under
     */
    public ObjectRegistry(String packageName) {
        Reflections reflections = new Reflections(packageName);
        for (Class<? extends WorldObject> type : reflections.getSubTypesOf(WorldObject.class)) {
            // Create a instance to work out what ID this has
            try {
                // X:Y
                Constructor<? extends WorldObject> constructor
                        = type.getConstructor(new Class[]{Integer.TYPE, Integer.TYPE});
                WorldObject instance = constructor.newInstance(-1, 1);
                objects.put(instance.getID(), type);
            } catch (InstantiationException |
                    InvocationTargetException |
                    NoSuchMethodException |
                    IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public WorldObject instanceWithUnknown(int idToAdd, int x, int y) {
        Class<? extends WorldObject> gameObjectClass = objects.get(idToAdd);
        if (gameObjectClass == null) {
            return new WorldObject.Unknown(x, y, idToAdd, 1);
        }

        try {
            // X:Y
            Constructor<? extends WorldObject> constructor
                    = gameObjectClass.getConstructor(new Class[]{Integer.TYPE, Integer.TYPE});
            return constructor.newInstance(x, y);
        } catch (InstantiationException |
                InvocationTargetException |
                NoSuchMethodException |
                IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }
}

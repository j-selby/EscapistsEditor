package net.jselby.escapists;

import org.reflections.Reflections;

import java.awt.*;
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
                WorldObject instance = constructor.newInstance(-1, -1);
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

    public static enum Objects {
        // General items
        BED(1),
        CHAIR(2),
        TOILET(3),
        OVEN(4),
        WASHING_MACHINE(5),
        PERSONAL_DESK(9),
        FREEZER(10),
        SERVING_TABLE(11),
        MEDICAL_BED(12),
        DINING_TABLE(13),
        CUTLERY_TABLE(15),
        SHOWER(21),
        CONTRABAND_DETECTOR(32),
        SECURITY_CAMERA(33),
        SINK(34),
        MINE(40),
        MEDICAL_SUPPLIES(48),
        LIGHT(49),
        LADDER_UP(52),
        FORCED_PRISONER_BED(54),
        FORCED_PRISONER_DESK(55),
        GUARD_BED(56),
        PRISON_SNIPER(57),
        SOLITARY_BED(59),
        PRISONER_STASH(61),
        JUNGLE_CHECKPOINT_GUARD(62),
        GENERATOR(65),
        CABINET(68),
        VISITATION_GUEST_SEAT(70),
        VISITATION_PLAYER_SEAT(71),
        ISLAND_DOCK(72),
        PAYPHONE(74),
        CABLE_TV(87),
        SUN_LOUNGER(88),

        // Job items
        JOB_DIRTY_LAUNDRY(35),
        JOB_CLEAN_LAUNDRY(36),
        JOB_RAW_WOOD(41),
        JOB_PREPARED_WOOD(42),
        JOB_RAW_METAL(43),
        JOB_PREPARED_METAL(44),
        JOB_METAL_TOOLS(45),
        JOB_SELECTION(46),
        JOB_CLEANING_SUPPLIES(47),
        JOB_MAILROOM_FILE(82),

        // Doors
        DOOR_GENERAL(24),
        DOOR_GENERAL_OUTSIDE(25),
        DOOR_ORANGE_KEY(26),
        DOOR_KITCHEN(27),
        DOOR_LAUNDRY(28),
        DOOR_JANITOR(29),
        DOOR_RED_KEY(30),
        DOOR_METALSHOP(31),
        DOOR_WOODSHOP(38),
        DOOR_PRISON_ENTRANCE(75),
        DOOR_MAILROOM(83),
        DOOR_GARDENING(84),
        DOOR_DELIVERIES(86),
        DOOR_JUNGLE_ENTRANCE(95),

        // Training
        TRAINING_BOOKSHELF(6),
        TRAINING_TREADMILL(7),
        TRAINING_WEIGHT(8),
        TRAINING_INTERNET(69),
        TRAINING_JOGGING(90),
        TRAINING_CHINUP(102),

        // Jeep
        AI_JEEP_1(63),
        AI_JEEP_2(64),
        AI_JEEP_3(66),
        AI_JEEP_4(67),

        // AI Waypoints
        AI_WP_GUARD_SHOWERS(14),
        AI_WP_PRISONER_ROLLCALL(16),
        AI_WP_GUARD_ROLLCALL(17),
        AI_WP_PRISONER_GENERAL(18),
        AI_WP_GUARD_GENERAL(19),
        AI_WP_GUARD_MEALS(22),
        AI_WP_GUARD_EXERCISE(39),
        AI_WP_DOCTOR_WORK(94),
        ;

        private final int id;

        /**
         * Basic constructor for all objects in a ENUM
         * @param id
         */
        Objects(int id) {
            this.id = id;
        }

        public int getID() {
            return id;
        }

        public WorldObject asWorldObject(int x, int y) {
            return new WorldObject.Unknown(x, y, id, 1);
        }
    }
}

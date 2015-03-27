package net.jselby.escapists.objects;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public enum Objects {
    // General items
    BED(1),
    CHAIR(2),
    TOILET(3),
    OVEN(4, 0, -1),
    WASHING_MACHINE(5, 0, -1),
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
    VENT(50),
    VENT_SLATS(51),
    LADDER_UP(52),
    LADDER_DOWN(53),
    FORCED_PRISONER_BED(54),
    FORCED_PRISONER_DESK(55),
    GUARD_BED(56),
    PRISON_SNIPER(57),
    SOLITARY_BED(59),
    ROOF_SPOTLIGHTS(60),
    PRISONER_STASH(61),
    JUNGLE_CHECKPOINT_GUARD(62, 0, -1),
    GENERATOR(65),
    CABINET(68, 0, -1),
    VISITATION_GUEST_SEAT(70),
    VISITATION_PLAYER_SEAT(71),
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
    JOB_METAL_TOOLS(45, 0, -1),
    JOB_SELECTION(46),
    JOB_CLEANING_SUPPLIES(47),
    JOB_DELIVERIES_TRUCK(78, -1.5, -1),
    JOB_FABRIC_CHEST(79),
    JOB_CLOTHING_STORAGE(80),
    JOB_BOOK_CHEST(81),
    JOB_MAILROOM_FILE(82),
    JOB_GARDENING_TOOLS(89),
    JOB_DELIVERIES_RED_BOX(92),
    JOB_DELIVERIES_BLUE_BOX(93),

    // Doors
    DOOR_GENERAL(24),
    DOOR_GENERAL_OUTSIDE(25),
    DOOR_ORANGE_KEY(26),
    DOOR_KITCHEN(27),
    DOOR_LAUNDRY(28),
    DOOR_JANITOR(29),
    DOOR_RED_KEY(30),
    DOOR_METALSHOP(31),
    DOOR_LIBRARIAN(37),
    DOOR_WOODSHOP(38),
    DOOR_PRISON_ENTRANCE(75),
    DOOR_MAILROOM(83),
    DOOR_GARDENING(84),
    DOOR_TAILORSHOP(85),
    DOOR_DELIVERIES(86),
    DOOR_JUNGLE_ENTRANCE(95),
    DOOR_VENT_ORANGE(103),

    // Ziplines
    ZIPLINE_START_UP(76),
    ZIPLINE_END(77),
    ZIPLINE_START_DOWN(99),
    ZIPLINE_START_RIGHT(100),
    ZIPLINE_START_LEFT(101),

    // Training
    TRAINING_BOOKSHELF(6, 0, -1),
    TRAINING_TREADMILL(7),
    TRAINING_WEIGHT(8),
    TRAINING_INTERNET(69),
    TRAINING_JOGGING(90),
    TRAINING_PRESSUPS(91),
    TRAINING_SKIPPING(96),
    TRAINING_PUNCHBAG(97, 0, -1),
    TRAINING_SPEEDBAG(98, 0, -1),
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
    AI_WP_PRISONER_MEALS(23),
    AI_WP_GUARD_EXERCISE(39),
    AI_NPC_SPAWN(72),
    AI_WP_EMPLOYMENT_OFFICER(73),
    AI_WP_DOCTOR_WORK(94),
    ;

    private final int id;
    private double drawXRelative;
    private double drawYRelative;

    private BufferedImage texture;
    private boolean textureLoaded;

    /**
     * Basic constructor for all objects in a ENUM
     * @param id The ID of the object
     */
    Objects(int id) {
        this.id = id;
    }

    /**
     * Basic constructor for all objects in a ENUM
     * @param id The ID of the object
     */
    Objects(int id, double drawXRelative, double drawYRelative) {
        this.id = id;
        this.drawXRelative = drawXRelative;
        this.drawYRelative = drawYRelative;
    }

    public int getID() {
        return id;
    }

    public double getDrawX() {
        return drawXRelative;
    }

    public double getDrawY() {
        return drawYRelative;
    }

    public BufferedImage getTexture() {
        if (textureLoaded) {
            return texture;
        }

        textureLoaded = true;

        // Load it, if possible
        try {
            texture = ImageIO.read(getClass()
                    .getResource("/objects/" + name().toLowerCase() + ".png"));
        } catch (IllegalArgumentException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        }

        return texture;
    }

    public WorldObject asWorldObject(int x, int y) {
        return new WorldObject.Unknown(x, y, id, 1);
    }
}
package com.delta.common.constant;

public final class BusinessStatusConstants {

    public static final String PENDING_STATUS_PENDING = "pending";
    public static final String PENDING_STATUS_PROCESSING = "processing";
    public static final String PENDING_STATUS_RESOLVED = "resolved";

    public static final String SCHEDULE_STATUS_AVAILABLE = "AVAILABLE";
    public static final String SCHEDULE_STATUS_BOOKED = "BOOKED";
    public static final String SCHEDULE_STATUS_UNAVAILABLE = "UNAVAILABLE";

    public static final String ASSIGN_STATUS_ACTIVE = "ACTIVE";
    public static final String ASSIGN_STATUS_INACTIVE = "INACTIVE";

    public static final String ASSIGN_TYPE_MANUAL = "MANUAL";
    public static final String ASSIGN_TYPE_SYSTEM = "SYSTEM";

    public static final String SERVICE_CATEGORY_ACCOMPANY = "ACCOMPANY";
    public static final String SERVICE_CATEGORY_PACKAGE = "PACKAGE";
    public static final String SERVICE_CATEGORY_TEACHING = "TEACHING";
    public static final String SERVICE_CATEGORY_SOCIAL = "SOCIAL";

    public static final String PRICE_UNIT_HOUR = "HOUR";
    public static final String PRICE_UNIT_NIGHT = "NIGHT";
    public static final String PRICE_UNIT_ORDER = "ORDER";

    public static final String ORDER_STATUS_COMPLETED = "COMPLETED";
    public static final String ORDER_STATUS_REFUNDED = "REFUNDED";

    public static final int ENABLED_INT = 1;
    public static final int DISABLED_INT = 0;

    public static final int NOT_DELETED = 0;

    public static final String ROLE_SYS_ADMIN = "SYS_ADMIN";
    public static final String ROLE_CS_LEADER = "CS_LEADER";
    public static final String ROLE_CS_STAFF = "CS_STAFF";

    public static final String NOTIFICATION_TYPE_PENDING_MESSAGE = "pending_message";

    public static final String GAME_TYPE_FPS = "FPS";
    public static final String USER_STATUS_ACTIVE = "ACTIVE";

    private BusinessStatusConstants() {
    }
}

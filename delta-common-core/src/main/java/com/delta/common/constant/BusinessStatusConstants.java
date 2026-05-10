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

    public static final String ORDER_STATUS_PENDING = "PENDING";
    public static final String ORDER_STATUS_CONFIRMED = "CONFIRMED";
    public static final String ORDER_STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String ORDER_STATUS_COMPLETED = "COMPLETED";
    public static final String ORDER_STATUS_PENDING_REVIEW = "PENDING_REVIEW";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";
    public static final String ORDER_STATUS_REFUNDED = "REFUNDED";
    public static final String ORDER_STATUS_ABNORMAL = "ABNORMAL";
    public static final String ORDER_STATUS_ARCHIVED = "ARCHIVED";

    public static final String PAYMENT_STATUS_UNPAID = "UNPAID";
    public static final String PAYMENT_STATUS_PARTIAL = "PARTIAL";
    public static final String PAYMENT_STATUS_PAID = "PAID";

    public static final int ENABLED_INT = 1;
    public static final int DISABLED_INT = 0;

    public static final int NOT_DELETED = 0;

    public static final String ROLE_SYS_ADMIN = "SYS_ADMIN";
    public static final String ROLE_CS_LEADER = "CS_LEADER";
    public static final String ROLE_CS_STAFF = "CS_STAFF";

    public static final String NOTIFICATION_TYPE_PENDING_MESSAGE = "pending_message";

    public static final String GAME_TYPE_FPS = "FPS";
    public static final String USER_STATUS_ACTIVE = "ACTIVE";

    public static final String EXCEL_ENABLED_TEXT = "启用";
    public static final String EXCEL_DISABLED_TEXT = "禁用";

    public static boolean parseExcelEnabled(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        return EXCEL_ENABLED_TEXT.equals(value) || "true".equalsIgnoreCase(value) || String.valueOf(ENABLED_INT).equals(value);
    }

    public static int parseExcelEnabledInt(String value) {
        return parseExcelEnabled(value) ? ENABLED_INT : DISABLED_INT;
    }

    /** 资源类型：俱乐部 */
    public static final String RESOURCE_TYPE_CLUB = "CLUB";

    /** 订单超时取消阈值（分钟）：PENDING状态超过此时间自动取消 */
    public static final int ORDER_TIMEOUT_CANCEL_MINUTES = 30;

    /** 订单超时任务每次批量处理的最大条数 */
    public static final int ORDER_TIMEOUT_BATCH_SIZE = 200;

    /** 私有构造方法，防止实例化 */
    private BusinessStatusConstants() {
    }
}

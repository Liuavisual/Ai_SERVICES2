package com.delta.common.constant;

public final class WorkOrderConstants {

    public static final String ORDER_NO_PREFIX = "WO";

    public static final String STATUS_NEW = "NEW";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_PENDING_CONFIRM = "PENDING_CONFIRM";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CLOSED = "CLOSED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    public static final String TYPE_CONSULT = "CONSULT";
    public static final String TYPE_BOOKING = "BOOKING";
    public static final String TYPE_COMPLAINT = "COMPLAINT";
    public static final String TYPE_REFUND = "REFUND";
    public static final String TYPE_SERVICE_TRACK = "SERVICE_TRACK";
    public static final String TYPE_OTHER = "OTHER";

    public static final String PRIORITY_NORMAL = "NORMAL";
    public static final String PRIORITY_URGENT = "URGENT";
    public static final String PRIORITY_CRITICAL = "CRITICAL";

    public static final String RECORD_TYPE_STATUS_CHANGE = "STATUS_CHANGE";
    public static final String RECORD_TYPE_HANDLE_RECORD = "HANDLE_RECORD";
    public static final String RECORD_TYPE_INTERNAL_NOTE = "INTERNAL_NOTE";
    public static final String RECORD_TYPE_SYSTEM_LOG = "SYSTEM_LOG";

    public static final String SERVICE_STATUS_PRE_SERVICE = "PRE_SERVICE";
    public static final String SERVICE_STATUS_IN_SERVICE = "IN_SERVICE";
    public static final String SERVICE_STATUS_POST_SERVICE = "POST_SERVICE";

    public static final String TRACK_STATUS_CONSULTING = "CONSULTING";
    public static final String TRACK_STATUS_BOOKED = "BOOKED";
    public static final String TRACK_STATUS_SERVICING = "SERVICING";
    public static final String TRACK_STATUS_SERVICE_DONE = "SERVICE_DONE";
    public static final String TRACK_STATUS_CONFIRMED = "CONFIRMED";

    public static final String ORDER_STATUS_PENDING = "PENDING";
    public static final String ORDER_STATUS_CONFIRMED = "CONFIRMED";
    public static final String ORDER_STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String ORDER_STATUS_COMPLETED = "COMPLETED";
    public static final String ORDER_STATUS_CANCELLED = "CANCELLED";
    public static final String ORDER_STATUS_REFUNDED = "REFUNDED";

    public static final String PAYMENT_STATUS_UNPAID = "UNPAID";
    public static final String PAYMENT_STATUS_PAID = "PAID";
    public static final String PAYMENT_STATUS_REFUNDED = "REFUNDED";

    public static final String SERVICE_RESULT_COMPLETED = "COMPLETED";
    public static final String SERVICE_RESULT_EARLY_TERMINATED = "EARLY_TERMINATED";
    public static final String SERVICE_RESULT_NO_SHOW = "NO_SHOW";

    public static final String FILE_TYPE_IMAGE = "IMAGE";
    public static final String FILE_TYPE_VIDEO = "VIDEO";
    public static final String FILE_TYPE_DOCUMENT = "DOCUMENT";
    public static final String FILE_TYPE_AUDIO = "AUDIO";

    public static final int PRIORITY_NORMAL_TIMEOUT_MINUTES = 30;
    public static final int PRIORITY_URGENT_TIMEOUT_MINUTES = 15;
    public static final int PRIORITY_CRITICAL_TIMEOUT_MINUTES = 5;

    public static final int PRIORITY_NORMAL_CONFIRM_DAYS = 7;
    public static final int PRIORITY_URGENT_CONFIRM_DAYS = 3;
    public static final int PRIORITY_CRITICAL_CONFIRM_DAYS = 1;

    public static final int HANDLE_RESULT_MIN_LENGTH = 50;

    public static final String ORDER_NO_SEQ_KEY_PREFIX = "delta:work_order:seq:";

    private WorkOrderConstants() {
    }
}

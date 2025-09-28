package de.jkamue.mqtt

enum class ConnectReasonCode(val value: Int) {
    SUCCESS(0),
    UNSPECIFIED_ERROR(128),
    MALFORMED_PACKET(129),
    PROTOCOL_ERROR(130),
    IMPLEMENTATION_SPECIFIC_ERROR(131),
    UNSUPPORTED_PROTOCOL_VERSION(132),
    CLIENT_IDENTIFIER_NOT_VALID(133),
    BAD_USER_NAME_OR_PASSWORD(134),
    NOT_AUTHORIZED(135),
    SERVER_UNAVAILABLE(136),
    SERVER_BUSY(137),
    BANNED(138),
    BAD_AUTHENTICATION_METHOD(140),
    TOPIC_NAME_INVALID(144),
    PACKET_TOO_LARGE(149),
    QUOTA_EXCEEDED(151),
    PAYLOAD_FORMAT_INVALID(153),
    RETAIN_NOT_SUPPORTED(154),
    QOS_NOT_SUPPORTED(155),
    USE_ANOTHER_SERVER(156),
    SERVER_MOVED(157),
    CONNECTION_RATE_EXCEEDED(159);

    companion object {
        private val map = entries.associateBy(ConnectReasonCode::value)
        fun fromValue(value: Int): ConnectReasonCode? = map[value]
    }
}

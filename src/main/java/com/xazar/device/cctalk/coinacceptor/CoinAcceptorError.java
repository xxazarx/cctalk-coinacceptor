package com.xazar.device.cctalk.coinacceptor;

public enum CoinAcceptorError {
    NO_ERROR(0, false),
    REJECT_COIN(1, true),
    INHIBITED_COIN(2, true),
    MULTIPLE_WINDOW(3, true),
    WAKEUP_TIMEOUT(4, true),
    VALIDATION_TIMEOUT(5, true),
    CREDIT_SENSOR_TIMEOUT(6, true),
    SORTER_OPTO_TIMEOUT(7, false),
    SECOND_CLOSE_COIN(8, true),
    ACCEPT_GATE_NOT_READY(9, true),
    CREDIT_SENSOR_NOT_READY(10, true),
    SORTER_NOT_READY(11, true),
    REJECT_COIN_NOT_CLEARED(12, true),
    VALIDATION_SENSOR_NOT_READY(13, true),
    CREDIT_SENSOR_BLOCKED(14, true),
    SORTER_OPTO_BLOCKED(15, true),
    CREDIT_SEQUENCE_ERROR(16, false),
    COIN_GOING_BACKWARDS(17, false),
    COIN_TOO_FAST_OVER_CREDIT_SENSOR(18, false),
    COIN_TOO_SLOW_OVER_CREDIT_SENSOR(19, false),
    COS_MECHANISM_ACTIVATED(20, false),
    DCE_OPTO_TIMEOUT(21, true),
    DCE_OPTO_NOT_SEEN(22, true),
    CREDIT_SENSOR_REACHED_TOO_EARLY(23, false),
    REJECT_COIN_REPEATED_SEQUENTIAL_TRIP(24, true),
    REJECT_SLUG(25, true),
    REJECT_SENSOR_BLOCKED(26, false),
    GAMES_OVERLOAD(27, false),
    MAX_COIN_METER_PULSES_EXCEEDED(28, false),
    ACCEPTED_GATE_OPEN_NOT_CLOSED(29, false),
    ACCEPTED_GATE_CLOSED_NOT_OPEN(30, true),
    MANIFOLD_OPTO_TIMEOUT(31, false),
    MANIFOLD_OPTO_BLOCKED(32, true),
    MANIFOLD_NOT_READY(33, true),
    SECURITY_STATUS_CHANGED(34, true),
    MOTOR_EXCEPTION(35, true),
    SWALLOWED_COIN(36, false),
    COIN_TOO_FAST_OVER_VALIDATION_SENSOR(37, true),
    COIN_TOO_SLOW_OVER_VALIDATION_SENSOR(38, true),
    COIN_INCORRECTLY_SORTED(39, false),
    EXTERNAL_LIGHT_ATTACK(40, false),

    INHIBITED_COIN_TYPE_N(128, true), // 128 - 159

    DATA_BLOCK_REQUEST(253, false),
    COIN_RETURN_MECHANISM_ACTIVATED(254, false),
    UNSPECIFIED_ALARM_CODE(255, false);

    private int code;
    private final boolean coinRejected;

    private CoinAcceptorError(int code, boolean coinRejected) {
        this.code = code;
        this.coinRejected = coinRejected;
    }

    public int getCode() {
        return code;
    }

    public boolean isCoinRejected() {
        return coinRejected;
    }

    public static CoinAcceptorError valueOf(int code) {

        if (code > 127 && code < 160) {
            CoinAcceptorError error = INHIBITED_COIN_TYPE_N;
            error.code = code;
            return error;
        }

        for (CoinAcceptorError error : CoinAcceptorError.values()) {
            if (error.code == code) {
                return error;
            }
        }
        return null;
    }
}

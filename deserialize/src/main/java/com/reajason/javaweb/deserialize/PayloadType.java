package com.reajason.javaweb.deserialize;

import com.reajason.javaweb.deserialize.payload.CommonsBeanutils19;
import lombok.Getter;

/**
 * @author ReaJason
 * @since 2024/12/13
 */
@Getter
public enum PayloadType {
    /**
     * CB 链
     */
    CommonsBeanutils19(new CommonsBeanutils19());

    private final Payload payload;

    PayloadType(Payload payload) {
        this.payload = payload;
    }

    public static PayloadType getPayloadType(String payloadType) {
        for (PayloadType value : values()) {
            if (value.name().equals(payloadType)) {
                return value;
            }
        }
        throw new IllegalArgumentException("unknown payload type: " + payloadType);
    }
}

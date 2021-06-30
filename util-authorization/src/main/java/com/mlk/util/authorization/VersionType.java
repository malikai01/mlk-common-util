package com.mlk.util.authorization;

import lombok.Getter;

/**
 * 常见版本
 */
public enum VersionType {
    NONE(0),
    /**
     * V1版本
     */
    V1(1),
    /**
     * V1.1版本
     */
    V1_1(101),
    /**
     * V1.1.1版本
     */
    V1_1_1(10_101),
    /**
     * V2版本
     */
    V2(2),
    /**
     * V2.1版本
     */
    V2_1(201),
    /**
     * V3版本
     */
    V3(3);
    @Getter
    private int code;

    private VersionType(int code) {
        this.code = code;
    }
}

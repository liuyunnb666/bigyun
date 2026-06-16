package com.bigyun.system.enums;

public enum LoginTypeEnum
{
    USERNAME("1"),
    PHONE("2"),
    EMAIL("3"),
    SCAN("4"),
    FACE("5");

    private final String code;

    LoginTypeEnum(String code)
    {
        this.code = code;
    }

    public String getCode()
    {
        return code;
    }
}

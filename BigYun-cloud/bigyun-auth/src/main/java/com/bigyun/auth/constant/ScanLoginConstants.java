package com.bigyun.auth.constant;

public interface ScanLoginConstants
{
    String SCAN_SESSION_KEY_PREFIX = "bigyun:scan:session:";
    String SCAN_CODE_KEY_PREFIX = "bigyun:scan:code:";
    String SCAN_LOCK_KEY_PREFIX = "bigyun:scan:lock:";
    String SCAN_GRANT_KEY_PREFIX = "bigyun:scan:grant:";

    long SESSION_TTL_SECONDS = 300L;
    long LOCK_TTL_SECONDS = 30L;
    long GRANT_TTL_SECONDS = 60L;

    String STATUS_CREATED = "CREATED";
    String STATUS_RESOLVED = "RESOLVED";
    String STATUS_CONFIRMED = "CONFIRMED";
}


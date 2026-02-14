package com.rbaciam.utils;

import java.time.LocalDateTime;

public class SoftDeleteUtil {

    /**
     * Returns true if the entity is NOT soft-deleted.
     */
    public static boolean isActive(LocalDateTime deletedAt) {
        return deletedAt == null;
    }

    /**
     * Returns true if the entity IS soft-deleted.
     */
    public static boolean isDeleted(LocalDateTime deletedAt) {
        return deletedAt != null;
    }

    /**
     * Returns the current timestamp to mark as deleted.
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }
}

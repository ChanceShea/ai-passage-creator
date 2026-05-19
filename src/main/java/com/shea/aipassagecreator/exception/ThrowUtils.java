package com.shea.aipassagecreator.exception;

/**
 * @author : Shea.
 * @since : 2026/5/18 09:38
 */
public class ThrowUtils {

    public static void throwIf(boolean t,BusinessException exception) {
        if (t) {
            throw exception;
        }
    }
}

package com.pro4j.util;

import com.pro4j.promise.PromiseCompleteException;
import com.pro4j.promise.PromiseException;
import com.pro4j.promise.PromiseTimeoutException;

public class PromiseUtil {
    //根据done或者join的返回值判断promise是否处于reject状态
    public static boolean isComplete(Object object) {
        return !(object instanceof PromiseException);
    }

    public static boolean isTimeout(Object object) {
        return object instanceof PromiseTimeoutException;
    }

    public static boolean isCompleteExceptionally(Object object) {
        return object instanceof PromiseCompleteException;
    }

    public static Object getReason(Object object) {
        if (object instanceof PromiseCompleteException) {
            return ((PromiseCompleteException)object).getReason();
        }
        return null;
    }
}

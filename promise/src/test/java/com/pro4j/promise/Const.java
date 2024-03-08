package com.pro4j.promise;

public @interface Const {
    int POLL_INTERVAL = 1;
    //    int DEFAULT_TIMEOUT = Integer.MAX_VALUE; // 方便调试，可以设置为 300
    int DEFAULT_TIMEOUT = 8*1000;
    int TIME_MIN = 5;
    int TIME_MEDIUM = 50;
    int TIME_LARGE = 100;
}

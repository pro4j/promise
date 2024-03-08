package com.pro4j.promise;

import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

public class PromiseExtJ {
    /**
     * Retry a task at a fixed interval until it is actively canceled or times out.
     * If timeout is set to null, which means the task will never time out.
     * @param task The task to be executed
     * @param periodMills retry interval, in milliseconds
     * @param timeout timeout, which can be null, which means the task will never timeout.
     **/
    public static <T> Promise<T> fixRateRepeat(Supplier<Promise<T>> task, long periodMills,  Timeout timeout) {
        Timer t = new Timer();
        TimerTask tt = new TimerTask() {
            @Override
            public void run() {
                task.get();
            }
        };
        Deferred<T> deferred = new Deferred<T>(timeout);
        if (timeout != null) {// timeout start count after offer has been called
            deferred.offer((resolve, reject) -> {});
        }
        deferred.done(o -> tt.cancel());
        t.scheduleAtFixedRate(tt, 0, periodMills);
        return deferred;
    }
}

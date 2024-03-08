package com.pro4j.promise;

import com.pro4j.promise.Promise.DoneCallback
import com.pro4j.promise.Promise.Reject
import com.pro4j.promise.Promise.ThenCallback
import java.util.Timer
import java.util.TimerTask
import java.util.function.Consumer
import java.util.function.Supplier

class PromiseExt {
    companion object {
        /**
         * retry at fixed intervals until success or timeout
         * @param task The task to be executed
         * @param attempts Maximum number of retries
         * @param period retry interval
         * @param timeout timeout time, failure is regarded as failure
         **/
        fun <T> fixRateRepeat(task: Supplier<Promise<T>>, period: Long, timeout: Timeout): Promise<T> {
            val deferred = Deferred<T>(timeout)
            val t = Timer()
            val promiseList = ArrayList<Promise<T>>()
            val tt: TimerTask = object : TimerTask() {
                override fun run() {
                    promiseList.add(
                        task.get().then(
                            ThenCallback { o: T ->
                                deferred.offer(Promise.Task { resolve: Promise.Resolve<T>, _: Reject<Any?>? ->
                                    resolve.apply(o)
                                })
                            })
                    )
                }
            }
            t.scheduleAtFixedRate(tt, 0, period)
            deferred.done(DoneCallback { o: Any? ->
                tt.cancel()
                promiseList.forEach(Consumer { promise -> promise.abort() })
            })
            return deferred
        }
    }
}
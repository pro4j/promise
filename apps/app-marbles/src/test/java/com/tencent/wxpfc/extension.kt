package com.tencent.wxpfc;

import com.pro4j.promise.Deferred
import com.pro4j.promise.Promise
import com.pro4j.promise.Promise.DoneCallback
import com.pro4j.promise.Promise.Reject
import com.pro4j.promise.Promise.ThenCallback
import com.pro4j.promise.Timeout
import java.util.ArrayList
import java.util.Timer
import java.util.TimerTask
import java.util.function.Consumer
import java.util.function.Supplier

class PromiseExt {
    companion object {
        /**
         * 扩展方法，固定的间隔重试，直到成功或者超时
         * @param task 需要执行的任务
         * @param attempts 最大重试次数
         * @param period 重试间隔
         * @param timeout 超时时间，未成功视为失败
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
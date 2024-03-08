package com.pro4j.promise;

import android.util.Log
import com.google.common.truth.Truth.assertThat
import com.pro4j.promise.Promise.*
import com.pro4j.util.TestUtil
import org.awaitility.Awaitility
import org.awaitility.Duration
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLog
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

private const val TAG = "KtBaseTest"

@RunWith(RobolectricTestRunner::class)
@Config(shadows = [ShadowLog::class], manifest = "src/main/AndroidManifest.xml", sdk = [23])
class KtBasicTest {

    private var aObject1: Any? = null
    private var aObject2: Any? = null
    private var aObject3: Any? = null
    private var aObject4: Any? = null
    private var aObject5: Any? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        Log.d(getTag(), "DslBaseTest setup")
        ShadowLog.stream = System.out
        Awaitility.setDefaultPollInterval(Const.POLL_INTERVAL.toLong(), TimeUnit.MILLISECONDS)
        Awaitility.setDefaultPollDelay(Duration.ZERO)
        Awaitility.setDefaultTimeout(
            Duration(
                Const.DEFAULT_TIMEOUT.toLong(),
                TimeUnit.MILLISECONDS
            )
        )
        aObject1 = null
        aObject2 = null
        aObject3 = null
        aObject4 = null
        aObject5 = null
    }

    @Test
    fun `basic usage test` () {
        Log.d(getTag(), "start resolveTimeoutTest")
        val result = "success"
        Promise({ resolve: Resolve<String>, _: Reject<Any?> ->
            Thread {
                try {
                    Thread.sleep(Const.TIME_MEDIUM.toLong())
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                Log.d(getTag(), "resolve with $result")
                resolve.apply(result)
            }.start()
        }, Timeout.ofMillis(1)).then { o:String ->
                aObject1 = o
                Log.d(getTag(), "received object $o")
        }.except { err: Throwable?, o: Any? ->
            aObject2 = err
            Log.d(getTag(), "received reject with object $o")
        }.done { o: Any? ->
            aObject3 = o
            Log.d(getTag(), "always object $o")
        }

        Awaitility.await().atMost(1, TimeUnit.SECONDS).until { aObject3 != null }
        assertThat(aObject1).isNull()
        assertThat(aObject2).isInstanceOf(PromiseTimeoutException::class.java)
        assertThat(aObject3).isEqualTo(aObject2)
    }

    @Test
    fun `dsl chain resolve test` () {
        Log.d(getTag(), "start dslChainResolveTest")
        val result1 = "stage1"
        val result2 = "stage2"
        Promise { resolve: Resolve<String>, _: Reject<Any?> ->
            Log.d(getTag(), "resolve with $result1")
            resolve.apply(result1)
        }.next { o: String ->
            aObject1 = o
            Log.d(getTag(), "next received object $o")
            Promise { resolve: Resolve<String>, _: Reject<Any?> ->
                Thread {
                    Log.d(getTag(), "next resolve apply " + (o + result2))
                    resolve.apply(o + result2)
                }.start()
            }
        }.then { o: String ->
            aObject2 = o
            Log.d(getTag(), "received object $o")
        }.except { _: Throwable?, o: Any? ->
            aObject3 = o
            Log.d(getTag(), "reject with object $o")
        }.done { o: Any? ->
            aObject4 = o
            Log.d(getTag(), "done object $o")
        }

        Awaitility.await().until { aObject4 != null }
        assertThat(aObject1).isEqualTo(result1)
        assertThat(aObject2).isEqualTo(result1 + result2)
        assertThat(aObject3).isNull()
        assertThat(aObject4).isEqualTo(result1 + result2)
    }

    @Test
    fun `abort basic delay` () {
        val result = "success"
        val promise = delay(Promise { resolve: Resolve<String>, _: Reject<Any?> ->
            Log.d(getTag(), "Resolve with success")
            resolve.apply(result)
        }.then {
            Log.d(getTag(), "Inner onResolve $it")
        }, Timeout.ofMillis(5000)).then {
            Log.d(getTag(), "Outer onResolve $it")
            aObject3 = it
        }.done {
            Log.d(getTag(), "Outer done $it")
            aObject4 = it
        }
        promise.abort("destroy")
        Awaitility.await().until { aObject4 != null }
        assertThat(aObject3).isNull()
        assertThat(aObject4).isInstanceOf(PromiseException::class.java)
        assertThat((aObject4 as Exception).cause).isInstanceOf(AbortException::class.java)
    }

    @Test
    fun `promise repeat test2` () {
        val rands = TestUtil.getRandomNums(10,100, 1000, 100, true)
        var attempt = 0;
        PromiseExt.fixRateRepeat(Supplier {
            val abortController = AbortController()
            object : Promise<String>(Task { resolve, reject ->
                val thread = Thread.currentThread()
                val seq = attempt
                abortController.signal.onAbort {
                    Log.d(getTag(), "<- $seq interrupt")
                    thread.interrupt()
                }
                attempt++
                Log.d(getTag(), "-> attempt $seq " + rands[seq])
                Thread.sleep(rands[seq].toLong())
                Log.d(getTag(), "<- $seq success")
                resolve.apply("success $seq")
            }, Timeout.ofMillis(1000)) {
                override fun onAbort(reason: String?) {
                    abortController.abort(reason)
                }
            }
        }, 50, Timeout.ofSeconds(10)).then {
            Log.d(getTag(), "then ok $it")
        }

//        Awaitility.await().atMost(Duration.FIVE_MINUTES).pollInterval(Duration.ONE_SECOND).until(
//            Callable() {
//                Log.d(getTag(), "gc")
//                System.gc()
//                false
//        })
    }

    private fun getTag(): String {
        return TAG + Thread.currentThread().id
    }

}
package com.pro4j.promise;

import static com.google.common.truth.Truth.assertThat;

import android.util.Log;
import java.util.concurrent.TimeUnit;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

@RunWith(RobolectricTestRunner.class)
@Config(shadows = {ShadowLog.class}, manifest = "src/main/AndroidManifest.xml", sdk = 23)
public class MemoryModelTest {

    public static final String TAG = "DslTrickyTest";

    private Object aObject;
    private int anInt;

    @Before
    public void setUp() throws Exception {
        Log.d(getTag(), "DslBaseTest setup");
        ShadowLog.stream = System.out;
        Awaitility.setDefaultPollInterval(Const.POLL_INTERVAL, TimeUnit.MILLISECONDS);
        Awaitility.setDefaultPollDelay(Duration.ZERO);
        Awaitility.setDefaultTimeout(new Duration(Const.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS));
        aObject = null;
        anInt = 0;
    }

    @Test
    public void accumulateTest() throws Exception {
        Log.d(getTag(), "start accumulateTest");

        Promise<Integer> promise = Promise.resolve(0);
        int i = 0;
        do {
            int c = i;
            promise = promise.next(n -> Promise.resolve(n + c));
        } while (++i <= 100);
        int val = promise.join();

        assertThat(val).isEqualTo(5050);
    }

    @Test
    public void accumulateTestGlobalVariable() throws Exception {
        Log.d(getTag(), "start accumulateTest");

        Promise<Integer> promise = Promise.resolve(anInt);
        int i = 0;
        do {
            promise = promise.next(n -> Promise.resolve(n + anInt++));
        } while (++i <= 100);
        int val = promise.join();

        assertThat(val).isEqualTo(5050);
    }

    @Test
    public void accumulateTest2() throws Exception {
        Log.d(getTag(), "start accumulateTest2");

        Promise<Integer> promise = Promise.resolve().next(unused -> Promise.resolve(0));
        int i = 0;
        do {
            int c = i;
            promise = promise.next(n -> Promise.resolve(n + c));
        } while (++i <= 100);
        int val = promise.join();

        assertThat(val).isEqualTo(5050);
    }

    @Test
    public void accumulateTest3() throws Exception {
        Log.d(getTag(), "start accumulateTest3");
        Promise.setDefaultExecutor(new Promise.ThreadPerTaskExecutor());
        Promise<Integer> promise = Promise.resolve().next(unused -> Promise.resolve(0));
        int i = 0;
        aObject = 0;
        do {
            int c = i;
            promise = promise.next(n -> {
                aObject = (int)aObject + 1;
                return Promise.resolve(n+c);
            });
        } while (++i <= 100);
        int val = promise.join();

        assertThat(val).isEqualTo(5050);
        assertThat(aObject).isEqualTo(101);//0 to 100
    }

    private String getTag() {
        return TAG + Thread.currentThread().getId();
    }
}
package com.pro4j.promise;

import android.util.Log;
import com.pro4j.promise.Promise.DoneCallback;
import com.pro4j.promise.Promise.Reject;
import com.pro4j.promise.Promise.Resolve;
import com.pro4j.promise.Promise.Task;
import com.pro4j.promise.Promise.ThenCallback;
import java.util.concurrent.Callable;
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
@Config(shadows = {ShadowLog.class}, manifest="src/main/AndroidManifest.xml", sdk = 23)
public class ExtTest {
    public static final String TAG = "ExtTest";

    @Before
    public void setUp() throws Exception {
        Log.d(getTag(),   "DslBaseTest setup");
        ShadowLog.stream = System.out;
        Awaitility.setDefaultPollInterval(Const.POLL_INTERVAL, TimeUnit.MILLISECONDS);
        Awaitility.setDefaultPollDelay(Duration.ZERO);
        Awaitility.setDefaultTimeout(new Duration(Const.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS));
    }

    @Test
    public void thenExceptAlwaysRejectTest() throws Exception {
        Promise<Void> promise = PromiseExtJ.fixRateRepeat(() -> new Promise<Void>(new Task<Void, Object>() {
            @Override
            public void exec(Resolve<Void> resolve, Reject<Object> reject) {
                Log.d(getTag(),   "doReport");
            }
        }), 1000, null);

        promise.done(new DoneCallback<Object>() {
            @Override
            public void onDone(Object o) {
                Log.d(getTag(),   "done " + o);
            }
        });

        Promise.delay(Timeout.ofSeconds(2), null).then(new ThenCallback<Void>() {
            @Override
            public void onResolve(Void unused) {
                promise.abort("timeup");
            }
        });
        Awaitility.await().atMost(Duration.FIVE_MINUTES).pollInterval(Duration.ONE_SECOND).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Log.d(getTag(), "gced");
                System.gc();
                return false;
            }
        });
    }

    private String getTag() {
        return TAG + Thread.currentThread().getId();
    }
}

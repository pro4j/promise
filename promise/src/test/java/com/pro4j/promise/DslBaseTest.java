package com.pro4j.promise;

import static com.google.common.truth.Truth.assertThat;
import static org.awaitility.Awaitility.await;

import android.util.Log;
import com.pro4j.promise.Promise.DoneCallback;
import com.pro4j.promise.Promise.NextCallback;
import com.pro4j.promise.Promise.Reject;
import com.pro4j.promise.Promise.RejectCallback;
import com.pro4j.promise.Promise.Resolve;
import com.pro4j.promise.Promise.Task;
import com.pro4j.promise.Promise.ThenCallback;
import com.pro4j.promise.Promise.ThreadPerTaskExecutor;
import com.pro4j.promise.ValidationFailedException.ValidationError;
import com.pro4j.util.PromiseUtil;
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
public class DslBaseTest {
    public static final String TAG = "DslBaseTest";

    private Object aObject1;
    private Object aObject2;
    private Object aObject3;
    private Object aObject4;
    private Object aObject5;

    @Before
    public void setUp() throws Exception {
        Log.d(getTag(),   "DslBaseTest setup");
        ShadowLog.stream = System.out;
        Awaitility.setDefaultPollInterval(Const.POLL_INTERVAL, TimeUnit.MILLISECONDS);
        Awaitility.setDefaultPollDelay(Duration.ZERO);
        Awaitility.setDefaultTimeout(new Duration(Const.DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS));
        aObject1 = null;
        aObject2 = null;
        aObject3 = null;
        aObject4 = null;
        aObject5 = null;
    }

    @Test
    public void thenExceptAlwaysRejectTest() throws Exception {
        Log.d(getTag(),  "start dslBaseRejectTest");
        String result = "fail";
        new Promise<String>((resolve, reject) -> new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(getTag(),  "reject with " + result);
                reject.apply(result);
            }
        }).start()).then(o -> {
            aObject1 = o;
            Log.d(getTag(),  "received object " + o);
        }).except(new RejectCallback<Object>() {
            @Override
            public void onReject(Throwable err, Object o) {
                aObject2 = o;
                Log.d(getTag(),  "reject with object " + o);
            }
        }).done(o -> {
            aObject3 = PromiseUtil.getReason(o);
            Log.d(getTag(),  "always object " + o);
        });

        await().until(() -> aObject3 != null);
        assertThat(aObject1).isNull();
        assertThat(aObject2).isEqualTo(result);
        assertThat(aObject3).isEqualTo(result);
    }

    @Test
    public void thenExceptAlwaysResolveTest() throws Exception {
        Log.d(getTag(),  "start dslBaseResolveTest");
        String result = "success";
        new Promise<String>((resolve, reject) -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(getTag(),  "resolve with " + result);
                    resolve.apply(result);
                }
            }).start();
        }).then(new ThenCallback<String>() {
            @Override
            public void onResolve(String o) {
                aObject1 = o;
                Log.d(getTag(),  "received object " + o);
            }
        }).except((err, o) -> {
            aObject2 = o;
            Log.d(getTag(),  "reject with object " + o);
        }).done(o -> {
            aObject3 = o;
            Log.d(getTag(),  "always object " + o);
        });

        await().until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return aObject3 != null;
            }
        });
        assertThat(aObject1).isEqualTo(result);
        assertThat(aObject2).isNull();
        assertThat(aObject3).isEqualTo(result);
    }

    @Test
    public void ExceptThenAlwaysResolveTest() throws Exception {
        Log.d(getTag(),  "start dslBaseResolveTest1");
        String result = "success";
        new Promise<String>((resolve, reject) -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(getTag(),  "resolve with " + result);
                    resolve.apply(result);
                }
            }).start();
        }).except((err, o) -> {
            aObject2 = o;
            Log.d(getTag(),  "reject with object " + o);
        }).then(new ThenCallback<String>() {
            @Override
            public void onResolve(String o) {
                aObject1 = o;
                Log.d(getTag(),  "received object " + o);
            }
        }).done(o -> {
            aObject3 = o;
            Log.d(getTag(),  "always object " + o);
        });

        await().until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return aObject3 != null;
            }
        });
        assertThat(aObject1).isEqualTo(result);
        assertThat(aObject2).isNull();
        assertThat(aObject3).isEqualTo(result);
    }

    @Test
    public void ExceptThenAlwaysRejectTest() throws Exception {
        Log.d(getTag(),  "start dslBaseResolveTest1");
        String result = "fail";
        new Promise<String>((resolve, reject) -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(getTag(),  "resolve with " + result);
                    reject.apply(result);
                }
            }).start();
        }).except((err, o) -> {
            aObject1 = o;
            Log.d(getTag(),  "reject with object " + o);
        }).then(new ThenCallback<String>() {
            @Override
            public void onResolve(String o) {
                aObject2 = o;
                Log.d(getTag(),  "received object " + o);
            }
        }).done(new DoneCallback<Object>() {
            @Override
            public void onDone(Object o) {
                aObject3 = PromiseUtil.getReason(o);
                Log.d(getTag(),  "always object " + o);
            }
        });

        await().until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return aObject3 != null;
            }
        });
        assertThat(aObject1).isEqualTo(result);
        assertThat(aObject2).isNull();
        assertThat(aObject3).isEqualTo(result);
    }

    @Test
    public void resolveTimeoutTest() throws Exception {
        Log.d(getTag(),  "start resolveTimeoutTest");
        String result = "success";
        new Promise<String>((resolve, reject) -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(Const.TIME_MEDIUM);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d(getTag(),  "resolve with " + result);
                    resolve.apply(result);
                }
            }).start();
        }, Timeout.ofMillis(1), new ThreadPerTaskExecutor()).then(new ThenCallback<String>() {
            @Override
            public void onResolve(String o) {
                aObject1 = o;
                Log.d(getTag(),  "received object " + o);
            }
        }).except((err, o) -> {
            aObject2 = err;
            Log.d(getTag(),  "received reject with object " + o);
        }).done(o -> {
            aObject3 = o;
            Log.d(getTag(),  "always object " + o);
        });
        
        await().atMost(1, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return aObject3 != null;
            }
        });
        assertThat(aObject1).isNull();
        assertThat(aObject2).isInstanceOf(PromiseTimeoutException.class);
        assertThat(aObject3).isEqualTo(aObject2);
    }

    @Test
    public void rejectTimeoutTest() throws Exception {
        Log.d(getTag(),  "start rejectTimeoutTest");
        String result = "fail";
        new Promise<String>((resolve, reject) -> {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(Const.TIME_MEDIUM);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.d(getTag(),  "reject with " + result);
                    reject.apply(result);
                }
            }).start();
        }, Timeout.ofMillis(1)).then(o -> {
            aObject1 = o;
            Log.d(getTag(),  "received object " + o);
        }).except((err, o) -> {
            aObject2 = err;
            Log.d(getTag(),  "received reject with object " + err);
        }).done(o -> {
            aObject3 = o;
            Log.d(getTag(),  "always object " + o);
        });

        await().atMost(1, TimeUnit.SECONDS).until(() -> aObject3 != null);
        assertThat(aObject1).isNull();
        assertThat(aObject2).isInstanceOf(PromiseTimeoutException.class);
        assertThat(aObject3).isEqualTo(aObject2);
    }

    @Test
    public void doneResolveTest() throws Exception {
        Log.d(getTag(),  "start doneResolveTest");
        String result = "success";
        new Promise<String>((resolve, reject) -> new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(getTag(),  "resolve with " + result);
                resolve.apply(result);
            }
        }).start()).done(o -> {
            aObject1 = o;
            Log.d(getTag(),  "always object " + o);
        });

        await().until(() -> aObject1 != null);
        assertThat(aObject1).isEqualTo(result);
    }

    @Test
    public void doneThenTest() throws Exception {
        Log.d(getTag(),  "start doneThenTest");
        String result = "success";
        new Promise<String>((resolve, reject) -> new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(getTag(),  "resolve with " + result);
                resolve.apply(result);
            }
        }).start()).done(o -> {
            aObject1 = o;
            Log.d(getTag(),  "always object " + o);
        }).then(new ThenCallback<String>() {
            @Override
            public void onResolve(String o) {
                aObject2 = o;
                Log.d(getTag(),  "then object " + o);
            }
        });

        await().until(() -> aObject2 != null);
        assertThat(aObject1).isEqualTo(result);
        assertThat(aObject2).isEqualTo(result);
    }

    @Test
    public void validateSuccessTest() throws Exception {
        Log.d(getTag(),  "start validateSuccessTest");
        String result = "success";
        new Promise<String>((resolve, reject) -> new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(getTag(),  "resolve with " + result);
                resolve.apply(result);
            }
        }).start()).validate(s -> s.equals(result)).done(o -> {
            aObject1 = o;
            Log.d(getTag(),  "always object " + o);
        }).then(new ThenCallback<String>() {
            @Override
            public void onResolve(String o) {
                aObject2 = o;
                Log.d(getTag(),  "then object " + o);
            }
        });

        await().until(() -> aObject2 != null);
        assertThat(aObject1).isEqualTo(result);
        assertThat(aObject2).isEqualTo(result);
    }

    @Test
    public void validateFailTest() throws Exception {
        Log.d(getTag(),  "start validateFailTest");
        String result = "success";
        new Promise<String>((resolve, reject) -> new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(getTag(),  "resolve with " + result);
                resolve.apply(result);
            }
        }).start()).validate(s -> s.equals("x")).done(o -> {
            aObject1 = o;
            Log.d(getTag(),  "always object " + o);
        }).except(new RejectCallback<Object>() {
            @Override
            public void onReject(Throwable error, Object o) {
                aObject2 = error;
                aObject3 = o;
            }
        });

        await().until(() -> aObject2 != null);
        assertThat(aObject1).isInstanceOf(PromiseException.class);
        assertThat(aObject2).isInstanceOf(ValidationFailedException.class);
        assertThat(((ValidationFailedException)aObject2).getErrorCode()).isEqualTo(ValidationError.UNKNOWN.code);
        assertThat(aObject3).isNull();
    }

    @Test
    public void validateFailTest2() throws Exception {
        Log.d(getTag(),  "start validateFailTest2");
        String result = "success";
        new Promise<String>((resolve, reject) -> new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(getTag(),  "resolve with " + result);
                resolve.apply(result);
            }
        }).start()).validate(s -> s.equals("xx")).done(o -> {
            aObject1 = o;
            Log.d(getTag(),  "always object " + o);
        }).except(new RejectCallback<Object>() {
            @Override
            public void onReject(Throwable error, Object o) {
                aObject2 = error;
                aObject3 = o;
            }
        });

        await().until(() -> aObject2 != null);
        assertThat(aObject1).isInstanceOf(PromiseException.class);
        assertThat(aObject2).isInstanceOf(ValidationFailedException.class);
        assertThat(((ValidationFailedException)aObject2).getErrorCode()).isEqualTo(ValidationError.UNKNOWN.code);
        assertThat(aObject3).isNull();
    }

    @Test
    public void validateFailTest3() throws Exception {
        Log.d(getTag(),  "start validateFailTest3");
        String result = "success";
        new Promise<String>((resolve, reject) -> new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(getTag(),  "resolve with " + result);
                resolve.apply(result);
            }
        }).start(), new Promise.ThreadPerTaskExecutor()).validate(new Validation<String>() {
            @Override
            public boolean validate(String s) {
                if (true) {
                    throw new RuntimeException("error validate");
                }
                return false;
            }
        }).done(o -> {
            aObject1 = o;
            Log.d(getTag(),  "always object " + o);
        }).except(new RejectCallback<Object>() {
            @Override
            public void onReject(Throwable error, Object o) {
                aObject2 = error;
                aObject3 = o;
            }
        });

        await().until(() -> aObject2 != null);
        assertThat(aObject1).isInstanceOf(PromiseException.class);
        assertThat(aObject2).isInstanceOf(ValidationFailedException.class);
        assertThat(((ValidationFailedException)aObject2).getErrorCode()).isEqualTo(ValidationError.EXCEPTION.code);
        assertThat(aObject3).isNull();
    }

    @Test
    public void doneDoneTest() throws Exception {
        Log.d(getTag(),  "start doneThenTest");
        String result = "success";
        new Promise<String>((resolve, reject) -> new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(getTag(),  "resolve with " + result);
                resolve.apply(result);
            }
        }).start()).done(o -> {
            aObject1 = o;
            Log.d(getTag(),  "always object " + o);
        }).done(new DoneCallback<Object>() {
            @Override
            public void onDone(Object o) {
                aObject2 = o;
            }
        });

        await().until(() -> aObject2 != null);
        assertThat(aObject1).isEqualTo(result);
        assertThat(aObject2).isEqualTo(result);
    }

    @Test
    public void doneChainTest() throws Exception {
        Log.d(getTag(),  "start doneThenTest");
        String result = "success";
        Promise<Integer> promise = new Promise<String>((resolve, reject) -> new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(getTag(),  "resolve with " + result);
                resolve.apply(result);
            }
        }).start()).done(new DoneCallback<Object>() {
            @Override
            public void onDone(Object o) {
                aObject1 = o;
            }
        }).next(new NextCallback<Integer, String>() {
            @Override
            public Promise<Integer> next(String s) {
                return new Promise<Integer>(new Task<Integer, Object>() {
                    @Override
                    public void exec(Resolve<Integer> resolve, Reject<Object> reject) {
                        aObject2 = s;
                        resolve.apply(s.length());
                    }
                });
            }
        }).done(o -> {
            aObject3 = o;
            Log.d(getTag(),  "always object " + o);
        }).done(new DoneCallback<Object>() {
            @Override
            public void onDone(Object o) {
                aObject4 = o;
            }
        });

        await().until(() -> aObject4 != null);
        assertThat(aObject1).isEqualTo(result);
        assertThat(aObject2).isEqualTo(result);
        assertThat(aObject3).isEqualTo(result.length());
        assertThat(aObject4).isEqualTo(result.length());
    }

    @Test
    public void staticMethodTest1() throws Exception {
        Log.d(getTag(),  "start staticMethodTest1");
        String result = "fail";
        Promise.reject(result).then(new ThenCallback<Void>() {
            @Override
            public void onResolve(Void o) {
                aObject1 = o;
                Log.d(getTag(),  "resolve object " + o);
            }
        }).except((err, o)-> {
            aObject2 = o;
            Log.d(getTag(),  "reject object " + o);
        }).done(new DoneCallback<Object>() {
            @Override
            public void onDone(Object o) {
                aObject3 = o;
                Log.d(getTag(),  "done object " + o);
            }
        });
        await().until(() -> aObject3 != null);
        assertThat(aObject1).isNull();
        assertThat(aObject2).isEqualTo(result);
        assertThat(aObject2).isEqualTo(aObject2);
    }

    @Test
    public void staticMethodTest2() throws Exception {
        Log.d(getTag(),  "start staticMethodTest2");
        String result = "success";
        Promise.resolve(result).then(new ThenCallback<String>() {
            @Override
            public void onResolve(String o) {
                aObject1 = o;
                Log.d(getTag(),  "resolve object " + o);
            }
        }).except((err, o) -> {
            aObject2 = o;
            Log.d(getTag(),  "reject object " + o);
        }).done(new DoneCallback<Object>() {
            @Override
            public void onDone(Object o) {
                aObject3 = o;
                Log.d(getTag(),  "done object " + o);
            }
        });
        await().until(() -> aObject3 != null);
        assertThat(aObject1).isEqualTo(result);
        assertThat(aObject2).isNull();
        assertThat(aObject3).isEqualTo(aObject1);
    }

    @Test
    public void thrownInResolveTest() throws Exception {
        Log.d(getTag(),  "start staticMethodTest2");
        String result = "success";
        new Promise<String>(new Task<String, Object>() {
            @Override
            public void exec(Resolve<String> resolve, Reject<Object> reject) {
                resolve.apply(result);
            }
        }).then(new ThenCallback<String>() {
            @Override
            public void onResolve(String s) {
                aObject1 = s;
                Log.d(getTag(),  "onResolve1 s:" + s);
                throw new AbortException("abort");
            }
        }).then(new ThenCallback<String>() {
            @Override
            public void onResolve(String s) {
                Log.d(getTag(),  "onResolve2 s:" + s);
                aObject5 = s;
            }
        }).except(new RejectCallback<Object>() {
            @Override
            public void onReject(Throwable error, Object o) {
                aObject2 = error;
                aObject3 = o;
                Log.d(getTag(),  "onReject error:" + error + ",o=" + o);
            }
        }).done(new DoneCallback<Object>() {
            @Override
            public void onDone(Object o) {
                aObject4 = o;
                Log.d(getTag(),  "onDone o:" + o);
            }
        });

        await().until(() -> aObject4 != null);
        assertThat(aObject1).isEqualTo(result);
        assertThat(aObject2).isInstanceOf(AbortException.class);
        assertThat(aObject3).isNull();
        assertThat(aObject4).isInstanceOf(PromiseException.class);
        assertThat(((Exception)aObject4).getCause()).isInstanceOf(AbortException.class);
        assertThat(aObject5).isNull();
    }

    private String getTag() {
        return TAG + Thread.currentThread().getId();
    }
}

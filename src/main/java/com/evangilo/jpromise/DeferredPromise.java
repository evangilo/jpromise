package com.evangilo.jpromise;

public class DeferredPromise {
    public final Promise promise;
    public final Function<?, ?> onFulfilled;
    public final Function<?, ?> onRejected;

    public DeferredPromise(Promise promise, Function onFulfilled, Function onRejected) {
        this.promise = promise;
        this.onFulfilled = onFulfilled;
        this.onRejected = onRejected;
    }
}

package com.evangilo.jpromise;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class Promise {
    private static final int PENDING = 0;
    private static final int FULFILLED = 1;
    private static final int REJECTED = 2;
    private static final int WAIT_PROMISE = 3;

    private int state = PENDING;
    private Object value = null;
    private List<DeferredPromise> deferreds = new ArrayList<>();
    private static final PromiseExecutor noop = (resolve, reject) -> {};

    public Promise(PromiseExecutor action) {
        Promise.doResolve(this, action);
    }

    public boolean isPending() {
        return this.state == PENDING;
    }

    public boolean isRejected() {
        return this.state == REJECTED;
    }

    public boolean isFulfilled() {
        return this.state == FULFILLED;
    }

    public Promise then(Function<?, ?> onFulfilled, Function<?, ?> onRejected) {
        Promise promise = new Promise(noop);
        Promise.handle(this, new DeferredPromise(promise, onFulfilled, onRejected));
        return promise;
    }

    public Promise then(Function<?, ?> onFulfilled) {
        return this.then(onFulfilled, null);
    }

    public Promise fail(Function<?, ?> onRejected) {
        return this.then(null, onRejected);
    }

    public static Promise resolve(final Object value) {
        if (value instanceof Promise) {
            return (Promise) value;
        }
        return new Promise((_resolve, __) -> _resolve.apply(value));
    }

    public static Promise reject(final Object value) {
        return new Promise((__, _reject) -> _reject.apply(value));
    }

    public static Promise race(List<Promise> promises) {
        return new Promise((_resolve, _reject) -> {
            for (Promise promise: promises) {
                promise.then(_resolve, _reject);
            }
        });
    }

    public static Promise all(List<Promise> promises) {
        return new Promise((resolve, reject) -> {
            final int size = promises.size();
            AtomicInteger remaining = new AtomicInteger(size);
            AtomicReference error = new AtomicReference();
            Object[] result = new Object[size];

            for (int i = 0; i < size; i++) {
                final int index = i;
                promises.get(i).then(value -> {
                    if (error.get() != null) {
                        return value;
                    }
                    result[index] = value;
                    if (remaining.decrementAndGet() == 0) {
                        resolve.apply(Arrays.asList(result));
                    }
                    return value;
                }).fail(err -> {
                    if (error.get() == null) {
                        error.set(err);
                        reject.apply(error.get());
                    }
                    return err;
                });
            }
        });
    }

    private static void finale(Promise self) {
        for (DeferredPromise deferred : self.deferreds) {
            Promise.handle(self, deferred);
        }
        self.deferreds.clear();
    }

    private static void reject(Promise self, Object newValue) {
        self.state = REJECTED;
        self.value = newValue;
        Promise.finale(self);
    }

    private static void resolve(Promise self, Object newValue) {
        try {
            if (newValue == self) throw new IllegalArgumentException("A promise cannot be resolved with itself.");
            self.state = (newValue instanceof Promise) ? WAIT_PROMISE : FULFILLED;
            self.value = newValue;
            Promise.finale(self);
        } catch (Exception e) {
            Promise.reject(self, e);
        }
    }

    private static void handle(Promise self, DeferredPromise deferred) {
        while (self.state == WAIT_PROMISE) {
            self = (Promise) self.value;
        }

        if (self.isPending()) {
            self.deferreds.add(deferred);
            return;
        }

        Function callback = self.isFulfilled() ? deferred.onFulfilled : deferred.onRejected;

        if (callback != null) {
            try {
                Promise.resolve(deferred.promise, callback.apply(self.value));
            } catch (Exception e) {
                Promise.reject(deferred.promise, e);
            }
        } else if (self.isFulfilled()) {
            Promise.resolve(deferred.promise, self.value);
        } else {
            Promise.reject(deferred.promise, self.value);
        }
    }

    private static void doResolve(Promise self, PromiseExecutor action) {
        final Function resolve =  value -> {
            Promise.resolve(self, value);
            return null;
        };

        final Function reject = reason -> {
            Promise.reject(self, reason);
            return null;
        };

        try {
            action.call(resolve, reject);
        } catch (Exception e) {
            Promise.reject(self, e);
        }
    }
}

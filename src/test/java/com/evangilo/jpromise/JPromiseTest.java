package com.evangilo.jpromise;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class JPromiseTest {

    private CountDownLatch lock = new CountDownLatch(1);

    private void asyncOperation(Function function, int delay, int result) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                function.apply(result);
            } catch (Exception e) {
                // pass
            }
        }).start();
    }

    private List<Integer> filterOddNumber(List<Integer> numbers) {
        return numbers.stream().filter(n -> n % 2 != 0).collect(Collectors.toList());
    }

    private List<String> mapToString(List<Integer> numbers) {
        return numbers.stream().map(n -> n.toString()).collect(Collectors.toList());
    }

    @Test public void test_then_method() throws Exception {
        AtomicInteger value = new AtomicInteger();

        new Promise((resolve, reject) -> resolve.apply(4))
            .then((Integer v) -> {
                value.set(v);
                return null;
            });
        Assert.assertEquals(4, value.get());
    }

    @Test public void test_fail_method() {
        final int expectedValue = 10;
        AtomicInteger value = new AtomicInteger();

        new Promise((resolve, reject) -> reject.apply(expectedValue))
            .fail((Integer error) -> {
                value.set(error);
                return null;
            });

        Assert.assertEquals(expectedValue, value.get());
    }

    @Test public void test_aync_operation() throws Exception {
        final int delay = 500;
        final int expectedValue = 10;
        AtomicInteger value = new AtomicInteger();

        new Promise((resolve, reject) -> asyncOperation(resolve, delay, expectedValue))
            .then((Integer v) -> {
                value.set(v);
                return null;
            });

        lock.await(delay + 100, TimeUnit.MILLISECONDS);
        Assert.assertEquals(expectedValue, value.get());
    }

    @Test public void test_promise_all_method() {
        Promise p1 = Promise.resolve(1);
        Promise p2 = Promise.resolve(2);

        List<Integer> result = new ArrayList<>();
        List<Integer> expectedValues = Arrays.asList(1, 2);

        Promise.all(Arrays.asList(p1, p2))
            .then((List<Integer> res) -> {
                result.addAll(res);
                return null;
            });

        Assert.assertArrayEquals(expectedValues.toArray(), result.toArray());
    }

    @Test public void test_promise_race_method() throws Exception {
        AtomicInteger value = new AtomicInteger();
        final int expectedValue = 2;
        Promise p1 = new Promise((resolve, reject) -> asyncOperation(resolve, 2000, 1));
        Promise p2 = new Promise((resolve, reject) -> asyncOperation(resolve, 1000, expectedValue));

        Promise.race(Arrays.asList(p1, p2))
            .then((Integer v) -> {
                value.set(v);
                return null;
            });

        lock.await(2100, TimeUnit.MILLISECONDS);

        Assert.assertEquals(expectedValue, value.get());
    }

    @Test public void test_pipe_then() {
        AtomicInteger value = new AtomicInteger();
        Promise.resolve(100)
            .then((Integer v) -> v * 2)
            .then((Integer v) -> {
                value.set(v);
                return null;
            });

        final int expectedValue = 200;

        Assert.assertEquals(expectedValue, value.get());
    }

    @Test public void test_pipe_transform_result() {
        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<String> expectedValue = mapToString(filterOddNumber(list));
        List<String> result = new ArrayList<>();
        AtomicReference<Object> value = new AtomicReference<>();

        Promise.resolve(list)
            .then((List<Integer> numbers) -> filterOddNumber(numbers))
            .then((List<Integer> numbers) -> mapToString(numbers))
            .then((List<String> numbers) -> {
                result.addAll(numbers);
                return null;
            })
            .then((Object v) -> {
                value.set(v);
                return null;
            });

        Assert.assertArrayEquals(expectedValue.toArray(), result.toArray());
        Assert.assertNull(value.get());
    }
}

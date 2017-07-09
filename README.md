## JPromise

A Promise library for Java, based on [Promise-Polyfill](https://github.com/taylorhakes/promise-polyfill)

## Installation

### Gradle

```
    repositories {
        maven {
            url "http://dl.bintray.com/evngilo/jpromise"
        }
    }
```

```
    compile 'com.evangilo.jpromise:jpromise:0.1.2'
```

## Usage

### Promise

```java
    new Promise((resolve, reject) -> {
        ...
        resolve.apply(Arrays.asList(1, 2, 3));
    }).then((List<Integer> numbers) -> {
        ...
        return sum(numbers);
    }).then((Integer result) -> {
        ...
        return result * 2;
    }).fail(error -> {
        ...
        return error;
    });
```

### Promise All

```java
    List<Promise> promises = Arrays.asList(p1, p2);
    Promise.all(promises).then(result -> {
        ...
        return result;
    });
```

### Promise Race

```java
    List<Promise> promises = Arrays.asList(p1, p2);
    Promise.race(promises)
        .then(value -> value * 2)
        .fail(error -> {
            println(error);
            return null;
        });
```

### Promise Resolve

```java
    Promise.resolve(100).then(value -> value * 2);
```

### Promise Resolve

```java
    Promise.reject(100).then(error -> {
        println(error);
        return null;
    });
```

## License

```
Copyright (c) 2017 evangilo

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```


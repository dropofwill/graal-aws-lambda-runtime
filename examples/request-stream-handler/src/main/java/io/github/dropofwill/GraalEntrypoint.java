package io.github.dropofwill.examples.stream;

import io.github.dropofwill.Runtime;

/**
 * Example using GraalVM
 */
public class GraalEntrypoint {
    public static void main(String[] args) {
        // Runtime.with(HelloWorldStreamHandler.streamPojoWorld);
        Runtime.with(HelloWorldStreamHandler.streamHelloWorld);
    }
}

package io.github.dropofwill.examples.json;

import io.github.dropofwill.Runtime;

/**
 * Example using GraalVM
 */
public class HelloLambda {

    public static void main(String[] args) {
        Runtime.with(HelloWorldHandler.helloWorldJson);
    }
}

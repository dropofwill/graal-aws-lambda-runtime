package io.github.dropofwill;

import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * Example using GraalVM
 */
public class HelloLambda {
    private static final RequestHandler<byte[], byte[]> helloWorld = (input, context) -> {
        System.out.println("Hello from inside handler");
        return "Hello world".getBytes();
    };

    private static final RequestStreamHandler streamHelloWorld =
        (inputStream, outputStream, context) -> {
        System.out.println("Hello from inside handler");
        String input = new BufferedReader(new InputStreamReader(inputStream)).lines()
                .collect(Collectors.joining("\n"));
        outputStream.write(("Hello world and " + input).getBytes());
    };

    public static void main(String[] args) {
        Runtime.with(streamHelloWorld);
    }
}

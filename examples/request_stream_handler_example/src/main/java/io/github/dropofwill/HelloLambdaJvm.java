package io.github.dropofwill;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.stream.Collectors;

/**
 * Example using AWS provided JVM
 */
public class HelloLambdaJvm implements RequestStreamHandler {
    @Override
    public void handleRequest(
        InputStream inputStream, OutputStream outputStream, Context context)
        throws IOException {
        String input = new BufferedReader(new InputStreamReader(inputStream)).lines()
            .collect(Collectors.joining("\n"));
        outputStream.write(("Hello world and " + input).getBytes());
    }
}

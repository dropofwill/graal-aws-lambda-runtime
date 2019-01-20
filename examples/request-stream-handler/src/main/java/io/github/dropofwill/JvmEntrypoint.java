package io.github.dropofwill.examples.stream;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Example using the same Handlers with AWS JVM runtime
 */
public class JvmEntrypoint implements RequestStreamHandler {
    @Override
    public void handleRequest(
        InputStream inputStream, OutputStream outputStream, Context context)
        throws IOException {

        // HelloWorldStreamHandler.streamPojoWorld
        //     .handleRequest(inputStream, outputStream, context);

        HelloWorldStreamHandler.streamHelloWorld
            .handleRequest(inputStream, outputStream, context);
    }
}

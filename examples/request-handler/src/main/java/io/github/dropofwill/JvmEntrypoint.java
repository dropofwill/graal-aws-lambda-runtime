package io.github.dropofwill.examples.json;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

/**
 * Example using the same Handlers with AWS JVM runtime
 */
public class HelloLambdaJvm implements RequestHandler<Pojo, Pojo> {

    @Override
    public void handleRequest(Pojo input, Context context) {
        HelloWorldHandler.helloWorldJson.handleRequest(input, context);
    }
}

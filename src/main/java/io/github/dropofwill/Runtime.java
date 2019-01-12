package io.github.dropofwill;

import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import io.github.dropofwill.ApacheRuntimeClient.LambdaEvent;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Runtime {
    private final Logger logger = LoggerFactory.getLogger(Runtime.class);
    private final ApacheRuntimeClient client;

    private Runtime() {
        this.client = new ApacheRuntimeClient();
    }

    public static void with(RequestHandler<byte[], byte[]>  handler) {
        new Runtime().start(handler);
    }

    public static void with(RequestStreamHandler handler) {
        new Runtime().start(handler);
    }

    private void start(RequestHandler<byte[], byte[]> handler) {

    }

    /**
     * client.next()
     *      .flatMap(handler)
     *      .recover()
     *      .andFinally(silentlyConsume)
     * @param handler
     */
    private void start(RequestStreamHandler handler) {
        while (true) {
            logger.info("Polling for next event");
            LambdaEvent nextEvent = client.next();
            logger.info("Received next event");
            ByteArrayOutputStream handlerOutput = new ByteArrayOutputStream();

            try (InputStream lambdaStream = nextEvent.getEvent()) {
                logger.info("Executing handler");
                handler.handleRequest(
                    lambdaStream, handlerOutput, nextEvent.getContext());
                logger.info("Handler responded successfully");
            } catch (Exception handlerProblem) {
                try {
                    logger.info("Handler through an exception");
                    client.invocationError(
                        nextEvent.getContext().getAwsRequestId(),
                        handlerProblem.getMessage().getBytes());
                } catch (Exception fatal) {
                    logger.error("Something went wrong with reporting the handler's failure");
                    client.initializationError("Something went totally wrong".getBytes());
                    System.exit(1);
                }
            }

            try {
                HttpResponse res = client.success(
                    nextEvent.getContext().getAwsRequestId(), handlerOutput.toByteArray());
                logger.info("Sent success message to runtime, status={}",
                    res.getStatusLine().getStatusCode());
            } catch (Exception responseProblem) {
                logger.error("Something went wrong with reporting the handler's success");
                client.initializationError("Something went totally wrong".getBytes());
                System.exit(1);
            }
        }
    }
}

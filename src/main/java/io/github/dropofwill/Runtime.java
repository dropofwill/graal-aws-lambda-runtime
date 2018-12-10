package io.github.dropofwill;

import io.github.dropofwill.ApacheRuntimeClient.LambdaEvent;
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

    private void start(RequestHandler<byte[], byte[]> handler) {
        while (true) {
            logger.info("Polling for next event");
            LambdaEvent nextEvent = client.next();
            logger.info("Received next event");
            byte[] output = new byte[0];

            try {
                logger.info("Executing handler");
                output = handler.handleRequest(nextEvent.getEvent(), nextEvent.getContext());
                logger.info("Handler responded successfully");
            } catch (Exception handlerProblem) {
                try {
                    logger.info("Handler through an exception");
                    client.invocationError(
                        nextEvent.getContext().getAwsRequestId(),
                        handlerProblem.getMessage().getBytes());
                } catch (Exception fatal) {
                    logger.info("Something went wrong with reporting the handler's failure");
                    System.exit(1);
                }
            }

            try {
                HttpResponse res = client.succes(
                    nextEvent.getContext().getAwsRequestId(), output);
                logger.info("Sent success message to runtime, status={}",
                    res.getStatusLine().getStatusCode());
            } catch (Exception responseProblem) {
                logger.info("Something went wrong with reporting the handler's success");
                client.initializationError("Something went totally wrong".getBytes());
                System.exit(1);
            }
        }
    }
}

package io.github.dropofwill;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import io.github.dropofwill.ApacheRuntimeClient.LambdaEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Runtime {
    private final Logger logger = LoggerFactory.getLogger(Runtime.class);
    private final ApacheRuntimeClient client;
    private static final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new AfterburnerModule());

    private Runtime() {
        this.client = new ApacheRuntimeClient();
    }

    public static <I, O> void with(
        RequestHandler<I, O> handler, Class<I> inputType, Class<O> outputType) {
        new Runtime().start(handler, inputType, outputType);
    }

    public static void with(RequestStreamHandler handler) {
        new Runtime().start(handler);
    }

    public <T> void write(OutputStream outputStream, T output) {
        try {
            mapper.writeValue(outputStream, output);
        } catch (IOException bubbleUp) {
            throw new SerializtionException(bubbleUp);
        }
    }

    public <T> T read(InputStream json, Class<T> contentClass) {
        JavaType type = mapper.getTypeFactory().constructType(contentClass);
        try {
            return mapper.readValue(json, type);
        } catch (IOException bubbleUp) {
            throw new SerializtionException(bubbleUp);
        }
    }

    private <I, O> void start(
        RequestHandler<I, O> handler, Class<I> inputType, Class<O> outputType) {

        while (true) {
            logger.debug("Polling for next event");
            LambdaEvent nextEvent = client.next();
            logger.debug("Received next event");
            ByteArrayOutputStream handlerOutput = new ByteArrayOutputStream();

            try (InputStream lambdaStream = nextEvent.getEvent()) {
                logger.debug("Executing handler");
                I input = read(lambdaStream, inputType);
                O output = handler.handleRequest(input, nextEvent.getContext());
                write(handlerOutput, output);
                logger.debug("Handler responded successfully");
            } catch (Exception handlerProblem) {
                try {
                    logger.error("Handler threw an exception", handlerProblem);
                    client.invocationError(
                        nextEvent.getContext().getAwsRequestId(),
                        handlerProblem.getMessage().getBytes());
                } catch (Exception fatal) {
                    logger.error("Reporting the handler's failure failed");
                    client.initializationError(
                        "Reporting the handler's failure failed".getBytes());
                    System.exit(1);
                }
            }

            try {
                HttpResponse res = client.success(
                    nextEvent.getContext().getAwsRequestId(), handlerOutput.toByteArray());
                logger.info("Sent success message to runtime, status={}",
                    res.getStatusLine().getStatusCode());
            } catch (Exception responseProblem) {
                logger.error("Reporting the handler's success failed", responseProblem);
                client.initializationError(
                    "Reporting the handler's success failed".getBytes());
                System.exit(1);
            }
        }
    }

    private void start(RequestStreamHandler handler) {
        while (true) {
            logger.debug("Polling for next event");
            LambdaEvent nextEvent = client.next();
            logger.debug("Received next event");
            ByteArrayOutputStream handlerOutput = new ByteArrayOutputStream();

            try (InputStream lambdaStream = nextEvent.getEvent()) {
                logger.debug("Executing handler");
                handler.handleRequest(
                    lambdaStream, handlerOutput, nextEvent.getContext());
                logger.debug("Handler responded successfully");
            } catch (Exception handlerProblem) {
                try {
                    logger.error("Handler threw an exception", handlerProblem);
                    client.invocationError(
                        nextEvent.getContext().getAwsRequestId(),
                        handlerProblem.getMessage().getBytes());
                } catch (Exception fatal) {
                    logger.error("Reporting the handler's failure failed");
                    client.initializationError(
                        "Reporting the handler's failure failed".getBytes());
                    System.exit(1);
                }
            }

            try {
                HttpResponse res = client.success(
                    nextEvent.getContext().getAwsRequestId(), handlerOutput.toByteArray());
                logger.info("Sent success message to runtime, status={}",
                    res.getStatusLine().getStatusCode());
            } catch (Exception responseProblem) {
                logger.error("Reporting the handler's success failed", responseProblem);
                client.initializationError(
                    "Reporting the handler's success failed".getBytes());
                System.exit(1);
            }
        }
    }

    public static class DeserializtionException extends RuntimeException {
        static final long serialVersionUID = 1L;

        public DeserializtionException(Throwable cause) {
            super(cause);
        }
    }

    public static class SerializtionException extends RuntimeException {
        static final long serialVersionUID = 1L;

        public SerializtionException(Throwable cause) {
            super(cause);
        }
    }
}

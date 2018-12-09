package io.github.dropofwill;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import org.apache.http.HttpResponse;

public class CurlRuntimeClient implements RuntimeClient {

    @Override
    public LambdaEvent next() {
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("curl -sS -L -X GET", String.format("%s/%s/runtime/invocation/next",
                Config.getEndpoint(), RuntimeClient.RUNTIME_API_VERSION));
        return null;
    }

    @Override
    public HttpResponse succes(String requestId, byte[] responseBody) {
        return null;
    }

    @Override
    public HttpResponse invocationError(String requestId, byte[] failureBody) {
        return null;
    }

    @Override
    public void initializationError(byte[] failureBody) {

    }

    private static class RedirectStreamToConsumer implements Runnable {
        private InputStream inputStream;
        private Consumer<String> consumer;

        public RedirectStreamToConsumer(InputStream inputStream, Consumer<String> consumer) {
            this.inputStream = inputStream;
            this.consumer = consumer;
        }

        @Override
        public void run() {
            new BufferedReader(new InputStreamReader(inputStream)).lines()
                .forEach(consumer);
        }
    }
}

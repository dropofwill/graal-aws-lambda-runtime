package io.github.dropofwill;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApacheRuntimeClient implements RuntimeClient {
    private final HttpClient client;
    private final Logger logger = LoggerFactory.getLogger(ApacheRuntimeClient.class);

    protected ApacheRuntimeClient() {
        this.client = HttpClients.custom()
            // Retry idempotent HTTP methods
            .setRetryHandler(new StandardHttpRequestRetryHandler())
            .setMaxConnTotal(1)
            .setMaxConnPerRoute(1)
            .build();
    }

    @Override
    public LambdaEvent next() {
        HttpUriRequest request = RequestBuilder.get()
            .setUri(String.format("%s/%s/runtime/invocation/next",
                Config.getEndpoint(), RuntimeClient.RUNTIME_API_VERSION))
            // Infinite socket timeout for long polling
//            .setConfig(RequestConfig.custom().setSocketTimeout(-1).build())
            .build();

        HttpResponse response;
        try {
            response = client.execute(request);
        } catch (IOException fatal) {
            throw new RuntimeException("Could not communicate with runtime", fatal);
        }
        return LambdaEvent.fromResponse(response);
    }

    @Override
    public HttpResponse succes(String requestId, byte[] responseBody) {
        BasicHttpEntity body = new BasicHttpEntity();
        body.setContent(new ByteArrayInputStream(responseBody));
        String uri = String.format("%s/%s/runtime/invocation/%s/response",
                Config.getEndpoint(), RuntimeClient.RUNTIME_API_VERSION, UUID.randomUUID());
        logger.info(uri);

        HttpUriRequest request = RequestBuilder.post()
            .setUri(uri)
            .setEntity(body)
            .build();

        try {
            return client.execute(request);
        } catch (IOException fatal) {
            throw new RuntimeException("Could not communicate with runtime", fatal);
        }
    }

    @Override
    public HttpResponse invocationError(String requestId, byte[] failureBody) {
        BasicHttpEntity body = new BasicHttpEntity();
        body.setContent(new ByteArrayInputStream(failureBody));
        String uri = String.format("%s/%s/runtime/invocation/%s/error",
                Config.getEndpoint(), RuntimeClient.RUNTIME_API_VERSION, requestId);
        logger.info(uri);

        HttpUriRequest request = RequestBuilder.post()
            .setUri(uri)
            .setEntity(body)
            .build();

        try {
            return client.execute(request);
        } catch (IOException fatal) {
            throw new RuntimeException("Could not communicate with runtime", fatal);
        }
    }

    @Override
    public void initializationError(byte[] failureBody) {
        BasicHttpEntity body = new BasicHttpEntity();
        body.setContent(new ByteArrayInputStream(failureBody));

        HttpUriRequest request = RequestBuilder.post()
            .setUri(String.format("%s/%s/runtime/init/error",
                Config.getEndpoint(), RuntimeClient.RUNTIME_API_VERSION))
            .setEntity(body)
            .build();

        try {
            client.execute(request);
        } catch (IOException fatal) {
            throw new RuntimeException("Could not communicate with runtime", fatal);
        }
    }

}
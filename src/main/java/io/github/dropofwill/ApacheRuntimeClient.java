package io.github.dropofwill;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.StandardHttpRequestRetryHandler;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApacheRuntimeClient implements RuntimeClient {
    private final HttpClient client;
    private final Logger logger = LoggerFactory.getLogger(ApacheRuntimeClient.class);

    protected ApacheRuntimeClient() {
        this.client = HttpClients.custom()
            // Retry idempotent HTTP methods
            .setRetryHandler(new StandardHttpRequestRetryHandler())
            .setConnectionManager(new BasicHttpClientConnectionManager())
            .setMaxConnTotal(1)
            .setMaxConnPerRoute(1)
            .build();
    }

    @Override
    public LambdaEvent next() {
        HttpUriRequest request = RequestBuilder.get()
            .setUri(String.format("%s/%s/runtime/invocation/next",
                Config.getEndpoint(), RuntimeClient.RUNTIME_API_VERSION))
            .setVersion(HttpVersion.HTTP_1_1)
            // Infinite socket timeout for long polling
            .setConfig(RequestConfig.custom().setSocketTimeout(-1).build())
            .build();

        HttpResponse response = null;
        try {
            response = client.execute(request);
            return LambdaEvent.fromResponse(response);
        } catch (IOException fatal) {
            throw new RuntimeException("Could not communicate with runtime", fatal);
        } finally {
            if (response != null) EntityUtils.consumeQuietly(response.getEntity());
        }
    }

    @Override
    public HttpResponse succes(String requestId, byte[] responseBody) {
        BasicHttpEntity body = new BasicHttpEntity();
        body.setContent(new ByteArrayInputStream(responseBody));
        String uri = String.format("%s/%s/runtime/invocation/%s/response",
                Config.getEndpoint(), RuntimeClient.RUNTIME_API_VERSION, requestId);
        logger.info(uri);

        HttpResponse response = null;
        HttpUriRequest request = RequestBuilder.post()
            .setUri(uri)
            .setEntity(body)
            .setVersion(HttpVersion.HTTP_1_1)
            .build();

        try {
            response = client.execute(request);
        } catch (IOException fatal) {
            throw new RuntimeException("Could not communicate with runtime", fatal);
        } finally {
            if (response != null) EntityUtils.consumeQuietly(response.getEntity());
        }
        return response;
    }

    @Override
    public void invocationError(String requestId, byte[] failureBody) {
        BasicHttpEntity body = new BasicHttpEntity();
        body.setContent(new ByteArrayInputStream(failureBody));
        String uri = String.format("%s/%s/runtime/invocation/%s/error",
                Config.getEndpoint(), RuntimeClient.RUNTIME_API_VERSION, requestId);
        logger.info(uri);

        HttpResponse response = null;
        HttpUriRequest request = RequestBuilder.post()
            .setUri(uri)
            .setEntity(body)
            .setVersion(HttpVersion.HTTP_1_1)
            .build();

        try {
            response = client.execute(request);
        } catch (IOException fatal) {
            throw new RuntimeException("Could not communicate with runtime", fatal);
        } finally {
            if (response != null) EntityUtils.consumeQuietly(response.getEntity());
        }
    }

    @Override
    public void initializationError(byte[] failureBody) {
        BasicHttpEntity body = new BasicHttpEntity();
        body.setContent(new ByteArrayInputStream(failureBody));

        HttpResponse response = null;
        HttpUriRequest request = RequestBuilder.post()
            .setUri(String.format("%s/%s/runtime/init/error",
                Config.getEndpoint(), RuntimeClient.RUNTIME_API_VERSION))
            .setEntity(body)
            .setVersion(HttpVersion.HTTP_1_1)
            .build();

        try {
            response = client.execute(request);
        } catch (IOException fatal) {
            throw new RuntimeException("Could not communicate with runtime", fatal);
        } finally {
            if (response != null) EntityUtils.consumeQuietly(response.getEntity());
        }
    }

}

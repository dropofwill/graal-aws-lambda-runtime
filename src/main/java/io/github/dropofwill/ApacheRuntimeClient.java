package io.github.dropofwill;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
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

public class ApacheRuntimeClient {
    public static final String RUNTIME_API_VERSION = "2018-06-01";
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

    public LambdaEvent next() {
        HttpUriRequest request = RequestBuilder.get()
            .setUri(String.format("%s/%s/runtime/invocation/next",
                Config.getEndpoint(), ApacheRuntimeClient.RUNTIME_API_VERSION))
            .setVersion(HttpVersion.HTTP_1_1)
            // Infinite socket timeout for long polling
            .setConfig(RequestConfig.custom().setSocketTimeout(-1).build())
            .build();

        HttpResponse response = null;
        try {
            response = client.execute(request);
            return new LambdaEvent(response);
        } catch (IOException fatal) {
            throw new RuntimeException("Could not communicate with runtime", fatal);
        }
    }

    public HttpResponse success(String requestId, byte[] responseBody) {
        BasicHttpEntity body = new BasicHttpEntity();
        body.setContent(new ByteArrayInputStream(responseBody));
        String uri = String.format("%s/%s/runtime/invocation/%s/response",
                Config.getEndpoint(), ApacheRuntimeClient.RUNTIME_API_VERSION, requestId);
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

    public void invocationError(String requestId, byte[] failureBody) {
        BasicHttpEntity body = new BasicHttpEntity();
        body.setContent(new ByteArrayInputStream(failureBody));
        String uri = String.format("%s/%s/runtime/invocation/%s/error",
                Config.getEndpoint(), ApacheRuntimeClient.RUNTIME_API_VERSION, requestId);
        logger.info(uri);

        HttpResponse response = null;
        HttpUriRequest request = RequestBuilder.post()
            .setUri(uri)
            .setEntity(body)
            .setVersion(HttpVersion.HTTP_1_1)
            .build();

        try {
            response = client.execute(request);
            logger.debug("Invocation error response=%s", response);
        } catch (IOException fatal) {
            throw new RuntimeException("Could not communicate with runtime", fatal);
        } finally {
            if (response != null) EntityUtils.consumeQuietly(response.getEntity());
        }
    }

    public void initializationError(byte[] failureBody) {
        BasicHttpEntity body = new BasicHttpEntity();
        body.setContent(new ByteArrayInputStream(failureBody));

        HttpResponse response = null;
        HttpUriRequest request = RequestBuilder.post()
            .setUri(String.format("%s/%s/runtime/init/error",
                Config.getEndpoint(), ApacheRuntimeClient.RUNTIME_API_VERSION))
            .setEntity(body)
            .setVersion(HttpVersion.HTTP_1_1)
            .build();

        try {
            response = client.execute(request);
            logger.debug("Initialization error response=%s", response);
        } catch (IOException fatal) {
            throw new RuntimeException("Could not communicate with runtime", fatal);
        } finally {
            if (response != null) EntityUtils.consumeQuietly(response.getEntity());
        }
    }

    public static class LambdaEvent implements AutoCloseable {
//        private final byte[] event;
        private final InputStream event;
        private final Context context;

        LambdaEvent(HttpResponse response) {
//            byte[] body;
//            try {
//                body = toByteArray(response.getEntity());
//            } catch (Exception fatal) {
//                throw new RuntimeException("Could not read body from runtime", fatal);
//            }
//            this.event = body;
            InputStream inputStream;
            try {
                inputStream = response.getEntity().getContent();
            } catch (IOException fatal) {
                throw new RuntimeException("Could not read body from runtime", fatal);
            }
            this.event = inputStream;
            this.context = LambdaEvent.getEventContext(response);
        }

        public void close() {
            if (event != null) {
                try {
                    event.close();
                } catch (IOException ignore) {}
            }
        }

        private static String getFirstHeader(HttpResponse response, String headerName) {
            // TODO ideally collect all null values before throwing, quick and dirty way here
            return Optional.ofNullable(response.getFirstHeader(headerName))
                .map(Header::getValue)
                .orElseThrow(() -> new IllegalArgumentException(
                    String.format("Null %s header from runtime", headerName)));
        }

        private static Context getEventContext(HttpResponse response) {
            return new ContextBuilder()
                .setAwsRequestId(getFirstHeader(response, "Lambda-Runtime-Aws-Request-Id"))
                .setInvokedFunctionArn(getFirstHeader(response, "Lambda-Runtime-Invoked-Function-Arn"))
                .setTraceId(getFirstHeader(response, "Lambda-Runtime-Trace-Id"))
                .setRequestDeadline(getFirstHeader(response, "Lambda-Runtime-Deadline-Ms"))
                // TODO handle cognito and friends
                .createContext();
        }

        private static byte[] toByteArray(HttpEntity entity) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                entity.writeTo(baos);
            } catch (Exception fatal) {
                throw new RuntimeException("Unable to extract body", fatal);
            }
            return baos.toByteArray();
        }

        public InputStream getEvent() {
            return event;
        }

//        public byte[] getEvent() {
//            return event;
//        }

        public Context getContext() {
            return context;
        }
    }

    // TODO panic if these variables aren't set
    public static class Config {
        public static String getEndpoint() {
            return "http://" + System.getenv("AWS_LAMBDA_RUNTIME_API");
        }

        public static String getMemory() {
            return System.getenv("AWS_LAMBDA_FUNCTION_MEMORY_SIZE");
        }

        public static String getLogGroup() {
            return System.getenv("AWS_LAMBDA_LOG_GROUP_NAME");
        }

        public static String getLogStream() {
            return System.getenv("AWS_LAMBDA_LOG_STREAM_NAME");
        }

        public static String getFunctionName() {
            return System.getenv("AWS_LAMBDA_FUNCTION_NAME");
        }

        public static String getFunctionVersion() {
            return System.getenv("AWS_LAMBDA_FUNCTION_VERSION");
        }
    }
}

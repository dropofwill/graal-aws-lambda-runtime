package io.github.dropofwill;

import java.io.ByteArrayOutputStream;
import java.util.Optional;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

public interface RuntimeClient {
    String RUNTIME_API_VERSION = "2018-06-01";

    LambdaEvent next();

    HttpResponse succes(String requestId, byte[] responseBody);

    HttpResponse invocationError(String requestId, byte[] failureBody);

    void initializationError(byte[] failureBody);

    class LambdaEvent {
        private final byte[] event;
        private final Context context;

        LambdaEvent(byte[] event, Context context) {
            this.event = event;
            this.context = context;
        }

        public static LambdaEvent fromResponse(HttpResponse response) {
            byte[] body;
            try {
                 body = toByteArray(response.getEntity());
            } catch (Exception fatal) {
                throw new RuntimeException("Could not read body from runtime", fatal);
            }
            return new LambdaEvent(body, getEventContext(response));
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

        public byte[] getEvent() {
            return event;
        }

        public Context getContext() {
            return context;
        }
    }

    class Config {
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

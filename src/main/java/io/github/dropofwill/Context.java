package io.github.dropofwill;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import java.util.Optional;

/**
 *
 */
class Context {
    private String awsRequestId;
    private String invokedFunctionArn;
    private String traceId;
    private String requestDeadline;
    private CognitoIdentity identity;
    private ClientContext clientContext;

    Context(
        String awsRequestId, String invokedFunctionArn, String traceId, String requestDeadline,
        CognitoIdentity identity, ClientContext clientContext) {
        this.awsRequestId = awsRequestId;
        this.invokedFunctionArn = invokedFunctionArn;
        this.traceId = traceId;
        this.requestDeadline = requestDeadline;
        this.identity = identity;
        this.clientContext = clientContext;
    }

    public String getAwsRequestId() {
        return awsRequestId;
    }

    public String getInvokedFunctionArn() {
        return invokedFunctionArn;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getRequestDeadline() {
        return requestDeadline;
    }

    public Optional<CognitoIdentity> getIdentity() {
        return Optional.ofNullable(identity);
    }

    public Optional<ClientContext> getClientContext() {
        return Optional.ofNullable(clientContext);
    }
}

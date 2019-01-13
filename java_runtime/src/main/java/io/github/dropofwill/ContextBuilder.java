package io.github.dropofwill;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;

public class ContextBuilder {
    private String awsRequestId;
    private String invokedFunctionArn;
    private String traceId;
    private String requestDeadline;
    private CognitoIdentity identity;
    private ClientContext clientContext;

    public ContextBuilder setAwsRequestId(String awsRequestId) {
        this.awsRequestId = awsRequestId;
        return this;
    }

    public ContextBuilder setInvokedFunctionArn(String invokedFunctionArn) {
        this.invokedFunctionArn = invokedFunctionArn;
        return this;
    }

    public ContextBuilder setTraceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    public ContextBuilder setRequestDeadline(String requestDeadline) {
        this.requestDeadline = requestDeadline;
        return this;
    }

    public ContextBuilder setIdentity(CognitoIdentity identity) {
        this.identity = identity;
        return this;
    }

    public ContextBuilder setClientContext(ClientContext clientContext) {
        this.clientContext = clientContext;
        return this;
    }

    public Context createContext() {
        return new Context(awsRequestId, invokedFunctionArn, traceId, requestDeadline, identity, clientContext);
    }
}
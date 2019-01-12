package io.github.dropofwill;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

class Context implements com.amazonaws.services.lambda.runtime.Context {
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

    @Override
    public String getLogGroupName() {
        return null;
    }

    @Override
    public String getLogStreamName() {
        return null;
    }

    @Override
    public String getFunctionName() {
        return null;
    }

    @Override
    public String getFunctionVersion() {
        return null;
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

    public CognitoIdentity getIdentity() {
        return identity;
    }

    public ClientContext getClientContext() {
        return clientContext;
    }

    @Override
    public int getRemainingTimeInMillis() {
        return 0;
    }

    @Override
    public int getMemoryLimitInMB() {
        return 0;
    }

    @Override
    public LambdaLogger getLogger() {
        return null;
    }
}

package io.github.dropofwill;

import org.apache.http.HttpResponse;

public class StdRuntimeClient implements RuntimeClient {

    @Override
    public LambdaEvent next() {
//        URLConnection connection = new URL(String.format("%s/%s/runtime/invocation/next",
//                Config.getEndpoint(), RUNTIME_API_VERSION));
//
//        connection.setReadTimeout(0);
//        connection.setConnectTimeout(1000);
//
//        try (InputStream is = connection.getInputStream()) {
//            BufferedReader rd = new BufferedReader(
//                new InputStreamReader(is, Charset.forName("UTF-8")));
//
//        }  catch (IOException fatal) {
//            throw new RuntimeException(fatal);
//        }
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
}

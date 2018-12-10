package io.github.dropofwill;

public class HelloLambda {
    private static final RequestHandler<byte[], byte[]> helloWorld = (input, context) -> {
        System.out.println("Hello from inside handler");
        return "Hello world".getBytes();
    };

    public static void main(String[] args) {
        Runtime.with(helloWorld);
    }
}

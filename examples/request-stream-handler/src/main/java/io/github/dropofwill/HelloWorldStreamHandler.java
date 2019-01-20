package io.github.dropofwill.examples.stream;

import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.stream.Collectors;

public class HelloWorldStreamHandler {

    /**
     * Simple lambda that writes the input as Hello ${input}
     */
    public static final RequestStreamHandler streamHelloWorld =
        (inputStream, outputStream, context) -> {
        String input = new BufferedReader(new InputStreamReader(inputStream)).lines()
                .collect(Collectors.joining("\n"));
        outputStream.write(("Hello " + input).getBytes());
    };

    /**
     * Example of doing JSON parsing on with the RequestStreamHandler interface
     */
    public static final RequestStreamHandler streamPojoWorld =
        (inputStream, outputStream, context) -> {
            Pojo request = Pojo.read(inputStream);
            request.setWho("Hello " + request.getWho());
            Pojo.write(request, outputStream);
    };

    public static class Pojo  {
        private static final ObjectMapper mapper = new ObjectMapper();

        @JsonProperty("who")
        private String who;

        public Pojo(String who) {
            this.who = who;
        }

        public static Pojo read(InputStream inputStream) {
            try {
                return mapper.readValue(inputStream, Pojo.class);
            } catch (IOException bubbleUp) {
                throw new RuntimeException(bubbleUp);
            }
        }

        public static void write(Pojo output, OutputStream outputStream) {
            try {
                mapper.writeValue(outputStream, output);
            } catch (IOException bubbleUp) {
                throw new RuntimeException(bubbleUp);
            }
        }

        public String getWho() {
            return who;
        }

        public void setWho(String who) {
            this.who = who;
        }
    }
}
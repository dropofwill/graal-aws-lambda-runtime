package io.github.dropofwill.examples.json;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.annotation.JsonProperty;

public class HelloWorldHandler {

    public static final RequestHandler<Pojo, Pojo> helloWorldJson = 
        (input, context) -> input.setWho("Hello " + input.getWho());

    public static class Pojo  {

        @JsonProperty("who")
        private String who;

        public Pojo(String who) {
            this.who = who;
        }

        public String getWho() {
            return who;
        }

        public Pojo setWho(String who) {
            this.who = who;
            return this;
        }
    }
}
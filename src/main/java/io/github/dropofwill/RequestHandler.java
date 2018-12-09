package io.github.dropofwill;

public interface RequestHandler<Input, Output> {
    Output handleRequest(Input input, Context context);
}


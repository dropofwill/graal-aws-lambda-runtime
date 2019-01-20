# GraalVM based AWS Lamba Runtime

This project demonstrates using the Graal's SubstrateVM native-image capabilities to generate a binary AWS Lambda Runtime to combat the long cold start times JVM based lambdas see today. This is still very much a WIP, but the basic workings are there and a Hello World lambda is working pretty well (10ms cold starts).

## Usage

GraalVM supports reflection, but in an awkward way (basically you have to ahead of time tell the compiler what reflections will resolve to), so there are some slight differences for end users than the Java runtime AWS maintains.

### RequestStramHanlder

The low level RequestStreamHandler interface from AWS is implemented, so the only difference for end users is to create a fat jar that kicks off the runtime.

```
private static final RequestStreamHandler handler = (inputStream, outputStream, context) -> {
    String input = new BufferedReader(new InputStreamReader(inputStream)).lines()
            .collect(Collectors.joining("\n"));
    outputStream.write(("Hello world and " + input).getBytes());
};

public static void main(String[] args) {
    Runtime.with(handler);
}
```

Then to build a deployable lambda run:
```
docker-compose build --build-arg JAR_FILE=<path to jar> package \
   && docker-compose run package > <path to where you want the zip>
```

## TODO
* Support all the header propagation for X-Ray et al.
* Cleaner interface for Java Lambdas (currently just exposing the low level `RequestHandler<byte[], byte[]>`, no JSON marshalling for example)
* Add Scala support

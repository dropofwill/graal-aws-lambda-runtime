# GraalVM based AWS Lamba Runtime

This project demonstrates using the Graal's SubstrateVM native-image capabilities to generate a binary AWS Lambda Runtime to combat the long cold start times JVM based lambdas see today. This is still very much a WIP, but the basic workings are there and a Hello World lambda is working pretty well (10ms cold starts).

## Usage

Create a Fat Jar with a main method like the following (see example HelloLambda):

```
private static final RequestHandler<byte[], byte[]> handler =
  (input, context) -> "Hello world".getBytes();

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

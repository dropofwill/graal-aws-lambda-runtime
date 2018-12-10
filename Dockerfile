FROM oracle/graalvm-ce:1.0.0-rc10

RUN yum install -y zip

RUN mkdir /app
WORKDIR /app
ARG JAR_FILE

COPY target/ .
# In order for any Cryto libs to work, this .so must be distributed with the
# lambda at runtime (by default in the same dir).
RUN cp /$JAVA_HOME/jre/lib/amd64/libsunec.so .
RUN native-image --verbose --no-server --enable-all-security-services --enable-https -H:+ReportUnsupportedElementsAtRuntime -jar ${JAR_FILE}

RUN mv ./$(basename -s .jar ${JAR_FILE}) ./bootstrap
RUN zip lambda.zip ./bootstrap libsunec.so

CMD cat ./lambda.zip


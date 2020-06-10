FROM docker.na1.hsdp.io/phm-services/spark-hbase:2.4.4-1.4.9

LABEL stack="spark-app"

COPY src/main/resources/stackoverflow/stackoverflow.csv /opt/spark/work-dir/

ENV SCALA_VERSION=2.12.4 \
    SCALA_HOME=/usr/share/scala

COPY target/scala-2.12/kursova-spark-hbase_2.12-0.1.0-SNAPSHOT.jar /app/lib/

RUN  mv /app/lib/kursova*.jar $SPARK_HOME/jars/

# NOTE: bash is used by scala/scalac scripts, and it cannot be easily replaced with ash.
RUN apk add --no-cache --virtual=.build-dependencies wget ca-certificates && \
    apk add --no-cache bash curl jq && \
    cd "/tmp" && \
    wget --no-verbose "https://downloads.typesafe.com/scala/${SCALA_VERSION}/scala-${SCALA_VERSION}.tgz" && \
    tar xzf "scala-${SCALA_VERSION}.tgz" && \
    mkdir "${SCALA_HOME}" && \
    rm "/tmp/scala-${SCALA_VERSION}/bin/"*.bat && \
    mv "/tmp/scala-${SCALA_VERSION}/bin" "/tmp/scala-${SCALA_VERSION}/lib" "${SCALA_HOME}" && \
    ln -s "${SCALA_HOME}/bin/"* "/usr/bin/" && \
    apk del .build-dependencies && \
    rm -rf "/tmp/"*

RUN export PATH="/usr/local/sbt/bin:$PATH" && apk update && apk add ca-certificates wget tar && mkdir -p "/usr/local/sbt" && wget -qO - --no-check-certificate "https://piccolo.link/sbt-0.13.16.tgz" | tar xz -C /usr/local/sbt --strip-components=1 && sbt sbtVersion

ENTRYPOINT ["/opt/entrypoint.sh"]
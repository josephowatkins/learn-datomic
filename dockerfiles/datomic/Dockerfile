FROM openjdk:8-jdk-slim

ENV DATOMIC_VERSION 0.9.5561.62

ADD https://my.datomic.com/downloads/free/${DATOMIC_VERSION} /tmp/datomic.zip

RUN unzip /tmp/datomic.zip && rm /tmp/datomic.zip

WORKDIR datomic-free-${DATOMIC_VERSION}

RUN cp config/samples/free-transactor-template.properties transactor.properties

RUN sed -i 's/host=localhost/host=0.0.0.0/' transactor.properties

RUN mkdir data
RUN sed -i 's/# data-dir=/data-dir=/' transactor.properties
VOLUME /data

RUN mkdir log
RUN sed -i 's/# log-dir=/log-dir=/' transactor.properties
VOLUME /log

ADD start.sh .
RUN chmod +x start.sh

EXPOSE 4334 4335 4336

CMD ["./start.sh"]
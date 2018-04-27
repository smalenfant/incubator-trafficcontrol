FROM maven:3.5.3-jdk-8

VOLUME root/.m2
ENV RHEL_VERSION el7
ENV BUILD_NUMBER 0
ADD http://www.verisignlabs.com/dnssec-tools/packages/old-releases/jdnssec-tools-0.12.tar.gz /tmp/jdnssec-tools-0.12.tar.gz
# RUN rm -f /tmp/jdnssec-tools-0.12.tar.gz

RUN mkdir /traffic_router
ADD traffic_router/ traffic_router/
RUN gunzip -c /tmp/jdnssec-tools-0.12.tar.gz | tar xvf -
WORKDIR traffic_router

# Install jdnssec-tools in the traffic_router project
#RUN mvn install::install-file -Dfile=/jdnssec-tools-0.12/lib/jdnssec-tools.jar -DgroupId=jdnssec -Dpackaging=jar -DartifactId=jdnssec-tools -Dversion="0.12"
RUN mvn install:install-file -Dfile=/jdnssec-tools-0.12/lib/jdnssec-tools.jar -DgroupId=jdnssec -DartifactId=jdnssec-tools -Dversion=0.12 -Dpackaging=jar
RUN mvn -P docker-build -Dmaven.test.skip=true -DminimumTPS=1 clean package

#RUN mvn clean package

#FROM tomcat:8.5.30-jre8
#MAINTAINER Steve Malenfant <steve.malenfant@cox.com>

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
############################################################
# Dockerfile to build native Docker image
############################################################

# Example Build and Run:
# docker build --rm -tag traffic_router -f infrastructure/docker/traffic_router/Dockerfile .

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
RUN mvn install:install-file -Dfile=/jdnssec-tools-0.12/lib/jdnssec-tools.jar -DgroupId=jdnssec -DartifactId=jdnssec-tools -Dversion=0.12 -Dpackaging=jar
RUN mvn -P !rpm-build -Dmaven.test.skip=true -DminimumTPS=1 clean package

#FROM tomcat:8.5.30-jre8
FROM tomcat:6.0.53-jdk7
MAINTAINER Steve Malenfant <steve.malenfant@cox.com>

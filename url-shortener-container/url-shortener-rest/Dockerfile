#############################
# Build script              #
#############################

FROM centos:7
USER root

#############################
# Install jdk8, gradle, git #
#############################

RUN yum -y install java-1.8.0-openjdk-devel wget unzip
RUN wget https://services.gradle.org/distributions/gradle-5.0-bin.zip -P /tmp
RUN unzip -d /opt/gradle /tmp/gradle-5.0-bin.zip
ENV JAVA_HOME=/usr
ENV PATH=$PATH:/opt/gradle/gradle-5.0/bin

#############################
# Build and run             #
#############################

WORKDIR /root/
RUN git clone --single-branch --branch springboot-refactor https://github.com/vilaca/UrlShortener.git
RUN mkdir UrlShortener/data
WORKDIR UrlShortener/url-shortener-rest
RUN gradle build -x test
CMD gradle bootRun

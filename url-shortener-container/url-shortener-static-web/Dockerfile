
#############################
# Static files              #
#############################

FROM centos:7
USER root

#############################
# Install git and nginx     #
#############################

RUN yum -y install git epel-release
RUN yum -y install nginx

#############################
# Build source              #
#############################

WORKDIR /root/
RUN git clone --single-branch --branch docker-fix https://github.com/vilaca/UrlShortener.git
WORKDIR UrlShortener/url-shortener-static-web/
RUN ls -ltr
RUN ls -ltr
COPY wwwroot/index.html /webroot/
COPY wwwroot/script.js /webroot/
COPY wwwroot/style.css /webroot/
COPY configuration/nginx.conf /etc/nginx/conf.d/default.conf
CMD ls -ltr


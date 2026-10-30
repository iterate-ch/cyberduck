FROM httpd:2.4

RUN mkdir -p /data /usr/local/apache2/var && \
    chown daemon:daemon /data /usr/local/apache2/var

COPY httpd.conf /usr/local/apache2/conf/httpd.conf
COPY htpasswd /usr/local/apache2/conf/htpasswd

EXPOSE 80

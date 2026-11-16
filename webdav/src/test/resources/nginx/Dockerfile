FROM debian:13.2-slim

RUN apt-get update && \
    apt-get install -y --no-install-recommends nginx-light libnginx-mod-http-dav-ext && \
    rm -rf /var/lib/apt/lists/* && \
    mkdir -p /data && \
    chown www-data:www-data /data

COPY nginx.conf /etc/nginx/nginx.conf
COPY htpasswd /etc/nginx/htpasswd

EXPOSE 80

CMD ["nginx"]

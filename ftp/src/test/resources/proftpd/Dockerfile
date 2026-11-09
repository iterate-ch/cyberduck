FROM debian:stable-slim

RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends proftpd-basic \
    && rm -rf /var/lib/apt/lists/*

RUN useradd --create-home --shell /bin/sh test \
    && echo "test:test" | chpasswd

COPY proftpd.conf /etc/proftpd/proftpd.conf

EXPOSE 21
CMD ["proftpd", "--nodaemon"]

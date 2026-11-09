FROM debian:stable-slim

RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends proftpd-basic proftpd-mod-crypto openssh-client \
    && rm -rf /var/lib/apt/lists/*

RUN ssh-keygen -q -t rsa -b 3072 -N "" -f /etc/proftpd/ssh_host_rsa_key \
    && ssh-keygen -q -t ecdsa -N "" -f /etc/proftpd/ssh_host_ecdsa_key \
    && ssh-keygen -q -t ed25519 -N "" -f /etc/proftpd/ssh_host_ed25519_key

RUN useradd --create-home --shell /bin/sh test \
    && echo "test:test" | chpasswd

COPY proftpd.conf /etc/proftpd/proftpd.conf

EXPOSE 2222
CMD ["proftpd", "--nodaemon"]

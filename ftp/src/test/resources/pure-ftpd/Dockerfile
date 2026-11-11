FROM debian:stable-slim

RUN apt-get update \
    && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends pure-ftpd \
    && rm -rf /var/lib/apt/lists/*

RUN useradd --create-home --shell /bin/sh test \
    && echo "test:test" | chpasswd

EXPOSE 21

# Authenticate against system users, no anonymous. Passive data connections are made to the port announced by the
# server and must be mapped one to one
CMD ["pure-ftpd", "-l", "unix", "-E", "-c", "50", "-C", "50", "-P", "127.0.0.1", "-p", "21040:21050"]

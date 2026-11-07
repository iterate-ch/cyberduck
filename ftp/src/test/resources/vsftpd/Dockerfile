FROM alpine:3.24

RUN apk add --no-cache vsftpd linux-pam tini

RUN mkdir -p /ftp \
    && adduser -D -h /ftp/test -s /bin/sh test \
    && echo "test:test" | chpasswd

COPY vsftpd.conf /etc/vsftpd/vsftpd.conf
COPY pam /etc/pam.d/vsftpd

EXPOSE 21

# vsftpd crashes randomly when running in the foreground as PID 1, run it with an init process.
# Passive port range and address are set on the command line
ENTRYPOINT ["/sbin/tini", "--"]
CMD ["vsftpd", "/etc/vsftpd/vsftpd.conf"]

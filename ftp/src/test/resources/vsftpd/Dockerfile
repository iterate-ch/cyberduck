FROM alpine:3.24

RUN apk add --no-cache vsftpd linux-pam tini

ARG FTP_USER
ARG FTP_PASSWORD
ARG FTP_HOME

RUN mkdir -p "$(dirname "${FTP_HOME}")" \
    && adduser -D -h "${FTP_HOME}" -s /bin/sh "${FTP_USER}" \
    && echo "${FTP_USER}:${FTP_PASSWORD}" | chpasswd

COPY vsftpd.conf /etc/vsftpd/vsftpd.conf
COPY pam /etc/pam.d/vsftpd

EXPOSE 21

# vsftpd crashes randomly when running in the foreground as PID 1, run it with an init process.
# Passive port range and address are set on the command line
ENTRYPOINT ["/sbin/tini", "--"]
CMD ["vsftpd", "/etc/vsftpd/vsftpd.conf"]

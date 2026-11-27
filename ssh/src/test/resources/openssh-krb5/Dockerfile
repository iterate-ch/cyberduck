FROM linuxserver/openssh-server:10.3_p1-r2-ls242
# Variant of sshd built with GSS-API support using MIT Kerberos instead of PAM
RUN apk add --no-cache openssh-server-krb5 \
    && sed -i 's#/usr/sbin/sshd.pam#/usr/sbin/sshd.krb5#' /etc/s6-overlay/s6-rc.d/svc-openssh-server/run \
    && grep -q sshd.krb5 /etc/s6-overlay/s6-rc.d/svc-openssh-server/run

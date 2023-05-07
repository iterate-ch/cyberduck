FROM alpine:3.7

RUN apk update && apk add --no-cache tini samba samba-common-tools supervisor bash

ENV SMB_USER smbj
ENV SMB_PASSWORD pass

COPY smb.conf /etc/samba/smb.conf
COPY supervisord.conf /etc/supervisord.conf
COPY entrypoint.sh /entrypoint.sh
ADD container-files/public /opt/samba/public
ADD container-files/user /opt/samba/user

RUN mkdir -p /opt/samba/readonly /opt/samba/user /opt/samba/dfs && \
    chmod 777 /opt/samba/readonly /opt/samba/user /opt/samba/dfs && \
    adduser -s /bin/false "$SMB_USER" -D $SMB_PASSWORD && \
    (echo "$SMB_PASSWORD"; echo "$SMB_PASSWORD" ) | pdbedit -a -u "$SMB_USER" && \
    chmod ugo+x /entrypoint.sh

EXPOSE 137/udp 138/udp 139 445

ENTRYPOINT ["/sbin/tini", "/entrypoint.sh"]
CMD ["supervisord"]


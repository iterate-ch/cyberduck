# Debian testing for a current Samba release (4.25), which grants SMB3 directory leases by default
FROM debian:forky-slim

RUN apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends tini samba samba-common-bin && rm -rf /var/lib/apt/lists/*

ENV SMB_USER=smbj
ENV SMB_PASSWORD=pass

COPY smb.conf /etc/samba/smb.conf
ADD entrypoint.sh /entrypoint.sh

RUN mkdir -p /opt/samba/public /opt/samba/readonly /opt/samba/user /opt/samba/dfs && \
    mkdir -p /opt/samba/readonly/empty_folder /opt/samba/user/empty_folder /opt/samba/dfs/empty_folder && \
    chmod -R 777 /opt/samba/public /opt/samba/readonly /opt/samba/user /opt/samba/dfs && \
    useradd -M -s /bin/false "$SMB_USER" && \
    (echo "$SMB_PASSWORD"; echo "$SMB_PASSWORD" ) | smbpasswd -s -a "$SMB_USER" && \
    chmod ugo+x /entrypoint.sh

EXPOSE 445

ENTRYPOINT ["/usr/bin/tini", "--", "/entrypoint.sh"]
CMD ["/usr/sbin/smbd", "--foreground", "--no-process-group", "--debug-stdout"]

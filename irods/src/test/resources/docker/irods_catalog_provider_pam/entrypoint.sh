#! /bin/bash -e

echo "Waiting for iRODS catalog database to be ready"
catalog_db_hostname=irods-catalog
until pg_isready -h ${catalog_db_hostname} -d ICAT -U irods -q; do
    sleep 1
done
echo "iRODS catalog database is ready"

unattended_install_file=/unattended_install.json
if [ -f "${unattended_install_file}" ]; then
    echo "Running iRODS setup"

    # Configure the server with secure communication (TLS) disabled. This
    # avoids issues with setup (e.g. the post install test).

    # Add generated hostname as a recognizable alias.
    sed -i "s/CONTAINER_HOSTNAME_ALIAS/${HOSTNAME}/g" ${unattended_install_file}
    python3 /var/lib/irods/scripts/setup_irods.py --json_configuration_file ${unattended_install_file}

    # Move the input file used to configure the server out of the way so
    # the container is restartable.
    mv ${unattended_install_file} ${unattended_install_file}.processed

    # Generate a self-signed certificate for the server.
    openssl genrsa -out /tmp/irods_server.key 2048
    openssl req -batch -new -x509 -key /tmp/irods_server.key -out /tmp/irods_server.crt -days 1
    openssl dhparam -2 -out /tmp/irods_dhparams.pem 2048
    chown irods:irods /tmp/irods_server.key

    # Update the server's configuration to require secure communication.
    sed -i 's/="CS_NEG_REFUSE"/="CS_NEG_REQUIRE"/g' /etc/irods/core.re
    sed -i 's/CS_NEG_REFUSE/CS_NEG_REQUIRE/g' /etc/irods/server_config.json /var/lib/irods/.irods/irods_environment.json

    # Configure PAM.
    ln -s /pam_password /etc/pam.d/irods

    # Set up the test user, for PAM authentication.
    useradd -m john
    echo "john:=i;r@o\\d&s" | chpasswd
fi

echo "Starting server"
su - irods -c 'irodsServer --stdout'

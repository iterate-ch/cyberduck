package ch.cyberduck.core.sts;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.test.TestcontainerTest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Category(TestcontainerTest.class)
public abstract class AbstractAssumeRoleWithWebIdentityTest {
    protected static final Logger log = LogManager.getLogger(AbstractAssumeRoleWithWebIdentityTest.class);

    protected static final long MILLIS = 1000;
    protected static int OAUTH_TTL_SECS = 5;

    protected static Profile profile = null;



    public static DockerComposeContainer prepareDockerComposeContainer(final String keyCloakRealmTempFile) {
        log.info("Preparing docker compose container...");
        return new DockerComposeContainer<>(
                new File(AbstractAssumeRoleWithWebIdentityTest.class.getResource("/testcontainer/docker-compose.yml").getFile()))
                .withEnv("KEYCLOAK_REALM_JSON", keyCloakRealmTempFile)
                .withPull(false)
                .withLocalCompose(true)
                .withOptions("--compatibility")
                .withExposedService("keycloak_1", 8080, Wait.forListeningPort())
                .withExposedService("minio_1", 9000, Wait.forListeningPort());
    }

    public static String getKeyCloakFile(Map<String, String> replacements) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement je = new Gson().fromJson(new InputStreamReader(AbstractAssumeRoleWithWebIdentityTest.class.getResourceAsStream("/testcontainer/keycloak/keycloak-realm.json")), JsonElement.class);
        JsonObject jo = je.getAsJsonObject();

        for(Map.Entry<String, String> replacement : replacements.entrySet()) {
            updateJsonValues(jo, replacement.getKey(), replacement.getValue());
        }

        String content = gson.toJson(jo);
        try {
            final Path tempFile = Files.createTempFile(null, null);
            Files.write(tempFile, content.getBytes(StandardCharsets.UTF_8));
            return tempFile.toAbsolutePath().toString();

        }
        catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void updateJsonValues(JsonObject jsonObj, String key, String newVal) {
        for(Map.Entry<String, JsonElement> entry : jsonObj.entrySet()) {
            JsonElement element = entry.getValue();
            if(element.isJsonArray()) {
                updateJsonValues(element.getAsJsonArray(), key, newVal);
            }
            else if(element.isJsonObject()) {
                updateJsonValues(element.getAsJsonObject(), key, newVal);
            }
            else if(entry.getKey().equals(key)) {
                jsonObj.remove(key);
                jsonObj.addProperty(key, newVal);
                break;
            }
        }
    }

    private static void updateJsonValues(JsonArray asJsonArray, String key, String newVal) {
        for(int index = 0; index < asJsonArray.size(); index++) {
            JsonElement element = asJsonArray.get(index);
            if(element.isJsonArray()) {
                updateJsonValues(element.getAsJsonArray(), key, newVal);
            }
            else if(element.isJsonObject()) {
                updateJsonValues(element.getAsJsonObject(), key, newVal);
            }
        }
    }

    public static String getKeyCloakFile() {
        return getKeyCloakFile(Collections.emptyMap());
    }

    public static Profile readProfile() throws AccessDeniedException {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        return new ProfilePlistReader(factory).read(
                AbstractAssumeRoleWithWebIdentityTest.class.getResourceAsStream("/S3 (OIDC).cyberduckprofile"));
    }
}
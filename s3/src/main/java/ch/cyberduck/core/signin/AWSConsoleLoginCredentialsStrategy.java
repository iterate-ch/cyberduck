package ch.cyberduck.core.signin;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.TemporaryAccessTokens;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.io.StreamGobbler;
import ch.cyberduck.core.s3.S3CredentialsStrategy;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Temporary S3 credentials exported by an AWS CLI browser-login profile.
 */
public class AWSConsoleLoginCredentialsStrategy implements S3CredentialsStrategy {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static final String DEFAULT_REGION = "us-east-1";
    static final String PROFILE_PREFIX = "cyberduck";
    static final String IDENTITY_PROPERTY = "s3.login.identity";

    private final Host host;
    private final CancelCallback cancel;
    private final String profile;
    private TemporaryAccessTokens tokens = TemporaryAccessTokens.EMPTY;

    public AWSConsoleLoginCredentialsStrategy(final Host host, final CancelCallback cancel) {
        this.host = host;
        this.cancel = cancel;
        this.profile = String.format("%s-%s", PROFILE_PREFIX, host.getUuid());
        host.setCredentials(new Credentials().setSaved(false));
    }

    @Override
    public synchronized Credentials get() throws BackgroundException {
        if(tokens.isExpired()) {
            Result result = this.export();
            if(!result.success()) {
                final Result login = this.execute("login", "--profile", profile, "--region",
                        StringUtils.defaultIfBlank(host.getRegion(), DEFAULT_REGION),
                        "--no-cli-auto-prompt", "--no-cli-pager");
                if(!login.success()) {
                    throw failure(login.error);
                }
                result = this.export();
            }
            if(!result.success()) {
                throw failure(result.error);
            }
            final TemporaryAccessTokens refreshed = parse(result.output);
            this.validateIdentity();
            tokens = refreshed;
        }
        return new Credentials().setTokens(tokens).setSaved(false);
    }

    private Result export() throws BackgroundException {
        return this.execute("configure", "export-credentials", "--profile", profile, "--region",
                StringUtils.defaultIfBlank(host.getRegion(), DEFAULT_REGION), "--format", "process",
                "--no-cli-auto-prompt", "--no-cli-pager");
    }

    private void validateIdentity() throws BackgroundException {
        final Result result = this.execute("sts", "get-caller-identity", "--profile", profile, "--region",
                StringUtils.defaultIfBlank(host.getRegion(), DEFAULT_REGION), "--output", "json",
                "--no-cli-auto-prompt", "--no-cli-pager");
        if(!result.success()) {
            throw failure(result.error);
        }
        try {
            final JsonNode identity = MAPPER.readTree(result.output);
            if(null == identity || !identity.isObject()) {
                throw failure();
            }
            validateIdentity(host, identity.path("Account").asText(), identity.path("Arn").asText());
        }
        catch(IOException e) {
            throw failure(e);
        }
    }

    protected Result execute(final String... arguments) throws BackgroundException {
        final List<String> command = new ArrayList<>();
        command.add(executable());
        Collections.addAll(command, arguments);
        final Process process;
        try {
            process = new ProcessBuilder(command).start();
        }
        catch(IOException e) {
            throw failure(e);
        }
        try(InputStream output = new StreamGobbler(process.getInputStream());
            InputStream error = new StreamGobbler(process.getErrorStream())) {
            // Browser login must never wait on an invisible terminal prompt.
            process.getOutputStream().close();
            while(!process.waitFor(250L, TimeUnit.MILLISECONDS)) {
                cancel.verify();
            }
            return new Result(process.exitValue(),
                    IOUtils.toString(output, StandardCharsets.UTF_8),
                    IOUtils.toString(error, StandardCharsets.UTF_8));
        }
        catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LoginCanceledException(e);
        }
        catch(IOException e) {
            throw failure(e);
        }
        finally {
            process.destroy();
        }
    }

    private static TemporaryAccessTokens parse(final String output) throws LoginFailureException {
        try {
            final JsonNode value = MAPPER.readTree(output);
            if(null == value || !value.isObject()) {
                throw failure();
            }
            final String accessKey = value.path("AccessKeyId").asText();
            final String secretKey = value.path("SecretAccessKey").asText();
            final String sessionToken = value.path("SessionToken").asText();
            final long expiration = OffsetDateTime.parse(value.path("Expiration").asText()).toInstant().toEpochMilli();
            if(StringUtils.isAnyBlank(accessKey, secretKey, sessionToken) || expiration <= System.currentTimeMillis()) {
                throw failure();
            }
            return new TemporaryAccessTokens(accessKey, secretKey, sessionToken, expiration);
        }
        catch(IOException | DateTimeParseException e) {
            throw failure(e);
        }
    }

    private static String executable() {
        if(Factory.Platform.Name.mac.equals(Factory.Platform.getDefault())) {
            for(String candidate : new String[]{"/usr/local/bin/aws", "/opt/homebrew/bin/aws"}) {
                if(Files.isExecutable(Paths.get(candidate))) {
                    return candidate;
                }
            }
        }
        return Factory.Platform.Name.windows.equals(Factory.Platform.getDefault()) ? "aws.exe" : "aws";
    }

    private static String identity(final String account, final String arn) {
        String resource = StringUtils.substringAfterLast(arn, ":");
        if(StringUtils.startsWith(resource, "assumed-role/")) {
            resource = String.format("role/%s", StringUtils.substringBeforeLast(
                    StringUtils.removeStart(resource, "assumed-role/"), "/"));
        }
        return String.format("%s/%s", account, resource);
    }

    static void validateIdentity(final Host host, final String account, final String arn) throws LoginFailureException {
        if(StringUtils.isAnyBlank(account, arn)) {
            throw failure();
        }
        final String selected = identity(account, arn);
        final String pinned = host.getProperty(IDENTITY_PROPERTY);
        if(StringUtils.isNotBlank(pinned) && !StringUtils.equals(pinned, selected)) {
            throw new LoginFailureException(String.format(
                    "AWS identity %s does not match this bookmark (%s). "
                            + "Create a new bookmark to use the other identity.", selected, pinned));
        }
        host.setProperty(IDENTITY_PROPERTY, selected);
        if(StringUtils.isBlank(host.getNickname())) {
            host.setNickname(selected);
        }
    }

    private static LoginFailureException failure() {
        return failure((Throwable) null);
    }

    private static LoginFailureException failure(final Throwable cause) {
        return new LoginFailureException(
                "AWS browser sign-in failed. AWS CLI 2.32 or later is required.",
                cause);
    }

    private static LoginFailureException failure(final String detail) {
        if(StringUtils.isBlank(detail)) {
            return failure();
        }
        return new LoginFailureException(String.format(
                "AWS browser sign-in failed: %s", StringUtils.abbreviate(StringUtils.trim(detail), 1000)));
    }

    protected static final class Result {
        private final int status;
        private final String output;
        private final String error;

        protected Result(final int status, final String output, final String error) {
            this.status = status;
            this.output = output;
            this.error = error;
        }

        private boolean success() {
            return status == 0;
        }
    }
}

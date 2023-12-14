package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.binding.foundation.FoundationKitFunctions;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.keychain.SecKeychainItemRef;
import ch.cyberduck.core.keychain.SecurityFunctions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;

import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

import static ch.cyberduck.core.keychain.SecurityFunctions.*;

public final class KeychainPasswordStore extends DefaultHostPasswordStore implements PasswordStore {
    private static final Logger log = LogManager.getLogger(KeychainPasswordStore.class);

    private static final Object lock = new Object();

    @Override
    public String getPassword(final Scheme scheme, final int port, final String serviceName, final String accountName) throws LocalAccessDeniedException {
        synchronized(lock) {
            final IntByReference passwordLength = new IntByReference();
            final PointerByReference passwordRef = new PointerByReference();
            final int err = SecurityFunctions.library.SecKeychainFindInternetPassword(null,
                serviceName.getBytes(StandardCharsets.UTF_8).length, serviceName.getBytes(StandardCharsets.UTF_8),
                0, null,
                accountName.getBytes(StandardCharsets.UTF_8).length, accountName.getBytes(StandardCharsets.UTF_8),
                0, null,
                port, toSecProtocolType(scheme), SecurityFunctions.kSecAuthenticationTypeDefault,
                passwordLength, passwordRef, null);
            if(errSecSuccess == err) {
                return new String(passwordRef.getValue().getByteArray(0, passwordLength.getValue()), StandardCharsets.UTF_8);
            }
            if(errSecItemNotFound == err) {
                return null;
            }
            log.error(String.format("Failure reading credentials for %s from keychain", serviceName));
            throw new LocalAccessDeniedException(SecurityFunctions.library.SecCopyErrorMessageString(err, null));
        }
    }

    @Override
    public void addPassword(final Scheme scheme, final int port, final String serviceName, final String accountName, final String password) throws LocalAccessDeniedException {
        synchronized(lock) {
            int err = library.SecKeychainAddInternetPassword(null,
                serviceName.getBytes(StandardCharsets.UTF_8).length, serviceName.getBytes(StandardCharsets.UTF_8),
                0, null,
                accountName.getBytes(StandardCharsets.UTF_8).length, accountName.getBytes(StandardCharsets.UTF_8),
                0, null,
                port, toSecProtocolType(scheme), kSecAuthenticationTypeDefault,
                password.getBytes(StandardCharsets.UTF_8).length,
                password.getBytes(StandardCharsets.UTF_8), null);
            if(errSecDuplicateItem == err) {
                // Found existing item
                final PointerByReference itemRef = new PointerByReference();
                err = SecurityFunctions.library.SecKeychainFindInternetPassword(null,
                    serviceName.getBytes(StandardCharsets.UTF_8).length, serviceName.getBytes(StandardCharsets.UTF_8),
                    0, null,
                    accountName.getBytes(StandardCharsets.UTF_8).length, accountName.getBytes(StandardCharsets.UTF_8),
                    0, null,
                    port, toSecProtocolType(scheme), SecurityFunctions.kSecAuthenticationTypeDefault,
                    null, null, itemRef);
                if(errSecSuccess != err) {
                    log.error(String.format("Failure saving credentials for %s in keychain", serviceName));
                    throw new LocalAccessDeniedException(SecurityFunctions.library.SecCopyErrorMessageString(err, null));
                }
                err = library.SecKeychainItemModifyContent(new SecKeychainItemRef(itemRef.getValue()), null,
                    password.getBytes(StandardCharsets.UTF_8).length,
                    password.getBytes(StandardCharsets.UTF_8));
                if(errSecSuccess != err) {
                    log.error(String.format("Failure modifying credentials for %s in keychain", serviceName));
                    throw new LocalAccessDeniedException(SecurityFunctions.library.SecCopyErrorMessageString(err, null));
                }
                FoundationKitFunctions.library.CFRelease(new SecKeychainItemRef(itemRef.getValue()));
            }
            if(errSecSuccess != err) {
                log.error(String.format("Failure saving credentials for %s in keychain", serviceName));
                throw new LocalAccessDeniedException(SecurityFunctions.library.SecCopyErrorMessageString(err, null));
            }
        }
    }

    @Override
    public void deletePassword(final Scheme scheme, final int port, final String serviceName, final String accountName) throws LocalAccessDeniedException {
        synchronized(lock) {
            final PointerByReference itemRef = new PointerByReference();
            int err = SecurityFunctions.library.SecKeychainFindInternetPassword(null,
                serviceName.getBytes(StandardCharsets.UTF_8).length, serviceName.getBytes(StandardCharsets.UTF_8),
                0, null,
                accountName.getBytes(StandardCharsets.UTF_8).length, accountName.getBytes(StandardCharsets.UTF_8),
                0, null,
                port,
                toSecProtocolType(scheme), SecurityFunctions.kSecAuthenticationTypeDefault,
                null, null, itemRef);
            if(errSecSuccess == err) {
                err = SecurityFunctions.library.SecKeychainItemDelete(itemRef.getValue());
                if(errSecSuccess != err) {
                    log.error(String.format("Failure deleting credentials for %s in keychain", serviceName));
                    throw new LocalAccessDeniedException(SecurityFunctions.library.SecCopyErrorMessageString(err, null));
                }
                return;
            }
            if(errSecItemNotFound == err) {
                return;
            }
            log.error(String.format("Failure deleting credentials for %s in keychain", serviceName));
            throw new LocalAccessDeniedException(SecurityFunctions.library.SecCopyErrorMessageString(err, null));
        }
    }

    @Override
    public String getPassword(final String serviceName, final String accountName) throws LocalAccessDeniedException {
        synchronized(lock) {
            final IntByReference passwordLength = new IntByReference();
            final PointerByReference passwordRef = new PointerByReference();
            int err = SecurityFunctions.library.SecKeychainFindGenericPassword(null,
                serviceName.getBytes(StandardCharsets.UTF_8).length, serviceName.getBytes(StandardCharsets.UTF_8),
                accountName.getBytes(StandardCharsets.UTF_8).length, accountName.getBytes(StandardCharsets.UTF_8),
                passwordLength, passwordRef, null);
            if(errSecSuccess == err) {
                return new String(passwordRef.getValue().getByteArray(0, passwordLength.getValue()), StandardCharsets.UTF_8);
            }
            if(errSecItemNotFound == err) {
                return null;
            }
            log.error(String.format("Failure reading credentials for %s in keychain", serviceName));
            throw new LocalAccessDeniedException(SecurityFunctions.library.SecCopyErrorMessageString(err, null));
        }
    }

    @Override
    public void addPassword(final String serviceName, final String accountName, final String password) throws LocalAccessDeniedException {
        synchronized(lock) {
            int err = library.SecKeychainAddGenericPassword(null,
                serviceName.getBytes(StandardCharsets.UTF_8).length, serviceName.getBytes(StandardCharsets.UTF_8),
                accountName.getBytes(StandardCharsets.UTF_8).length, accountName.getBytes(StandardCharsets.UTF_8),
                password.getBytes(StandardCharsets.UTF_8).length,
                password.getBytes(StandardCharsets.UTF_8), null);
            if(errSecDuplicateItem == err) {
                // Found existing item
                final PointerByReference itemRef = new PointerByReference();
                err = SecurityFunctions.library.SecKeychainFindGenericPassword(null,
                    serviceName.getBytes(StandardCharsets.UTF_8).length, serviceName.getBytes(StandardCharsets.UTF_8),
                    accountName.getBytes(StandardCharsets.UTF_8).length, accountName.getBytes(StandardCharsets.UTF_8),
                    null, null, itemRef);
                if(errSecSuccess != err) {
                    log.error(String.format("Failure saving credentials for %s in keychain", serviceName));
                    throw new LocalAccessDeniedException(SecurityFunctions.library.SecCopyErrorMessageString(err, null));
                }
                err = library.SecKeychainItemModifyContent(new SecKeychainItemRef(itemRef.getValue()), null,
                    password.getBytes(StandardCharsets.UTF_8).length,
                    password.getBytes(StandardCharsets.UTF_8));
                FoundationKitFunctions.library.CFRelease(new SecKeychainItemRef(itemRef.getValue()));
                if(errSecSuccess != err) {
                    log.error(String.format("Failure saving credentials for %s in keychain", serviceName));
                    throw new LocalAccessDeniedException(SecurityFunctions.library.SecCopyErrorMessageString(err, null));
                }
            }
            if(errSecSuccess != err) {
                log.error(String.format("Failure saving credentials for %s in keychain", serviceName));
                throw new LocalAccessDeniedException(SecurityFunctions.library.SecCopyErrorMessageString(err, null));
            }
        }
    }

    @Override
    public void deletePassword(final String serviceName, final String accountName) throws LocalAccessDeniedException {
        synchronized(lock) {
            final PointerByReference itemRef = new PointerByReference();
            int err = SecurityFunctions.library.SecKeychainFindGenericPassword(null,
                serviceName.getBytes(StandardCharsets.UTF_8).length, serviceName.getBytes(StandardCharsets.UTF_8),
                accountName.getBytes(StandardCharsets.UTF_8).length, accountName.getBytes(StandardCharsets.UTF_8),
                null, null, itemRef);
            if(errSecSuccess == err) {
                err = SecurityFunctions.library.SecKeychainItemDelete(itemRef.getValue());
                if(errSecSuccess != err) {
                    log.error(String.format("Failure deleting credentials for %s in keychain", serviceName));
                    throw new LocalAccessDeniedException(SecurityFunctions.library.SecCopyErrorMessageString(err, null));
                }
                return;
            }
            if(errSecItemNotFound == err) {
                return;
            }
            log.error(String.format("Failure deleting credentials for %s in keychain", serviceName));
            throw new LocalAccessDeniedException(SecurityFunctions.library.SecCopyErrorMessageString(err, null));
        }
    }

    private static int toSecProtocolType(final Scheme scheme) {
        switch(scheme) {
            case ftp:
                return kSecProtocolTypeFTP;
            case ftps:
                return kSecProtocolTypeFTPS;
            case sftp:
                return kSecProtocolTypeSSH;
            case http:
                return kSecProtocolTypeHTTP;
            case https:
                return kSecProtocolTypeHTTPS;
            default:
                return kSecProtocolTypeAny;
        }
    }
}

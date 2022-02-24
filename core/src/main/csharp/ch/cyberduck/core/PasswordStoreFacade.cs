//
// Copyright (c) 2010-2017 Yves Langisch. All rights reserved.
// http://cyberduck.io/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// Bug fixes, suggestions and comments should be sent to:
// feedback@cyberduck.io
//

using ch.cyberduck.core;
using org.apache.logging.log4j;
using System;
using System.Linq.Expressions;
using System.Reflection;

namespace Ch.Cyberduck.Core
{
    public class PasswordStoreFacade : HostPasswordStore
    {
        private readonly CredentialManagerPasswordStore credentialManagerPasswordStore = new CredentialManagerPasswordStore();
        private readonly DataProtectorPasswordStore dataProtectorPasswordStore = new DataProtectorPasswordStore();
        private readonly Logger logger = LogManager.getLogger(typeof(PasswordStoreFacade).AssemblyQualifiedName);

        void PasswordStore.addPassword(string serviceName, string accountName, string password)
        {
            RunWithFallback(x => x.addPassword, (Action<string, string, string> callback) => callback(serviceName, accountName, password));
        }

        void PasswordStore.addPassword(Scheme scheme, int port, string hostname, string user, string password)
        {
            RunWithFallback(x => x.addPassword, (Action<Scheme, int, string, string, string> callback) => callback(scheme, port, hostname, user, password));
        }

        void HostPasswordStore.delete(Host bookmark)
        {
            RunWithFallback(x => x.delete, (Action<Host> callback) => callback(bookmark));
        }

        void PasswordStore.deletePassword(string serviceName, string user)
        {
            RunWithFallback(x => x.deletePassword, (Action<string, string> callback) => callback(serviceName, user));
        }

        void PasswordStore.deletePassword(Scheme scheme, int port, string hostname, string user)
        {
            RunWithFallback(x => x.deletePassword, (Action<Scheme, int, string, string> callback) => callback(scheme, port, hostname, user));
        }

        string HostPasswordStore.findLoginPassword(Host bookmark)
        {
            return GetWithFallback(x => x.findLoginPassword, (Func<Host, string> callback) => callback(bookmark), v => !string.IsNullOrWhiteSpace(v));
        }

        string HostPasswordStore.findLoginToken(Host bookmark)
        {
            return GetWithFallback(x => x.findLoginToken, (Func<Host, string> callback) => callback(bookmark), v => !string.IsNullOrWhiteSpace(v));
        }

        OAuthTokens HostPasswordStore.findOAuthTokens(Host bookmark)
        {
            return GetWithFallback(x => x.findOAuthTokens, (Func<Host, OAuthTokens> callback) => callback(bookmark), v => v != OAuthTokens.EMPTY, OAuthTokens.EMPTY);
        }

        string HostPasswordStore.findPrivateKeyPassphrase(Host bookmark)
        {
            return GetWithFallback(x => x.findPrivateKeyPassphrase, (Func<Host, string> callback) => callback(bookmark), v => !string.IsNullOrWhiteSpace(v));
        }

        string PasswordStore.getPassword(string serviceName, string accountName)
        {
            return GetWithFallback(x => x.getPassword, (Func<string, string, string> callback) => callback(serviceName, accountName), v => !string.IsNullOrWhiteSpace(v));
        }

        string PasswordStore.getPassword(Scheme scheme, int port, string hostname, string user)
        {
            return GetWithFallback(
                x => x.getPassword,
                (Func<Scheme, int, string, string, string> callback) => callback(scheme, port, hostname, user),
                v => !string.IsNullOrWhiteSpace(v));
        }

        void HostPasswordStore.save(Host bookmark)
        {
            RunWithFallback(x => x.save, (Action<Host> callback) => callback(bookmark));
        }

        private T GetWithFallback<T>(Func<PasswordStore, (T, bool)> action, T defaultValue = default)
        {
            T value;
            bool result;
            (value, result) = action(credentialManagerPasswordStore);
            if (result)
                return value;
            (value, result) = action(dataProtectorPasswordStore);
            if (result)
                return value;
            return defaultValue;
        }

        private TValue GetWithFallback<TValue, TDelegate>(Expression<Func<HostPasswordStore, TDelegate>> expression, Func<TDelegate, TValue> callback, Predicate<TValue> filter, TValue defaultValue = default) where TDelegate : Delegate
        {
            var unaryExpression = (UnaryExpression)expression.Body;
            var methodCallExpression = (MethodCallExpression)unaryExpression.Operand;
            var methodInfoExpression = (ConstantExpression)methodCallExpression.Object;
            var methodInfo = (MethodInfo)methodInfoExpression.Value;

            return GetWithFallback(store =>
            {
                TValue value;
                try
                {
                    value = callback((TDelegate)methodInfo.CreateDelegate(typeof(TDelegate), store));
                }
                catch (Exception e)
                {
                    logger.error(e);
                    return (default, false);
                }
                return (value, filter(value));
            }, defaultValue);
        }

        private bool RunWithFallback(Func<PasswordStore, bool> action)
        {
            if (action(credentialManagerPasswordStore))
                return true;
            if (action(dataProtectorPasswordStore))
                return true;
            return false;
        }

        private void RunWithFallback<T>(Expression<Func<HostPasswordStore, T>> expression, Action<T> callback) where T : Delegate
        {
            var unaryExpression = (UnaryExpression)expression.Body;
            var methodCallExpression = (MethodCallExpression)unaryExpression.Operand;
            var methodInfoExpression = (ConstantExpression)methodCallExpression.Object;
            var methodInfo = (MethodInfo)methodInfoExpression.Value;

            RunWithFallback(store =>
            {
                try
                {
                    callback((T)methodInfo.CreateDelegate(typeof(T), store));
                }
                catch (Exception e)
                {
                    logger.error(e);
                    return false;
                }
                return true;
            });
        }
    }
}

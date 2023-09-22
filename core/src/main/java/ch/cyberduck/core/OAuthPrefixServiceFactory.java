package ch.cyberduck.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class OAuthPrefixServiceFactory extends Factory<OAuthPrefixService> {
    private static final Logger log = LogManager.getLogger(OAuthPrefixServiceFactory.class);

    public OAuthPrefixServiceFactory() {
        super("factory.oauthprefixservice.class");
    }

    public OAuthPrefixService create(Host bookmark) {
        try {
            final Constructor<? extends OAuthPrefixService> constructor = ConstructorUtils
                    .getMatchingAccessibleConstructor(clazz, Host.class);
            if (null == constructor) {
                log.warn(String.format("No matching constructor for parameter %s", Host.class));
                // Call default constructor for disabled implementations
                return clazz.getDeclaredConstructor().newInstance();
            }
            return constructor.newInstance(bookmark);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException
                | NoSuchMethodException e) {
            log.error(String.format("Failure loading oauthprefixservice class %s. %s", clazz, e.getMessage()));
            return new DefaultOAuthPrefixService(bookmark);
        }
    }
}
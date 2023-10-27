package ch.cyberduck.core;

import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class PasswordStorePrefixServiceFactory extends Factory<PasswordStorePrefixService> {
    private static final Logger log = LogManager.getLogger(PasswordStorePrefixServiceFactory.class);

    public PasswordStorePrefixServiceFactory() {
        super("factory.passwordstoreprefixservice.class");
    }

    public PasswordStorePrefixService create(Host bookmark) {
        try {
            final Constructor<? extends PasswordStorePrefixService> constructor = ConstructorUtils
                    .getMatchingAccessibleConstructor(clazz, Host.class);
            if (null == constructor) {
                log.warn(String.format("No matching constructor for parameter %s", Host.class));
                // Call default constructor for disabled implementations
                return clazz.getDeclaredConstructor().newInstance();
            }
            return constructor.newInstance(bookmark);
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException
                | NoSuchMethodException e) {
            log.error(String.format("Failure loading class %s. %s", clazz, e.getMessage()));
            return new DefaultPasswordStorePrefixService(bookmark);
        }
    }
}
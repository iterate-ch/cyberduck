package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;

import com.nulabinc.zxcvbn.Zxcvbn;

public class PasswordStrengthValidator {

    private final Zxcvbn zxcvbn = new Zxcvbn();

    public Strength getScore(final String password) {
        if(StringUtils.isEmpty(password)) {
            return Strength.veryweak;
        }
        else {
            final int score = zxcvbn.measure(password, Collections.singletonList(
                    PreferencesFactory.get().getProperty("application.name"))).getScore();
            switch(score) {
                case 0:
                    return Strength.veryweak;
                case 1:
                    return Strength.weak;
                case 2:
                    return Strength.fair;
                case 3:
                    return Strength.strong;
                case 4:
                default:
                    return Strength.verystrong;
            }
        }
    }

    public enum Strength {
        veryweak {
            @Override
            public String getDescription() {
                return LocaleFactory.localizedString("Very weak", "Cryptomator");
            }
        },
        weak {
            @Override
            public String getDescription() {
                return LocaleFactory.localizedString("Weak", "Cryptomator");
            }
        },
        fair {
            @Override
            public String getDescription() {
                return LocaleFactory.localizedString("Fair", "Cryptomator");
            }
        },
        strong {
            @Override
            public String getDescription() {
                return LocaleFactory.localizedString("Strong", "Cryptomator");
            }
        },
        verystrong {
            @Override
            public String getDescription() {
                return LocaleFactory.localizedString("Very strong", "Cryptomator");
            }
        };

        public int getScore() {
            return this.ordinal();
        }

        public abstract String getDescription();
    }
}

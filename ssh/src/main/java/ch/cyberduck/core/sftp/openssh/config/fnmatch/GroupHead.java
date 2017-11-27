/*
 * Copyright (C) 2008, Florian Köberle <florianskarten@web.de>
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or
 * without modification, are permitted provided that the following
 * conditions are met:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above
 *   copyright notice, this list of conditions and the following
 *   disclaimer in the documentation and/or other materials provided
 *   with the distribution.
 *
 * - Neither the name of the Git Development Community nor the
 *   names of its contributors may be used to endorse or promote
 *   products derived from this software without specific prior
 *   written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ch.cyberduck.core.sftp.openssh.config.fnmatch;

import ch.cyberduck.core.sftp.openssh.config.errors.InvalidPatternException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class GroupHead extends AbstractHead {
    private final List<CharacterPattern> characterClasses;

    private static final Pattern REGEX_PATTERN = Pattern
            .compile("([^-][-][^-]|\\[[.:=].*?[.:=]\\])");

    private final boolean inverse;

    GroupHead(String pattern, final String wholePattern)
            throws InvalidPatternException {
        super(false);
        this.characterClasses = new ArrayList<CharacterPattern>();
        this.inverse = pattern.startsWith("!");
        if(inverse) {
            pattern = pattern.substring(1);
        }
        final Matcher matcher = REGEX_PATTERN.matcher(pattern);
        while(matcher.find()) {
            final String characterClass = matcher.group(0);
            if(characterClass.length() == 3 && characterClass.charAt(1) == '-') {
                final char start = characterClass.charAt(0);
                final char end = characterClass.charAt(2);
                characterClasses.add(new CharacterRange(start, end));
            }
            else if(characterClass.equals("[:alnum:]")) {
                characterClasses.add(LetterPattern.INSTANCE);
                characterClasses.add(DigitPattern.INSTANCE);
            }
            else if(characterClass.equals("[:alpha:]")) {
                characterClasses.add(LetterPattern.INSTANCE);
            }
            else if(characterClass.equals("[:blank:]")) {
                characterClasses.add(new OneCharacterPattern(' '));
                characterClasses.add(new OneCharacterPattern('\t'));
            }
            else if(characterClass.equals("[:cntrl:]")) {
                characterClasses.add(new CharacterRange('\u0000', '\u001F'));
                characterClasses.add(new OneCharacterPattern('\u007F'));
            }
            else if(characterClass.equals("[:digit:]")) {
                characterClasses.add(DigitPattern.INSTANCE);
            }
            else if(characterClass.equals("[:graph:]")) {
                characterClasses.add(new CharacterRange('\u0021', '\u007E'));
                characterClasses.add(LetterPattern.INSTANCE);
                characterClasses.add(DigitPattern.INSTANCE);
            }
            else if(characterClass.equals("[:lower:]")) {
                characterClasses.add(LowerPattern.INSTANCE);
            }
            else if(characterClass.equals("[:print:]")) {
                characterClasses.add(new CharacterRange('\u0020', '\u007E'));
                characterClasses.add(LetterPattern.INSTANCE);
                characterClasses.add(DigitPattern.INSTANCE);
            }
            else if(characterClass.equals("[:punct:]")) {
                characterClasses.add(PunctPattern.INSTANCE);
            }
            else if(characterClass.equals("[:space:]")) {
                characterClasses.add(WhitespacePattern.INSTANCE);
            }
            else if(characterClass.equals("[:upper:]")) {
                characterClasses.add(UpperPattern.INSTANCE);
            }
            else if(characterClass.equals("[:xdigit:]")) {
                characterClasses.add(new CharacterRange('0', '9'));
                characterClasses.add(new CharacterRange('a', 'f'));
                characterClasses.add(new CharacterRange('A', 'F'));
            }
            else if(characterClass.equals("[:word:]")) {
                characterClasses.add(new OneCharacterPattern('_'));
                characterClasses.add(LetterPattern.INSTANCE);
                characterClasses.add(DigitPattern.INSTANCE);
            }
            else {
                final String message = String.format(
                        "The character class %s is not supported.",
                        characterClass);
                throw new InvalidPatternException(message, wholePattern);
            }

            pattern = matcher.replaceFirst("");
            matcher.reset(pattern);
        }
        // pattern contains now no ranges
        for(int i = 0; i < pattern.length(); i++) {
            final char c = pattern.charAt(i);
            characterClasses.add(new OneCharacterPattern(c));
        }
    }

    @Override
    protected final boolean matches(final char c) {
        for(CharacterPattern pattern : characterClasses) {
            if(pattern.matches(c)) {
                return !inverse;
            }
        }
        return inverse;
    }

    private interface CharacterPattern {
        /**
         * @param c the character to test
         * @return returns true if the character matches a pattern.
         */
        boolean matches(char c);
    }

    private static final class CharacterRange implements CharacterPattern {
        private final char start;

        private final char end;

        CharacterRange(char start, char end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public final boolean matches(char c) {
            return start <= c && c <= end;
        }
    }

    private static final class DigitPattern implements CharacterPattern {
        static final GroupHead.DigitPattern INSTANCE = new DigitPattern();

        @Override
        public final boolean matches(char c) {
            return Character.isDigit(c);
        }
    }

    private static final class LetterPattern implements CharacterPattern {
        static final GroupHead.LetterPattern INSTANCE = new LetterPattern();

        public final boolean matches(char c) {
            return Character.isLetter(c);
        }
    }

    private static final class LowerPattern implements CharacterPattern {
        static final GroupHead.LowerPattern INSTANCE = new LowerPattern();

        public final boolean matches(char c) {
            return Character.isLowerCase(c);
        }
    }

    private static final class UpperPattern implements CharacterPattern {
        static final GroupHead.UpperPattern INSTANCE = new UpperPattern();

        @Override
        public final boolean matches(char c) {
            return Character.isUpperCase(c);
        }
    }

    private static final class WhitespacePattern implements CharacterPattern {
        static final GroupHead.WhitespacePattern INSTANCE = new WhitespacePattern();

        @Override
        public final boolean matches(char c) {
            return Character.isWhitespace(c);
        }
    }

    private static final class OneCharacterPattern implements CharacterPattern {
        private final char expectedCharacter;

        OneCharacterPattern(final char c) {
            this.expectedCharacter = c;
        }

        @Override
        public final boolean matches(char c) {
            return this.expectedCharacter == c;
        }
    }

    private static final class PunctPattern implements CharacterPattern {
        static final GroupHead.PunctPattern INSTANCE = new PunctPattern();

        private static final String punctCharacters = "-!\"#$%&'()*+,./:;<=>?@[\\]_`{|}~";

        @Override
        public boolean matches(char c) {
            return punctCharacters.indexOf(c) != -1;
        }
    }

}

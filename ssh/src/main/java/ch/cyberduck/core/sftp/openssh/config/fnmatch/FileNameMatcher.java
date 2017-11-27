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
import ch.cyberduck.core.sftp.openssh.config.errors.NoClosingBracketException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class can be used to match filenames against fnmatch like patterns. It
 * is not thread save.
 * <p>
 * Supported are the wildcard characters * and ? and groups with:
 * <ul>
 * <li> characters e.g. [abc]</li>
 * <li> ranges e.g. [a-z]</li>
 * <li> the following character classes
 * <ul>
 * <li>[:alnum:]</li>
 * <li>[:alpha:]</li>
 * <li>[:blank:]</li>
 * <li>[:cntrl:]</li>
 * <li>[:digit:]</li>
 * <li>[:graph:]</li>
 * <li>[:lower:]</li>
 * <li>[:print:]</li>
 * <li>[:punct:]</li>
 * <li>[:space:]</li>
 * <li>[:upper:]</li>
 * <li>[:word:]</li>
 * <li>[:xdigit:]</li>
 * </ul>
 * e. g. [[:xdigit:]] </li>
 * </ul>
 * </p>
 */
public class FileNameMatcher {
    static final List<Head> EMPTY_HEAD_LIST = Collections.emptyList();

    private static final Pattern characterClassStartPattern = Pattern
            .compile("\\[[.:=]");

    private final List<Head> headsStartValue;

    private List<Head> heads;

    /**
     * {{@link #extendStringToMatchByOneCharacter(char)} needs a list for the
     * new heads, allocating a new array would be bad for the performance, as
     * the method gets called very often.
     */
    private List<Head> listForLocalUseage;

    /**
     * @param headsStartValue must be a list which will never be modified.
     */
    private FileNameMatcher(final List<Head> headsStartValue) {
        this(headsStartValue, headsStartValue);
    }

    /**
     * @param headsStartValue must be a list which will never be modified.
     * @param heads           a list which will be cloned and then used as current head
     *                        list.
     */
    private FileNameMatcher(final List<Head> headsStartValue,
                            final List<Head> heads) {
        this.headsStartValue = headsStartValue;
        this.heads = new ArrayList<Head>(heads.size());
        this.heads.addAll(heads);
        this.listForLocalUseage = new ArrayList<Head>(heads.size());
    }

    /**
     * @param patternString           must contain a pattern which fnmatch would accept.
     * @param invalidWildgetCharacter if this parameter isn't null then this character will not
     *                                match at wildcards(* and ? are wildcards).
     * @throws InvalidPatternException if the patternString contains a invalid fnmatch pattern.
     */
    public FileNameMatcher(final String patternString,
                           final Character invalidWildgetCharacter)
            throws InvalidPatternException {
        this(createHeadsStartValues(patternString, invalidWildgetCharacter));
    }

    /**
     * A Copy Constructor which creates a new {@link FileNameMatcher} with the
     * same state and reset point like <code>other</code>.
     *
     * @param other another {@link FileNameMatcher} instance.
     */
    public FileNameMatcher(FileNameMatcher other) {
        this(other.headsStartValue, other.heads);
    }

    private static List<Head> createHeadsStartValues(
            final String patternString, final Character invalidWildgetCharacter)
            throws InvalidPatternException {

        final List<AbstractHead> allHeads = parseHeads(patternString,
                invalidWildgetCharacter);

        List<Head> nextHeadsSuggestion = new ArrayList<Head>(2);
        nextHeadsSuggestion.add(LastHead.INSTANCE);
        for(int i = allHeads.size() - 1; i >= 0; i--) {
            final AbstractHead head = allHeads.get(i);

            // explanation:
            // a and * of the pattern "a*b"
            // need *b as newHeads
            // that's why * extends the list for it self and it's left neighbor.
            if(head.isStar()) {
                nextHeadsSuggestion.add(head);
                head.setNewHeads(nextHeadsSuggestion);
            }
            else {
                head.setNewHeads(nextHeadsSuggestion);
                nextHeadsSuggestion = new ArrayList<Head>(2);
                nextHeadsSuggestion.add(head);
            }
        }
        return nextHeadsSuggestion;
    }

    private static int findGroupEnd(final int indexOfStartBracket,
                                    final String pattern) throws InvalidPatternException {
        int firstValidCharClassIndex = indexOfStartBracket + 1;
        int firstValidEndBracketIndex = indexOfStartBracket + 2;

        if(indexOfStartBracket + 1 >= pattern.length()) {
            throw new NoClosingBracketException(indexOfStartBracket, "[", "]",
                    pattern);
        }

        if(pattern.charAt(firstValidCharClassIndex) == '!') {
            firstValidCharClassIndex++;
            firstValidEndBracketIndex++;
        }

        final Matcher charClassStartMatcher = characterClassStartPattern
                .matcher(pattern);

        int groupEnd = -1;
        while(groupEnd == -1) {

            final int possibleGroupEnd = pattern.indexOf(']',
                    firstValidEndBracketIndex);
            if(possibleGroupEnd == -1) {
                throw new NoClosingBracketException(indexOfStartBracket, "[",
                        "]", pattern);
            }

            final boolean foundCharClass = charClassStartMatcher
                    .find(firstValidCharClassIndex);

            if(foundCharClass
                    && charClassStartMatcher.start() < possibleGroupEnd) {

                final String classStart = charClassStartMatcher.group(0);
                final String classEnd = classStart.charAt(1) + "]";

                final int classStartIndex = charClassStartMatcher.start();
                final int classEndIndex = pattern.indexOf(classEnd,
                        classStartIndex + 2);

                if(classEndIndex == -1) {
                    throw new NoClosingBracketException(classStartIndex,
                            classStart, classEnd, pattern);
                }
                firstValidCharClassIndex = classEndIndex + 2;
                firstValidEndBracketIndex = firstValidCharClassIndex;
            }
            else {
                groupEnd = possibleGroupEnd;
            }
        }
        return groupEnd;
    }

    private static List<AbstractHead> parseHeads(final String pattern,
                                                 final Character invalidWildgetCharacter)
            throws InvalidPatternException {

        int currentIndex = 0;
        List<AbstractHead> heads = new ArrayList<AbstractHead>();
        while(currentIndex < pattern.length()) {
            final int groupStart = pattern.indexOf('[', currentIndex);
            if(groupStart == -1) {
                final String patternPart = pattern.substring(currentIndex);
                heads.addAll(createSimpleHeads(patternPart,
                        invalidWildgetCharacter));
                currentIndex = pattern.length();
            }
            else {
                final String patternPart = pattern.substring(currentIndex,
                        groupStart);
                heads.addAll(createSimpleHeads(patternPart,
                        invalidWildgetCharacter));

                final int groupEnd = findGroupEnd(groupStart, pattern);
                final String groupPart = pattern.substring(groupStart + 1,
                        groupEnd);
                heads.add(new GroupHead(groupPart, pattern));
                currentIndex = groupEnd + 1;
            }
        }
        return heads;
    }

    private static List<AbstractHead> createSimpleHeads(
            final String patternPart, final Character invalidWildgetCharacter) {
        final List<AbstractHead> heads = new ArrayList<AbstractHead>(
                patternPart.length());
        for(int i = 0; i < patternPart.length(); i++) {
            final char c = patternPart.charAt(i);
            switch(c) {
                case '*': {
                    final AbstractHead head = createWildCardHead(
                            invalidWildgetCharacter, true);
                    heads.add(head);
                    break;
                }
                case '?': {
                    final AbstractHead head = createWildCardHead(
                            invalidWildgetCharacter, false);
                    heads.add(head);
                    break;
                }
                default:
                    final CharacterHead head = new CharacterHead(c);
                    heads.add(head);
            }
        }
        return heads;
    }

    private static AbstractHead createWildCardHead(
            final Character invalidWildgetCharacter, final boolean star) {
        if(invalidWildgetCharacter != null) {
            return new RestrictedWildCardHead(invalidWildgetCharacter, star);
        }
        else {
            return new WildCardHead(star);
        }
    }

    private void extendStringToMatchByOneCharacter(final char c) {
        final List<Head> newHeads = listForLocalUseage;
        newHeads.clear();
        List<Head> lastAddedHeads = null;
        for(final Head head : heads) {
            final List<Head> headsToAdd = head.getNextHeads(c);
            // Why the next performance optimization isn't wrong:
            // Some times two heads return the very same list.
            // We save future effort if we don't add these heads again.
            // This is the case with the heads "a" and "*" of "a*b" which
            // both can return the list ["*","b"]
            if(!headsToAdd.equals(lastAddedHeads)) {
                newHeads.addAll(headsToAdd);
                lastAddedHeads = headsToAdd;
            }
        }
        listForLocalUseage = heads;
        heads = newHeads;
    }

    /**
     * @param stringToMatch extends the string which is matched against the patterns of
     *                      this class.
     */
    public void append(final String stringToMatch) {
        for(int i = 0; i < stringToMatch.length(); i++) {
            final char c = stringToMatch.charAt(i);
            extendStringToMatchByOneCharacter(c);
        }
    }

    /**
     * Resets this matcher to it's state right after construction.
     */
    public void reset() {
        heads.clear();
        heads.addAll(headsStartValue);
    }

    /**
     * @return a {@link FileNameMatcher} instance which uses the same pattern
     *         like this matcher, but has the current state of this matcher as
     *         reset and start point.
     */
    public FileNameMatcher createMatcherForSuffix() {
        final List<Head> copyOfHeads = new ArrayList<Head>(heads.size());
        copyOfHeads.addAll(heads);
        return new FileNameMatcher(copyOfHeads);
    }

    /**
     * @return true, if the string currently being matched does match.
     */
    public boolean isMatch() {
        final ListIterator<Head> headIterator = heads
                .listIterator(heads.size());
        while(headIterator.hasPrevious()) {
            final Head head = headIterator.previous();
            if(head == LastHead.INSTANCE) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return false, if the string being matched will not match when the string
     *         gets extended.
     */
    public boolean canAppendMatch() {
        for(Head head : heads) {
            if(head != LastHead.INSTANCE) {
                return true;
            }
        }
        return false;
    }
}

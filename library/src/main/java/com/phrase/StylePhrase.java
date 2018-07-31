package com.phrase;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class StylePhrase {
    /**
     * The unmodified original pattern.
     */
    @NonNull
    private final CharSequence pattern;
    /**
     * Cached result after replacing all keys with corresponding values.
     */
    @Nullable
    private CharSequence formatted;
    /**
     * The constructor parses the original pattern into this doubly-linked list
     * of tokens.
     */
    @Nullable
    private Token head;

    /**
     * When parsing, this is the current character.
     */
    private char curChar;

    private int curCharIndex;

    private static final int EOF = 0;

    private Builder mFirstBuilder;
    private Builder mSecondBuilder;
    private Builder mOutBuilder;

    private List<Builder> mBuilders;

    public static StylePhrase from(CharSequence pattern) {
        return new StylePhrase(pattern);
    }

    public static StylePhrase from(Resources resources, @StringRes int patternResourceId) {
        return from(resources.getText(patternResourceId));
    }

    public static StylePhrase from(View view, @StringRes int patternResourceId) {
        return from(view.getResources(), patternResourceId);
    }

    public static StylePhrase from(Fragment f, @StringRes int patternResourceId) {
        return from(f.getResources(), patternResourceId);
    }

    public static StylePhrase from(Context ctx, @StringRes int patternResourceId) {
        return from(ctx.getResources(), patternResourceId);
    }

    public StylePhrase(@NonNull CharSequence pattern) {
        curChar = (pattern.length() > 0) ? pattern.charAt(0) : EOF;
        this.pattern = pattern;

        mBuilders = new ArrayList<>();

        mFirstBuilder = new Builder();
        mFirstBuilder.setSeparator("{}");
        mSecondBuilder = new Builder();
        mSecondBuilder.setSeparator("[]");

        mOutBuilder = new Builder();

        mBuilders.add(mFirstBuilder);
        mBuilders.add(mSecondBuilder);

        formatted = null;
    }

    public StylePhrase setFirstSeparator(String separator) {
        mFirstBuilder.setSeparator(separator);
        return this;
    }

    public StylePhrase setInnerFirstColor(@ColorInt int innerFirstColor) {
        mFirstBuilder.setColor(innerFirstColor);
        return this;
    }

    public StylePhrase setInnerFirstSize(int innerFirstSize) {
        mFirstBuilder.setSize(innerFirstSize);
        return this;
    }

    public StylePhrase setSecondSeparator(String separator) {
        mSecondBuilder.setSeparator(separator);
        return this;
    }

    public StylePhrase setInnerSecondColor(@ColorInt int innerSecondColor) {
        mSecondBuilder.setColor(innerSecondColor);
        return this;
    }

    public StylePhrase setInnerSecondSize(int innerSecondSize) {
        mSecondBuilder.setSize(innerSecondSize);
        return this;
    }

    public StylePhrase setOuterColor(@ColorInt int outerColor) {
        mOutBuilder.setColor(outerColor);
        return this;
    }

    public StylePhrase setOuterSize(int outerSize) {
        mOutBuilder.setSize(outerSize);
        return this;
    }

    public Builder getFirstBuilder() {
        return mFirstBuilder;
    }

    public Builder getSecondBuilder() {
        return mSecondBuilder;
    }

    public Builder getOutBuilder() {
        return mOutBuilder;
    }

    public StylePhrase addBuilder(Builder builder) {
        mBuilders.add(builder);
        return this;
    }

    public List<Builder> getBuilders() {
        if (mBuilders == null) {
            return new ArrayList<>();
        }
        return mBuilders;
    }

    /**
     * cut the pattern with the separators and linked them with double link
     * structure;
     */
    private void createDoubleLinkWithToken() {
        Token prev = null;
        Token next;
        while ((next = token(prev)) != null) {
            // Creates a doubly-linked list of tokens starting with head.
            if (head == null)
                head = next;
            prev = next;
        }
    }

    /**
     * Returns the next token from the input pattern, or null when finished
     * parsing.
     */
    private Token token(Token prev) {
        if (curChar == EOF) {
            return null;
        }
        if (null != mBuilders) {
            for (Builder builder : mBuilders) {
                if (curChar == builder.getLeftSeparator()) {
                    char nextChar = lookahead();
                    if (nextChar == builder.getLeftSeparator()) {
                        return leftSeparator(prev, nextChar);
                    } else {
                        return inner(prev, builder.getRightSeparator(), builder.getCharacterStyles());
                    }
                }
            }
        }
        return outer(prev);
    }

    /**
     * Returns the text after replacing all keys with values.
     *
     * @throws IllegalArgumentException if any keys are not replaced.
     */
    @Nullable
    public CharSequence format() {
        try {
            if (formatted == null) {
                if (!checkPattern()) {
                    throw new IllegalStateException("the separators don't match in the pattern!");
                }
                createDoubleLinkWithToken();
                // Copy the original pattern to preserve all spans, such as bold,
                // italic, etc.
                SpannableStringBuilder sb = new SpannableStringBuilder(pattern);
                for (Token t = head; t != null; t = t.next) {
                    if (t instanceof OuterToken) {
                        continue;
                    }
                    t.expand(sb);
                }

                formatted = sb;
            }
        } catch (Exception e) {
            e.printStackTrace();
            formatted = pattern;
        }
        return formatted;
    }

    /**
     * check if the pattern has legal separators
     *
     * @return
     */
    private boolean checkPattern() {
        if (pattern == null) {
            return false;
        }
        Stack<Character> separatorStack = new Stack<Character>();
        for (int i = 0; i < pattern.length(); i++) {
            char cur = pattern.charAt(i);
            if (cur == mFirstBuilder.getLeftSeparator() || cur == mSecondBuilder.getLeftSeparator()) {
                separatorStack.push(cur);
            } else if (cur == mFirstBuilder.getRightSeparator() || cur == mSecondBuilder.getRightSeparator()) {
                if (!separatorStack.isEmpty()) {
                    char separator = separatorStack.pop();
                    if ((separator == mFirstBuilder.getLeftSeparator() || separator == mSecondBuilder.getLeftSeparator())) {
                        continue;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return separatorStack.isEmpty();
    }

    @NonNull
    private InnerToken inner(Token prev, char separator, List<CharacterStyle> characterStyles) {

        // Store keys as normal Strings; we don't want keys to contain spans.
        StringBuilder sb = new StringBuilder();

        // Consume the left separator.
        consume();
        while (curChar != separator && curChar != EOF) {
            sb.append(curChar);
            consume();
        }

        if (curChar == EOF) {
            throw new IllegalArgumentException("Missing closing separator");
        }
        // consume the right separator.
        consume();

        if (sb.length() == 0) {
            throw new IllegalStateException("Disallow empty content between separators,for example {}");
        }

        return new InnerToken(prev, sb.toString(), characterStyles);
    }

    /**
     * Consumes and returns a token for a sequence of text.
     */
    @NonNull
    private OuterToken outer(Token prev) {
        int startIndex = curCharIndex;

        while (isConsume() && curChar != EOF) {
            consume();
        }
        return new OuterToken(prev, curCharIndex - startIndex, mOutBuilder.getCharacterStyles());
    }

    private boolean isConsume() {
        int size = 0;
        for (Builder builder : mBuilders) {
            if (curChar != builder.getLeftSeparator()) {
                ++size;
            }
        }
        return size >= mBuilders.size();
    }

    /**
     * Consumes and returns a token representing two consecutive curly brackets.
     */
    @NonNull
    private LeftSeparatorToken leftSeparator(Token prev, char leftSeparator) {
        consume();
        consume();
        return new LeftSeparatorToken(prev, leftSeparator);
    }

    /**
     * Returns the next character in the input pattern without advancing.
     */
    private char lookahead() {
        return curCharIndex < pattern.length() - 1 ? pattern.charAt(curCharIndex + 1) : EOF;
    }

    /**
     * Advances the current character position without any error checking.
     * Consuming beyond the end of the string can only happen if this parser
     * contains a bug.
     */
    private void consume() {
        curCharIndex++;
        curChar = (curCharIndex == pattern.length()) ? EOF : pattern.charAt(curCharIndex);
    }

    /**
     * Returns the raw pattern without expanding keys; only useful for
     * debugging. Does not pass through to {@link #format()} because doing so
     * would drop all spans.
     */
    @NonNull
    @Override
    public String toString() {
        return pattern.toString();
    }

    private abstract static class Token {
        @Nullable
        private final Token prev;
        private Token next;

        protected Token(@Nullable Token prev) {
            this.prev = prev;
            if (prev != null)
                prev.next = this;
        }

        /**
         * Replace text in {@code target} with this token's associated value.
         */
        abstract void expand(SpannableStringBuilder target);

        /**
         * Returns the number of characters after expansion.
         */
        abstract int getFormattedLength();

        /**
         * Returns the character index after expansion.
         */
        final int getFormattedStart() {
            if (prev == null) {
                // The first token.
                return 0;
            } else {
                // Recursively ask the predecessor node for the starting index.
                return prev.getFormattedStart() + prev.getFormattedLength();
            }
        }
    }

    /**
     * Ordinary text between tokens.
     */
    private static class OuterToken extends Token {

        private final int textLength;

        private final List<CharacterStyle> mCharacterStyles;
        private final int mFlags;

        private int color;

        OuterToken(Token prev, int textLength, List<CharacterStyle> characterStyles) {
            this(prev, textLength, characterStyles, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        OuterToken(Token prev, int textLength, List<CharacterStyle> characterStyles, int flags) {
            super(prev);
            this.textLength = textLength;
            this.mCharacterStyles = characterStyles;
            this.mFlags = flags;
        }

        @Override
        void expand(@NonNull SpannableStringBuilder target) {

            int startPoint = getFormattedStart();
            int endPoint = startPoint + textLength;

            if (null != mCharacterStyles) {
                for (CharacterStyle characterStyle : mCharacterStyles) {
                    if (characterStyle instanceof ClickableSpan) {
                        target.setSpan(characterStyle, startPoint, endPoint, mFlags);
                    } else {
                        target.setSpan(CharacterStyle.wrap(characterStyle), startPoint, endPoint, mFlags);
                    }
                }
            }

            target.setSpan(new ForegroundColorSpan(color), startPoint, endPoint, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        @Override
        int getFormattedLength() {
            return textLength;
        }
    }

    /**
     * A sequence of two curly brackets.
     */
    private static class LeftSeparatorToken extends Token {
        private char leftSeparator;

        LeftSeparatorToken(Token prev, char _leftSeparator) {
            super(prev);
            leftSeparator = _leftSeparator;
        }

        @Override
        void expand(@NonNull SpannableStringBuilder target) {
            int start = getFormattedStart();
            target.replace(start, start + 2, String.valueOf(leftSeparator));
        }

        @Override
        int getFormattedLength() {
            // for example,,Replace "{{" with "{".
            return 1;
        }
    }

    private static class InnerToken extends Token {
        /**
         * The InnerText without separators,like '{' and '}'.
         */
        private final String mInnerText;
        private final List<CharacterStyle> mCharacterStyles;
        private final int mFlags;

        InnerToken(Token prev, String inner, List<CharacterStyle> characterStyles) {
            this(prev, inner, characterStyles, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        InnerToken(Token prev, String inner, List<CharacterStyle> parcelableSpans, int flags) {
            super(prev);
            this.mInnerText = inner;
            this.mCharacterStyles = parcelableSpans;
            this.mFlags = flags;
        }

        @Override
        void expand(@NonNull SpannableStringBuilder target) {

            int replaceFrom = getFormattedStart();
            // Add 2 to account for the separators.
            int replaceTo = replaceFrom + mInnerText.length() + 2;
            target.replace(replaceFrom, replaceTo, mInnerText);

            if (null != mCharacterStyles) {
                for (CharacterStyle span : mCharacterStyles) {
                    if (span instanceof ClickableSpan) {
                        target.setSpan(span, replaceFrom, replaceTo - 2, mFlags);
                    } else {
                        target.setSpan(CharacterStyle.wrap(span), replaceFrom, replaceTo - 2, mFlags);
                    }
                }
            }
        }

        @Override
        int getFormattedLength() {
            return mInnerText.length();
        }
    }

    public static class Builder {

        private ForegroundColorSpan foregroundColorSpan;
        private AbsoluteSizeSpan absoluteSizeSpan;

        private String separator;
        private char leftSeparator;
        private char rightSeparator;

        private List<CharacterStyle> mCharacterStyles;

        public Builder() {
            mCharacterStyles = new ArrayList<>();
        }


        public Builder setSize(@IntRange(from = 0) int size) {
            absoluteSizeSpan = new AbsoluteSizeSpan(size, true);
            return this;
        }

        public Builder setSize(@IntRange(from = 0) int size, boolean dip) {
            absoluteSizeSpan = new AbsoluteSizeSpan(size, dip);
            return this;
        }

        public Builder setColor(@ColorInt int color) {
            foregroundColorSpan = new ForegroundColorSpan(color);
            return this;
        }

        public Builder setSeparator(String separator) {
            if (TextUtils.isEmpty(separator)) {
                throw new IllegalArgumentException("separator must not be empty!");
            }
            if (separator.length() > 2) {
                throw new IllegalArgumentException("separator‘s length must not be more than 3 charactors!");
            }
            this.separator = separator;
            leftSeparator = separator.charAt(0);
            if (separator.length() == 2) {
                rightSeparator = separator.charAt(1);
            } else {
                rightSeparator = separator.charAt(0);
            }
            return this;
        }

        public Builder addParcelableSpan(CharacterStyle characterStyle) {
            if (null != characterStyle && !mCharacterStyles.contains(characterStyle)) {
                mCharacterStyles.add(characterStyle);
            }
            return this;
        }

        public List<CharacterStyle> getCharacterStyles() {
            if (mCharacterStyles == null) {
                return new ArrayList<>();
            }
            addParcelableSpan(foregroundColorSpan);
            addParcelableSpan(absoluteSizeSpan);
            return mCharacterStyles;
        }

        public String getSeparator() {
            return separator == null ? "" : separator;
        }

        public char getLeftSeparator() {
            return leftSeparator;
        }

        public char getRightSeparator() {
            return rightSeparator;
        }
    }

}

package com.zez.app.util;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * High-performance Markdown and rich-text markup parser for chat messages.
 * Supports:
 * - Headers: # H1, ## H2, ### H3
 * - Bold: **text** and WhatsApp-style *text*
 * - Italic: _text_
 * - Strikethrough: ~text~
 * - Monospace / Code: `code` and ```code```
 * - Bullets: - item or * item
 */
public class MarkdownRenderer {

    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```([\\s\\S]*?)```");
    private static final Pattern INLINE_CODE_PATTERN = Pattern.compile("`([^`\\n]+)`");
    private static final Pattern HEADER3_PATTERN = Pattern.compile("(?m)^###\\s+([^\\n]+)");
    private static final Pattern HEADER2_PATTERN = Pattern.compile("(?m)^##\\s+([^\\n]+)");
    private static final Pattern HEADER1_PATTERN = Pattern.compile("(?m)^#\\s+([^\\n]+)");
    private static final Pattern BOLD_DOUBLE_PATTERN = Pattern.compile("\\*\\*([^*\\n]+?)\\*\\*");
    private static final Pattern BOLD_SINGLE_PATTERN = Pattern.compile("(?<=^|[\\s\\p{Punct}])\\*([^\\s*][^*\\n]*?[^\\s*]|[^\\s*])\\*(?=$|[\\s\\p{Punct}])");
    private static final Pattern ITALIC_PATTERN = Pattern.compile("(?<=^|[\\s\\p{Punct}])_([^\\s_][^_\\n]*?[^\\s_]|[^\\s_])_(?=$|[\\s\\p{Punct}])");
    private static final Pattern STRIKE_PATTERN = Pattern.compile("(?<=^|[\\s\\p{Punct}])~([^\\s~][^~\\n]*?[^\\s~]|[^\\s~])~(?=$|[\\s\\p{Punct}])");
    private static final Pattern BULLET_PATTERN = Pattern.compile("(?m)^[-*]\\s+");

    public static CharSequence render(CharSequence input) {
        if (input == null || input.length() == 0) return "";
        String str = input.toString();

        // Fast-path: if text contains no markup triggers, return untouched
        if (!str.contains("#") && !str.contains("*") && !str.contains("_")
                && !str.contains("~") && !str.contains("`") && !str.contains("-")) {
            return input;
        }

        try {
            SpannableStringBuilder builder = new SpannableStringBuilder(input);

            // 1. Multi-line code blocks ```code```
            applyPatternReplacement(builder, CODE_BLOCK_PATTERN, 1, (ssb, start, end) -> {
                ssb.setSpan(new TypefaceSpan("monospace"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new BackgroundColorSpan(0x28888888), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            });

            // 2. Inline code `code`
            applyPatternReplacement(builder, INLINE_CODE_PATTERN, 1, (ssb, start, end) -> {
                ssb.setSpan(new TypefaceSpan("monospace"), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new BackgroundColorSpan(0x28888888), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            });

            // 3. Headers: ### H3, ## H2, # H1
            applyPatternReplacement(builder, HEADER3_PATTERN, 1, (ssb, start, end) -> {
                ssb.setSpan(new RelativeSizeSpan(1.15f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            });
            applyPatternReplacement(builder, HEADER2_PATTERN, 1, (ssb, start, end) -> {
                ssb.setSpan(new RelativeSizeSpan(1.25f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            });
            applyPatternReplacement(builder, HEADER1_PATTERN, 1, (ssb, start, end) -> {
                ssb.setSpan(new RelativeSizeSpan(1.35f), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            });

            // 4. Bold **text**
            applyPatternReplacement(builder, BOLD_DOUBLE_PATTERN, 1, (ssb, start, end) -> {
                ssb.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            });

            // 5. Bold *text* (WhatsApp style)
            applyPatternReplacement(builder, BOLD_SINGLE_PATTERN, 1, (ssb, start, end) -> {
                ssb.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            });

            // 6. Italic _text_
            applyPatternReplacement(builder, ITALIC_PATTERN, 1, (ssb, start, end) -> {
                ssb.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            });

            // 7. Strikethrough ~text~
            applyPatternReplacement(builder, STRIKE_PATTERN, 1, (ssb, start, end) -> {
                ssb.setSpan(new StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            });

            // 8. Bullets: lines starting with "- " or "* " -> "• "
            replaceBullets(builder);

            return builder;
        } catch (Exception e) {
            // Safe fallback to raw text if parsing encounters any unexpected anomaly
            return input;
        }
    }

    private interface SpanApplier {
        void apply(SpannableStringBuilder ssb, int start, int end);
    }

    private static void applyPatternReplacement(SpannableStringBuilder builder, Pattern pattern, int contentGroup, SpanApplier applier) {
        Matcher matcher = pattern.matcher(builder);
        while (matcher.find()) {
            int matchStart = matcher.start();
            int matchEnd = matcher.end();
            int groupStart = matcher.start(contentGroup);
            int groupEnd = matcher.end(contentGroup);

            String innerContent = builder.subSequence(groupStart, groupEnd).toString();
            builder.replace(matchStart, matchEnd, innerContent);

            int newEnd = matchStart + innerContent.length();
            if (applier != null && newEnd > matchStart) {
                applier.apply(builder, matchStart, newEnd);
            }
            matcher.reset(builder);
        }
    }

    private static void replaceBullets(SpannableStringBuilder builder) {
        Matcher matcher = BULLET_PATTERN.matcher(builder);
        while (matcher.find()) {
            builder.replace(matcher.start(), matcher.end(), "• ");
            matcher.reset(builder);
        }
    }
}

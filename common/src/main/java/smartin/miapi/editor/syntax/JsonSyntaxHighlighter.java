package smartin.miapi.editor.syntax;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import smartin.miapi.Miapi;
import smartin.miapi.modules.properties.util.EditorError;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonSyntaxHighlighter implements EditorInterface {
    private static final ResourceLocation ID = Miapi.id("miapi", "json_syntax");

    // Colors in RGBA format (packed into integers)
    private static final int STRING_COLOR = pack(0.0f, 0.8f, 0.0f, 1.0f);  // Green
    private static final int NUMBER_COLOR = pack(0.4f, 0.4f, 1.0f, 1.0f);  // Blue
    private static final int KEYWORD_COLOR = pack(0.8f, 0.2f, 0.8f, 1.0f); // Purple
    private static final int PROPERTY_COLOR = pack(0.9f, 0.6f, 0.3f, 1.0f); // Orange
    private static final int BRACKET_COLOR = pack(0.7f, 0.7f, 0.7f, 1.0f);  // Gray

    // Regex patterns for JSON elements
    private static final Pattern STRING_PATTERN = Pattern.compile("\"(\\\\.|[^\"])*\"");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?\\b");
    private static final Pattern KEYWORD_PATTERN = Pattern.compile("\\b(true|false|null)\\b");
    private static final Pattern PROPERTY_PATTERN = Pattern.compile("\"(\\\\.|[^\"])*\"(?=\\s*:)");
    private static final Pattern BRACKET_PATTERN = Pattern.compile("[\\[\\]{}]");

    @Override
    public List<EditorError> validateContent(@Nullable JsonElement json, String rawContent) {
        List<EditorError> errors = new ArrayList<>();

        // Check for unmatched brackets
        checkBracketMatching(rawContent, errors);

        // Check for trailing commas
        checkTrailingCommas(rawContent, errors);

        // Check for missing colons in properties
        checkPropertyColons(rawContent, errors);

        return errors;
    }

    @Override
    public Map<TextRange, Integer> getSyntaxHighlighting(String content) {
        Map<TextRange, Integer> highlighting = new HashMap<>();

        // Add highlighting for each pattern
        try {
            addHighlighting(content, STRING_PATTERN, STRING_COLOR, highlighting);
            addHighlighting(content, NUMBER_PATTERN, NUMBER_COLOR, highlighting);
            addHighlighting(content, KEYWORD_PATTERN, KEYWORD_COLOR, highlighting);
            addHighlighting(content, PROPERTY_PATTERN, PROPERTY_COLOR, highlighting);
            addHighlighting(content, BRACKET_PATTERN, BRACKET_COLOR, highlighting);
        } catch (StackOverflowError error) {

        }

        return highlighting;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    private void addHighlighting(String content, Pattern pattern, int color, Map<TextRange, Integer> highlighting) {
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            highlighting.put(new TextRange(matcher.start(), matcher.end()), color);
        }
    }

    private void checkBracketMatching(String content, List<EditorError> errors) {
        Stack<BracketInfo> stack = new Stack<>();
        int line = 1;

        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '\n') {
                line++;
                continue;
            }

            if (c == '{' || c == '[') {
                stack.push(new BracketInfo(c, line));
            } else if (c == '}' || c == ']') {
                if (stack.isEmpty()) {
                    errors.add(new EditorError(line, "Unexpected closing bracket: " + c, EditorError.ErrorSeverity.ERROR));
                    continue;
                }

                BracketInfo opening = stack.pop();
                char expected = (opening.bracket == '{') ? '}' : ']';
                if (c != expected) {
                    errors.add(new EditorError(line,
                            "Mismatched brackets: Expected " + expected + " but found " + c,
                            EditorError.ErrorSeverity.ERROR));
                }
            }
        }

        // Check for unclosed brackets
        while (!stack.isEmpty()) {
            BracketInfo bracket = stack.pop();
            errors.add(new EditorError(bracket.line,
                    "Unclosed bracket: " + bracket.bracket,
                    EditorError.ErrorSeverity.ERROR));
        }
    }

    private void checkTrailingCommas(String content, List<EditorError> errors) {
        Pattern trailingComma = Pattern.compile(",\\s*[}\\]]");
        Matcher matcher = trailingComma.matcher(content);
        int line = 1;
        int lastNewline = 0;

        while (matcher.find()) {
            // Count lines up to the match
            for (int i = lastNewline; i < matcher.start(); i++) {
                if (content.charAt(i) == '\n') line++;
            }
            lastNewline = matcher.start();

            errors.add(new EditorError(line,
                    "Trailing comma before closing bracket",
                    EditorError.ErrorSeverity.ERROR));
        }
    }

    private void checkPropertyColons(String content, List<EditorError> errors) {
        Pattern propertyPattern = Pattern.compile("\"(?:\\\\.|[^\"\\\\])*\"\\s*[^:]");
        try{
            Matcher matcher = propertyPattern.matcher(content);
            int line = 1;
            int lastNewline = 0;

            while (matcher.find()) {
                // Only check if this is actually a property (has a valid context)
                int pos = matcher.start();
                while (pos > 0 && Character.isWhitespace(content.charAt(pos - 1))) pos--;
                if (pos > 0 && content.charAt(pos - 1) == ',') {
                    // Count lines up to the match
                    for (int i = lastNewline; i < matcher.start(); i++) {
                        if (content.charAt(i) == '\n') line++;
                    }
                    lastNewline = matcher.start();

                    errors.add(new EditorError(line,
                            "Missing colon after property name",
                            EditorError.ErrorSeverity.ERROR));
                }
            }
        }catch (StackOverflowError e){
            Miapi.LOGGER.error("stack overflow in collon finder");
        }
    }

    private static int pack(float r, float g, float b, float a) {
        return ((int) (r * 255) << 24) |
               ((int) (g * 255) << 16) |
               ((int) (b * 255) << 8) |
               ((int) (a * 255));
    }

    private record BracketInfo(char bracket, int line) {
    }
} 
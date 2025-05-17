package com.alibaba.langengine.jsonrepair;


import com.alibaba.langengine.jsonrepair.JsonContext.ContextValues;

import java.util.*;
import java.util.logging.Logger;

/**
 * JsonParser - A parser for repairing malformed JSON strings
 * <p>
 * This class implements a parser that can handle and repair various
 * malformations in JSON strings, including:
 * <ul>
 *   <li>Missing quotes around keys</li>
 *   <li>Missing commas between elements</li>
 *   <li>Trailing commas in arrays and objects</li>
 *   <li>Single quotes instead of double quotes</li>
 *   <li>Comments (both // and /* style)</li>
 *   <li>Unquoted string literals</li>
 * </ul>
 */
public class JsonParser {

    // Constants
    private static final List<Character> STRING_DELIMITERS = Arrays.asList('"', '\'');

    // Logger for debugging
    private static final Logger logger = Logger.getLogger(JsonParser.class.getName());

    // The string to parse
    private final String jsonStr;

    // Index is our iterator that will keep track of which character we are looking at
    private int index;

    // Context for parsing
    private final JsonContext context;

    // Flag for stream stability
    private boolean streamStable;

    /**
     * Creates a new JsonParser
     *
     * @param jsonStr The JSON string to parse
     */
    public JsonParser(String jsonStr) {
        this(jsonStr, false);
    }

    /**
     * Creates a new JsonParser
     *
     * @param jsonStr      The JSON string to parse
     * @param streamStable Whether the parser should handle streaming JSON
     */
    public JsonParser(String jsonStr, boolean streamStable) {
        this.jsonStr = jsonStr;
        this.index = 0;
        this.context = new JsonContext();
        this.streamStable = streamStable;
    }

    /**
     * Parses the JSON string
     *
     * @return The parsed object
     */
    public Object parse() {
        try {
            Object json = parseJson();

            if (index < jsonStr.length()) {
                // The parser returned early, checking if there's more JSON elements
                List<Object> jsonArray = new ArrayList<>();
                jsonArray.add(json);

                while (index < jsonStr.length()) {
                    Object j = parseJson();
                    if (j != null && !"".equals(j)) {
                        jsonArray.add(j);
                    }
                }

                // If nothing extra was found, don't return an array
                if (jsonArray.size() == 1) {
                    return jsonArray.get(0);
                }

                return jsonArray;
            }

            return json;
        } catch (Exception e) {
            // Log the error and return the best we could parse
            logger.warning("Error parsing JSON: " + e.getMessage());
            // If we can't parse anything, return an empty object
            if (index == 0) {
                return new LinkedHashMap<>();
            }
            // Otherwise return what we've parsed so far
            return jsonStr.substring(0, index);
        }
    }

    /**
     * Parses a JSON value
     *
     * @return The parsed JSON value
     */
    private Object parseJson() {
        while (true) {
            Character ch = getCharAt();

            // End of string
            if (ch == null) {
                return "";
            }

            // Object starts with '{'
            if (ch == '{') {
                index++;
                return parseObject();
            }

            // Array starts with '['
            if (ch == '[') {
                index++;
                return parseArray();
            }

            // Handle case where a key is empty at the end of an object
            if (context.getCurrent() == JsonContext.ContextValues.OBJECT_VALUE && ch == '}') {
                return "";
            }

            // String starts with a quote or letter
            if (!context.isEmpty() && (STRING_DELIMITERS.contains(ch) || Character.isLetter(ch))) {
                return parseString();
            }

            // Number starts with digit or minus
            if (!context.isEmpty() && (Character.isDigit(ch) || ch == '-' || ch == '.')) {
                return parseNumber();
            }

            // Comment starts with # or /
            if (ch == '#' || ch == '/') {
                parseComment();
                continue;
            }

            // Skip other characters
            index++;
        }
    }

    /**
     * Parses a JSON object
     *
     * @return The parsed JSON object
     */
    private Map<String, Object> parseObject() {
        Map<String, Object> obj = new LinkedHashMap<>();

        // Continue until we find the closing brace or reach the end
        while (index < jsonStr.length() && getCharAt() != '}') {
            // Skip whitespace
            skipWhitespacesAt();

            // Handle case where we find a colon before a key
            if (getCharAt() == ':') {
                index++;
            }

            // Set context for object key
            context.set(ContextValues.OBJECT_KEY);

            // Save index in case we need to handle duplicate keys
            int rollbackIndex = index;

            // Parse the key
            Object keyObj = parseString();
            String key;

            // Handle non-string keys (convert to string)
            if (keyObj == null) {
                key = "null";
            } else {
                key = String.valueOf(keyObj);
            }

            // Skip whitespace
            skipWhitespacesAt();

            // Handle end of object
            if (getCharAt() == '}') {
                continue;
            }

            // Skip whitespace
            skipWhitespacesAt();

            // Handle missing colon after key
            if (getCharAt() != ':') {
                // Missing colon, but we'll continue anyway
                // Try to find a colon within a reasonable distance
                int colonPos = findNextChar(':', 5);
                if (colonPos > 0) {
                    index = colonPos + 1;
                }
            } else {
                index++;
            }

            // Reset context and set for object value
            context.reset();
            context.set(ContextValues.OBJECT_VALUE);

            // Parse the value
            Object value = parseJson();

            // Reset context
            context.reset();

            // Add key-value pair to object
            obj.put(key, value);

            // Skip comma or quotes
            Character nextChar = getCharAt();
            if (nextChar != null && (nextChar == ',' || STRING_DELIMITERS.contains(nextChar))) {
                index++;
            }

            // Skip trailing whitespace
            skipWhitespacesAt();
        }

        // Skip closing brace
        index++;

        return obj;
    }

    /**
     * Parses a JSON array
     *
     * @return The parsed JSON array
     */
    private List<Object> parseArray() {
        List<Object> arr = new ArrayList<>();
        context.set(ContextValues.ARRAY);

        // Continue until we find the closing bracket or reach the end
        Character ch = getCharAt();
        while (ch != null && ch != ']' && ch != '}') {
            skipWhitespacesAt();
            Object value = parseJson();

            // Handle empty values
            if ("".equals(value)) {
                index++;
            } else if ("...".equals(value) && index > 0 && getCharAtOffset(-1) == '.') {
                // Ignore "..." in arrays
            } else {
                arr.add(value);
            }

            // Skip comma
            Character commaChar = getCharAt();
            if (commaChar != null && commaChar == ',') {
                index++;
            }

            skipWhitespacesAt();
            ch = getCharAt();
        }

        // Skip closing bracket
        if (ch == ']') {
            index++;
        }

        context.reset();
        return arr;
    }

    /**
     * Parses a JSON string
     *
     * @return The parsed string
     */
    private Object parseString() {
        // Flag to manage corner cases
        boolean missingQuotes = false;
        boolean doubledQuotes = false;
        char lStringDelimiter = '"';
        char rStringDelimiter = '"';

        Character ch = getCharAt();

        // Handle comments
        if (ch != null && (ch == '#' || ch == '/')) {
            parseComment();
            return "";
        }

        // Skip non-alphanumeric characters until we find a quote or letter
        while (ch != null && !STRING_DELIMITERS.contains(ch) && !Character.isLetterOrDigit(ch)) {
            index++;
            ch = getCharAt();
        }

        // Handle empty string
        if (ch == null) {
            return "";
        }

        // Set the correct string delimiter
        if (ch == '\'') {
            lStringDelimiter = '\'';
            rStringDelimiter = '\'';
        } else if (Character.isLetterOrDigit(ch)) {
            // This could be a boolean or null
            if ((ch == 't' || ch == 'f' || ch == 'n') &&
                    context.getCurrent() != ContextValues.OBJECT_KEY) {
                Object value = parseBooleanOrNull();
                if (value != null && !"".equals(value)) {
                    return value;
                }
            }

            // Missing quotes for a literal
            missingQuotes = true;
        }

        // Skip opening quote if present
        if (!missingQuotes) {
            index++;
        }

        // Handle doubled quotes
        Character currentChar = getCharAt();
        if (currentChar != null && currentChar == lStringDelimiter) {
            // Empty key
            Character nextChar = getCharAtOffset(1);
            if (context.getCurrent() == ContextValues.OBJECT_KEY && nextChar != null && nextChar == ':') {
                index++;
                return "";
            }

            // Doubled quotes
            Character doubleQuoteChar = getCharAtOffset(1);
            if (doubleQuoteChar != null && doubleQuoteChar == lStringDelimiter) {
                doubledQuotes = true;
                index++;
            } else {
                // Check if this is an empty string
                int i = skipWhitespacesAt(1, false);
                Character nextCh = getCharAtOffset(i);

                if (nextCh != null && (STRING_DELIMITERS.contains(nextCh) || nextCh == '{' || nextCh == '[')) {
                    index++;
                    return "";
                } else if (nextCh != null && nextCh != ',' && nextCh != ']' && nextCh != '}') {
                    index++;
                }
            }
        }

        // Initialize return value
        StringBuilder stringAcc = new StringBuilder();

        // Parse the string content
        ch = getCharAt();
        while (ch != null && ch != rStringDelimiter) {
            // Handle missing quotes in object key
            if (missingQuotes && context.getCurrent() != null && context.getCurrent() == JsonContext.ContextValues.OBJECT_KEY &&
                    ch != null && (ch == ':' || Character.isWhitespace(ch))) {
                break;
            }

            // Handle missing quotes in object value
            if (missingQuotes && context.getCurrent() != null && context.getCurrent() == JsonContext.ContextValues.OBJECT_VALUE &&
                    ch != null && (ch == ',' || ch == '}')) {
                break;
            }

            // Handle array end
            if ((missingQuotes || !streamStable) && ch != null && ch == ']' &&
                    context != null && context.contains(JsonContext.ContextValues.ARRAY)) {
                int i = skipToCharacter(rStringDelimiter);
                if (getCharAtOffset(i) == null) {
                    break;
                }
            }

            stringAcc.append(ch);
            index++;
            ch = getCharAt();

            // Handle escape sequences
            if (ch != null && stringAcc.length() > 0 && stringAcc.charAt(stringAcc.length() - 1) == '\\') {
                if (ch == rStringDelimiter || ch == 't' || ch == 'n' || ch == 'r' || ch == 'b' || ch == '\\' || ch == 'u') {
                    stringAcc.setLength(stringAcc.length() - 1);

                    if (ch == 't') stringAcc.append('\t');
                    else if (ch == 'n') stringAcc.append('\n');
                    else if (ch == 'r') stringAcc.append('\r');
                    else if (ch == 'b') stringAcc.append('\b');
                    else if (ch == 'u') {
                        // Handle Unicode escape sequences XXXX
                        try {
                            // Read the next 4 characters as a hex number
                            String hexCode = "";
                            for (int i = 0; i < 4; i++) {
                                index++;
                                Character hexChar = getCharAt();
                                if (hexChar == null) {
                                    break;
                                }
                                hexCode += hexChar;
                            }

                            // Convert hex to character and append
                            if (hexCode.length() == 4) {
                                try {
                                    int codePoint = Integer.parseInt(hexCode, 16);
                                    stringAcc.append((char) codePoint);
                                } catch (NumberFormatException e) {
                                    // If parsing fails, just append the original sequence
                                    stringAcc.append("\\u").append(hexCode);
                                }
                            } else {
                                // Incomplete Unicode escape, append as is
                                stringAcc.append("\\u").append(hexCode);
                            }
                        } catch (Exception e) {
                            // If any error occurs, just append 'u'
                            stringAcc.append('u');
                        }
                    }
                    else stringAcc.append(ch);

                    index++;
                    ch = getCharAt();
                }
            }

            // Handle colon in object key context
            if (ch != null && ch == ':' && !missingQuotes &&
                    context.getCurrent() != null && context.getCurrent() == ContextValues.OBJECT_KEY) {
                // Check if this is followed by a value
                int i = skipToCharacter(lStringDelimiter, 1);
                Character nextCh = getCharAtOffset(i);

                if (nextCh != null) {
                    i++;
                    i = skipToCharacter(rStringDelimiter, i);
                    nextCh = getCharAtOffset(i);

                    if (nextCh != null) {
                        i++;
                        i = skipWhitespacesAt(i, false);
                        nextCh = getCharAtOffset(i);

                        if (nextCh != null && (nextCh == ',' || nextCh == '}')) {
                            break;
                        }
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
        }

        // Handle missing closing quote
        if (ch != rStringDelimiter) {
            if (!streamStable) {
                // Trim trailing whitespace for missing quotes
                while (stringAcc.length() > 0 && Character.isWhitespace(stringAcc.charAt(stringAcc.length() - 1))) {
                    stringAcc.setLength(stringAcc.length() - 1);
                }
            }
        } else {
            index++;
        }

        // Clean whitespace for corner cases
        if (!streamStable && (missingQuotes ||
                (stringAcc.length() > 0 && stringAcc.charAt(stringAcc.length() - 1) == '\n'))) {
            // Trim trailing whitespace
            while (stringAcc.length() > 0 && Character.isWhitespace(stringAcc.charAt(stringAcc.length() - 1))) {
                stringAcc.setLength(stringAcc.length() - 1);
            }
        }

        return stringAcc.toString();
    }

    /**
     * Parses a JSON number
     *
     * @return The parsed number
     */
    private Object parseNumber() {
        StringBuilder numberStr = new StringBuilder();
        Character ch = getCharAt();
        boolean isArray = context.getCurrent() == ContextValues.ARRAY;
        String numberChars = "0123456789-.eE+/,";

        while (ch != null && numberChars.indexOf(ch) >= 0 && (!isArray || ch != ',')) {
            numberStr.append(ch);
            index++;
            ch = getCharAt();
        }

        // Handle invalid number endings
        if (numberStr.length() > 0 && "-eE+/,".indexOf(numberStr.charAt(numberStr.length() - 1)) >= 0) {
            numberStr.setLength(numberStr.length() - 1);
            index--;
        } else if (ch != null && Character.isLetter(ch)) {
            // This was a string instead
            index -= numberStr.length();
            return parseString();
        }

        try {
            String numStr = numberStr.toString();

            // Handle comma as decimal separator
            if (numStr.contains(",")) {
                numStr = numStr.replace(",", ".");
            }

            // Handle multiple decimal points (keep only the first one)
            int firstDecimal = numStr.indexOf('.');
            if (firstDecimal >= 0) {
                int secondDecimal = numStr.indexOf('.', firstDecimal + 1);
                if (secondDecimal >= 0) {
                    numStr = numStr.substring(0, secondDecimal) +
                            numStr.substring(secondDecimal + 1);
                }
            }

            // Try parsing as integer first
            if (!numStr.contains(".") && !numStr.contains("e") && !numStr.contains("E")) {
                try {
                    return Integer.parseInt(numStr);
                } catch (NumberFormatException e) {
                    try {
                        return Long.parseLong(numStr);
                    } catch (NumberFormatException e2) {
                        // Fall through to double parsing
                    }
                }
            }

            // Parse as double
            return Double.parseDouble(numStr);
        } catch (NumberFormatException e) {
            // If parsing fails, return the string
            return numberStr.toString();
        }
    }

    /**
     * Parses a boolean or null value
     *
     * @return The parsed boolean or null
     */
    private Object parseBooleanOrNull() {
        int startingIndex = index;
        Character ch = getCharAt();

        if (ch == null) {
            return "";
        }

        String valueStr;
        Object value;

        if (ch == 't' || ch == 'T') {
            valueStr = "true";
            value = Boolean.TRUE;
        } else if (ch == 'f' || ch == 'F') {
            valueStr = "false";
            value = Boolean.FALSE;
        } else if (ch == 'n' || ch == 'N') {
            valueStr = "null";
            value = null;
        } else {
            return "";
        }

        // Check if the characters match the expected value
        int i = 0;
        while (ch != null && i < valueStr.length() &&
                Character.toLowerCase(ch) == valueStr.charAt(i)) {
            i++;
            index++;
            ch = getCharAt();
        }

        if (i == valueStr.length()) {
            return value;
        }

        // Reset index if not a match
        index = startingIndex;
        return "";
    }

    /**
     * Parses and skips comments
     */
    private void parseComment() {
        Character ch = getCharAt();

        if (ch == null) {
            return;
        }

        List<Character> terminationChars = new ArrayList<>(Arrays.asList('\n', '\r'));

        if (context.contains(ContextValues.ARRAY)) {
            terminationChars.add(']');
        }

        if (context.getCurrent() == ContextValues.OBJECT_VALUE) {
            terminationChars.add('}');
        }

        if (context.getCurrent() == ContextValues.OBJECT_KEY) {
            terminationChars.add(':');
        }

        // Line comment starting with #
        if (ch == '#') {
            while (ch != null && !terminationChars.contains(ch)) {
                index++;
                ch = getCharAt();
            }
            return;
        }

        // Comments starting with /
        if (ch == '/') {
            Character nextCh = getCharAtOffset(1);

            // Line comment //
            if (nextCh != null && nextCh == '/') {
                index += 2; // Skip both slashes
                ch = getCharAt();

                while (ch != null && !terminationChars.contains(ch)) {
                    index++;
                    ch = getCharAt();
                }
                return;
            }

            // Block comment /* */
            if (nextCh != null && nextCh == '*') {
                index += 2; // Skip /*

                while (true) {
                    ch = getCharAt();

                    if (ch == null) {
                        // Unclosed block comment
                        break;
                    }

                    index++;

                    if (ch == '*' && getCharAt() == '/') {
                        index++; // Skip the closing /
                        break;
                    }
                }
            }
        }
    }

    /**
     * Gets the character at the current index
     *
     * @return The character at the current index, or null if out of bounds
     */
    private Character getCharAt() {
        return getCharAtOffset(0);
    }

    /**
     * Gets the character at the current index plus an offset
     *
     * @param offset The offset from the current index
     * @return The character at the offset, or null if out of bounds
     */
    private Character getCharAt(int offset) {
        return getCharAtOffset(offset);
    }

    /**
     * Gets the character at the current index plus an offset
     *
     * @param offset The offset from the current index
     * @return The character at the offset, or null if out of bounds
     */
    private Character getCharAtOffset(int offset) {
        int targetIndex = index + offset;
        if (targetIndex < 0 || targetIndex >= jsonStr.length()) {
            return null;
        }
        try {
            return jsonStr.charAt(targetIndex);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    /**
     * Skips whitespace characters starting at the current index
     */
    private void skipWhitespacesAt() {
        skipWhitespacesAt(0, true);
    }

    /**
     * Skips whitespace characters starting at the current index plus an offset
     *
     * @param offset        The offset from the current index
     * @param moveMainIndex Whether to move the main index
     * @return The number of characters skipped
     */
    private int skipWhitespacesAt(int offset, boolean moveMainIndex) {
        int idx = offset;
        Character ch = getCharAtOffset(idx);

        while (ch != null && Character.isWhitespace(ch)) {
            if (moveMainIndex) {
                index++;
            } else {
                idx++;
            }
            ch = getCharAtOffset(idx);
        }

        return idx;
    }

    /**
     * Skips to a specific character
     *
     * @param character The character to skip to
     * @return The offset to the character, or the end of the string
     */
    private int skipToCharacter(char character) {
        return skipToCharacter(character, 0);
    }

    /**
     * Skips to a specific character starting at an offset
     *
     * @param character The character to skip to
     * @param offset    The offset to start from
     * @return The offset to the character, or the end of the string
     */
    private int skipToCharacter(char character, int offset) {
        int idx = offset;
        Character ch = getCharAtOffset(idx);

        while (ch != null && ch != character) {
            idx++;
            ch = getCharAtOffset(idx);
        }

        return idx;
    }

    /**
     * Finds the next occurrence of a character within a maximum distance
     *
     * @param character The character to find
     * @param maxDistance The maximum distance to search
     * @return The position of the character, or -1 if not found within the distance
     */
    private int findNextChar(char character, int maxDistance) {
        int currentPos = index;
        int endPos = Math.min(currentPos + maxDistance, jsonStr.length() - 1);

        for (int i = currentPos; i <= endPos; i++) {
            try {
                if (jsonStr.charAt(i) == character) {
                    return i;
                }
            } catch (IndexOutOfBoundsException e) {
                return -1;
            }
        }

        return -1;
    }
}

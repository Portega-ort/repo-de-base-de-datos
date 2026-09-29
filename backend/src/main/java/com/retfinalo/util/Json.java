package com.retfinalo.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Parser/serializador JSON mínimo (objetos, listas, cadenas, números, booleanos y null). */
public final class Json {
    private Json() {
    }

    public static Object parse(String json) {
        return new Parser(json).parseValue();
    }

    public static String stringify(Object value) {
        StringBuilder out = new StringBuilder();
        write(value, out);
        return out.toString();
    }

    private static void write(Object value, StringBuilder out) {
        if (value == null) {
            out.append("null");
        } else if (value instanceof String s) {
            out.append('"');
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                switch (c) {
                    case '"' -> out.append("\\\"");
                    case '\\' -> out.append("\\\\");
                    case '\b' -> out.append("\\b");
                    case '\f' -> out.append("\\f");
                    case '\n' -> out.append("\\n");
                    case '\r' -> out.append("\\r");
                    case '\t' -> out.append("\\t");
                    default -> {
                        if (c < 0x20) {
                            out.append(String.format("\\u%04x", (int) c));
                        } else {
                            out.append(c);
                        }
                    }
                }
            }
            out.append('"');
        } else if (value instanceof Map<?, ?> m) {
            out.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : m.entrySet()) {
                if (!first) out.append(',');
                first = false;
                write(String.valueOf(e.getKey()), out);
                out.append(':');
                write(e.getValue(), out);
            }
            out.append('}');
        } else if (value instanceof List<?> l) {
            out.append('[');
            boolean first = true;
            for (Object o : l) {
                if (!first) out.append(',');
                first = false;
                write(o, out);
            }
            out.append(']');
        } else if (value instanceof Number || value instanceof Boolean) {
            out.append(value);
        } else {
            throw new IllegalArgumentException("Tipo no serializable: " + value.getClass().getName());
        }
    }

    private static final class Parser {
        private final String text;
        private int pos;

        private Parser(String text) {
            this.text = text;
        }

        private Object parseValue() {
            skipWhitespace();
            if (pos >= text.length()) throw error("JSON vacío");
            return switch (text.charAt(pos)) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't' -> parseLiteral("true", Boolean.TRUE);
                case 'f' -> parseLiteral("false", Boolean.FALSE);
                case 'n' -> parseLiteral("null", null);
                default -> parseNumber();
            };
        }

        private Map<String, Object> parseObject() {
            expect('{');
            Map<String, Object> map = new LinkedHashMap<>();
            skipWhitespace();
            if (peek() == '}') {
                pos++;
                return map;
            }
            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                map.put(key, parseValue());
                skipWhitespace();
                char c = peek();
                if (c == ',') {
                    pos++;
                } else if (c == '}') {
                    pos++;
                    return map;
                } else {
                    throw error("Se esperaba ',' o '}'");
                }
            }
        }

        private List<Object> parseArray() {
            expect('[');
            List<Object> list = new ArrayList<>();
            skipWhitespace();
            if (peek() == ']') {
                pos++;
                return list;
            }
            while (true) {
                list.add(parseValue());
                skipWhitespace();
                char c = peek();
                if (c == ',') {
                    pos++;
                } else if (c == ']') {
                    pos++;
                    return list;
                } else {
                    throw error("Se esperaba ',' o ']'");
                }
            }
        }

        private String parseString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (pos < text.length()) {
                char c = text.charAt(pos++);
                if (c == '"') return sb.toString();
                if (c == '\\') {
                    if (pos >= text.length()) throw error("Escape incompleto");
                    char e = text.charAt(pos++);
                    switch (e) {
                        case '"' -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case '/' -> sb.append('/');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'u' -> {
                            if (pos + 4 > text.length()) throw error("Unicode incompleto");
                            sb.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16));
                            pos += 4;
                        }
                        default -> throw error("Escape desconocido: \\" + e);
                    }
                } else {
                    sb.append(c);
                }
            }
            throw error("Cadena sin cerrar");
        }

        private Object parseNumber() {
            int start = pos;
            while (pos < text.length() && "+-0123456789.eE".indexOf(text.charAt(pos)) >= 0) pos++;
            String num = text.substring(start, pos);
            try {
                if (num.contains(".") || num.contains("e") || num.contains("E")) {
                    return Double.parseDouble(num);
                }
                return Long.valueOf(Long.parseLong(num));
            } catch (NumberFormatException e) {
                throw error("Número inválido: " + num);
            }
        }

        private Object parseLiteral(String literal, Object value) {
            if (!text.startsWith(literal, pos)) throw error("Literal esperado: " + literal);
            pos += literal.length();
            return value;
        }

        private void skipWhitespace() {
            while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) pos++;
        }

        private char peek() {
            if (pos >= text.length()) throw error("JSON incompleto");
            return text.charAt(pos);
        }

        private void expect(char c) {
            if (pos >= text.length() || text.charAt(pos) != c) throw error("Se esperaba '" + c + "'");
            pos++;
        }

        private IllegalArgumentException error(String message) {
            return new IllegalArgumentException(message + " (posición " + pos + ")");
        }
    }
}
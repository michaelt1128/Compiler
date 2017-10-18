import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

class FileInterpreter {
    private static ArrayList<Token> tokens = new ArrayList<>();
    private static int commentDepth = 0;
    private static int blockDepth = 0;

    FileInterpreter() {

    }

    private static void readLine(String x) {
//        System.out.println("\nINPUT: " + x);
        readString(x);
    }

    private static void readString(String x) {
        if (x.length() > 0) {
            Type type;
            String text;
            if (commentDepth > 0) {
                if (x.startsWith("/*")) {
                    type = Type.COMMENT;
                    text = "/*";
                    commentDepth++;
                } else if (x.startsWith("*/")) {
                    type = Type.COMMENT;
                    text = "*/";
                    commentDepth--;
                } else if (x.startsWith("//")) {
                    type = Type.COMMENT;
                    text = "//";
                } else {
                    type = Type.COMMENT;
                    text = x.substring(0, 1);
                }
            } else {
                String placeholder = x.split(
                        "(\\s" +
                                "|(?=([{}()\\[\\],;]))" +
                                "|((?=(\\d|\\w))(?<=[/*<=>!+]))" +
                                "|((?=[/*<=>!+])(?<=(\\d|\\w))))"
                )[0];
                if (placeholder.matches("\\d+")) {
                    type = Type.NUM;
                    text = placeholder;
                } else if (placeholder.matches("(\\d+)(\\.\\d+)?(E[+-]?\\d+)?")) {
                    type = Type.FLOAT;
                    text = placeholder;
                } else {
                    // include -
                    placeholder = x.split(
                            "(\\s" +
                                    "|(?=([{}()\\[\\],;]))" +
                                    "|((?=(\\d|\\w))(?<=[/*<=>!+-]))" +
                                    "|((?=[/*<=>!+-])(?<=(\\d|\\w))))"
                    )[0];
                    if (x.startsWith("/*")) {
                        type = Type.COMMENT;
                        text = "/*";
                        commentDepth++;
                    } else if (x.startsWith("//")) {
                        type = Type.COMMENT;
                        text = x;
                    } else if (placeholder.equals("==")) {
                        type = Type.EQUAL_TO;
                        text = placeholder;
                    } else if (placeholder.equals("!=")) {
                        type = Type.NOT_EQUAL;
                        text = placeholder;
                    } else if (placeholder.equals("<=")) {
                        type = Type.EQUAL_LESS;
                        text = placeholder;
                    } else if (placeholder.equals(">=")) {
                        type = Type.EQUAL_GREATER;
                        text = placeholder;
                    } else if (placeholder.equals("=")) {
                        type = Type.SET_EQUAL;
                        text = placeholder;
                    } else if (placeholder.equals("+")) {
                        type = Type.PLUS;
                        text = placeholder;
                    } else if (placeholder.equals("-")) {
                        type = Type.MINUS;
                        text = placeholder;
                    } else if (placeholder.equals("*")) {
                        type = Type.MULTIPLY;
                        text = placeholder;
                    } else if (placeholder.equals("/")) {
                        type = Type.DIVIDE;
                        text = placeholder;
                    } else if (placeholder.equals("<")) {
                        type = Type.LESS_THAN;
                        text = placeholder;
                    } else if (placeholder.equals(">")) {
                        type = Type.GREATER_THAN;
                        text = placeholder;
                    } else if (x.startsWith(";")) {
                        type = Type.SEMICOLON;
                        text = ";";
                    } else if (x.startsWith(",")) {
                        type = Type.COMMA;
                        text = ",";
                    } else if (x.startsWith("(")) {
                        type = Type.LEFT_PAREN;
                        text = "(";
                    } else if (x.startsWith(")")) {
                        type = Type.RIGHT_PAREN;
                        text = placeholder;
                    } else if (x.startsWith("[")) {
                        type = Type.LEFT_BRACKET;
                        text = placeholder;
                    } else if (x.startsWith("]")) {
                        type = Type.RIGHT_BRACKET;
                        text = placeholder;
                    } else if (x.startsWith("{")) {
                        type = Type.LEFT_BRACE;
                        text = placeholder;
                        blockDepth++;
                    } else if (x.startsWith("}")) {
                        type = Type.RIGHT_BRACE;
                        text = placeholder;
                        blockDepth--;
                    } else if (placeholder.equals("int")) {
                        type = Type.INT;
                        text = placeholder;
                    } else if (placeholder.equals("void")) {
                        type = Type.VOID;
                        text = placeholder;
                    } else if (placeholder.equals("if")) {
                        type = Type.IF;
                        text = placeholder;
                    } else if (placeholder.equals("while")) {
                        type = Type.WHILE;
                        text = placeholder;
                    } else if (placeholder.equals("return")) {
                        type = Type.RETURN;
                        text = placeholder;
                    } else if (placeholder.equals("else")) {
                        type = Type.ELSE;
                        text = placeholder;
                    } else if (placeholder.matches("[a-zA-Z]+")) {
                        type = Type.ID;
                        text = placeholder;
                    } else if (placeholder.matches("(\\d+)(\\.\\d+)?(E[+-]?\\d+)?")) {
                        type = Type.NUM;
                        text = placeholder;
                    } else if (x.startsWith("$")) {
                        type = Type.DOLLAR;
                        text = x;
                    } else {
                        type = Type.ERROR;
                        text = placeholder;
                    }
                }
            }
            Token t = new Token(text, type);
//            t.print(blockDepth);
            tokens.add(t);
            if (text.length() > 0) {
                readString(x.substring(text.length()).trim());
            }
        }
    }

    public ArrayList<Token> readFile(File file) {
        try {
            Files.lines(file.toPath()).map(s -> s.trim()).forEach(s -> readLine(s));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tokens;
    }
}

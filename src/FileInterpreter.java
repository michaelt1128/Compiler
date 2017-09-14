import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

public class FileInterpreter {
    private static ArrayList<Token> tokens = new ArrayList<Token>();
    private static int commentDepth = 0;
    private static int blockDepth = 0;

    public FileInterpreter() {

    }

    private static void readLine(String x) {
        System.out.println("\nINPUT: " + x);
        readString(x);
    }

    private static void readString(String x) {
        if (x.length() > 0) {
            Type type = Type.ERROR;
            String text = x;
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
                    text = x;
                } else {
                    type = Type.COMMENT;
                    text = x.substring(0,1);
                }
            } else {
                String placeholder = x.split(
                        "(\\s" +
                        "|(?=([{}()\\[\\],;]))" +
                        "|((?=(\\d|\\w))(?<=[/*<=>!+-]))" +
                        "|((?=[/*<=>!+-])(?<=(\\d|\\w))))"
                )[0];
                if (x.startsWith("/*")) {
                    type = Type.COMMENT;
                    text = "/*";
                    commentDepth++;
                } else if (x.startsWith("*/")) {
                    type = Type.ERROR;
                    text = x;
                } else if (x.startsWith("//")) {
                    type = Type.COMMENT;
                    text = x;
                } else if (placeholder.equals("==")) {
                    type = Type.SYMBOL;
                    text = placeholder;
                } else if (placeholder.equals("!=")) {
                    type = Type.SYMBOL;
                    text = placeholder;
                } else if (placeholder.equals("<=")) {
                    type = Type.SYMBOL;
                    text = placeholder;
                } else if (placeholder.equals(">=")) {
                    type = Type.SYMBOL;
                    text = placeholder;
                } else if (placeholder.equals("=")) {
                    type = Type.SYMBOL;
                    text = placeholder;
                } else if (placeholder.equals("+")) {
                    type = Type.SYMBOL;
                    text = placeholder;
                } else if (placeholder.equals("-")) {
                    type = Type.SYMBOL;
                    text = placeholder;
                } else if (placeholder.equals("*")) {
                    type = Type.SYMBOL;
                    text = placeholder;
                } else if (placeholder.equals("/")) {
                    type = Type.SYMBOL;
                    text = placeholder;
                } else if (placeholder.equals("<")) {
                    type = Type.SYMBOL;
                    text = placeholder;
                } else if (placeholder.equals(">")) {
                    type = Type.SYMBOL;
                    text = placeholder;
                } else if (x.startsWith(";")) {
                    type = Type.SYMBOL;
                    text = ";";
                } else if (x.startsWith(",")) {
                    type = Type.SYMBOL;
                    text = ",";
                } else if (x.startsWith("(")) {
                    type = Type.SYMBOL;
                    text = "(";
                } else if (x.startsWith(")")) {
                    type = Type.SYMBOL;
                    text = placeholder;
                } else if (x.startsWith("[")) {
                    type = Type.SYMBOL;
                    text = placeholder;
                } else if (x.startsWith("]")) {
                    type = Type.SYMBOL;
                    text = placeholder;
                } else if (x.startsWith("{")) {
                    type = Type.SYMBOL;
                    text = placeholder;
                } else if (x.startsWith("}")) {
                    type = Type.SYMBOL;
                    text = placeholder;
                } else if (placeholder.equals("else") || placeholder.equals("if") || placeholder.equals("int") || placeholder.equals("return") || placeholder.equals("void") || placeholder.equals("while")) {
                    type = Type.KEYWORD;
                    text = placeholder;
                } else if (placeholder.matches("[a-zA-Z]+")) {
                    type = Type.ID;
                    text = placeholder;
                } else if (placeholder.matches("(\\d+)(\\.\\d+)?(E(-|\\+)?\\d+)?")) {
                    type = Type.NUM;
                    text = placeholder;
                } else {
                    type = Type.ERROR;
                    text = placeholder;
                }
            }
            Token t = new Token(text, type);
            t.print();
            if (text.length() > 0) {
                readString(x.substring(text.length()).trim());
            }
        }
    }

    public ArrayList<Token> readFile(File file) {
        try {
            Files.lines(file.toPath()).map(s -> s.trim()).forEach(s -> {readLine(s);});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tokens;
    }
}

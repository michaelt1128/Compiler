import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class Compiler {
    public static void main(String[] args) {
        System.out.println("Starting compilation of " + args[0]);

        FileInterpreter interpreter = new FileInterpreter();
        ArrayList<Token> tokens = interpreter.readFile(new File(args[0]));
        tokens.forEach(token -> token.print(-1));
        Parser p = new Parser(tokens);
    }
}

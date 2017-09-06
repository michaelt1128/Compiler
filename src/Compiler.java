import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class Compiler {
    public static void main(String[] args) {
        System.out.println("Starting compilation of " + args[0]);

        FileInterpreter interpreter = new FileInterpreter();
        ArrayList<Token> tokens = interpreter.readFile(new File(args[0]));
        final boolean[] hasError = {false};
        tokens.forEach((Token token) -> {
            if ((token.getType() == Type.ERROR)) {
                hasError[0] = true;
            }
        });
        if (hasError[0]) {
            System.out.println("\nErrors exist in compilation");
        }
    }
}

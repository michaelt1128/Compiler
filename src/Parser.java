import java.util.ArrayList;

class Parser {
    private Token lookahead;
    private ArrayList<Token> tokens;
    private int index = 0;

    Parser(ArrayList<Token> tokens) {
        System.out.println("Parsing");
        this.tokens = tokens;
        this.lookahead = tokens.get(0);
        if (tokens.get(tokens.size()-1).getType() != Type.DOLLAR) {
            this.tokens.add(new Token("$", Type.DOLLAR));
        }
        program();
        if (this.lookahead.getType().equals(Type.DOLLAR)) {
            System.out.println("ACCEPT");
        } else {
            System.out.println("REJECT");
        }
    }

    private void match(Type t) {
        if (t.equals(lookahead.getType())) {
            lookahead = tokens.get(this.index + 1);
            index++;
        } else {
            System.out.println("REJECT");
            System.exit(0);
        }
    }

    private void program() {
        if (lookahead.getType().equals(Type.INT) || lookahead.getType().equals(Type.FLOAT) || lookahead.getType().equals(Type.VOID)) {
            declarationList();
        }
    }
    private void declarationList() {
        if (lookahead.getType().equals(Type.INT) || lookahead.getType().equals(Type.FLOAT) || lookahead.getType().equals(Type.VOID)) {
            declaration();
            declarationListPrime();
        }
    }
    private void declarationListPrime() {
        if (lookahead.getType().equals(Type.INT) || lookahead.getType().equals(Type.FLOAT) || lookahead.getType().equals(Type.VOID)) {
            declaration();
            declarationListPrime();
        }
    }
    private void declaration() {
        if (lookahead.getType().equals(Type.INT) || lookahead.getType().equals(Type.FLOAT) || lookahead.getType().equals(Type.VOID)) {
            typeSpecifier();
            match(Type.ID);
            declarationPrime();
        }
    }
    private void declarationPrime() {
        if (lookahead.getType().equals(Type.SEMICOLON)) {
            specifier();
        } else if (lookahead.getType().equals(Type.LEFT_BRACKET)) {
            specifier();
        } else if(lookahead.getType().equals(Type.LEFT_PAREN)) {
            match(Type.LEFT_PAREN);
            params();
            match(Type.RIGHT_PAREN);
            compoundStmt();
        }
    }
    private void specifier() {
        if (lookahead.getType().equals(Type.SEMICOLON)) {
            match(Type.SEMICOLON);
        } else if (lookahead.getType().equals(Type.LEFT_BRACKET)){
            match(Type.LEFT_BRACKET);
            match(Type.NUM);
            match(Type.RIGHT_BRACKET);
            match(Type.SEMICOLON);
        }
    }
    private void typeSpecifier() {
        if (lookahead.getType().equals(Type.INT)) {
            match(Type.INT);
        } else if (lookahead.getType().equals(Type.FLOAT)) {
            match(Type.FLOAT);
        } else if (lookahead.getType().equals(Type.VOID)) {
            match(Type.VOID);
        }
    }
    private void params() {
        if (lookahead.getType().equals(Type.INT)) {
            match(Type.INT);
            match(Type.ID);
            paramPrime();
            paramListPrime();
        } else if (lookahead.getType().equals(Type.VOID)) {
            match(Type.VOID);
            paramList();
        } else if (lookahead.getType().equals(Type.FLOAT)) {
            match(Type.FLOAT);
            match(Type.ID);
            paramPrime();
            paramListPrime();
        }
    }
    private void paramList() {
        if (lookahead.getType().equals(Type.ID)) {
            match(Type.ID);
            paramPrime();
            paramListPrime();
        }
    }
    private void paramListPrime() {
        if (lookahead.getType().equals(Type.COMMA)) {
            match(Type.COMMA);
            param();
            paramListPrime();
        }
    }
    private void param() {
        if (lookahead.getType().equals(Type.INT)) {
            typeSpecifier();
            match(Type.ID);
            paramPrime();
        } else if (lookahead.getType().equals(Type.FLOAT)) {
            typeSpecifier();
            match(Type.ID);
            paramPrime();
        } else if (lookahead.getType().equals(Type.VOID)) {
            typeSpecifier();
            match(Type.ID);
            paramPrime();
        }
    }
    private void paramPrime() {
        if (lookahead.getType().equals(Type.LEFT_BRACKET)) {
            match(Type.LEFT_BRACKET);
            match(Type.RIGHT_BRACKET);
        }
    }
    private void compoundStmt() {
        if (lookahead.getType().equals(Type.LEFT_BRACE)) {
            match(Type.LEFT_BRACE);
            localDeclarations();
            statementList();
            match(Type.RIGHT_BRACE);
        }
    }
    private void localDeclarations() {
        if (lookahead.getType().equals(Type.INT)) {
            localDeclarationsPrime();
        } else if (lookahead.getType().equals(Type.FLOAT)) {
            localDeclarationsPrime();
        } else if (lookahead.getType().equals(Type.VOID)) {
            localDeclarationsPrime();
        }
    }
    private void localDeclarationsPrime() {
        if (lookahead.getType().equals(Type.INT)) {
            typeSpecifier();
            match(Type.ID);
            specifier();
            localDeclarationsPrime();
        } else if (lookahead.getType().equals(Type.FLOAT)) {
            typeSpecifier();
            match(Type.ID);
            specifier();
            localDeclarationsPrime();
        } else if (lookahead.getType().equals(Type.VOID)) {
            typeSpecifier();
            match(Type.ID);
            specifier();
            localDeclarationsPrime();
        }
    }
    private void statementList() {
        if (lookahead.getType().equals(Type.ID)) {
            statementListPrime();
        } else if (lookahead.getType().equals(Type.SEMICOLON)) {
            statementListPrime();
        } else if (lookahead.getType().equals(Type.NUM)) {
            statementListPrime();
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            statementListPrime();
        } else if (lookahead.getType().equals(Type.LEFT_BRACE)) {
            statementListPrime();
        } else if (lookahead.getType().equals(Type.IF)) {
            statementListPrime();
        } else if (lookahead.getType().equals(Type.WHILE)) {
            statementListPrime();
        } else if (lookahead.getType().equals(Type.RETURN)) {
            statementListPrime();
        }
    }
    private void statementListPrime() {
        if (lookahead.getType().equals(Type.ID)) {
            statement();
            statementListPrime();
        } else if (lookahead.getType().equals(Type.SEMICOLON)) {
            statement();
            statementListPrime();
        } else if (lookahead.getType().equals(Type.NUM)) {
            statement();
            statementListPrime();
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            statement();
            statementListPrime();
        } else if (lookahead.getType().equals(Type.LEFT_BRACE)) {
            statement();
            statementListPrime();
        } else if (lookahead.getType().equals(Type.IF)) {
            statement();
            statementListPrime();
        } else if (lookahead.getType().equals(Type.WHILE)) {
            statement();
            statementListPrime();
        } else if (lookahead.getType().equals(Type.RETURN)) {
            statement();
            statementListPrime();
        }
    }
    private void statement() {
        if (lookahead.getType().equals(Type.ID)) {
            expressionStmt();
        } else if (lookahead.getType().equals(Type.SEMICOLON)) {
            expressionStmt();
        } else if (lookahead.getType().equals(Type.NUM)) {
            expressionStmt();
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            expressionStmt();
        } else if (lookahead.getType().equals(Type.SEMICOLON)) {
            expressionStmt();
        } else if (lookahead.getType().equals(Type.LEFT_BRACE)) {
            compoundStmt();
        } else if (lookahead.getType().equals(Type.IF)) {
            selectionStmt();
        } else if (lookahead.getType().equals(Type.WHILE)) {
            iterationStmt();
        } else if (lookahead.getType().equals(Type.RETURN)) {
            returnStmt();
        }
    }
    private void expressionStmt() {
        if (lookahead.getType().equals(Type.ID)) {
            expression();
            match(Type.SEMICOLON);
        } else if (lookahead.getType().equals(Type.SEMICOLON)) {
            match(Type.SEMICOLON);
        } else if (lookahead.getType().equals(Type.NUM)) {
            expression();
            match(Type.SEMICOLON);
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            expression();
            match(Type.SEMICOLON);
        }
    }
    private void selectionStmt() {
        if (lookahead.getType().equals(Type.IF)) {
            match(Type.IF);
            match(Type.LEFT_PAREN);
            expression();
            match(Type.RIGHT_PAREN);
            statement();
            selectionStmtPrime();
        }
    }
    private void selectionStmtPrime() {
        if (lookahead.getType().equals(Type.ELSE)) {
            match(Type.ELSE);
            statement();
        }
    }
    private void iterationStmt() {
        if (lookahead.getType().equals(Type.WHILE)) {
            match(Type.WHILE);
            match(Type.LEFT_PAREN);
            expression();
            match(Type.RIGHT_PAREN);
            statement();
        }
    }
    private void returnStmt() {
        if (lookahead.getType().equals(Type.RETURN)) {
            match(Type.RETURN);
            returnStmtPrime();
        }
    }
    private void returnStmtPrime() {
        if (lookahead.getType().equals(Type.ID)) {
            expression();
            match(Type.SEMICOLON);
        } else if (lookahead.getType().equals(Type.SEMICOLON)) {
            match(Type.SEMICOLON);
        } else if (lookahead.getType().equals(Type.NUM)) {
            expression();
            match(Type.SEMICOLON);
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            expression();
            match(Type.SEMICOLON);
        }
    }
    private void expression() {
        if (lookahead.getType().equals(Type.ID)) {
            match(Type.ID);
            var();
        } else if (lookahead.getType().equals(Type.NUM)) {
            match(Type.NUM);
            termPrime();
            additiveExpressionPrime();
            relopExpression();
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            match(Type.LEFT_PAREN);
            expression();
            match(Type.RIGHT_PAREN);
            termPrime();
            additiveExpressionPrime();
            relopExpression();
        }
    }
    private void var() {
        if (lookahead.getType().equals(Type.LEFT_BRACKET)) {
            varArr();
            varPrime();
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            match(Type.LEFT_PAREN);
            args();
            match(Type.RIGHT_PAREN);
            termPrime();
            additiveExpressionPrime();
            relopExpression();
        } else if (lookahead.getType().equals(Type.SET_EQUAL)) {
            varArr();
            varPrime();
        } else if (lookahead.getType().equals(Type.EQUAL_TO)) {
            varArr();
            varPrime();
        } else if (lookahead.getType().equals(Type.EQUAL_GREATER)) {
            varArr();
            varPrime();
        } else if (lookahead.getType().equals(Type.EQUAL_LESS)) {
            varArr();
            varPrime();
        } else if (lookahead.getType().equals(Type.GREATER_THAN)) {
            varArr();
            varPrime();
        } else if (lookahead.getType().equals(Type.LESS_THAN)) {
            varArr();
            varPrime();
        } else if (lookahead.getType().equals(Type.NOT_EQUAL)) {
            varArr();
            varPrime();
        } else if (lookahead.getType().equals(Type.PLUS)) {
            varArr();
            varPrime();
        } else if (lookahead.getType().equals(Type.MINUS)) {
            varArr();
            varPrime();
        } else if (lookahead.getType().equals(Type.DIVIDE)) {
            varArr();
            varPrime();
        } else if (lookahead.getType().equals(Type.MULTIPLY)) {
            varArr();
            varPrime();
        }
    }
    private void varPrime() {
        if (lookahead.getType().equals(Type.SET_EQUAL)) {
            match(Type.SET_EQUAL);
            expression();
        } else if (lookahead.getType().equals(Type.EQUAL_TO)) {
            termPrime();
            additiveExpressionPrime();
            relopExpression();
        } else if (lookahead.getType().equals(Type.EQUAL_GREATER)) {
            termPrime();
            additiveExpressionPrime();
            relopExpression();
        } else if (lookahead.getType().equals(Type.EQUAL_LESS)) {
            termPrime();
            additiveExpressionPrime();
            relopExpression();
        } else if (lookahead.getType().equals(Type.GREATER_THAN)) {
            termPrime();
            additiveExpressionPrime();
            relopExpression();
        } else if (lookahead.getType().equals(Type.LESS_THAN)) {
            termPrime();
            additiveExpressionPrime();
            relopExpression();
        } else if (lookahead.getType().equals(Type.NOT_EQUAL)) {
            termPrime();
            additiveExpressionPrime();
            relopExpression();
        } else if (lookahead.getType().equals(Type.PLUS)) {
            termPrime();
            additiveExpressionPrime();
            relopExpression();
        } else if (lookahead.getType().equals(Type.MINUS)) {
            termPrime();
            additiveExpressionPrime();
            relopExpression();
        } else if (lookahead.getType().equals(Type.DIVIDE)) {
            termPrime();
            additiveExpressionPrime();
            relopExpression();
        } else if (lookahead.getType().equals(Type.MULTIPLY)) {
            termPrime();
            additiveExpressionPrime();
            relopExpression();
        }
    }
    private void varArr() {
        if (lookahead.getType().equals(Type.LEFT_BRACKET)) {
            match(Type.LEFT_BRACKET);
            expression();
            match(Type.RIGHT_BRACKET);
        }
    }
    private void relopExpression() {
        if (lookahead.getType().equals(Type.EQUAL_TO)) {
            relop();
            additiveExpression();
        } else if (lookahead.getType().equals(Type.EQUAL_GREATER)) {
            relop();
            additiveExpression();
        } else if (lookahead.getType().equals(Type.EQUAL_LESS)) {
            relop();
            additiveExpression();
        } else if (lookahead.getType().equals(Type.GREATER_THAN)) {
            relop();
            additiveExpression();
        } else if (lookahead.getType().equals(Type.LESS_THAN)) {
            relop();
            additiveExpression();
        } else if (lookahead.getType().equals(Type.NOT_EQUAL)) {
            relop();
            additiveExpression();
        }
    }
    private void relop() {
        if (lookahead.getType().equals(Type.EQUAL_TO)) {
            match(Type.EQUAL_TO);
        } else if (lookahead.getType().equals(Type.EQUAL_GREATER)) {
            match(Type.EQUAL_GREATER);
        } else if (lookahead.getType().equals(Type.EQUAL_LESS)) {
            match(Type.EQUAL_LESS);
        } else if (lookahead.getType().equals(Type.GREATER_THAN)) {
            match(Type.GREATER_THAN);
        } else if (lookahead.getType().equals(Type.LESS_THAN)) {
            match(Type.LESS_THAN);
        } else if (lookahead.getType().equals(Type.NOT_EQUAL)) {
            match(Type.NOT_EQUAL);
        }
    }
    private void additiveExpression() {
        if (lookahead.getType().equals(Type.ID)) {
            match(Type.ID);
            call();
            termPrime();
            additiveExpressionPrime();
        } else if (lookahead.getType().equals(Type.NUM)) {
            match(Type.NUM);
            termPrime();
            additiveExpressionPrime();
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            match(Type.LEFT_PAREN);
            expression();
            match(Type.RIGHT_PAREN);
            termPrime();
            additiveExpressionPrime();
        }
    }
    private void additiveExpressionPrime() {
        if (lookahead.getType().equals(Type.PLUS)) {
            addop();
            term();
            additiveExpressionPrime();
        } else if (lookahead.getType().equals(Type.MINUS)) {
            addop();
            term();
            additiveExpressionPrime();
        }
    }
    private void addop() {
        if (lookahead.getType().equals(Type.PLUS)) {
            match(Type.PLUS);
        } else if (lookahead.getType().equals(Type.MINUS)) {
            match(Type.MINUS);
        }
    }
    private void term() {
        if (lookahead.getType().equals(Type.ID)) {
            match(Type.ID);
            call();
            termPrime();
        } else if (lookahead.getType().equals(Type.NUM)) {
            match(Type.NUM);
            termPrime();
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            match(Type.LEFT_PAREN);
            expression();
            match(Type.RIGHT_PAREN);
            termPrime();
        }
    }
    private void termPrime() {
        if (lookahead.getType().equals(Type.MULTIPLY)) {
            mulop();
            factor();
            termPrime();
        } else if (lookahead.getType().equals(Type.DIVIDE)) {
            mulop();
            factor();
            termPrime();
        }
    }
    private void factor() {
        if (lookahead.getType().equals(Type.ID)) {
            match(Type.ID);
            call();
        } else if (lookahead.getType().equals(Type.NUM)) {
            match(Type.NUM);
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            match(Type.LEFT_PAREN);
            expression();
            match(Type.RIGHT_PAREN);
        }
    }
    private void mulop() {
        if (lookahead.getType().equals(Type.MULTIPLY)) {
            match(Type.MULTIPLY);
        } else if (lookahead.getType().equals(Type.DIVIDE)) {
            match(Type.DIVIDE);
        }
    }
    private void call() {
        if (lookahead.getType().equals(Type.LEFT_BRACKET)) {
            varArr();
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            match(Type.LEFT_PAREN);
            args();
            match(Type.RIGHT_PAREN);
        }
    }
    private void args() {
        if (lookahead.getType().equals(Type.ID)) {
            argList();
        } else if (lookahead.getType().equals(Type.NUM)) {
            argList();
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            argList();
        }
    }
    private void argList() {
        if (lookahead.getType().equals(Type.ID)) {
            expression();
            argListPrime();
        } else if (lookahead.getType().equals(Type.NUM)) {
            expression();
            argListPrime();
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            expression();
            argListPrime();
        }
    }
    private void argListPrime() {
        if (lookahead.getType().equals(Type.COMMA)) {
            match(Type.COMMA);
            expression();
            argListPrime();
        }
    }
}


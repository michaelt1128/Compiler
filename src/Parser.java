import java.util.ArrayList;

class Parser {
    private Token lookahead;
    private ArrayList<Token> tokens;
    private int index = 0;
    private ArrayList<ArrayList<Symbol>> symbolTableStack = new ArrayList<>();
    private ArrayList<String[]> quadruples = new ArrayList<>();
    private int tempVariableIndex = 0;

    Parser(ArrayList<Token> tokens) {
        this.tokens = tokens;
        this.lookahead = tokens.get(0);

        if (tokens.get(tokens.size()-1).getType() != Type.DOLLAR) {
            this.tokens.add(new Token("$", Type.DOLLAR));
        }

        // add the global symbol table
        symbolTableStack.add(new ArrayList<>());

        //run program
        program();

        if (this.lookahead.getType().equals(Type.DOLLAR)) {
//            System.out.println("ACCEPT");
            System.out.printf("%-11s%-12s%-12s%-12s\n", "-----------", "|-----------", "|-----------", "|-----------");
            quadruples.forEach(q -> System.out.printf("%-12s%-12s%-12s%-12s\n", q[0], q[1], q[2], q[3]));
        } else {
            reject();
        }
    }

    private void reject() {
        System.out.println("REJECT");
        System.exit(0);
    }

    private boolean findIdentifierInCurrentLevel(Token t, Type type) {
        boolean found = false;
        for (int i = 0; i < symbolTableStack.get(symbolTableStack.size() - 1).size(); i++) {
            if (symbolTableStack.get(symbolTableStack.size() - 1).get(i).getId().equals(t.getValue())) {
                reject();
                found = true;
            }
        }
        if (!found) {
            symbolTableStack.get(symbolTableStack.size() - 1).add(new Symbol(false, t.getValue(), type));
        }
        return found;
    }

    private Type findIdentifierInStack(Token t) {
        boolean found = false;
        for (int i = symbolTableStack.size() - 1; i >= 0; i--) {
            if (symbolTableStack.get(i).size() > 0) {
                for (int j = 0; j < symbolTableStack.get(i).size() && !found; j++) {
                    if (symbolTableStack.get(i).get(j).getId().equals(t.getValue())) {
                        return symbolTableStack.get(i).get(j).getType();
                    }
                }
            }
        }
        reject();
        return null;
    }

    private void arrayCheckInStack(Token t, int size) {
        boolean found = false;
        for (int i = symbolTableStack.size() - 1; i >= 0; i--) {
            if (symbolTableStack.get(i).size() > 0) {
                for (int j = 0; j < symbolTableStack.get(i).size() && !found; j++) {
                    if (symbolTableStack.get(i).get(j).getId().equals(t.getValue())) {
                        if (symbolTableStack.get(i).get(j).isArray()) {
                            if (size > symbolTableStack.get(i).get(j).getArraySize() - 1) {
                                reject();
                            }
                        } else {
                            symbolTableStack.get(i).get(j).setArraySize(size);
                            symbolTableStack.get(i).get(j).setArray(true);
                        }
                        return;
                    }
                }
            }
        }
        reject();
    }

    private void updateParametersInStack(Token t, ArrayList<Symbol> parameters) {
        boolean found = false;
        for (int i = symbolTableStack.size() - 1; i >= 0; i--) {
            if (symbolTableStack.get(i).size() > 0) {
                for (int j = 0; j < symbolTableStack.get(i).size() && !found; j++) {
                    if (symbolTableStack.get(i).get(j).getId().equals(t.getValue())) {
                        if (parameters != null) symbolTableStack.get(i).get(j).setParameterTypes(parameters);
                        return;
                    }
                }
            }
        }
        reject();
    }
    private void checkParameters(Token t, ArrayList<Symbol> parameters) {
        boolean found = false;
        for (int i = symbolTableStack.size() - 1; i >= 0; i--) {
            if (symbolTableStack.get(i).size() > 0) {
                for (int j = 0; j < symbolTableStack.get(i).size() && !found; j++) {
                    if (symbolTableStack.get(i).get(j).getId().equals(t.getValue())) {
                        Symbol symbol = symbolTableStack.get(i).get(j);
                        if (symbol.getParameterTypes().size() != parameters.size()) {
                            reject();
                        }
                        for (int k = 0; k < symbol.getParameterTypes().size(); k++) {
                            if (!symbol.getParameterTypes().get(k).getType().equals(parameters.get(k).getType())) {
                                reject();
                            }
                        }
                        return;
                    }
                }
            }
        }
        reject();
    }

    private void match(Type t) {
        if (t.equals(lookahead.getType())) {
            lookahead = tokens.get(this.index + 1);
            index++;
        } else {
            reject();
        }
    }

    private void program() {
        if (lookahead.getType().equals(Type.INT_DEC) || lookahead.getType().equals(Type.FLOAT_DEC) || lookahead.getType().equals(Type.VOID)) {
            declarationList();
        }
    }
    private void declarationList() {
        if (lookahead.getType().equals(Type.INT_DEC) || lookahead.getType().equals(Type.FLOAT_DEC) || lookahead.getType().equals(Type.VOID)) {
            declaration();
            declarationListPrime();
        }
    }
    private void declarationListPrime() {
        if (lookahead.getType().equals(Type.INT_DEC) || lookahead.getType().equals(Type.FLOAT_DEC) || lookahead.getType().equals(Type.VOID)) {
            declaration();
            declarationListPrime();
        }
    }
    private void declaration() {
        if (lookahead.getType().equals(Type.VOID)) {
            typeSpecifier();
            findIdentifierInCurrentLevel(lookahead, Type.VOID);
            Token identifier = lookahead;
            match(Type.ID);
            boolean function = lookahead.getType().equals(Type.LEFT_PAREN);

            // void must be a function
            if (!function) {
                reject();
            }
            Type decType = declarationPrime(identifier, Type.VOID);
            if (function && decType != null && !decType.equals(Type.VOID)) {
                reject();
            }
        } else if (lookahead.getType().equals(Type.INT_DEC)) {
            typeSpecifier();
            findIdentifierInCurrentLevel(lookahead, Type.NUM);
            Token identifier = lookahead;
            match(Type.ID);
            boolean function = lookahead.getType().equals(Type.LEFT_PAREN);

            Type decType = declarationPrime(identifier, Type.INT_DEC);
            if (function && (decType == null || !decType.equals(Type.NUM))) {
                reject();
            }
        } else if (lookahead.getType().equals(Type.FLOAT_DEC)) {
            typeSpecifier();
            findIdentifierInCurrentLevel(lookahead, Type.FLOAT);
            Token identifier = lookahead;
            match(Type.ID);
            boolean function = lookahead.getType().equals(Type.LEFT_PAREN);

            Type decType = declarationPrime(identifier, Type.FLOAT_DEC);
            if (function && (decType == null || !decType.equals(Type.FLOAT))) {
                reject();
            }
        }
    }
    private Type declarationPrime(Token identifier, Type funcType) {
        if (lookahead.getType().equals(Type.SEMICOLON)) {
            specifier(identifier);
        } else if (lookahead.getType().equals(Type.LEFT_BRACKET)) {
            specifier(identifier);
        } else if(lookahead.getType().equals(Type.LEFT_PAREN)) {
            Token id = tokens.get(index - 1);
            match(Type.LEFT_PAREN);
            symbolTableStack.add(new ArrayList<>());
            ArrayList<Symbol> params = params();
            if (params == null) params = new ArrayList<>();
            if (funcType.equals(Type.VOID)) {
                quadruples.add(new String[]{"func", identifier.getValue(), "void", Integer.toString(params.size())});
            } else if (funcType.equals(Type.INT_DEC)) {
                quadruples.add(new String[]{"func", identifier.getValue(), "int", Integer.toString(params.size())});
            } else if (funcType.equals(Type.FLOAT_DEC)) {
                quadruples.add(new String[]{"func", identifier.getValue(), "float", Integer.toString(params.size())});
            }
            if (params.size() > 0) {
                quadruples.add(new String[]{"param", "", "", ""});
                params.forEach(p -> quadruples.add(new String[]{"alloc", "4", "", p.getId()}));
            }
            updateParametersInStack(id, params);
            match(Type.RIGHT_PAREN);
            return compoundStmt();
        }
        return null;
    }
    private void specifier(Token identifier) {
        if (lookahead.getType().equals(Type.SEMICOLON)) {
            quadruples.add(new String[]{"alloc", "4", "", identifier.getValue()});
            match(Type.SEMICOLON);
        } else if (lookahead.getType().equals(Type.LEFT_BRACKET)){
            match(Type.LEFT_BRACKET);
            arrayCheckInStack(identifier, Integer.parseInt(lookahead.getValue()));
            quadruples.add(new String[]{"alloc", Integer.toString(4 * Integer.parseInt(lookahead.getValue())), "", identifier.getValue()});
            match(Type.NUM);
            match(Type.RIGHT_BRACKET);
            match(Type.SEMICOLON);
        }
    }
    private void typeSpecifier() {
        if (lookahead.getType().equals(Type.INT_DEC)) {
            match(Type.INT_DEC);
        } else if (lookahead.getType().equals(Type.FLOAT_DEC)) {
            match(Type.FLOAT_DEC);
        } else if (lookahead.getType().equals(Type.VOID)) {
            match(Type.VOID);
        }
    }
    private ArrayList<Symbol> params() {
        ArrayList<Symbol> paramArrayList = new ArrayList<>();
        if (lookahead.getType().equals(Type.INT_DEC)) {
            match(Type.INT_DEC);
            findIdentifierInCurrentLevel(lookahead, Type.NUM);
            Token tempLookahead = lookahead;
            match(Type.ID);
            boolean isArray = paramPrime();
            paramArrayList.add(new Symbol(isArray, tempLookahead.getValue(), Type.NUM));
            paramArrayList = paramListPrime(paramArrayList);
            return paramArrayList;
        } else if (lookahead.getType().equals(Type.VOID)) {
            match(Type.VOID);
            paramArrayList.add(new Symbol(false, "", Type.VOID));
            paramList(paramArrayList);
        } else if (lookahead.getType().equals(Type.FLOAT_DEC)) {
            match(Type.FLOAT_DEC);
            findIdentifierInCurrentLevel(lookahead, Type.FLOAT);
            Token tempLookahead = lookahead;
            match(Type.ID);
            boolean isArray = paramPrime();
            paramArrayList.add(new Symbol(isArray, tempLookahead.getValue(), Type.FLOAT));
            paramArrayList = paramListPrime(paramArrayList);
            return paramArrayList;
        }
        return null;
    }
    private ArrayList<Symbol> paramList(ArrayList<Symbol> paramArrayList) {
        if (lookahead.getType().equals(Type.ID)) {
            reject();
            match(Type.ID);
            return paramListPrime(paramArrayList);
        }
        return paramArrayList;
    }
    private ArrayList<Symbol> paramListPrime(ArrayList<Symbol> paramArrayList) {
        if (lookahead.getType().equals(Type.COMMA)) {
            match(Type.COMMA);
            Symbol p = param();
            if (p != null) {
                paramArrayList.add(p);
            }
            return paramListPrime(paramArrayList);
        }
        return paramArrayList;
    }
    private Symbol param() {
        if (lookahead.getType().equals(Type.INT_DEC)) {
            typeSpecifier();
            String id = lookahead.getValue();
            match(Type.ID);
            boolean isArray = paramPrime();
            return new Symbol(isArray, id, Type.NUM);
        } else if (lookahead.getType().equals(Type.FLOAT_DEC)) {
            typeSpecifier();
            String id = lookahead.getValue();
            match(Type.ID);
            boolean isArray = paramPrime();
            return new Symbol(isArray, id, Type.FLOAT);
        } else if (lookahead.getType().equals(Type.VOID)) {
            typeSpecifier();
            match(Type.ID);
            reject();
        }
        return null;
    }
    private boolean paramPrime() {
        if (lookahead.getType().equals(Type.LEFT_BRACKET)) {
            match(Type.LEFT_BRACKET);
            match(Type.RIGHT_BRACKET);
            return true;
        }
        return false;
    }
    private Type compoundStmt() {
        if (lookahead.getType().equals(Type.LEFT_BRACE)) {
            match(Type.LEFT_BRACE);
            localDeclarations();
            Type stmtType = statementList();
            match(Type.RIGHT_BRACE);
            symbolTableStack.remove(symbolTableStack.size() - 1);
            return stmtType;
        }
        return null;
    }
    private void localDeclarations() {
        if (lookahead.getType().equals(Type.INT_DEC)) {
            localDeclarationsPrime();
        } else if (lookahead.getType().equals(Type.FLOAT_DEC)) {
            localDeclarationsPrime();
        } else if (lookahead.getType().equals(Type.VOID)) {
            localDeclarationsPrime();
        }
    }
    private void localDeclarationsPrime() {
        if (lookahead.getType().equals(Type.INT_DEC)) {
            typeSpecifier();
            findIdentifierInCurrentLevel(lookahead, Type.NUM);
            Token identifier = lookahead;
            match(Type.ID);
            specifier(identifier);
            localDeclarationsPrime();
        } else if (lookahead.getType().equals(Type.FLOAT_DEC)) {
            typeSpecifier();
            findIdentifierInCurrentLevel(lookahead, Type.FLOAT);
            Token identifier = lookahead;
            match(Type.ID);
            specifier(identifier);
            localDeclarationsPrime();
        } else if (lookahead.getType().equals(Type.VOID)) {
            typeSpecifier();
            findIdentifierInCurrentLevel(lookahead, Type.VOID);
            Token identifier = lookahead;
            match(Type.ID);
            specifier(identifier);
            reject();
            localDeclarationsPrime();
        }
    }
    private Type statementList() {
        if (lookahead.getType().equals(Type.ID)) {
            return statementListPrime();
        } else if (lookahead.getType().equals(Type.SEMICOLON)) {
            return statementListPrime();
        } else if (lookahead.getType().equals(Type.NUM)) {
            return statementListPrime();
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            return statementListPrime();
        } else if (lookahead.getType().equals(Type.LEFT_BRACE)) {
            return statementListPrime();
        } else if (lookahead.getType().equals(Type.IF)) {
            return statementListPrime();
        } else if (lookahead.getType().equals(Type.WHILE)) {
            return statementListPrime();
        } else if (lookahead.getType().equals(Type.RETURN)) {
            return statementListPrime();
        }
        return null;
    }
    private Type statementListPrime() {
        if (lookahead.getType().equals(Type.ID)) {
            Type stmtType = statement();
            Type stmtPrimeType = statementListPrime();
            if (stmtType != null) {
                return stmtType;
            } else if (stmtPrimeType != null) {
                return stmtPrimeType;
            }
        } else if (lookahead.getType().equals(Type.SEMICOLON)) {
            Type stmtType = statement();
            Type stmtPrimeType = statementListPrime();
            if (stmtType != null) {
                return stmtType;
            } else if (stmtPrimeType != null) {
                return stmtPrimeType;
            }
        } else if (lookahead.getType().equals(Type.NUM)) {
            Type stmtType = statement();
            Type stmtPrimeType = statementListPrime();
            if (stmtType != null) {
                return stmtType;
            } else if (stmtPrimeType != null) {
                return stmtPrimeType;
            }
        } else if (lookahead.getType().equals(Type.FLOAT)) {
            Type stmtType = statement();
            Type stmtPrimeType = statementListPrime();
            if (stmtType != null) {
                return stmtType;
            } else if (stmtPrimeType != null) {
                return stmtPrimeType;
            }
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            Type stmtType = statement();
            Type stmtPrimeType = statementListPrime();
            if (stmtType != null) {
                return stmtType;
            } else if (stmtPrimeType != null) {
                return stmtPrimeType;
            }
        } else if (lookahead.getType().equals(Type.LEFT_BRACE)) {
            Type stmtType = statement();
            Type stmtPrimeType = statementListPrime();
            if (stmtType != null) {
                return stmtType;
            } else if (stmtPrimeType != null) {
                return stmtPrimeType;
            }
        } else if (lookahead.getType().equals(Type.IF)) {
            Type stmtType = statement();
            Type stmtPrimeType = statementListPrime();
            if (stmtType != null) {
                return stmtType;
            } else if (stmtPrimeType != null) {
                return stmtPrimeType;
            }
        } else if (lookahead.getType().equals(Type.WHILE)) {
            Type stmtType = statement();
            Type stmtPrimeType = statementListPrime();
            if (stmtType != null) {
                return stmtType;
            } else if (stmtPrimeType != null) {
                return stmtPrimeType;
            }
        } else if (lookahead.getType().equals(Type.RETURN)) {
            Type stmtType = statement();
            Type stmtPrimeType = statementListPrime();
            if (stmtType != null) {
                return stmtType;
            } else if (stmtPrimeType != null) {
                return stmtPrimeType;
            }
        }
        return null;
    }
    private Type statement() {
        if (lookahead.getType().equals(Type.ID)) {
            expressionStmt();
        } else if (lookahead.getType().equals(Type.SEMICOLON)) {
            expressionStmt();
        } else if (lookahead.getType().equals(Type.NUM)) {
            expressionStmt();
        } else if (lookahead.getType().equals(Type.FLOAT)) {
            expressionStmt();
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            expressionStmt();
        } else if (lookahead.getType().equals(Type.SEMICOLON)) {
            expressionStmt();
        } else if (lookahead.getType().equals(Type.LEFT_BRACE)) {
            symbolTableStack.add(new ArrayList<>());
            compoundStmt();
        } else if (lookahead.getType().equals(Type.IF)) {
            selectionStmt();
        } else if (lookahead.getType().equals(Type.WHILE)) {
            iterationStmt();
        } else if (lookahead.getType().equals(Type.RETURN)) {
            return returnStmt();
        }
        return null;
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
        } else if (lookahead.getType().equals(Type.FLOAT)) {
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
    private Type returnStmt() {
        if (lookahead.getType().equals(Type.RETURN)) {
            match(Type.RETURN);
            return returnStmtPrime();
        }
        return null;
    }
    private Type returnStmtPrime() {
        if (lookahead.getType().equals(Type.ID)) {
            Type expType = expression();
            match(Type.SEMICOLON);
            return expType;
        } else if (lookahead.getType().equals(Type.SEMICOLON)) {
            match(Type.SEMICOLON);
            return Type.VOID;
        } else if (lookahead.getType().equals(Type.NUM)) {
            Type expType = expression();
            match(Type.SEMICOLON);
            return expType;
        } else if (lookahead.getType().equals(Type.FLOAT)) {
            Type expType = expression();
            match(Type.SEMICOLON);
            return expType;
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            Type expType = expression();
            match(Type.SEMICOLON);
            return expType;
        }
        return null;
    }
    private Type expression() {
        if (lookahead.getType().equals(Type.ID)) {
            Type expressionType = findIdentifierInStack(lookahead);

            Token identifier = lookahead;
            match(Type.ID);
            Type varType = var(identifier);
            if (varType != null && !expressionType.equals(varType)) {
                reject();
            };
            return expressionType;
        } else if (lookahead.getType().equals(Type.NUM)) {
            String lookaheadValue = lookahead.getValue();
            match(Type.NUM);
            Type termType = termPrime();
            if (termType != null && !termType.equals(Type.NUM)) {
                reject();
            }
            Type aepType = additiveExpressionPrime(lookaheadValue);
            if (aepType != null && !aepType.equals(Type.NUM)) {
                reject();
            }
            relopExpression();
            return Type.NUM;
        } else if (lookahead.getType().equals(Type.FLOAT)) {
            String lookaheadValue = lookahead.getValue();
            match(Type.FLOAT);
            Type termType = termPrime();
            if (termType != null && !termType.equals(Type.FLOAT)) {
                reject();
            }
            Type aepType = additiveExpressionPrime(lookaheadValue);
            if (aepType != null && !aepType.equals(Type.FLOAT)) {
                reject();
            }
            relopExpression();
            return Type.FLOAT;
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            match(Type.LEFT_PAREN);
            Type expressionType = expression();
            match(Type.RIGHT_PAREN);
            termPrime();
            additiveExpressionPrime("");
            relopExpression();
            return expressionType;
        }
        return null;
    }
    private Type var(Token identifier) {
        if (lookahead.getType().equals(Type.LEFT_BRACKET)) {
            varArr(identifier);
            varPrime();
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            match(Type.LEFT_PAREN);
            ArrayList<Symbol> arguments = args();
            checkParameters(identifier, arguments);
            match(Type.RIGHT_PAREN);
            termPrime();
            additiveExpressionPrime("");
            relopExpression();
        } else if (lookahead.getType().equals(Type.SET_EQUAL)) {
            varArr(identifier);
            Type vpResult = varPrime();
            quadruples.add(new String[]{"assign","","",identifier.getValue()});
            return vpResult;
        } else if (lookahead.getType().equals(Type.EQUAL_TO)) {
            varArr(identifier);
            return varPrime();
        } else if (lookahead.getType().equals(Type.EQUAL_GREATER)) {
            varArr(identifier);
            return varPrime();
        } else if (lookahead.getType().equals(Type.EQUAL_LESS)) {
            varArr(identifier);
            return varPrime();
        } else if (lookahead.getType().equals(Type.GREATER_THAN)) {
            varArr(identifier);
            return varPrime();
        } else if (lookahead.getType().equals(Type.LESS_THAN)) {
            varArr(identifier);
            return varPrime();
        } else if (lookahead.getType().equals(Type.NOT_EQUAL)) {
            varArr(identifier);
            return varPrime();
        } else if (lookahead.getType().equals(Type.PLUS)) {
            varArr(identifier);
            return varPrime();
        } else if (lookahead.getType().equals(Type.MINUS)) {
            varArr(identifier);
            return varPrime();
        } else if (lookahead.getType().equals(Type.DIVIDE)) {
            varArr(identifier);
            return  varPrime();
        } else if (lookahead.getType().equals(Type.MULTIPLY)) {
            varArr(identifier);
            return varPrime();
        }
        return null;
    }
    private Type varPrime() {
        if (lookahead.getType().equals(Type.SET_EQUAL)) {
            match(Type.SET_EQUAL);
            return expression();
        } else if (lookahead.getType().equals(Type.EQUAL_TO)) {
            termPrime();
            Type aepType = additiveExpressionPrime("");
            relopExpression();
            return aepType;
        } else if (lookahead.getType().equals(Type.EQUAL_GREATER)) {
            termPrime();
            Type aepType = additiveExpressionPrime("");
            relopExpression();
            return aepType;
        } else if (lookahead.getType().equals(Type.EQUAL_LESS)) {
            termPrime();
            Type aepType = additiveExpressionPrime("");
            relopExpression();
            return aepType;
        } else if (lookahead.getType().equals(Type.GREATER_THAN)) {
            termPrime();
            Type aepType = additiveExpressionPrime("");
            relopExpression();
            return aepType;
        } else if (lookahead.getType().equals(Type.LESS_THAN)) {
            termPrime();
            Type aepType = additiveExpressionPrime("");
            relopExpression();
            return aepType;
        } else if (lookahead.getType().equals(Type.NOT_EQUAL)) {
            termPrime();
            Type aepType = additiveExpressionPrime("");
            relopExpression();
            return aepType;
        } else if (lookahead.getType().equals(Type.PLUS)) {
            termPrime();
            Type aepType = additiveExpressionPrime("");
            relopExpression();
            return aepType;
        } else if (lookahead.getType().equals(Type.MINUS)) {
            termPrime();
            Type aepType = additiveExpressionPrime("");
            relopExpression();
            return aepType;
        } else if (lookahead.getType().equals(Type.DIVIDE)) {
            termPrime();
            Type aepType = additiveExpressionPrime("");
            relopExpression();
            return aepType;
        } else if (lookahead.getType().equals(Type.MULTIPLY)) {
            termPrime();
            Type aepType = additiveExpressionPrime("");
            relopExpression();
            return aepType;
        }
        return null;
    }
    private void varArr(Token identifier) {
        if (lookahead.getType().equals(Type.LEFT_BRACKET)) {
            match(Type.LEFT_BRACKET);
            Type expType = expression();
            if (expType != null && !expType.equals(Type.NUM)) {
                reject();
            }
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
            Type lookaheadType = findIdentifierInStack(lookahead);
            Token identifier = lookahead;
            match(Type.ID);
            call(identifier);
            Type termPrimeType = termPrime();
            quadruples.add(new String[]{"add", identifier.getValue(), "", ""});
            if (!lookaheadType.equals(termPrimeType)) {
                reject();
            }
            Type aepType = additiveExpressionPrime("");
            if (aepType != null && !lookaheadType.equals(aepType)) {
                reject();
            };
        } else if (lookahead.getType().equals(Type.NUM)) {
            match(Type.NUM);
            termPrime();
            additiveExpressionPrime("");
        } else if (lookahead.getType().equals(Type.FLOAT)) {
            match(Type.FLOAT);
            termPrime();
            additiveExpressionPrime("");
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            match(Type.LEFT_PAREN);
            expression();
            match(Type.RIGHT_PAREN);
            termPrime();
            additiveExpressionPrime("");
        }
    }
    private Type additiveExpressionPrime(String previousValue) {
        if (lookahead.getType().equals(Type.PLUS)) {
            addop();
            TypeLabelReturn typeLabelReturn = term();
            String newIndex = "t" + tempVariableIndex;
            quadruples.add(new String[]{"add", previousValue, typeLabelReturn.label, newIndex});
            tempVariableIndex++;

            Type aepType = additiveExpressionPrime(newIndex);
            if (aepType != null && !aepType.equals(typeLabelReturn.type)) {
                reject();
            }
            return typeLabelReturn.type;
        } else if (lookahead.getType().equals(Type.MINUS)) {
            addop();
            TypeLabelReturn typeLabelReturn = term();
            String newIndex = "t" + tempVariableIndex;
            quadruples.add(new String[]{"sub", previousValue, typeLabelReturn.label, newIndex});
            tempVariableIndex++;

            Type aepType = additiveExpressionPrime(newIndex);
            if (aepType != null && !aepType.equals(typeLabelReturn.type)) {
                System.out.println("rejected here");
                reject();
            }
            return typeLabelReturn.type;
        }
        return null;
    }
    private void addop() {
        if (lookahead.getType().equals(Type.PLUS)) {
            match(Type.PLUS);
        } else if (lookahead.getType().equals(Type.MINUS)) {
            match(Type.MINUS);
        }
    }
    private TypeLabelReturn term() {
        if (lookahead.getType().equals(Type.ID)) {
            Type termType = findIdentifierInStack(lookahead);
            Token identifier = lookahead;
            match(Type.ID);
            call(identifier);
            termPrime();
            return new TypeLabelReturn(identifier.getType(), identifier.getValue());
        } else if (lookahead.getType().equals(Type.NUM)) {
            Token identifier = lookahead;
            match(Type.NUM);
            termPrime();
            return new TypeLabelReturn(Type.NUM, identifier.getValue());
        } else if (lookahead.getType().equals(Type.FLOAT)) {
            Token identifier = lookahead;
            match(Type.FLOAT);
            termPrime();
            return new TypeLabelReturn(Type.FLOAT, identifier.getValue());
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            match(Type.LEFT_PAREN);
            Type termType = expression();
            match(Type.RIGHT_PAREN);
            termPrime();
            return new TypeLabelReturn(termType, "");
        }
        return null;
    }
    private Type termPrime() {
        if (lookahead.getType().equals(Type.MULTIPLY)) {
            mulop();
            Type factorType = factor();
            termPrime();
            return factorType;
        } else if (lookahead.getType().equals(Type.DIVIDE)) {
            mulop();
            Type factorType = factor();
            termPrime();
            return factorType;
        }
        return null;
    }
    private Type factor() {
        if (lookahead.getType().equals(Type.ID)) {
            Type expressionType = findIdentifierInStack(lookahead);
            Token identifier = lookahead;
            match(Type.ID);
            call(identifier);
            return expressionType;
        } else if (lookahead.getType().equals(Type.NUM)) {
            match(Type.NUM);
            return Type.NUM;
        } else if (lookahead.getType().equals(Type.FLOAT)) {
            match(Type.FLOAT);
            return Type.FLOAT;
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            match(Type.LEFT_PAREN);
            Type expressionType = expression();
            match(Type.RIGHT_PAREN);
            return expressionType;
        }
        return null;
    }
    private void mulop() {
        if (lookahead.getType().equals(Type.MULTIPLY)) {
            match(Type.MULTIPLY);
        } else if (lookahead.getType().equals(Type.DIVIDE)) {
            match(Type.DIVIDE);
        }
    }
    private void call(Token identifier) {
        if (lookahead.getType().equals(Type.LEFT_BRACKET)) {
            varArr(identifier);
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            match(Type.LEFT_PAREN);
            ArrayList<Symbol> arguments = args();
            checkParameters(identifier, arguments);
            match(Type.RIGHT_PAREN);
        }
    }
    private ArrayList<Symbol> args() {
        ArrayList<Symbol> symbolArrayList = new ArrayList<Symbol>();
        if (lookahead.getType().equals(Type.ID)) {
            argList(symbolArrayList);
        } else if (lookahead.getType().equals(Type.NUM)) {
            argList(symbolArrayList);
        } else if (lookahead.getType().equals(Type.FLOAT)) {
            argList(symbolArrayList);
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            argList(symbolArrayList);
        }
        return symbolArrayList;
    }
    private ArrayList<Symbol> argList(ArrayList<Symbol> symbolArrayList) {
        if (lookahead.getType().equals(Type.ID)) {
            Type expType = expression();
            symbolArrayList.add(new Symbol(false, "", expType));
            symbolArrayList = argListPrime(symbolArrayList);
            return symbolArrayList;
        } else if (lookahead.getType().equals(Type.NUM)) {
            Type expType = expression();
            symbolArrayList.add(new Symbol(false, "", expType));
            symbolArrayList = argListPrime(symbolArrayList);
            return symbolArrayList;
        } else if (lookahead.getType().equals(Type.FLOAT)) {
            Type expType = expression();
            symbolArrayList.add(new Symbol(false, "", expType));
            symbolArrayList = argListPrime(symbolArrayList);
            return symbolArrayList;
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            Type expType = expression();
            symbolArrayList.add(new Symbol(false, "", expType));
            symbolArrayList = argListPrime(symbolArrayList);
            return symbolArrayList;
        }
        return symbolArrayList;
    }
    private ArrayList<Symbol> argListPrime(ArrayList<Symbol> symbolArrayList) {
        if (lookahead.getType().equals(Type.COMMA)) {
            match(Type.COMMA);
            Type expType = expression();
            symbolArrayList.add(new Symbol(false, "", expType));
            symbolArrayList = argListPrime(symbolArrayList);
            return symbolArrayList;
        }
        return symbolArrayList;
    }
}

class TypeLabelReturn {
    public Type type;
    public String label;

    public TypeLabelReturn(Type type, String label) {
        this.type = type;
        this.label = label;
    }
}

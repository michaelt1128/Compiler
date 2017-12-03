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

        if (tokens.get(tokens.size() - 1).getType() != Type.DOLLAR) {
            this.tokens.add(new Token("$", Type.DOLLAR));
        }

        // add the global symbol table
        symbolTableStack.add(new ArrayList<>());

        //run program
        program();

        if (this.lookahead.getType().equals(Type.DOLLAR)) {
//            System.out.println("ACCEPT");
            System.out.printf("%-5s%-12s%-12s%-12s%-12s\n", "-----", "|-----------", "|-----------", "|-----------", "|-----------");
            quadruples.forEach(q -> System.out.printf("%-6s%-12s%-12s%-12s%-12s\n", quadruples.indexOf(q) + 1, q[0], q[1], q[2], q[3]));
        } else {
            reject();
        }
    }

    private void reject() {
//        System.out.printf("%-5s%-12s%-12s%-12s%-12s\n", "-----", "|-----------", "|-----------", "|-----------", "|-----------");
//        quadruples.forEach(q -> System.out.printf("%-6s%-12s%-12s%-12s%-12s\n", quadruples.indexOf(q) + 1, q[0], q[1], q[2], q[3]));
//        System.out.println("REJECT");
//        System.exit(0);
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

            // this validation is broken


//            if (function && decType != null && !decType.equals(Type.VOID)) {
//                reject();
//            }
        } else if (lookahead.getType().equals(Type.INT_DEC)) {
            typeSpecifier();
            findIdentifierInCurrentLevel(lookahead, Type.NUM);
            Token identifier = lookahead;
            match(Type.ID);
            boolean function = lookahead.getType().equals(Type.LEFT_PAREN);

            Type decType = declarationPrime(identifier, Type.INT_DEC);
//            if (function && (decType == null || !decType.equals(Type.NUM))) {
//                reject();
//            }
        } else if (lookahead.getType().equals(Type.FLOAT_DEC)) {
            typeSpecifier();
            findIdentifierInCurrentLevel(lookahead, Type.FLOAT);
            Token identifier = lookahead;
            match(Type.ID);
            boolean function = lookahead.getType().equals(Type.LEFT_PAREN);

            Type decType = declarationPrime(identifier, Type.FLOAT_DEC);
//            if (function && (decType == null || !decType.equals(Type.FLOAT))) {
//                reject();
//            }
        }
    }

    private Type declarationPrime(Token identifier, Type funcType) {
        if (lookahead.getType().equals(Type.SEMICOLON)) {
            specifier(identifier);
        } else if (lookahead.getType().equals(Type.LEFT_BRACKET)) {
            specifier(identifier);
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
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
            Type cpResults = compoundStmt();
            quadruples.add(new String[]{"end", "func", identifier.getValue(), ""});
            if (cpResults == null) {
                return funcType;
            }
            return cpResults;
        }
        return null;
    }

    private void specifier(Token identifier) {
        if (lookahead.getType().equals(Type.SEMICOLON)) {
            quadruples.add(new String[]{"alloc", "4", "", identifier.getValue()});
            match(Type.SEMICOLON);
        } else if (lookahead.getType().equals(Type.LEFT_BRACKET)) {
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
        if (lookahead.getType().equals(Type.ID)
                || lookahead.getType().equals(Type.SEMICOLON)
                || lookahead.getType().equals(Type.NUM)
                || lookahead.getType().equals(Type.FLOAT)
                || lookahead.getType().equals(Type.LEFT_PAREN)
                || lookahead.getType().equals(Type.LEFT_BRACE)
                || lookahead.getType().equals(Type.IF)
                || lookahead.getType().equals(Type.WHILE)
                || lookahead.getType().equals(Type.RETURN)) {
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
            return expressionStmt();
        } else if (lookahead.getType().equals(Type.SEMICOLON)) {
            return expressionStmt();
        } else if (lookahead.getType().equals(Type.NUM)) {
            return expressionStmt();
        } else if (lookahead.getType().equals(Type.FLOAT)) {
            return expressionStmt();
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            return expressionStmt();
        } else if (lookahead.getType().equals(Type.SEMICOLON)) {
            return expressionStmt();
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

    private Type expressionStmt() {
        if (lookahead.getType().equals(Type.ID)) {
            Type expType = expression().type;
            match(Type.SEMICOLON);
            return expType;
        } else if (lookahead.getType().equals(Type.SEMICOLON)) {
            match(Type.SEMICOLON);
        } else if (lookahead.getType().equals(Type.NUM)) {
            Type expType = expression().type;
            match(Type.SEMICOLON);
            return expType;
        } else if (lookahead.getType().equals(Type.FLOAT)) {
            Type expType = expression().type;
            match(Type.SEMICOLON);
            return expType;
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            Type expType = expression().type;
            match(Type.SEMICOLON);
            return expType;
        }
        return null;
    }

    private void selectionStmt() {
        if (lookahead.getType().equals(Type.IF)) {
            match(Type.IF);
            match(Type.LEFT_PAREN);
            int quadrupleIndex = quadruples.size();
            TypeLabelReturn expressionResult = expression();
            if (expressionResult.type.equals(Type.EQUAL_TO)) {
                quadruples.add(new String[]{"BRNE", expressionResult.label, "", ""});
            } else if (expressionResult.type.equals(Type.EQUAL_GREATER)) {
                quadruples.add(new String[]{"BRLT", expressionResult.label, "", ""});
            } else if (expressionResult.type.equals(Type.EQUAL_LESS)) {
                quadruples.add(new String[]{"BRGT", expressionResult.label, "", ""});
            } else if (expressionResult.type.equals(Type.GREATER_THAN)) {
                quadruples.add(new String[]{"BRLE", expressionResult.label, "", ""});
            } else if (expressionResult.type.equals(Type.LESS_THAN)) {
                quadruples.add(new String[]{"BRGE", expressionResult.label, "", ""});
            } else if (expressionResult.type.equals(Type.NOT_EQUAL)) {
                quadruples.add(new String[]{"BRE", expressionResult.label, "", ""});
            }
            match(Type.RIGHT_PAREN);
            statement();
            selectionStmtPrime();
            quadruples.get(quadrupleIndex)[3] = Integer.toString(quadruples.size());
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

            int beforeExpressionIndex = quadruples.size();
            TypeLabelReturn expressionResult = expression();
            int afterExpressionIndex = quadruples.size();

            if (expressionResult.type.equals(Type.EQUAL_TO)) {
                quadruples.add(new String[]{"BRNE", expressionResult.label, "", ""});
            } else if (expressionResult.type.equals(Type.EQUAL_GREATER)) {
                quadruples.add(new String[]{"BRLT", expressionResult.label, "", ""});
            } else if (expressionResult.type.equals(Type.EQUAL_LESS)) {
                quadruples.add(new String[]{"BRGT", expressionResult.label, "", ""});
            } else if (expressionResult.type.equals(Type.GREATER_THAN)) {
                quadruples.add(new String[]{"BRLE", expressionResult.label, "", ""});
            } else if (expressionResult.type.equals(Type.LESS_THAN)) {
                quadruples.add(new String[]{"BRGE", expressionResult.label, "", ""});
            } else if (expressionResult.type.equals(Type.NOT_EQUAL)) {
                quadruples.add(new String[]{"BRE", expressionResult.label, "", ""});
            }
            match(Type.RIGHT_PAREN);
            statement();
            quadruples.add(new String[]{"BR", "", "", Integer.toString(beforeExpressionIndex)});
            quadruples.get(afterExpressionIndex)[3] = Integer.toString(quadruples.size() + 1);
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
            TypeLabelReturn expType = expression();
            match(Type.SEMICOLON);
            quadruples.add(new String[]{"return", "", "", expType.label});
            return expType.type;
        } else if (lookahead.getType().equals(Type.SEMICOLON)) {
            match(Type.SEMICOLON);
            quadruples.add(new String[]{"return", "", "", ""});
            return Type.VOID;
        } else if (lookahead.getType().equals(Type.NUM)) {
            TypeLabelReturn expType = expression();
            match(Type.SEMICOLON);
            quadruples.add(new String[]{"return", "", "", expType.label});
            return expType.type;
        } else if (lookahead.getType().equals(Type.FLOAT)) {
            TypeLabelReturn expType = expression();
            match(Type.SEMICOLON);
            quadruples.add(new String[]{"return", "", "", expType.label});
            return expType.type;
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            TypeLabelReturn expType = expression();
            match(Type.SEMICOLON);
            quadruples.add(new String[]{"return", "", "", expType.label});
            return expType.type;
        }
        return null;
    }

    private TypeLabelReturn expression() {
        if (lookahead.getType().equals(Type.ID)) {
            Type expressionType = findIdentifierInStack(lookahead);

            Token identifier = lookahead;
            match(Type.ID);
            TypeLabelReturn varType = var(identifier);
//            if (varType.type != null && !expressionType.equals(varType.type)) {
//                reject();
//            }
            if (varType.type == null && varType.label == null) {
                return new TypeLabelReturn(null, identifier.getValue());
            }
            return varType;
        } else if (lookahead.getType().equals(Type.NUM)) {
            String lookaheadValue = lookahead.getValue();
            match(Type.NUM);
            TypeLabelReturn termPrimeResponse = termPrime(lookaheadValue);
            if (termPrimeResponse.type != null && !termPrimeResponse.type.equals(Type.NUM)) {
                reject();
            }
            TypeLabelReturn aepResult = additiveExpressionPrime(lookaheadValue);
            if (aepResult.type != null && !aepResult.type.equals(Type.NUM)) {
                reject();
            }
            TypeLabelReturn relopResponse = relopExpression(aepResult.label);
            if (relopResponse.type != null) {
                return relopResponse;
            }
            if (aepResult.type == null && aepResult.label == null) {
                return new TypeLabelReturn(null, lookaheadValue);
            }
            return aepResult;
        } else if (lookahead.getType().equals(Type.FLOAT)) {
            String lookaheadValue = lookahead.getValue();
            match(Type.FLOAT);
            TypeLabelReturn termPrimeResponse = termPrime(lookaheadValue);
            if (termPrimeResponse.type != null && !termPrimeResponse.type.equals(Type.FLOAT)) {
                reject();
            }
            TypeLabelReturn aepResult = additiveExpressionPrime(lookaheadValue);
            if (aepResult.type != null && !aepResult.type.equals(Type.FLOAT)) {
                reject();
            }
            TypeLabelReturn relopResponse = relopExpression(aepResult.label);
            if (relopResponse.type != null) {
                return relopResponse;
            }
            if (aepResult.type == null && aepResult.label == null) {
                return new TypeLabelReturn(null, lookaheadValue);
            }
            return aepResult;
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            match(Type.LEFT_PAREN);
            TypeLabelReturn expressionType = expression();
            match(Type.RIGHT_PAREN);
            termPrime(expressionType.label);
            TypeLabelReturn aepResult = additiveExpressionPrime("");
            relopExpression(aepResult.label);
            return expressionType;
        }
        return new TypeLabelReturn(null, null);
    }

    private TypeLabelReturn var(Token identifier) {
        if (lookahead.getType().equals(Type.LEFT_BRACKET)
                || lookahead.getType().equals(Type.SET_EQUAL)) {
            String leftSideValue = varArr(identifier);
            TypeLabelReturn vpResult = varPrime(leftSideValue);
            return vpResult;
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            match(Type.LEFT_PAREN);
            ArrayList<Symbol> arguments = args();
            checkParameters(identifier, arguments);
            match(Type.RIGHT_PAREN);
            quadruples.add(new String[]{"call", identifier.getValue(), Integer.toString(arguments.size()), "t"+tempVariableIndex});
            tempVariableIndex++;
            termPrime(identifier.getValue());
            TypeLabelReturn aepResult = additiveExpressionPrime("");
            relopExpression(aepResult.label);
        } else if (lookahead.getType().equals(Type.EQUAL_TO)
                || lookahead.getType().equals(Type.EQUAL_GREATER)
                || lookahead.getType().equals(Type.EQUAL_LESS)
                || lookahead.getType().equals(Type.GREATER_THAN)
                || lookahead.getType().equals(Type.LESS_THAN)
                || lookahead.getType().equals(Type.NOT_EQUAL)
                || lookahead.getType().equals(Type.PLUS)
                || lookahead.getType().equals(Type.MINUS)
                || lookahead.getType().equals(Type.MULTIPLY)
                || lookahead.getType().equals(Type.DIVIDE)) {
            String leftSideValue = varArr(identifier);
            TypeLabelReturn vpResult = varPrime(leftSideValue);
            return vpResult;
        }
        return new TypeLabelReturn(null, null);
    }

    private TypeLabelReturn varPrime(String identifierValue) {
        if (lookahead.getType().equals(Type.SET_EQUAL)) {
            match(Type.SET_EQUAL);
            TypeLabelReturn expressionResult = expression();
            quadruples.add(new String[]{"assign", expressionResult.label, "", identifierValue});
            return expressionResult;
        } else if (lookahead.getType().equals(Type.EQUAL_TO)
                || lookahead.getType().equals(Type.EQUAL_GREATER)
                || lookahead.getType().equals(Type.EQUAL_LESS)
                || lookahead.getType().equals(Type.GREATER_THAN)
                || lookahead.getType().equals(Type.LESS_THAN)
                || lookahead.getType().equals(Type.NOT_EQUAL)
                || lookahead.getType().equals(Type.PLUS)
                || lookahead.getType().equals(Type.MINUS)
                || lookahead.getType().equals(Type.MULTIPLY)
                || lookahead.getType().equals(Type.DIVIDE)) {
            termPrime(identifierValue);
            TypeLabelReturn aepResult = additiveExpressionPrime(identifierValue);
            TypeLabelReturn relopResult = relopExpression(aepResult.label);
            return new TypeLabelReturn(relopResult.type, aepResult.label);
        }
        return new TypeLabelReturn(null, null);
    }

    private String varArr(Token identifier) {
        if (lookahead.getType().equals(Type.LEFT_BRACKET)) {
            match(Type.LEFT_BRACKET);
            String dispValue = Integer.toString(4 * Integer.parseInt(lookahead.getValue()));
            String tempValue = "t" + tempVariableIndex;
            TypeLabelReturn expType = expression();
            if (expType.type != null && !expType.type.equals(Type.NUM)) {
                reject();
            }
            match(Type.RIGHT_BRACKET);
            quadruples.add(new String[]{"disp", identifier.getValue(), dispValue, tempValue});
            tempVariableIndex++;
            return tempValue;
        }
        return identifier.getValue();
    }

    private TypeLabelReturn relopExpression(String label) {
        Type lookaheadType = lookahead.getType();
        if (lookahead.getType().equals(Type.EQUAL_TO)
                || lookahead.getType().equals(Type.EQUAL_GREATER)
                || lookahead.getType().equals(Type.EQUAL_LESS)
                || lookahead.getType().equals(Type.GREATER_THAN)
                || lookahead.getType().equals(Type.LESS_THAN)
                || lookahead.getType().equals(Type.NOT_EQUAL)) {
            relop();
            TypeLabelReturn aeResult = additiveExpression();
            String tempVariable = "t" + tempVariableIndex;
            tempVariableIndex++;
            quadruples.add(new String[]{"comp", label, aeResult.label,tempVariable});
            return new TypeLabelReturn(lookaheadType, tempVariable);
        }
        return new TypeLabelReturn(null, label);
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

    private TypeLabelReturn additiveExpression() {
        if (lookahead.getType().equals(Type.ID)) {
            Type lookaheadType = findIdentifierInStack(lookahead);
            Token identifier = lookahead;
            match(Type.ID);
            call(identifier);
            TypeLabelReturn termPrimeResponse = termPrime(identifier.getValue());
            if (!lookaheadType.equals(termPrimeResponse.type)) {
                reject();
            }
            TypeLabelReturn aepResponse = additiveExpressionPrime(identifier.getValue());
            if (aepResponse.type != null && !lookaheadType.equals(aepResponse.type)) {
                reject();
            }
            return new TypeLabelReturn(lookaheadType, "");
        } else if (lookahead.getType().equals(Type.NUM)) {
            String identifierValue = lookahead.getValue();
            match(Type.NUM);
            TypeLabelReturn tpResponse = termPrime(identifierValue);
            TypeLabelReturn aepResponse = additiveExpressionPrime(identifierValue);
            String label = aepResponse.label != null ? aepResponse.label : tpResponse.label;
            return new TypeLabelReturn(Type.NUM, label);
        } else if (lookahead.getType().equals(Type.FLOAT)) {
            String identifierValue = lookahead.getValue();
            match(Type.FLOAT);
            termPrime(identifierValue);
            additiveExpressionPrime(identifierValue);
            return new TypeLabelReturn(Type.FLOAT, "");
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            match(Type.LEFT_PAREN);
            TypeLabelReturn expressionResponse = expression();
            match(Type.RIGHT_PAREN);
            termPrime(expressionResponse.label);
            additiveExpressionPrime("");
            return new TypeLabelReturn(expressionResponse.type, "");
        }
        return new TypeLabelReturn(null, null);
    }

    private TypeLabelReturn additiveExpressionPrime(String previousValue) {
        if (lookahead.getType().equals(Type.PLUS)) {
            addop();
            TypeLabelReturn typeLabelReturn = term();
            String newIndex = "t" + tempVariableIndex;
            quadruples.add(new String[]{"add", previousValue, typeLabelReturn.label, newIndex});
            tempVariableIndex++;

            TypeLabelReturn aepResponse = additiveExpressionPrime(newIndex);
            if (aepResponse.type != null && !aepResponse.type.equals(typeLabelReturn.type)) {
                reject();
            }
            return new TypeLabelReturn(typeLabelReturn.type, aepResponse.label);
        } else if (lookahead.getType().equals(Type.MINUS)) {
            addop();
            TypeLabelReturn typeLabelReturn = term();
            String newIndex = "t" + tempVariableIndex;
            quadruples.add(new String[]{"sub", previousValue, typeLabelReturn.label, newIndex});
            tempVariableIndex++;

            Type aepType = additiveExpressionPrime(newIndex).type;
            if (aepType != null && !aepType.equals(typeLabelReturn.type)) {
                System.out.println("rejected here");
                reject();
            }
            return new TypeLabelReturn(typeLabelReturn.type, newIndex);
        }
        return new TypeLabelReturn(null, previousValue);
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
            termPrime(identifier.getValue());
            return new TypeLabelReturn(identifier.getType(), identifier.getValue());
        } else if (lookahead.getType().equals(Type.NUM)) {
            Token identifier = lookahead;
            match(Type.NUM);
            TypeLabelReturn tpResponse = termPrime(identifier.getValue());
            return new TypeLabelReturn(Type.NUM, tpResponse.label);
        } else if (lookahead.getType().equals(Type.FLOAT)) {
            Token identifier = lookahead;
            match(Type.FLOAT);
            TypeLabelReturn tpResponse = termPrime(identifier.getValue());
            return new TypeLabelReturn(Type.FLOAT, tpResponse.label);
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            match(Type.LEFT_PAREN);
            TypeLabelReturn expressionResult = expression();
            match(Type.RIGHT_PAREN);
            termPrime(expressionResult.label);
            return expressionResult;
        }
        return null;
    }

    private TypeLabelReturn termPrime(String previousValue) {
        if (lookahead.getType().equals(Type.MULTIPLY)) {
            mulop();
            TypeLabelReturn factorResult = factor();
            String tempVariable = "t" + tempVariableIndex;
            tempVariableIndex++;
            quadruples.add(new String[]{"mult", previousValue, factorResult.label, tempVariable});
            TypeLabelReturn tpResult = termPrime(tempVariable);
            return new TypeLabelReturn(factorResult.type, tpResult.label);
        } else if (lookahead.getType().equals(Type.DIVIDE)) {
            mulop();
            TypeLabelReturn factorResult = factor();
            String tempVariable = "t" + tempVariableIndex;
            tempVariableIndex++;
            quadruples.add(new String[]{"div", previousValue, factorResult.label, tempVariable});
            TypeLabelReturn tpResult = termPrime(tempVariable);
            return new TypeLabelReturn(factorResult.type, tpResult.label);
        }
        return new TypeLabelReturn(null, previousValue);
    }

    private TypeLabelReturn factor() {
        if (lookahead.getType().equals(Type.ID)) {
            Type expressionType = findIdentifierInStack(lookahead);
            Token identifier = lookahead;
            match(Type.ID);
            call(identifier);
            return new TypeLabelReturn(expressionType, identifier.getValue());
        } else if (lookahead.getType().equals(Type.NUM)) {
            Token identifier = lookahead;
            match(Type.NUM);
            return new TypeLabelReturn(Type.NUM, identifier.getValue());
        } else if (lookahead.getType().equals(Type.FLOAT)) {
            Token identifier = lookahead;
            match(Type.FLOAT);
            return new TypeLabelReturn(Type.FLOAT, identifier.getValue());
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            match(Type.LEFT_PAREN);
            TypeLabelReturn expressionResponse = expression();
            match(Type.RIGHT_PAREN);
            return expressionResponse;
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
            quadruples.add(new String[]{"call", identifier.getValue(), Integer.toString(arguments.size()), "t"+tempVariableIndex});
            tempVariableIndex++;
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
            Type expType = expression().type;
            symbolArrayList.add(new Symbol(false, "", expType));
            symbolArrayList = argListPrime(symbolArrayList);
            return symbolArrayList;
        } else if (lookahead.getType().equals(Type.NUM)) {
            Type expType = expression().type;
            symbolArrayList.add(new Symbol(false, "", Type.NUM));
            symbolArrayList = argListPrime(symbolArrayList);
            return symbolArrayList;
        } else if (lookahead.getType().equals(Type.FLOAT)) {
            Type expType = expression().type;
            symbolArrayList.add(new Symbol(false, "", Type.FLOAT));
            symbolArrayList = argListPrime(symbolArrayList);
            return symbolArrayList;
        } else if (lookahead.getType().equals(Type.LEFT_PAREN)) {
            Type expType = expression().type;
            symbolArrayList.add(new Symbol(false, "", expType));
            symbolArrayList = argListPrime(symbolArrayList);
            return symbolArrayList;
        }
        return symbolArrayList;
    }

    private ArrayList<Symbol> argListPrime(ArrayList<Symbol> symbolArrayList) {
        if (lookahead.getType().equals(Type.COMMA)) {
            match(Type.COMMA);
            Type expType = expression().type;
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

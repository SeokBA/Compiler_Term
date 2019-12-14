package listener.main.bytecode;

import generated.MiniCBaseListener;
import generated.MiniCParser;
import generated.MiniCParser.ParamsContext;
import listener.main.TranslationGUI;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import static listener.main.bytecode.BytecodeGenListenerHelper.*;
import static listener.main.bytecode.BytecodeSymbolTable.Type;

public class BytecodeGenListener extends MiniCBaseListener implements ParseTreeListener {
    ParseTreeProperty<String> newTexts = new ParseTreeProperty<String>();
    BytecodeSymbolTable symbolTable = new BytecodeSymbolTable();
    TranslationGUI.setAddressListener setAddressListener;

    int tab = 0;

    public void setGUI(TranslationGUI.setAddressListener setAddressListener) {
        this.setAddressListener = setAddressListener;
    }

    @Override
    public void exitProgram(MiniCParser.ProgramContext ctx) {
        String classProlog = getFunProlog();
        String programStr;
        String fun_decl = "", var_decl = "";

        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (isFunDecl(ctx, i))
                fun_decl += newTexts.get(ctx.decl(i));
            else
                var_decl += newTexts.get(ctx.decl(i));
        }

        newTexts.put(ctx, classProlog + var_decl + fun_decl);

        // 변수 id <= 3에 해당하는 명령문 '_' 처리
        programStr = newTexts.get(ctx);
        for (int i = 0; i < 4; i++) {
            programStr = programStr.replace("ldc " + i + "\n", "iconst_" + i + "\n");
            programStr = programStr.replace("iload " + i + "\n", "iload_" + i + "\n");
            programStr = programStr.replace("istore " + i + "\n", "istore_" + i + "\n");
        }
        System.out.println(programStr);
        if (setAddressListener != null)
            setAddressListener.outputData = programStr;
    }

    // decl	: var_decl | fun_decl
    @Override
    public void exitDecl(MiniCParser.DeclContext ctx) {
        String decl = "";
        if (ctx.getChildCount() == 1) {
            if (ctx.var_decl() != null)                //var_decl
                decl += newTexts.get(ctx.var_decl());
            else                            //fun_decl
                decl += newTexts.get(ctx.fun_decl());
        }
        newTexts.put(ctx, decl);
    }

    // var_decl	: type_spec IDENT ';' | type_spec IDENT '=' LITERAL ';'|type_spec IDENT '[' LITERAL ']' ';'
    @Override
    public void enterVar_decl(MiniCParser.Var_declContext ctx) {
        String varName = ctx.IDENT().getText();

        if (isArrayDecl(ctx)) {
            symbolTable.putGlobalArr(varName, Type.INTARRAY, Integer.parseInt(ctx.LITERAL().getText()));
        } else if (isDeclWithInit(ctx)) {
            symbolTable.putGlobalVarWithInitVal(varName, Type.INT, initVal(ctx));
        } else { // simple decl
            symbolTable.putGlobalVar(varName, Type.INT);
        }
    }

    @Override
    public void exitVar_decl(MiniCParser.Var_declContext ctx) {
        String varName = ctx.IDENT().getText();
        String varDecl = "";

        if (isDeclWithInit(ctx)) {
            varDecl += "putfield " + varName + "\n";
            // v. initialization => Later! skip now..:
        }
        newTexts.put(ctx, varDecl);
    }

    @Override
    public void enterLocal_decl(MiniCParser.Local_declContext ctx) {
        if (isArrayDecl(ctx)) {
            symbolTable.putLocalVar(getLocalVarName(ctx), Type.INTARRAY);
        } else if (isDeclWithInit(ctx)) {
            symbolTable.putLocalVarWithInitVal(getLocalVarName(ctx), Type.INT, initVal(ctx));
        } else { // simple decl
            symbolTable.putLocalVar(getLocalVarName(ctx), Type.INT);
        }
    }

    @Override
    public void exitLocal_decl(MiniCParser.Local_declContext ctx) {
        String varDecl = "", literalStr = "";

        if (isDeclWithInit(ctx)) {
            String vId = symbolTable.getVarId(ctx);
            literalStr = ctx.LITERAL().getText();
            literalStr = "ldc " + literalStr;
            varDecl += literalStr + "\n"
                    + "istore " + vId + "\n";
        }

        newTexts.put(ctx, varDecl);
    }

    // program	: decl+
    @Override
    public void enterFun_decl(MiniCParser.Fun_declContext ctx) {
        symbolTable.initFunDecl();

        String fname = getFunName(ctx);
        ParamsContext params;

        if (fname.equals("main")) { // 메인 함수
            symbolTable.putLocalVar("args", Type.INTARRAY);
        } else { // 일반 함수
            symbolTable.putFunSpecStr(ctx);
            params = (ParamsContext) ctx.getChild(3);
            symbolTable.putParams(params);
        }
    }

    @Override
    public void exitFun_decl(MiniCParser.Fun_declContext ctx) {
        // <(2) Fill here!>
        String fun_decl = funcHeader(ctx, getFunName(ctx)) + newTexts.get(ctx.compound_stmt());
        if (ctx.compound_stmt().stmt(ctx.compound_stmt().stmt().size() - 1).return_stmt() == null)
            fun_decl += "return\n";
        String temp = fun_decl;
        fun_decl = "";
        for (int i = 0; i < temp.length(); i++) {
            fun_decl += temp.charAt(i);
            if (temp.charAt(i) == '\n' && i != temp.length() - 1) {
                fun_decl += "\t";
            }
        }
        newTexts.put(ctx, fun_decl + ".end method\n");
    }

    private String funcHeader(MiniCParser.Fun_declContext ctx, String fname) { // function header
        return ".method public static " + symbolTable.getFunSpecStr(fname) + "\n"
                + ".limit stack " + getStackSize(ctx) + "\n"
                + ".limit locals " + getLocalVarSize(ctx) + "\n";
    }

    // params		: param (',' param)* | VOID |			;
    // param		: type_spec IDENT | type_spec IDENT '[' ']'	;

    // stmt	: expr_stmt | compound_stmt | if_stmt | while_stmt | return_stmt
    @Override
    public void exitStmt(MiniCParser.StmtContext ctx) {
        String stmt = "";
        if (ctx.getChildCount() > 0) {
            if (ctx.expr_stmt() != null)                // expr_stmt
                stmt += newTexts.get(ctx.expr_stmt());
            else if (ctx.compound_stmt() != null)    // compound_stmt
                stmt += newTexts.get(ctx.compound_stmt());
                // <(0) Fill here>
            else if (ctx.if_stmt() != null)
                stmt += newTexts.get(ctx.if_stmt());
            else if (ctx.while_stmt() != null)
                stmt += newTexts.get(ctx.while_stmt());
            else
                stmt += newTexts.get(ctx.return_stmt());
        }
        newTexts.put(ctx, stmt);
    }

    // expr_stmt	: expr ';'
    @Override
    public void exitExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
        String stmt = "";
        if (ctx.getChildCount() == 2) {
            stmt += newTexts.get(ctx.expr());    // expr
        }
        newTexts.put(ctx, stmt);
    }

    // compound_stmt	: '{' local_decl* stmt* '}'
    @Override
    public void exitCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
        // <(3) Fill here>
        String compoundStr = "";

        for (MiniCParser.Local_declContext i : ctx.local_decl()) {
            if (!newTexts.get(i).equals(""))
                compoundStr += newTexts.get(i);
        }
        for (MiniCParser.StmtContext i : ctx.stmt()) {
            if (!newTexts.get(i).equals(""))
                compoundStr += newTexts.get(i);
        }
        newTexts.put(ctx, compoundStr);
    }

    // if_stmt	: IF '(' expr ')' stmt | IF '(' expr ')' stmt ELSE stmt;
    @Override
    public void exitIf_stmt(MiniCParser.If_stmtContext ctx) {
        String stmt = "";
        String condExpr = newTexts.get(ctx.expr());
        String thenStmt = newTexts.get(ctx.stmt(0));

        String lend = symbolTable.newLabel();
        String lelse = symbolTable.newLabel();

        if (noElse(ctx)) {
            stmt += condExpr
                    + "ifeq " + lend + "\n"
                    + thenStmt
                    + lend + ":" + "\n";
        } else {
            String elseStmt = newTexts.get(ctx.stmt(1));
            stmt += condExpr
                    + "ifeq " + lelse + "\n"
                    + thenStmt
                    + "goto " + lend + "\n"
                    + lelse + ": " + "\n"
                    + elseStmt
                    + lend + ":" + "\n";
        }

        newTexts.put(ctx, stmt);
    }

    // while_stmt	: WHILE '(' expr ')' stmt
    @Override
    public void exitWhile_stmt(MiniCParser.While_stmtContext ctx) {
        // <(1) Fill here!>
        String stmt = "";
        String condExpr = newTexts.get(ctx.expr());
        String thenStmt = newTexts.get(ctx.stmt());
        String lRe = symbolTable.newLabel();
        String lBreak = symbolTable.newLabel();

        stmt += lRe + ":\n"
                + condExpr
                + "ifeq " + lBreak + "\n"
                + thenStmt
                + "goto " + lRe + "\n"
                + lBreak + ":\n";
        newTexts.put(ctx, stmt);
    }

    // return_stmt	: RETURN ';' | RETURN expr ';'
    @Override
    public void exitReturn_stmt(MiniCParser.Return_stmtContext ctx) {
        // <(4) Fill here>
        String returnStr;
        if (ctx.getChildCount() == 3)
            returnStr = newTexts.get(ctx.expr()) + "i" + ctx.RETURN().toString() + "\n";
        else
            returnStr = ctx.RETURN().toString() + "\n";

        newTexts.put(ctx, returnStr);
    }

    @Override
    public void exitExpr(MiniCParser.ExprContext ctx) {
        String expr = "";

        if (ctx.getChildCount() <= 0) {
            newTexts.put(ctx, "");
            return;
        }

        if (ctx.getChildCount() == 1) { // IDENT | LITERAL
            if (ctx.IDENT() != null) {
                String idName = ctx.IDENT().getText();
                if (symbolTable.getVarType(idName) == Type.INT) {
                    expr += "iload " + symbolTable.getVarId(idName) + "\n";
                } else    // Type int array
                    expr += "iaload " + symbolTable.getVarId(idName) + "\n";
            } else if (ctx.LITERAL() != null) {
                String literalStr = ctx.LITERAL().getText();
                expr += "ldc " + literalStr + " \n";
            }
        } else if (ctx.getChildCount() == 2) { // UnaryOperation
            expr = handleUnaryExpr(ctx, newTexts.get(ctx) + expr);
        } else if (ctx.getChildCount() == 3) {
            if (ctx.getChild(0).getText().equals("(")) {        // '(' expr ')'
                expr = newTexts.get(ctx.expr(0));

            } else if (ctx.getChild(1).getText().equals("=")) {    // IDENT '=' expr
                expr = newTexts.get(ctx.expr(0))
                        + "istore " + symbolTable.getVarId(ctx.IDENT().getText()) + "\n";

            } else {                                            // binary operation
                expr = handleBinExpr(ctx, expr);

            }
        }
        // IDENT '(' args ')' |  IDENT '[' expr ']'
        else if (ctx.getChildCount() == 4) {
            if (ctx.args() != null) {        // function calls
                expr = handleFunCall(ctx, expr);
            } else { // expr
                // Arrays
                String arrStr = ctx.IDENT().getText();
                expr = "iaload " + symbolTable.getVarId(arrStr) + "\n" +
                        "ldc " + newTexts.get(ctx.expr(0)) + "\n";
            }
        }
        // IDENT '[' expr ']' '=' expr
        else { // Arrays
            String arrStr = ctx.IDENT().getText();
            expr = "iaload " + symbolTable.getVarId(arrStr) + "\n" +
                    "ldc " + newTexts.get(ctx.expr(0)) + "\n" +
                    "ldc " + newTexts.get(ctx.expr(1)) + "\n" +
                    "iastore\n";
        }
        newTexts.put(ctx, expr);
    }

    private String handleUnaryExpr(MiniCParser.ExprContext ctx, String expr) { // op a
        String l1 = symbolTable.newLabel();
        String l2 = symbolTable.newLabel();
        String lend = symbolTable.newLabel();
        if (expr.equals("null"))
            expr = "";
        expr += newTexts.get(ctx.expr(0));
        switch (ctx.getChild(0).getText()) {
            case "-":
                expr += "ineg \n";
                break;
            case "--":
                expr = "iinc " + symbolTable.getVarId(ctx.expr(0).IDENT().getText()) + " -1 \n";
                break;
            case "++":
                expr = "iinc " + symbolTable.getVarId(ctx.expr(0).IDENT().getText()) + " 1 \n";
                break;
            case "!":
                expr += "ifeq " + l2 + "\n"
                        + l1 + ": \n"
                        + "iconst_0" + "\n"
                        + "goto " + lend + "\n"
                        + l2 + ": \n"
                        + "iconst_1" + "\n"
                        + lend + ": " + "\n";
                break;
        }
        return expr;
    }


    private String handleBinExpr(MiniCParser.ExprContext ctx, String expr) { // a op b
        String l2 = symbolTable.newLabel();
        String lend = symbolTable.newLabel();

        expr += newTexts.get(ctx.expr(0));
        expr += newTexts.get(ctx.expr(1));

        switch (ctx.getChild(1).getText()) {
            case "*":
                expr += "imul \n";
                break;
            case "/":
                expr += "idiv \n";
                break;
            case "%":
                expr += "irem \n";
                break;
            case "+":        // expr(0) expr(1) iadd
                expr += "iadd \n";
                break;
            case "-":
                expr += "isub \n";
                break;

            case "==":
                expr += "isub " + "\n"
                        + "ifeq " + l2 + "\n"
                        + "iconst_0" + "\n"
                        + "goto " + lend + "\n"
                        + l2 + ": \n"
                        + "iconst_1" + "\n"
                        + lend + ": " + "\n";
                break;
            case "!=":
                expr += "isub " + "\n"
                        + "ifne " + l2 + "\n"
                        + "iconst_0" + "\n"
                        + "goto " + lend + "\n"
                        + l2 + ": \n"
                        + "iconst_1" + "\n"
                        + lend + ": " + "\n";
                break;
            case "<=":
                // <(5) Fill here>
                expr += "isub " + "\n"
                        + "ifle " + l2 + "\n"
                        + "iconst_0" + "\n"
                        + "goto " + lend + "\n"
                        + l2 + ": \n"
                        + "iconst_1" + "\n"
                        + lend + ": " + "\n";
                break;
            case "<":
                // <(6) Fill here>
                expr += "isub " + "\n"
                        + "iflt " + l2 + "\n"
                        + "iconst_0" + "\n"
                        + "goto " + lend + "\n"
                        + l2 + ": \n"
                        + "iconst_1" + "\n"
                        + lend + ": " + "\n";
                break;

            case ">=":
                // <(7) Fill here>
                expr += "isub " + "\n"
                        + "ifge " + l2 + "\n"
                        + "iconst_0" + "\n"
                        + "goto " + lend + "\n"
                        + l2 + ": \n"
                        + "iconst_1" + "\n"
                        + lend + ": " + "\n";
                break;

            case ">":
                // <(8) Fill here>
                expr += "isub " + "\n"
                        + "ifgt " + l2 + "\n"
                        + "iconst_0" + "\n"
                        + "goto " + lend + "\n"
                        + l2 + ": \n"
                        + "iconst_1" + "\n"
                        + lend + ": " + "\n";
                break;

            case "and":
                expr += "ifne " + lend + "\n"
                        + "pop" + "\n" + "iconst_0" + "\n"
                        + lend + ": " + "\n";
                break;
            case "or":
                // <(9) Fill here>
                expr += "ifeq " + lend + "\n"
                        + "pop" + "\n" + "iconst_1" + "\n"
                        + lend + ": " + "\n";
                break;
        }
        return expr;
    }

    private String handleFunCall(MiniCParser.ExprContext ctx, String expr) {
        String fname = getFunName(ctx);

        if (fname.equals("_print")) {        // System.out.println
            expr = "getstatic java/lang/System/out Ljava/io/PrintStream; " + "\n"
                    + newTexts.get(ctx.args())
                    + "invokevirtual " + symbolTable.getFunSpecStr("_print") + "\n";
        } else {
            expr = newTexts.get(ctx.args())
                    + "invokestatic " + getCurrentClassName() + "/" + symbolTable.getFunSpecStr(fname) + "\n";
        }

        return expr;

    }

    // args	: expr (',' expr)* | ;
    @Override
    public void exitArgs(MiniCParser.ArgsContext ctx) {
        String argsStr = "";
        for (int i = 0; i < ctx.expr().size(); i++) {
            argsStr += newTexts.get(ctx.expr(i));
        }
        newTexts.put(ctx, argsStr);
    }
}

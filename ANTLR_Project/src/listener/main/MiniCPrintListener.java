package listener.main;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

import generated.*;

public class MiniCPrintListener extends MiniCBaseListener {
    ParseTreeProperty<String> newTexts = new ParseTreeProperty<String>();

    boolean isBinaryOperation(MiniCParser.ExprContext ctx) {
        return ctx.getChildCount() == 3 && ctx.getChild(1) != ctx.expr();
    }

    boolean isLiteral(MiniCParser.ExprContext ctx) {
        return ctx.getChildCount() == 1 && ctx.getChild(0) == ctx.LITERAL();
    }

    boolean isIdent(MiniCParser.ExprContext ctx) {
        return ctx.getChildCount() == 1 && ctx.getChild(0) == ctx.IDENT();
    }

    boolean isPrefix(MiniCParser.ExprContext ctx) {
        return ctx.getChildCount() == 2 && ctx.getChild(0) != ctx.expr();
    }

    boolean isIdentFirst(MiniCParser.ExprContext ctx) {
        return ctx.getChild(0) == ctx.IDENT();
    }

    boolean isArrOrArgs(MiniCParser.ExprContext ctx) {
        return ctx.getChildCount() == 4;
    }

    boolean isParentheses(MiniCParser.ExprContext ctx) {
        return ctx.getChildCount() == 3 && ctx.getChild(1) == ctx.expr(0);
    }


    @Override
    public void exitProgram(MiniCParser.ProgramContext ctx) { // decl+
        String declStr = "";
        for (MiniCParser.DeclContext i : ctx.decl())
            declStr += newTexts.get(i);
        newTexts.put(ctx, declStr);
        System.out.print(newTexts.get(ctx)); // Print pretty code
    }

    @Override
    public void exitDecl(MiniCParser.DeclContext ctx) {
        if (ctx.getChild(0) == ctx.var_decl()) { // var_decl
            newTexts.put(ctx, newTexts.get(ctx.var_decl()));
        } else { // fun_decl
            newTexts.put(ctx, newTexts.get(ctx.fun_decl()));
        }
    }

    @Override
    public void exitVar_decl(MiniCParser.Var_declContext ctx) {
        String type, ident, literal;
        type = newTexts.get(ctx.type_spec());
        ident = ctx.IDENT().getText();

        if (ctx.getChildCount() == 3) { // type_spec IDENT ';'
            newTexts.put(ctx, type + " " + ident + ";\n");
        } else if (ctx.getChildCount() == 5) { // type_spec IDENT '=' LITERAL ';'
            literal = ctx.LITERAL().getText();
            newTexts.put(ctx, type + " " + ident + " = " + literal + ";\n");
        } else { // type_spec IDENT '[' LITERAL ']' ';'
            literal = ctx.LITERAL().getText();
            newTexts.put(ctx, type + " " + ident + "[" + literal + "];\n");
        }
    }

    @Override
    public void exitType_spec(MiniCParser.Type_specContext ctx) { // VOID, INT
        newTexts.put(ctx, ctx.getText());
    }

    @Override
    public void exitFun_decl(MiniCParser.Fun_declContext ctx) { // type_spec IDENT '(' params ')' compound_stmt
        String type, ident, params, compountStmt;
        type = newTexts.get(ctx.type_spec());
        ident = ctx.IDENT().getText();
        params = newTexts.get(ctx.params());
        compountStmt = newTexts.get(ctx.compound_stmt());
        newTexts.put(ctx, type + " " + ident + " (" + params + ") " + compountStmt + "\n");
    }

    @Override
    public void exitParams(MiniCParser.ParamsContext ctx) {
        if (ctx.getChild(0) == null) { // null
            newTexts.put(ctx, "");
        } else if (ctx.getChild(0) == ctx.VOID()) { // VOID
            newTexts.put(ctx, ctx.VOID().getText());
        } else { // param (',' param)*
            String paramsStr = "";
            int size = ctx.getChildCount();
            for (MiniCParser.ParamContext i : ctx.param()) {
                paramsStr += i.getText();
                if (i == ctx.param(size - 1))
                    paramsStr += ", ";
            }
            newTexts.put(ctx, paramsStr);
        }
    }

    @Override
    public void exitParam(MiniCParser.ParamContext ctx) {
        String type, ident;
        type = newTexts.get(ctx.type_spec());
        ident = ctx.IDENT().getText();

        if (ctx.getChildCount() == 2) { // type_spec IDENT
            newTexts.put(ctx, type + " " + ident);
        } else { // type_spec IDENT '[' ']'
            newTexts.put(ctx, type + " " + ident + "[]");
        }
    }

    @Override
    public void exitStmt(MiniCParser.StmtContext ctx) {
        String stmt;

        if (ctx.getChild(0) == ctx.expr_stmt()) { // expr_stmt
            stmt = newTexts.get(ctx.expr_stmt());
        } else if (ctx.getChild(0) == ctx.compound_stmt()) { // compound_stmt
            stmt = newTexts.get(ctx.compound_stmt());
        } else if (ctx.getChild(0) == ctx.if_stmt()) { // if_stmt
            stmt = newTexts.get(ctx.if_stmt());
        } else if (ctx.getChild(0) == ctx.while_stmt()) { // while_stmt
            stmt = newTexts.get(ctx.while_stmt());
        } else { // return_stmt
            stmt = newTexts.get(ctx.return_stmt());
        }
        newTexts.put(ctx, stmt);
    }

    @Override
    public void exitExpr_stmt(MiniCParser.Expr_stmtContext ctx) { // expr ';'
        String expr = newTexts.get(ctx.expr());
        newTexts.put(ctx, expr + ";\n");
    }

    @Override
    public void exitWhile_stmt(MiniCParser.While_stmtContext ctx) { // WHILE '(' expr ')' stmt
        String wh, expr, stmt;
        wh = ctx.WHILE().getText();
        expr = newTexts.get(ctx.expr());
        stmt = newTexts.get(ctx.stmt());
        if(ctx.stmt().getChild(0) != ctx.stmt().compound_stmt()){
            String temp = "", tap = "....";
            if (ctx.stmt().getChild(0) == ctx.stmt().expr_stmt()){
                stmt = "\n...." + stmt;
            }
            else {
                temp += "\n" + tap;
                for (int j = 0; j < stmt.length(); j++) {
                    temp += stmt.charAt(j);
                    if (stmt.charAt(j) == '\n' && j != stmt.length() - 1) {
                        temp += tap;
                    }
                }
                stmt = temp;
            }
        }
        newTexts.put(ctx, wh + "(" + expr + ")" + stmt);
    }

    @Override
    public void exitCompound_stmt(MiniCParser.Compound_stmtContext ctx) { // '{' local_decl* stmt* '}'
        String compoundStr = "", tap = "....";

        for (MiniCParser.Local_declContext i : ctx.local_decl()) {
            compoundStr += tap + newTexts.get(i);
        }
        for (MiniCParser.StmtContext i : ctx.stmt()) {
            String temp = newTexts.get(i);
            if (i.getChild(0) == i.expr_stmt()){
                compoundStr += tap + temp;
            }
            else {
                compoundStr += tap;
                for (int j = 0; j < temp.length(); j++) {
                    compoundStr += temp.charAt(j);
                    if (temp.charAt(j) == '\n' && j != temp.length() - 1) {
                        compoundStr += tap;
                    }
                }
            }
        }
        newTexts.put(ctx, "\n{\n" + compoundStr + "}\n");
    }

    @Override
    public void exitLocal_decl(MiniCParser.Local_declContext ctx) {
        String type, ident, literal;
        type = newTexts.get(ctx.type_spec());
        ident = ctx.IDENT().getText();

        if (ctx.getChildCount() == 3) { // type_spec IDENT ';'
            newTexts.put(ctx, type + " " + ident + ";\n");
        } else if (ctx.getChildCount() == 5) { // type_spec IDENT '=' LITERAL ';'
            literal = ctx.LITERAL().getText();
            newTexts.put(ctx, type + " " + ident + " = " + literal + ";\n");
        } else { // type_spec IDENT '[' LITERAL ']' ';'
            literal = ctx.LITERAL().getText();
            newTexts.put(ctx, type + " " + ident + "[" + literal + "];\n");
        }
    }

    @Override
    public void exitIf_stmt(MiniCParser.If_stmtContext ctx) {
        String ifStmt, expr, stmt1, stmt2, elStmt;
        ifStmt = ctx.IF().getText();
        expr = newTexts.get(ctx.expr());
        stmt1 = newTexts.get(ctx.stmt(0));

        if(ctx.stmt(0).getChild(0) != ctx.stmt(0).compound_stmt()){
            String temp = "", tap = "....";
            if (ctx.stmt(0).getChild(0) == ctx.stmt(0).expr_stmt()){
                stmt1 = "\n...." + stmt1;
            }
            else {
                temp += "\n" + tap;
                for (int j = 0; j < stmt1.length(); j++) {
                    temp += stmt1.charAt(j);
                    if (stmt1.charAt(j) == '\n' && j != stmt1.length() - 1) {
                        temp += tap;
                    }
                }
                stmt1 = temp;
            }
        }

        if (ctx.getChildCount() == 5) { // IF '(' expr ')' stmt
            newTexts.put(ctx, ifStmt + " (" + expr + ") " + stmt1);
        } else { // IF '(' expr ')' stmt ELSE stmt
            elStmt = ctx.ELSE().getText();
            stmt2 = newTexts.get(ctx.stmt(1));

            if(ctx.stmt(1).getChild(0) != ctx.stmt(1).compound_stmt()){
                String temp = "", tap = "....";
                if (ctx.stmt(1).getChild(0) == ctx.stmt(1).expr_stmt()){
                    stmt2 = "\n...." + stmt2;
                }
                else {
                    temp += "\n" + tap;
                    for (int j = 0; j < stmt2.length(); j++) {
                        temp += stmt2.charAt(j);
                        if (stmt2.charAt(j) == '\n' && j != stmt2.length() - 1) {
                            temp += tap;
                        }
                    }
                    stmt2 = temp;
                }
            }
            newTexts.put(ctx, ifStmt + " (" + expr + ") " + stmt1 + elStmt + stmt2);
        }
    }

    @Override
    public void exitReturn_stmt(MiniCParser.Return_stmtContext ctx) {
        String retn, expr;
        retn = ctx.RETURN().getText();

        if (ctx.getChildCount() == 2) { // RETURN ';'
            newTexts.put(ctx, retn + ";\n");
        } else { // RETURN expr ';'
            expr = newTexts.get(ctx.expr());
            newTexts.put(ctx, retn + expr + ";\n");
        }
    }

    @Override
    public void exitExpr(MiniCParser.ExprContext ctx) {
        String s1 = null, s2 = null, s3 = null, op = null;

        if (isIdent(ctx)) { // IDENT
            newTexts.put(ctx, ctx.IDENT().getText());
        } else if (isLiteral(ctx)) { // LITERAL
            newTexts.put(ctx, ctx.LITERAL().getText());
        }
        else if(isParentheses(ctx)){ // '(' expr ')'
            s1 = newTexts.get(ctx.expr(0));
            newTexts.put(ctx, "(" + s1 + ")");
        }
        else if (isBinaryOperation(ctx)) { // x op x
            if(isIdentFirst(ctx)){ // IDENT op expr
                s1 = ctx.IDENT().getText();
                s2 = newTexts.get(ctx.expr(0));
            }
            else { // expr op expr
                s1 = newTexts.get(ctx.expr(0));
                s2 = newTexts.get(ctx.expr(1));
            }
            op = ctx.getChild(1).getText();
            newTexts.put(ctx, s1 + " " + op + " " + s2);
        }



        else if (isPrefix(ctx)) { // op expr
            s1 = newTexts.get(ctx.expr(0));
            op = ctx.getChild(0).getText();
            newTexts.put(ctx, op + s1);
        }

        else if(isArrOrArgs(ctx)){
            s1 = ctx.IDENT().getText();
            if(ctx.getChild(2) == ctx.expr(0)){ // IDENT '[' expr ']'
                s2 = newTexts.get(ctx.expr(0));
            } else { // IDENT '(' args ')'
                s2 = newTexts.get(ctx.args());
            }
            newTexts.put(ctx, s1 + ctx.getChild(1) + s2 + ctx.getChild(3));
        }

        else { // IDENT '[' expr ']' '=' expr
            s1 = ctx.IDENT().getText();
            s2 = newTexts.get(ctx.expr(0));
            s3 = newTexts.get(ctx.expr(1));
            newTexts.put(ctx, s1 + "[" + s2 + "] = " + s3);
        }
    }

    @Override
    public void exitArgs(MiniCParser.ArgsContext ctx) {
        if (ctx.getChild(0) == null) { // null
            newTexts.put(ctx, "");
        } else { // expr (',' expr)*
            String exprStr = "";
            int size = ctx.getChildCount();
            for (MiniCParser.ExprContext i : ctx.expr()) {
                exprStr += newTexts.get(i);
                if (i != ctx.expr(size - 1))
                    exprStr += ", ";
            }
            newTexts.put(ctx, exprStr);
        }
    }
}

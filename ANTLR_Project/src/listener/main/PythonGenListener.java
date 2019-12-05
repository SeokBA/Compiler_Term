package listener.main;

import generated.MiniCBaseListener;
import generated.MiniCParser;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

public class PythonGenListener extends MiniCBaseListener implements ParseTreeListener {
    ParseTreeProperty<String> newTexts = new ParseTreeProperty<String>();
    SymbolTable symbolTable = new SymbolTable();
    TranslationGUI.setAddressListener setAddressListener;

    public void setGUI(TranslationGUI.setAddressListener setAddressListener) {
        this.setAddressListener = setAddressListener;
    }

    @Override
    public void exitProgram(MiniCParser.ProgramContext ctx) {
        StringBuilder programStr = new StringBuilder("");

        for (int i = 0; i < ctx.getChildCount(); i++) {
            programStr.append(newTexts.get(ctx.decl(i)));
        }
        if (setAddressListener != null)
            setAddressListener.outputData = programStr.toString();
        System.out.println(programStr.toString());
    }

    //    decl		: var_decl | fun_decl		;
    @Override
    public void exitDecl(MiniCParser.DeclContext ctx) {
        if (ctx.getChild(0) == ctx.var_decl())
            newTexts.put(ctx, newTexts.get(ctx.var_decl()));
        else
            newTexts.put(ctx, newTexts.get(ctx.fun_decl()));
    }

    //    var_decl	:  type_spec IDENT ';'
//            | type_spec IDENT '=' LITERAL ';'
//            | type_spec IDENT '[' LITERAL ']' ';'	;
    @Override
    public void exitVar_decl(MiniCParser.Var_declContext ctx) {
        StringBuilder stringBuilder = new StringBuilder();
        if (ctx.getChildCount() == 3) {
//            stringBuilder.append(newTexts.get(ctx.type_spec()));
            if (newTexts.get(ctx.type_spec()).equals("int")) {
                stringBuilder.append(ctx.IDENT().getText());
                stringBuilder.append(" = 0");
            } else {
                stringBuilder.append(ctx.IDENT().getText());
            }
        } else if (ctx.getChildCount() == 5) {
//            stringBuilder.append(newTexts.get(ctx.type_spec()));
            stringBuilder.append(ctx.IDENT().getText());
            stringBuilder.append(" = ");
            stringBuilder.append(ctx.LITERAL().getText());
        } else {
//            stringBuilder.append(newTexts.get(ctx.type_spec()));
            stringBuilder.append(ctx.IDENT().getText());
            stringBuilder.append(" = ");
            stringBuilder.append("[0] * ");
            stringBuilder.append(ctx.LITERAL().getText());
        }
        stringBuilder.append("\n");
        newTexts.put(ctx, stringBuilder.toString());
    }

    //    type_spec	: VOID
//		| INT
    @Override
    public void exitType_spec(MiniCParser.Type_specContext ctx) {
        if (ctx.getChild(0) == ctx.INT()) {
            newTexts.put(ctx, ctx.INT().getText());
        } else {
            newTexts.put(ctx, ctx.VOID().getText());
        }
    }

    //    fun_decl	: type_spec IDENT '(' params ')' compound_stmt ;
    @Override
    public void exitFun_decl(MiniCParser.Fun_declContext ctx) {
        StringBuilder stringBuilder = new StringBuilder();
        if (ctx.IDENT().getText().equals("main")) {
            stringBuilder.append("\nif __name__ == '__main__':");
        } else {
            stringBuilder.append("def ");
//            stringBuilder.append(newTexts.get(ctx.type_spec()));
            stringBuilder.append(ctx.IDENT().getText());
            stringBuilder.append("(");
            stringBuilder.append(newTexts.get(ctx.params()));
            stringBuilder.append("):");
        }
        stringBuilder.append(newTexts.get(ctx.compound_stmt()));
        newTexts.put(ctx, stringBuilder.toString());
    }

    //    params		: param (',' param)* | VOID  |			;
    @Override
    public void exitParams(MiniCParser.ParamsContext ctx) {
        StringBuilder stringBuilder = new StringBuilder();
//        System.out.println(ctx.getChild(0).getText());
        if (ctx.getChildCount() > 1) {
            stringBuilder.append(newTexts.get(ctx.param(0)));
            for (int i = 1; i < ctx.param().size(); i++) {
                stringBuilder.append(", ");
                stringBuilder.append(newTexts.get(ctx.param(i)));
            }
        }
        newTexts.put(ctx, stringBuilder.toString());

    }

    //    param		: type_spec IDENT
//		| type_spec IDENT '[' ']'	;
    @Override
    public void exitParam(MiniCParser.ParamContext ctx) {
//        if (ctx.getChildCount() == 2) {
//            newTexts.put(ctx, newTexts.get(ctx.type_spec()) + " " + ctx.IDENT().getText());
//        } else {
//            newTexts.put(ctx, newTexts.get(ctx.type_spec()) + " " + ctx.IDENT().getText() + " " + "[" + "]");
//        }
        newTexts.put(ctx, ctx.IDENT().getText());
    }

    //    stmt		: expr_stmt
//		| compound_stmt
//		| if_stmt
//		| while_stmt
//		| return_stmt			;
    @Override
    public void exitStmt(MiniCParser.StmtContext ctx) {
        if (ctx.getChild(0) == ctx.expr_stmt()) {
            newTexts.put(ctx, newTexts.get(ctx.expr_stmt()));
        } else if (ctx.getChild(0) == ctx.compound_stmt()) {
            newTexts.put(ctx, newTexts.get(ctx.compound_stmt()));
        } else if (ctx.getChild(0) == ctx.if_stmt()) {
            newTexts.put(ctx, newTexts.get(ctx.if_stmt()));
        } else if (ctx.getChild(0) == ctx.while_stmt()) {
            newTexts.put(ctx, newTexts.get(ctx.while_stmt()));
        } else {
            newTexts.put(ctx, newTexts.get(ctx.return_stmt()));
        }
    }

    //    expr_stmt	: expr ';'			;
    @Override
    public void exitExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
        newTexts.put(ctx, newTexts.get(ctx.expr()) + "\n");
    }

    //    while_stmt	: WHILE '(' expr ')' stmt	;
    @Override
    public void exitWhile_stmt(MiniCParser.While_stmtContext ctx) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ctx.WHILE());
        stringBuilder.append(" ");
        stringBuilder.append(newTexts.get(ctx.expr()));
        stringBuilder.append(":");
        if (ctx.stmt().getChild(0) == ctx.stmt().expr_stmt()) {
            stringBuilder.append("\n    ");
            stringBuilder.append(newTexts.get(ctx.stmt().getChild(0)));
        } else
            stringBuilder.append(newTexts.get(ctx.stmt()));
        newTexts.put(ctx, stringBuilder.toString());
    }

    //    compound_stmt: '{' local_decl* stmt* '}'	;
    @Override
    public void exitCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n");
        for (int i = 0; i < ctx.local_decl().size(); i++) {
            stringBuilder.append("    ");
            stringBuilder.append(newTexts.get(ctx.local_decl(i)));
        }
        for (int i = 0; i < ctx.stmt().size(); i++) {
            String temp = newTexts.get(ctx.stmt(i));
            stringBuilder.append("    ");
            for (int j = 0; j < temp.length(); j++) {
                if (temp.charAt(j) == '\n' && j != temp.length() - 1) {
                    if (j != 0)
                        stringBuilder.append("\n    ");

                } else
                    stringBuilder.append(temp.charAt(j));
            }
        }
        stringBuilder.append("\n");
        newTexts.put(ctx, stringBuilder.toString());
    }

    //    local_decl	: type_spec IDENT ';'
//            | type_spec IDENT '=' LITERAL ';'
//            | type_spec IDENT '[' LITERAL ']' ';'	;
    @Override
    public void exitLocal_decl(MiniCParser.Local_declContext ctx) {
        StringBuilder stringBuilder = new StringBuilder();
        if (ctx.getChildCount() == 3) {
//            stringBuilder.append(newTexts.get(ctx.type_spec()));
            if (newTexts.get(ctx.type_spec()).equals("int")) {
                stringBuilder.append(ctx.IDENT().getText());
                stringBuilder.append(" = 0");

            }
        } else if (ctx.getChildCount() == 5) {
//            stringBuilder.append(newTexts.get(ctx.type_spec()));
            stringBuilder.append(ctx.IDENT().getText());
            stringBuilder.append(" = ");
            stringBuilder.append(ctx.LITERAL().getText());
        } else {
//            stringBuilder.append(newTexts.get(ctx.type_spec()));
            stringBuilder.append(ctx.IDENT().getText());
            stringBuilder.append(" = ");
            stringBuilder.append("[0] * ");
            stringBuilder.append(ctx.LITERAL().getText());
        }
        stringBuilder.append("\n");
        newTexts.put(ctx, stringBuilder.toString());
    }

    //    if_stmt		: IF '(' expr ')' stmt
//		| IF '(' expr ')' stmt ELSE stmt 		;
    @Override
    public void exitIf_stmt(MiniCParser.If_stmtContext ctx) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(ctx.IF());
        stringBuilder.append(" ");
        stringBuilder.append(newTexts.get(ctx.expr()));
        stringBuilder.append(": ");
        if (ctx.stmt(0).getChild(0) == ctx.stmt(0).expr_stmt()) {
            stringBuilder.append("\n    ");
            stringBuilder.append(newTexts.get(ctx.stmt(0).getChild(0)));
        } else
            stringBuilder.append(newTexts.get(ctx.stmt(0)));
        if (ctx.getChildCount() == 7) {
            stringBuilder.append(ctx.ELSE());
            stringBuilder.append(": ");
            if (ctx.stmt(1).getChild(0) == ctx.stmt(1).expr_stmt()) {
                stringBuilder.append("\n    ");
                stringBuilder.append(newTexts.get(ctx.stmt(1).getChild(0)));
            } else
                stringBuilder.append(newTexts.get(ctx.stmt(1)));
        }
        newTexts.put(ctx, stringBuilder.toString());

    }

    //    return_stmt	: RETURN ';'
//            | RETURN expr ';'				;
    @Override
    public void exitReturn_stmt(MiniCParser.Return_stmtContext ctx) {
        if (ctx.getChild(1) == ctx.expr()) {
            newTexts.put(ctx, ctx.RETURN().getText() + " " + newTexts.get(ctx.expr()) + "\n");
        } else
            newTexts.put(ctx, ctx.RETURN().getText() + "\n");
    }

    //    expr	:  LITERAL | '(' expr ')' | IDENT
//	| IDENT '[' expr ']' | IDENT '(' args ')' | '-' expr
//	| '+' expr | '--' expr | '++' expr | expr '*' expr
//	| expr '/' expr | expr '%' expr | expr '+' expr | expr '-' expr
//	| expr EQ expr | expr NE expr | expr LE expr | expr '<' expr
//	| expr GE expr | expr '>' expr | '!' expr
//	| expr AND expr | expr OR expr | IDENT '=' expr
//	| IDENT '[' expr ']' '=' expr		;
    @Override
    public void exitExpr(MiniCParser.ExprContext ctx) {
        if (ctx.getChildCount() == 1) { // 자식 노드가 한 개일 때.
            if (ctx.getChild(0) == ctx.IDENT()) {
                newTexts.put(ctx, ctx.IDENT().getText());
            } else {
                newTexts.put(ctx, ctx.LITERAL().getText());
            }
        } else if (ctx.getChildCount() == 2) {// 자식 노드가 두 개일 때. 연산자 문자열과 0번째 expr 노드의 text
            switch (ctx.getChild(0).getText()) {
                case "+":
                    newTexts.put(ctx, "+" + " " + newTexts.get(ctx.expr(0)));
                    break;
                case "++":
                    newTexts.put(ctx, "++" + newTexts.get(ctx.expr(0)));
                    break;
                case "-":
                    newTexts.put(ctx, "-" + " " + newTexts.get(ctx.expr(0)));
                    break;
                case "--":
                    newTexts.put(ctx, "--" + newTexts.get(ctx.expr(0)));
                    break;
                case "!":
                    newTexts.put(ctx, "!" + newTexts.get(ctx.expr(0)));
                    break;
            }
        } else if (ctx.getChildCount() == 3) {// 자식 노드가 세 개일 때.
            // 세 개인 경우에도 각 자식 노드의 위치별로 다른 노드가 올 수 있으므로 그에 따른 구별을 위한 조건 분기.
            if (ctx.getChild(1) == ctx.expr()) {
                newTexts.put(ctx, "(" + newTexts.get(ctx.expr(0)) + ")");
            } else {
                if (ctx.getChild(0) == ctx.IDENT()) {
                    newTexts.put(ctx, ctx.IDENT().getText() + " = " + newTexts.get(ctx.expr(0)));
                } else {
                    if (ctx.getChild(1).getText().equals("*")) {
                        newTexts.put(ctx, newTexts.get(ctx.expr(0)) + " * " + newTexts.get(ctx.expr(1)));
                    } else if (ctx.getChild(1).getText().equals("/")) {
                        newTexts.put(ctx, newTexts.get(ctx.expr(0)) + " / " + newTexts.get(ctx.expr(1)));
                    } else if (ctx.getChild(1).getText().equals("%")) {
                        newTexts.put(ctx, newTexts.get(ctx.expr(0)) + " % " + newTexts.get(ctx.expr(1)));
                    } else if (ctx.getChild(1).getText().equals("+")) {
                        newTexts.put(ctx, newTexts.get(ctx.expr(0)) + " + " + newTexts.get(ctx.expr(1)));
                    } else if (ctx.getChild(1).getText().equals("-")) {
                        newTexts.put(ctx, newTexts.get(ctx.expr(0)) + " - " + newTexts.get(ctx.expr(1)));
                    } else if (ctx.getChild(1).getText().equals("<")) {
                        newTexts.put(ctx, newTexts.get(ctx.expr(0)) + " < " + newTexts.get(ctx.expr(1)));
                    } else if (ctx.getChild(1).getText().equals(">")) {
                        newTexts.put(ctx, newTexts.get(ctx.expr(0)) + " > " + newTexts.get(ctx.expr(1)));
                    } else if (ctx.getChild(1) == ctx.EQ()) {
                        newTexts.put(ctx, newTexts.get(ctx.expr(0)) + " " + ctx.EQ() + " " + newTexts.get(ctx.expr(1)));
                    } else if (ctx.getChild(1) == ctx.NE()) {
                        newTexts.put(ctx, newTexts.get(ctx.expr(0)) + " " + ctx.NE() + " " + newTexts.get(ctx.expr(1)));
                    } else if (ctx.getChild(1) == ctx.LE()) {
                        newTexts.put(ctx, newTexts.get(ctx.expr(0)) + " " + ctx.LE() + " " + newTexts.get(ctx.expr(1)));
                    } else if (ctx.getChild(1) == ctx.GE()) {
                        newTexts.put(ctx, newTexts.get(ctx.expr(0)) + " " + ctx.GE() + " " + newTexts.get(ctx.expr(1)));
                    } else if (ctx.getChild(1) == ctx.AND()) {
                        newTexts.put(ctx, newTexts.get(ctx.expr(0)) + " " + ctx.AND() + " " + newTexts.get(ctx.expr(1)));
                    } else if (ctx.getChild(1) == ctx.OR()) {
                        newTexts.put(ctx, newTexts.get(ctx.expr(0)) + " " + ctx.OR() + " " + newTexts.get(ctx.expr(1)));
                    }
                }
            }
        } else if (ctx.getChildCount() == 4) {// 자식 노드가 네 개일 때.
            if (ctx.getChild(1).getText().equals("[")) {
                newTexts.put(ctx, ctx.IDENT().getText() + "[" + newTexts.get(ctx.expr(0)) + "]");
            } else {
                String t = ctx.IDENT().getText();
                if (t.equals("_print"))
                    t = "print";

                newTexts.put(ctx, t + "(" + newTexts.get(ctx.args()) + ")");
            }
        } else if (ctx.getChildCount() == 6) {// 자식 노드가 여섯 개일 때.
            newTexts.put(ctx, ctx.IDENT().getText() + "[" + newTexts.get(ctx.expr(0)) + "]" + " = " + newTexts.get(ctx.expr(1)));
        }
    }

    //    args	: expr (',' expr)*
//            |					 ;
    @Override
    public void exitArgs(MiniCParser.ArgsContext ctx) { // arguments in function call
        StringBuilder stringBuilder = new StringBuilder();
        if (ctx.getChild(0) == null) {
            newTexts.put(ctx, "");
        } else {
            stringBuilder.append(newTexts.get(ctx.expr(0)));
            for (int i = 1; i < ctx.expr().size(); i++) {
                stringBuilder.append(", ");
                stringBuilder.append(newTexts.get(ctx.expr(i)));
            }
            newTexts.put(ctx, stringBuilder.toString());
        }
    }
}

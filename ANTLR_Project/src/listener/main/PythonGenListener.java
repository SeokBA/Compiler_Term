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

        // System.out.println(programStr);
        if (setAddressListener != null)
            setAddressListener.outputData = programStr.toString();
    }

    //    decl		: var_decl | fun_decl		;
    @Override
    public void exitDecl(MiniCParser.DeclContext ctx) {
        if (ctx.getChild(0).equals(ctx.var_decl()))
            newTexts.put(ctx, newTexts.get(ctx.var_decl()));
        else
            newTexts.put(ctx, newTexts.get(ctx.fun_decl()));
    }

    //    var_decl	:  type_spec IDENT ';'
//            | type_spec IDENT '=' LITERAL ';'
//            | type_spec IDENT '[' LITERAL ']' ';'	;
    @Override
    public void enterVar_decl(MiniCParser.Var_declContext ctx) {

    }

    //    var_decl	:  type_spec IDENT ';'
//            | type_spec IDENT '=' LITERAL ';'
//            | type_spec IDENT '[' LITERAL ']' ';'	;
    @Override
    public void exitVar_decl(MiniCParser.Var_declContext ctx) {
        StringBuilder stringBuilder = new StringBuilder();
        if (ctx.getChildCount() == 3) {
            stringBuilder.append(newTexts.get(ctx.type_spec()));
            stringBuilder.append(ctx.IDENT().getText());
        } else if (ctx.getChildCount() == 5) {
            stringBuilder.append(newTexts.get(ctx.type_spec()));
            stringBuilder.append(ctx.IDENT().getText());
            stringBuilder.append(" = ");
            stringBuilder.append(ctx.LITERAL().getText());
        } else {
            stringBuilder.append(newTexts.get(ctx.type_spec()));
            stringBuilder.append(ctx.IDENT().getText());
            stringBuilder.append("[");
            stringBuilder.append(ctx.LITERAL().getText());
            stringBuilder.append("]");
        }
        newTexts.put(ctx, stringBuilder.toString());
    }

    //    type_spec	: VOID
//		| INT
    @Override
    public void exitType_spec(MiniCParser.Type_specContext ctx) {
        newTexts.put(ctx, "");
    }

    //    fun_decl	: type_spec IDENT '(' params ')' compound_stmt ;
    @Override
    public void exitFun_decl(MiniCParser.Fun_declContext ctx) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("def ");
        stringBuilder.append(newTexts.get(ctx.type_spec()));
        if (ctx.IDENT().getText().equals("main")) {
            stringBuilder.append("if __main__ == '__main__':\n");
        } else {
            stringBuilder.append(ctx.IDENT().getText());
            stringBuilder.append("(");
            stringBuilder.append(newTexts.get(ctx.params()));
            stringBuilder.append(") :\n");
        }
        stringBuilder.append(newTexts.get(ctx.compound_stmt()));
        newTexts.put(ctx, stringBuilder.toString());
    }

    //    params		: param (',' param)* | VOID  |			;
    @Override
    public void exitParams(MiniCParser.ParamsContext ctx) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("self");
        if (ctx.getChild(0).equals(ctx.param())) {
            stringBuilder.append(", ");
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
    public void enterParam(MiniCParser.ParamContext ctx) {

    }

    //    param		: type_spec IDENT
//		| type_spec IDENT '[' ']'	;
    @Override
    public void exitParam(MiniCParser.ParamContext ctx) {

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
    public void enterWhile_stmt(MiniCParser.While_stmtContext ctx) {

    }

    //    while_stmt	: WHILE '(' expr ')' stmt	;
    @Override
    public void exitWhile_stmt(MiniCParser.While_stmtContext ctx) {

    }

    //    compound_stmt: '{' local_decl* stmt* '}'	;
    @Override
    public void enterCompound_stmt(MiniCParser.Compound_stmtContext ctx) {

    }

    //    compound_stmt: '{' local_decl* stmt* '}'	;
    @Override
    public void exitCompound_stmt(MiniCParser.Compound_stmtContext ctx) {

    }

    //    local_decl	: type_spec IDENT ';'
//            | type_spec IDENT '=' LITERAL ';'
//            | type_spec IDENT '[' LITERAL ']' ';'	;
    @Override
    public void enterLocal_decl(MiniCParser.Local_declContext ctx) {

    }

    //    local_decl	: type_spec IDENT ';'
//            | type_spec IDENT '=' LITERAL ';'
//            | type_spec IDENT '[' LITERAL ']' ';'	;
    @Override
    public void exitLocal_decl(MiniCParser.Local_declContext ctx) {

    }

    //    if_stmt		: IF '(' expr ')' stmt
//		| IF '(' expr ')' stmt ELSE stmt 		;
    @Override
    public void enterIf_stmt(MiniCParser.If_stmtContext ctx) {

    }


    //    if_stmt		: IF '(' expr ')' stmt
//		| IF '(' expr ')' stmt ELSE stmt 		;
    @Override
    public void exitIf_stmt(MiniCParser.If_stmtContext ctx) {

    }

    //    return_stmt	: RETURN ';'
//            | RETURN expr ';'				;
    @Override
    public void exitReturn_stmt(MiniCParser.Return_stmtContext ctx) {

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
    public void enterExpr(MiniCParser.ExprContext ctx) {

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

    }

    //    args	: expr (',' expr)*
//            |					 ;
    @Override
    public void exitArgs(MiniCParser.ArgsContext ctx) {
    }
}

package listener.main;

import generated.MiniCBaseListener;
import generated.MiniCParser;
import org.antlr.v4.runtime.ParserRuleContext;
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
    public void enterProgram(MiniCParser.ProgramContext ctx) {
    }


    @Override
    public void exitProgram(MiniCParser.ProgramContext ctx) {
        String programStr = "";
        // System.out.println(programStr);
        if (setAddressListener != null)
            setAddressListener.outputData = programStr;
    }

    @Override
    public void enterDecl(MiniCParser.DeclContext ctx) {
    }


    @Override
    public void exitDecl(MiniCParser.DeclContext ctx) {
    }


    @Override
    public void enterVar_decl(MiniCParser.Var_declContext ctx) {
    }


    @Override
    public void exitVar_decl(MiniCParser.Var_declContext ctx) {
    }


    @Override
    public void enterType_spec(MiniCParser.Type_specContext ctx) {
    }


    @Override
    public void exitType_spec(MiniCParser.Type_specContext ctx) {
    }


    @Override
    public void enterFun_decl(MiniCParser.Fun_declContext ctx) {
    }


    @Override
    public void exitFun_decl(MiniCParser.Fun_declContext ctx) {
    }


    @Override
    public void enterParams(MiniCParser.ParamsContext ctx) {
    }


    @Override
    public void exitParams(MiniCParser.ParamsContext ctx) {
    }


    @Override
    public void enterParam(MiniCParser.ParamContext ctx) {
    }


    @Override
    public void exitParam(MiniCParser.ParamContext ctx) {
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation does nothing.</p>
     */
    @Override
    public void enterStmt(MiniCParser.StmtContext ctx) {
    }


    @Override
    public void exitStmt(MiniCParser.StmtContext ctx) {
    }


    @Override
    public void enterExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
    }


    @Override
    public void exitExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
    }


    @Override
    public void enterWhile_stmt(MiniCParser.While_stmtContext ctx) {
    }


    @Override
    public void exitWhile_stmt(MiniCParser.While_stmtContext ctx) {
    }


    @Override
    public void enterCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
    }


    @Override
    public void exitCompound_stmt(MiniCParser.Compound_stmtContext ctx) {
    }


    @Override
    public void enterLocal_decl(MiniCParser.Local_declContext ctx) {
    }


    @Override
    public void exitLocal_decl(MiniCParser.Local_declContext ctx) {
    }


    @Override
    public void enterIf_stmt(MiniCParser.If_stmtContext ctx) {
    }


    @Override
    public void exitIf_stmt(MiniCParser.If_stmtContext ctx) {
    }


    @Override
    public void enterReturn_stmt(MiniCParser.Return_stmtContext ctx) {
    }


    @Override
    public void exitReturn_stmt(MiniCParser.Return_stmtContext ctx) {
    }


    @Override
    public void enterExpr(MiniCParser.ExprContext ctx) {
    }


    @Override
    public void exitExpr(MiniCParser.ExprContext ctx) {
    }


    @Override
    public void enterArgs(MiniCParser.ArgsContext ctx) {
    }


    @Override
    public void exitArgs(MiniCParser.ArgsContext ctx) {
    }

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
    }


    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
    }
}

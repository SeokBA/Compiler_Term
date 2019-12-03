package listener.main;

import generated.MiniCBaseListener;
import generated.MiniCParser;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import static listener.main.BytecodeGenListenerHelper.getFunName;

public class JavaGenListener extends MiniCBaseListener implements ParseTreeListener {
    ParseTreeProperty<String> newTexts = new ParseTreeProperty<String>();
    //JavaSymbolTable symbolTable = new JavaSymbolTable();
    TranslationGUI.setAddressListener setAddressListener;


    public void setGUI(TranslationGUI.setAddressListener setAddressListener){
        this.setAddressListener = setAddressListener;
    }

    @Override
    public void exitProgram(MiniCParser.ProgramContext ctx) {
        int size = ctx.getChildCount();
        String programStr="";
        programStr+=newTexts.get(ctx.decl(0))+"\n";
        if (size > 1) {
            for (int i = 1; i < size; i++) {//pretty print된 결과물을 newTexts에 가져와 출력
                programStr+=newTexts.get(ctx.decl(i))+"\n";
            }
        }
        System.out.println(programStr);
        if (setAddressListener != null)
            setAddressListener.outputData = programStr;
    }

    boolean isBinaryOperation(MiniCParser.ExprContext ctx) {//BinaryOperation인지 확인
        return ctx.getChildCount() == 3 && ctx.getChild(1) != ctx.expr();
    }

    boolean isOperation(String op) {//연산자인지 확인
        switch (op) {
            case "-":
            case "+":
            case "*":
            case "/":
            case "%":
            case ">":
            case "<":
            case "or":
            case "and":
            case "<=":
            case ">=":
            case "==":
            case "=":
            case "!=":
                return true;
            default:
                return false;
        }
    }

    @Override
    public void enterProgram(MiniCParser.ProgramContext ctx) {///처음 프로그램을 시작할 때 실행되는 함수
        newTexts.put(ctx, ctx.getText());
    }

    @Override
    public void enterDecl(MiniCParser.DeclContext ctx) {
        newTexts.put(ctx, ctx.getText());
    }

    @Override
    public void exitDecl(MiniCParser.DeclContext ctx) {//변경된 Decl결과를 newTexts에 넣음
        newTexts.put(ctx, newTexts.get(ctx.getChild(0)));
    }

    @Override
    public void enterVar_decl(MiniCParser.Var_declContext ctx) {
        newTexts.put(ctx, ctx.getText());
    }

    @Override
    public void exitVar_decl(MiniCParser.Var_declContext ctx) {//유형에 따라 다른 결과값을 가지고 newTexts에 put함
        String type = newTexts.get(ctx.type_spec());//앞에서 변형한 type_spec()을 newTexts에서 가져옴
        String ident = ctx.getChild(1).getText();
        String op = ctx.getChild(2).getText();//자식에서 text를 직접 가져옴
        if (op.equals("=")) {//연산자에 맞게 수정하여 newtext에 넣어줌
            String literal = ctx.getChild(3).getText();
            newTexts.put(ctx, type + ident + " = " + literal + ";");
        } else if (op.equals("[")) {
            String literal = ctx.getChild(3).getText();
            newTexts.put(ctx, type + ident + "[" + literal + "];");
        } else {
            newTexts.put(ctx, type + ident + ";");
        }

    }

    @Override
    public void enterType_spec(MiniCParser.Type_specContext ctx) {
        newTexts.put(ctx, ctx.getText());
    }

    @Override
    public void exitType_spec(MiniCParser.Type_specContext ctx) {//Type_spec다음에 빈칸을 추가하여 newTexts에 put함
        newTexts.put(ctx, ctx.getText() + " ");
    }

    @Override
    public void enterParams(MiniCParser.ParamsContext ctx) {
        newTexts.put(ctx, ctx.getText());
    }

    @Override
    public void exitParams(MiniCParser.ParamsContext ctx) {//param여러개 void,공백
        int param_size = ctx.getChildCount();
        if (param_size == 0) {//자식 갯수가 0이면
            newTexts.put(ctx, " ");//공백 넣기
            return;
        }
        String par = newTexts.get(ctx.getChild(0));//newTexts에서 첫번째 자식을 가져옴
        String temp = ctx.getChild(0).getText();
        if (temp.equals("void")) {
            par = "void";
        }
        for (int i = 1; i <param_size-1; i++) {//param이 여러개면 반복문을 통해
            par = par + "," + newTexts.get(ctx.param(i));//','로 이어줌
        }
        newTexts.put(ctx, par);//완료된 결과물 넣어주기
    }

    @Override
    public void enterParam(MiniCParser.ParamContext ctx) {
        newTexts.put(ctx, ctx.getText());
    }

    @Override
    public void exitParam(MiniCParser.ParamContext ctx) {
        int size = ctx.getChildCount();
        if (size>2 &&(ctx.getChild(2).getText()).equals("[")) {//자식 갯수가 2개 이상이고 인덱스 2번 자식이 '['라면
            String spec = ctx.type_spec().getText();
            String id = ctx.IDENT().getText();//필요한 문자열들을 가져오기
            newTexts.put(ctx, spec + " " + id + "[ ]");//type_spec IDENT '[' ']'
        } else {
            String spec = ctx.type_spec().getText();
            String id = ctx.IDENT().getText();
            newTexts.put(ctx, spec + " " + id);// type_spec IDENT
        }
    }

    @Override
    public void enterStmt(MiniCParser.StmtContext ctx) {
        newTexts.put(ctx, ctx.getText());
    }

    @Override
    public void exitStmt(MiniCParser.StmtContext ctx) {
        newTexts.put(ctx, newTexts.get(ctx.getChild(0)));//newTexts에서 변형이 완료된 stmt를 가져옵
    }

    @Override
    public void enterExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
    }

    @Override
    public void exitExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
        newTexts.put(ctx, newTexts.get(ctx.getChild(0)) + ";");//newTexts에서 expr을 가져와 ;와 합함
    }

    @Override
    public void enterWhile_stmt(MiniCParser.While_stmtContext ctx) {
        newTexts.put(ctx, ctx.getText());
    }

    @Override
    public void exitWhile_stmt(MiniCParser.While_stmtContext ctx) {
        String result = "",front;
        result=result+"\n"+newTexts.get(ctx.stmt());//중괄호 유무와 관련없이 stmt몸통 부분을 추가하는 부분은 동일
        front = ctx.WHILE() + "(" + newTexts.get(ctx.expr()) + ")";//while(expr)형식 만들기
        if (! (ctx.stmt().getChild(0) instanceof MiniCParser.Compound_stmtContext)) {//중괄호 없다면
            result = result.replaceAll("\n", "\n    ");//replace함수를 사용하여 ....갯수 추가(\n을 이용해 while이 중첩될 때마다 ....이 추가된다)
        }
        newTexts.put(ctx, front+result);//while(expr)과 stmt줄을 합친 결과를 newText에 put
    }

    @Override
    public void enterCompound_stmt(MiniCParser.Compound_stmtContext ctx) {//{}중괄호 들어오면 여기로
        newTexts.put(ctx, ctx.getText());
    }

    @Override
    public void exitCompound_stmt(MiniCParser.Compound_stmtContext ctx) {//중괄호랑 ...추가
        int size = ctx.getChildCount();
        String body="";
        for (int i = 1; i <size-1 ; i++) {//자식 갯수 만큼
            body=body+"\n"+newTexts.get(ctx.getChild(i));//newTexts에서 가져와서 body추가
        }
        body = body.replaceAll("\n", "\n    ");//replace함수를 통해 중첩된 구문들도 자동으로 ....이 추가되도록 함
        body="{"+body+"\n}";
        newTexts.put(ctx, body);
    }

    @Override
    public void enterLocal_decl(MiniCParser.Local_declContext ctx) {
        newTexts.put(ctx, ctx.getText());
    }

    @Override
    public void exitLocal_decl(MiniCParser.Local_declContext ctx) {
        String type = newTexts.get(ctx.type_spec());
        String ident = ctx.getChild(1).getText();
        if (ctx.getChildCount() < 4) {
            newTexts.put(ctx, type + ident + ";");
            return;
        } else {// ;까지 합하면 4개이므로
            String op = ctx.getChild(2).getText();
            if (op.equals("=")) { // type_spec IDENT = LITERAL;형식으로 만들어줌
                newTexts.put(ctx, type + ident + " = " + ctx.getChild(3).getText() + ";");
            } else {// type_spec IDENT [LITERAL];
                newTexts.put(ctx, type + ident + "[" + ctx.getChild(3).getText() + "];");
            }
        }
    }

    @Override
    public void enterIf_stmt(MiniCParser.If_stmtContext ctx) {
    }

    @Override
    public void exitIf_stmt(MiniCParser.If_stmtContext ctx) {
        int stmt_size = ctx.stmt().size();
        String if_front = "",else_back="",front;

        front = ctx.IF() + "(" + newTexts.get(ctx.expr()) + ")";//if(expr)형식으로 앞부분을 만들어줌
        if_front =if_front+"\n"+newTexts.get(ctx.stmt(0).getChild(0));//몸통을 위해 처음에 \n을 붙여줌
        if ( !(ctx.stmt(0).getChild(0) instanceof MiniCParser.Compound_stmtContext)) {//중괄호 없다면
            if_front =if_front.replaceAll("\n", "\n    ");//중괄호 안에 ....을 맞춰 추가해주기 위해 replace를 해줌
        }

        if (ctx.stmt().size() == 2) {//else문도 만들기
            if ((ctx.stmt(1).getChild(0) instanceof MiniCParser.Compound_stmtContext)) {//중괄호 있는지 확인
                else_back = else_back +"\n"+ctx.ELSE()+"\n"+ newTexts.get(ctx.stmt(1).getChild(0));//중괄호 있다면!
            }
            else{
                else_back =else_back+ "\n" +newTexts.get(ctx.stmt(1).getChild(0));
                else_back = else_back.replaceAll("\n", "\n    ");//중괄호 안에 ....을 맞춰 추가해주기 위해 replace를 따로 해줌(compound_stmt로 인식못해서 모자란 ....채우기)
                else_back="\n"+ctx.ELSE()+else_back;
            }
        }

        newTexts.put(ctx, front+if_front+else_back);
    }

    @Override
    public void enterReturn_stmt(MiniCParser.Return_stmtContext ctx) {
    }

    @Override
    public void exitReturn_stmt(MiniCParser.Return_stmtContext ctx) {
        int size = ctx.getChildCount();
        if (size == 1) {//자식 갯수로 유형 판별
            newTexts.put(ctx, ctx.RETURN() + ";");
        } else {
            newTexts.put(ctx, ctx.RETURN() + " " + newTexts.get(ctx.expr()) + ";");
        }
    }

    @Override
    public void enterFun_decl(MiniCParser.Fun_declContext ctx) {
        newTexts.put(ctx, ctx.getText());
    }

    @Override
    public void exitFun_decl(MiniCParser.Fun_declContext ctx) {
        String ident = ctx.getChild(1).getText();
        String compound = newTexts.get(ctx.compound_stmt());
        if(ident.equals("main")) {
            newTexts.put(ctx, "public static void main(String[] args)\n"+ compound);
            return;
        }
        String type = newTexts.get(ctx.type_spec());
        String par = newTexts.get(ctx.params());
        newTexts.put(ctx, "public static "+type + ident + "(" + par + ")\n" + compound);//메인 함수에 넣으려면 static있어야 함
    }

    @Override
    public void enterExpr(MiniCParser.ExprContext ctx) {
        newTexts.put(ctx, ctx.getText());
    }

    @Override
    public void exitExpr(MiniCParser.ExprContext ctx) {
        String s0 = null, s2 = null, op = null;
        if(ctx.getChildCount() == 4){//IDENT '[' expr ']' 과 IDENT '(' args ')'	처리하기
            String fname=ctx.getChild(0).getText();
            String exp2=ctx.getChild(1).getText();
            String exp3= newTexts.get(ctx.getChild(2));//처리된  expr 결과 가져오기
            String exp4= ctx.getChild(3).getText();
            if (fname.equals("_print")) {
                fname="System.out.println";
            }
            newTexts.put(ctx, fname+exp2+exp3+exp4);
            return;
        }
        if (isBinaryOperation(ctx)) {
            s0 = newTexts.get(ctx.getChild(0));//왼쪽 중간결과(맵 같은 거라서 키값 넣어주면 value가 나옴)
            s2 = newTexts.get(ctx.getChild(2));//오른쪽 중간결과
            if(s0==null||s2==null){//'(' expr ')'유형을 처리해 주기 위해 추가((,)때문에 null이 나와서 추가)
                if(s0==null) s0 = ctx.getChild(0).getText();
                if(s2==null) s2 = ctx.getChild(2).getText();//newTexts.get(ctx.getChild())가 널이면 직접 받기
            }
            op = ctx.getChild(1).getText();//연산자
            if(s0.equals("(")){//'(' expr ')'유형
                newTexts.put(ctx, s0 + " " +newTexts.get(ctx.getChild(1)) + " " + s2);
                return;
            }
            if (isOperation(op)) {//a + b해주기 위한 처리
                newTexts.put(ctx, s0 + " " + op + " " + s2);
            }
            return;
        }//처리 끝났으면 종료

        else if(ctx.getChildCount()==6){//IDENT '[' expr ']' '=' expr
            newTexts.put(ctx, ctx.getChild(0)+"["+newTexts.get(ctx.getChild(2))+"] = "+newTexts.get(ctx.getChild(5)));//완료된 결과 가져오기
            return;
        }
        newTexts.put(ctx, newTexts.get(ctx));//if문 통과하나도 안했으면 그대로 넣기
    }

    @Override
    public void enterArgs(MiniCParser.ArgsContext ctx) {
        newTexts.put(ctx, ctx.getText());
    }

    @Override
    public void exitArgs(MiniCParser.ArgsContext ctx) {
        int size = ctx.expr().size();
        String input="";
        if (size == 0) {//자식이 없다면 " "넣기
            newTexts.put(ctx, " ");
            return;
        }
        input=newTexts.get(ctx.expr(0));
        for (int i = 1; i < size; i++) {//expr이루는 갯수가 여러개면 여러개 붙이기
            input=input+", "+newTexts.get(ctx.expr(i));
        }

        newTexts.put(ctx, input);//전체 결과put하기
    }


}
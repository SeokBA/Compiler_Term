package listener.main.java;

import generated.MiniCBaseListener;
import generated.MiniCParser;
import listener.main.gui.TranslationGUI;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

public class JavaGenListener extends MiniCBaseListener implements ParseTreeListener {
    ParseTreeProperty<String> newTexts = new ParseTreeProperty<String>();
    JavaSymbolTable symbolTable = new JavaSymbolTable();
    JavaGenListenerHelper genListenerHelper = new JavaGenListenerHelper();
    TranslationGUI.setAddressListener setAddressListener;
    private StringBuilder errorDump = new StringBuilder();


    public void setGUI(TranslationGUI.setAddressListener setAddressListener){
        this.setAddressListener = setAddressListener;
    }

    @Override
    public void exitProgram(MiniCParser.ProgramContext ctx) {//replaceAll("\n", "\n    ");
        int size = ctx.getChildCount();
        String programStr="public class TestJava {";
        programStr+="\n"+newTexts.get(ctx.decl(0));
        if (size > 1) {
            for (int i = 1; i < size; i++) {//pretty print된 결과물을 newTexts에 가져와 출력
                programStr+="\n"+newTexts.get(ctx.decl(i));
            }
        }
        programStr=programStr.replaceAll("\n", "\n    ");
        programStr+="\n}";
        System.out.println(programStr);
        if (setAddressListener != null) {
            if (!errorDump.toString().equals(""))
                setAddressListener.outputData = errorDump.toString();//에러가 아래에서 걸리면 지금까지 모은 에러 문구 한번에 넘겨주기
            else
                setAddressListener.outputData = programStr.toString();
        }
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
        String varName = ctx.IDENT().getText();
        if (symbolTable.hasGlobalName(varName)) {//이미 정의된 함수라면 안되지
            if (setAddressListener != null)
                setAddressListener.setException();
            System.out.println(varName + " : 이미 정의된 변수입니다.");
            errorDump.append(varName + " : 이미 정의된 변수입니다.\n");
        } else if (symbolTable.hasFunName(varName)) {//함수이름인데 착각했을 때
            if (setAddressListener != null)
                setAddressListener.setException();
            System.out.println(varName + " : 다른 형식의 선언에 사용된 이름입니다.");
            errorDump.append(varName + " : 다른 형식의 선언에 사용된 이름입니다.\n");
        }
        newTexts.put(ctx, ctx.getText());
    }

    @Override
    public void exitVar_decl(MiniCParser.Var_declContext ctx) {//유형에 따라 다른 결과값을 가지고 newTexts에 put함
        String type = newTexts.get(ctx.type_spec());//앞에서 변형한 type_spec()을 newTexts에서 가져옴
        if(type.equals("void ")){
            if (setAddressListener != null)
                setAddressListener.setException();
            System.out.println(ctx.IDENT().getText() +"void 타입 전역변수는 올 수 없습니다.");
            errorDump.append(ctx.IDENT().getText() + " : void 타입 전역변수는 올 수 없습니다.\n");
           //자바에선 type_spec IDENT, type_spec IDENT '[' ']'이 경우에서 타입이 void가 오는 경우가 없으므로
        }
        //각각 해당되는 심볼테이블에 넣어주기
        if (genListenerHelper.isArrayDecl(ctx)) {
            symbolTable.putGlobalVar(ctx.IDENT().getText(), JavaSymbolTable.Type.INTARRAY);
        } else if (genListenerHelper.isDeclWithInit(ctx)) {
            symbolTable.putGlobalVarWithInitVal(ctx.IDENT().getText(), JavaSymbolTable.Type.INT, genListenerHelper.initVal(ctx));
        } else {
            symbolTable.putGlobalVar(ctx.IDENT().getText(), JavaSymbolTable.Type.INT);//위에 조건 통과했다면 무조건 INT형이니까
        }


        String ident = ctx.getChild(1).getText();
        String op = ctx.getChild(2).getText();//자식에서 text를 직접 가져옴
        if (op.equals("=")) {//연산자에 맞게 수정하여 newtext에 넣어줌
            String literal = ctx.getChild(3).getText();
            newTexts.put(ctx, type + ident + " = " + literal + ";");
        } else if (op.equals("[")) {
            String literal = ctx.getChild(3).getText();
            newTexts.put(ctx, type +ident+"[] = new "+ type.split(" ")[0]+"["+ ctx.LITERAL().getText() + "];");//중간에 빈칸없애기 위해 split사용
        } else {
            newTexts.put(ctx, type + ident + "=0;");
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
            par = " ";//자바에서 매개변수로 void만 주어지는 경우가 없으므로 빈칸으로 바꾸기
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
            String type = ctx.type_spec().getText();
            if(type.equals("void")){
                if (setAddressListener != null)
                    setAddressListener.setException();
                System.out.println(ctx.IDENT().getText() +"void 타입 배열은 올 수 없습니다.");
                errorDump.append(ctx.IDENT().getText() + " : void 타입 배열은 올 수 없습니다.\n");
                //자바에선 type_spec IDENT, type_spec IDENT '[' ']'이 경우에서 타입이 void가 오는 경우가 없으므로
            }
            String id = ctx.IDENT().getText();//필요한 문자열들을 가져오기
            newTexts.put(ctx, type + " " + id + "[ ]");//type_spec IDENT '[' ']'
        } else {
            String type = ctx.type_spec().getText();

            if(type.equals("void")){
                if (setAddressListener != null)
                    setAddressListener.setException();
                System.out.println(ctx.IDENT().getText() +": void 타입 매개변수는 올 수 없습니다.");
                errorDump.append(ctx.IDENT().getText() + " : void 타입 매개변수는 올 수 없습니다.\n");
               //자바에선 type_spec IDENT, type_spec IDENT '[' ']'이 경우에서 타입이 void가 오는 경우가 없으므로
            }
            if(ctx.getChildCount()==2){//type_spec IDENT
                symbolTable.putLocalVar(ctx.IDENT().getText(), JavaSymbolTable.Type.INT);
            }else{//type_spec IDENT '[' ']'
                symbolTable.putLocalVar(ctx.IDENT().getText(), JavaSymbolTable.Type.INTARRAY);
            }
          String id = ctx.IDENT().getText();
            newTexts.put(ctx, type + " " + id);// type_spec IDENT
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
    public void exitExpr_stmt(MiniCParser.Expr_stmtContext ctx) {
        newTexts.put(ctx, newTexts.get(ctx.getChild(0)) + ";");//newTexts에서 expr을 가져와 ;와 합함(초기화 자동으로 해야 에러가 나지 않음)
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
        String varName = ctx.IDENT().getText();
        newTexts.put(ctx, ctx.getText());
        if (symbolTable.hasLocalName(varName) || symbolTable.hasGlobalName(varName)) {
            if (setAddressListener != null)
                setAddressListener.setException();
            System.out.println(varName + " : 이미 정의된 변수입니다.");
            errorDump.append(varName + " : 이미 정의된 변수입니다.\n");
        } else if (symbolTable.hasFunName(varName)) {
            if (setAddressListener != null)
                setAddressListener.setException();
            System.out.println(varName + " : 다른 형식의 선언에 사용된 이름입니다.");
            errorDump.append(varName + " : 다른 형식의 선언에 사용된 이름입니다.\n");
        }

    }

    @Override
    public void exitLocal_decl(MiniCParser.Local_declContext ctx) {
        String type = newTexts.get(ctx.type_spec());
        if(type.equals("void ")){
            if (setAddressListener != null)
                setAddressListener.setException();
            System.out.println(ctx.IDENT().getText() +": void 타입 전역변수는 올 수 없습니다.");
            errorDump.append(ctx.IDENT().getText() + " : void 타입 전역변수는 올 수 없습니다.\n");
            //자바에선 type_spec IDENT, type_spec IDENT '[' ']'이 경우에서 타입이 void가 오는 경우가 없으므로
        }
        if (genListenerHelper.isArrayDecl(ctx)) {
            symbolTable.putLocalVar(ctx.IDENT().getText(), JavaSymbolTable.Type.INTARRAY);
        } else if (genListenerHelper.isDeclWithInit(ctx)) {
            symbolTable.putLocalVarWithInitVal(ctx.IDENT().getText(), JavaSymbolTable.Type.INT, genListenerHelper.initVal(ctx));
        } else {
            symbolTable.putLocalVar(ctx.IDENT().getText(), JavaSymbolTable.Type.INT);
        }//위에 조건 통과했다면 무조건 INT형이니까

        String ident = ctx.getChild(1).getText();
        if (ctx.getChildCount() < 4) {
            newTexts.put(ctx, type + ident + "=0;");//a++나 더할 때 에러 검출 막기 위해 초기화 미리 해줌
            return;
        } else {// ;까지 합하면 4개이므로
            String op = ctx.getChild(2).getText();
            if (op.equals("=")) { // type_spec IDENT = LITERAL;형식으로 만들어줌
                newTexts.put(ctx, type + ident + " = " + ctx.getChild(3).getText() + ";");
            } else {// type_spec IDENT [LITERAL];
                newTexts.put(ctx, type +ident+"[] = new "+ type.split(" ")[0]+"["+ ctx.LITERAL().getText() + "];");//중간에 빈칸없애기 위해 split사용
            }
        }
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
    public void exitReturn_stmt(MiniCParser.Return_stmtContext ctx) {
        int size = ctx.getChildCount();
        if (size == 2) {//자식 갯수로 유형 판별
            newTexts.put(ctx, ctx.RETURN() + ";");
        } else {
            newTexts.put(ctx, ctx.RETURN() + " " + newTexts.get(ctx.expr()) + ";");
        }
    }

    @Override
    public void enterFun_decl(MiniCParser.Fun_declContext ctx) {
        symbolTable.putFunSpecStr(ctx);
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

       if(ctx.getChildCount()>=4){//IDENT '[' expr ']' 과 IDENT '(' args ')' IDENT '[' expr ']' '=' expr 선언 여부 확인
            String fname=ctx.getChild(0).getText();
            if(ctx.getChild(1).getText().equals("(")&&!symbolTable.hasFunName(fname) && !fname.equals("_print")){//함수인 경우    IDENT '(' args ')'
                if (setAddressListener != null)
                    setAddressListener.setException();
                System.out.println(fname + " : 선언되지 않은 함수입니다.");
                errorDump.append(fname + " :  선언되지 않은 함수입니다.\n");
            }
            else if(ctx.getChild(1).getText().equals("[")&&!(symbolTable.hasGlobalName(fname) || symbolTable.hasLocalName(fname))){//IDENT '[' expr ']'   IDENT '[' expr ']' '=' expr
                if (setAddressListener != null)
                    setAddressListener.setException();
                System.out.println(fname + " : 선언되지 않은 배열입니다.");
                errorDump.append(fname + " : 선언되지 않은 배열입니다.\n");

            }
        }
        else if(isBinaryOperation(ctx)){//expr '/' expr 이런 얘들

            //a=5,a=h이런 문구에서 변수 모조리 검사(연산자 양쪽 다 검사)

           if(ctx.getChild(1).getText().equals("=")){// k = i / 2;이 경우 i/2는 선언 검사 건너뛰기
               if(ctx.getChild(2).getChildCount() == 1){
                   try{
                       Integer.parseInt(ctx.getChild(0).getText());//숫자로 변환이 잘되면 넘어가고
                   }catch(NumberFormatException e) {//숫자가 아니면 선언된 변수인지 검사하기
                       String vname = ctx.getChild(0).getText();
                       if(!(symbolTable.hasGlobalName(vname) || symbolTable.hasLocalName(vname))){
                           if (setAddressListener != null)
                               setAddressListener.setException();
                           System.out.println(vname + " : 정의되지 않은 변수가 호출되었습니다.");
                           errorDump.append(vname + " : 정의되지 않은 변수가 호출되었습니다\n");
                       }
                   }
               }
           }
           else{

               try{
                   Integer.parseInt(ctx.getChild(0).getText());//숫자로 변환이 잘되면 넘어가고
               }catch(NumberFormatException e) {//숫자가 아니면 선언된 변수인지 검사하기
                   String vname = ctx.getChild(0).getText();
                   if(!(symbolTable.hasGlobalName(vname) || symbolTable.hasLocalName(vname))){
                       if (setAddressListener != null)
                           setAddressListener.setException();
                       System.out.println(vname + " : 정의되지 않은 변수가 호출되었습니다.");
                       errorDump.append(vname + " : 정의되지 않은 변수가 호출되었습니다\n");
                   }
               }

               try{
                   Integer.parseInt(ctx.getChild(2).getText());
               }catch(NumberFormatException e) {//숫자가 아니면 검사하기
                   String vname = ctx.getChild(2).getText();
                   if (!(symbolTable.hasGlobalName(vname) || symbolTable.hasLocalName(vname))){
                       if (setAddressListener != null)
                           setAddressListener.setException();
                       System.out.println(vname + " : 정의되지 않은 변수가 호출되었습니다.");
                       errorDump.append(vname + " : 정의되지 않은 변수가 호출되었습니다\n");
                   }
               }
           }
        }
        else if(ctx.getChildCount()==2){//'++' expr이런 얘들

            String varname = ctx.getChild(1).getText();
           try{
               Integer.parseInt(ctx.getChild(1).getText());
           }catch(NumberFormatException e) {//숫자가 아니면 검사하기(return -1이런 경우도 다 잡아야하니까)
               if (!(symbolTable.hasGlobalName(varname) || symbolTable.hasLocalName(varname))){
                   if (setAddressListener != null)
                       setAddressListener.setException();
                   System.out.println(varname + " : 정의되지 않은 변수가 호출되었습니다.");
                   errorDump.append(varname + " : 정의되지 않은 변수가 호출되었습니다\n");
               }
           }
        }
//=================================선언된 함수인지 확인하는 부분 끝

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
package listener.main.python;

import generated.MiniCParser.*;

public class PythonGenListenerHelper {

	// <boolean functions>
	static boolean isFunDecl(ProgramContext ctx, int i) {
		return ctx.getChild(i).getChild(0) instanceof Fun_declContext;
	}

	// type_spec IDENT '[' ']'
	static boolean isArrayParamDecl(ParamContext param) {
		return param.getChildCount() == 4;
	}

	// global vars
	static int initVal(Var_declContext ctx) {
		return Integer.parseInt(ctx.LITERAL().getText());
	}

	// var_decl	: type_spec IDENT '=' LITERAL ';
	static boolean isDeclWithInit(Var_declContext ctx) {
		return ctx.getChildCount() == 5 ;
	}
	// var_decl	: type_spec IDENT '[' LITERAL ']' ';'
	static boolean isArrayDecl(Var_declContext ctx) {
		return ctx.getChildCount() == 6;
	}

	// <local vars>
	// local_decl	: type_spec IDENT '[' LITERAL ']' ';'
	static int initVal(Local_declContext ctx) {
		return Integer.parseInt(ctx.LITERAL().getText());
	}

	static boolean isArrayDecl(Local_declContext ctx) {
		return ctx.getChildCount() == 6;
	}

	static boolean isDeclWithInit(Local_declContext ctx) {
		return ctx.getChildCount() == 5 ;
	}

	static boolean isVoidF(Fun_declContext ctx) {
			// <Fill in>
		return ctx.getChild(0).toString().equals("void");
	}

	static boolean isIntReturn(Return_stmtContext ctx) {
		return ctx.getChildCount() ==3;
	}


	static boolean isVoidReturn(Return_stmtContext ctx) {
		return ctx.getChildCount() == 2;
	}

	// <information extraction>
	static String getStackSize(Fun_declContext ctx) {
		return "32";
	}
	static String getLocalVarSize(Fun_declContext ctx) {
		return "32";
	}
	static String getTypeText(Type_specContext typespec) {
			// <Fill in>
		return typespec.toString();
	}

	// params
	static String getParamName(ParamContext param) {
		// <Fill in>
		return param.IDENT().toString();
	}

	static String getParamTypesText(ParamsContext params) {
		String typeText = "";

		for(int i = 0; i < params.param().size(); i++) {
			Type_specContext typespec = (Type_specContext)  params.param(i).getChild(0);
			typeText += getTypeText(typespec); // + ";";
		}
		return typeText;
	}

	static String getLocalVarName(Local_declContext local_decl) {
		// <Fill in>
		return local_decl.IDENT().toString();
	}

	static String getFunName(Fun_declContext ctx) {
		// <Fill in>
		return ctx.IDENT().toString();
	}

	static String getFunName(ExprContext ctx) {
		// <Fill in>
		return ctx.IDENT().toString();
	}

	static boolean noElse(If_stmtContext ctx) {
		return ctx.getChildCount() == 5;
	}

  }

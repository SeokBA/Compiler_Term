package listener.main.bytecode;

import generated.MiniCParser;
import generated.MiniCParser.Fun_declContext;
import generated.MiniCParser.Local_declContext;
import generated.MiniCParser.Var_declContext;

import java.util.HashMap;
import java.util.Map;

import static listener.main.bytecode.BytecodeGenListenerHelper.getFunName;


public class BytecodeSymbolTable {
	enum Type {
		INT, INTARRAY, VOID, ERROR
	}

	static public class VarInfo {
		Type type;
		int id;
		int initVal;

		public VarInfo(Type type,  int id, int initVal) {
			this.type = type;
			this.id = id;
			this.initVal = initVal;
		}
		public VarInfo(Type type,  int id) {
			this.type = type;
			this.id = id;
			this.initVal = 0;
		}
	}

	static public class FInfo {
		public String sigStr;
	}

	// add
	static public class ArrInfo extends VarInfo{
		VarInfo[] varArr;
		int size;

		public ArrInfo(Type type, int id, int size) {
			super(type, id);
			VarInfo[] varArr = new VarInfo[size];
			for(int i = 0; i < size; i++)
				varArr[i] = new VarInfo(type, id);
		}

		public void idxAssignment(int index, VarInfo var){
			varArr[index].initVal = var.initVal;
		}

		public void idxAssignment(int index, int initVal){
			varArr[index].initVal = initVal;
		}
	}
	// add End

	private Map<String, VarInfo> _lsymtable = new HashMap<>();	// local v.
	private Map<String, VarInfo> _gsymtable = new HashMap<>();	// global v.
	private Map<String, FInfo> _fsymtable = new HashMap<>();	// function


	private int _globalVarID = 0;
	private int _localVarID = 0;
	private int _labelID = 0;
	private int _tempVarID = 0;

	BytecodeSymbolTable(){
		initFunDecl();
		initFunTable();
	}

	void initFunDecl(){		// at each func decl
		_localVarID = 0;
		_labelID = 0;
		_tempVarID = 32;
		_lsymtable = new HashMap<>();
	}

	void putLocalVar(String varname, Type type){
		//<Fill here>
		_lsymtable.put(varname, new VarInfo(type, _localVarID++));
	}

	void putGlobalVar(String varname, Type type){
		//<Fill here>
		_gsymtable.put(varname, new VarInfo(type, _globalVarID++));
	}

	void putGlobalArr(String varname, Type type, int size){
		//<Fill here>
		_gsymtable.put(varname, new ArrInfo(type, _globalVarID++, size));
	}

	void putLocalVarWithInitVal(String varname, Type type, int initVar){
		//<Fill here>
		_lsymtable.put(varname, new VarInfo(type, _localVarID++, initVar));
	}
	void putGlobalVarWithInitVal(String varname, Type type, int initVar){
		//<Fill here>
		_gsymtable.put(varname, new VarInfo(type, _globalVarID++, initVar));
	}

	void putParams(MiniCParser.ParamsContext params) {
		for(int i = 0; i < params.param().size(); i++) {
		//<Fill here>
			MiniCParser.ParamContext param = params.param(i);
			_lsymtable.put(param.IDENT().toString(), new VarInfo(Type.INT, _localVarID++));
		}
	}

	private void initFunTable() {
		FInfo printlninfo = new FInfo();
		printlninfo.sigStr = "java/io/PrintStream/println(I)V";

		FInfo maininfo = new FInfo();
		maininfo.sigStr = "main([Ljava/lang/String;)V";
		_fsymtable.put("_print", printlninfo);
		_fsymtable.put("main", maininfo);
	}

	public String getFunSpecStr(String fname) {
		// <Fill here>
		return _fsymtable.get(fname).sigStr;
	}

	public String getFunSpecStr(Fun_declContext ctx) {
		// <Fill here>
		return _fsymtable.get(ctx.getText()).sigStr;
	}

	public String putFunSpecStr(Fun_declContext ctx) {
		String fname = getFunName(ctx);
		String argtype = "";
		String rtype = "";
		String res = "";

		// <Fill here>
		MiniCParser.ParamsContext params = ctx.params();
		for (int i = 0; i < params.param().size(); i++) {
			if (params.param(i).type_spec().INT() != null)
				argtype += "I";
		}

		if (ctx.type_spec().INT() != null)
			rtype = "I";

		res =  fname + "(" + argtype + ")" + rtype;

		FInfo finfo = new FInfo();
		finfo.sigStr = res;
		_fsymtable.put(fname, finfo);

		return res;
	}

	String getVarId(String name){
		// <Fill here>
		return String.valueOf(_lsymtable.get(name).id);
	}

	Type getVarType(String name){
		VarInfo lvar = (VarInfo) _lsymtable.get(name);
		if (lvar != null) {
			return lvar.type;
		}

		VarInfo gvar = (VarInfo) _gsymtable.get(name);
		if (gvar != null) {
			return gvar.type;
		}

		return Type.ERROR;
	}
	String newLabel() {
		return "label" + _labelID++;
	}

	String newTempVar() {
		String id = "";
		return id + _tempVarID--;
	}

	// global
	public String getVarId(Var_declContext ctx) {
		// <Fill here>
		return String.valueOf(_gsymtable.get(ctx.getText()).id);
	}

	// local
	public String getVarId(Local_declContext ctx) {
		String sname = "";
		sname += getVarId(ctx.IDENT().getText());
		return sname;
	}
}

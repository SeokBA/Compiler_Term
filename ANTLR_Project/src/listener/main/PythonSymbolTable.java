package listener.main;

import generated.MiniCParser;
import generated.MiniCParser.Fun_declContext;
import generated.MiniCParser.Local_declContext;
import generated.MiniCParser.Var_declContext;

import java.util.HashMap;
import java.util.Map;

import static listener.main.PythonGenListenerHelper.getFunName;


public class PythonSymbolTable {
    enum Type {
        INT, INTARRAY, VOID, ERROR
    }

    static public class VarInfo {
        Type type;
        int id;
        int initVal;

        public VarInfo(Type type, int id, int initVal) {
            this.type = type;
            this.id = id;
            this.initVal = initVal;
        }

        public VarInfo(Type type, int id) {
            this.type = type;
            this.id = id;
            this.initVal = 0;
        }
    }

    static public class FInfo {
        public String sigStr;
    }

    private Map<String, VarInfo> _lsymtable = new HashMap<>();    // local v.
    private Map<String, VarInfo> _gsymtable = new HashMap<>();    // global v.
    private Map<String, FInfo> _fsymtable = new HashMap<>();    // function


    public static boolean hasFlag = false;
    private int _globalVarID = 0;
    private int _localVarID = 0;
    private int _labelID = 0;
    private int _tempVarID = 0;

    PythonSymbolTable() {
        initFunDecl();
        initFunTable();
    }

    void initFunDecl() {        // at each func decl
        _localVarID = 0;
        _labelID = 0;
        _tempVarID = 32;
        _lsymtable = new HashMap<>();
    }

    void putLocalVar(String varname, Type type) {
        //<Fill here>
        _lsymtable.put(varname, new VarInfo(type, _localVarID++));

    }

    void putGlobalVar(String varname, Type type) {
        //<Fill here>
        _gsymtable.put(varname, new VarInfo(type, _globalVarID++));

    }

    void putLocalVarWithInitVal(String varname, Type type, int initVar) {
        //<Fill here>
        _lsymtable.put(varname, new VarInfo(type, _localVarID++, initVar));
    }

    void putGlobalVarWithInitVal(String varname, Type type, int initVar) {
        //<Fill here>
        _gsymtable.put(varname, new VarInfo(type, _globalVarID++, initVar));
    }

    void putParams(MiniCParser.ParamsContext params) {
        for (int i = 0; i < params.param().size(); i++) {
            //<Fill here>
            MiniCParser.ParamContext param = params.param(i);
            _lsymtable.put(param.IDENT().toString(), new VarInfo(Type.INT, _localVarID++));
        }
    }

    private void initFunTable() {
        FInfo printlninfo = new FInfo();
        printlninfo.sigStr = "print";
        _fsymtable.put("_print", printlninfo);
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
                argtype += "int";
        }

        if (ctx.type_spec().INT() != null)
            rtype = "int";

        res = fname + "(" + argtype + ")" + rtype;

        FInfo finfo = new FInfo();
        finfo.sigStr = res;
        _fsymtable.put(fname, finfo);

        return res;
    }

    boolean hasFunName(String name) {
        return _fsymtable.containsKey(name);
    }

    boolean hasLocalName(String name) {
        return _lsymtable.containsKey(name);
    }

    boolean hasGlobalName(String name) {
        return _gsymtable.containsKey(name);
    }

    String getVarId(String name) {
        // <Fill here>
        return String.valueOf(_lsymtable.get(name).id);
    }

    Type getVarType(String name) {
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

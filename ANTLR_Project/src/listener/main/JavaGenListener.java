package listener.main;

import generated.MiniCBaseListener;
import generated.MiniCParser;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

public class JavaGenListener extends MiniCBaseListener implements ParseTreeListener {
    ParseTreeProperty<String> newTexts = new ParseTreeProperty<String>();
    SymbolTable symbolTable = new SymbolTable();
    TranslationGUI.setAddressListener setAddressListener;


    public void setGUI(TranslationGUI.setAddressListener setAddressListener){
        this.setAddressListener = setAddressListener;
    }

    @Override
    public void exitProgram(MiniCParser.ProgramContext ctx) {
        String programStr = "";
        // System.out.println(programStr);
        if (setAddressListener != null)
            setAddressListener.outputData = programStr;
    }
}
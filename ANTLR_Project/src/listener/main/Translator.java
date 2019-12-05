package listener.main;

import generated.MiniCLexer;
import generated.MiniCParser;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class Translator {
    enum OPTIONS {
        PRETTYPRINT, BYTECODEGEN, UCODEGEN, PYTHON, JAVA, ERROR
    }

    private static OPTIONS getOption(String[] args) {
        if (args.length < 1)
//            return OPTIONS.JAVA;
            return OPTIONS.PYTHON;
//			return OPTIONS.BYTECODEGEN;

        if (args[0].startsWith("-p")
                || args[0].startsWith("-P"))
            return OPTIONS.PRETTYPRINT;

        if (args[0].startsWith("-b")
                || args[0].startsWith("-B"))
            return OPTIONS.BYTECODEGEN;

        if (args[0].startsWith("-u")
                || args[0].startsWith("-U"))
            return OPTIONS.UCODEGEN;

        if (args[0].startsWith("-py")
                || args[0].startsWith("-Py"))
            return OPTIONS.PYTHON;
        if (args[0].startsWith("-j")
                || args[0].startsWith("-J"))
            return OPTIONS.JAVA;

        return OPTIONS.ERROR;
    }

    public static void main(String[] args) throws Exception {
        CharStream codeCharStream = CharStreams.fromFileName("/Users/min-yungi/Desktop/Git/Compiler_Term/ANTLR_Project/test.c");
        MiniCLexer lexer = new MiniCLexer(codeCharStream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MiniCParser parser = new MiniCParser(tokens);
        ParseTree tree = parser.program();

        ParseTreeWalker walker = new ParseTreeWalker();
        switch (getOption(args)) {
            case PRETTYPRINT:
                // walker.walk(new MiniCPrintListener(), tree );
                break;
            case BYTECODEGEN:
                walker.walk(new BytecodeGenListener(), tree);
                break;
            case UCODEGEN:
                // walker.walk(new UCodeGenListener(), tree );
                break;
            case PYTHON:
                walker.walk(new PythonGenListener(), tree);
//            case JAVA:
//                walker.walk(new JavaGenListener(), tree);
            default:
                break;
        }
    }
}
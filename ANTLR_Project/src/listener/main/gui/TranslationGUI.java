package listener.main.gui;

import generated.MiniCLexer;
import generated.MiniCParser;
import listener.main.bytecode.BytecodeGenListener;
import listener.main.java.JavaGenListener;
import listener.main.python.PythonGenListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.StringTokenizer;

public class TranslationGUI extends JFrame {
    CharStream codeCharStream;
    MiniCLexer lexer;
    CommonTokenStream tokens;
    MiniCParser parser;
    ParseTree tree;
    ParseTreeWalker walker;

    BytecodeGenListener bytecodeGenListener;
    JavaGenListener javaGenListener;
    PythonGenListener pythonGenListener;

    Container contentPanel;

    JPanel pnlSelectFile;

    JButton btnFileSelect;
    JButton btnTranslate;

    JLabel lbFileAddress;
    JLabel lbSelectLang;

    JTextArea taFileAddress;
    JTextArea taConsole;

    JScrollPane scrollConsole;

    JComboBox<String> cbSelectLang;

    setAddressListener setAddressListener = new setAddressListener();

    public static void main(String[] args) {
        new TranslationGUI();
        System.out.println("TranslationGUI Start\n");
    }

    public TranslationGUI() {
        // main
        setTitle("Translation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(250, 250, 380, 300);
        contentPanel = new JPanel();
        ((JComponent) contentPanel).setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPanel);
        contentPanel.setLayout(null);


        // Panel
        pnlSelectFile = new JPanel();
        pnlSelectFile.setLayout(null);
        pnlSelectFile.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Select File", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        pnlSelectFile.setBounds(20, 20, 340, 240);
        contentPanel.add(pnlSelectFile);


        // Button
        btnFileSelect = new JButton("File Select");
        btnFileSelect.setBounds(40, 100, 120, 25);
        btnFileSelect.addActionListener(setAddressListener);
        pnlSelectFile.add(btnFileSelect);


        btnTranslate = new JButton("Translate");
        btnTranslate.setBounds(180, 100, 120, 25);
        btnTranslate.addActionListener(setAddressListener);
        pnlSelectFile.add(btnTranslate);


        // Label
        lbFileAddress = new JLabel("MiniC File : ");
        lbFileAddress.setBounds(40, 30, 120, 25);
        pnlSelectFile.add(lbFileAddress);

        lbSelectLang = new JLabel("Language : ");
        lbSelectLang.setBounds(40, 60, 120, 25);
        pnlSelectFile.add(lbSelectLang);


        // Text Area
        taFileAddress = new JTextArea("  Destination Address");
        taFileAddress.setBounds(130, 30, 170, 25);
        taFileAddress.setEnabled(false);
        pnlSelectFile.add(taFileAddress);

        taConsole = new JTextArea();
        taConsole.setBounds(20, 135, 300, 80);
        taConsole.setEnabled(false);
        pnlSelectFile.add(taConsole);


        // scroll
        scrollConsole = new JScrollPane(taConsole);
        scrollConsole.setBounds(20, 135, 300, 80);
        pnlSelectFile.add(scrollConsole);


        // ComboBox
        cbSelectLang = new JComboBox<>(new String[]{"Bytecode", "Java", "Python"});
        cbSelectLang.setBounds(110, 60, 190, 25);
        pnlSelectFile.add(cbSelectLang);


        setVisible(true);
    }

    public class setAddressListener implements ActionListener {
        JFileChooser fileChooser, folderChooser;
        StringTokenizer stringTokenizer;
        String inputFileName;
        String outputFileName;
        public String outputData;
        String extension;
        boolean exception = false;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == btnFileSelect) {
                fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("MiniC 파일 선택");
                fileChooser.setFileFilter(new FileNameExtensionFilter("MiniC FILES", "*.c", ".c", "c"));
                if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
                    JOptionPane.showMessageDialog(null, "파일을 선택하지 않았습니다.", "경고", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                inputFileName = fileChooser.getSelectedFile().getPath();
                stringTokenizer = new StringTokenizer(inputFileName, "/");
                while (stringTokenizer.hasMoreTokens())
                    outputFileName = stringTokenizer.nextToken();
                stringTokenizer = new StringTokenizer(outputFileName, ".");
                outputFileName = stringTokenizer.nextToken();
                taFileAddress.setText(inputFileName);
                setConsole("Source File Path : " + inputFileName);
            }

            if (e.getSource() == btnTranslate) {
                if (taFileAddress.getText().equals("  Destination Address")) {
                    JOptionPane.showMessageDialog(null, "MiniC 파일을 선택하지 않았습니다.", "경고", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                try {
                    codeCharStream = CharStreams.fromFileName(taFileAddress.getText());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                lexer = new MiniCLexer(codeCharStream);
                tokens = new CommonTokenStream(lexer);
                parser = new MiniCParser(tokens);
                tree = parser.program();

                walker = new ParseTreeWalker();

                switch (cbSelectLang.getSelectedIndex()) {
                    case 0:
                        bytecodeGenListener = new BytecodeGenListener();
                        bytecodeGenListener.setGUI(this);
                        try {
                            walker.walk(bytecodeGenListener, tree);
                            setConsole("\nTranslate Complete (MiniC -> Bytecode)");
                            extension = ".j";
                        } catch (Exception err) {
                            setConsole("Translation Error");
                        }
                        break;
                    case 1:
                        javaGenListener = new JavaGenListener();
                        javaGenListener.setGUI(this);
                        try {
                            walker.walk(javaGenListener, tree);
                            setConsole("\nTranslate Complete (MiniC -> Java)");
                            extension = ".java";
                        } catch (Exception err) {
                            setConsole("\nTranslation Error");
                        }
                        break;
                    case 2:
                        pythonGenListener = new PythonGenListener();
                        pythonGenListener.setGUI(this);
                        try {
                            walker.walk(pythonGenListener, tree);
                            setConsole("Translate Complete (MiniC -> Python)");
                            extension = ".py";
                        } catch (Exception err) {
                            setConsole("Translation Error");
                        }
                        break;
                    default:
                        break;
                }

                if(exception){ // walker 실행 중 오류가 있었을 시,
                    setConsole(outputData);
                    return;
                }

                folderChooser = new JFileChooser();
                folderChooser.setDialogTitle("저장할 폴더 선택");
                folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                folderChooser.setAcceptAllFileFilterUsed(false);

                if (folderChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
                    JOptionPane.showMessageDialog(null, "폴더를 선택하지 않았습니다.", "경고", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String saveFileName = folderChooser.getSelectedFile().getPath() + "/" + outputFileName + extension;
                setConsole("Save Path : " + saveFileName);
                try {
                    OutputStream outputStream = new FileOutputStream(saveFileName);
                    byte[] by = outputData.getBytes();
                    outputStream.write(by);
                    setConsole("Save Complete");
                } catch (IOException ex) {
                    setConsole("Save Error");
                    ex.printStackTrace();
                }
            }
        }

        public void setException(){
            exception = true;
        }
    }

    public void setConsole(String str) {
        System.out.println(str);
        taConsole.setText(taConsole.getText() + str + "\n\n");
    }
}

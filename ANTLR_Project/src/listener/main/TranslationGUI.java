package listener.main;

import generated.MiniCLexer;
import generated.MiniCParser;
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

class TranslationGUI extends JFrame {
    CharStream codeCharStream;
    MiniCLexer lexer;
    CommonTokenStream tokens;
    MiniCParser parser;
    ParseTree tree;
    ParseTreeWalker walker;
    String output;

    BytecodeGenListener bytecodeGenListener;

    Container contentPanel;

    JPanel pnlSelectFile;

    JButton btnFileSelect;
    JButton btnTranslate;

    JLabel lbFileAddress;
    JLabel lbSelectLang;

    JTextArea taFileAddress;

    JComboBox<String> cbSelectLang;

    setAddressListener setAddressListener = new setAddressListener();

    public static void main(String[] args) {
        new TranslationGUI();
    }

    public TranslationGUI() {
        // main
        setTitle("Translation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(250, 250, 400, 250);
        contentPanel = new JPanel();
        ((JComponent) contentPanel).setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPanel);
        contentPanel.setLayout(null);


        // Panel
        pnlSelectFile = new JPanel();
        pnlSelectFile.setLayout(null);
        pnlSelectFile.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Select File", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
        pnlSelectFile.setBounds(20, 20, 340, 170);
        contentPanel.add(pnlSelectFile);


        // Button
        btnFileSelect = new JButton("File Select");
        btnFileSelect.setBounds(40, 120, 120, 25);
        btnFileSelect.addActionListener(setAddressListener);
        pnlSelectFile.add(btnFileSelect);


        btnTranslate = new JButton("Translate");
        btnTranslate.setBounds(180, 120, 120, 25);
        btnTranslate.addActionListener(setAddressListener);
        pnlSelectFile.add(btnTranslate);


        // Label
        lbFileAddress = new JLabel("MiniC File (.c) : ");
        lbFileAddress.setBounds(40, 30, 120, 25);
        pnlSelectFile.add(lbFileAddress);

        lbSelectLang = new JLabel("Language : ");
        lbSelectLang.setBounds(40, 70, 120, 25);
        pnlSelectFile.add(lbSelectLang);


        // Text Area
        taFileAddress = new JTextArea("  Destination Address");
        taFileAddress.setBounds(130, 30, 170, 25);
        taFileAddress.setEnabled(false);
        pnlSelectFile.add(taFileAddress);


        // ComboBox
        cbSelectLang = new JComboBox<>(new String[]{"Bytecode", "Java", "Python"});
        cbSelectLang.setBounds(110, 70, 190, 25);
        pnlSelectFile.add(cbSelectLang);


        setVisible(true);
    }

    class setAddressListener implements ActionListener {
        JFileChooser fileChooser, folderChooser;
        StringTokenizer stringTokenizer;
        String inputFileName, outputFileName;

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
                while(stringTokenizer.hasMoreTokens())
                    outputFileName = stringTokenizer.nextToken();
                stringTokenizer = new StringTokenizer(outputFileName, ".");
                outputFileName = stringTokenizer.nextToken();
                taFileAddress.setText(inputFileName);
            }

            if (e.getSource() == btnTranslate) {
                if (taFileAddress.getText().equals("  Destination Address")){
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
                        walker.walk(bytecodeGenListener, tree);
                        output = bytecodeGenListener.getTexts();
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    default:
                        break;
                }

                // todo: 변환된 파일 저장, 생성자에서 저장 위치를 받아 exitProgram() 에서 newTest.get(ctx)를 파일로 저장하게 함
                folderChooser = new JFileChooser();
                folderChooser.setDialogTitle("저장할 폴더 선택");
                folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                folderChooser.setAcceptAllFileFilterUsed(false);

                if (folderChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
                    JOptionPane.showMessageDialog(null, "폴더를 선택하지 않았습니다.", "경고", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String saveFileName = folderChooser.getSelectedFile().getPath() + "/" + outputFileName + ".j";
                System.out.println(saveFileName);
                try {
                    OutputStream outputStream = new FileOutputStream(saveFileName);
                    byte[] by = output.getBytes();
                    outputStream.write(by);
                    System.out.println("Complete");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
}

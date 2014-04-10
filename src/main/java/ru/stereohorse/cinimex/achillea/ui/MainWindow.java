package ru.stereohorse.cinimex.achillea.ui;


import ru.stereohorse.cinimex.achillea.XmlParser;
import ru.stereohorse.cinimex.achillea.model.XmlElement;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class MainWindow extends JFrame {
    private static final String WINDOW_TITLE = "Achillea";
    private static final String BTN_OPEN_TITLE = "Открыть XSD";
    private static final String BTN_SAVE_TITLE = "Сохранить CSV";
    private static final String INITIAL_ROOT = "Выберите XSD-схему";

    private static final double WINDOW_SIZE_SCALE = 8d / 10d;


    private XmlParser xmlParser;

    private XmlElement currentDocumentRoot;

    private JTree xsdTree = new JTree(new DefaultMutableTreeNode(INITIAL_ROOT));

    public MainWindow(XmlParser xmlParser) {
        super(WINDOW_TITLE);

        this.xmlParser = xmlParser;

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.LINE_AXIS));

        createChooseXsdBtn();
        createChooseElementTree();
        createSaveBtn();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSizes();
    }

    private void createChooseXsdBtn() {
        JButton btnChooseXsd = new JButton(BTN_OPEN_TITLE);
        btnChooseXsd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                JFileChooser fileChooser = new JFileChooser();
                if (fileChooser.showOpenDialog(MainWindow.this.getContentPane()) != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                try {
                    File xsd = fileChooser.getSelectedFile();
                    currentDocumentRoot = xmlParser.getXmlElements(xsd);
                    xsdTree.setModel(new DefaultTreeModel(createTree(new DefaultMutableTreeNode(xsd.getName()), currentDocumentRoot)));
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MainWindow.this, e.getLocalizedMessage(), null, JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        });

        this.getContentPane().add(btnChooseXsd);
    }

    private void createChooseElementTree() {
        this.getContentPane().add(new JScrollPane(xsdTree));
    }

    private void createSaveBtn() {
        JButton btnSave = new JButton(BTN_SAVE_TITLE);
        this.getContentPane().add(btnSave);
    }

    private void setSizes() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        double width = dimension.getWidth() * WINDOW_SIZE_SCALE;
        double height = dimension.getHeight() * WINDOW_SIZE_SCALE;

        setSize(new Dimension((int)width, (int)height));

        setLocationRelativeTo(null);
    }

    private DefaultMutableTreeNode createTree(DefaultMutableTreeNode treeNode, XmlElement element) {
        for (XmlElement child: element.getChildren()) {
            treeNode.add(createTree(new DefaultMutableTreeNode(child), child));
        }

        return treeNode;
    }
}

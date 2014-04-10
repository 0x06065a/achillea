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
import java.io.IOException;

public class MainWindow extends JFrame {
    private static final String WINDOW_TITLE = "Achillea";
    private static final String BTN_OPEN_TITLE = "Открыть XSD";
    private static final String BTN_SAVE_TITLE = "Сохранить CSV";

    private static final String FILE_SAVED = "CSV-файл сохранен";
    private static final String CHOOSE_XSD = "Выберите XSD-схему";

    private static final double WINDOW_SIZE_SCALE = 8d / 10d;

    private XmlParser xmlParser;
    private XmlElement currentDocumentRoot;

    private JFileChooser openFileChooser = new JFileChooser();
    private JFileChooser saveFileChooser = new JFileChooser();
    private JTree xsdTree = new JTree(new DefaultMutableTreeNode(CHOOSE_XSD));

    public MainWindow(XmlParser xmlParser) {
        super(WINDOW_TITLE);

        this.xmlParser = xmlParser;

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.LINE_AXIS));

        createChooseXsdBtn();
        createChooseElementTree();
        createSaveBtn();

        setSizes();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void createChooseXsdBtn() {
        JButton btnChooseXsd = new JButton(BTN_OPEN_TITLE);
        btnChooseXsd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (openFileChooser.showOpenDialog(MainWindow.this) != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                try {
                    File xml = openFileChooser.getSelectedFile();
                    currentDocumentRoot = xmlParser.getXmlElements(xml);
                    xsdTree.setModel(new DefaultTreeModel(createTree(new DefaultMutableTreeNode(xml.getName()), currentDocumentRoot)));
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
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (currentDocumentRoot == null) {
                    JOptionPane.showMessageDialog(MainWindow.this, CHOOSE_XSD, null, JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (saveFileChooser.showSaveDialog(MainWindow.this) != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                try {
                    xmlParser.toCsv(currentDocumentRoot, saveFileChooser.getSelectedFile());
                    JOptionPane.showMessageDialog(MainWindow.this, FILE_SAVED);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(MainWindow.this, e.getLocalizedMessage(), null, JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        });

        this.getContentPane().add(btnSave);
    }

    private void setSizes() {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        double width = dimension.getWidth() * WINDOW_SIZE_SCALE;
        double height = dimension.getHeight() * WINDOW_SIZE_SCALE;

        setSize(new Dimension((int) width, (int) height));

        setLocationRelativeTo(null);
    }

    private DefaultMutableTreeNode createTree(DefaultMutableTreeNode treeNode, XmlElement element) {
        for (XmlElement child : element.getChildren()) {
            treeNode.add(createTree(new DefaultMutableTreeNode(child), child));
        }

        return treeNode;
    }
}

package ru.stereohorse.cinimex.achillea.ui;


import ru.stereohorse.cinimex.achillea.model.Csv;
import ru.stereohorse.cinimex.achillea.model.XmlNode;
import ru.stereohorse.cinimex.achillea.model.XsdSchema;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class MainWindow extends JFrame {
    private static final String WINDOW_TITLE = "Achillea";
    private static final String BTN_OPEN_TITLE = "Открыть XSD";
    private static final String BTN_SAVE_TITLE = "Сохранить CSV";

    private static final String FILE_SAVED = "CSV-файл сохранен";
    private static final String CHOOSE_XSD = "Выберите XSD-схему";
    private static final String CHOOSE_NODE = "Выберите узел";

    private static final double WINDOW_SIZE_SCALE = 8d / 10d;

    private XsdSchema currentSchema;

    private final JFileChooser openFileChooser = new JFileChooser();
    private final JFileChooser saveFileChooser = new JFileChooser();
    private final JTree xsdTree = new JTree(new DefaultMutableTreeNode(CHOOSE_XSD));

    public MainWindow() {
        super(WINDOW_TITLE);

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
                    currentSchema = new XsdSchema(xml);
                    xsdTree.setModel(new DefaultTreeModel(createTree(new DefaultMutableTreeNode(xml.getName()), currentSchema.getRoot())));
                    xsdTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
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
                if (currentSchema == null) {
                    JOptionPane.showMessageDialog(MainWindow.this, CHOOSE_XSD, null, JOptionPane.WARNING_MESSAGE);
                    return;
                }

                DefaultMutableTreeNode treeSelection = (DefaultMutableTreeNode) xsdTree.getLastSelectedPathComponent();
                if (treeSelection == null) {
                    JOptionPane.showMessageDialog(MainWindow.this, CHOOSE_NODE, null, JOptionPane.WARNING_MESSAGE);
                    return;
                }

                try {
                    Csv csv = new Csv((XmlNode)treeSelection.getUserObject());

                    if (saveFileChooser.showSaveDialog(MainWindow.this) != JFileChooser.APPROVE_OPTION) {
                        return;
                    }

                    try (OutputStream out = new BufferedOutputStream(new FileOutputStream(saveFileChooser.getSelectedFile()))) {
                        try (OutputStreamWriter writer = new OutputStreamWriter(out)) {
                            writer.write(csv.toString());
                            JOptionPane.showMessageDialog(MainWindow.this, FILE_SAVED);
                        }
                    }

                    Desktop.getDesktop().open(saveFileChooser.getSelectedFile().getParentFile());
                } catch (Exception e) {
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

    private DefaultMutableTreeNode createTree(DefaultMutableTreeNode treeNode, XmlNode element) {
        for (XmlNode child : element.getChildren()) {
            treeNode.add(createTree(new DefaultMutableTreeNode(child), child));
        }

        return treeNode;
    }
}

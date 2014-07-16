package ru.stereohorse.cinimex.achillea.ui;


import ru.stereohorse.cinimex.achillea.model.Csv;
import ru.stereohorse.cinimex.achillea.model.XmlNode;
import ru.stereohorse.cinimex.achillea.model.XsdSchema;
import ru.stereohorse.cinimex.achillea.model.formats.CsvFormatter;
import ru.stereohorse.cinimex.achillea.model.formats.FullFormat;
import ru.stereohorse.cinimex.achillea.model.formats.MappingStyleFormat;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.stream.XMLStreamException;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;

public class MainWindow extends JFrame {
    private static final String WINDOW_TITLE = "Achillea";
    private static final String BTN_OPEN_TITLE = "Открыть XSD";
    private static final String BTN_SAVE_FULL_TITLE = "Сохранить полную CSV";
    private static final String BTN_SAVE_MAPPING_STYLE_TITLE = "Сохранить короткую CSV";

    private static final String FILE_SAVED = "CSV-файл сохранен";
    private static final String CHOOSE_XSD = "Выберите XSD-схему";
    private static final String CHOOSE_NODE = "Выберите узел";

    private static final double WINDOW_SIZE_SCALE = 8d / 10d;
    private static final String ICON = "icon.png";

    private XsdSchema currentSchema;

    private final JFileChooser openFileChooser = new JFileChooser();
    private final JFileChooser saveFileChooser = new JFileChooser();
    private final JTree xsdTree = new JTree(new DefaultMutableTreeNode(CHOOSE_XSD));

    public MainWindow() {
        super(WINDOW_TITLE);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.LINE_AXIS));

        createChooseXsdBtn();
        createChooseElementTree();
        createSaveButtons();

        setSizes();
        setIcon();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void setIcon() {
        Image img = Toolkit.getDefaultToolkit().createImage(getClass().getResource(ICON));
        setIconImage(img);
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
                    openFile(openFileChooser.getSelectedFile());
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MainWindow.this, e.getLocalizedMessage(), null, JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            }
        });

        this.getContentPane().add(btnChooseXsd);
    }

    private void openFile(File xml) throws IOException, XMLStreamException {
        currentSchema = new XsdSchema(xml);
        xsdTree.setModel(new DefaultTreeModel(createTree(new DefaultMutableTreeNode(xml.getName()), currentSchema.getRoot())));
        xsdTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    private void createChooseElementTree() {
        xsdTree.setTransferHandler(new TransferHandler() {
            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }

                Transferable transferable = support.getTransferable();

                try {
                    java.util.List<File> files = (java.util.List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    if (files.size() != 1) {
                        return false;
                    }

                    openFile(files.get(0));

                    return true;
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MainWindow.this, e.getLocalizedMessage(), null, JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }

                return false;
            }

            @Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }
        });

        this.getContentPane().add(new JScrollPane(xsdTree));
    }

    private void createSaveButtons() {
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.PAGE_AXIS));

        JButton btnSaveInFullFormat = new JButton(BTN_SAVE_FULL_TITLE);
        btnSaveInFullFormat.addActionListener(new SaveAction(new FullFormat()));
        buttonsPanel.add(btnSaveInFullFormat);

        JButton btnSaveInMappingStyle = new JButton(BTN_SAVE_MAPPING_STYLE_TITLE);
        btnSaveInMappingStyle.addActionListener(new SaveAction(new MappingStyleFormat()));
        buttonsPanel.add(btnSaveInMappingStyle);

        this.getContentPane().add(buttonsPanel);
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

    private class SaveAction implements ActionListener {
        private CsvFormatter formatter;

        public SaveAction(CsvFormatter formatter) {
            this.formatter = formatter;
        }

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
                        writer.write(formatter.format(csv));
                        JOptionPane.showMessageDialog(MainWindow.this, FILE_SAVED);
                    }
                }

                Desktop.getDesktop().open(saveFileChooser.getSelectedFile().getParentFile());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(MainWindow.this, e.getLocalizedMessage(), null, JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }
}

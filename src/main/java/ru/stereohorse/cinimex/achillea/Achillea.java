package ru.stereohorse.cinimex.achillea;


import ru.stereohorse.cinimex.achillea.ui.MainWindow;

import javax.swing.*;

class Achillea {
    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        // TODO: check args for cmd mode

        new Achillea().startUI();
    }

    private void startUI() throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }
}

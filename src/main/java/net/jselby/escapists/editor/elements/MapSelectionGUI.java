package net.jselby.escapists.editor.elements;

import net.jselby.escapists.EscapistsEditor;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

/**
 * A simple map selection GUI
 */
public class MapSelectionGUI extends JFrame {
    private RenderView oldView;

    public MapSelectionGUI(final EscapistsEditor editor) throws IOException {
        String[] maps = editor.getMaps();
        setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        add(Box.createVerticalStrut(20));
        final JLabel descriptor = new JLabel("Select a map:");
        descriptor.setAlignmentX(CENTER_ALIGNMENT);
        add(descriptor);

        add(Box.createVerticalStrut(20));

        final JComboBox mapsSelector = new JComboBox(maps);
        mapsSelector.setAlignmentX(CENTER_ALIGNMENT);
        mapsSelector.setMaximumSize(new Dimension(150, 30));
        add(mapsSelector);

        add(Box.createVerticalStrut(20));

        // Allow for custom maps
        final JButton custom = new JButton("Other...");
        custom.setAlignmentX(CENTER_ALIGNMENT);
        custom.setMaximumSize(new Dimension(150, 30));
        add(custom);

        final JButton go = new JButton("Go!");
        go.setAlignmentX(CENTER_ALIGNMENT);
        go.setMaximumSize(new Dimension(150, 30));
        add(go);

        // Button actions
        custom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                final JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.getName().toLowerCase().endsWith(".map") || f.isDirectory()
                                || f.getName().toLowerCase().endsWith(".pmap");
                    }

                    @Override
                    public String getDescription() {
                        return ".map or .pmap";
                    }
                });

                int result = fc.showDialog(MapSelectionGUI.this, "Load");
                if (result == JFileChooser.APPROVE_OPTION) {
                    mapsSelector.setEditable(false);
                    descriptor.setText("Loading...");
                    custom.setEnabled(false);
                    go.setEnabled(false);

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                editor.edit(fc.getSelectedFile().getAbsolutePath());
                            } catch (Exception e1) {
                                JOptionPane.
                                        showConfirmDialog(MapSelectionGUI.this,
                                                "Error: " + e1.getLocalizedMessage(),
                                                "Escapists Map Editor",
                                                JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
                                e1.printStackTrace();
                                System.exit(1);
                            }

                            dispose();
                        }
                    });
                }
            }
        });

        go.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mapsSelector.setEditable(false);
                descriptor.setText("Loading...");
                custom.setEnabled(false);
                go.setEnabled(false);

                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            editor.edit((String) mapsSelector.getSelectedItem());
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }

                        dispose();
                    }
                });
            }
        });

        setSize(200, 200);
        setResizable(false);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (oldView != null) {
                    oldView.setEnabled(true);
                }
            }
        });

        setLocationRelativeTo(null);
        setTitle("Load...");
        setIconImage(ImageIO.read(getClass().getResource("/icon.png")));
        try {
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException |
                InstantiationException |
                IllegalAccessException |
                UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        setVisible(true);
    }

    public void setOldView(RenderView oldView) {
        this.oldView = oldView;
    }

    public RenderView getOldView() {
        return oldView;
    }
}

package net.jselby.escapists.editor.elements;

import net.jselby.escapists.mapping.Map;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

/**
 * A simple dialog for showing properties.
 */
public class ZonesDialog extends JDialog {
    private final MapRendererComponent renderer;
    private final Map mapToEdit;

    public ZonesDialog(RenderView parent, final MapRendererComponent renderer, final Map mapToEdit) {
        super(parent, "Zone Editor", false);

        this.renderer = renderer;
        this.mapToEdit = mapToEdit;

        JPanel scrollableContent = new JPanel();
        scrollableContent.setLayout(new GridLayout(0, 2, 0, 10));
        scrollableContent.add(Box.createVerticalStrut(10));
        scrollableContent.add(Box.createVerticalGlue());
        scrollableContent.add(Box.createVerticalGlue());
        scrollableContent.add(new JLabel("Syntax: \"x1_y1_x2_y2\" or <empty>."));
        scrollableContent.add(Box.createVerticalGlue());
        scrollableContent.add(new JLabel("Coordinates is *16 from tiles!"));

        // Build fields
        for (java.util.Map.Entry<String, Object> fieldSet : mapToEdit.getZones().entrySet()) {
            final String name = fieldSet.getKey();
            final String startingContent = ((String) fieldSet.getValue()).trim();

            JLabel label = new JLabel(name);

            final JTextField contentField = new JTextField(startingContent);
            label.setLabelFor(contentField);
            label.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 0));

            contentField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    documentEvent(e);
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    documentEvent(e);
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    documentEvent(e);
                }

                public void documentEvent(DocumentEvent evt) {
                    mapToEdit.set("Zones." + name, "");
                    int amount = contentField.getText().trim().split("_").length;
                    if (contentField.getText().trim().length() != 0 && amount != 4) {
                        contentField.setBackground(Color.red);
                        return;
                    }

                    if (contentField.getText().trim().length() != 0) {
                        for (String str : contentField.getText().split("_")) {
                            try {
                                int number = Integer.parseInt(str);
                                if (number < 0 || number > (96 * 16)) {
                                    System.out.println("Invalid number: out of range");
                                    contentField.setBackground(Color.red);
                                    return;
                                }
                            } catch (Exception err) {
                                System.out.println("Invalid number: not a number");
                                contentField.setBackground(Color.red);
                                return;
                            }
                        }
                    }

                    contentField.setBackground(Color.white);
                    if (amount == 4) {
                        mapToEdit.set("Zones." + name, contentField.getText().trim());
                    } else {
                        mapToEdit.set("Zones." + name, "");
                    }
                    renderer.refresh();
                }
            });

            // Sizing
            contentField.setMinimumSize(new Dimension(150, 50));
            scrollableContent.add(label, BorderLayout.WEST);
            scrollableContent.add(contentField, BorderLayout.CENTER);
        }

        scrollableContent.add(Box.createVerticalStrut(10));
        scrollableContent.add(Box.createVerticalGlue());

        JScrollPane scroller = new JScrollPane(scrollableContent);
        scroller.getVerticalScrollBar().setUnitIncrement(16);

        add(scroller);

        setSize(new Dimension(640, 480));
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}

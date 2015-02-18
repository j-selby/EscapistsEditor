package net.jselby.escapists.editor;

import net.jselby.escapists.Map;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.LinkedHashMap;

/**
 * A simple dialog for showing properties.
 */
public class PropertiesDialog extends JDialog {
    private final static java.util.Map<String, String> fieldsToEdit = new LinkedHashMap<>();

    static {
        fieldsToEdit.put("Prison Name", "Info.MapName");
        fieldsToEdit.put("Inmates (Number)", "Info.Inmates");
        fieldsToEdit.put("Guards (Number)", "Info.Guards");
        fieldsToEdit.put("NPC Level", "Info.NPClvl");
        fieldsToEdit.put("Map Type", "Info.MapType");

        // Jobs
        fieldsToEdit.put("Starting Job (first letter CAPITAL)", "Jobs.StartingJob");
        fieldsToEdit.put("Metalshop Job Enabled (1 or 0)", "Jobs.Metalshop");
        fieldsToEdit.put("Mailman Job Enabled (1 or 0)", "Jobs.Mailman");
        fieldsToEdit.put("Kitchen Job Enabled (1 or 0)", "Jobs.Kitchen");
        fieldsToEdit.put("Gardening Job Enabled (1 or 0)", "Jobs.Gardening");
        fieldsToEdit.put("Janitor Job Enabled (1 or 0)", "Jobs.Janitor");
        fieldsToEdit.put("Woodshop Job Enabled (1 or 0)", "Jobs.Woodshop");
        fieldsToEdit.put("Library Job Enabled (1 or 0)", "Jobs.Library");
        fieldsToEdit.put("Tailor Job Enabled (1 or 0)", "Jobs.Tailor");
        fieldsToEdit.put("Deliveries Job Enabled (1 or 0)", "Jobs.Deliveries");
        fieldsToEdit.put("Laundry Job Enabled (1 or 0)", "Jobs.Laundry");
    }

    private final MapRendererComponent renderer;
    private final Map mapToEdit;

    public PropertiesDialog(RenderView parent, MapRendererComponent renderer, final Map mapToEdit) {
        super(parent, "Map Properties", true);

        this.renderer = renderer;
        this.mapToEdit = mapToEdit;

        JPanel scrollableContent = new JPanel();
        scrollableContent.setLayout(new GridLayout(0, 2, 0, 10));
        scrollableContent.add(Box.createVerticalStrut(10));
        scrollableContent.add(Box.createVerticalGlue());

        // Build fields
        for (java.util.Map.Entry<String, String> fieldSet : fieldsToEdit.entrySet()) {
            String name = fieldSet.getKey();
            final String valueSrc = fieldSet.getValue();

            String startingContent = (String) mapToEdit.get(valueSrc);

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
                    mapToEdit.set(valueSrc, contentField.getText());
                }
            });

            // Sizing
            contentField.setMinimumSize(new Dimension(150, 50));
            scrollableContent.add(label, BorderLayout.WEST);
            scrollableContent.add(contentField, BorderLayout.CENTER);
        }

        scrollableContent.add(Box.createVerticalStrut(10));
        scrollableContent.add(Box.createVerticalGlue());

        add(new JScrollPane(scrollableContent));

        setSize(new Dimension(640, 480));
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}

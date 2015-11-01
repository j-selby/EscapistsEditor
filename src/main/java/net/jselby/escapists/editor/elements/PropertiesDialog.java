package net.jselby.escapists.editor.elements;

import net.jselby.escapists.mapping.Map;
import net.jselby.escapists.utils.StringUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple dialog for showing properties.
 */
public class PropertiesDialog extends JDialog {
    private final MapRendererComponent renderer;
    private final Map mapToEdit;

    public PropertiesDialog(RenderView parent, MapRendererComponent renderer, final Map mapToEdit) {
        super(parent, "Map Properties", true);

        this.renderer = renderer;
        this.mapToEdit = mapToEdit;

        // TODO: Add dropdown menus for some of these options
        // Build fields
        ArrayList<Component[]> fieldsToEdit = new ArrayList<>();
        fieldsToEdit.add(stringField(mapToEdit, "Prison Name", "Info.MapName"));
        fieldsToEdit.add(numberField(mapToEdit, "Inmates (1 - around 20)", "Info.Inmates"));
        fieldsToEdit.add(numberField(mapToEdit, "Guards (1- around 20)", "Info.Guards"));
        fieldsToEdit.add(numberField(mapToEdit, "NPC Level (1-3 known)", "Info.NPClvl"));
        fieldsToEdit.add(stringField(mapToEdit, "Music", "Info.Music"));
        fieldsToEdit.add(stringField(mapToEdit, "Fight Frequency", "Info.FightFreq"));
        fieldsToEdit.add(stringField(mapToEdit, "Tileset", "Info.Tileset"));
        fieldsToEdit.add(stringField(mapToEdit, "Warden name", "Info.Warden"));
        fieldsToEdit.add(textBoxField(mapToEdit, "Intro text", "Info.Intro"));

        // Jobs
        fieldsToEdit.add(stringField(mapToEdit, "Starting Job (first letter CAPITAL)", "Jobs.StartingJob"));
        fieldsToEdit.add(booleanField(mapToEdit, "Metalshop Job Enabled", "Jobs.Metalshop"));
        fieldsToEdit.add(booleanField(mapToEdit, "Mailman Job Enabled", "Jobs.Mailman"));
        fieldsToEdit.add(booleanField(mapToEdit, "Kitchen Job Enabled", "Jobs.Kitchen"));
        fieldsToEdit.add(booleanField(mapToEdit, "Gardening Job Enabled", "Jobs.Gardening"));
        fieldsToEdit.add(booleanField(mapToEdit, "Janitor Job Enabled", "Jobs.Janitor"));
        fieldsToEdit.add(booleanField(mapToEdit, "Woodshop Job Enabled", "Jobs.Woodshop"));
        fieldsToEdit.add(booleanField(mapToEdit, "Library Job Enabled", "Jobs.Library"));
        fieldsToEdit.add(booleanField(mapToEdit, "Tailor Job Enabled", "Jobs.Tailor"));
        fieldsToEdit.add(booleanField(mapToEdit, "Deliveries Job Enabled", "Jobs.Deliveries"));
        fieldsToEdit.add(booleanField(mapToEdit, "Laundry Job Enabled", "Jobs.Laundry"));

        // Experimental
        fieldsToEdit.add(new Component[]{Box.createVerticalStrut(50), Box.createHorizontalStrut(1)});
        JLabel experimental = new JLabel("The following are unsupported ");
        experimental.setHorizontalAlignment(SwingConstants.RIGHT);
        JLabel experimental2 = new JLabel("and likely won't work yet.");
        experimental.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        fieldsToEdit.add(new Component[]{experimental, experimental2});

        fieldsToEdit.add(stringField(mapToEdit, "Map Type", "Info.MapType"));
        fieldsToEdit.add(stringField(mapToEdit, "Routine Set", "Info.RoutineSet"));
        fieldsToEdit.add(textBoxField(mapToEdit, "Hint (Part 1)", "Info.Hint1"));
        fieldsToEdit.add(textBoxField(mapToEdit, "Hint (Part 2)", "Info.Hint2"));
        fieldsToEdit.add(textBoxField(mapToEdit, "Hint (Part 3)", "Info.Hint3"));

        // Build GUI
        JPanel scrollableContent = new JPanel();
        scrollableContent.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        scrollableContent.add(Box.createVerticalStrut(10), c);

        // Build fields
        int y = 1;
        c.ipady = 10;
        for (Component[] components : fieldsToEdit) {
            if (components.length == 0) {
                continue;
            }

            c.gridy = y;
            c.gridx = 0;
            c.weightx = 0.3;
            c.fill = GridBagConstraints.NONE;
            c.fill = GridBagConstraints.HORIZONTAL;

            if (components[0] instanceof JComponent) {
                ((JComponent)components[0]).setAlignmentX(LEFT_ALIGNMENT);
            }
            if (components[1] instanceof JComponent) {
                ((JComponent)components[1]).setAlignmentX(LEFT_ALIGNMENT);
            }

            scrollableContent.add(components[0], c);
            c.gridx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 0.7;
            scrollableContent.add(components[1], c);
            y++;
        }
        c.fill = GridBagConstraints.NONE;
        c.ipady = 0;
        c.weightx = 0;

        c.gridy = y;

        scrollableContent.add(Box.createVerticalStrut(10), c);

        JScrollPane scrollableContainer = new JScrollPane(scrollableContent);
        scrollableContainer.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollableContainer);

        setSize(new Dimension(640, 480));
        setResizable(false);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private static JComponent[] numberField(final Map mapToEdit, String name, final String valueSrc) {
        String startingContent = (String) mapToEdit.get(valueSrc);
        startingContent = startingContent.trim();

        JLabel label = new JLabel(name);

        final JTextField contentField = new JTextField(startingContent);
        label.setLabelFor(contentField);
        label.setHorizontalTextPosition(SwingConstants.LEFT);
        label.setBorder(BorderFactory.createEmptyBorder(10, 50, 0, 0));

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

        // Force numbers only
        ((AbstractDocument) contentField.getDocument()).setDocumentFilter(new DocumentFilter() {
            Pattern regEx = Pattern.compile("\\d+");

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text,
                                AttributeSet attrs) throws BadLocationException {
                Matcher matcher = regEx.matcher(text);
                if (!matcher.matches()) {
                    return;
                }
                super.replace(fb, offset, length, text, attrs);
            }
        });

        // Sizing
        contentField.setMinimumSize(new Dimension(150, 20));
        contentField.setMaximumSize(new Dimension(150, 20));
        contentField.setPreferredSize(new Dimension(150, 20));
        contentField.setSize(new Dimension(150, 20));

        return new JComponent[]{label, contentField};
    }

    private static JComponent[] booleanField(final Map mapToEdit, String name, final String valueSrc) {
        String val = (mapToEdit.get(valueSrc) + "").trim();
        int startingContent = StringUtils.isNumber(val) ? Integer.parseInt(val) : 0;

        JLabel label = new JLabel(name);

        final JCheckBox box = new JCheckBox();
        label.setLabelFor(box);
        label.setHorizontalTextPosition(SwingConstants.LEFT);
        label.setBorder(BorderFactory.createEmptyBorder(10, 50, 0, 0));
        box.setSelected(startingContent == 1);

        box.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mapToEdit.set(valueSrc, box.isSelected() ? 1 : 0);
            }
        });

        return new JComponent[]{label, box};
    }

    private static JComponent[] textBoxField(final Map mapToEdit, String name, final String valueSrc) {
        String startingContent = (String) mapToEdit.get(valueSrc);
        if (startingContent == null) {
            startingContent = "";
        }
        startingContent = startingContent.trim();

        JLabel label = new JLabel(name);

        final JTextArea contentField = new JTextArea(startingContent);
        contentField.setLineWrap(true);
        contentField.setWrapStyleWord(true);
        label.setLabelFor(contentField);
        label.setHorizontalTextPosition(SwingConstants.LEFT);
        label.setBorder(BorderFactory.createEmptyBorder(10, 50, 0, 0));

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

        JScrollPane scrollPane = new JScrollPane(contentField);
        scrollPane.setMinimumSize(new Dimension(150, 50));
        scrollPane.setMaximumSize(new Dimension(150, 50));
        scrollPane.setPreferredSize(new Dimension(150, 50));
        scrollPane.setSize(new Dimension(150, 50));

        JPanel northOnlyPanel = new JPanel();
        northOnlyPanel.setLayout(new BorderLayout());
        northOnlyPanel.add(scrollPane, BorderLayout.NORTH);
        return new JComponent[]{label, northOnlyPanel};
    }

    private static JComponent[] stringField(final Map mapToEdit, String name, final String valueSrc) {
        if (name == null || valueSrc == null) {
            return new JComponent[0];
        }

        String startingContent = (String) mapToEdit.get(valueSrc);
        if (startingContent == null) {
            startingContent = "";
        }
        startingContent = startingContent.trim();

        JLabel label = new JLabel(name);

        final JTextField contentField = new JTextField(startingContent);
        label.setLabelFor(contentField);
        label.setHorizontalTextPosition(SwingConstants.LEFT);
        label.setBorder(BorderFactory.createEmptyBorder(10, 50, 0, 0));

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
        contentField.setMinimumSize(new Dimension(150, 20));
        contentField.setMaximumSize(new Dimension(150, 20));
        contentField.setPreferredSize(new Dimension(150, 20));
        contentField.setSize(new Dimension(150, 20));

        return new JComponent[]{label, contentField};
    }
}

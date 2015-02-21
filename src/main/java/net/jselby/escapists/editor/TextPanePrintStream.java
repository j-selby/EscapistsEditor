package net.jselby.escapists.editor;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class TextPanePrintStream extends OutputStream {

    private final JTextArea textArea;

    private final StringBuilder sb = new StringBuilder();
    private final PrintStream parent;

    public TextPanePrintStream(final JTextArea textArea) {
        parent = System.out;
        this.textArea = textArea;
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
    }

    @Override
    public void write(int b) throws IOException {
        parent.write(b);
        if (b == '\r') {
            return;
        }

        if (b == '\n') {
            final String text = "- " + sb.toString() + "\n";
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    // Shorten to X lines
                    String[] lines = textArea.getText().split("\n");
                    String newContent = "";
                    int linesToKeep = 4;
                    int pullFrom = (lines.length == linesToKeep ? 2 : 1);
                    for (int i = lines.length - pullFrom; i > lines.length - pullFrom - linesToKeep; i--) {
                        if (i < 0) {
                            continue;
                        }
                        newContent = lines[i] + "\n" + newContent;
                    }
                    newContent = text + newContent;
                    textArea.setText(newContent);
                }
            });
            sb.setLength(0);
        } else {
            sb.append((char) b);
        }
    }
}

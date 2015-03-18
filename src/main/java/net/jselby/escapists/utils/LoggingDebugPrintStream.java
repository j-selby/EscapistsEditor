package net.jselby.escapists.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Redirects a stream to a output stream and the original printstream.
 *
 * @author j_selby
 */
public class LoggingDebugPrintStream extends PrintStream {
    private PrintStream file;
    private PrintStream original;

    public LoggingDebugPrintStream(OutputStream file, PrintStream original) {
        super(file, true);
        this.file = new PrintStream(file);
        this.original = original;
    }

    @Override
    public void write(int b) {
        file.write(b);
        original.write(b);
        if (b == '\n') {
            flush();
        }
    }

    @Override
    public void flush() {
        file.flush();
        original.flush();

    }

    @Override
    public void print(boolean b) {
        file.print(b);
        original.print(b);
    }

    @Override
    public void print(char c) {
        file.print(c);
        original.print(c);
    }

    @Override
    public void print(double d) {
        file.print(d);
        original.print(d);
    }

    @Override
    public void print(float f) {
        file.print(f);
        original.print(f);
    }

    @Override
    public void print(int i) {
        file.print(i);
        original.print(i);
    }

    @Override
    public void print(long l) {
        file.print(l);
        original.print(l);
    }

    @Override
    public void print(Object obj) {
        file.print(obj);
        original.print(obj);
    }

    @Override
    public void print(char[] s) {
        file.print(s);
        original.print(s);
    }

    @Override
    public void print(String s) {
        file.print(s);
        original.print(s);
    }

    @Override
    public void println() {
        file.println();
        original.println();
    }

    @Override
    public void println(boolean x) {
        file.println(x);
        original.println(x);
    }

    @Override
    public void println(char x) {
        file.println(x);
        original.println(x);
    }

    @Override
    public void println(int x) {
        file.println(x);
        original.println(x);
    }

    @Override
    public void println(double x) {
        file.println(x);
        original.println(x);
    }

    @Override
    public void println(char[] x) {
        file.println(x);
        original.println(x);
    }

    @Override
    public void println(String x) {
        file.println(x);
        original.println(x);
    }

    @Override
    public void println(long x) {
        file.println(x);
        original.println(x);
    }

    @Override
    public void println(float x) {
        file.println(x);
        original.println(x);
    }

    @Override
    public void println(Object x) {
        file.println(x);
        original.println(x);
    }

    @Override
    public void write(byte[] b) throws IOException {
        file.write(b);
        original.write(b);
    }

    @Override
    public void write(byte[] buf, int off, int len) {
        file.write(buf, off, len);
        original.write(buf, off, len);
    }
}

package org.neo4j.shell.commands;


import org.neo4j.shell.cli.Format;
import org.neo4j.shell.log.Logger;

import java.io.PrintStream;

public class StringLogger implements Logger {

    static String NEWLINE = System.getProperty("line.separator");

    final StringBuilder sb;
    final Format format;

    public StringLogger(Format format) {
        this.format = format;
        sb = new StringBuilder();
    }

    @Override
    public PrintStream getOutputStream() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public PrintStream getErrorStream() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void printError(Throwable throwable) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void printError(String text) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public void printOut(String text) {
        sb.append(text).append(NEWLINE);
    }

    @Override
    public Format getFormat() {
        return format;
    }

    @Override
    public void setFormat(Format format) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    void clear() {
        sb.setLength(0);
    }

    String output() {
        return sb.toString();
    }
}

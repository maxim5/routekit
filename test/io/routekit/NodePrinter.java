package io.routekit;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

public class NodePrinter {
    private static final int DEFAULT_TAB = 4;

    public static <T> void println(Router.Node<T> node) {
        println(node, DEFAULT_TAB);
    }

    public static <T> void println(Router.Node<T> node, int tab) {
        println(node, tab, System.out, 0);
    }

    public static <T> String printlnToString(Router.Node<T> node) {
        return printlnToString(node, DEFAULT_TAB);
    }

    public static <T> String printlnToString(Router.Node<T> node, int tab) {
        return printlnToString(node, tab, Charset.defaultCharset());
    }

    public static <T> String printlnToString(Router.Node<T> node, int tab, Charset charset) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(stream, false, charset);
        println(node, tab, printStream, 0);
        return stream.toString(charset);
    }

    public static <T> void println(Router.Node<T> node, int tab, PrintStream out, int indent) {
        for (int i = 0; i < indent; i++) {
            out.print(' ');
        }
        out.print(node.token());
        if (node.isTerminal()) {
            out.print(" -> ");
            out.print(node.terminalRule().handler());
        }
        out.println();

        for (Router.Node<T> next : node.next()) {
            println(next, tab, out, indent + tab);
        }
    }
}

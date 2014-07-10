import java.io.*;

public class DebugLogger {

    static Writer w = null;

    public static String escape(String s) {
        s = s.replaceAll("&", "&amp;");
        s = s.replaceAll("<", "&lt;");
        s = s.replaceAll(">", "&gt;");
        s = s.replaceAll("'", "&apos;");
        s = s.replaceAll("\"", "&quot;");
        return s;
    }

    static void activate() {
        w = new OutputStreamWriter(System.err);
        try {
            w.write("<?xml version=\"1.0\" encoding='UTF-8'?><messages>\n");
        } catch (IOException e) {}
    }

    static void log(String s) {
        if (w != null)
            try {
                w.write("<message>" + s + "</message>");
                w.flush();
            } catch (IOException e) {}
    }

    static void escapeLog(String s) {
        log(escape(s));
    }

    static void closeLog() {
        if (w != null) 
            try {
                w.write("</messages>");
                w.close();
            } catch (IOException e) {
            }
    }

}
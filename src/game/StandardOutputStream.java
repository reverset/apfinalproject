package game;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class StandardOutputStream extends OutputStream {
    private final PrintStream originalStdout;
    private final PrintStream originalStderr;
    private static final int MAX_LENGTH = 20;

    // access is not synchronized but hasnt caused problems, ill just leave it for now.
    private ArrayList<String> lines = new ArrayList<>();
    private int i = 0;

    public StandardOutputStream(PrintStream originalStdout, PrintStream originalStderr) {
        this.originalStdout = originalStdout;
        this.originalStderr = originalStderr;
        lines.add("");
    }

    public void writeError(int b) {
        writeGen(b, originalStderr);
    }

    public void writeGen(int b, PrintStream original) {
        original.write(b);
        char c = (char)b;
        if (c == '\n') {
            if (lines.size() > MAX_LENGTH) {
                lines.remove(0);
            } else {
                i += 1;
            }
            lines.add("");
        }
        lines.set(i, lines.get(i)+c);
    }

    @Override
    public void write(int b) throws IOException {
        writeGen(b, originalStdout);
    }

    public List<String> getLines() {
        return lines;
    }
    
}

package models;

import java.io.*;
import java.util.*;

/**
 * Lightweight CSV reader for our FakeGameDatabase.
 * Expects a header row, which it skips automatically.
 */
public class ReadCSV implements Closeable {
    private BufferedReader reader;

    /**
     * @param resourcePath path on classpath, e.g. "db/rooms.csv"
     */
    public ReadCSV(String resourcePath) throws IOException {
        InputStream in = this.getClass()
                            .getClassLoader()
                            .getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IOException("Could not open CSV: " + resourcePath);
        }
        reader = new BufferedReader(new InputStreamReader(in));
        // skip header
        reader.readLine();
    }

    /**
     * @return next line as list of tokens, or null at EOF
     */
    public List<String> next() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return null;
        }
        List<String> tokens = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens;
    }

    @Override
    public void close() throws IOException {
        reader.close();
    }
} 
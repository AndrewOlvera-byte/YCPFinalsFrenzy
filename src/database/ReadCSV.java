package database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Utility class for reading CSV files.
 */
public class ReadCSV {
    private BufferedReader reader;
    
    /**
     * Creates a new ReadCSV object with the given CSV file.
     * 
     * @param resourceName The name of the CSV file
     * @throws IOException If the file cannot be read
     */
    public ReadCSV(String resourceName) throws IOException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("csv/" + resourceName);
        
        if (in == null) {
            throw new IOException("Couldn't find CSV file: " + resourceName);
        }
        
        this.reader = new BufferedReader(new InputStreamReader(in));
    }
    
    /**
     * Reads the next line of the CSV file and returns it as a list of strings.
     * 
     * @return A list of strings representing the next line of the CSV file,
     *         or null if there are no more lines
     * @throws IOException If there is an error reading the file
     */
    public List<String> next() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            return null;
        }
        
        List<String> values = new ArrayList<String>();
        StringTokenizer tokenizer = new StringTokenizer(line, ",", false);
        while (tokenizer.hasMoreTokens()) {
            values.add(tokenizer.nextToken().trim());
        }
        
        return values;
    }
    
    /**
     * Closes the CSV file.
     */
    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            // Ignore
        }
    }
} 
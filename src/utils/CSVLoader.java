package utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import orm.OrmManager;

public class CSVLoader {
    public static <T> void loadAndSaveCSV(String fileName, Class<T> clazz, OrmManager orm, List<T> optionalCollection) throws Exception {
        InputStream is = getFileFromResources(fileName);
        if (is == null) {
            throw new RuntimeException("CSV file not found: " + fileName);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String headerLine = reader.readLine();
            if (headerLine == null) return;

            String[] headers = headerLine.split(",");
            List<Field> fields = new ArrayList<>();
            for (String header : headers) {
                Field field = clazz.getDeclaredField(header.trim());
                field.setAccessible(true);
                fields.add(field);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] values = line.split(",", -1);
                T obj = clazz.getDeclaredConstructor().newInstance();

                for (int i = 0; i < fields.size(); i++) {
                    Field field = fields.get(i);
                    String value = values[i].trim();
                    if (value.isEmpty()) continue;

                    Class<?> type = field.getType();
                    if (type == int.class || type == Integer.class) {
                        field.set(obj, Integer.parseInt(value));
                    } else if (type == long.class || type == Long.class) {
                        field.set(obj, Long.parseLong(value));
                    } else if (type == boolean.class || type == Boolean.class) {
                        field.set(obj, Boolean.parseBoolean(value));
                    } else {
                        field.set(obj, value);
                    }
                }

                orm.save(obj);
                if (optionalCollection != null) {
                    optionalCollection.add(obj);
                }
            }
        }
    }

    private static InputStream getFileFromResources(String fileName) {
        ClassLoader classLoader = CSVLoader.class.getClassLoader();
        URL resource = classLoader.getResource(fileName);
        if (resource == null) return null;
        return classLoader.getResourceAsStream(fileName);
    }
}

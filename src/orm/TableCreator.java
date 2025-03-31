package orm;

import orm.annotations.*;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class TableCreator {
    public static void createTable(Connection connection, Class<?> clazz) throws SQLException {
        EntityMetaData entityMetaData = new EntityMetaData(clazz);
        StringBuilder sql = new StringBuilder("CREATE TABLE ");
        sql.append(entityMetaData.getTableName()).append(" (");

        // Collect all column definitions first
        StringBuilder columnsBuilder = new StringBuilder();

        Field idField = entityMetaData.getIdField();
        idField.setAccessible(true);
        if (idField.isAnnotationPresent(GeneratedValue.class)) {
            columnsBuilder.append(idField.getName())
                .append(" INT GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY");
        } else {
            columnsBuilder.append(idField.getName()).append(" INT PRIMARY KEY");
        }

        for (Field column : entityMetaData.getColumns()) {
            column.setAccessible(true);
            if (column.isAnnotationPresent(Column.class)) {
                Column columnAnnotation = column.getAnnotation(Column.class);
                String columnName = columnAnnotation.name();
                String columnType = mapJavaTypeToSQL(column.getType());
                columnsBuilder.append(", ").append(columnName).append(" ").append(columnType);
            } else if (column.isAnnotationPresent(JoinColumn.class)) {
                JoinColumn joinColumn = column.getAnnotation(JoinColumn.class);
                String columnName = joinColumn.name();
                // Assuming foreign key columns are INT
                columnsBuilder.append(", ").append(columnName).append(" INT");
            }
        }

        sql.append(columnsBuilder).append(")");

        try (Statement stmt = connection.createStatement()) {
            try {
                stmt.execute(sql.toString());
                System.out.println("Created table: " + entityMetaData.getTableName());
            } catch (SQLException e) {
                // SQLState "X0Y32" indicates that the table already exists in Derby.
                if ("X0Y32".equals(e.getSQLState())) {
                    System.out.println("Table already exists: " + entityMetaData.getTableName());
                } else {
                    throw e;
                }
            }
        }
    }

    private static String mapJavaTypeToSQL(Class<?> type) {
        if (type == int.class || type == Integer.class) return "INT";
        if (type == String.class) return "VARCHAR(255)";
        if (type == boolean.class || type == Boolean.class) return "BOOLEAN";
        return "VARCHAR(255)"; // default fallback
    }
}

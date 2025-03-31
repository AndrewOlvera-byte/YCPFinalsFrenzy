package orm;

import orm.annotations.*;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class EntityMetaData {
    private Class<?> clazz;
    private String tableName;
    private Field idField;
    private List<Field> columns;

    public EntityMetaData(Class<?> clazz) {
        this.clazz = clazz;
        this.columns = new ArrayList<>();
        parseAnnotations(clazz); // Supports inherited fields
    }

    private void parseAnnotations(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new RuntimeException("Class " + clazz.getSimpleName() + " is not annotated with @Entity");
        }

        // Set table name from @Table annotation, or default to class name
        if (clazz.isAnnotationPresent(Table.class)) {
            this.tableName = clazz.getAnnotation(Table.class).name();
        } else {
            this.tableName = clazz.getSimpleName().toLowerCase();
        }

        // Gather all fields including inherited ones
        List<Field> allFields = getAllFieldsIncludingInherited(clazz);

        // Locate @Id field from class hierarchy
        this.idField = getIdFieldFromClassHierarchy(clazz);
        if (idField == null) {
            throw new RuntimeException("No @Id field found in " + clazz.getSimpleName());
        }

        // Only add non-id fields annotated with @Column or @JoinColumn
        for (Field field : allFields) {
            if (field.equals(idField)) {
                continue; // avoid duplicate ID column
            }
            if (field.isAnnotationPresent(Column.class) || field.isAnnotationPresent(JoinColumn.class)) {
                columns.add(field);
            }
        }
    }

    private Field getIdFieldFromClassHierarchy(Class<?> clazz) {
        Class<?> current = clazz;
        while (current != null) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(Id.class)) {
                    return field;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }
    
    public void populateFieldsFromResultSet(Object obj, ResultSet rs, Connection connection) throws Exception {
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);

            // Basic column mapping
            if (field.isAnnotationPresent(Column.class)) {
                String columnName = field.getAnnotation(Column.class).name();
                Object value = rs.getObject(columnName);
                field.set(obj, value);
            }

            // Handle @JoinColumn (foreign key reference)
            else if (field.isAnnotationPresent(JoinColumn.class)) {
                JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
                String fkColumnName = joinColumn.name();
                int foreignKeyId = rs.getInt(fkColumnName);

                if (foreignKeyId != 0) {
                    Class<?> fieldType = field.getType();
                    Object relatedObj = fieldType.getDeclaredConstructor().newInstance();

                    Field idField = fieldType.getDeclaredField("id");
                    idField.setAccessible(true);
                    idField.set(relatedObj, foreignKeyId);

                    field.set(obj, relatedObj);
                }
            }
        }
    }


    private List<Field> getAllFieldsIncludingInherited(Class<?> clazz) {
        List<Field> allFields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null) {
            for (Field field : current.getDeclaredFields()) {
                allFields.add(field);
            }
            current = current.getSuperclass();
        }
        return allFields;
    }

    public String getTableName() {
        return tableName;
    }

    public Field getIdField() {
        return idField;
    }

    public List<Field> getColumns() {
        return columns;
    }

    public Class<?> getClazz() {
        return clazz;
    }
}

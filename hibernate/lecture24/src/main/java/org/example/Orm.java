package org.example;

import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.RequiredArgsConstructor;
import org.example.entity.Person;
import org.postgresql.ds.PGSimpleDataSource;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public class Orm {
    private final static PGSimpleDataSource dataSource = new PGSimpleDataSource();

    static {
        dataSource.setServerNames(new String[]{"localhost"});
        dataSource.setPortNumbers(new int[]{5432});
        dataSource.setUser("vitalii_fedyk");
        dataSource.setPassword("26062004");
        dataSource.setDatabaseName("hibernate");
    }

    /**
     * Finds an entity by its ID in the database.
     *
     * @param clazz The class representing the entity.
     * @param id    The ID of the entity to find.
     * @param <T>   The type of the entity.
     * @return The found entity, or null if not found.
     */
    public <T> T findById(Class<T> clazz, Long id) {
        try (final Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            final String tableName = ReflectionUtils.getTableName(clazz); //It is supposed to be
            try (final Connection connection1 = dataSource.getConnection();
                 final ResultSet resultSet = connection.createStatement().executeQuery(
                         "SELECT * FROM ".concat(tableName)
                                 .concat(" WHERE id = ").concat(String.valueOf(id)).concat(";")
                 )) {

                final Field[] declaredFields = clazz.getDeclaredFields();

                final List<String> columnNames = Arrays.stream(declaredFields).map(field -> {
                    if (field.isAnnotationPresent(Column.class)) {
                       return field.getAnnotation(Column.class).name();
                    } else {
                        return field.getName();
                    }
                }).toList();

                final T t = clazz.getDeclaredConstructor().newInstance();
                resultSet.next();
                for (int i = 0; i < declaredFields.length; i++) {
                    declaredFields[i].setAccessible(true);
                    declaredFields[i].set(t, resultSet.getObject(columnNames.get(i)));
                } //works for primitives and objects

                return t;
            } catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
                throw new UnsupportedOperationException("Failed to create an object.", e);
            }
        } catch (SQLException | NoSuchMethodException e) {
            throw new UnsupportedOperationException("It is not possible to open a connection.", e);
        }
    }

    private static class ReflectionUtils {
        public static String getTableName(Class<?> clazz) {
            return clazz.getAnnotation(Table.class).name();
        }
    }

    public static void main(String[] args) {
        final Orm orm = new Orm();
        System.out.println(orm.findById(Person.class, 1L));
    }
}

package com.abaudna.haxagonal.infra.outputAdapter;

import com.abaudna.haxagonal.infra.outputPort.EntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Repository
public class PostgresRepository implements EntityRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public <T> T save(T reg) {
        Field[] entityFields = reg.getClass().getDeclaredFields();
        String[] fields = new String[entityFields.length];
        Object[] fieldValues = new Object[entityFields.length];

        try {
            for (int i = 0; i < entityFields.length; i++) {
                fields[i] = entityFields[i].getName();
                String getterName = "get" + fields[i].substring(0, 1).toUpperCase() + fields[i].substring(1);
                Method getter = reg.getClass().getMethod(getterName);
                fieldValues[i] = getter.invoke(reg);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                 | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to access entity fields", e);
        }

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ")
                .append(reg.getClass().getSimpleName())
                .append("(").append(String.join(",", fields)).append(")")
                .append(" VALUES ")
                .append("(").append(String.join(",", Collections.nCopies(fields.length, "?"))).append(")");

        jdbcTemplate.update(sql.toString(), fieldValues);
        return reg;
    }

    @Override
    public <T> T getById(String id, Class<T> clazz) {
        List<T> list = jdbcTemplate.query("SELECT * FROM " + clazz.getSimpleName() + " WHERE id = ?",
                new LombokRowMapper<>(clazz), id);

        if (!list.isEmpty()) return list.get(0);
        return null;
    }

    @Override
    public <T> List<T> getAll(Class<T> clazz) {
        return jdbcTemplate.query("SELECT * FROM " + clazz.getSimpleName(), new LombokRowMapper<>(clazz));
    }

    private static class LombokRowMapper<T> implements RowMapper<T> {
        private final Class<?> clazz;

        public LombokRowMapper(Class<?> clazz) {
            this.clazz = clazz;
        }

        @Override
        public T mapRow(ResultSet rs, int rowNum) throws SQLException {
            try {
                Method builderMethod = clazz.getMethod("builder");
                Object row = builderMethod.invoke(null);
                Method[] methods = row.getClass().getDeclaredMethods();

                for (Method method : methods) {
                    String fieldName = method.getName();
                    if (fieldName.startsWith("set")) {
                        String columnName = fieldName.substring(3).toLowerCase();
                        try {
                            Object fieldValue = rs.getObject(columnName);
                            method.invoke(row, fieldValue);
                        } catch (SQLException ignored) {
                            // Ignore missing columns
                        }
                    }
                }

                return (T) row.getClass().getMethod("build").invoke(row);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
                     | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
                throw new RuntimeException("Failed to map row to entity", e);
            }
        }
    }
}
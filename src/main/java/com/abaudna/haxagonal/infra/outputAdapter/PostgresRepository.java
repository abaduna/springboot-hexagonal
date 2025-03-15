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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class PostgresRepository implements EntityRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public <T> T save(T reg) {
        Field[] entityFields = reg.getClass().getDeclaredFields();
        List<String> fields = new ArrayList<>();
        List<Object> fieldValues = new ArrayList<>();

        try {
            for (Field field : entityFields) {
                // Excluir el campo ID si es autoincremental
                if (field.getName().equalsIgnoreCase("id")) continue;

                String getterName = "get" + field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
                Method getter = reg.getClass().getMethod(getterName);
                Object value = getter.invoke(reg);

                fields.add(field.getName());
                fieldValues.add(value);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al acceder a los campos", e);
        }

        String sql = String.format(
                "INSERT INTO %s (%s) VALUES (%s)",
                reg.getClass().getSimpleName(),
                String.join(",", fields),
                String.join(",", Collections.nCopies(fields.size(), "?"))
        );

        jdbcTemplate.update(sql, fieldValues.toArray());
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
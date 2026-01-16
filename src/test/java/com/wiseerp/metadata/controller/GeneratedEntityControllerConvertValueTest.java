package com.wiseerp.metadata.controller;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GeneratedEntityControllerConvertValueTest {

    private Object invokeConvert(Object raw, String dtype) throws Exception {
        var ctrl = new com.wiseerp.metadata.controller.GeneratedEntityFullController(null, null);
        Method m = com.wiseerp.metadata.controller.GeneratedEntityFullController.class.getDeclaredMethod("convertValue", Object.class, String.class);
        m.setAccessible(true);
        return m.invoke(ctrl, raw, dtype);
    }

    @Test
    void convert_null_returns_null() throws Exception {
        assertThat(invokeConvert(null, "STRING")).isNull();
    }

    @Test
    void convert_integer_and_invalid() throws Exception {
        Object r = invokeConvert("123", "INTEGER");
        assertThat(r).isInstanceOf(Integer.class).isEqualTo(123);

        Object r2 = invokeConvert(45.7, "INTEGER");
        assertThat(r2).isInstanceOf(Integer.class).isEqualTo(45);

        Throwable t = org.assertj.core.api.Assertions.catchThrowable(() -> invokeConvert("notanumber", "INTEGER"));
        assertThat(t).isInstanceOf(java.lang.reflect.InvocationTargetException.class);
        assertThat(t.getCause()).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Failed to convert");
    }

    @Test
    void convert_decimal() throws Exception {
        Object r = invokeConvert("12.34", "DECIMAL");
        assertThat(r).isInstanceOf(BigDecimal.class).isEqualTo(new BigDecimal("12.34"));
    }

    @Test
    void convert_boolean() throws Exception {
        assertThat(invokeConvert("true", "BOOLEAN")).isEqualTo(true);
        assertThat(invokeConvert("False", "BOOLEAN")).isEqualTo(false);
    }

    @Test
    void convert_date_and_datetime() throws Exception {
        Object d = invokeConvert("2021-12-31", "DATE");
        assertThat(d).isInstanceOf(Date.class).isEqualTo(Date.valueOf("2021-12-31"));

        Object dt = invokeConvert("2021-12-31T12:34:56Z", "DATETIME");
        assertThat(dt).isInstanceOf(Timestamp.class).isEqualTo(Timestamp.from(Instant.parse("2021-12-31T12:34:56Z")));
    }

    @Test
    void convert_json_and_binary_and_default() throws Exception {
        Map<String,Object> m = new HashMap<>();
        m.put("a", 1);
        m.put("b", "x");
        Object json = invokeConvert(m, "JSON");
        assertThat(json).isInstanceOf(String.class).asString().contains("\"a\":1");

        byte[] bytes = "hello".getBytes();
        String enc = Base64.getEncoder().encodeToString(bytes);
        Object bin = invokeConvert(enc, "BINARY");
        assertThat(bin).isInstanceOf(byte[].class).isEqualTo(bytes);

        Object def = invokeConvert(12345, "UNKNOWN_TYPE");
        assertThat(def).isInstanceOf(String.class).isEqualTo("12345");
    }
}

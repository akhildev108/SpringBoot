package io.akhil.printapp.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum ContentType {
    PDF, RAW;

    private static Map<String, ContentType> namesMap = new HashMap<>();

    static {
        namesMap.put("pdf", PDF);
        namesMap.put("raw", RAW);
    }

    @JsonCreator
    public static ContentType forValue(String value) {
        if(value==null) {
            return null;
        }
        return namesMap.get(value.trim().toLowerCase());
    }

    @JsonValue
    public String toValue() {
        for (Map.Entry<String, ContentType> entry : namesMap.entrySet()) {
            if (entry.getValue() == this)
                return entry.getKey();
        }
        return null; // or fail
    }
}

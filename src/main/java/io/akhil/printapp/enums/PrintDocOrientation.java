package io.akhil.printapp.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum PrintDocOrientation {
    LANDSCAPE,
    PORTRAIT;

    private static Map<String, PrintDocOrientation> namesMap = new HashMap<>();

    static {
        namesMap.put("landscape", LANDSCAPE);
        namesMap.put("portrait", PORTRAIT);
    }

    @JsonCreator
    public static PrintDocOrientation forValue(String value) {
        if(value==null) {
            return null;
        }
        return namesMap.get(value.trim().toLowerCase());
    }

    @JsonValue
    public String toValue() {
        for (Map.Entry<String, PrintDocOrientation> entry : namesMap.entrySet()) {
            if (entry.getValue() == this)
                return entry.getKey();
        }

        return null; // or fail
    }

}

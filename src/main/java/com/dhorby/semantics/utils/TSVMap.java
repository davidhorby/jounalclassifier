package com.dhorby.semantics.utils;

import com.dhorby.semantics.model.DataValue;

import java.util.Optional;
import java.util.function.Function;

public class TSVMap {

    public static Function<String, Optional<Object>> mapBMCDataSet = (line) -> {
        String[] p = line.split("	");
        try {
            return  Optional.of(new DataValue(p[0], p[1], p[2]));
        } catch (ArrayIndexOutOfBoundsException ex ) {
            return Optional.empty();
        }
    };
}
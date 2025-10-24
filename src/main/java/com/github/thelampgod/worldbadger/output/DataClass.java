package com.github.thelampgod.worldbadger.output;

import java.util.List;

public interface DataClass {
    List<String> getFieldNames();
    List<Object> getFieldValues();
}

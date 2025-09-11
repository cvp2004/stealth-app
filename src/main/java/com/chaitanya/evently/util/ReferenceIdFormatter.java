package com.chaitanya.evently.util;

import org.springframework.stereotype.Component;

@Component
public class ReferenceIdFormatter {

    public String format(String prefix, long id, int width) {
        String pattern = String.format("%%s%%0%dd", width);
        return String.format(pattern, prefix, id);
    }
}

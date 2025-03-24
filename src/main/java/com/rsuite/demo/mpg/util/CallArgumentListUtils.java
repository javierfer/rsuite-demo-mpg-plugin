package com.rsuite.demo.mpg.util;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.HashMap;
import java.util.Map;

import com.reallysi.rsuite.api.remoteapi.CallArgument;
import com.reallysi.rsuite.api.remoteapi.CallArgumentList;

public class CallArgumentListUtils {

    private static final String DEFAULT_DELIMITER = ",";

    private CallArgumentListUtils() {
    }

    /**
     * All values for multiple fields are concatenated in same original parameter
     * name by delimiter.
     * 
     * @param args
     * @return map
     */
    public static Map<String, String> concatMultipleParams(CallArgumentList args) {

        Map<String, String> map = new HashMap<>();

        for (CallArgument arg : args.getAll()) {
            String name = arg.getName();
            String value = arg.getValue();

            if (isNotBlank(value)) {
                if (map.containsKey(name)) {
                    value = value.concat(DEFAULT_DELIMITER).concat(map.get(name));
                }
                map.put(name, value);
            }
        }

        return map;
    }
}

package edu.cmipt.gcs.util;

import java.util.HashMap;
import java.util.Map;

import edu.cmipt.gcs.pojo.error.ErrorVO;

public class ErrorMessageUtil {

    private static Map<String, ErrorVO> errorMap;

    public static ErrorVO generateError(String message) {
        if (errorMap == null) {
            errorMap = new HashMap<>();
        }
        if (!errorMap.containsKey(message)) {
            errorMap.put(message, new ErrorVO(message));
        }
        return errorMap.get(message);
    }
}

package com.naratil.batch.util;

import java.math.BigDecimal;

/**
 * 숫자 관련 유틸 클래스
 */
public class NumberUtils {

    /**
     * 숫자로 변경 가능한 문자열인가?
     * @param str 문자열
     * @return 변경 가능 여부
     */
    public static boolean isNumeric(String str) {
        if (str == null) return false;
        try {
            new BigDecimal(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}

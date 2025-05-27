package com.naratil.batch.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 벡터 관련 공통 유틸리티
 */
public class VectorUtils {

    /**
     *  벡터 List<Double> -> List<Float>로 변환
     *
     * @param rawList Object (List<Number>)
     * @return List<Float>
     */
    @SuppressWarnings("unchecked")
    public static List<Float> toFloatList(Object rawList) {
        if (!(rawList instanceof List<?> list)) return Collections.emptyList();

        List<Float> result = new ArrayList<>(list.size());
        for (Object val : list) {
            if (val instanceof Number num) {
                result.add(num.floatValue());
            }
        }
        return result;
    }

}

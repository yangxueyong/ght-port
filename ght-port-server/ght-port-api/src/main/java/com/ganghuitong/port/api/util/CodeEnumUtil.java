package com.ganghuitong.port.api.util;

import cn.hutool.core.util.StrUtil;
import com.ganghuitong.port.api.enums.CodeEnum;

/**
 * 编码枚举工具
 *
 * @author yangxueyong
 */
public final class CodeEnumUtil {

    /**
     * 私有构造，禁止实例化
     */
    private CodeEnumUtil() {
    }

    /**
     * 判断编码是否属于指定枚举
     *
     * @param enumClass 枚举类型
     * @param code      待判断的编码
     * @param <E>       枚举泛型
     * @return true-合法编码，false-非法编码
     */
    public static <E extends Enum<E> & CodeEnum> boolean contains(Class<E> enumClass, String code) {
        return find(enumClass, code) != null;
    }

    /**
     * 根据编码获取中文描述
     *
     * @param enumClass 枚举类型
     * @param code      编码
     * @param <E>       枚举泛型
     * @return 中文描述，找不到时返回空串
     */
    public static <E extends Enum<E> & CodeEnum> String desc(Class<E> enumClass, String code) {
        E e = find(enumClass, code);
        return e == null ? StrUtil.EMPTY : e.getDesc();
    }

    /**
     * 根据编码查找枚举项
     *
     * @param enumClass 枚举类型
     * @param code      编码
     * @param <E>       枚举泛型
     * @return 枚举项，找不到时返回null
     */
    public static <E extends Enum<E> & CodeEnum> E find(Class<E> enumClass, String code) {
        for (E e : enumClass.getEnumConstants()) {
            if (StrUtil.equals(e.getCode(), code)) {
                return e;
            }
        }
        return null;
    }
}

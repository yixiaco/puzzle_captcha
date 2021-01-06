package com.hexm.puzzle.captcha.util;

import java.util.function.Function;

/**
 * 字符串工具类
 *
 * @author hexm
 * @date 2020/6/30 16:06
 */
public class StrUtil extends cn.hutool.core.util.StrUtil {

    /**
     * 分割字符串，返回小写首字母
     * 例如： str=sys_user =>su
     *
     * @param str
     * @return
     */
    public static String firstLetter(String str) {
        return firstLetter(str, "_");
    }

    /**
     * 分割字符串，返回小写首字母
     * 例如： str=sys_user concat=_ =>su
     *
     * @param str
     * @param concat
     * @return
     */
    public static String firstLetter(String str, String concat) {
        if (isEmpty(str)) {
            return str;
        }
        String[] strs = split(str, concat);
        StringBuilder sb = new StringBuilder();
        for (String s : strs) {
            sb.append(s.charAt(0));
        }
        return sb.toString().toLowerCase();
    }

    /**
     * 重复n个字符串
     *
     * @param str 字符串
     * @param n   系数
     * @return
     */
    public static String multiply(CharSequence str, int n) {
        if (n > 1 && str != null && str.length() > 0) {
            StringBuilder sb = new StringBuilder(str);
            for (int i = 0; i < n - 1; i++) {
                sb.append(str);
            }
            return sb.toString();
        }
        return String.valueOf(str);
    }

    /**
     * 获取最大的数,null默认最小
     *
     * @param s
     * @return
     */
    public static String max(String... s) {
        if (s == null || s.length == 0) {
            return null;
        } else if (s.length == 1) {
            return s[0];
        } else {
            String maxS = null;
            for (int i = 0; i < s.length - 1; i++) {
                if (s[i] == null) {
                    maxS = s[i + 1];
                } else if (s[i + 1] == null) {
                    maxS = s[i];
                } else {
                    int r = s[i].compareTo(s[i + 1]);
                    maxS = r > 0 ? s[i] : s[i + 1];
                }
            }
            return maxS;
        }
    }

    /**
     * 获取最小的数,null默认最大
     *
     * @param s
     * @return
     */
    public static String min(String... s) {
        if (s == null || s.length == 0) {
            return null;
        } else if (s.length == 1) {
            return s[0];
        } else {
            String minS = null;
            for (int i = 0; i < s.length - 1; i++) {
                if (s[i] == null) {
                    minS = s[i + 1];
                } else if (s[i + 1] == null) {
                    minS = s[i];
                } else {
                    int r = s[i].compareTo(s[i + 1]);
                    minS = r > 0 ? s[i + 1] : s[i];
                }
            }
            return minS;
        }
    }

    /**
     * 转化为字符串
     *
     * @param o
     * @return
     */
    public static String valueOf(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    /**
     * 转int
     *
     * @param o
     * @return
     */
    public static Integer toInt(Object o) {
        return toInt(o, null);
    }

    /**
     * 转int
     *
     * @param o
     * @return
     */
    public static Integer toInt(Object o, Integer isNull) {
        return to(o, o1 -> {
            if (o1 instanceof Number) {
                return ((Number) o1).intValue();
            }
            return Integer.parseInt(o1.toString());
        }, isNull, Integer.class);
    }

    /**
     * 转long
     *
     * @param o
     * @return
     */
    public static Long toLong(Object o) {
        return toLong(o, null);
    }

    /**
     * 转long
     *
     * @param o
     * @return
     */
    public static Long toLong(Object o, Long isNull) {
        return to(o, o1 -> {
            if (o1 instanceof Number) {
                return ((Number) o1).longValue();
            }
            return Long.parseLong(o1.toString());
        }, isNull, Long.class);
    }

    /**
     * 转double
     *
     * @param o
     * @return
     */
    public static Double toDouble(Object o) {
        return toDouble(o, null);
    }

    /**
     * 转double
     *
     * @param o
     * @return
     */
    public static Double toDouble(Object o, Double isNull) {
        return to(o, o1 -> {
            if (o1 instanceof Number) {
                return ((Number) o1).doubleValue();
            }
            return Double.parseDouble(o1.toString());
        }, isNull, Double.class);
    }

    /**
     * 转换通用
     *
     * @param o
     * @param function
     * @param isNull
     * @param <T>
     * @return
     */
    private static <T extends Number> T to(Object o, Function<Object, T> function, T isNull, Class<?> clz) {
        return o == null || "".equals(o) ? isNull : clz.isAssignableFrom(o.getClass()) ? (T) o : function.apply(o);
    }
}

package com.hexm.puzzle.captcha.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * 打印完整堆栈异常信息
 * @author hexm
 * @date 2019/8/27 11:34
 */
public class ThrowableUtil {

    private ThrowableUtil(){}

    /**
     * 得到完整堆栈异常信息
     * @param cause
     * @return
     */
    public static String stackTraceToString(Throwable cause) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream pout = new PrintStream(out);
        cause.printStackTrace(pout);
        pout.flush();
        try {
            return out.toString();
        } finally {
            try {
                out.close();
            } catch (IOException ignore) {
                // ignore as should never happen
            }
        }
    }
}

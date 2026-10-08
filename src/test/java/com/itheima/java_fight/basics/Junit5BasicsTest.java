package com.itheima.java_fight.basics;

import com.itheima.java_fight.common.Result;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class Junit5BasicsTest {

    @Test
    void ReturnOne(){
        String msg = "abc";
        Result<String>result = Result.success(msg);

        assertEquals(1,result.getCode());
    }

    @Test
    void ReturnMsg(){
        String msg = "你是大傻逼";
        Result<String>result = Result.error(msg);

        assertEquals("你是大傻逼",result.getMsg());
    }

    @Test
    void tassertTrue(){
        boolean result = "sb".startsWith("s");

        assertTrue(result);
    }

    private int divide(int a, int b) {
        return a / b;
    }

    @Test
    void tassertThrows(){
        assertThrows(ArithmeticException.class, () -> divide(1, 0));
    }




}

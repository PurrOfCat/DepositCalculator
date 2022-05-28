package com.example.depositcalculator;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void test_course_getter() {
        try {
            JSONObject json = new JSONObject(
                    IOUtils.toString(new URL(
                            "https://free.currconv.com/api/v7/convert?apiKey=efdeb145deda59536479&q=USD_RUB&compact=ultra"
                    ), StandardCharsets.UTF_8)
            );
            System.out.println(json.getDouble("USD_RUB"));
        } catch (JSONException | IOException exception) {
            exception.printStackTrace();
        }
    }
}
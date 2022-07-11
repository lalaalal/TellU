package com.letmeinform.tellu;

import android.content.Context;
import android.widget.Toast;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class DBHelperTest {
    @Test
    public void addProducts() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        try (DBHelper dbHelper = new DBHelper(appContext)) {
            ArrayList<Product> expected = new ArrayList<>();
            expected.add(new Product(1, "Test1", new Date()));
            expected.add(new Product(2, "Test2", new Date()));
            for (Product product : expected) {
                dbHelper.addProduct(product);
            }


            assertEquals(expected, dbHelper.getProducts());
        }
    }
}
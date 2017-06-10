package tests;

/**
 * Created by tinhdang on 6/9/17.
 */

import android.content.Context;
import android.support.test.InstrumentationRegistry;



import org.junit.Test;

import java.util.concurrent.TimeoutException;

import g25.com.dejaphoto.DejaPhotoService;
import g25.com.dejaphoto.SettingsActivity;

import static junit.framework.Assert.assertTrue;

public class ServicesTest {

    @Test
    public void test_services_started_on_init() throws TimeoutException {
        final Context context = InstrumentationRegistry.getTargetContext();

        assertTrue(!DejaPhotoService.isServiceStarted());

        assertTrue(!SettingsActivity.isServiceStarted());
    }
}

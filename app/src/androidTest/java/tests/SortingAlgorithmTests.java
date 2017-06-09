package tests;

import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import g25.com.dejaphoto.BackgroundPhoto;
import g25.com.dejaphoto.SettingsActivity;
import g25.com.dejaphoto.SortingAlgorithm;

import static junit.framework.Assert.assertTrue;

/**
 * Created by angelazhang on 5/14/17.
 */

public class SortingAlgorithmTests {

    BackgroundPhoto photo;
    SortingAlgorithm sorter;

    @Rule
    public ActivityTestRule<SettingsActivity> settingsActivity = new ActivityTestRule<SettingsActivity>(SettingsActivity.class);


    @Before
    public void setUp(){
        sorter = new SortingAlgorithm();
        photo = new BackgroundPhoto(null, settingsActivity.getActivity());
    }



    @Test
    public void testAssignPoints(){
        int points;
        assertTrue(photo.getPoints() == 0);

        photo.giveKarma("unique Id 1", null);
        points = sorter.assignPoints(photo);
        assertTrue(points ==  1);

        photo.release();
        points = sorter.assignPoints(photo);
        assertTrue(photo.getPoints() == -1);
        assertTrue(photo.isReleased() == true);
    }

    @Test
    public void testToMins(){
        String time = "05:20";
        int num = sorter.toMins(time);
        assertTrue(num == 320);
    }


}

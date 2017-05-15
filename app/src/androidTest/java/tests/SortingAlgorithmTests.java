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
        int points = sorter.assignPoints(photo);
        assertTrue(points == 0);

        photo.giveKarma();
        points = sorter.assignPoints(photo);
        assertTrue(points == 5);

        photo.release();
        points = sorter.assignPoints(photo);
        assertTrue(points == -1);
    }
}

package tests;

import android.location.Location;
import android.support.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.GregorianCalendar;

import g25.com.dejaphoto.BackgroundPhoto;
import g25.com.dejaphoto.LocationWrapper;
import g25.com.dejaphoto.SettingsActivity;
import g25.com.dejaphoto.SortingAlgorithm;

import static junit.framework.Assert.assertTrue;

/**
 * Created by angelazhang on 5/14/17.
 */

public class SortingAlgorithmTests {
    private static final String MOCK_PROVIDER = "dummyprovier";
    private static final double MOCK_LAT = 100.0;
    private static final double MOCK_LNG = 100.0;
    private static final double MOCK_LAT_FAR = 200.0;
    private static final double MOCK_LNG_FAR = 200.0;
    private static final double MOCK_LNG_CLOSE = 100;
    private static final double MOCK_LAT_CLOSE = 100;

    //for fake dates
    private static final int FIVE_DAYS = 5;
    private static final int MOCK_YEAR = 1971;
    private static final int MOCK_MONTH = 4;
    private static final int MOCK_DAY = 20;
    private static final int MOCK_HOUR = 4;
    private static final int MOCK_MIN = 4;
    private static final int MOCK_SEC = 20;

    SortingAlgorithm sorter;

    //mocked "fake" Photos
    BackgroundPhoto photo;
    BackgroundPhoto photoWithKarma;
    BackgroundPhoto photoClose;
    BackgroundPhoto photoFar;
    BackgroundPhoto photoRecent;
    BackgroundPhoto photoOld;

    //mocked "fake" objects
    LocationWrapper mockLocationWrapper;
    Location mockLocation;
    Location mockLocationFar;
    Location mockLocationClose;
    GregorianCalendar mockDate;
    GregorianCalendar mockOldDate;

    @Rule
    public ActivityTestRule<SettingsActivity> settingsActivity = new ActivityTestRule<SettingsActivity>(SettingsActivity.class);


    @Before
    public void setUp(){

        //init sorter
        sorter = new SortingAlgorithm();

        //LocationWrapper with mockLocation at LatLng at defined values
        mockLocation = new Location(MOCK_PROVIDER);
        mockLocation.setLatitude(MOCK_LAT);
        mockLocation.setLongitude(MOCK_LNG);
        mockLocationWrapper = new LocationWrapper();
        mockLocationWrapper.setCurrentUserLocation(mockLocation);

        //fake far location
        mockLocationFar = new Location(MOCK_PROVIDER);
        mockLocationFar.setLatitude(MOCK_LAT_FAR);
        mockLocationFar.setLongitude(MOCK_LNG_FAR);

        //fake close location
        mockLocationClose = new Location(MOCK_PROVIDER);
        mockLocationClose.setLatitude(MOCK_LAT_CLOSE);
        mockLocationClose.setLongitude(MOCK_LNG_CLOSE);

        //fake current date
        mockDate =
                new GregorianCalendar(MOCK_YEAR, MOCK_MONTH, MOCK_DAY, MOCK_HOUR,
                        MOCK_MIN, MOCK_SEC);
        //fake old date
        mockOldDate =
                new GregorianCalendar(MOCK_YEAR, MOCK_MONTH, MOCK_DAY - FIVE_DAYS, MOCK_HOUR,
                        MOCK_MIN, MOCK_SEC);

        //mock sorter with fake dates
        sorter.setCurrentDate(mockDate.getTime());
        //mock sorter with fake location
        sorter.setLocationWrapper(mockLocationWrapper);

        //fake photos
        photo = new BackgroundPhoto("photo", 0);
        //karma
        photoWithKarma = new BackgroundPhoto("photoWithKarma", 0);
        photoWithKarma.giveKarma("test", null);
        //close
        photoClose = new BackgroundPhoto("photoClose", 0);
        photoClose.setLocation(mockLocationClose);
        //far
        photoFar = new BackgroundPhoto("photoFar", 0) ;
        photoFar.setLocation(mockLocationFar);
        //recent
        photoRecent = new BackgroundPhoto("photoRecent", 0);
        photoRecent.setCalendar(mockDate);
        //old
        photoOld = new BackgroundPhoto("photoOld", 0);
        photoOld.setCalendar(mockOldDate);

    }



    @Test
    public void testAssignPointsRelease(){
        int points;
        assertTrue(photo.getPoints() == 0);

        photo.giveKarma("unique Id 1", null);
        points = sorter.assignPoints(photo);
        assertTrue(points !=  0);

        photo.release();
        sorter.assignPoints(photo);
        assertTrue(photo.getPoints() == -1);
        assertTrue(photo.isReleased() == true);
    }

    @Test
    public void testAssignPointsKarma(){
        sorter.assignPoints(photoWithKarma);
        int points = photoWithKarma.getPoints();
        assertTrue(points != 0);
    }

    @Test
    public void testAssignPointsLocationClose(){
        photoClose.setLocation(mockLocationClose);
        sorter.assignPoints(photoClose);
        int points = photoClose.getPoints();
        assertTrue(points != 0);
    }

    @Test public void testAssignPointsLocationFar(){
        sorter.assignPoints(photoFar);
        int points = photoFar.getPoints();
        assertTrue(points == 0);
    }

    @Test
    public void testAssignPointsRecent(){
        sorter.assignPoints(photoRecent);
        int points = photoRecent.getPoints();
        assertTrue(points != 0);
    }

    @Test public void testAssignPointsOld(){
        sorter.assignPoints(photoOld);
        int points = photoOld.getPoints();
        assertTrue(points == 0);
    }

    @Test
    public void testToMins(){
        String time = "05:20";
        int num = sorter.toMins(time);
        assertTrue(num == 320);
    }


}

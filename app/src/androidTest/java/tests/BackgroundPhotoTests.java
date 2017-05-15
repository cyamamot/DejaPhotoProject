package tests;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import g25.com.dejaphoto.BackgroundPhoto;
import static junit.framework.Assert.assertTrue;

/**
 * Created by Tim on 5/14/2017.
 */

public class BackgroundPhotoTests {
    private BackgroundPhoto photo;
    public final ExpectedException exception = ExpectedException.none();


    @Before
    public void setUp(){
        photo = new BackgroundPhoto(null, null);

    }

    @Test
    public void testFormatLatLng(){
        String testCoord = "1/1,1/1,1/1";
        String[] testDir = {"N", "S", "E", "W"};
        double calc = 1 + 1/60 + 1/3600;
        Double[] expectedVal = {calc, -calc, calc, -calc};

        for(int i = 0; i < expectedVal.length; i ++){
            double output = photo.formatLatLng(testCoord, testDir[i]);
            assertTrue(output == expectedVal[i]);
        }


        //Null case
        exception.expect(IndexOutOfBoundsException.class);
        photo.formatLatLng(null, null);

    }

    @Test
    public void doesntHaveKarma(){
        assertTrue(photo.hasKarma());
    }

    @Test
    public void hasKarma(){
        photo.giveKarma();
        assertTrue(photo.hasKarma());
    }

    @Test
    public void checkRelease(){
        assertTrue(photo.isReleased());
    }

    @Test
    public void releaseThenCheckRelease(){
        photo.release();
        assertTrue(photo.isReleased());
    }

}

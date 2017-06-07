package tests;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import g25.com.dejaphoto.BackgroundPhoto;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

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
        String testCoord = "100/1,100/1,100/1";
        String[] testDir = {"N", "S", "E", "W"};
        double calc = 100  + 100/60 + 100/3600;
        double[] expectedVal = {calc, 0-calc, calc, 0-calc};

        for(int i = 0; i < expectedVal.length; i ++){
            double output = photo.formatLatLng(testCoord, testDir[i]);
            assertTrue((int)output == (int)expectedVal[i]);
        }


        //Null case
        try{
            photo.formatLatLng(null, null);
            fail();
        }
        catch(NullPointerException e){

        }

    }

    @Test
    public void doesntHaveKarma(){
        assertTrue(!photo.hasKarma());
    }

    @Test
    public void hasKarma(){
        photo.giveKarma("");
        assertTrue(photo.hasKarma());
    }

    @Test
    public void checkRelease(){
        assertTrue(!photo.isReleased());
    }

    @Test
    public void releaseThenCheckRelease(){
        photo.release();
        assertTrue(photo.isReleased());
    }

}

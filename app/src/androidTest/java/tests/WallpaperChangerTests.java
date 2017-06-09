package tests;

<<<<<<< Updated upstream
import android.test.AndroidTestCase;
=======
import android.content.Context;
import android.content.SharedPreferences;
import android.test.AndroidTestCase;
import android.test.mock.MockContext;
>>>>>>> Stashed changes

import org.junit.Before;
import org.junit.Test;

<<<<<<< Updated upstream
=======
import java.util.PriorityQueue;

>>>>>>> Stashed changes
import g25.com.dejaphoto.BackgroundPhoto;
import g25.com.dejaphoto.WallpaperChanger;
/**
 * Created by tinhdang on 6/7/17.
 */

public class WallpaperChangerTests extends AndroidTestCase{


    private WallpaperChanger wallpaperChanger;
<<<<<<< Updated upstream

    BackgroundPhoto photo_1;
    BackgroundPhoto photo_2;
    BackgroundPhoto photo_3;

    /*String photo1 = "photo1";
    String photo2 = "photo2";
    String photo3 = "photo3";
*/

=======
    static SharedPreferences.Editor settingsEditorTest;
    static SharedPreferences settingsTest;
    private PriorityQueue<BackgroundPhoto> queue;


    String photo1 = "photo1";
    String photo2 = "photo2";
    String photo3 = "photo3";
>>>>>>> Stashed changes

    String numOfKarma = "numOfKarma";
    static final String KARMA = "DJP_KARMA";
    static final String NumKARMAERS = "DJP_KARMAERS";
<<<<<<< Updated upstream

    @Before
    public void setUp() {
       // Context context = new DelegatedMockContext(getContext());
        wallpaperChanger = new WallpaperChanger();

        photo_1 = new BackgroundPhoto("photo1", 420);
        photo_2 = new BackgroundPhoto("photo2", 110 );
        photo_3 = new BackgroundPhoto("photo3", 200);

    }

 /*   class DelegatedMockContext extends MockContext {
=======
    @Before
    public void setUp() {
        Context context = new DelegatedMockContext(getContext());
        wallpaperChanger = new WallpaperChanger(context);


    }

    class DelegatedMockContext extends MockContext {
>>>>>>> Stashed changes

        private Context mDelegatedContext;
        private static final String PREFIX = "test";

        public DelegatedMockContext(Context context) {
            mDelegatedContext = context;
        }

        @Override
        public String getPackageName(){
            return PREFIX;
        }

        @Override
        public SharedPreferences getSharedPreferences(String name, int mode) {
<<<<<<< Updated upstream
            /*settingsTest = getSharedPreferences(name, MODE_PRIVATE);
=======
            settingsTest = getSharedPreferences(name, MODE_PRIVATE);
>>>>>>> Stashed changes
            settingsEditorTest = settingsTest.edit();

            settingsEditorTest.putBoolean("useCustomAlbum", false);
            settingsEditorTest.commit();

            settingsEditorTest.putBoolean(photo1, true);
            settingsEditorTest.commit();
            settingsEditorTest.putString(photo1+KARMA, numOfKarma);
            settingsEditorTest.commit();

            settingsEditorTest.putBoolean(photo2, true);
            settingsEditorTest.commit();
            settingsEditorTest.putString(photo2+KARMA, numOfKarma);
            settingsEditorTest.commit();

            settingsEditorTest.putBoolean(photo3, true);
            settingsEditorTest.commit();
            settingsEditorTest.putString(photo3+KARMA, numOfKarma);
            settingsEditorTest.commit();

            return mDelegatedContext.getSharedPreferences(name, mode);
        }
<<<<<<< Updated upstream
    }*/


        @Test
        public void testSize() {

            assertTrue(wallpaperChanger.queue.size() == 0);

            wallpaperChanger.queue.add(photo_1);
            wallpaperChanger.queue.add(photo_2);
            wallpaperChanger.queue.add(photo_3);

            assertTrue(wallpaperChanger.queue.size() == 3);

        }

        @Test
        public void testOrder() {

            wallpaperChanger.queue.add(photo_1);
            wallpaperChanger.queue.add(photo_2);
            wallpaperChanger.queue.add(photo_3);

            assertTrue(wallpaperChanger.queue.peek() == photo_1);
            wallpaperChanger.queue.poll();
            assertTrue(wallpaperChanger.queue.peek() == photo_3);
            wallpaperChanger.queue.poll();
            assertTrue(wallpaperChanger.queue.peek() == photo_2);
            wallpaperChanger.queue.poll();

        }

    @Test
    public void testNullException() {

        try{
            wallpaperChanger.queue.add(null);
            fail();
        }
        catch(NullPointerException e){

        }


    }




=======
    }


        @Test
        public void testInitialize()
        {
            assertTrue(wallpaperChanger != null);
            //wallpaperChanger.initialize();
        }


        @Test
        public void testNext() {

            wallpaperChanger.next();
        }

        @Test
        public void testPrevious() {

            wallpaperChanger.previous();
        }

        @Test
        public void testSetLocation() {

            wallpaperChanger.setLocation();
        }


>>>>>>> Stashed changes



}
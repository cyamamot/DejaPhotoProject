package tests;


import android.content.SharedPreferences;
import android.test.AndroidTestCase;

import org.junit.Before;
import org.junit.Test;

import java.util.PriorityQueue;

import g25.com.dejaphoto.BackgroundPhoto;
import g25.com.dejaphoto.WallpaperChanger;
/**
 * Created by tinhdang on 6/7/17.
 */

public class WallpaperChangerTests extends AndroidTestCase{


    private WallpaperChanger wallpaperChanger;


    BackgroundPhoto photo_1;
    BackgroundPhoto photo_2;
    BackgroundPhoto photo_3;


    static SharedPreferences.Editor settingsEditorTest;
    static SharedPreferences settingsTest;
    private PriorityQueue<BackgroundPhoto> queue;


    String photo1 = "photo1";
    String photo2 = "photo2";
    String photo3 = "photo3";


    @Before
    public void setUp() {
       // Context context = new DelegatedMockContext(getContext());
        wallpaperChanger = new WallpaperChanger();

        photo_1 = new BackgroundPhoto("photo1", 420);
        photo_2 = new BackgroundPhoto("photo2", 110 );
        photo_3 = new BackgroundPhoto("photo3", 200);

    }




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





}
package tests;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;

import g25.com.dejaphoto.FirebaseWrapper;
import static org.junit.Assert.assertTrue;

/**
 * Created by bigmak712 on 6/9/17.
 */

public class FriendsTests {

    private FirebaseWrapper fbWrapper;
    private Context testContext;

    @Before
    public void setUp() {
        testContext = InstrumentationRegistry.getContext();
        fbWrapper = new FirebaseWrapper(testContext);
    }

    @Test
    public void testAddingFriend(){
        String friend = "friend@gmail.com";
        fbWrapper.addFriend(friend);
        assertTrue(fbWrapper.findFriend(friend));
        fbWrapper.removeFriend(friend);
    }
}

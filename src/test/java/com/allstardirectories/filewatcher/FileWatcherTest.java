package com.allstardirectories.filewatcher;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 *@author John Tran.
 */
public class FileWatcherTest {
    String from = "/home/john.tran/HelloPolopolyNitro/module-asd/src/main/webapp/WEB-INF/velocity";
    String to = "/home/john.tran/HelloPolopolyNitro/webapp-dispatcher/src/main/webapp/WEB-INF/velocity";
    CustomFileListener customFileListener;


    @Before
    public void setup(){
        FileWatcher .FromTo fromTo = new FileWatcher.FromTo();
        fromTo.from = from;
        fromTo.to = to;
        customFileListener = new CustomFileListener(fromTo);
    }



    @Test
    public void testFormDestinationPath(){

        //Test with a file that gets changed at root path
        String fileChanged = "/blah.vm";
        String destinationPath = customFileListener.formDestinationPath(from + fileChanged);
        assertNotNull(destinationPath);
        assertEquals(to+fileChanged, destinationPath);


        //Test with a subdirectory
        fileChanged = "/asd/blah.vm";
        destinationPath = customFileListener.formDestinationPath(from + fileChanged);
        assertNotNull(destinationPath);
        assertEquals(to+fileChanged, destinationPath);

    }



}

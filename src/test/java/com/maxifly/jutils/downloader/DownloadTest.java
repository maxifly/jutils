package com.maxifly.jutils.downloader;

/**
 * Created by Maximus on 02.06.2016.
 */

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class DownloadTest {
    @Test
    public void testDownload() throws Exception {
        URL url = new URL("https://github.com/maxifly/time-calculator/releases/download/v1.2/calc_t.exe");

        Download download = new Download(url, "c:/kuku/testFile");
        Downloader downloader = new Downloader();
        TstProgress tstProgress = new TstProgress();
        TstProgress downloadProgress = new TstProgress();
        download.setProgress(downloadProgress);
        downloader.setProgress(tstProgress);

        downloader.startTask(download);


        int i = 1;
        while (downloader.checkTasks() != true && i < 101) {
            Thread.sleep(500);
            i++;
        }

        downloader.close();

        Assert.assertTrue(i<101);

        Assert.assertEquals(1,tstProgress.max);
        Assert.assertEquals(1,tstProgress.done);
        Assert.assertNotNull(tstProgress.mess);
        Assert.assertTrue((downloadProgress.max>1));
        Assert.assertTrue((downloadProgress.done>1));
        Assert.assertNotNull(tstProgress.mess);

    }

}

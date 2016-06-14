package com.maxifly.jutils.downloader;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * Created by Maximus on 01.06.2016.
 */
public class Download implements Callable<DownStatus> {

    private static final int MAX_BUFFER_SIZE = 10; //1024;


    private URL url;
    private File destFile;
    //private String destFileName;
    private int size;
    private int downloaded;
    private DownStatus status;

    public Download(URL url, String destFileName) {
        super();
        this.prepare(url, new File(destFileName));
    }

    public Download(URL url, File destFile) {
        super();
        this.prepare(url, destFile);
    }

    public URL getUrl() {
        return url;
    }

    private void prepare(URL url, File destFile) {
        this.url = url;
        this.destFile = destFile;
        size = -1;
        downloaded = 0;
        status = DownStatus.DOWNLOADING;
    }

    @Override
    public DownStatus call() throws Exception {
        RandomAccessFile file = null;
        InputStream stream = null;

        try {
            HttpURLConnection connection =
                    (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Range",
                    downloaded + "-");

            connection.connect();

            if (connection.getResponseCode() / 100 != 2) {
                throw new Exception("Can not open connection: " + connection.getResponseCode());
            }

            int contentLength = connection.getContentLength();
            if (contentLength < 1) {
                throw new Exception("Can not get content size");
            }

            if (size == -1) {
                size = contentLength;
            }

            System.out.println("Start download "+ this.url + " to " + this.destFile.getAbsolutePath());
            file = new RandomAccessFile(this.destFile, "rw");
            file.seek(downloaded);

            stream = connection.getInputStream();
            while (status == DownStatus.DOWNLOADING) {
                byte buffer[];
                if (size - downloaded > MAX_BUFFER_SIZE) {
                    buffer = new byte[MAX_BUFFER_SIZE];
                } else {
                    buffer = new byte[size - downloaded];
                }

                int read = stream.read(buffer);
                if (read == -1)
                    break;

                file.write(buffer, 0, read);
                downloaded += read;
            }

            System.out.println("downloaded "+downloaded);

            if (status == DownStatus.DOWNLOADING) {
                status = DownStatus.COMPLITE;
            }
        } catch (Exception e) {
            System.out.println("Error "+e.getMessage());
            //error();
        } finally {
            if (file != null)
                try {
                    file.close();
                } catch (Exception e) {
                    // ...
                }

            if (stream != null)
                try {
                    stream.close();
                } catch (Exception e) {
                    // ...
                }
        }

        return status;
    }

}

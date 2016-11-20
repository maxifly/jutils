package com.maxifly.jutils.downloader;

import com.maxifly.jutils.I_Progress;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/**
 * Created by Maximus on 01.06.2016.
 */
public class Download implements Callable<DownStatus> {

    private static final int MAX_BUFFER_SIZE = 1024;


    private URL url;
    private File destFile;
    //private String destFileName;
    private int size;
    private int downloaded;
    private DownStatus status;
    private I_Progress progress = null;
    private String srcFileName = null;
    private Integer bufferSize;

    public Download(URL url, String destFileName) {
        super();
        this.bufferSize = MAX_BUFFER_SIZE;
        this.prepare(url, new File(destFileName));
    }

    public Download(URL url, String destFileName, int bufferSize) {
        super();
        this.bufferSize = bufferSize;
        this.prepare(url, new File(destFileName));
    }

    public void setProgress(I_Progress progress) {
        this.progress = progress;
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
        this.srcFileName = (new File(url.getFile())).getName();
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
                if (size - downloaded > this.bufferSize) {
                    buffer = new byte[this.bufferSize];
                } else {
                    buffer = new byte[size - downloaded];
                }

                int read = stream.read(buffer);
                if (read == -1)
                    break;

                file.write(buffer, 0, read);
                downloaded += read;
                change_progress();
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


    private void change_progress() {
        if (progress != null && size > 0) {
           progress.updateProgress(downloaded,size,srcFileName + " (" + downloaded + " из " + size +")");
        }
    }
}

package eternity;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class WriteFileQueue extends Thread
{
    private final MemoryQueue queue;

    WriteFileQueue(MemoryQueue q)
    {
        queue = q;
    }

    private synchronized void writeToFile(FileToWrite ftw)
    {
        BufferedWriter bw = null;

        try
        {
            bw = new BufferedWriter(new FileWriter(ftw.getFilename(), ftw.isAppend()));

            bw.write(ftw.getContent().toString());
            bw.flush();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
        finally
        {
            if (bw != null)
                try
                {
                    bw.close();
                }
                catch (IOException ioe)
                {
                    ioe.printStackTrace();
                }
        }
    }

    public synchronized void Stop()
    {
        synchronized(queue)
        {
            queue.send("");
        }
    }

    @Override
    public void run()
    {
        boolean goOn = true;
        FileToWrite value = null;
        System.out.println("start WriteMessageQueue...");

        while (goOn)
        {
            try
            {
                synchronized(queue)
                {
                    try
                    {
                        value = (FileToWrite) queue.receive();
                        if (value != null)
                            writeToFile(value);
                    }
                    catch (Exception e)
                    {
                        goOn = false;
                    }
                }
                //sleep(500);
            }
            /*catch (InterruptedException ex)
            {
                goOn = false;
            }*/
            catch (RuntimeException re)
            {
                goOn = false;
            }
        }

        System.out.println("stop WriteMessageQueue.");
    }
}

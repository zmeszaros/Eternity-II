package eternity;

public class PrintMessageQueue extends Thread
{
    private final MemoryQueue queue;

    PrintMessageQueue(MemoryQueue q)
    {
        queue = q;
    }

    public synchronized void Stop()
    {
        synchronized(queue)
        {
            queue.send("*");
        }
    }

    @Override
    public void run()
    {
        String value = "start PrintMessageQueue...\n";
        while (!value.equals("*"))
        {
            try
            {
                System.out.print(value);
                synchronized(queue)
                {
                    value = queue.receive().toString();
                }
            }
            catch (RuntimeException e) {}
        }

        System.out.println("stop PrintMessageQueue.");
    }
}
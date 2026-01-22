package eternity;

import java.util.LinkedList;

public class MemoryQueue
{
    protected LinkedList list = new LinkedList();

    public MemoryQueue() {}

    public synchronized boolean isEmpty()
    {
        boolean result;
        synchronized (this)
        {
            result = list.isEmpty();
        }
        return result;
    }

    /**
     * Send an object into the queue.
     *
     * @param value
     */
    public synchronized void send(Object value)
    {
        if (value == null)
        {
            throw new NullPointerException("You can not send null into a queue.");
        }
        synchronized (this)
        {
            list.add(value);
            this.notifyAll();
        }
    }

    /**
     * Gets out the top element of the queue. This is an asynchronous version
     * that returns {@code null} if the queue is empty.
     * <p>
     *
     * @return an object from the queue or {@code null}.
     */
    private synchronized Object receiveAsync()
    {
        Object obj = list.isEmpty() ? null : list.get(0);
        if (obj != null)
        {
            list.remove(0);
        }
        return obj;
    }

    /**
     * Get the top element of the queue. Wait infinitely until there is an
     * element available.
     *
     * @return an object from the queue.
     */
    public synchronized Object receive()
    {
        Object obj;
        while ((obj = receiveAsync()) == null)
        {
            try
            {
                this.wait();
            }
            catch (InterruptedException e) { }
        }
        return obj;
    }

    private synchronized Object receiveTill(long end)
    {
        Object obj = null;
        long now;
        while ((obj = receiveAsync()) == null && ((now = System.currentTimeMillis()) < end))
        {
            try
            {
                this.wait(end - now);
            }
            catch (InterruptedException e) {
            }
        }
        return obj;
    }

    /**
     * Get the top element of the queue or return {@code null} if there is none
     * within {@code timeout} millis.
     *
     * @param timeout
     * @return the object or null
     */
    public Object receive(long timeout)
    {
        long now = System.currentTimeMillis();
        long end = now + timeout;
        return receiveTill(end);
    }
}
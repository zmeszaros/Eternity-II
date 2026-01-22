package eternity;

public class FileToWrite
{
    private String filename = null;
    private boolean append = false;
    private StringBuilder content = null;

    public FileToWrite(String fn, boolean app, StringBuilder cont)
    {
        filename = fn;
        append = app;
        content = cont;
    }

    public FileToWrite getFileToWrite()
    {
        return this;
    }

    public String getFilename()
    {
        return filename;
    }

    public boolean isAppend()
    {
        return append;
    }

    public StringBuilder getContent()
    {
        return content;
    }
}

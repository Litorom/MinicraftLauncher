package launcher;

import java.io.IOException;
import java.io.File;
import java.io.Serializable;

public class MinicraftVersion implements Serializable
{
    public String name;
    public String description;
    public String fileurl;
    public File localFile;
    public boolean downloaded;

    public void play(File f) {
        final ProcessBuilder pb = new ProcessBuilder("java", "-jar", f.getAbsolutePath());
        pb.directory(new File(MinicraftLauncher.getSaveDirectory()));
        try {
            pb.start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public String toString() {
        return this.name;
    }
}

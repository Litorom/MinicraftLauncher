package launcher;

import java.awt.*;
import parser.GetReleaseParser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;

public class MinicraftLauncher {
    Frame f = new Frame("Minicrafters Launcher");
    public MinicraftVersion minicraftVersion = new MinicraftVersion();
    public ArrayList<MinicraftVersion> versions;
    final Choice ch = new Choice();
    final Checkbox modded = new Checkbox("Modded?", false);
    private Button playButton;
    private Button downloadButton;

    public MinicraftLauncher() {
        this.loadMinicraftVersions();
        this.versions = new ArrayList<MinicraftVersion>();
        this.initializeStartingScene();
        f.setVisible(true);
        new WindowCloser();
    }

    private void saveMinicraftVersions() {
        final String filePath = getSaveDirectory() + "launcher.data";
        try {
            final FileOutputStream fout = new FileOutputStream(filePath);
            final ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(this.versions);
            oos.close();
            fout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadMinicraftVersions() {
        final String filePath = getSaveDirectory() + "launcher.data";
        try {
            final FileInputStream fin = new FileInputStream(filePath);
            final ObjectInputStream ois = new ObjectInputStream(fin);
            this.versions = (ArrayList<MinicraftVersion>) ois.readObject();
            ois.close();
            fin.close();
        } catch (Exception ex) {
        }
        final ArrayList<MinicraftVersion> githubVersions = GetReleaseParser.parseGithubReleases();
        for (final MinicraftVersion v : githubVersions) {
            boolean addVersion = true;
            for (final MinicraftVersion v2 : this.versions) {
                if (v.name.equals(v2.name)) {
                    addVersion = false;
                    break;
                }
            }
            if (addVersion) {
                this.versions.add(v);
            }
        }
    }

    public void loadMinicraftMods() {
        final String filePath = getSaveDirectory() + "launcher.data";
        try {
            final FileInputStream fin = new FileInputStream(filePath);
            final ObjectInputStream ois = new ObjectInputStream(fin);
            this.versions = (ArrayList<MinicraftVersion>) ois.readObject();
            ois.close();
            fin.close();
        } catch (Exception ex) {
        }
        final ArrayList<MinicraftVersion> githubVersions = GetReleaseParser.parseGithubReleases();
        for (final MinicraftVersion v : githubVersions) {
            boolean addVersion = true;
            for (final MinicraftVersion v2 : this.versions) {
                if (v.name.equals(v2.name)) {
                    addVersion = false;
                    break;
                }
            }
            if (addVersion) {
                this.versions.add(v);
            }
        }
    }

    private void initializeStartingScene() {
        f.setSize(400, 400);
        this.initializeWelcomeScreen();
    }

    public void initializeWelcomeScreen() {
        modded.setName("Modded?");
        f.setLayout(new FlowLayout(FlowLayout.LEFT));
        final Label versionSelectLabel = new Label("Select version: ");
        loadMinicraftVersions();
        for (MinicraftVersion version : versions) {
            ch.addItem(version.name);
        }
        final Label versionDescriptionLabel = new Label("");
        this.playButton = new Button("Play");
        this.downloadButton = new Button("Download");
        Boolean downloaded = minicraftVersion.downloaded;
        String newVersion = ch.getSelectedItem();
        String description = minicraftVersion.description;
        versionDescriptionLabel.setText(description);
        f.add(versionSelectLabel);
        f.add(ch);
        f.remove(playButton);
        f.remove(downloadButton);
        if (downloaded) {
            f.add(playButton);
        } else {
            f.add(downloadButton);
        }
        f.add(modded);
        f.add(versionDescriptionLabel);
        installListener install = new installListener();
        playListener play = new playListener();
        this.downloadButton.addActionListener(install);
        this.playButton.addActionListener(play);
    }

    private void installNewMinicraftVersion(MinicraftVersion version) {
        loadMinicraftVersions();
        try {
            final URL fileUrl = new URL(versions.get(ch.getSelectedIndex()).fileurl);
            final ReadableByteChannel rbc = Channels.newChannel(fileUrl.openStream());
            final File f = new File(getSaveDirectory() + version.name + ".jar");
            f.getParentFile().mkdirs();
            final FileOutputStream fos = new FileOutputStream(getSaveDirectory() + version.name + ".jar");
            fos.getChannel().transferFrom(rbc, 0L, Long.MAX_VALUE);
            fos.close();
            version.localFile = f;
            version.downloaded = true;
            this.f.remove(downloadButton);
            this.f.add(playButton);
            this.saveMinicraftVersions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getSaveDirectory() {
        final String OS = System.getProperty("os.name").toLowerCase();
        String path = "";
        if (OS.contains("windows")) {
            path = path + System.getenv("APPDATA");
            path = path + "/playminicraft/launcher/MinicraftPlus/";
        } else {
            path = path + System.getProperty("user.home");
            path = path + "/.playminicraft/launcher/MinicraftPlus/";
        }
        return path;
    }

    class installListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            installNewMinicraftVersion(minicraftVersion);
        }
    }

    class playListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            new MinicraftVersion().play();
        }
    }
    class WindowCloser extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            super.windowClosing(e);
            System.exit(0);
        }
    }
}




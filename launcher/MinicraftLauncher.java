package launcher;

import java.awt.*;

import parser.GetModParser;
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
    public MinicraftMods minicraftMods = new MinicraftMods();
    public ArrayList<MinicraftVersion> versions = new ArrayList<MinicraftVersion>();
    public ArrayList<MinicraftMods> mods;
    final Choice ch = new Choice();
    final Choice modList = new Choice();
    final Checkbox modded = new Checkbox("Modded?", false);
    private Button playButton;
    private Button downloadButton;
    private Button installModButton;


    public MinicraftLauncher() {
        this.loadMinicraftVersions();
        this.initializeStartingScene();
        f.setVisible(true);
        this.downloadButton.addActionListener(new installListener());
        this.playButton.addActionListener(new playListener());

            this.initializeModdedScreen();

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
        } catch (Exception ignored) {
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
            this.mods = (ArrayList<MinicraftMods>) ois.readObject();
            ois.close();
            fin.close();
        } catch (Exception ignored) {
        }
        final ArrayList<MinicraftMods> githubVersions = GetModParser.parseGithubReleases();
        for (final MinicraftMods v : githubVersions) {
            boolean addVersion = true;
            for (final MinicraftMods v2 : this.mods) {
                if (v.name.equals(v2.name)) {
                    addVersion = false;
                    break;
                }
            }
            if (addVersion) {
                this.mods.add(v);
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
        Boolean downloaded = versions.get(ch.getSelectedIndex()).downloaded;
        String newVersion = ch.getSelectedItem();
        String description = versions.get(ch.getSelectedIndex()).description;
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
        f.add(versionDescriptionLabel).setLocation(0,20);
    }

    public void initializeModdedScreen() {
        f.setLayout(new FlowLayout(FlowLayout.LEFT));
        final Label versionSelectLabel = new Label("Select Mod: ");
        loadMinicraftVersions();
        for (MinicraftMods mods : mods) {
            modList.addItem(mods.name);
        }
        this.installModButton = new Button("Install");
        Boolean downloaded = minicraftMods.downloaded;
        f.add(versionSelectLabel);
        f.add(modList);
        f.remove(installModButton);
        if (!downloaded) {
            f.add(installModButton);
        }
        this.installModButton.addActionListener(new installModsListener());
    }


    private void installNewMinicraftVersion(MinicraftVersion version) {
        loadMinicraftVersions();
        try {
            final URL fileUrl = new URL(versions.get(ch.getSelectedIndex()).fileurl);
            final ReadableByteChannel rbc = Channels.newChannel(fileUrl.openStream());
            final File f = new File(getSaveDirectory() + versions.get(ch.getSelectedIndex()).name + ".jar");
            f.getParentFile().mkdirs();
            final FileOutputStream fos = new FileOutputStream(getSaveDirectory() + versions.get(ch.getSelectedIndex()).name + ".jar");
            fos.getChannel().transferFrom(rbc, 0L, Long.MAX_VALUE);
            fos.close();
            versions.get(ch.getSelectedIndex()).localFile = f;
            versions.get(ch.getSelectedIndex()).downloaded = true;
            this.f.remove(downloadButton);
            this.f.add(playButton);
            this.saveMinicraftVersions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void installNewMinicraftMod(MinicraftMods mod) {
        loadMinicraftVersions();
        try {
            final URL fileUrl = new URL(mods.get(modList.getSelectedIndex()).fileurl);
            final ReadableByteChannel rbc = Channels.newChannel(fileUrl.openStream());
            final File f = new File(getSaveDirectory() + mod.name + ".jar");
            f.getParentFile().mkdirs();
            final FileOutputStream fos = new FileOutputStream(getSaveDirectory() + mod.name + ".jar");
            fos.getChannel().transferFrom(rbc, 0L, Long.MAX_VALUE);
            fos.close();
            mod.localFile = f;
            mod.downloaded = true;
            this.f.remove(installModButton);
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

    class installModsListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            installNewMinicraftMod(minicraftMods);
        }
    }

    static class playListener implements ActionListener {

        final MinicraftVersion minicraftVersion = new MinicraftVersion();
        @Override
        public void actionPerformed(ActionEvent e) {
            minicraftVersion.play(minicraftVersion.localFile);
        }
    }

    static class WindowCloser extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            super.windowClosing(e);
            System.exit(0);
        }
    }
}




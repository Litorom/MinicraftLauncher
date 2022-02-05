// 
// Decompiled by Procyon v0.5.36
// 

package parser;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import org.json.simple.parser.ParseException;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import launcher.MinicraftVersion;
import java.util.ArrayList;

public class GetReleaseParser
{
    public static ArrayList<MinicraftVersion> parseGithubReleases() {
        final ArrayList<MinicraftVersion> versions = new ArrayList<MinicraftVersion>();
        final String json = getJsonFromGithub();
        final JSONParser parser = new JSONParser();
        try {
            final Object parsedObject = parser.parse(json);
            final JSONArray allReleases = (JSONArray)parsedObject;
            for (int i = 0; i < allReleases.size(); ++i) {
                final JSONObject release = (JSONObject) allReleases.get(i);
                final String name = (String) release.get("name");
                final String desc = (String) release.get("body");
                String downloadUrl = "";
                final JSONArray assets = (JSONArray) release.get("assets");
                for (final Object o : assets) {
                    final JSONObject asset = (JSONObject)o;
                    final String assetName = (String) asset.get("name");
                    if (assetName.contains(".jar")) {
                        downloadUrl = (String) asset.get("browser_download_url");
                    }
                }
                final MinicraftVersion version = new MinicraftVersion();
                version.name = name;
                version.description = desc;
                version.fileurl = downloadUrl;
                versions.add(version);
            }
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return versions;
    }
    
    private static String getJsonFromGithub() {
        final String url = "https://api.github.com/repos/MinicraftPlus/minicraft-plus-revived/releases";
        final StringBuilder jsonBuilder = new StringBuilder();
        try {
            final InputStream response = new URL(url).openStream();
            int byteCount = 1;
            while (byteCount > 0) {
                final byte[] bytes = new byte[10000];
                byteCount = response.read(bytes);
                for (int i = 0; i < byteCount; ++i) {
                    jsonBuilder.append((char)bytes[i]);
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return jsonBuilder.toString();
    }
}

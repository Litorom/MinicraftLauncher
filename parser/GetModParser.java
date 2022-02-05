// 
// Decompiled by Procyon v0.5.36
// 

package parser;

import launcher.MinicraftMods;
import launcher.MinicraftVersion;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class GetModParser
{
    public static ArrayList<MinicraftMods> parseGithubReleases() {
        final ArrayList<MinicraftMods> minicraftMods = new ArrayList<MinicraftMods>();
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
                final MinicraftMods mods = new MinicraftMods();
                mods.name = name;
                mods.description = desc;
                mods.fileurl = downloadUrl;
                minicraftMods.add(mods);
            }
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return minicraftMods;
    }
    
    private static String getJsonFromGithub() {
        final MinicraftVersion minicraftVersion = new MinicraftVersion();
        final String url = "https://api.github.com/repos/Litorom/MiniFabric-Mod-Portal/contents/mods"+minicraftVersion.name;
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

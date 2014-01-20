package de.gemo.smartlauncher.listener;

import java.util.List;

import javax.swing.JOptionPane;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import de.gemo.smartlauncher.actions.GetPackIconAction;
import de.gemo.smartlauncher.core.Logger;
import de.gemo.smartlauncher.core.Main;
import de.gemo.smartlauncher.frames.MainFrame;
import de.gemo.smartlauncher.internet.GETResponse;
import de.gemo.smartlauncher.internet.Worker;
import de.gemo.smartlauncher.internet.HTTPAction;
import de.gemo.smartlauncher.internet.HTTPListener;
import de.gemo.smartlauncher.units.Pack;
import de.gemo.smartlauncher.units.PackVersion;

public class GetPacksListener extends HTTPListener {

    public void onStart(HTTPAction action) {
        Logger.fine("getting packs...");
    }

    public void onFinish(HTTPAction action) {
        GETResponse response = (GETResponse) this.getWorker().getResponse();
        try {
            JsonObject object = JsonObject.readFrom(response.getResponse().toString());
            JsonArray json = object.get("packs").asArray();

            List<JsonValue> singlePacks = json.values();
            for (JsonValue singlePack : singlePacks) {
                try {
                    JsonObject thisPack = singlePack.asObject();
                    String packName = thisPack.get("name").asString();
                    if (Pack.loadedPacks.containsKey(packName)) {
                        Logger.warning("Pack with name '" + packName + "' already exists! Ignoring this one...");
                        continue;
                    }

                    String recommended = thisPack.get("recommended_version").asString();
                    Pack pack = new Pack(packName);

                    // add recommended version...
                    pack.addVersion(new PackVersion(recommended + (" - recommended")));

                    JsonValue versionObject = thisPack.get("versions");
                    if (versionObject != null) {
                        JsonArray versionArray = versionObject.asArray();
                        for (int index = 0; index < versionArray.size(); index++) {
                            JsonValue value = versionArray.get(index);
                            try {
                                pack.addVersion(new PackVersion(value.asString()));
                            } catch (Exception e2) {
                                // ignore this version....
                                e2.printStackTrace();
                            }
                        }
                    }
                    Main.appendWorker(new Worker(new GetPackIconAction(pack), new GetPackIconListener(pack)));
                    Pack.loadedPacks.put(packName, pack);
                    Logger.fine("loaded pack '" + pack.getPackName() + "' with " + pack.getVersions().size() + " versions...");
                } catch (Exception e) {
                    // ignore pack...
                    e.printStackTrace();
                }
            }
            Logger.fine("Packs fetched...");

            // get icons...
            Main.startThread();

        } catch (Exception e) {
            e.printStackTrace();
            this.onError(action);
            return;
        }
    }

    @Override
    public void onError(HTTPAction action) {
        Logger.error("Could not fetch packs!");
        JOptionPane.showMessageDialog(null, "Could not fetch available packs...\n\nExiting....", "Error", JOptionPane.ERROR_MESSAGE);
        MainFrame.INSTANCE.exit(0);
    }

    @Override
    public void onProgress(int maximumLength, int currentLength) {
        // do nothing...
    }

}

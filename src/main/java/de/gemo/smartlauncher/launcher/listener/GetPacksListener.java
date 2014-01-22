package de.gemo.smartlauncher.launcher.listener;

import java.util.List;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import de.gemo.smartlauncher.launcher.actions.GetPackIconAction;
import de.gemo.smartlauncher.launcher.core.GameLauncher;
import de.gemo.smartlauncher.launcher.core.Logger;
import de.gemo.smartlauncher.launcher.frames.MainFrame;
import de.gemo.smartlauncher.launcher.units.Pack;
import de.gemo.smartlauncher.launcher.units.PackVersion;
import de.gemo.smartlauncher.universal.internet.GETResponse;
import de.gemo.smartlauncher.universal.internet.HTTPAction;
import de.gemo.smartlauncher.universal.internet.HTTPListener;
import de.gemo.smartlauncher.universal.internet.Worker;
import de.gemo.smartlauncher.universal.units.ThreadHolder;

public class GetPacksListener extends HTTPListener {

    public void onStart(HTTPAction action) {
        Logger.info("getting packs...");
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
                    ThreadHolder.appendWorker(new Worker(new GetPackIconAction(pack), new GetPackIconListener(pack)));
                    Pack.loadedPacks.put(packName, pack);
                    Logger.fine("loaded pack '" + pack.getPackName() + "' with " + pack.getVersions().size() + " versions...");
                } catch (Exception e) {
                    // ignore pack...
                    e.printStackTrace();
                }
            }
            Logger.fine("Packs fetched...");

            // get icons...
            ThreadHolder.startThread();

        } catch (Exception e) {
            e.printStackTrace();
            this.onError(action);
            return;
        }
    }

    @Override
    public void onError(HTTPAction action) {
        GameLauncher.handleException(new Exception("Could not fetch available packs...\nExiting..."));
        MainFrame.INSTANCE.exit(0);
    }

    @Override
    public void onProgress(int maximumLength, int currentLength) {
        // do nothing...
    }

}

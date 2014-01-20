package de.gemo.smartlauncher.listener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JOptionPane;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import de.gemo.smartlauncher.actions.GetSinglePackAction;
import de.gemo.smartlauncher.core.Launcher;
import de.gemo.smartlauncher.core.Logger;
import de.gemo.smartlauncher.core.Main;
import de.gemo.smartlauncher.frames.MainFrame;
import de.gemo.smartlauncher.frames.StatusFrame;
import de.gemo.smartlauncher.internet.ByteResponse;
import de.gemo.smartlauncher.internet.HTTPAction;
import de.gemo.smartlauncher.internet.HTTPListener;
import de.gemo.smartlauncher.units.Library;
import de.gemo.smartlauncher.units.VARS;

public class GetSinglePackListener extends HTTPListener {

    public void onStart(HTTPAction action) {
        Logger.info(action.getShortDescription());
    }

    public void onFinish(HTTPAction action) {
        try {
            GetSinglePackAction thisAction = (GetSinglePackAction) action;
            if (action.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Logger.fine("Pack downloaded... " + thisAction.getPack().getPackName() + " - " + thisAction.getPackVersion());

                // get response
                ByteResponse response = (ByteResponse) this.getWorker().getResponse();

                // handle...
                this.handlePack(thisAction, response.getResponse());

                // launch game...
                Launcher.INSTANCE.launchPack();
            } else if (action.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                // clear all...
                Launcher.onError();

                // show info...
                StatusFrame.INSTANCE.showFrame(false);
                MainFrame.INSTANCE.showFrame(true);
                Logger.error("User is not allowed to use this pack! (ERROR " + action.getResponseCode() + ")");
                JOptionPane.showMessageDialog(null, "You are not allowed to use this pack!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                this.onError(action);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.onError(action);
            return;
        }
    }

    @Override
    public void onError(HTTPAction action) {
        // clear...
        Launcher.onError();

        // show info...
        Logger.error("Could not download pack! (Statuscode: " + action.getResponseCode() + ")");
        StatusFrame.INSTANCE.showFrame(false);
        MainFrame.INSTANCE.showFrame(true);
        JOptionPane.showMessageDialog(null, "Could not download pack...\n\nExiting....", "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void handlePack(GetSinglePackAction thisAction, byte[] bytes) throws Exception {
        InputStream inputStream = new ByteArrayInputStream(bytes);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(inputStream));
        ZipEntry entry;

        String dir = VARS.DIR.PROFILES + "/" + Main.authData.getMCUserName() + "/" + thisAction.getPack().getPackName() + "/" + thisAction.getPackVersion() + "/";
        while ((entry = zis.getNextEntry()) != null) {
            String entryName = entry.getName().replaceFirst(thisAction.getPack().getPackName() + "/", "");
            if (entry.isDirectory()) {
                entryName = entryName.replaceAll(thisAction.getPack().getPackName() + "/minecraft/", "");
                File file = new File(dir + entryName);
                file.mkdirs();
                continue;
            }
            entryName = entryName.replaceAll(thisAction.getPack().getPackName() + "/minecraft/", "");

            int size;
            byte[] buffer = new byte[2048];

            entryName = (dir + entryName).replaceAll("/", "\\\\");
            File file = new File(entryName);

            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length);

            while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
                bos.write(buffer, 0, size);
            }
            bos.flush();
            bos.close();
            if (entryName.endsWith("pack.json")) {
                if (!this.handlePackJson(entryName)) {
                    throw new Exception("pack.json is invalid!");
                }
            }
        }

        zis.close();

    }

    private boolean handlePackJson(String fileName) {
        return this.handlePackJson(new File(fileName));
    }

    public boolean handlePackJson(File file) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            JsonObject json = JsonObject.readFrom(reader);

            // get minecraft-version
            JsonValue jsonValue = json.get("id");
            if (jsonValue == null) {
                return false;
            }
            Launcher.getPackInfo().setGameVersion(jsonValue.asString().replaceAll("\"", ""));

            // override mainClass...
            jsonValue = json.get("mainClass");
            if (jsonValue != null) {
                Launcher.getPackInfo().overrideMainClass(jsonValue.toString());
            }

            // override mcArguments
            jsonValue = json.get("minecraftArguments");
            if (jsonValue != null) {
                Launcher.getPackInfo().overrideMCArguments(jsonValue.toString());
            }

            // get libraries...
            jsonValue = json.get("libraries");
            if (jsonValue != null) {
                JsonArray jsonLib = jsonValue.asArray();
                JsonObject singleLibrary;
                for (JsonValue value : jsonLib.values()) {
                    singleLibrary = value.asObject();
                    Library lib = new Library(singleLibrary);

                    // add the library, if it should be added...
                    if (lib.addLibrary()) {
                        lib.addLibraryToDownloads();
                    }
                }
            }

            // close stream
            reader.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onProgress(int maximumLength, int currentLength) {
        float percent = ((float) currentLength / (float) maximumLength) * 100;
        int percentInt = (int) (percent * 100);
        percent = percentInt / 100f;
        StatusFrame.INSTANCE.setProgress((int) percent);
    }

}

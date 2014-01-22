package de.gemo.smartlauncher.launcher.listener;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.swing.JOptionPane;

import com.eclipsesource.json.JsonObject;

import de.gemo.smartlauncher.launcher.actions.GetPacksAction;
import de.gemo.smartlauncher.launcher.core.Logger;
import de.gemo.smartlauncher.launcher.core.Main;
import de.gemo.smartlauncher.launcher.frames.LoginFrame;
import de.gemo.smartlauncher.launcher.units.AuthData;
import de.gemo.smartlauncher.universal.frames.StatusFrame;
import de.gemo.smartlauncher.universal.internet.GETResponse;
import de.gemo.smartlauncher.universal.internet.HTTPAction;
import de.gemo.smartlauncher.universal.internet.HTTPListener;
import de.gemo.smartlauncher.universal.internet.Worker;

public class LoginListener extends HTTPListener {

    public void onStart(HTTPAction action) {
        Logger.info("Logging in...");
    }

    public void onFinish(HTTPAction action) {
        GETResponse response = (GETResponse) this.getWorker().getResponse();
        AuthData loginData = Main.authData;
        try {
            if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // LOGIN OK
                JsonObject answer = JsonObject.readFrom(response.getResponse().toString());
                JsonObject profiles = answer.get("selectedProfile").asObject();

                // set vars...
                loginData.setMCUserName(profiles.get("name").toString());
                loginData.setClientToken(answer.get("clientToken").toString());
                loginData.setAccessToken(answer.get("accessToken").asString());
                loginData.setProfileID(profiles.get("id").toString());
                if (profiles.get("legacy") == null) {
                    loginData.setLegacy(false);
                } else {
                    loginData.setLegacy(profiles.get("legacy").asBoolean());
                }
                try {
                    loginData.save();
                } catch (IOException e) {
                    e.printStackTrace();
                    loginData.resetData();
                    JOptionPane.showMessageDialog(null, "Could not save profile!", "Login failed!", JOptionPane.ERROR_MESSAGE);
                    LoginFrame.INSTANCE.enableLoginGUI(true);
                    return;
                }

                Logger.fine("Logged in as '" + loginData.getMCUserName() + "'...");

                // get packs...
                Main.appendWorker(new Worker(new GetPacksAction(), new GetPacksListener()));
                Main.startThread();
            } else {
                this.onError(action);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.onError(action);
        }
    }

    public void onError(HTTPAction action) {
        if (StatusFrame.INSTANCE != null) {
            StatusFrame.INSTANCE.showFrame(false);
        }

        GETResponse response = (GETResponse) this.getWorker().getResponse();
        JsonObject json = JsonObject.readFrom(response.getResponse().toString());
        if (json != null) {
            String message = json.get("errorMessage").asString();
            JOptionPane.showMessageDialog(null, message, "Login failed!", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, response.getResponse(), "ERROR", JOptionPane.ERROR_MESSAGE);
        }

        if (LoginFrame.INSTANCE == null) {
            new LoginFrame(200, 190);
        } else {
            LoginFrame.INSTANCE.showGUI(true);
        }
        LoginFrame.INSTANCE.enableLoginGUI(true);
        Logger.warning("Login failed...");
    }

    @Override
    public void onProgress(int maximumLength, int currentLength) {
        // do nothing...
    }

}

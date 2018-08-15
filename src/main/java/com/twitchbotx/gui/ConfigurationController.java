/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.twitchbotx.gui;

import com.twitchbotx.bot.Datastore;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 * FXML Controller class
 *
 * @author Raxa
 */
public class ConfigurationController implements Initializable {

    ScreensController myController = new ScreensController();
    Datastore store;

    @FXML
    TextField testMessageText;

    @FXML
    TextField soundTestText;

    @FXML
    RadioButton ytEnabled;

    @FXML
    RadioButton ytDisabled;

    @FXML
    ToggleGroup ytToggle;

    @FXML
    private void dash(ActionEvent event) {
        setDimensions();
        myController.loadScreen(guiHandler.dashboardID, guiHandler.dashboardFile);
        myController.setScreen(guiHandler.dashboardID);
        myController.setId("dashboard");
        myController.show(myController);
    }

    // Takes user input filename and attempts to play
    // must be mp3 or wav
    @FXML
    private void playTestSound() {
        try {
            soundTestText.selectAll();
            soundTestText.copy();
            Path xmlFile = Paths.get("");
            Path xmlResolved = xmlFile.resolve(soundTestText.getText());
            Media hit = new Media(xmlResolved.toUri().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(hit);
            mediaPlayer.play();
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    guiHandler.bot.getStore().getEventList().addList("Successful sound test");
                }
            });
        } catch (Exception e) {
            soundTestText.setText("Error playing song");
        }
    }

    // Takes user input message to send to chat
    @FXML
    private void sendTestMessage() {
        testMessageText.selectAll();
        testMessageText.copy();
        String message = testMessageText.getText();
        try {
            DashboardController.twitchWSIRC.sendMessage(message);
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    guiHandler.bot.getStore().getEventList().addList("Attempted to send: \'" + message + "\' to chat, reconnect if failed");
                }
            });
        } catch (Exception e) {
            System.out.println("error occured sending test");
            e.printStackTrace();
        }
    }

    @FXML
    public void saveSettings() {
        //grab radiobuttons, etc and save all content
        RadioButton toggle = (RadioButton) ytToggle.getSelectedToggle();
        String choice = toggle.getText();
        System.out.println(choice);
        String enabled;
        if (choice.equals("Enabled")) {
            enabled = "on";
        } else {
            enabled = "off";
        }
        //send enabled to yt here
        store.modifyConfiguration("ytStatus", enabled);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        store = guiHandler.bot.getStore();
        if (store.getConfiguration().ytStatus.equalsIgnoreCase("on")) {
            ytEnabled.setSelected(true);
        } else {
            ytDisabled.setSelected(true);
        }
    }

    guiHandler.dimensions dm = ScreensController.dm;

    private void setDimensions() {
        int h = (int) guiHandler.stage.getHeight();
        int w = (int) guiHandler.stage.getWidth();
        dm.setHeight(h);
        dm.setWidth(w);
    }

}

package com.twitchbotx.bot.handlers;

import com.twitchbotx.bot.Datastore;
import com.twitchbotx.gui.guiHandler;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public final class TwitchStatusHandler {

    private static final Logger LOGGER = Logger.getLogger(TwitchStatusHandler.class.getSimpleName());

    private final Datastore store;

    public TwitchStatusHandler(final Datastore store) {
        this.store = store;
    }

    /**
     * This creates the URL = api.twitch.tv/kraken with desired streamer name("myChannel") from kfbot1.0.xml
     * Opens a connection, begins reading using BufferedReader brin, builds a String response based on API reply
     * nce response is done building, checks for "stream\:null" response - this means stream is not live
     * Creates Strings to hold content placed between int "bi" and int "ei" as per their defined index
     *
     * @param msg
     */
    public String uptime(final String msg) {
        try {
            String statusURL = guiHandler.bot.getStore().getConfiguration().streamerStatus;
            statusURL = statusURL.replaceAll("#streamer", guiHandler.bot.getStore().getConfiguration().joinedChannel);
            URL url = new URL(statusURL);
            URLConnection con = (URLConnection) url.openConnection();
            con.setRequestProperty("Accept", "application/vnd.twitchtv.v3+json");
            con.setRequestProperty("Authorization", guiHandler.bot.getStore().getConfiguration().password);
            con.setRequestProperty("Client-ID", guiHandler.bot.getStore().getConfiguration().clientID);
            BufferedReader brin = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = brin.readLine()) != null) {
                response.append(inputLine);
            }
            brin.close();
            if (response.toString().contains("\"stream\":null")) {
                return "Stream is not currently live.";
            }

            else {
                int bi = response.toString().indexOf("\"created_at\":") + 14;
                int ei = response.toString().indexOf("\",", bi);
                String s = response.toString().substring(bi, ei);
                Instant start = Instant.parse(s);
                Instant current = Instant.now();
                long gap = ChronoUnit.MILLIS.between(start, current);
                String upT = String.format("%d hours, %d minutes, %d seconds", new Object[]{
                        TimeUnit.MILLISECONDS.toHours(gap),
                        TimeUnit.MILLISECONDS.toMinutes(gap) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(gap)),
                        TimeUnit.MILLISECONDS.toSeconds(gap) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(gap))
                });
                return "Stream has been up for " + upT + ".";
            }
        } catch (Exception e) {
            LOGGER.severe(e.toString());
        }

        return "Unable to connect to Twitch server. Please try again later.";
    }

    /*
** This delivers the original follow date
**
** @param user
** @return formated date of created_at per https://api.twitch.tv/kraken/users/test_user1/follows/channels/test_channel
**
     */
     public String followage(final String user) {
        try {
            String followURL = guiHandler.bot.getStore().getConfiguration().followage;
            //test values
            //followURL = followURL.replaceAll("#user", "raxa");
            //followURL = followURL.replaceAll("#streamer", "kungfufruitcup");
            followURL = followURL.replaceAll("#user", user);
            followURL = followURL.replaceAll("#streamer", guiHandler.bot.getStore().getConfiguration().joinedChannel);
            URL url = new URL(followURL);
            URLConnection con = (URLConnection) url.openConnection();
            con.setRequestProperty("Accept", "application/vnd.twitchtv.v3+json");
            con.setRequestProperty("Authorization", guiHandler.bot.getStore().getConfiguration().password);
            con.setRequestProperty("Client-ID", guiHandler.bot.getStore().getConfiguration().clientID);
            BufferedReader brin = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = brin.readLine()) != null) {
                response.append(inputLine);
            }

            System.out.println(response);
            
            int bi = response.toString().indexOf("\"created_at\":") + 14;
            int ei = response.toString().indexOf("\"", bi);
            String s = response.toString().substring(bi, ei);

            System.out.println(s);
            
            DateTimeFormatter full = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss'Z'");
            DateTimeFormatter format = DateTimeFormatter.ofPattern("MMM dd, uuuu");
            ZoneId z = ZoneId.of("UTC-1");
            LocalDateTime begin = LocalDateTime.parse(s, full);
            begin.atZone(z);
            LocalDateTime today = LocalDateTime.now(z);
            long diff = ChronoUnit.MILLIS.between(begin, today);
            long diffDay = diff / (24 * 60 * 60 * 1000);
            diff = diff - (diffDay * 24 * 60 * 60 * 1000);
            long diffHours = diff / (60 * 60 * 1000);
            diff = diff - (diffHours * 60 * 60 * 1000);
            long diffMinutes = diff / (60 * 1000);
            diff = diff - (diffMinutes * 60 * 1000);
            long diffSeconds = diff / 1000;
            diff = diff - (diffSeconds * 1000);
           if(diffDay < 0 || diffHours < 0 || diffMinutes < 0){
                diffDay = 0;
                diffHours = 0;
                diffMinutes = 0;
                diffSeconds = 0;
            }
            String beginFormatted = begin.format(format);
            String gap = diffDay +" days " + diffHours + " hours " + diffMinutes +" minutes " + diffSeconds + " seconds"; 
            System.out.println("test formatting: milis:" + diff + " " + diffDay + "days " + diffHours + ":" + diffMinutes + ":" + diffSeconds);
            brin.close();
            return user + " has been following for " + gap + ". Starting on " + beginFormatted + ".";

        } catch (FileNotFoundException e) {
            return "User " + user + "  is not following " + guiHandler.bot.getStore().getConfiguration().joinedChannel;
        } catch (Exception e) {
            LOGGER.severe(e.toString());
            e.printStackTrace();
        }

        return "Unable to connect to Twitch server. Please try again later.";
    }
    
    //TODO implement highlight system and commands to google doc system
    // mod and viewer editions
    
 /*   public void highlight() {

        String uptime = uptime();
        if (!uptime.equals("0")) {
            sendMessage("Highlight marked suggested added at " + uptime);
            try {
                String googleSheetID = this.elements.configNode.getElementsByTagName("googleSheetID").item(0).getTextContent();
                String sheetAPI = "https://sheets.googleapis.com/v4/spreadsheets/" + googleSheetID + "/values/{range}:append";
                URL url = new URL(sheetAPI);
                URLConnection con = (URLConnection) url.openConnection();
                con.setRequestProperty("range", "M6:M20");
                con.setRequestProperty("majorDimension", "COLUMNS");
                BufferedReader sheetIn = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder values = new StringBuilder();
                String valueLines;
                while ((valueLines = sheetIn.readLine()) != null) {
                    values.append(valueLines);
                }
                sheetIn.close();
            } catch (IOException e) {
                LOGGER.severe(e.toString());
            }
        } else {
            sendMessage("Stream is not currently live.");
        }
    }
*/
    
}

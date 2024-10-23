package io.github.dvyadav.momsbrain;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

public class DriveResourceManager {
    
    // feilds for drive service setup
    private static NetHttpTransport httpTransport;
    private static Drive driveService;
    private static final String APPLICATION_NAME = "Mom_bot_Service_Account_Drive_Manager";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    
    private static List<File> mostRecentfoldersList = new ArrayList<>();
    private static List<File> mostRecentfilesList = new ArrayList<>();
    private static final String rootFolderName = "Notes_from_discord_server"; //this is parent of all the files and folders on drive
    
    
    private DriveResourceManager(){}
    
    // authenticate credentails to obtain access of the drive
    private static GoogleCredentials getCredentials() throws IOException, GeneralSecurityException{  
    GoogleCredentials credentials = ServiceAccountCredentials.fromStream(
        new FileInputStream(DriveResourceManager.class.getResource("credentials.json").getPath().replaceAll("%20", " ")))
        .createScoped(SCOPES);
        return credentials;
    }    
    
    // create and return service object
    public static boolean initDriveService(){
        
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            // setting driveService
            driveService = new Drive.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(getCredentials()))
            .setApplicationName(APPLICATION_NAME)
            .build();
        } catch (Exception e) {                    
            e.printStackTrace();
            return false;
        }    
        return true;
    }    
        
    // return all files and folders as one complete list of entities
    private static List<File> getDriveEntityList() throws IOException{
    
        // empty list of folders
        List<File> entityList = new ArrayList<>();
    
        // loop till all pages ends
        String pageToken = null;
        do{
            // get list of all files and folders
            FileList result = driveService.files().list()
            .setPageToken(pageToken)
            .setFields("nextPageToken, files(id, name, mimeType)")
            .execute();
    
            entityList.addAll(result.getFiles());
    
            pageToken = result.getNextPageToken();
        }while(pageToken!=null);
    
        return entityList;
    }
    
    // returns the list of latest FOLDERS from storage
    public static List<File> getLatestFoldersList() throws IOException{
        
        mostRecentfoldersList.clear();
        // filter only folders excluding root folder and updating old folder list
            for (File enity : getDriveEntityList()) {
                if(enity.getMimeType().equals("application/vnd.google-apps.folder") && !(enity.getName().equals(rootFolderName)))
                    mostRecentfoldersList.add(enity);
            }        
        return mostRecentfoldersList;
    }    

    // returns the list of latest FILES from storage
    public static List<File> getLatestFilesList() throws IOException{

        mostRecentfilesList.clear();
            // filter only files and update old file list
            for (File entity : getDriveEntityList()) {
                if(! entity.getMimeType().equals("application/vnd.google-apps.folder"))
                    mostRecentfilesList.add(entity);
            }        
        return mostRecentfilesList;
    }    

    // return presaved folder list instead of using api calls
    public static List<File> getMostRecentFolderList() throws IOException{
        // if null then use api call
        if(mostRecentfoldersList.isEmpty()){
            return getLatestFoldersList();
        }else{
            return mostRecentfoldersList;
        }

    }

    // return presaved files list instead of using api calls
    public static List<File> getMostRecentFileList() throws IOException{
        // if null then use api call
        if(mostRecentfilesList.isEmpty()){
            return getLatestFilesList();
        }else{
            return mostRecentfilesList;
        }

    }

    public static void main(String[] args) {
        DriveResourceManager.initDriveService();
        System.out.println("SUCCESS");
    }

}

package io.github.dvyadav.momsbrain;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

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
    
    private static NetHttpTransport httpTransport;

    // feilds for drive service setup
    private static final String APPLICATION_NAME = "Mom_bot_Service_Account_Drive_Manager";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

    private DriveResourceManager(){}

    // authenticate credentails to obtain access of the drive
    private static GoogleCredentials getCredentials() throws IOException, GeneralSecurityException{  
    GoogleCredentials credentials = ServiceAccountCredentials.fromStream(
        new FileInputStream(DriveResourceManager.class.getResource("credentials.json").getPath().replaceAll("%20", " ")))
        .createScoped(SCOPES);
        return credentials;
    }

    // create and return service object
    public static Drive getDriveService(){
        Drive driveService = null;
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            driveService = new Drive.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(getCredentials()))
                                .setApplicationName(APPLICATION_NAME)
                                .build();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return driveService;
    }

    // returns the list of folders from storage
    public static List<File> getFolders() throws IOException{

        // empty list of folders
        List<File> foldersList = new ArrayList<>();

        // loop till all pages ends
        String pageToken = null;
        do{
            // get list of all files and folders
            FileList result = getDriveService().files().list()
            .setPageToken(pageToken)
            .setFields("nextPageToken, files(id, name, mimeType)")
            .execute();
            // filter only folders
            List<File> files = result.getFiles();
            for (File file : files) {
                if(file.getMimeType().equals("application/vnd.google-apps.folder"))
                    foldersList.add(file);
            }
            pageToken = result.getNextPageToken();
        }while(pageToken != null);

        return foldersList;
    }

    public static void main(String[] args) {
        DriveResourceManager.getDriveService();
        System.out.println("SUCCESS");
    }

}

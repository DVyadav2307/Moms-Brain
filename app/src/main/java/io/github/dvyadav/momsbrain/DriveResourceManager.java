package io.github.dvyadav.momsbrain;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
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
            .setFields("nextPageToken, files(id, name, mimeType, properties, webViewLink, parents)")
            .execute();
    
            entityList.addAll(result.getFiles());
    
            pageToken = result.getNextPageToken();
        }while(pageToken!=null);
    
        return entityList;
    }
    
    // returns the list of latest FOLDERS from storage
    public static List<File> getLatestFoldersList() throws IOException{
        
        List<File> latestFolders = new ArrayList<>();
        // filter only folders excluding root folder and updating old folder list
        for (File enity : getDriveEntityList()) {
            if(enity.getMimeType().equals("application/vnd.google-apps.folder") && !(enity.getName().equals(rootFolderName)))
                latestFolders.add(enity);
        }        
        return latestFolders;
    }    

    // returns the list of latest FILES from storage
    public static List<File> getLatestFilesList() throws IOException{

        List<File> latestFiles = new ArrayList<>();
        // filter only files and update old file list
        for (File entity : getDriveEntityList()) {
            if(! entity.getMimeType().equals("application/vnd.google-apps.folder"))
                latestFiles.add(entity);
        }        
        return latestFiles;
    }    

    // return presaved folder list instead of using api calls
    public static List<File> getMostRecentFolderList() throws IOException{
        // if null then use api call
        if(mostRecentfoldersList.isEmpty() || mostRecentfoldersList == null){
            mostRecentfoldersList = getLatestFoldersList();
        }
        return mostRecentfoldersList;
    }

    // return presaved files list instead of using api calls
    public static List<File> getMostRecentFileList() throws IOException{
        // if null then use api call
        if(mostRecentfilesList.isEmpty() || mostRecentfilesList ==  null){
            mostRecentfilesList = getLatestFilesList();
        }
        return mostRecentfilesList;
    }


    // upload files to drive in a specific folder
    public static void uploadFile(String fileUrl,String fileName, String fileType, String topics, String folderName, String uploaderName)throws IOException{

        // Setting metadata of file
        File fileMetadata  = new File();
        fileMetadata.setName(ZonedDateTime.now(ZoneId.of("GMT"))+"_"+uploaderName+"_"+fileName);//TODO:what should be names of the notes uploaded maybe date+uploader name??

        // filtering desired folder and obatin id()
        List<String> parentsIdList = getMostRecentFolderList().stream()
                                                        .filter(fl->fl.getName().equals(folderName))
                                                        .map(File::getId)//fl->fl.getId()
                                                        .collect(Collectors.toList());
        if(parentsIdList.isEmpty() || parentsIdList == null){
            throw new IOException("This Subject is not available yet.\nPlease select appropriate subject name from the option list.");
        }
        fileMetadata.setParents(parentsIdList);

        // this map is used to make a indexable property of file
        Map<String,String> topicMap = new HashMap<>();
        topicMap.put("topic", topics);
        fileMetadata.setProperties(topicMap);
        fileMetadata.setDescription(topics); //this will help in debugging

        // download inputstream of actual file via http connection
        @SuppressWarnings("deprecation")
        HttpURLConnection con = (HttpURLConnection) (new URL(fileUrl).openConnection());
        con.setRequestMethod("GET");
        con.connect();
        // inject inputstream and upload if connection OK
        if(con.getResponseCode() == HttpURLConnection.HTTP_OK){
            //uploading file
            File uploadedFile = driveService.files().create(fileMetadata, new InputStreamContent(fileType, con.getInputStream()))
                                            .setFields("id, name, mimeType, properties, webViewLink, parents")
                                            .execute();
            // upadating offline list of available notes
            getMostRecentFileList().add(uploadedFile);
        }else{
            // error while laoding file from discord server
            throw new IOException("Problem with uploading file. Please retry.");
        }

    }

    public static List<File> searchAndGetFiles(String folderName, String[] topicsToSearch) throws IOException{

        // collect the id of target subject folder
        String folderId = null;
        for (File folder : getMostRecentFolderList()) {
            if(folder.getName().equals(folderName)){
                folderId = folder.getId();
                break;
            }
        }
        if(folderId == null){
            throw new IOException("This Subject is not available yet.\nPlease select appropriate subject name from the option list.");
        }

        //Map removes redundent files via unique file id as key and helps in searching
        HashMap<String, File> searchResultFilesMap = new HashMap<>();
        for (String topic : topicsToSearch) {
            for (File file : getMostRecentFileList()) {
                if(file.getParents().getFirst().equals(folderId) &&
                    file.getProperties().get("topic").contains(topic.trim())){
                        searchResultFilesMap.put(file.getId(), file);
                    }
            }
        }
        if(searchResultFilesMap.isEmpty()){
            throw new IOException("Notes on given topics not found. Please try with different keyword.");
        }

        //prepare a list of downloadable links
        List<File> downloadLinksList = new ArrayList<>();
        for (String key : searchResultFilesMap.keySet()) {
            downloadLinksList.add(searchResultFilesMap.get(key));
        }
        return downloadLinksList;
    }

    //  create folder in rootFolder
    public static void createFolder(String folderName) throws IOException{
        // setting meta info
        File folderMetaData = new File();
        folderMetaData.setName(folderName)
                    .setMimeType("application/vnd.google-apps.folder")
                    .setParents(getDriveEntityList()
                                .stream()
                                .filter( f -> f.getName().equals(rootFolderName) )
                                .map(f -> f.getId())
                                .collect(Collectors.toList()) );
        // creating folder
        File newFolder = driveService.files().create(folderMetaData)
                            .setFields("id, name, mimeType, properties, webViewLink, parents")
                            .execute();
        // add to folder list
        mostRecentfoldersList.add(newFolder);
    }

    //WARNING: destructive operation! use carefully. //TODO: build command to delet files and folders
    private static void deleteAllFiles(){
        try {
        getMostRecentFileList().forEach(file->{
                try {
                    driveService.files().delete(file.getId()).execute();
                } catch (IOException e) {
                    System.out.println("Couldnt delete file: "+ file.getName()+
                                        "Reason: "+ e.getMessage());
                }
            });
            System.out.println("File Deletion Success");
        } catch (Exception e) {
            System.out.println("Exception: Problem in fetching File List \n"+e.getMessage());
        }
    }

    //WARNING: destructive operation! use carefully.
    private static void deleteAllFolders(){
        try {
        getMostRecentFolderList().forEach(folder->{
                try {
                    driveService.files().delete(folder.getId()).execute();
                } catch (IOException e) {
                    System.out.println("Couldnt delete file: "+ folder.getName()+
                                        "Reason: "+ e.getMessage());
                }
            });
            System.out.println("Folder Deletion Success");
        } catch (Exception e) {
            System.out.println("Exception: Problem in fetching Folder List \n"+e.getMessage());
        }
    }


    public static void main(String[] args){
        DriveResourceManager.initDriveService();
        System.out.println("SUCCESS");
    }

}

package io.github.dvyadav.momsbrain;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.google.api.services.drive.model.File;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;

@SuppressWarnings("null")
public class DiscordEventListener extends ListenerAdapter {

    // list of subject folders from drive (only for class use)
    private List<File> subjFolders;
    
    /* Class to handle profanity moderation tasks */
    ProfanityManager  profanityManager = new ProfanityManager();
    
    @Override
    public void onReady(ReadyEvent event){
       
        // thread to avoid delays on execution of other processes
        Thread.ofVirtual().start(() -> profanityManager.loadProfaneWordset());

        // thread to initliaze google drive service
        Thread.ofVirtual().start(() -> DriveResourceManager.initDriveService());

        // TODO: decide better command deletion technique refer last todo at MomBotSlashCommandManager class
        
        // // deleting global commands when bot reboots
        // event.getJDA().retrieveCommands().queue(cmdlist -> {
        //     cmdlist.forEach(cmd -> {
        //         cmd.delete().queue();
        //     });
        // });
        // // deleting  every guilds commnds when bot reboots
        // event.getJDA().getGuilds().forEach(guild -> {
        //     guild.retrieveCommands().queue(cmdList -> {
        //         cmdList.forEach(cmd -> {
        //             cmd.delete().queue();
        //         });
        //     });
        // });

        // TODO:Decide Global and Guild Commands and implement accordingly

        // Server specific command for "MOM'S BASEMENT"
        event.getJDA().getGuildsByName("Mom's Basement", false).getFirst()
        .updateCommands().addCommands(MomBasementSlashCommandManager.getCommandsAsList()).queue();

        // Ready reminder
        event.getJDA().getGuilds().forEach(guild->{
            guild.getTextChannels()
            .getFirst()
            .sendMessage("I have **Succesfully Booted Up**. Glad to be alive again! :robot::sparkles:")
            .queue();
        });
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event){

        
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        Message chatMsg = event.getMessage();

        /* ignore messeges from bots */
        if(event.getAuthor().isBot()) return;

        /* Handle cuss chats in saperate thread */
        Thread.ofVirtual().start(()-> profanityManager.handleProfanity(chatMsg));

    }

    
    @Override
    public void onMessageUpdate(MessageUpdateEvent event){
        profanityManager.handleProfanity(event.getMessage());
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event){
        //this virtual thread prevents bot from freezing.
        Thread.ofVirtual().start(()->{


            // create new subject folder
            if(event.getName().equals("add_subject")){

                // private replies
                event.deferReply().setEphemeral(true).queue();
                InteractionHook hook  = event.getHook();

                String subjectName = event.getOption("name").getAsString();
                try {
                    // folder creation
                    DriveResourceManager.createFolder(subjectName);

                    // private response
                    hook.sendMessage("Subject created successFully :thumbsup:").queue();

                    // public response
                    event.getGuild()
                        .getTextChannels()
                        .getFirst()
                        .sendMessage(":file_folder: "+subjectName+" unlocked as new subject.\n"
                                     +"now @everyone can access/publish the content")
                        .queue();

                } catch (IOException e) {
                    hook.sendMessage(":warning: Failed to create subject.\nReason: "+e.getMessage()).queue();
                    e.printStackTrace();

                }

                return;
            }


            // get download links of notes
            if(event.getName().equals("pull_notes")){

                event.deferReply().setEphemeral(true).queue();
                InteractionHook hook = event.getHook();

                String subject = event.getOption("subject").getAsString();
                String[] topics = event.getOption("topics").getAsString().toLowerCase().split(",");
                try {
                    // searching and prepareing file list
                    List<File> downloadFiles = DriveResourceManager.searchAndGetFiles(subject, topics);

                    // preparing link response in string
                    String links = "";
                    for(File file : downloadFiles){
                        links += "\n * " + file.getWebViewLink() +" Id: "+file.getId();//id obtained from here will aid in deletion and identification
                    }

                    // private response to user
                    hook.sendMessage(":inbox_tray: Download / View your files from following links:"+links).queue();

                } catch (Exception e) {
                    e.printStackTrace();
                    hook.sendMessage(":warning: Couldn't Fetch Links.\nReason:"+e.getMessage()).queue();
                }
                
                return;
            }


            // upload notes to appropriate folder on google-drive
            if(event.getName().equals("push_notes")){

                // bot reply privately, use hook fro reply to the messege
                event.deferReply().setEphemeral(true).queue();
                InteractionHook hook = event.getHook();
                
                String subject = event.getOption("subject").getAsString();
                Attachment file = event.getOption("attachment").getAsAttachment();
                // store the  topics name in a list  after removal of whitespaces
                String topics = event.getOption("topics").getAsString().toLowerCase().trim();
                try {
                    // uploading the file
                    DriveResourceManager.uploadFile(file.getUrl(), file.getFileName(), file.getContentType(),topics, subject, event.getMember().getEffectiveName());

                    // responses by bot to user
                    hook.sendMessage("Uploaded Successfully!. \nThanks for sharing the resource with others.").queue();
                    event.getGuild()
                        .getTextChannels().getFirst()
                        .sendMessage("New material added in "+subject+" by "+event.getMember().getEffectiveName()+" :star_struck:")
                        .queue();
                    
                }catch (IOException e) {
                    e.printStackTrace();
                    hook.sendMessage(":warning: Couldn't Upload your File.\n"+
                                    "Reason: "+ e.getMessage()).queue();
                }
                return;
            }


            // list the available notes subject to user whne used "update_notes" command
            if(event.getName().equals("show_available_subjects")){

                // private replies by bot only
                event.deferReply().setEphemeral(true).queue();
                InteractionHook hook = event.getHook();

                try {
                    // retrive folder list from drive
                    subjFolders = DriveResourceManager.getMostRecentFolderList();
                    // prepare the names in one string
                    String subjectNames = "";
                    for (File file : subjFolders)
                    subjectNames += "\n * "+file.getName();
                    // send list names
                    hook.sendMessage("Updated Successfully!! \nFollowing subject notes are available:-"+subjectNames).queue();
                } catch (IOException e) {
                    e.printStackTrace();
                    hook.sendMessage("Some error occured! Inform the admin.").setEphemeral(true).queue();
                }
                return;
            }


            //DEMO: time coammnd interaction
            if (event.getName().equals("show_time")) {
                event.deferReply().setEphemeral(true).queue();
                InteractionHook msgHook = event.getHook();
                if(event.getOption("really").getAsBoolean() == true){
                    msgHook.sendMessage("I dont know what time it is! \nBut I am sure its late.").queue();
                }else{
                    msgHook.sendMessage("Fine false means no!").queue();
                }
                return;
            }




        });
    }  

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event){

        // pull & display the names of the subjects from drive when user uses "pull_notes" or "push_notes" command
        if(
            (event.getName().equals("pull_notes") || event.getName().equals("push_notes")) 
            &&  event.getFocusedOption().getName().equals("subject")
        ){
            try {
                
                // fetch subject folders from drive only once in command lifespan
                if(event.getFocusedOption().getValue().equals("")){
                    subjFolders = DriveResourceManager.getLatestFoldersList();
                }
                // filter folders matching with search feild
                // map to new Command.Choice object to create a new folder list
                List<Command.Choice> optionChoice = subjFolders.stream().filter( folder -> folder.getName().toLowerCase().contains(event.getFocusedOption().getValue().toLowerCase()))
                                                                        .map( folder -> new Command.Choice(folder.getName(), folder.getName()))
                                                                        .collect(Collectors.toList());
                // send the new folder list
                event.replyChoices(optionChoice).queue();
            } catch (Exception e) {
                e.printStackTrace();
                event.replyChoice("Couldn't load choice. Please inform admin!","EROR").queue();
            }
        }


        // TODO:learn more on autocomplete commands
    }
}
 
package io.github.dvyadav.momsbrain;

import java.util.List;

import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.entities.Message;
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
        System.out.println("___THE BOT IS READY___");

        /* thread to avoid delays on execution of other processes*/
        Thread.ofVirtual().start(() -> profanityManager.loadProfaneWordset());

        // thread to initliaze google drive service
        Thread.ofVirtual().start(() -> DriveResourceManager.initDriveService());

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

        // list the available notes subject to user whne used "update_notes" command
        if(event.getName().equals("update_notes")){
            // private replies by bot only
            event.deferReply().setEphemeral(true).queue();
            InteractionHook hook = event.getHook();

            try {
                // retrive folder list from drive
                subjFolders = DriveResourceManager.getLatestFoldersList();
                // prepare the names in one string
                String subjectNames = "";
                for (File file : subjFolders)
                subjectNames += "\n * "+file.getName();
                // send list names
                hook.sendMessage("Updated Successfully!! \nFollowing subject notes are avialble:-"+subjectNames).queue();
            } catch (Exception e) {
                e.printStackTrace();
                hook.sendMessage("Some error occured! Inform the admin.").setEphemeral(true).queue();
            }
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
        }

    }  

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event){

        // pull & display the names of the subjects from drive when user uses "notes_pull" command
        if(event.getName().equals("pull_notes") && event.getFocusedOption().getName().equals("subject")){
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
 
package io.github.dvyadav.momsbrain;

import java.util.List;

import com.google.api.services.drive.model.File;

import java.util.ArrayList;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;

@SuppressWarnings("null")
public class DiscordEventListener extends ListenerAdapter {

    
    /* Class to handle profanity moderation tasks */
    ProfanityManager  profanityManager = new ProfanityManager();
    
    @Override
    public void onReady(ReadyEvent event){
        System.out.println("___THE BOT IS READY___");

        /* thread to avoid delays on execution of other processes*/
        Thread.ofVirtual().start(() -> profanityManager.loadProfaneWordset());

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
        // list of subject folders in drive
        List<File> subjectList = new ArrayList<>();


        // update_notes command handling
        // thids method prepares the list of subject folders
        // and prompt bot users about avialable subjects
        if(event.getName().equals("update_notes")){
            // private replies by bot only
            event.deferReply().setEphemeral(true).queue();
            InteractionHook hook = event.getHook();

            try {
                // retrive folder list from drive
                subjectList = DriveResourceManager.getFolders();
                // prepare the names in one string
                String subjectNames = "";
                for (File file : subjectList) {
                    if(!file.getName().equals("Notes_from_discord_server"))//exclude root folder name
                        subjectNames += "\n * "+file.getName();
                }
                // send list names
                hook.sendMessage("Updated Successfully!! \nFollowing subject notes are avialble:-"+subjectNames).queue();
                
            } catch (Exception e) {
                e.printStackTrace();
                hook.sendMessage("Some error occured! Inform the admin");
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
        // TODO:learn more on autocomplete commands
    }
}
 
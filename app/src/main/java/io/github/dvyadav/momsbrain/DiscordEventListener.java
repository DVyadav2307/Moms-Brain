package io.github.dvyadav.momsbrain;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
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
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("null")
public class DiscordEventListener extends ListenerAdapter {

    // list of subject folders from drive (only for class use)
    private List<File> subjFolders;
    
    /* Class to handle profanity moderation tasks */
    ProfanityManager  profanityManager = new ProfanityManager();
    
    @Override
    public void onReady(ReadyEvent event){
       
        /* thread to avoid delays on execution of other processes*/
        Thread.ofVirtual().start(() -> profanityManager.loadProfaneWordSet());

        // thread to initialize Google Drive service
        Thread.ofVirtual().start(DriveResourceManager::initDriveService);

        // deleting global commands when bot reboots
        event.getJDA().retrieveCommands().queue(c->c.forEach(Command::delete));

        // deleting every guild command when bot reboots
        event.getJDA().getGuilds().forEach(guild->{
            guild.retrieveCommands().queue(cmdList->{
                cmdList.forEach(Command::delete);
            });
        });

        // TODO:Decide Global and Guild Commands and implement accordingly

        // Server specific command for "MOM'S BASEMENT"
        event.getJDA().getGuildsByName("Mom's Basement", false).getFirst()
        .updateCommands().addCommands(MomBasementSlashCommandManager.getCommandsAsList()).queue();

        
        System.out.println("BoTs Is ReAdY!!");
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event){

        
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        Message chatMsg = event.getMessage();

        /* ignore messages from bots */
        if(event.getAuthor().isBot()) return;

        /* Handle cuss chats in separate thread */
        Thread.ofVirtual().start(()-> profanityManager.handleProfanity(chatMsg));

    }

    
    @Override
    public void onMessageUpdate(MessageUpdateEvent event){
        profanityManager.handleProfanity(event.getMessage());
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event){
        
        // upload notes to appropriate folder on google-drive
        if(event.getName().equals("push_notes")){


            // bot reply privately, use hook for reply to the message
            event.deferReply().setEphemeral(true).queue();
            InteractionHook hook = event.getHook();
            
            try {
                String subject = Objects.requireNonNull(event.getOption("subject")).getAsString();
                Attachment file = Objects.requireNonNull(event.getOption("attachment")).getAsAttachment();
                // store the topic name in a list after removal of whitespaces
               String topics = Objects.requireNonNull(event.getOption("topics")).getAsString().trim();
                // uploading the file
                DriveResourceManager.uploadFile(file.getUrl(), file.getFileName(), file.getContentType(),topics, subject, Objects.requireNonNull(event.getMember()).getEffectiveName());

                // responses by bot to user
                hook.sendMessage("Uploaded Successfully!. \nThanks for sharing the resource with others.").queue();
                event.getChannel().sendMessage("@everyone New material added in "+subject+" by "+event.getMember().getEffectiveName()+":star_struck:").queue();
                
            }catch (IOException e) {
                e.printStackTrace();
                hook.sendMessage("Couldn't Upload your File. Please Inform Admin");
            }
            return;
        }

        // list the available notes subject to user when used "update_notes" command
        if(event.getName().equals("update_notes")){


            // private replies by bot only
            event.deferReply().setEphemeral(true).queue();
            InteractionHook hook = event.getHook();

            try {
                // retrieve a folder list from drive
                subjFolders = DriveResourceManager.getMostRecentFolderList();/* TODO:monitor behaviour and use getLatestFoldersList() if required */
                // prepare the names in one string
                StringBuilder subjectNames = new StringBuilder();
                for (File file : subjFolders) subjectNames.append("\n * ").append(file.getName());
                // send list names
                hook.sendMessage("Updated Successfully!! \nFollowing subject notes are avialble:-"+subjectNames).queue();
            } catch (IOException e) {
                e.printStackTrace();
                hook.sendMessage("Some error occured! Inform the admin.").setEphemeral(true).queue();
            }
            return;
        }





        //DEMO: time command interaction
        if (event.getName().equals("show_time")) {
            event.deferReply().setEphemeral(true).queue();
            InteractionHook msgHook = event.getHook();
            if(Objects.requireNonNull(event.getOption("really")).getAsBoolean()){
                msgHook.sendMessage("I dont know what time it is! \nBut I am sure its late.").queue();
            }else{
                msgHook.sendMessage("Fine false means no!").queue();
            }
            return;
        }

    }  

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event){

        // pull and display the names of the subjects from drive when a user uses "pull_notes" or "push_notes" command
        if(
            (event.getName().equals("pull_notes") || event.getName().equals("push_notes")) 
            &&  event.getFocusedOption().getName().equals("subject")
        ){
            try {
                
                // fetch subject folders from drive only once in command lifespan
                if(event.getFocusedOption().getValue().isEmpty()){
                    subjFolders = DriveResourceManager.getLatestFoldersList();
                }
                // filter folders matching with search feild
                // map to a new Command.Choice object to create a new folder list
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
 
package io.github.dvyadav.momsbrain;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

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
    
    }  

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event){
        // TODO:learn more on autocomplete commands
    }
}
 
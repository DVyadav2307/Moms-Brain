package io.github.dvyadav.momsbrain;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@SuppressWarnings("null")
public class DiscordEventListener extends ListenerAdapter {
    
    @Override
    public void onReady(ReadyEvent event){
        System.out.println("___THE BOT IS READY___");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        if(event.getAuthor().isBot()) return;

        Message msg = event.getMessage()    ;
        if(msg.getContentRaw().toLowerCase().contains("shit")){
            System.out.println("___ "+event.getAuthor()+"SAID THIS"+msg.getContentRaw()+"___");
            msg.delete().queue((v) ->{
                MessageChannel chnl = event.getChannel();
                chnl.sendMessage(v+" Mind Your Language "+event.getAuthor().getEffectiveName()).queue(/* (w) ->
                {
                    event.getAuthor().
                } */);
            });
        }
    }
}

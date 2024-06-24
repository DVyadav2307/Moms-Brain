package io.github.dvyadav.momsbrain;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
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

        Message msg = event.getMessage();

        if(msg.getContentRaw().equals("!printName")){
            event.getChannel().sendMessage("Namee of Channel is :"+event.getGuild().getTextChannelById(1252188418817921077L).getName()).queue();
        }

        if(msg.getContentRaw().equals("!printID")){
            event.getChannel().sendMessage("Channel "+event.getChannel().getName()+" ID is:"+event.getChannel().getId()).queue();
        }

        if(msg.getContentRaw().toLowerCase().contains("shit")){
            System.out.println("___ "+event.getAuthor()+"SAID THIS"+msg.getContentRaw()+"___");
            msg.delete().queue((v) ->{
                MessageChannel chnl = event.getChannel();
                chnl.sendMessage(" Mind Your Language "+event.getAuthor().getEffectiveName()).queue(/* (w) ->
                {
                    event.getAuthor().
                } */);
            });
        }
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event){

        String newMembeeName = event.getUser().getAsMention();

        System.out.println("___"+newMembeeName+" JOINED THE "+event.getGuild().getName()+"___");

        TextChannel channel = event.getGuild().getTextChannelById(1252188418817921077L);
        channel.sendMessage("Welcome to Mom's Basement "+newMembeeName+",\nPlz Check Pinned Messege for the Notes!!").queue();
        
    }

}

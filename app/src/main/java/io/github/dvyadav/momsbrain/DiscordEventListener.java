package io.github.dvyadav.momsbrain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Member;

@SuppressWarnings("null")
public class DiscordEventListener extends ListenerAdapter {

    /* fetching path of file containing profane words */
    private final URL CUSSWORDSFILE =getClass().getResource("profaneList.txt");

    /* object to read from the profane-word file */
    private BufferedReader reader ;

    /* object to load the profane words from file to the Set */
    private Set<String> cusswordsSet = new HashSet<>();
    
    @Override
    public void onReady(ReadyEvent event){
        System.out.println("___THE BOT IS READY___");

        /* cuss-words file read operation on saperate virtual thread */
        Thread.ofVirtual().start(() -> {

            try {

                /* read and save to the Set */
                reader = new BufferedReader(new FileReader(new File(CUSSWORDSFILE.toURI())));
                String line;
                while ((line = reader.readLine()) != null) {
                    cusswordsSet.add(line.trim());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        });


        // other normal processes
        

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event){

        Message chatMsg = event.getMessage();

        /* ignore messeges from bots */
        if(event.getAuthor().isBot()) return;

        /* Handle cuss chats in saperate thread */
        Thread.ofVirtual().start(()->{

            String[] words = chatMsg
            .getContentStripped()                              /* remove text formatting */                          
            .toLowerCase()                                     /* list is in LowerCase so words should be in LC too */
            .split("\\s+|,\\s*|\\.\\s*|\\?\\s*|!\\s*");  /* spilt sentence to words acc to regex */

            for (String word : words){ /* check for each word in the sentence */
                if(cusswordsSet.contains(word)){/* if the word is a cuss */
                    
                    chatMsg.reply(":warning:Language Warning!!:warning:"+ /* Warn to sender */
                                "\nPlease avoid using inappropriate language.").queue(
                                    /* Report to the Server Owner */
                                    (e)->{event.getJDA().getUserById(event.getGuild().getOwnerIdLong()).openPrivateChannel().queue( /* open owner DM */
                                        (openPrivateChannel)->{openPrivateChannel.sendMessage( /* send Message in a format */
                                                                chatMsg.getAuthor().getEffectiveName()+ // DiscordUser said: bla-bla-bla
                                                                " said: "+ chatMsg.getContentDisplay()+ // on 20-02-2050  13:07:45
                                                                "\non: "+chatMsg.getTimeCreated().      // Please have a look   
                                                                            atZoneSameInstant(ZoneId.of("Asia/Kolkata")).
                                                                            format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))+
                                                                "\n Please have a look."
                                        ).queue(/* TODO: DEVELOP A BETTER SYSTEM TO MANAGE THE ABUSIVE RESPONSES */);
                                        });
                                });
                    break;
                }
            }

        });

        
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event){

        
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event){

    }
}

package io.github.dvyadav.momsbrain;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@SuppressWarnings("null")
public class DiscordEventListener extends ListenerAdapter {

    /* fetching path of file containing profane words */
    private final URL CUSSWORDSFILE =getClass().getResource("profaneList.txt");

    /* object to load the profane words from file to the Set */
    private Set<String> cusswordsSet = new HashSet<>();
    
    @Override
    public void onReady(ReadyEvent event){
        System.out.println("___THE BOT IS READY___");

        /* cuss-words file read operation on saperate virtual thread */
        Thread.ofVirtual().start(() -> {
            
            try (InputStream inputStream = CUSSWORDSFILE.openStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) cusswordsSet.add(line.trim());
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

                    /* store username, msg and time into variable */
                    String senderName = chatMsg.getAuthor().getEffectiveName();
                    String msg = chatMsg.getContentDisplay();
                    String datetime = chatMsg.getTimeCreated().atZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                                             .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

                     /* Warn to sender */
                    chatMsg.reply(":warning:Language Warning!!:warning:"+"\nPlease avoid using inappropriate language.").queue(
                                    /* Report to the Server Owner */
                                    /* open owner DM */
                                    (e)->{event.getJDA().getUserById(event.getGuild().getOwnerIdLong()).openPrivateChannel().queue(
                                        /* send Message in a format */
                                        (openPrivateChannel)->{openPrivateChannel.sendMessage(                      // DiscordUser said: bla-bla-bla
                                            senderName+" said: "+msg+"\non: "+datetime+"\n Please have a look."     // on 20-02-2050  13:07:45
                                        ).queue(/* TODO: DEVELOP A BETTER SYSTEM TO MANAGE THE ABUSIVE RESPONSES */);// Please have a look
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
 
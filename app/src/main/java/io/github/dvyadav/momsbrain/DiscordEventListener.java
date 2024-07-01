package io.github.dvyadav.momsbrain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

@SuppressWarnings("null")
public class DiscordEventListener extends ListenerAdapter {

    /* fetching path of file containing profane words */
    private final URL CUSSWORDSFILE =getClass().getResource("profaneList.txt");

    /* object to read from the profane-word file */
    private BufferedReader reader ;

    /* object to load the profane words from file to the Set */
    private Set<String> cusswords = new HashSet<>();
    
    @Override
    public void onReady(ReadyEvent event){
        System.out.println("___THE BOT IS READY___");

        /* cuss-words file read operation on saperate virtual thread */
        @SuppressWarnings("unused")
        Thread thread = Thread.ofVirtual().start(() -> {

            try {

                /* read and save to the Set */
                reader = new BufferedReader(new FileReader(new File(CUSSWORDSFILE.toURI())));
                String line;
                while ((line = reader.readLine()) != null) {
                    cusswords.add(line.trim());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        });


        // other normal processes
        

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event){

        if(event.getMessage().getContentRaw().equals("!printCuss")){

            event.getChannel().sendMessage("logging .... Check Logs!!").queue();

            for (String string : cusswords) {
                System.out.println(">>> "+ string);
            }
            event.getChannel().sendMessage("Logging done.").queue();
        }
        
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event){

        
    }


}

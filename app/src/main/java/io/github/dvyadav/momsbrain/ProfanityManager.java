package io.github.dvyadav.momsbrain;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Stream;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public class ProfanityManager {

    /* fetching path of file containing profane words */
    private final URL CUSSWORDSFILE =getClass().getResource("profaneList.txt");

    /* Stores profane word list from file */
    private HashSet<String> profaneWordsSet = new HashSet<>();

    /* Stores the user with abuse count */
    private HashMap<Long, Integer> abuseRecords = new HashMap<>();

    /* pupolates the PROFANEWORDSET set from profanelist.txt file */
    public void loadProfaneWordset(){

        try (Stream<String> lines = Files.lines(Path.of(CUSSWORDSFILE.toURI()))) {
            lines.forEach(profaneWordsSet::add);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("null")
    public void handleProfanity(Message chatMsg){

        if(isChatProfane(chatMsg.getContentStripped())){

            /* varibale must be declared after profanity check
             * to ensure the use of memory only at neccesary times. */
            User user = chatMsg.getAuthor();
            /* retrives owner from caches */
            Member serverOwner = chatMsg.getGuild().retrieveOwner().complete();

            String msgTimeDate = chatMsg.getTimeCreated().atZoneSameInstant(ZoneId.of("Asia/Kolkata"))
            .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

            String kickedUserDMReply = "Hey "+user.getName()+","
                                    +"\n:no_entry:You have been removed from the server due to repeated violations of our abuse policy."
                                    +"If you think this was a mistake, please contact "+serverOwner.getAsMention()+" for help."
                                    +"\nThanks for understanding!:pray:";
            String kickedUserReportToOwner = ":no_entry:"+user.getName()+" is Kicked!!"
                                            +"\nAt Time: "+msgTimeDate
                                            +"\nReason: Abuse Policy Violation";

            String[] warnMsg = {":warning: Watch Your Words "+user.getEffectiveName()+"! I am counting.", ":warning: Mind your language! You wont get many chances.", ":warning:What did you just said?"};
            String warnReportToOwner = ":warning:"+user.getName()+" abused: \""+chatMsg.getContentDisplay()+"\""
                                    +"\nOn Channel: " + chatMsg.getChannel().getName()
                                    +"\nAt Time: "+ msgTimeDate
                                    +"\n No. of times: "+(getUserAbuseCount(user.getIdLong())+1);
            

            /* User exceeds the limit of 5 abuses in server 
             * -inform kicking action to abuser
             * -kick user
             * -report action to user. */
            if(getUserAbuseCount(user.getIdLong()) > 4){
                
                user.openPrivateChannel().queue(privateChannel ->{
                    /*  Warn/Inform User in DM */
                    privateChannel.sendMessage(kickedUserDMReply).queue(e ->{
                        e.addReaction(Emoji.fromFormatted("U+1F480")).queue();
                        /* then sequentially Kick user from Server */
                        chatMsg.getGuild().kick(user).queue();
                        clearUserAbuseRecrd(user.getIdLong());
                    });
                });
                
                /* Report Server Owner in DM */
                serverOwner.getUser().openPrivateChannel().queue( privateChannel -> {
                    privateChannel.sendMessage(kickedUserReportToOwner).queue();
                });

                return;
            }

            /* If user under the limit of 5 abuses
             * -Increase abuse count
             * -warn to user in channel
             * -report to owner in DM. */
            increaseUserAbuseCount(user.getIdLong());

            chatMsg.reply(warnMsg[(int)(Math.random() * 3)]).queue(e ->{
                e.addReaction(Emoji.fromFormatted("U+003"+getUserAbuseCount(user.getIdLong())+" U+FE0F U+20E3")).queue();
                serverOwner.getUser().openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessage(warnReportToOwner).queue();
                });
            });

        }
    }

    /* matches each word from chat with profane list and returns accordingly */
    private boolean isChatProfane(String chatMsgString){

        String[] words = chatMsgString                     /* preloaded with removed text formatting (see method call)*/                          
        .toLowerCase()                                     /* list is in LowerCase so words should be in LC too */
        .split("\\s+|,\\s*|\\.\\s*|\\?\\s*|!\\s*");  /* spilt sentence to words acc to regex */

        for (String word : words) {
            if(profaneWordsSet.contains(word)) return true;
        }
        return false;
    }

    /* returns the no of times a user abused */
    private Integer getUserAbuseCount(Long userId){
        return abuseRecords.getOrDefault(userId, 0);
    }

    /* increment of abuse count by one */
    private void increaseUserAbuseCount(Long userId){

        if(abuseRecords.containsKey(userId))
            abuseRecords.replace(userId, abuseRecords.get(userId) + 1);
        else
            abuseRecords.put(userId, 1);
    }

    /* clear the user abuse count */
    private void clearUserAbuseRecrd(Long useId){
        abuseRecords.remove(useId);
    }
}

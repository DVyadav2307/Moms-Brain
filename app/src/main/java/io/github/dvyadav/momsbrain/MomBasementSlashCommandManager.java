package io.github.dvyadav.momsbrain;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

//This Class generates "Mom's Basement" server specific interaction commands
public class MomBasementSlashCommandManager {
    
    // Arraylist to store commands, to be fed to addCommand() Method
    private static List<CommandData> cmdList = new ArrayList<>();


    public static List<CommandData> getCommandsAsList(){
        cmdList.clear();
        prepareCommands();
        return cmdList;
    }

    //TODO: Urgent need to create a delete notes commmand (avaible only to admin role)

    
    private static void prepareCommands(){
        cmdList.add(
            Commands.slash("show_time", "show current time.")
               .addOption(OptionType.BOOLEAN, "really", "enter true or false", true)  
        );
        cmdList.add(
            Commands.slash("show_available_subjects", "Updates the subjects and notes from bot's repo")
        );
        cmdList.add(
            Commands.slash("pull_notes", "Get download links of subject notes.")
                .addOption(
                    OptionType.STRING, "subject", "select/type subject", true, true
                )
                .addOption(OptionType.STRING, "topics", "Use comma as saparator for mutiple topics", true)
        );
        cmdList.add(
            Commands.slash("push_notes", "Upload notes and be a good boy/girl. Max Size: 10 MB")
                .addOption(OptionType.STRING, "subject", "Select the subject of notes. If not in list then select \'Others\' and inform the admin", true, true)
                .addOption(OptionType.ATTACHMENT, "attachment", "Attach the notes", true)
                .addOption(OptionType.STRING, "topics", "Enter the topic names saperated by commas.", true)
        );

        // TODO: add new Commands just above this line. ENSURE TO ADD THEM TO cmdList.
    }
}

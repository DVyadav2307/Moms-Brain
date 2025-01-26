package io.github.dvyadav.momsbrain;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
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

    //connot be added to DM beacuse it has added to guild at init Look onReady()
    private static void prepareCommands(){

        // First demo command for testing purpose --more like and easter
        cmdList.add(
            Commands.slash("show_time", "show current time.")
               .addOption(OptionType.BOOLEAN, "really", "enter true or false", true)
        );

        // reponds with list of available subjects TODO: also show avaible content count
        cmdList.add(
            Commands.slash("show_available_subjects", "Updates the subjects and notes from bot's repo")
        );

        // reposnds with download links
        cmdList.add(
            Commands.slash("pull_notes", "Get download links of subject notes.")
                .addOption(
                    OptionType.STRING, "subject", "select/type subject", true, true
                )
                .addOption(OptionType.STRING, "topics", "Use comma as saparator for mutiple topics", true)
        );

        // upload notes to gdrive
        cmdList.add(
            Commands.slash("push_notes", "Upload notes and be a good boy/girl. Max Size: 10 MB")
                .addOption(OptionType.STRING, "subject", "Select the subject of notes. If not in list then select \'Others\' and inform the admin", true, true)
                .addOption(OptionType.ATTACHMENT, "attachment", "Attach the notes", true)
                .addOption(OptionType.STRING, "topics", "Enter the topic names saperated by commas.", true)
        );

        // create subject folder in drive, allowed only to admins
        cmdList.add(
            Commands.slash("add_subject", "Create a new Subject folder")
                .addOption(OptionType.STRING, "name", "Give subject name in title format", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        );

        

        // TODO: add new Commands just above this line. ENSURE TO ADD THEM TO cmdList.

        // TODO: create a commadn that prints id of all comammands, then allows them to delete from amd itself
    }
}

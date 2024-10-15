package io.github.dvyadav.momsbrain;

import java.util.ArrayList;
import java.util.List;

import org.apache.hc.core5.http.nio.command.CommandSupport;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

/**
 * <p>
 * This class manages slash command avaible globally 
 * with bot. It prepares the {@code List} of 
 * {@code Commands} objectand return the {@code List} 
 * staically as:
 * <pre>
 * {@code
 *  GlobalSlashCommandmanager.getCommandsAsList();
 * }
 * </pre>
 * 
 * @author Divyanshu Yadav
 */
public class GlobalSlashCommandManager {


    
    // Arraylist to store commands, to be fed to addCommand() Method
    private static List<CommandData> cmdList = new ArrayList<>();


    /**
     * <p>This methods return the prepared 
     * {@code List<Commands>} of commands.</p>
     * 
     * <p>Through this method, {@code Commands} 
     * objects are created and aded to List.
     * Then the prepared List is returned.</p>
     * 
     * @return List of Commands
     */
    public static List<CommandData> getCommandsAsList(){
        cmdList.clear();
        prepareCommands();
        return cmdList;
    }


    /**
     * This method creates new commands and adds to List.
     */
    private static void prepareCommands(){
        cmdList.add(
            Commands.slash("show_time", "show current time.")
               .addOption(OptionType.BOOLEAN, "really", "enter true or false", true)  
        );
        cmdList.add(
            Commands.slash("update_notes", "Updates the subjects and notes from bot's repo")
        );
        cmdList.add(
            
        )


        // TODO: add new Commands just above this line. ENSURE TO ADD THEM TO cmdList.
    }
}

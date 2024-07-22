package io.github.dvyadav.momsbrain;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class Main {
    public static void main(String[] args) {

        // New JDA api from JDA builder
        JDA api = JDABuilder.createDefault(System.getenv("MOM_BOT_TOKEN"))
        .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
        .setMemberCachePolicy(MemberCachePolicy.OWNER)
        .addEventListeners(new DiscordEventListener())
        .build();

        // delete unimplemented or old commands when bot reboots
        api.retrieveCommands().complete().forEach(Command::delete);

        // add newly implemented commands when bot reboots
        api.updateCommands()
        .addCommands(GlobalSlashCommandManager.getCommandsAsList())
        .queue();
    }
}

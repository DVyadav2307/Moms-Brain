package io.github.dvyadav.momsbrain;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class Main {
    public static void main(String[] args) {

        // New JDA api from JDA builder
        JDA api = JDABuilder.createDefault(System.getenv("MOM_BOT_TOKEN"))
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
                .build();
        api.addEventListener(new DiscordEventListener());

        
        
    }
}

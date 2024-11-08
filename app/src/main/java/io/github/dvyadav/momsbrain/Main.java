package io.github.dvyadav.momsbrain;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class Main {
    public static void main(String[] args) {

        // New JDA api from JDA builder
        JDABuilder.createDefault(System.getenv("MOM_BOT_TOKEN"))
        .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
        .setMemberCachePolicy(MemberCachePolicy.OWNER)
        .addEventListeners(new DiscordEventListener())
        .build();
    }
}

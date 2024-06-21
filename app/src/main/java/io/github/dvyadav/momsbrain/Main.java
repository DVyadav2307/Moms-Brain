package io.github.dvyadav.momsbrain;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

public class Main {
    public static void main(String[] args) {

        // New JDA api from JDA builder
        JDA api = JDABuilder.createDefault(System.getenv("MOM_BOT_TOKEN")).build();
        api.addEventListener(new DiscordEventListener());
    }
}

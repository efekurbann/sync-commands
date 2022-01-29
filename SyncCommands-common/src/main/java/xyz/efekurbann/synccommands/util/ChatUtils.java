package xyz.efekurbann.synccommands.util;

import net.md_5.bungee.api.ChatColor;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatUtils {

    private final static Pattern HEX_PATTERN = Pattern.compile("#[a-fA-F0-9]{6}");

    public static String color(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of(matcher.group()).toString());
        }
        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    public static List<String> color(List<String> list) {
        return list.stream().map(ChatUtils::color).collect(Collectors.toList());
    }

}

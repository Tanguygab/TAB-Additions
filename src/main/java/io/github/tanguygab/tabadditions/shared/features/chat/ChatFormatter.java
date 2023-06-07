package io.github.tanguygab.tabadditions.shared.features.chat;

import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.shared.config.file.ConfigurationFile;
import me.neznamy.tab.shared.platform.TabPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatFormatter {

    public boolean filterEnabled;
    public String filterChar;
    public int filterFakeLength;
    public String filterOutput;
    public List<Pattern> filterPatterns;
    public List<String> filterExempt;

    public boolean itemEnabled;
    public String itemMainHand;
    public String itemOffHand;
    public String itemOutput;
    public String itemOutputSingle;
    public String itemOutputAir;
    public boolean itemPermission;

    private final Map<String,Map<String,Object>> customInteractions;

    private final boolean embedURLs;
    private final String urlsOutput;
    private final Pattern urlPattern = Pattern.compile("([&§][a-fA-Fk-oK-OrR0-9])?(?<url>(http(s)?:/.)?(www\\.)?[-a-zA-Z0-9@:%._+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_+.~#?&/=]*))");
    private final Pattern ipv4Pattern = Pattern.compile("(?:[0-9]{1,3}\\.){3}[0-9]{1,3}");

    @SuppressWarnings("unchecked")
    public ChatFormatter(ConfigurationFile config) {
        filterEnabled = config.getBoolean("char-filter.enabled",true);
        filterChar = config.getString("char-filter.char-replacement","*");
        filterFakeLength = config.getInt("char-filter.fake-length",0);
        filterOutput = ChatUtils.componentToMM(config.getConfigurationSection("char-filter.output"));
        filterPatterns = config.getStringList("char-filter.filter", List.of()).stream().map(Pattern::compile).collect(Collectors.toList());
        filterExempt = config.getStringList("char-filter.exempt", List.of());

        itemEnabled = config.getBoolean("item.enabled",true);
        itemMainHand = config.getString("item.mainhand","[item]");
        itemOffHand = config.getString("item.offhand","[offhand]");
        itemOutput = config.getString("item.output","%name% x%amount%");
        itemOutputSingle = config.getString("item.output-single","%name%");
        itemOutputAir = config.getString("item.output-air","No Item");
        itemPermission = config.getBoolean("item.permission",false);

        customInteractions = new HashMap<>(config.getConfigurationSection("custom-interactions"));
        Map<String,String> map = new HashMap<>();
        customInteractions.forEach((key,cfg)->map.put(key,ChatUtils.componentToMM((Map<String, Object>) cfg.get("output"))));
        map.forEach((key,output)->{
            Map<String,Object> cfg = new HashMap<>(customInteractions.get(key));
            cfg.put("output",output);
            customInteractions.put(key,cfg);
        });

        embedURLs = config.getBoolean("embed-urls.enabled",true);
        urlsOutput = ChatUtils.componentToMM(config.getConfigurationSection("embed-urls.output"));
    }


    public String process(String message, TabPlayer sender) {
        if (filterEnabled && !sender.hasPermission("tabadditions.chat.bypass.filter")) message = filter(message);
        if (embedURLs) message = embedURLs(message);
        if (itemEnabled && (!itemPermission || sender.hasPermission("tabadditions.chat.item"))) {
            message = formatItems(sender,message,false);
            message = formatItems(sender,message,true);
        }
        if (!customInteractions.isEmpty()) message = formatInteractions(sender,message);

        return message;
    }

    private String filter(String msg) {
        for (Pattern pattern : filterPatterns) {
            Matcher matcher = pattern.matcher(msg);
            Map<Integer, String> map = new HashMap<>();
            for (String bypass : filterExempt) {
                if (!msg.contains(bypass)) continue;
                Matcher m = Pattern.compile(bypass).matcher(msg);
                while (m.find()) {
                    map.put(m.start(), bypass);
                }
            }
            Map<String, Integer> posJumps = new HashMap<>();
            while (matcher.find()) {
                String word = matcher.group();
                StringBuilder wordReplaced = new StringBuilder();
                int i = filterFakeLength < 1 ? word.length() : filterFakeLength;
                wordReplaced.append(filterChar.repeat(i));
                int posJump = posJumps.getOrDefault(word,0);
                String output = filterOutput.replace("%word%",word).replace("%replacement%", wordReplaced.toString());

                if (map.isEmpty()) {
                    msg = msg.replace(word,output);
                } else {
                    for (int pos : map.keySet()) {
                        if (map.get(pos).length() > word.length() && pos <= matcher.start() && pos + map.get(pos).length() > matcher.start())
                            continue;
                        StringBuilder sb = new StringBuilder(msg);
                        sb.replace(matcher.start() + posJump, matcher.end() + posJump, output);
                        msg = sb.toString();

                        posJumps.put(word,posJump+output.length()-word.length());
                    }
                }
            }
        }

        return msg;
    }
    private String formatItems(TabPlayer sender, String message, boolean offhand) {
        String input = offhand ? itemOffHand : itemMainHand;
        if (input.equals("") || !message.contains(input)) return message;

        ChatItem item = TABAdditions.getInstance().getPlatform().getItem(sender, offhand);
        if (item.getType().equals("AIR")) return message.replace(input,itemOutputAir);

        String text = "<hover:show_item:'"+item.getType()+"':"+item.getAmount()+"";
        if (item.getNbt() != null) text+=":'"+item.getNbt().replace("'","\\'")+"'";
        text+=">"+(item.getAmount() == 1 ? itemOutputSingle : itemOutput)
                .replace("%name%",item.getName())
                .replace("%amount%",item.getAmount()+"")
                +"</hover>";
        return message.replace(input,text);
    }
    private String formatInteractions(TabPlayer sender, String message) {
        for (String key : customInteractions.keySet()) {
            Map<String,Object> interaction = customInteractions.get(key);
            if (interaction.containsKey("permission") && (boolean) interaction.get("permission")
                    && !sender.hasPermission("tabadditions.chat.interaction." + key)) continue;
            if (interaction.getOrDefault("input","").equals("")) continue;

            message = message.replace(interaction.get("input")+"",
                    TABAdditions.getInstance().parsePlaceholders(interaction.get("output")+"",sender));
        }
        return message;
    }
    private String embedURLs(String message) {
        String msg2 = message.replaceAll("#[A-Fa-f0-9]{6}"," "); // removing RGB colors to avoid IPV4 check from breaking them
        Matcher urlMatcher = urlPattern.matcher(msg2);
        Matcher ipv4Matcher = ipv4Pattern.matcher(msg2);

        while (urlMatcher.find()) {
            String url = urlMatcher.group("url");
            message = message.replace(url,urlsOutput.replace("%url%",url).replace("%fullurl%","https://"+url));
        }
        while (ipv4Matcher.find()) {
            String ipv4 = ipv4Matcher.group();
            message = message.replace(ipv4,urlsOutput.replace("%url%",ipv4));
        }
        return message;

    }
}
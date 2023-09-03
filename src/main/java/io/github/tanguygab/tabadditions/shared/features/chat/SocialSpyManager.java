package io.github.tanguygab.tabadditions.shared.features.chat;

import me.neznamy.tab.shared.platform.TabPlayer;

public class SocialSpyManager extends ChatManager {

    private final boolean msgSpy;
    private final String msgOutput;
    private final boolean channelSpy;
    private final String channelOutput;
    private final boolean viewConditionSpy;
    private final String viewConditionOutput;

    public SocialSpyManager(Chat chat, boolean msgSpy, String msgOutput, boolean channelSpy, String channelOutput, boolean viewConditionSpy, String viewConditionOutput) {
        super(chat, true, "socialspy", "socialspy", "socialspy");
        setToggleCmdMsgs(translation.socialSpyOff,translation.socialSpyOn);
        invertToggleCmdPlaceholder();
        this.msgSpy = msgSpy;
        this.msgOutput = msgOutput;
        this.channelSpy = channelSpy;
        this.channelOutput = channelOutput;
        this.viewConditionSpy = viewConditionSpy;
        this.viewConditionOutput = viewConditionOutput;
    }

    public String isSpying(TabPlayer sender, TabPlayer viewer, ChatFormat senderFormat) {
        if (!hasCmdToggled(viewer) || !viewer.hasPermission("tabadditions.chat.socialspy")) {
            toggled.remove(viewer.getUniqueId());
            return "";
        }
        if (channelSpy && !senderFormat.getChannel().equals(chat.getFormat(viewer).getChannel())) return "channel";
        if (viewConditionSpy && !senderFormat.isViewConditionMet(sender,viewer)) return "view-condition";
        return "";
    }

    public void process(TabPlayer sender, TabPlayer viewer, String message, String type) {
        String output = type.equals("msg") && msgSpy ? msgOutput
                : type.equals("channel") && channelSpy ? channelOutput.replace("%channel%",chat.getFormat(sender).getChannel())
                : type.equals("view-condition") && viewConditionSpy ? viewConditionOutput.replace("%view-condition%",chat.getFormat(sender).getViewCondition().getName())
                : null;
        if (output == null) return;
        if (!type.equals("msg")) {
            chat.sendMessage(viewer,chat.createMessage(sender,viewer,message,output));
            return;
        }
        toggled.removeIf(uuid ->{
            TabPlayer p = tab.getPlayer(uuid);
            return p != null && p.hasPermission("tabadditions.chat.socialspy");
        });
        toggled.forEach(uuid->{
            TabPlayer spy = tab.getPlayer(uuid);
            if (spy == null || spy == sender) return;
            chat.sendMessage(spy,chat.createMessage(sender,viewer,message,output));
        });
    }

    @Override
    public boolean onCommand(TabPlayer sender, String command) {
        return toggleCmd(sender);
    }
}

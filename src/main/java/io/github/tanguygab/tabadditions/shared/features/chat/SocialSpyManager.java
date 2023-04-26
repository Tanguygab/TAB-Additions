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
        this.msgSpy = msgSpy;
        this.msgOutput = msgOutput;
        this.channelSpy = channelSpy;
        this.channelOutput = channelOutput;
        this.viewConditionSpy = viewConditionSpy;
        this.viewConditionOutput = viewConditionOutput;
    }

    public String isSpying(TabPlayer sender, TabPlayer viewer, ChatFormat senderFormat) {
        if (!hasCmdToggled(viewer)) return "";
        if (channelSpy && !senderFormat.getChannel().equals(chat.getFormat(viewer).getChannel())) return "channel";
        if (viewConditionSpy && !senderFormat.isViewConditionMet(sender,viewer)) return "view-condition";
        return "";
    }

    public void process(TabPlayer sender, TabPlayer viewer, String message, String type) {
        String output = type.equals("msg") && msgSpy ? msgOutput
                : type.equals("channel") && channelSpy ? channelOutput
                : type.equals("view-condition") && viewConditionSpy ? viewConditionOutput
                : null;
        if (output == null) return;
        toggled.forEach(uuid->{
            TabPlayer spy = tab.getPlayer(uuid);
            if (spy == null) return;
            chat.sendMessage(spy,chat.createMessage(sender,viewer,output,message));
        });
    }

    @Override
    public boolean onCommand(TabPlayer sender, String command) {
        return plugin.toggleCmd(true,sender,toggled,translation.socialSpyOff,translation.socialSpyOn);
    }
}

package io.github.tanguygab.tabadditions.shared.features.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ChatItem {

    @Getter private final String type;
    @Getter private final String name;
    @Getter private final int amount;
    @Getter private final String nbt;

}

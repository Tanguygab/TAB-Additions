package io.github.tanguygab.tabadditions.shared.features.chat;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatItem {

    private final String type;
    private final String name;
    private final int amount;
    private final String nbt;

}

package io.github.tanguygab.tabadditions.shared.features;

public enum TAFeature {

    ACTIONBAR("ActionBar&r"),
    TITLE("Title"),
    CHAT("Chat"),
    TA_LAYOUT("TAB+ Layout"),
    RFP("Real Fake Players"),
    SNEAK_HIDE_NAMETAG("Sneak Hide Nametag"),
    NAMETAG_IN_RANGE("Nametag in Range"),
    TABLIST_NAMES_RADIUS("Tablist Names Radius"),
    ONLY_YOU("Only You"),
    UNLIMITED_ITEM_LINES("Unlimited Item Lines");

    private final String displayName;

    TAFeature(String displayName){
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return "&a"+displayName+"&r";
    }
}

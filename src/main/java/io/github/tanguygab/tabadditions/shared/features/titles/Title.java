package io.github.tanguygab.tabadditions.shared.features.titles;

public class Title {

    private final String name;
    private final String title;
    private final String subtitle;

    public Title(String name, String title, String subtitle) {
        this.name = name;
        this.title = title;
        this.subtitle = subtitle;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }
}

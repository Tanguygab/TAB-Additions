package io.github.tanguygab.tabadditions.shared.features.advancedconditions;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import lombok.Getter;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.placeholders.TabPlaceholder;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.PlaceholderManagerImpl;

/**
 * The main condition class. It allows users to configure different
 * condition types that must be met in order to display specified
 * text or make a condition requirement for a visual to be displayed.
 */
public class AdvancedConditions {

    /** All conditions defined in configuration including anonymous conditions */
    private static Map<String, AdvancedConditions> registeredConditions = new HashMap<>();

    /** All supported sub-condition types */
    @Getter private static final Map<String, Function<String, BiFunction<TabPlayer, TabPlayer, Boolean>>> conditionTypes =
            new LinkedHashMap<>() {{
                put(">=", line -> new NumericCondition(line.split(">="), (left, right) -> left >= right)::isMet);
                put(">", line -> new NumericCondition(line.split(">"), (left, right) -> left > right)::isMet);
                put("<=", line -> new NumericCondition(line.split("<="), (left, right) -> left <= right)::isMet);
                put("<-", line -> new StringCondition(line.split("<-"), String::contains)::isMet);
                put("<", line -> new NumericCondition(line.split("<"), (left, right) -> left < right)::isMet);
                put("|-", line -> new StringCondition(line.split("|-"), String::startsWith)::isMet);
                put("-|", line -> new StringCondition(line.split("-|"), String::endsWith)::isMet);
                put("!=", line -> new StringCondition(line.split("!="), (left, right) -> !left.equals(right))::isMet);
                put("=", line -> new StringCondition(line.split("="), String::equals)::isMet);
                put("permission:", line -> (viewer,target) -> viewer == null || viewer.hasPermission(line.split(":")[1]));
            }};

    /** Name of this condition defined in configuration */
    @Getter private final String name;

    /** All defined sub-conditions inside this conditions */
    protected Map<BiFunction<TabPlayer, TabPlayer, Boolean>,String> subConditions = new LinkedHashMap<>();

    /**
     * Refresh interval of placeholder created from this condition.
     * It is calculated based on nested placeholders used in sub-conditions.
     */
    @Getter private int refresh = -1;

    /** List of all placeholders used inside this condition */
    private final List<String> placeholdersInConditions = new ArrayList<>();
    /** Boolean that returns whether the condition has relational placeholders or not */
    private boolean hasRelationalPlaceholders;

    /**
     * Constructs new instance with given parameters and registers
     * this condition to list as well as the placeholder.
     *
     * @param   name
     *          name of condition
     * @param   conditions
     *          map of condition lines and their output
     */
    public AdvancedConditions(String name, Map<String,String> conditions) {
        this.name = name;
        if (conditions == null) {
            TAB.getInstance().getMisconfigurationHelper().conditionHasNoConditions(name);
            return;
        }
        PlaceholderManagerImpl pm = TAB.getInstance().getPlaceholderManager();
        conditions.forEach((line,text)->{
            BiFunction<TabPlayer, TabPlayer, Boolean> condition = compile(line);
            if (condition == null && !line.equals("else")) {
                TAB.getInstance().getMisconfigurationHelper().invalidConditionPattern("[TAB-Additions]-"+name, line);
                return;
            }
            subConditions.put(condition,text);
            if (!line.startsWith("permission:")) placeholdersInConditions.addAll(pm.detectPlaceholders(line));
            else if (refresh > 1000 || refresh == -1) refresh = 1000; //permission refreshing will be done every second
            placeholdersInConditions.addAll(pm.detectPlaceholders(text));
        });
        hasRelationalPlaceholders = placeholdersInConditions.stream().anyMatch(placeholder->placeholder.startsWith("%rel_"));
        registeredConditions.put(name, this);

        if (hasRelationalPlaceholders) pm.registerRelationalPlaceholder("%rel_condition+:"+name+"%", refresh, (viewer,target) -> getText((TabPlayer) viewer,(TabPlayer) target));
        else pm.registerPlayerPlaceholder("%condition+:"+name+"%", refresh, p -> getText((TabPlayer) p,null));
    }

    /**
     * Configures refresh interval and registers nested placeholders
     */
    public void finishSetup() {
        PlaceholderManagerImpl pm = TAB.getInstance().getPlaceholderManager();
        for (String placeholder : placeholdersInConditions) {
            TabPlaceholder pl = pm.getPlaceholder(placeholder);
            pl.addParent((hasRelationalPlaceholders ? "%rel_" : "%") +"condition+:"+name+"%");
            if (pl.getRefresh() < refresh && pl.getRefresh() != -1) refresh = pl.getRefresh();
        }
        pm.addUsedPlaceholders(placeholdersInConditions);
    }

    /**
     * Returns text for player based on if condition is met or not
     *
     * @return  yes or no value depending on if condition passed or not
     */
    public String getText(TabPlayer viewer, TabPlayer target) {
        return subConditions.get(check(viewer,target));
    }

    /**
     * Returns {@code true} if condition is met for player, {@code false} if not
     *
     * @return  {@code true} if met, {@code false} if not
     */
    public boolean isMet(TabPlayer viewer, TabPlayer target) {
        return check(viewer,target) != null;
    }

    /**
     * Returns {@code true} if condition is met for player, {@code false} if not
     *
     * @return  {@code true} if met, {@code false} if not
     */
    public boolean isMet(TabPlayer viewer) {
        return check(viewer,viewer) != null;
    }

    public BiFunction<TabPlayer,TabPlayer,Boolean> check(TabPlayer viewer, TabPlayer target) {
        for (BiFunction<TabPlayer, TabPlayer, Boolean> condition : subConditions.keySet())
            if (condition == null || condition.apply(viewer,target)) return condition;
        return null;
    }

    /**
     * Returns condition from given string. If the string is name of a condition,
     * that condition is returned. If it's a condition pattern, it is compiled and
     * returned. If the string is null, null is returned.
     *
     * @param   string
     *          condition name or pattern
     * @return  condition from string
     */
    public static AdvancedConditions getCondition(String string) {
        if (string == null || string.equals("")) return null;
        if (registeredConditions.containsKey(string))
            return registeredConditions.get(string);

        Map<String,String> conditions = new HashMap<>();
        conditions.put(string,"");
        AdvancedConditions c = new AdvancedConditions("AnonymousCondition["+string+"]", conditions);
        c.finishSetup();
        return c;
    }

    /**
     * Clears registered condition map on plugin reload
     */
    public static void clearConditions() {
        registeredConditions = new HashMap<>();
    }

    /**
     * Marks all placeholders used in the condition as used and registers them.
     * Using a separate method to avoid premature registration of nested conditional placeholders
     * before they are registered properly.
     */
    public static void finishSetups() {
        registeredConditions.values().forEach(AdvancedConditions::finishSetup);
    }

    /**
     * Compiles condition from condition line. This includes detection
     * what kind of condition it is and creating it.
     *
     * @param   line
     *          condition line
     * @return  compiled condition or null if no valid pattern was found
     */
    private static BiFunction<TabPlayer, TabPlayer, Boolean> compile(String line) {
        for (Map.Entry<String, Function<String, BiFunction<TabPlayer, TabPlayer, Boolean>>> entry : AdvancedConditions.getConditionTypes().entrySet())
            if (line.contains(entry.getKey())) return entry.getValue().apply(line);
        return null;
    }
}
package io.github.tanguygab.tabadditions.bungee;


import io.github.tanguygab.tabadditions.shared.Platform;
import io.github.tanguygab.tabadditions.shared.PlatformType;
import io.github.tanguygab.tabadditions.shared.TABAdditions;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.Property;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.features.PlaceholderManager;
import me.neznamy.tab.shared.placeholders.PlayerPlaceholder;
import me.neznamy.tab.shared.placeholders.ServerPlaceholder;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeePlatform extends Platform {

	private final Plugin plugin;
	
	public BungeePlatform(Plugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public PlatformType getType() {
		return PlatformType.BUNGEE;
	}

	@Override
	public Object getSkin(String[] props) {
		String[][] properties = new String[1][3];
		properties[0][0] = "textures";
		properties[0][1] = props[0];
		properties[0][2] = props[1];
		return properties;
	}

	@Override
	public boolean isPluginEnabled(String plugin) {
		return this.plugin.getProxy().getPluginManager().getPlugin(plugin) != null;
	}

	@Override
	public String getVersion() {
		return ProxyServer.getInstance().getPluginManager().getPlugin("TAB-Additions").getDescription().getVersion();
	}

	@Override
	public void registerPlaceholders() {
		PlaceholderManager pm = TAB.getInstance().getPlaceholderManager();
		pm.registerPlaceholder(new PlayerPlaceholder("%money%",1000) {
			@Override
			public String get(TabPlayer p) {
				return new Property(p,"%vault_eco_balance%").updateAndGet();
			}
		});
		pm.registerPlaceholder(new PlayerPlaceholder("%deaths%",1000) {
			@Override
			public String get(TabPlayer p) {
				return new Property(p,"%statistic_deaths%").updateAndGet();
			}
		});
		pm.registerPlaceholder(new PlayerPlaceholder("%health%",100) {
			@Override
			public String get(TabPlayer p) {
				return new Property(p,"%player_health%").updateAndGet();
			}
		});
		pm.registerPlaceholder(new ServerPlaceholder("%tps%",1000) {
			@Override
			public String get() {
				return new Property(null,"%vault_eco_balance%").updateAndGet();
			}
		});
		pm.registerPlaceholder(new PlayerPlaceholder("%afk%",500) {
			@Override
			public String get(TabPlayer p) {
				String afk = new Property(p,"%essentials_afk%").updateAndGet();
				String output;
				if (afk.equals("yes")) output = TAB.getInstance().getConfiguration().config.getString("placeholders.afk-yes"," &4*&4&lAFK&4*&r");
				else output = TAB.getInstance().getConfiguration().config.getString("placeholders.afk-no","");
				return output;
			}
		});
		for (String server : ProxyServer.getInstance().getServers().keySet()) {
			pm.registerPlaceholder(new PlayerPlaceholder("%server-online:" + server + "%",10000) {
				@Override
				public String get(TabPlayer tabPlayer) {
					return ((TABAdditionsBungeeCord)plugin).getServerStatus(server);
				}
			});
		}
	}

	@Override
	public void sendTitle(TabPlayer p, String title, String subtitle, int fadein, int stay, int fadeout) {
		Title t = plugin.getProxy().createTitle()
				.title(new TextComponent(title))
				.subTitle(new TextComponent(subtitle))
				.fadeIn(fadein)
				.stay(stay)
				.fadeOut(fadeout);
		((ProxiedPlayer)p.getPlayer()).sendTitle(t);
	}

	@Override
	public void reload() {
		TABAdditions.getInstance().reload();
		plugin.getProxy().getPluginManager().unregisterListeners(plugin);
		plugin.getProxy().getPluginManager().registerListener(plugin, new BungeeEvents());
	}

	@Override
	public void disable() {
		plugin.getProxy().getPluginManager().unregisterCommands(plugin);
		plugin.getProxy().getPluginManager().unregisterListeners(plugin);

	}
}
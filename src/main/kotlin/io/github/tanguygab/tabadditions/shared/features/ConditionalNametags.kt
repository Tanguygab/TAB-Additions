package io.github.tanguygab.tabadditions.shared.features

import io.github.tanguygab.tabadditions.shared.features.advancedconditions.AdvancedConditions
import me.neznamy.tab.shared.Property
import me.neznamy.tab.shared.TAB
import me.neznamy.tab.shared.features.types.JoinListener
import me.neznamy.tab.shared.features.types.QuitListener
import me.neznamy.tab.shared.features.types.RefreshableFeature
import me.neznamy.tab.shared.features.types.UnLoadable
import me.neznamy.tab.shared.platform.TabPlayer

class ConditionalNametags(private val def: Boolean, private val relational: Boolean)
    : RefreshableFeature(), JoinListener, QuitListener, UnLoadable {
    private val tab = TAB.getInstance()
    private val ntm = tab.nameTagManager!!
    private val properties = mutableMapOf<TabPlayer, Property>()

    override fun getFeatureName() = "Conditional Nametags"
    override fun getRefreshDisplayName() = "&aConditional Nametags&r"

    init {
        for (all in tab.onlinePlayers) onJoin(all)
    }

    override fun onJoin(player: TabPlayer) {
        properties[player] = player.loadPropertyFromConfig(this, "nametag-condition", "")
        refresh(player, true)
    }

    override fun onQuit(player: TabPlayer) {
        properties.remove(player)
    }

    override fun refresh(p: TabPlayer, force: Boolean) {
        if (!p.isLoaded) return
        if (relational) {
            for (all in tab.onlinePlayers) {
                if (p === all || !all.isLoaded) continue
                refresh(p, all)
                refresh(all, p)
            }
            return
        }
        refresh(p, null)
    }

    private fun refresh(target: TabPlayer, viewer: TabPlayer?) {
        if (viewer != null) {
            if (getCondition(target, viewer)) ntm.showNameTag(target, viewer)
            else ntm.hideNameTag(target, viewer)
            return
        }
        if (getCondition(target, target)) ntm.showNameTag(target)
        else ntm.hideNameTag(target)
    }


    fun getCondition(target: TabPlayer, viewer: TabPlayer): Boolean {
        val prop = properties[target] ?: return def
        val cond = prop.currentRawValue
        if (cond.isEmpty()) return def
        return def != AdvancedConditions.getCondition(cond)!!.isMet(viewer, target)
    }

    override fun unload() {
        if (!tab.featureManager.isFeatureEnabled("NameTags")) return
        tab.onlinePlayers.forEach { if (it.isLoaded) ntm.showNameTag(it) }
    }
}

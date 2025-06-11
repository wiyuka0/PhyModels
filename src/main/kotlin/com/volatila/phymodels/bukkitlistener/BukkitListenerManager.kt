package com.volatila.phymodels.bukkitlistener

import com.volatila.phymodels.listener.Listeners
import com.volatila.phymodels.tools.Active
import com.volatila.phymodels.tools.AreaSelect
import com.volatila.phymodels.tools.Crawl
import com.volatila.phymodels.tools.LiquidActive
import com.volatila.phymodels.tools.P2PCreator
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

class BukkitListenerManager {
    companion object {
        fun registerBukkitListeners(plugin: Plugin){
            Bukkit.getPluginManager().registerEvents(ExplosionListener(), plugin)

            Bukkit.getPluginManager().registerEvents(Listeners(), plugin)
            Bukkit.getPluginManager().registerEvents(BlockUpdateListener(), plugin)

            Bukkit.getPluginManager().registerEvents(LiquidActive(), plugin)
            Bukkit.getPluginManager().registerEvents(AreaSelect(), plugin)
            Bukkit.getPluginManager().registerEvents(Active(), plugin)
            Bukkit.getPluginManager().registerEvents(P2PCreator(), plugin)
            Bukkit.getPluginManager().registerEvents(Crawl(), plugin)

            if(plugin.config.getBoolean("enableDroppedItem")){
                Bukkit.getPluginManager().registerEvents(DroppedItemListener(), plugin)
            }
        }
    }
}
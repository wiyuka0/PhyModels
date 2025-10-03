package com.wiyuka.phymodels.bukkitlistener

import com.wiyuka.phymodels.listener.Listeners
import com.wiyuka.phymodels.tools.Active
import com.wiyuka.phymodels.tools.AreaSelect
import com.wiyuka.phymodels.tools.Crawl
import com.wiyuka.phymodels.tools.LiquidActive
import com.wiyuka.phymodels.tools.P2PCreator
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
            Bukkit.getPluginManager().registerEvents(PlayerDeathListener(), plugin)

            if(plugin.config.getBoolean("enableDroppedItem")){
//                Bukkit.getPluginManager().registerEvents(DroppedItemListener(), plugin)
            }
        }
    }
}
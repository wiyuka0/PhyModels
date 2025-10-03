package com.wiyuka.phymodels.bukkitlistener

import com.wiyuka.phymodels.physics.body.BodyManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent

class PlayerDeathListener: Listener{
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.player
        val location = player.location
        BodyManager.createBody(location)
    }
}
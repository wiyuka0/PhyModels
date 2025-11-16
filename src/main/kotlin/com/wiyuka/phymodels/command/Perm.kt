package com.wiyuka.phymodels.command

import org.bukkit.command.CommandSender

object Perm {
    fun hasPerm(sender: CommandSender): Boolean {
        return (sender.hasPermission("phymodels.command"))
    }
}
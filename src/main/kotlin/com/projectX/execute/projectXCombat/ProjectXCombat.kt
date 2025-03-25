package com.projectX.execute.projectXCombat

import com.projectX.execute.cooking.Cooking
import org.bukkit.plugin.java.JavaPlugin

class ProjectXCombat : JavaPlugin() {

    override fun onEnable() {
        // Plugin startup logic
        server.pluginManager.registerEvents(Skill(this), this)
        server.pluginManager.registerEvents(DamagedDisplay(this), this)
        server.pluginManager.registerEvents(Cooking(this), this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}

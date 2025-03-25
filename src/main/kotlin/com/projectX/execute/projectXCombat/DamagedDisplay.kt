package com.projectX.execute.projectXCombat

import net.kyori.adventure.text.Component
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

class DamagedDisplay(private val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun onEntityDamage(event: EntityDamageByEntityEvent) {
        val entity = event.entity

        // 피해를 받은 엔티티가 LivingEntity인지 확인
        if (entity is LivingEntity) {
            val damage = event.finalDamage
            val remainingHealth = (entity.health - damage).coerceAtLeast(0.0)

            // 데미지와 남은 체력을 표시하는 메시지 생성
            val displayName = "데미지: ${"%.1f".format(damage)} | 체력: ${"%.1f".format(remainingHealth)}"

            // 기존 커스텀 네임 저장
            val originalName = entity.customName()

            // 커스텀 네임 설정 및 표시
            val componentName = Component.text(displayName)
            entity.customName(componentName)
            entity.isCustomNameVisible = true

            // 3초 후에 커스텀 네임을 원래대로 복원
            object : BukkitRunnable() {
                override fun run() {
                    entity.customName(originalName)
                    entity.isCustomNameVisible = false
                }
            }.runTaskLater(plugin, 60L) // 60L = 3초 (20틱 = 1초)
        }
    }
}
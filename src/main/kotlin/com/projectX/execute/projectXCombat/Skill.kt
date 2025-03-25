package com.projectX.execute.projectXCombat

import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import kotlin.math.acos

class Skill(private val plugin: JavaPlugin) : Listener {
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand

        if (item.type == Material.DIAMOND_SWORD && event.action.isRightClick) {
            performComboAttack(player)
        }
    }

    /**
     * 플레이어의 시선 방향, 공격 범위, 시야각 내에 있는 적을 감지합니다.
     *
     * @param player 플레이어 객체
     * @param range 공격 범위 (블록 단위)
     * @param fov 시야각 (도 단위)
     * @return 감지된 적 엔티티 목록
     */
    private fun detectEnemiesInSight(player: Player, range: Double, fov: Double): List<LivingEntity> {
        val nearbyEntities = player.getNearbyEntities(range, range, range)
        val playerEyeLocation = player.eyeLocation
        val playerEyeDirection = playerEyeLocation.direction.normalize()
        val fovInRadians = Math.toRadians(fov)

        return nearbyEntities.filterIsInstance<LivingEntity>().filter { entity ->
            val toEntity = entity.location.subtract(playerEyeLocation).toVector().normalize()
            val dotProduct = playerEyeDirection.dot(toEntity)
            val angle = acos(dotProduct)

            angle <= fovInRadians / 2 && playerEyeLocation.distance(entity.location) <= range
        }
    }

    private fun performComboAttack(player: Player) {
        val location = player.location
        val direction = location.direction.normalize()
        val eyeLocation = player.eyeLocation
        val range = 3.0
        val effectLocation = eyeLocation.add(direction.multiply(range))
        val attackPeriod = 5L

        // 공격 애니메이션 실행
        object : BukkitRunnable() {
            var motionCount = 0
            override fun run() {
                if (motionCount < 3) {
                    player.swingMainHand()
                    player.world.spawnParticle(Particle.SWEEP_ATTACK, effectLocation, 10, 0.5, 0.5, 0.5, 0.0)
                    motionCount++
                } else {
                    cancel()
                }
            }
        }.runTaskTimer(plugin, 0L, attackPeriod)

        val targets = detectEnemiesInSight(player, range, 80.0)

        if (targets.isNotEmpty()) {
            object : BukkitRunnable() {
                var hits = 0

                override fun run() {
                    if (hits < 3) {
                        // 감지된 모든 적을 공격
                        for (target in targets) {
                            target.damage(5.0, player) // 5.0의 피해를 가함
                        }
                        hits++
                        player.sendMessage("$hits 연타!")
                    } else {
                        cancel()
                    }
                }
            }.runTaskTimer(plugin, 0L, attackPeriod)
        }
    }
}

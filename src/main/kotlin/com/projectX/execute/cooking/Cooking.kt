package com.projectX.execute.cooking

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

class Cooking(private val plugin: Plugin) : Listener {
    private val inventoryMap: MutableMap<Inventory, String> = mutableMapOf()
    private val cookingPlayers: MutableSet<Player> = mutableSetOf()

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            val clickBlock = event.clickedBlock
            // TODO: DIAMOND_BLOCK -> 추후에 요리 아이템으로 변경
            if (clickBlock?.type == Material.DIAMOND_BLOCK) {
                val player = event.player
                if (cookingPlayers.contains(player)) {
                    player.sendMessage("이미 요리를 진행 중입니다.")
                } else {
                    openCookingInventory(player)
                }
            }
        }
    }

    private fun openCookingInventory(player: Player) {
        val title = Component.text("요리하기")
        val inventory = Bukkit.createInventory(null, 54, title)

        // TODO: Material.GRAY_DYE -> 버튼 이미지 변환
        val buttonItem = ItemStack(Material.GRAY_DYE)
        val meta = buttonItem.itemMeta
        meta?.let {
            it.displayName(Component.text("요리하기", NamedTextColor.WHITE))
            buttonItem.itemMeta = it
        }

        inventory.setItem(52, buttonItem)
        inventory.setItem(53, buttonItem)

        player.openInventory(inventory)
        inventoryMap[inventory] = "요리하기"
    }

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val inventory = event.inventory
        val title = inventoryMap[inventory]
        if (title == "요리하기") {
            val clickedSlot = event.rawSlot
            val player = event.whoClicked as Player

            if (clickedSlot == 52 || clickedSlot == 53) {
                event.isCancelled = true
                if (cookingPlayers.contains(player)) {
                    player.sendMessage("이미 요리를 진행 중입니다.")
                } else {
                    val result = processCooking(inventory, player)
                    if (result != null) {
                        cookingPlayers.add(player)
                        val cookingTime = 10 // 요리 시간(초)
                        val cookingProgress = CookingProgress(player, cookingTime, result, cookingPlayers)
                        cookingProgress.runTaskTimer(plugin, 0L, 20L)
                        player.closeInventory()
                    } else {
                        player.sendMessage("레시피가 올바르지 않습니다.")
                    }
                }
            }
        }
    }

    private fun processCooking(inventory: Inventory, player: Player): ItemStack? {
        val requiredItems = mapOf(
            Material.APPLE to 2,
            Material.SUGAR to 1,
        )

        val ingredientCount = countIngredients(inventory)

        return if (hasRequiredIngredients(ingredientCount, requiredItems)) {
            consumeIngredients(inventory, requiredItems)    // 재료 소모
            returnRemainingIngredients(inventory, player)   // 남은 재료 반환
            createCustomItem()  // 결과물 생성
        } else {
            null
        }
    }

    private fun countIngredients(inventory: Inventory): MutableMap<Material, Int> {
        val ingredientCount = mutableMapOf<Material, Int>()
        for (i in 0 until 52) {
            val item = inventory.getItem(i)
            if (item != null) {
                val material = item.type
                val count = item.amount
                ingredientCount[material] = ingredientCount.getOrDefault(material, 0) + count
            }
        }
        return ingredientCount
    }

    private fun hasRequiredIngredients(
        ingredientCount: Map<Material, Int>,
        requiredItems: Map<Material, Int>
    ): Boolean {
        return requiredItems.all { (material, count) ->
            ingredientCount.getOrDefault(material, 0) >= count
        }
    }

    private fun consumeIngredients(inventory: Inventory, requiredItems: Map<Material, Int>) {
        for ((material, requiredCount) in requiredItems) {
            var remaining = requiredCount
            for (i in 0 until 52) {
                val item = inventory.getItem(i)
                if (item != null && item.type == material) {
                    if (item.amount > remaining) {
                        item.amount -= remaining
                        remaining = 0
                    } else {
                        remaining -= item.amount
                        inventory.clear(i)
                    }
                    if (remaining == 0) break
                }
            }
        }
    }

    private fun returnRemainingIngredients(inventory: Inventory, player: Player) {
        for (i in 0 until 52) { // 요리하기 버튼을 제외한 슬롯
            val item = inventory.getItem(i)
            if (item != null) {
                // 플레이어의 인벤토리에 아이템 추가 시 남는 아이템 확인
                val remainingItems = player.inventory.addItem(item)
                if (remainingItems.isNotEmpty()) {
                    // 인벤토리에 공간이 부족하여 남은 아이템이 있을 경우, 해당 아이템을 드롭
                    for (remainingItem in remainingItems.values) {
                        player.world.dropItemNaturally(player.location, remainingItem)
                    }
                }
                inventory.clear(i) // 인벤토리 슬롯 비우기
            }
        }
    }

    private fun createCustomItem(): ItemStack {
        val item = ItemStack(Material.GOLDEN_APPLE)
        val meta = item.itemMeta
        meta?.let {
            // TODO: recipe에 따라 결과물 생성
            val displayName = Component.text("특제 요리", NamedTextColor.GOLD)
            it.displayName(displayName)
            item.itemMeta = it
        }
        return item
    }
}
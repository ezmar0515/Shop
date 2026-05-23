package Shop.Shop.gui

import Shop.Shop.data.ShopConfig
import Shop.Shop.data.ShopDataManager
import Shop.Shop.data.ShopItem
import Shop.Shop.economy.CurrencyManager
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ShopGUI(
    private val dataManager: ShopDataManager,
    private val npcID: String,
    private val currencyManager: CurrencyManager,
) {
    fun open(player: Player) {
        val config = dataManager.loadShop(npcID) ?: run {
            player.sendMessage("§c설정된 상점이 없습니다!")
            return
        }

        val gui = ChestGui(3, "§8[ 상점 : ${config.shopTitle} ]")
        val pane = StaticPane(0, 0, 9, 3)

        // 저장된 아이템들을 불러와서 진열
        config.items.forEach { shopItem ->
            val itemStack = ItemStack(Material.valueOf(shopItem.material))
            val meta = itemStack.itemMeta
            meta?.setDisplayName("§f${shopItem.material}")
            meta?.lore = listOf(
                "§6가격: ${shopItem.price}골",
                "§7클릭하여 구매"
            )
            itemStack.itemMeta = meta

            pane.addItem(GuiItem(itemStack) { event ->
                event.isCancelled = true

                val player = event.whoClicked as Player

                if (event.isLeftClick) {
                    // 구매 로직
                    handlePurchase(player, config, shopItem)
                } else if (event.isRightClick) {
                    // 판매 로직
                    handleSell(player, shopItem, config)
                }
            }, shopItem.slot % 9, shopItem.slot / 9)
        }

        gui.addPane(pane)
        gui.show(player)
    }
    fun handlePurchase(player: Player, config: ShopConfig, shopItem: ShopItem) {
        // 1. 재고 확인 (관리자 상점이 아닐때만)
        if(!config.isAdminShop && shopItem.stock <= 0) {
            player.sendMessage("§c§l[!] §f이 아이템은 현재 품절되었습니다!")
            return
        }

        // 2. 플레이어 돈 확인
        val playerMoney = currencyManager.countPlayerMoney(player)
        if (playerMoney < shopItem.price) {
            player.sendMessage("§c§l[!] §f돈이 부족합니다! §7(필요: ${shopItem.price}골)")
            return
        }

        // 3. 돈 차감
        if (currencyManager.deductMoney(player, shopItem.price)) {
            // 4. 아이템 지급
            val itemToGive = ItemStack(Material.valueOf(shopItem.material))
            player.inventory.addItem(itemToGive)

            // 5. 재고 차감 (관리자 상점이 아닐 때만)
            if (!config.isAdminShop) {
                shopItem.stock -= 1

                // 데이터 저장 (이 npcID에 해당하는 데이터를 다시 저장해줘야 함)
                dataManager.saveShop(npcID, config)
            }

            player.sendMessage("§a§l[!] §f구매 완료! §7(남은 재고: ${if (config.isAdminShop) "무제한" else "${shopItem.stock}개"})")

            // GUI 새로고침 (재고 숫자를 실시간으로 보여주기 위해)
            open(player)
        }
    }
    fun handleSell(player: Player, shopItem: ShopItem, config: ShopConfig) {
        val material = Material.valueOf(shopItem.material)

        // 1. 플레이어 인벤토리에 아이템이 있는지 확인
        if (!player.inventory.contains(material)) {
            player.sendMessage("§c§l[!] §f판매할 아이템(${shopItem.material})이 없습니다.")
            return
        }

        // 2. [추가된 로직] 개인 상점인데 상점에 잔액이 부족한 경우 체크
        val sellPrice = ((shopItem.price * shopItem.sellRate) / 100).toInt() // 판매가

        if (!config.isAdminShop && config.storedMoney < sellPrice) {
            player.sendMessage("§c§l[!] §f상점에 잔액이 부족하여 아이템을 팔 수 없습니다.")
            player.playSound(player.location, org.bukkit.Sound.ENTITY_VILLAGER_NO, 1f, 1f)
            return
        }

        // 3. 실제 판매 로직 진행
        val itemInInv = player.inventory.contents.find { it?.type == material }
        if (itemInInv != null) {
            // 아이템 차감
            itemInInv.amount -= 1

            // 유저에게 돈 지급
            currencyManager.giveMoney(player, sellPrice)

            // 4. [추가된 로직] 개인 상점일 경우 상점 데이터 업데이트
            if (!config.isAdminShop) {
                config.storedMoney -= sellPrice // 상점 금고에서 돈 차감
                shopItem.stock += 1            // 팔린 아이템은 상점 재고로 추가
            }

            player.sendMessage("§e§l[!] §f판매 완료! §6${sellPrice}골§f을 받았습니다.")

            // 변경된 데이터 저장 (npcID가 필요하므로 호출 시점에서 저장하거나 여기서 처리)
            dataManager.saveShop(npcID, config)
        }
    }
}
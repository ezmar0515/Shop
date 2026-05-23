package Shop.Shop.gui

import Shop.Shop.data.ShopDataManager
import Shop.Shop.data.ShopConfig
import Shop.Shop.data.ShopItem
import Shop.Shop.economy.CurrencyManager
import Shop.Shop.ShopAdminManager.ShopEditState
import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui
import com.github.stefvanschie.inventoryframework.pane.StaticPane
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class ShopAdminGUI(
    private val dataManager: ShopDataManager,
    private val npcID: String,
    private val currencyManager: CurrencyManager
) {
    fun open(player: Player, currentConfig: ShopConfig? = null) {
        // 1. 데이터 결정. 기존 상점 데이터 불러오기(없으면 새로 생성)
        val config = currentConfig
            ?: dataManager.loadShop(npcID)
            ?: ShopConfig(shopTitle = "새로운 상점", isAdminShop = true, ownerUUID = null)

        val gui = ChestGui(6, "§c[ 상점 설정: $npcID ]")
        val pane = StaticPane(0, 0, 9, 6)

        // 2. 현재 저장된 아이템들 진열
        config.items.forEach { shopItem ->
            val itemStack = ItemStack(Material.valueOf(shopItem.material))

            // 아이템 메타 설정
            val meta = itemStack.itemMeta
            if (meta != null) {
                meta.setDisplayName("§e${shopItem.material}")
                meta.lore = listOf(
                    "§7--------------------",
                    "§f현재 가격: §6${shopItem.price}골",
                    "§f현재 재고: §b${if (config.isAdminShop) "무제한" else "${shopItem.stock}개"}",
                    "§7--------------------",
                    "§e[좌클릭] §f가격을 수정합니다.",
                    "§c[우클릭] §f이 아이템을 제거합니다."
                )
                itemStack.itemMeta = meta
            }

            // 아이템 클릭(좌클릭 & 우클릭)
            pane.addItem(GuiItem(itemStack) { event ->
                event.isCancelled = true

                if (event.isShiftClick) {
                    val hand = player.inventory.itemInMainHand
                    if (hand.type.name == shopItem.material) {
                        shopItem.stock += hand.amount
                        player.inventory.setItemInMainHand(null)
                        player.sendMessage("§a§l[!] §f재고가 ${hand.amount}개 추가되었습니다.")
                        open(player, config)
                    } else {
                        player.sendMessage("§c§l[!] §f손에 같은 종류의 아이템을 들어주세요.")
                    }
                } else if (event.isLeftClick) {
                    ShopItemEditor(player, config, shopItem, this, dataManager, currencyManager).open()
                } else if (event.isRightClick) {
                    config.items.remove(shopItem)
                    player.sendMessage("§c아이템이 제거되었습니다.")
                    open(player, config)
                }
            }, shopItem.slot % 9, shopItem.slot / 9)
        }

        // 3. [상점 모드 전환 버튼] - (0, 5) 위치
        val modeItem = ItemStack(if (config.isAdminShop) Material.COMMAND_BLOCK else Material.PLAYER_HEAD)
        modeItem.itemMeta = modeItem.itemMeta?.apply {
            setDisplayName(if (config.isAdminShop) "§b[ 모드: 관리자 상점 ]" else "§e[ 모드: 개인 상점 ]")
            lore = listOf("§7클릭하여 전환", "§7현재 재고: ${if (config.isAdminShop) "무한" else "개인설정"}")
        }
        pane.addItem(GuiItem(modeItem) {
            config.isAdminShop = !config.isAdminShop
            if (!config.isAdminShop) config.items.forEach { it.stock = 0 }
            player.sendMessage("§a상점 타입이 변경되었습니다.")
            open(player, config)
        }, 0, 5)

        // 4. [상점 주인 설정] - (2, 5)
        val ownerItem = ItemStack(Material.NAME_TAG)
        ownerItem.itemMeta = ownerItem.itemMeta?.apply {
            setDisplayName("§d[ 주인 설정 ]")
            lore = listOf("§f현재 주인: §7${config.ownerUUID ?: "없음"}", "", "§e[클릭] §f채팅으로 플레이어 이름 입력")
        }
        pane.addItem(GuiItem(ownerItem) {
            player.closeInventory()
            ShopEditState.pendingEdits[player.uniqueId] = npcID to ShopEditState.EditType.OWNER
            player.sendMessage("§d§l[!] §f주인으로 지정할 플레이어의 이름을 채팅에 입력하세요.")
        }, 2, 5)

        // 5. [아이템 등록 버튼] - (4, 5)
        val addItemButton = ItemStack(Material.HOPPER)
        addItemButton.itemMeta = addItemButton.itemMeta?.apply { setDisplayName("§6[ 새 아이템 등록 ]") }
        pane.addItem(GuiItem(addItemButton) { event ->
            event.isCancelled = true

            val hand = player.inventory.itemInMainHand
            if (hand.type != Material.AIR) {
                config.items.add(ShopItem(hand.type.name, 0, 10, 0, config.items.size))
                open(player, config)
            }
        }, 4, 5)

        // 6. [상점 외형 및 이름 설정] - (7, 5)
        val skinItem = ItemStack(Material.PLAYER_HEAD)
        skinItem.itemMeta = skinItem.itemMeta?.apply {
            setDisplayName("§d[ NPC 스킨 설정 ]")
            lore = listOf("§e[클릭] §f채팅으로 정품 닉네임 입력")
        }
        pane.addItem(GuiItem(skinItem) {
            player.closeInventory()
            ShopEditState.pendingEdits[player.uniqueId] = npcID to ShopEditState.EditType.SKIN
            player.sendMessage("§d§l[!] §fNPC에게 씌울 정품 유저 닉네임을 채팅에 입력하세요.")
        }, 7, 5)

        // 7. [타이틀 설정] (6, 5)
        val settingsItem = ItemStack(Material.PAINTING)
        settingsItem.itemMeta = settingsItem.itemMeta?.apply {
            setDisplayName("§d[ 상점 이름 설정 ]")
            lore = listOf("§e[클릭] §f채팅으로 상점 이름 입력")
        }
        pane.addItem(GuiItem(settingsItem) {
            player.closeInventory()
            ShopEditState.pendingEdits[player.uniqueId] = npcID to ShopEditState.EditType.TITLE
            player.sendMessage("§b§l[!] §f변경할 상점 타이틀을 채팅에 입력하세요. (컬러코드 & 지원)")
        }, 6, 5)

        // 8. [설정 저장 버튼] - (8, 5)
        val saveButton = ItemStack(Material.NETHER_STAR)
        val sMeta = saveButton.itemMeta
        sMeta?.setDisplayName("§a§l[ 설정 저장 ]")
        saveButton.itemMeta = sMeta

        pane.addItem(GuiItem(saveButton){
            dataManager.saveShop(npcID, config)
            player.sendMessage("§a상점 설정이 JSON으로 저장되었습니다!")
            player.closeInventory()
            it.isCancelled = true
        }, 8, 5)

        // 9. [수익금 확인] - (1, 5)
        val moneyItem = ItemStack(Material.GOLD_INGOT)
        moneyItem.itemMeta = moneyItem.itemMeta?.apply {
            setDisplayName("§6[ 수익금 정산 ]")
            lore = listOf(
                "§f현재 쌓인 수익금: §e${config.storedMoney}골",
                "",
                "§e[클릭] §f수익금을 모두 수령합니다."
            )
        }

        pane.addItem(GuiItem(moneyItem) {
            if (config.storedMoney <= 0) {
                player.sendMessage("§c[!] 수령할 수익금이 없습니다.")
            } else {
                val amount = config.storedMoney
                config.storedMoney = 0 // 초기화
                currencyManager.giveMoney(player, amount) // 돈 지급

                player.sendMessage("§a[!] 수익금 §e${amount}골§f을 수령했습니다!")
                dataManager.saveShop(npcID, config)
                open(player, config)
            }
        }, 1, 5)

        gui.addPane(pane)
        gui.show(player)
    }
}
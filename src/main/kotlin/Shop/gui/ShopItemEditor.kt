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

class ShopItemEditor(
    private val player: Player,
    private val config: ShopConfig,
    private val item: ShopItem,
    private val parentGui: ShopAdminGUI, // 뒤로 가기를 위해 부모 GUI를 받음
    private val dataManager: ShopDataManager,
    private val currencyManager: CurrencyManager
) {
    fun open() {
        val gui = ChestGui(3, "§8[ 아이템 편집: ${item.material} ]")
        val pane = StaticPane(0, 0, 9, 3)

        // 1. 현재 상태 표시 (가운데 상단)
        val infoItem = ItemStack(Material.valueOf(item.material))
        val infoMeta = infoItem.itemMeta
        infoMeta?.setDisplayName("§e현재 설정")
        infoMeta?.lore = listOf("§f가격: §6${item.price}골", "§f재고: §b${item.stock}개")
        infoItem.itemMeta = infoMeta
        pane.addItem(GuiItem(infoItem) { it.isCancelled = true }, 4, 0)

        // 2. 가격 조절 버튼 배치
        // [ + ] 버튼들 (오른쪽)
        pane.addItem(createValueButton(Material.GOLD_BLOCK, "§a+100골", 100), 7, 1)
        pane.addItem(createValueButton(Material.GOLD_INGOT, "§a+10골", 10), 6, 1)
        pane.addItem(createValueButton(Material.GOLD_NUGGET, "§a+1골", 1), 5, 1) // 4,1은 가운데

        // [ - ] 버튼들 (왼쪽)
        pane.addItem(createValueButton(Material.IRON_BLOCK, "§c-100골", -100), 1, 1)
        pane.addItem(createValueButton(Material.IRON_INGOT, "§c-10골", -10), 2, 1)
        pane.addItem(createValueButton(Material.IRON_NUGGET, "§c-1골", -1), 3, 1)

        // 3. 저장 및 뒤로가기 버튼
        val confirm = ItemStack(Material.LIME_STAINED_GLASS_PANE)
        val cMeta = confirm.itemMeta
        cMeta?.setDisplayName("§a§l[ 저장 및 뒤로가기 ]")
        cMeta?.lore = listOf("§7클릭 시 현재 설정을 파일에 저장하고", "§7목록으로 돌아갑니다.")
        confirm.itemMeta = cMeta

        pane.addItem(GuiItem(confirm) {
            it.isCancelled = true
            parentGui.open(player, config) // 부모 창(목록)으로 돌아가면서 새로고침
        }, 8, 2)

        gui.addPane(pane)
        gui.show(player)
    }

    // delta 값을 받아 가격을 증감시키는 공용 함수
    private fun createValueButton(material: Material, name: String, delta: Int): GuiItem {
        val stack = ItemStack(material)
        val meta = stack.itemMeta
        meta?.setDisplayName(name)
        stack.itemMeta = meta

        return GuiItem(stack) {
            item.price = (item.price + delta).coerceAtLeast(0) // 0골 미만 방지
            open() // 수치 갱신을 위해 현재 창 다시 열기
            it.isCancelled = true
        }
    }
}
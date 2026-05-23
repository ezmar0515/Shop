package Shop.Shop.listener

import Shop.Shop.data.ShopDataManager
import Shop.Shop.economy.CurrencyManager
import Shop.Shop.gui.ShopAdminGUI
import Shop.Shop.gui.ShopGUI
import de.oliver.fancynpcs.api.events.NpcInteractEvent
import de.oliver.fancynpcs.api.actions.ActionTrigger
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class NPCListener(
    private val dataManager: ShopDataManager,
    private val currencyManager: CurrencyManager
    ): Listener {

    @EventHandler
    fun onNPCClick(event: NpcInteractEvent) {
        val player = event.player
        val npc = event.npc
        val npcID = npc.data.id // FancyNpcs에서 등록된 ID

        // 1. 관리자가 SHIFT + 우클릭 시 설정창 오픈
        if (player.isOp &&
            player.isSneaking &&
            event.interactionType == ActionTrigger.RIGHT_CLICK) {
            // 값의 순서가 헷갈리지 않게 이름을 직접 지정
            ShopAdminGUI(
                dataManager = dataManager,
                npcID = npcID,
                currencyManager = currencyManager
            ).open(player)
            return
        }

        // 2. 일반 우클릭 시 구매 상점 오픈
        if (event.interactionType == ActionTrigger.RIGHT_CLICK) {
            player.sendMessage("§a상점을 여는 중... (ID: $npcID)")
            // ShopGUI에 npcID를 전달하여 해당 상점 데이터를 불러오게 함
            ShopGUI(dataManager = dataManager, npcID = npcID, currencyManager = currencyManager).open(player)
        }
    }
// 미사용 코드 필요시 삭제
//    fun setShopOwner(npcID: String, targetPlayer: Player) {
//        val config = dataManager.loadShop(npcID) ?: return
//        config.ownerUUID = targetPlayer.uniqueId.toString()
//        dataManager.saveShop(npcID, config)
//    }
}
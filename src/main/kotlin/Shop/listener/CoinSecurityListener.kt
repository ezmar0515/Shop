package Shop.Shop.listener

import Shop.Shop.economy.CurrencyManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent

class CoinSecurityListener(private val currencyManager: CurrencyManager) : Listener {

    @EventHandler
    fun onPrepareCraft(event: PrepareItemCraftEvent) {
        // 1. 제작대에 올라온 모든 아이템을 확인
        val matrix = event.inventory.matrix

        for (item in matrix) {
            // 2. 만약 아이템 중 하나라도 코인인지 확인
            if (currencyManager.isCurrency(item)) {

                // 3. 결과물 칸을 null(공기)로 만들어 제작 차단
                event.inventory.result = null
                return
            }
        }
    }

}
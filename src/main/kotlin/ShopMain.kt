package Shop

import Shop.Shop.listener.NPCListener
import Shop.Shop.economy.CoinType
import org.bukkit.entity.Player

import Shop.Shop.economy.CurrencyManager
import Shop.Shop.data.ShopDataManager
import Shop.Shop.listener.CoinSecurityListener
import Shop.Shop.listener.ShopEditListener
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class ShopMain : JavaPlugin() {
    private lateinit var currencyManager: CurrencyManager
    private lateinit var shopDataManager: ShopDataManager

    override fun onEnable() {
        // 매니저 초기화
        currencyManager = CurrencyManager(this)
        shopDataManager = ShopDataManager(this)

        // 리스너
        server.pluginManager.registerEvents(CoinSecurityListener(currencyManager), this)
        server.pluginManager.registerEvents(NPCListener(shopDataManager, currencyManager), this)
        server.pluginManager.registerEvents(ShopEditListener(shopDataManager), this)

        logger.info("상점 플러그인이 활성화되었습니다!")
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        // "코인지급" 명령어 처리
        if (label == "코인지급") {
            // 명령어를 보낸 대상이 플레이어인지 확인
            if (sender !is Player) {
                sender.sendMessage("§c명령어는 플레이어만 사용할 수 있습니다.")
                return true
            }

            val player = sender // 이제 player 변수를 사용할 수 있습니다.
            val coin = currencyManager.createCoin(CoinType.GOLD)
            player.inventory.addItem(coin)
            player.sendMessage("§6테스트용 금전을 지급했습니다.")
            return true
        }

        return false // 다른 명령어가 들어오면 false 반환
    }
}


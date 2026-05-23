package Shop.Shop.listener

import Shop.ShopMain
import Shop.Shop.ShopAdminManager.ShopEditState
import Shop.Shop.data.ShopDataManager
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.plugin.java.JavaPlugin

class ShopEditListener(private val dataManager: ShopDataManager) : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun onChat(event: AsyncPlayerChatEvent) {
        val player = event.player
        val state = ShopEditState.pendingEdits[player.uniqueId] ?: return

        // 입력 대기 상태라면 채팅 취소
        event.isCancelled = true
        val (npcID, editType) = state
        val input = event.message

        // 상태 제거(한 번 입력하면 끝)
        ShopEditState.pendingEdits.remove(player.uniqueId)

        // 메인 스레드에서 데이터 처리 (BUKKIT API 접근을 위해)
        Bukkit.getScheduler().runTask(JavaPlugin.getPlugin(ShopMain::class.java), Runnable {
            val config = dataManager.loadShop(npcID) ?: return@Runnable

            when (editType) {
                ShopEditState.EditType.TITLE -> {
                    val formattedTitle = input.replace("&", "§")
                    config.shopTitle = formattedTitle

                    // /npc displayname <id> <new_name>
                    player.performCommand("npc displayname $npcID $input")

                    player.sendMessage("§a§l[!] §f상점 타이틀이 '${formattedTitle}§f'로 변경되었습니다.")
                }
                ShopEditState.EditType.OWNER -> {
                    val target = Bukkit.getOfflinePlayer(input)
                    config.ownerUUID = target.uniqueId.toString()
                    player.sendMessage("§d§l[!] §f주인이 ${target.name}님으로 변경되었습니다.")
                }
                ShopEditState.EditType.SKIN -> {
                    // FancyNpcs 명령어 실행: npc skin <id> <name>
                    player.performCommand("npc skin $npcID $input")
                    player.sendMessage("§a§l[!] §fNPC 스킨이 '${input}' 님의 스킨으로 변경되었습니다.")
                }
            }
            dataManager.saveShop(npcID, config)
        })
    }
}
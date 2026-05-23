package Shop.Shop.economy

import Shop.Shop.economy.CoinType
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

class CurrencyManager(private val plugin: JavaPlugin) {
    // 1. 태그를 위한 고유 키 설정
    private val coinKey = NamespacedKey(plugin, "is_currency")
    private val typeKey = NamespacedKey(plugin, "coin_type")

    // 2. 코인 생성 함수
    fun createCoin(type: CoinType): ItemStack {
        // 기반 아이템 금조각
        val item = ItemStack(Material.GOLD_NUGGET)
        val meta = item.itemMeta ?: return item

        // 이름 설정
        meta.setDisplayName(type.displayName)
        meta.lore = listOf(
            type.description,
            "§6가치: ${type.value}골"
        )

        // 리소스팩 이미지 연결
        meta.setCustomModelData(type.customModelData)

        // PDC 태그 심기
        meta.persistentDataContainer.set(coinKey, PersistentDataType.BOOLEAN, true)
        meta.persistentDataContainer.set(typeKey, PersistentDataType.STRING, type.name)

        item.itemMeta = meta
        return item
    }

    // 3. 아이템으로부터 CoinType을 읽어오는 함수
    fun getCoinType(item: ItemStack?): CoinType? {
        if (item == null || !item.hasItemMeta()) return null

        val meta = item.itemMeta ?: return null
        val typeName = meta.persistentDataContainer.get(typeKey, PersistentDataType.STRING) ?: return null

        return try {
            CoinType.valueOf(typeName) // 저장된 이름으로 Enum을 찾음
        } catch (e: Exception) {
            null
        }
    }

    fun isCurrency(item: ItemStack?): Boolean {
        if (item == null || !item.hasItemMeta()) return false

        // coinKey는 클래스 상단에 선언된 NamespacedKey여야 함
        return item.itemMeta!!.persistentDataContainer.has(coinKey, PersistentDataType.BOOLEAN)
    }

    fun countPlayerMoney(player: Player):Int {
        var total = 0
        player.inventory.contents.filterNotNull().forEach { item ->
            val type = getCoinType(item)
            if (type != null) {
                total += type.value * item.amount
            }
        }
        return total
    }

    fun deductMoney(player: Player, amount: Int): Boolean {
        // 1. 총액이 부족하면 바로 거절
        if (countPlayerMoney(player) < amount) return false

        var remaining = amount
        val inv = player.inventory

        // 실제로 지울 슬롯과 수량을 미리 기록해두는 지도
        val itemsToRemove = mutableMapOf<Int, Int>() // 슬롯 번호 to 제거 수량
        val sortedTypes = CoinType.entries.sortedByDescending { it.value }

        // 2. 가상으로 계산해보기
        for (type in sortedTypes) {
            if (remaining <= 0) break

            for (i in 0 until inv.size) {
                val item = inv.getItem(i) ?: continue
                if (getCoinType(item) == type) {
                    var countInSlot = item.amount
                    var canTake = 0

                    // 이 슬롯에서 몇 개까지 뺄 수 있는지 계산
                    while (countInSlot > 0 && remaining >= type.value) {
                        countInSlot--
                        canTake++
                        remaining -= type.value
                    }

                    if (canTake > 0) {
                        itemsToRemove[i] = canTake
                    }
                }
                if (remaining <= 0) break
            }
        }

        // 3. 최종 확인: 잔돈이 딱 맞아서 remainging이 0이 되었을때만 실제로 아이템 제거
        if (remaining == 0) {
            itemsToRemove.forEach { (slot, amount) ->
                val item = inv.getItem(slot)
                if (item != null) {
                    if (item.amount <= amount) {
                        inv.setItem(slot, null)
                    } else {
                        item.amount -= amount
                    }
                }
            }
            return true
        } else {
            // 잔액이 남았다는 건 잔돈이 안 맞는다는 뜻 (은전이 없어서 결제 불가 등)
            player.sendMessage("§c§l[!] §f잔돈이 부족합니다! 환전소에서 동전을 은전으로 바꿔오세요.")
            return false // 결제 실패 (아이템은 아무것도 건드리지 않음)
        }
    }

    fun giveMoney(player: Player, amount: Int) {
        var remaining = amount
        val goldVal = CoinType.GOLD.value
        val silverVal = CoinType.SILVER.value
        val bronzeVal = CoinType.BRONZE.value

        // 1. 금전 지급
        if (remaining >= goldVal) {
            val count = remaining / goldVal
            player.inventory.addItem(createCoin(CoinType.GOLD).apply { this.amount = count})
            remaining %= goldVal
        }

        // 2. 은전 지급
        if (remaining >= silverVal) {
            val count = remaining / silverVal
            player.inventory.addItem(createCoin(CoinType.SILVER).apply { this.amount = count})
            remaining %= silverVal
        }

        // 3. 동전 지급
        if (remaining >= bronzeVal) {
            val count = remaining / bronzeVal
            player.inventory.addItem(createCoin(CoinType.BRONZE).apply { this.amount = count})
            remaining %= bronzeVal
        }
    }
}
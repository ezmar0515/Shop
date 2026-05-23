package Shop.Shop.data

// 전체 상점 정보
data class ShopConfig(
    var shopTitle: String,
    var isAdminShop: Boolean,   // true면 무한 재고, false면 플레이어 상점
    val items: MutableList<ShopItem> = mutableListOf(),
    var storedMoney: Int = 0,    // 플레이어 상점일 때 쌓인 수익금
    var ownerUUID: String? = null // 상점 주인의 UUID(null이면 관리자 전용)
)
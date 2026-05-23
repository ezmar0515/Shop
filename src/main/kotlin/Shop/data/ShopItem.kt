package Shop.Shop.data

// 상점에 진열된 아이템 정보
data class ShopItem(
    val material: String,       // 아이템 종류
    val customModelData: Int,   // 이미지 번호
    var price: Int,             // 가격(동전 단위)
    var stock: Int,             // 재고
    var slot: Int,              // GUI에서의 위치
    var sellRate: Int = 100      // 판매가의 비율 측정 (기본 100%)
)
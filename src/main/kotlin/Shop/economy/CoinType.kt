package Shop.Shop.economy

// 코인의 종류를 정의하는 Enum
enum class CoinType(
    val displayName: String,
    val value: Int,
    val customModelData: Int,
    val description: String
) {
    BRONZE("§c동전", 1, 1001, "동전이다. 상점에서 사용할 수 있을 것 같다."),
    SILVER("§f은전", 10, 1002, "은전이다. 상점에서 나쁘지 않게 사용할 수 있을 것 같다."),
    GOLD("§6금전", 100, 1003, "금전이다. 상점에서 유용하게 사용할 수 있을 것 같다.")
}
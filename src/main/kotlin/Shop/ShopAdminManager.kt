package Shop.Shop

import java.util.UUID

class ShopAdminManager {
    object ShopEditState {
        enum class EditType { TITLE, OWNER, SKIN }

        // 플레이어 UUID -> 수정 중인 npcID, 어떤 종류인지
        val pendingEdits = mutableMapOf<UUID, Pair<String, EditType>>()
    }
}
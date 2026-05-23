package Shop.Shop.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class ShopDataManager(private val plugin: JavaPlugin) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    // prettyPrinting: 사람이 읽기 좋게 줄바꿈을 해주는 설정
    private val folder = File(plugin.dataFolder, "shops")

    init {
        if (!folder.exists()) folder.mkdirs() // 상점 저장 폴더 생성
        // mkdirs(): 경로 생성. 경로상에 없는 모든 폴더를 알아서 다 만듦
    }

    // 상점 정보를 JSON 파일로 저장
    fun saveShop(id: String, config: ShopConfig) {
        val file = File(folder, "$id.json")
        file.writeText(gson.toJson(config))
    }

    // JSON 파일을 읽어서 상점 정보로 변환
    fun loadShop(id: String): ShopConfig? {
        val file = File(folder, "$id.json")
        if (!file.exists()) return null
        return gson.fromJson(file.readText(), ShopConfig::class.java)
    }
}
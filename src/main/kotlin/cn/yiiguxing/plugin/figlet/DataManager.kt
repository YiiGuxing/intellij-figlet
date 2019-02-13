package cn.yiiguxing.plugin.figlet

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "DataManager", storages = [(Storage("yiiguxing.figlet.xml"))])
class DataManager : PersistentStateComponent<DataManager.State> {

    val state: State = State(FIGlet.DEFAULT_FONT, FIGlet.FEATURED_FONTS)
        @JvmName("state") get

    override fun getState(): State = state

    override fun loadState(state: State) {
        this.state.apply {
            lastUsedFont = state.lastUsedFont
            commonFonts = state.commonFonts
        }
    }

    companion object {
        val instance: DataManager
            get() = ServiceManager.getService(DataManager::class.java)
    }

    data class State(var lastUsedFont: String, var commonFonts: List<String>)
}
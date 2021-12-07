package cn.yiiguxing.plugin.figlet

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.annotations.CollectionBean

@State(name = "DataManager", storages = [Storage("yiiguxing.figlet.xml")])
class DataManager : PersistentStateComponent<DataManager.State> {

    val state: State = State()
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
            get() = ApplicationManager.getApplication().getService(DataManager::class.java)
    }

    data class State(var lastUsedFont: String, @CollectionBean var commonFonts: List<String>) {
        constructor() : this(FIGlet.DEFAULT_FONT, ArrayList(FIGlet.FEATURED_FONTS)/* For deserialization */)
    }
}
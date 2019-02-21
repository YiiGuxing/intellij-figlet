package cn.yiiguxing.plugin.figlet

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(name = "FigletSettings", storages = [Storage("yiiguxing.figlet.xml")])
class Settings : PersistentStateComponent<Settings> {

    var trimOutput: Boolean = true

    var previewFontSize: Int = 12
        get() = maxOf(field, 1)
        set(value) {
            field = maxOf(value, 1)
        }

    override fun getState(): Settings = this

    override fun loadState(state: Settings) {
        trimOutput = state.trimOutput
        previewFontSize = state.previewFontSize
    }

    companion object {
        val instance: Settings
            get() = ServiceManager.getService(Settings::class.java)
    }
}
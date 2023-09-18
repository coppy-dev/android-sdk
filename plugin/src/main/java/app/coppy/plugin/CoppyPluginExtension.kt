package app.coppy.plugin
abstract class CoppyPluginExtension() {
    abstract var contentKey: String?
    var updateType: String? = null
    var updateInterval: Int? = null
}
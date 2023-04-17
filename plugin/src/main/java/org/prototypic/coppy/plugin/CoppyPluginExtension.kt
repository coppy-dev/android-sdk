package org.prototypic.coppy.plugin
abstract class CoppyPluginExtension() {
    abstract var spaceKey: String?
    var updateType: String? = null
    var updateInterval: Int? = null
}
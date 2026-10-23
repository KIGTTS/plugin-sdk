package com.kigtts.plugin.api

/** Broken plugins must not make an unbounded UI tree or collide in the host's action namespace. */
internal fun validateSnapshot(snapshot: PluginSnapshot) {
    fun symbol(name: String?) { require(name == null || name.matches(Regex("[a-z0-9_]{1,80}"))) { "图标名称无效" } }
    val contributions = snapshot.contributions
    require(contributions.actions.size <= 500 && contributions.drawerEntries.size <= 64 && contributions.gestures.size <= 32)
    require(contributions.automationActions.size <= 64 && contributions.automationActions.distinct().size == contributions.automationActions.size)
    contributions.automationActions.forEach { require(it.length in 1..128) }
    require(contributions.settingsEntries.size <= 64 && contributions.settingsEntries.map { it.id }.distinct().size == contributions.settingsEntries.size)
    contributions.settingsEntries.forEach { require(it.id.length in 1..128 && it.pageId.length in 1..128 && it.label.length in 1..160 && it.summary.length <= 1000 && it.cardTitle.length <= 160 && it.cardId.matches(Regex("[a-z][a-z0-9.-]{0,63}"))) }
    require(contributions.managedPackages.size <= 256 && contributions.managedPackages.map { it.id }.distinct().size == contributions.managedPackages.size)
    contributions.managedPackages.forEach {
        require(it.id.matches(Regex("[a-z][a-z0-9.-]{1,127}")) && it.version.length in 1..64 && it.kind.length in 1..32)
        require(it.name.length in 1..160 && it.error.length <= 1000 && it.permissions.size <= 32)
        require(it.permissionPurposes.size <= 32 && it.permissionPurposes.all { (key, value) -> key in it.permissions && value.length in 1..600 })
        listOfNotNull(it.manageAction, it.enableAction, it.removeAction).forEach { action -> require(action.length in 1..128) }
    }
    contributions.settingsEntries.forEach { symbol(it.symbol) }
    contributions.sourceAction?.let { require(it in contributions.automationActions) }
    require((contributions.actions.map { it.id } + contributions.gestures.map { it.id }).let { it.distinct().size == it.size })
    contributions.actions.forEach {
        symbol(it.symbol)
        require(it.id.length in 1..128 && it.label.length in 1..160)
        require((it.slot == UiSlot.EDITOR_MENU) == (it.presentation == UiPresentation.MENU_ITEM))
    }
    require(snapshot.pages.size <= 64 && snapshot.pages.map { it.id }.distinct().size == snapshot.pages.size)
    var count = 0
    fun blocks(items: List<PageBlock>, depth: Int) {
        require(depth <= 8)
        for (block in items) {
            require(++count <= 1000)
            when (block) {
                is PageBlock.Slider -> {
                    val c = block.control
                    require(c.title.length in 1..160 && c.actionId.length in 1..128)
                    require(c.min.isFinite() && c.max.isFinite() && c.min < c.max && c.value.isFinite() && c.value in c.min..c.max && c.steps in 0..1000)
                }
                is PageBlock.Browser -> {
                    require((block.root != null) != (block.url != null))
                    require(block.mainPage.length in 1..240 && block.mainPage.split('/').none { it == ".." || it == "." || it.isBlank() })
                    block.url?.let { require(it.length <= 4000 && java.net.URI(it).let { u -> u.scheme == "https" && u.host != null && u.userInfo == null }) }
                    require(block.allowedActions.size <= 32 && block.allowedActions.all { it.length in 1..128 })
                }
                is PageBlock.Text -> require(block.text.length <= 16_000)
                is PageBlock.Markdown -> {
                    require(block.text.length <= 32_000 && block.images.size <= 128)
                    block.images.forEach { (key, resource) -> require(key.length in 1..240 && resource.path.length in 1..512) }
                }
                is PageBlock.Gallery -> require(block.images.size <= 8 && block.images.all { it.description.length <= 160 && it.resource.path.length in 1..512 })
                is PageBlock.Notice -> require(block.text.length <= 16_000)
                is PageBlock.SourceEditor -> require(block.id.length in 1..128 && block.text.length <= 64_000 && block.saveAction.length in 1..128 && block.language.length <= 32)
                is PageBlock.Files -> {
                    require(block.items.size <= 1000 && block.items.map { it.path }.distinct().size == block.items.size)
                    require(block.title.length <= 160 && block.directory.length <= 240)
                    block.items.forEach { require(it.path.length in 1..240 && it.name.length in 1..240 && it.bytes >= 0) }
                    listOfNotNull(block.openAction, block.operationAction, block.importAction, block.createAction, block.parentAction).forEach { require(it.length in 1..128) }
                }
                is PageBlock.Toggle -> { symbol(block.symbol); require(block.title.length <= 160 && block.summary.length <= 1000 && block.actionId.length in 1..128) }
                is PageBlock.Input -> { symbol(block.symbol); require(block.title.length <= 160 && block.value.length <= 64_000 && block.actionId.length in 1..128) }
                is PageBlock.Select -> {
                    symbol(block.symbol); block.options.forEach { symbol(it.symbol) }
                    require(block.title.length <= 160 && block.options.size in 1..100)
                    require(block.options.map { it.id }.distinct().size == block.options.size)
                    block.options.forEach { require(it.id.length in 1..128 && it.label.length in 1..300) }
                }
                is PageBlock.Choice -> {
                    symbol(block.symbol); block.options.forEach { symbol(it.symbol) }
                    require(block.title.length <= 160 && block.options.size in 1..12)
                    require(block.options.map { it.id }.distinct().size == block.options.size)
                    block.options.forEach { require(it.id.length in 1..128 && it.label.length in 1..160) }
                }
                is PageBlock.Chat -> {
                    require(block.draftImages.size <= 4 && block.queued.size <= 20)
                    block.queued.forEach { require(it.id.length in 1..128 && it.text.length <= 1000 && it.removeAction.length in 1..128 && it.frontAction.length in 1..128 && (it.steerAction == null || it.steerAction.length in 1..128) && (it.reorderAction == null || it.reorderAction.length in 1..128)) }
                    require(block.messages.size <= 100 && block.messages.map { it.id }.distinct().size == block.messages.size)
                    require(block.sendAction.length in 1..128 && block.stopAction.length in 1..128)
                    require(block.controls.size <= 4 && block.conversationId.length <= 128)
                    block.controls.forEach { control -> require(control.title.length <= 160 && control.options.size in 1..256); control.options.forEach { require(it.action.length in 1..128 && it.label.length <= 160 && it.value.length <= 512 && it.section.length <= 160) } }
                    block.approval?.let { require(it.title.length <= 160 && it.detail.length <= 4000 && it.approveAction.length in 1..128 && it.rejectAction.length in 1..128) }
                    block.messages.forEach { require(it.images.size <= 4 && it.images.all { image -> image.description.length <= 300 && image.source.path.length <= 512 }); require(it.id.length in 1..128 && it.text.length <= 64_000 && it.reasoning.length <= 64_000 && it.summary.length <= 2000 && it.detail.length <= 16_000 && it.toolName.length <= 160) }
                }
                is PageBlock.Group -> { require(block.title.length <= 160); blocks(block.children, depth + 1) }
                is PageBlock.Actions -> {
                    block.actions.forEach { symbol(it.symbol) }
                    require(block.actions.size <= 100)
                    block.actions.forEach { require(it.id.length in 1..128 && it.label.length in 1..160) }
                }
            }
        }
    }
    snapshot.pages.forEach { page ->
        require(page.id.length in 1..128 && page.title.length in 1..160)
        page.onEnterAction?.let { require(it.length in 1..128) }
        page.refreshAction?.let { require(it.length in 1..128) }
        page.headerAction?.let { require(page.header != null) }
        page.header?.let {
            symbol(it.symbol)
            require(it.id.length in 1..128 && it.title.length in 1..160 && it.subtitle.length <= 1000)
            require(it.primaryAction == null && it.actions.isEmpty() && it.toggle == null)
            it.image?.let { image -> require(image.path.length in 1..512) }
        }
        page.management?.items?.forEach { symbol(it.symbol) }
        blocks(page.blocks, 0)
    }
}

package com.twojstar.llmbench.share

internal object CreateMessageAppAction {
    const val ACTION = "com.twojstar.llmbench.action.CREATE_MESSAGE"
    const val EXTRA_TEXT = "text"

    fun payload(action: String?, text: CharSequence?): IncomingSharePayload? {
        if (action != ACTION) return null
        return normalizeIncomingSharePayload(
            text = text?.toString(),
            uriStrings = emptyList(),
        )
    }
}

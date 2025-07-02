package com.alibaba.langengine.mcp.spec;

public enum MethodDefined implements Method {
    Initialize("initialize"),
    Ping("ping"),
    ResourcesList("resources/list"),
    ResourcesTemplatesList("resources/templates/list"),
    ResourcesRead("resources/read"),
    ResourcesSubscribe("resources/subscribe"),
    ResourcesUnsubscribe("resources/unsubscribe"),
    PromptsList("prompts/list"),
    PromptsGet("prompts/get"),
    NotificationsCancelled("notifications/cancelled"),
    NotificationsInitialized("notifications/initialized"),
    NotificationsProgress("notifications/progress"),
    NotificationsMessage("notifications/message"),
    NotificationsResourcesUpdated("notifications/resources/updated"),
    NotificationsResourcesListChanged("notifications/resources/list_changed"),
    NotificationsToolsListChanged("notifications/tools/list_changed"),
    NotificationsRootsListChanged("notifications/roots/list_changed"),
    NotificationsPromptsListChanged("notifications/prompts/list_changed"),
    ToolsList("tools/list"),
    ToolsCall("tools/call"),
    LoggingSetLevel("logging/setLevel"),
    SamplingCreateMessage("sampling/createMessage"),
    CompletionComplete("completion/complete"),
    RootsList("roots/list");

    private final String value;

    MethodDefined(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return this.value;
    }
}

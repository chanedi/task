package com.lufax.task.toolwindow;

import com.intellij.openapi.util.Comparing;
import com.intellij.tasks.TaskRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TaskUpdateConfig {

    private String name;
    private String detailUrl;
    private Map<String, ActionUrl> completeUrls = new HashMap();

    private Map<String, ActionUrl> cancelUrls = new HashMap();

    public TaskUpdateConfig() {
        completeUrls.put(StatusActionUrlMapping.DEFAULT_STATUS, new ActionUrl());
        cancelUrls.put(StatusActionUrlMapping.DEFAULT_STATUS, new ActionUrl());
    }

    public TaskUpdateConfig(TaskRepository taskRepository) {
        this();
        this.name = taskRepository.getPresentableName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetailUrl() {
        return detailUrl;
    }

    public void setDetailUrl(String detailUrl) {
        this.detailUrl = detailUrl;
    }

    public Map<String, ActionUrl> getCompleteUrls() {
        return completeUrls;
    }

    public void setCompleteUrls(Map<String, ActionUrl> completeUrls) {
        this.completeUrls = completeUrls;
    }

    public Map<String, ActionUrl> getCancelUrls() {
        return cancelUrls;
    }

    public void setCancelUrls(Map<String, ActionUrl> cancelUrls) {
        this.cancelUrls = cancelUrls;
    }

    public ActionUrl getCompleteUrl(String status) {
        ActionUrl actionUrl = completeUrls.get(status);
        if (actionUrl != null) {
            return actionUrl;
        }
        return completeUrls.get(StatusActionUrlMapping.DEFAULT_STATUS);
    }

    public ActionUrl getCancelUrl(String status) {
        ActionUrl actionUrl = cancelUrls.get(status);
        if (actionUrl != null) {
            return actionUrl;
        }
        return cancelUrls.get(StatusActionUrlMapping.DEFAULT_STATUS);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TaskUpdateConfig)) return false;
        TaskUpdateConfig that = (TaskUpdateConfig) o;
        if (!Objects.equals(getName(), that.getName())) return false;
        if (!Objects.equals(getDetailUrl(), that.getDetailUrl())) return false;
        if (!Comparing.equal(getCompleteUrls(), that.getCompleteUrls())) return false;
        if (!Comparing.equal(getCancelUrls(), that.getCancelUrls())) return false;
        return true;
    }

}

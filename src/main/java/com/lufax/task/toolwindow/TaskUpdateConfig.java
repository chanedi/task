package com.lufax.task.toolwindow;

import com.intellij.tasks.TaskRepository;
import com.intellij.tasks.generic.TemplateVariable;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.net.HTTPMethod;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TaskUpdateConfig {

    private String name;
    private String detailUrl;
    private String completeUrl;
    private HTTPMethod completeMethod;
    private List<TemplateVariable> templateVariables = new ArrayList<>();
    private String cancelUrl;
    private HTTPMethod cancelMethod;

    public TaskUpdateConfig() {
    }

    public TaskUpdateConfig(TaskRepository taskRepository) {
        this.name = taskRepository.getPresentableName();
        this.completeMethod = HTTPMethod.GET;
        this.cancelMethod = HTTPMethod.GET;
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

    public String getCompleteUrl() {
        return completeUrl;
    }

    public void setCompleteUrl(String completeUrl) {
        this.completeUrl = completeUrl;
    }

    public HTTPMethod getCompleteMethod() {
        return completeMethod;
    }

    public void setCompleteMethod(HTTPMethod completeMethod) {
        this.completeMethod = completeMethod;
    }

    public List<TemplateVariable> getTemplateVariables() {
        return templateVariables;
    }

    public List<TemplateVariable> getAllTemplateVariables() {
        return getTemplateVariables();
    }

    public void setTemplateVariables(List<TemplateVariable> templateVariables) {
        this.templateVariables = templateVariables;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }

    public HTTPMethod getCancelMethod() {
        return cancelMethod;
    }

    public void setCancelMethod(HTTPMethod cancelMethod) {
        this.cancelMethod = cancelMethod;
    }

}

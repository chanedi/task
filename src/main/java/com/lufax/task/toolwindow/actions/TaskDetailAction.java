package com.lufax.task.toolwindow.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.lufax.task.toolwindow.ActionUrl;
import com.lufax.task.toolwindow.TaskUpdateConfig;
import com.lufax.task.toolwindow.TaskUpdateConfigsState;
import org.jetbrains.annotations.NotNull;

public class TaskDetailAction extends TaskItemAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        TaskUpdateConfigsState configsState = TaskUpdateConfigsState.getInstance(getEventProject(e));
        TaskUpdateConfig updateConfig = configsState.getUpdateConfig();
        doActionUrl(e, null, null, new ActionUrl(updateConfig.getDetailUrl(), ActionUrl.HTTPMethod.BROWSER), "open");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        TaskUpdateConfigsState configsState = TaskUpdateConfigsState.getInstance(getEventProject(e));
        TaskUpdateConfig updateConfig = configsState.getUpdateConfig();
        boolean enable = updateConfig == null ? false : isEnable(e, updateConfig.getDetailUrl());
        e.getPresentation().setEnabled(enable);
    }

}

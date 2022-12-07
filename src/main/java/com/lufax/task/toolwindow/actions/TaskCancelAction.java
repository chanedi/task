package com.lufax.task.toolwindow.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskRepository;
import com.lufax.task.toolwindow.ActionUrl;
import com.lufax.task.toolwindow.StatusActionUrlMapping;
import com.lufax.task.toolwindow.TaskUpdateConfig;
import com.lufax.task.toolwindow.TaskUpdateConfigsState;
import org.jetbrains.annotations.NotNull;

public class TaskCancelAction extends TaskItemAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = getEventProject(e);
        Task selectedTask = getSelectedTask(e);
        TaskUpdateConfigsState configsState = TaskUpdateConfigsState.getInstance(project);
        TaskUpdateConfig updateConfig = configsState.getUpdateConfig();
        TaskRepository taskRepository = configsState.getSelectedTaskRepository();
        ActionUrl cancelUrl = updateConfig.getCancelUrl(getStatus(selectedTask));
        doActionUrl(e, taskRepository, selectedTask, cancelUrl, "cancel");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        TaskUpdateConfigsState configsState = TaskUpdateConfigsState.getInstance(getEventProject(e));
        TaskUpdateConfig updateConfig = configsState.getUpdateConfig();
        boolean enable = updateConfig == null ? false : isEnable(e, updateConfig.getCancelUrl(StatusActionUrlMapping.DEFAULT_STATUS).getUrl());
        e.getPresentation().setEnabled(enable);
    }
}

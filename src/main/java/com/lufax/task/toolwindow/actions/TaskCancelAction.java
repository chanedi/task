package com.lufax.task.toolwindow.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.intellij.tasks.TaskRepository;
import com.lufax.task.toolwindow.TaskUpdateConfig;
import com.lufax.task.toolwindow.TaskUpdateConfigsState;
import com.lufax.task.utils.HttpUtils;
import org.jetbrains.annotations.NotNull;

public class TaskCancelAction extends TaskItemAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        TaskUpdateConfigsState configsState = TaskUpdateConfigsState.getInstance(getEventProject(e));
        TaskUpdateConfig updateConfig = configsState.getUpdateConfig();
        TaskRepository taskRepository = configsState.getSelectedTaskRepository();
        try {
            String result = HttpUtils.executeMethod(taskRepository, updateConfig.getCancelMethod(), updateConfig.getCancelUrl(), getTemplateVariables(e));
            Messages.showInfoMessage(result, "Complete task result");
        } catch (Exception ex) {
            Messages.showErrorDialog(ex.getLocalizedMessage(), "Occur error when cancel task");
            throw new RuntimeException(ex);
        }
        refreshTable(e);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        TaskUpdateConfigsState configsState = TaskUpdateConfigsState.getInstance(getEventProject(e));
        TaskUpdateConfig updateConfig = configsState.getUpdateConfig();
        boolean enable = updateConfig == null ? false : isEnable(e, updateConfig.getCancelUrl());
        e.getPresentation().setEnabled(enable);
    }
}

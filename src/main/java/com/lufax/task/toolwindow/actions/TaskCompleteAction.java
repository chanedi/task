package com.lufax.task.toolwindow.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskRepository;
import com.lufax.task.toolwindow.ActionUrl;
import com.lufax.task.toolwindow.StatusActionUrlMapping;
import com.lufax.task.toolwindow.TaskUpdateConfig;
import com.lufax.task.toolwindow.TaskUpdateConfigsState;
import com.lufax.task.utils.HttpUtils;
import org.jetbrains.annotations.NotNull;

public class TaskCompleteAction extends TaskItemAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = getEventProject(e);
        int confirm_cancel = Messages.showOkCancelDialog(project, "Are you sure you want to complete task?", "Confirm Complete", Messages.getQuestionIcon());
        if (confirm_cancel != Messages.OK) {
            return;
        }

        Task selectedTask = getSelectedTask(e);
        TaskUpdateConfigsState configsState = TaskUpdateConfigsState.getInstance(project);
        TaskUpdateConfig updateConfig = configsState.getUpdateConfig();
        TaskRepository taskRepository = configsState.getSelectedTaskRepository();
        ActionUrl completeUrl = updateConfig.getCompleteUrl(getStatus(selectedTask));
        try {
            String result = HttpUtils.executeMethod(taskRepository, completeUrl.getMethod(), completeUrl.getUrl(), getTemplateVariables(selectedTask, project));
            Messages.showInfoMessage(result, "Complete task result");
        } catch (Exception ex) {
            Messages.showErrorDialog(ex.getLocalizedMessage(), "Occur error when complete task");
            throw new RuntimeException(ex);
        }
        refreshTable(e);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        TaskUpdateConfigsState configsState = TaskUpdateConfigsState.getInstance(getEventProject(e));
        TaskUpdateConfig updateConfig = configsState.getUpdateConfig();
        boolean enable = updateConfig == null ? false : isEnable(e, updateConfig.getCompleteUrl(StatusActionUrlMapping.DEFAULT_STATUS).getUrl());
        e.getPresentation().setEnabled(enable);
    }
}

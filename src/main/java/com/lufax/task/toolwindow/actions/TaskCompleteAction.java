package com.lufax.task.toolwindow.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.tasks.TaskRepository;
import com.lufax.task.config.TaskUpdateConfig;
import com.lufax.task.config.TaskUpdateConfigsState;
import com.lufax.task.utils.HttpUtils;
import org.jetbrains.annotations.NotNull;

public class TaskCompleteAction extends TaskItemAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        TaskUpdateConfigsState configsState = TaskUpdateConfigsState.getInstance(getEventProject(e));
        TaskUpdateConfig updateConfig = configsState.getUpdateConfig();
        TaskRepository taskRepository = configsState.getSelectedTaskRepository();
        try {
            String result = HttpUtils.executeMethod(taskRepository, updateConfig.getCompleteMethod(), updateConfig.getCompleteUrl(), getTemplateVariables(e));

        } catch (Exception ex) {
            throw new RuntimeException(ex); // TODO 异常处理
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        TaskUpdateConfigsState configsState = TaskUpdateConfigsState.getInstance(getEventProject(e));
        TaskUpdateConfig updateConfig = configsState.getUpdateConfig();
        boolean enable = isEnable(e, updateConfig.getCompleteUrl());
        e.getPresentation().setEnabled(enable);
    }
}

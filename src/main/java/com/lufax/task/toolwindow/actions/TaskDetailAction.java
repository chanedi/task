package com.lufax.task.toolwindow.actions;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.intellij.tasks.generic.GenericRepositoryUtil;
import com.lufax.task.toolwindow.TaskUpdateConfig;
import com.lufax.task.toolwindow.TaskUpdateConfigsState;
import org.jetbrains.annotations.NotNull;

public class TaskDetailAction extends TaskItemAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        TaskUpdateConfigsState configsState = TaskUpdateConfigsState.getInstance(getEventProject(e));
        TaskUpdateConfig updateConfig = configsState.getUpdateConfig();
        String detailUrl = null;
        try {
            detailUrl = GenericRepositoryUtil.substituteTemplateVariables(updateConfig.getDetailUrl(), getTemplateVariables(e));
        } catch (Exception ex) {
            Messages.showErrorDialog(ex.getLocalizedMessage(), "Occur error when open task");
            throw new RuntimeException(ex);
        }
        BrowserUtil.browse(detailUrl);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        TaskUpdateConfigsState configsState = TaskUpdateConfigsState.getInstance(getEventProject(e));
        TaskUpdateConfig updateConfig = configsState.getUpdateConfig();
        boolean enable = updateConfig == null ? false : isEnable(e, updateConfig.getDetailUrl());
        e.getPresentation().setEnabled(enable);
    }

}

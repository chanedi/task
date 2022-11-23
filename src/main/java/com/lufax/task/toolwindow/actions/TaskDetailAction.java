package com.lufax.task.toolwindow.actions;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
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
            throw new RuntimeException(ex); // TODO 异常处理
        }
        BrowserUtil.browse(detailUrl);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        TaskUpdateConfigsState configsState = TaskUpdateConfigsState.getInstance(getEventProject(e));
        TaskUpdateConfig updateConfig = configsState.getUpdateConfig();
        boolean enable = isEnable(e, updateConfig.getDetailUrl());
        e.getPresentation().setEnabled(enable);
    }

}

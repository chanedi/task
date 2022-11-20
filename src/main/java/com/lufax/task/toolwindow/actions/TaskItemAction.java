package com.lufax.task.toolwindow.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.Task;
import com.intellij.tasks.generic.TemplateVariable;
import com.intellij.ui.table.JBTable;
import com.lufax.task.config.TaskUpdateConfig;
import com.lufax.task.config.TaskUpdateConfigsState;
import com.lufax.task.toolwindow.TaskListTableModel;

import java.util.ArrayList;
import java.util.List;

public abstract class TaskItemAction extends AnAction {

    protected List<TemplateVariable> getTemplateVariables(AnActionEvent e) {
        Task selectedTask = getSelectedTask(e);
        TaskUpdateConfigsState configsState = TaskUpdateConfigsState.getInstance(getEventProject(e));
        TaskUpdateConfig updateConfig = configsState.getUpdateConfig();
        List<TemplateVariable> templateVariables = new ArrayList<>(updateConfig.getTemplateVariables());
        templateVariables.add(new TemplateVariable(TaskUpdateConfig.TASK_ID, selectedTask.getId()));
        return templateVariables;
    }

    protected Task getSelectedTask(AnActionEvent e) {
        JBTable table = (JBTable) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
        TaskListTableModel tableModel = (TaskListTableModel) table.getModel();
        Task selectedTask = tableModel.getItem(table.getSelectedRow());
        return selectedTask;
    }

    protected boolean isEnable(AnActionEvent e) {
        JBTable table = (JBTable) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
        int selectedRow = table.getSelectedRow();
        return selectedRow >= 0;
    }

    protected boolean isEnable(AnActionEvent e, String configUrl) {
        if (StringUtil.isEmpty(configUrl)) {
            return false;
        }
        JBTable table = (JBTable) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
        int selectedRow = table.getSelectedRow();
        return selectedRow >= 0;
    }

}

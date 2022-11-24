package com.lufax.task.toolwindow.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.Task;
import com.intellij.tasks.generic.TemplateVariable;
import com.intellij.ui.table.JBTable;
import com.lufax.task.repository.SuperGenericTask;
import com.lufax.task.toolwindow.TaskUpdateConfig;
import com.lufax.task.toolwindow.TaskUpdateConfigsState;
import com.lufax.task.toolwindow.TaskListTableModel;

import java.util.ArrayList;
import java.util.List;

import static com.lufax.task.repository.SelectorBasedSuperResponseHandler.*;

public abstract class TaskItemAction extends AnAction {

    protected List<TemplateVariable> getTemplateVariables(AnActionEvent e) {
        Task selectedTask = getSelectedTask(e);
        TaskUpdateConfigsState configsState = TaskUpdateConfigsState.getInstance(getEventProject(e));
        TaskUpdateConfig updateConfig = configsState.getUpdateConfig();
        List<TemplateVariable> templateVariables = new ArrayList<>(updateConfig.getTemplateVariables());
        templateVariables.add(new TemplateVariable(ID, selectedTask.getId()));
        templateVariables.add(new TemplateVariable(SUMMARY, selectedTask.getSummary()));
        templateVariables.add(new TemplateVariable(DESCRIPTION, selectedTask.getDescription()));
        if (selectedTask instanceof SuperGenericTask) {
            templateVariables.add(new TemplateVariable(STATUS, ((SuperGenericTask) selectedTask).getStatus()));
            templateVariables.add(new TemplateVariable(RELEASE_DATE, ((SuperGenericTask) selectedTask).getReleaseDate()));
            templateVariables.add(new TemplateVariable(CUSTOM_FIELD_1, ((SuperGenericTask) selectedTask).getCustomField1()));
            templateVariables.add(new TemplateVariable(CUSTOM_FIELD_2, ((SuperGenericTask) selectedTask).getCustomField2()));
            templateVariables.add(new TemplateVariable(CUSTOM_FIELD_3, ((SuperGenericTask) selectedTask).getCustomField3()));
            templateVariables.add(new TemplateVariable(CUSTOM_FIELD_4, ((SuperGenericTask) selectedTask).getCustomField4()));
            templateVariables.add(new TemplateVariable(CUSTOM_FIELD_5, ((SuperGenericTask) selectedTask).getCustomField5()));
        }
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

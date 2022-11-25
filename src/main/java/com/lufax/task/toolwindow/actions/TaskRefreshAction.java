package com.lufax.task.toolwindow.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.ui.table.JBTable;
import com.lufax.task.toolwindow.TaskListTableModel;
import com.lufax.task.toolwindow.TaskUpdateConfigsState;
import org.jetbrains.annotations.NotNull;

/** server update action */
public class TaskRefreshAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        JBTable table = (JBTable) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
        TaskListTableModel tableModel = (TaskListTableModel) table.getModel();
        TaskUpdateConfigsState configsState = TaskUpdateConfigsState.getInstance(getEventProject(e));
        tableModel.updateTaskRepository(configsState.getSelectedTaskRepository());
    }

}

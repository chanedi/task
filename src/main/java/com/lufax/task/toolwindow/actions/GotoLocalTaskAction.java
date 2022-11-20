package com.lufax.task.toolwindow.actions;

import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.tasks.LocalTask;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskManager;
import com.intellij.tasks.actions.OpenTaskDialog;
import com.intellij.tasks.generic.GenericRepositoryUtil;
import com.lufax.task.config.TaskUpdateConfig;
import com.lufax.task.config.TaskUpdateConfigsState;
import org.jetbrains.annotations.NotNull;

public class GotoLocalTaskAction extends TaskItemAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = getEventProject(e);
        TaskManager taskManager = TaskManager.getManager(project);
        Task task = getSelectedTask(e);
        LocalTask localTask = taskManager.findTask(task.getId());

        if (localTask != null) {
            taskManager.activateTask(localTask, true);
        } else {
            showOpenTaskDialog(project, task);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean enable = isEnable(e);
        e.getPresentation().setEnabled(enable);
    }

    private static void showOpenTaskDialog(final Project project, final Task task) {
        JBPopup hint = DocumentationManager.getInstance(project).getDocInfoHint();
        if (hint != null) hint.cancel();
        ApplicationManager.getApplication().invokeLater(() -> new OpenTaskDialog(project, task).show());
    }

}

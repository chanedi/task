package com.lufax.task.toolwindow.actions;

import com.intellij.codeInsight.documentation.DocumentationManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.tasks.LocalTask;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskManager;
import com.intellij.tasks.actions.OpenTaskDialog;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class GotoLocalTaskAction extends TaskItemAction {

    private static Map<String, Task> localTaskMap = new HashMap<>();
    private static ThreadLocal<Task> taskThreadLocal = new ThreadLocal<>();

    public static Task getRepositoryTask(LocalTask localTask) {
        if (localTaskMap.get(localTask.getId()) == null) {
            localTaskMap.put(localTask.getId(), taskThreadLocal.get());
        }
        return localTaskMap.get(localTask.getId());
    }

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
        ApplicationManager.getApplication().invokeLater(() -> {
            taskThreadLocal.set(task);
            OpenTaskDialog openTaskDialog = new OpenTaskDialog(project, task);
            try {
                Field field = OpenTaskDialog.class.getDeclaredField("myTask");
                field.setAccessible(true);
                LocalTask localTask = (LocalTask) field.get(openTaskDialog);
                localTaskMap.put(localTask.getId(), task);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                taskThreadLocal.remove();
            }
            openTaskDialog.show();
        });
    }

}

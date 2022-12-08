package com.lufax.task.toolwindow.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ex.ComboBoxAction;
import com.intellij.openapi.project.Project;
import com.intellij.tasks.TaskManager;
import com.intellij.tasks.TaskRepository;
import com.lufax.task.toolwindow.TaskToolWindowPanel;
import com.lufax.task.toolwindow.TaskUpdateConfig;
import com.lufax.task.toolwindow.TaskUpdateConfigsState;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class TaskServerSelectAction extends ComboBoxAction {

    private Project project;
    private TaskToolWindowPanel taskToolWindowPanel;

    public TaskServerSelectAction(@NotNull TaskToolWindowPanel taskToolWindowPanel) {
        this.taskToolWindowPanel = taskToolWindowPanel;
        this.myPopupTitle = "Select A Task Server";

        project = taskToolWindowPanel.getProject();

        taskToolWindowPanel.updateTaskRepository(TaskUpdateConfigsState.getInstance(project).getSelectedTaskRepository());
    }

    @Override
    protected @NotNull DefaultActionGroup createPopupActionGroup(JComponent button) {
        DefaultActionGroup group = new DefaultActionGroup();
        TaskUpdateConfigsState updateConfigsState = TaskUpdateConfigsState.getInstance(project);
        TaskRepository[] repositories = TaskManager.getManager(project).getAllRepositories();
        for (TaskRepository repository : repositories) {
            TaskUpdateConfig updateConfig = updateConfigsState.getUpdateConfig(repository);
            group.add(new AnAction(updateConfig.getName(), updateConfig.getName(), repository.getIcon()) {
                @Override
                public void actionPerformed(@NotNull AnActionEvent e) {
                    taskToolWindowPanel.updateTaskRepository(repository);
                }
            });
        }
        return group;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        Project project = getEventProject(e);
        if (project == null || project.isDefault() || project.isDisposed()) {
            presentation.setEnabledAndVisible(false);
            presentation.setText("");
            presentation.setIcon(null);
        } else if (e.isFromActionToolbar()) {
            TaskUpdateConfigsState configsState = TaskUpdateConfigsState.getInstance(project);
            TaskRepository selectedTaskRepository = configsState.getSelectedTaskRepository();
            TaskUpdateConfig updateConfig = configsState.getUpdateConfig();

            if (selectedTaskRepository == null) {
                presentation.setEnabledAndVisible(false);
            } else {
                presentation.setEnabledAndVisible(true);
                presentation.setText(updateConfig.getName(), false);
                presentation.setIcon(selectedTaskRepository.getIcon());
                presentation.setDescription(updateConfig.getName());
            }
        } else {
            presentation.setEnabledAndVisible(true);
            presentation.copyFrom(getTemplatePresentation());
        }
    }

}

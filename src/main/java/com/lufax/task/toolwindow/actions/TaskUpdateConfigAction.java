package com.lufax.task.toolwindow.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.tasks.TaskRepository;
import com.lufax.task.config.TaskUpdateConfigsState;
import com.lufax.task.config.TaskUpdateConfigurable;
import com.lufax.task.toolwindow.TaskToolWindowPanel;
import org.jetbrains.annotations.NotNull;

/** server update action */
public class TaskUpdateConfigAction extends AnAction {

    private TaskToolWindowPanel taskToolWindowPanel;

    public TaskUpdateConfigAction(@NotNull TaskToolWindowPanel taskToolWindowPanel) {
        super(AllIcons.General.Settings);
        this.taskToolWindowPanel = taskToolWindowPanel;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = getEventProject(e);
        TaskUpdateConfigurable configurable = new TaskUpdateConfigurable(project);
        if (ShowSettingsUtil.getInstance().editConfigurable(project, configurable)) {
            taskToolWindowPanel.updateTaskRepository(TaskUpdateConfigsState.getInstance(project).getSelectedTaskRepository());
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        Project project = getEventProject(e);
        if (project == null || project.isDefault() || project.isDisposed()) {
            presentation.setEnabledAndVisible(false);
        } else if (e.isFromActionToolbar()) {
            TaskUpdateConfigsState configsState = TaskUpdateConfigsState.getInstance(project);
            TaskRepository selectedTaskRepository = configsState.getSelectedTaskRepository();

            if (selectedTaskRepository == null) {
                presentation.setEnabledAndVisible(false);
            } else {
                presentation.setEnabledAndVisible(true);
            }
        } else {
            presentation.setEnabledAndVisible(true);
            presentation.copyFrom(getTemplatePresentation());
        }
    }

}

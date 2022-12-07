package com.lufax.task.toolwindow;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.tasks.TaskRepository;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.table.JBTable;
import com.lufax.task.toolwindow.actions.TaskServerSelectAction;
import com.lufax.task.toolwindow.actions.TaskUpdateConfigAction;
import org.jetbrains.annotations.NotNull;

public class TaskToolWindowPanel extends SimpleToolWindowPanel {

    private Project project;
    private JBTable taskTable = new JBTable(new TaskListTableModel());

    public TaskToolWindowPanel(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        super(true, true);
        this.project = project;

//        @NotNull List<AnAction> actions = new ArrayList<>();
//        actions.add(new TaskServerSelectAction(this));
//        actions.add(new TaskUpdateConfigAction(this));
//        toolWindow.setTitleActions(actions);

        // toolbar
        final ActionManager actionManager = ActionManager.getInstance();
        DefaultActionGroup group = new DefaultActionGroup();
        group.add(new TaskServerSelectAction(this));
        group.add(new TaskUpdateConfigAction(this));
        group.addSeparator();
        group.addAll(actionManager.getAction("task.toolWindow.toolbar"));
        ActionToolbar actionToolbar = actionManager.createActionToolbar("Task Navigator Toolbar",
                group,
                true);
        actionToolbar.setTargetComponent(taskTable);
        setToolbar(actionToolbar.getComponent());
        setContent(ScrollPaneFactory.createScrollPane(taskTable));
    }

    public void updateTaskRepository(TaskRepository repository) {
        TaskUpdateConfigsState configsState = TaskUpdateConfigsState.getInstance(project);
        if (repository == null) {
            repository = configsState.getSelectedTaskRepository();
        } else {
            configsState.setSelectedTaskRepository(repository);

        }
        ((TaskListTableModel) taskTable.getModel()).updateTaskRepository(repository);
    }

    public Project getProject() {
        return project;
    }

}

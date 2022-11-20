package com.lufax.task.toolwindow;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskRepository;
import com.intellij.ui.AnActionButton;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class TaskListTableModel extends ListTableModel<Task> {

    private static final Logger LOG = Logger.getInstance(TaskListTableModel.class);

    private TaskRepository taskRepository;

    public TaskListTableModel() {
        super(new ColumnInfo<Task, String>("Id") {
            @Override
            public @Nullable String valueOf(Task task) {
                return task.getId();
            }
        }, new ColumnInfo<Task, String>("Summary") {
            @Override
            public @Nullable String valueOf(Task task) {
                return task.getSummary();
            }
        }, new ColumnInfo<Task, String>("Description") {
            @Override
            public @Nullable String valueOf(Task task) {
                return task.getDescription();
            }
        }, new ColumnInfo<Task, String>("State") {
            @Override
            public @Nullable String valueOf(Task task) {
                return task.getState() == null ? "" : task.getState().name();
            }
        });
    }

    public void updateTaskRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        try {
            setItems(Arrays.asList(taskRepository.getIssues("", 0, 10, false)));
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
        }
    }
}

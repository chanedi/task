package com.lufax.task.toolwindow;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskRepository;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import com.lufax.task.repository.SuperGenericTask;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
        }, new ColumnInfo<Task, String>("ReleaseDate") {
            @Override
            public @Nullable String valueOf(Task task) {
                if (task instanceof SuperGenericTask) {
                    return ((SuperGenericTask) task).getReleaseDate() == null ? "" : ((SuperGenericTask) task).getReleaseDate();
                }
                return "";
            }
        }, new ColumnInfo<Task, String>("Status") {
            @Override
            public @Nullable String valueOf(Task task) {
                if (task instanceof SuperGenericTask) {
                    return ((SuperGenericTask) task).getStatus() == null ? "" : ((SuperGenericTask) task).getStatus();
                }
                return task.getState() == null ? "" : task.getState().name();
            }
        }, new ColumnInfo<Task, String>("Tag") {
            @Override
            public @Nullable String valueOf(Task task) {
                if (task instanceof SuperGenericTask) {
                    return ((SuperGenericTask) task).getTag() == null ? "" : ((SuperGenericTask) task).getTag();
                }
                return "";
            }
        }, new ColumnInfo<Task, String>("Description") {
            @Override
            public @Nullable String valueOf(Task task) {
                return task.getDescription();
            }
        }, new ColumnInfo<Task, String>("CustomField1") {
            @Override
            public @Nullable String valueOf(Task task) {
                if (task instanceof SuperGenericTask) {
                    return ((SuperGenericTask) task).getCustomField1() == null ? "" : ((SuperGenericTask) task).getCustomField1();
                }
                return "";
            }
        }, new ColumnInfo<Task, String>("CustomField2") {
            @Override
            public @Nullable String valueOf(Task task) {
                if (task instanceof SuperGenericTask) {
                    return ((SuperGenericTask) task).getCustomField2() == null ? "" : ((SuperGenericTask) task).getCustomField2();
                }
                return "";
            }
        }, new ColumnInfo<Task, String>("CustomField3") {
            @Override
            public @Nullable String valueOf(Task task) {
                if (task instanceof SuperGenericTask) {
                    return ((SuperGenericTask) task).getCustomField3() == null ? "" : ((SuperGenericTask) task).getCustomField3();
                }
                return "";
            }
        }, new ColumnInfo<Task, String>("CustomField4") {
            @Override
            public @Nullable String valueOf(Task task) {
                if (task instanceof SuperGenericTask) {
                    return ((SuperGenericTask) task).getCustomField4() == null ? "" : ((SuperGenericTask) task).getCustomField4();
                }
                return "";
            }
        }, new ColumnInfo<Task, String>("CustomField5") {
            @Override
            public @Nullable String valueOf(Task task) {
                if (task instanceof SuperGenericTask) {
                    return ((SuperGenericTask) task).getCustomField5() == null ? "" : ((SuperGenericTask) task).getCustomField5();
                }
                return "";
            }
        });
    }

    public void updateTaskRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        if (taskRepository == null) {
            setItems(new ArrayList<>());
        }
        Application app = ApplicationManager.getApplication();
        if (!(app == null || app.isUnitTestMode() || app.isHeadlessEnvironment() || !app.isReadAccessAllowed())) {
            try {
                setItems(Arrays.asList(taskRepository.getIssues("", 0, 100, false)));
            } catch (Exception e) {
                LOG.warn(e.getMessage(), e);
            }
        }
    }
}

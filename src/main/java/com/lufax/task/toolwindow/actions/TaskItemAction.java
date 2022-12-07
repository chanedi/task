package com.lufax.task.toolwindow.actions;

import com.intellij.dvcs.branch.DvcsTaskHandler;
import com.intellij.dvcs.repo.Repository;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.VcsTaskHandler;
import com.intellij.tasks.BranchInfo;
import com.intellij.tasks.LocalTask;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskManager;
import com.intellij.tasks.generic.TemplateVariable;
import com.intellij.ui.table.JBTable;
import com.lufax.task.repository.SuperGenericRepository;
import com.lufax.task.repository.SuperGenericTask;
import com.lufax.task.toolwindow.TaskListTableModel;
import com.lufax.task.toolwindow.TaskUpdateConfigsState;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.lufax.task.repository.SelectorBasedSuperResponseHandler.*;

public abstract class TaskItemAction extends AnAction {

    public static final String BRANCH = "branchName";
    public static final String REVISION = "currentRevision";
    public static final String APP_NAME = "appName";

    protected List<TemplateVariable> getTemplateVariables(Task selectedTask, Project project) {
        TaskUpdateConfigsState configsState = TaskUpdateConfigsState.getInstance(project);
        TaskManager taskManager = TaskManager.getManager(project);
        List<TemplateVariable> templateVariables = new ArrayList<>();
        templateVariables.add(new TemplateVariable(ID, selectedTask.getId()));
        templateVariables.add(new TemplateVariable(SUMMARY, selectedTask.getSummary()));
        templateVariables.add(new TemplateVariable(DESCRIPTION, selectedTask.getDescription() == null ? "" : selectedTask.getDescription()));
        templateVariables.add(new TemplateVariable(SuperGenericRepository.SERVER_URL, configsState.getSelectedTaskRepository().getUrl()));
        templateVariables.add(new TemplateVariable(APP_NAME, project.getName()));
        if (taskManager.isVcsEnabled()) {
            LocalTask task = taskManager.findTask(selectedTask.getId());
            if (task != null) {
                List<BranchInfo> branches = task.getBranches(false);
                if (branches.size() > 0) {
                    BranchInfo branchInfo = branches.get(0);
                    templateVariables.add(new TemplateVariable(BRANCH, branchInfo.name));

                    DvcsTaskHandler dvcsTaskHandler = null;
                    VcsTaskHandler[] allHandlers = VcsTaskHandler.getAllHandlers(project);
                    for (VcsTaskHandler handler : allHandlers) {
                        if (!(handler instanceof DvcsTaskHandler)) {
                            continue;
                        }
                        dvcsTaskHandler = (DvcsTaskHandler) handler;
                        break;
                    }

                    if (dvcsTaskHandler != null) {
                        List<String> repositoryUrls = new ArrayList<>();
                        repositoryUrls.add(branchInfo.repository);
                        List<? extends Repository> repositories;
                        try {
                            Method reflectMethod = DvcsTaskHandler.class.getDeclaredMethod("getRepositories", Collection.class);
                            reflectMethod.setAccessible(true);
                            repositories = (List<? extends Repository>) reflectMethod.invoke(dvcsTaskHandler, repositoryUrls);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                        if (repositories.size() > 0) {
                            templateVariables.add(new TemplateVariable(REVISION, repositories.get(0).getCurrentRevision()));
                        }
                    }
                }
            }
        }

        if (selectedTask instanceof SuperGenericTask) {
            templateVariables.addAll(((SuperGenericRepository) configsState.getSelectedTaskRepository()).getAllTemplateVariables());
            templateVariables.add(new TemplateVariable(STATUS, ((SuperGenericTask) selectedTask).getStatus()));
            templateVariables.add(new TemplateVariable(RELEASE_DATE, ((SuperGenericTask) selectedTask).getReleaseDate()));
            templateVariables.add(new TemplateVariable(TAG, ((SuperGenericTask) selectedTask).getTag()));
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

    protected String getStatus(Task task) {
        if (task instanceof SuperGenericTask) {
            return ((SuperGenericTask) task).getStatus() == null ? "" : ((SuperGenericTask) task).getStatus();
        }
        return task.getState() == null ? "" : task.getState().name();
    }

    protected void refreshTable(AnActionEvent e) {
        JBTable table = (JBTable) e.getData(PlatformDataKeys.CONTEXT_COMPONENT);
        TaskListTableModel tableModel = (TaskListTableModel) table.getModel();
        TaskUpdateConfigsState configsState = TaskUpdateConfigsState.getInstance(getEventProject(e));
        tableModel.updateTaskRepository(configsState.getSelectedTaskRepository());
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

package com.lufax.task.toolwindow;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import com.lufax.task.toolwindow.actions.TaskItemAction;
import com.lufax.task.utils.HttpUtils;
import com.lufax.task.utils.SwingUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.tasks.generic.GenericRepositoryUtil.concat;
import static com.lufax.task.repository.SelectorBasedSuperResponseHandler.*;

public class TaskUpdateConfigurable implements SearchableConfigurable {

    private final Project myProject;
    private JPanel myWholePanel;
    private JTextField myNameField;
    private JTextField myLoginUrlField;
    private EditorTextField myDetailUrlField;
    private JPanel myCompletePannel;
    private JPanel myCancelPannel;

    private StatusUrlMappingTable completeTable;
    private StatusUrlMappingTable cancelTable;

    public TaskUpdateConfigurable(Project project) {
        super();
        this.myProject = project;

        TaskUpdateConfigsState taskUpdateConfigsState = TaskUpdateConfigsState.getInstance(myProject);
        TaskUpdateConfig updateConfig = taskUpdateConfigsState.getUpdateConfig();
//        myCompleteUrlField = SwingUtils.createTextFieldWithCompletion(myProject, updateConfig.getCompleteUrl(StatusActionUrlMapping.DEFAULT_STATUS).getUrl(), placeholders);
//        myCancelUrlField = SwingUtils.createTextFieldWithCompletion(myProject, updateConfig.getCancelUrl(StatusActionUrlMapping.DEFAULT_STATUS).getUrl(), placeholders);
        completeTable = new StatusUrlMappingTable(taskUpdateConfigsState.getSelectedTaskRepository(), updateConfig.getCompleteUrls());
        myCompletePannel.add(completeTable.getComponent(), BorderLayout.CENTER);
        cancelTable = new StatusUrlMappingTable(taskUpdateConfigsState.getSelectedTaskRepository(), updateConfig.getCancelUrls());
        myCancelPannel.add(cancelTable.getComponent(), BorderLayout.CENTER);

//        myManageTemplateVariablesButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(final ActionEvent e) {
//                final ManageTemplateVariablesDialog dialog = new ManageTemplateVariablesDialog(manageTemplateVariablesButton, false);
//                dialog.setTemplateVariables(taskUpdateConfigsState.getUpdateConfig().getAllTemplateVariables());
//                if (dialog.showAndGet()) {
//                    taskUpdateConfigsState.getUpdateConfig().setTemplateVariables(ContainerUtil.filter(dialog.getTemplateVariables(), variable -> !variable.isReadOnly()));
//                    myCustomPanel.removeAll();
//                    myCustomPanel.add(createCustomPanel());
//                    //myCustomPanel.repaint();
//                    myTabbedPane.getComponentAt(0).repaint();
//
//                    //myLoginURLText = createEditorFieldWithPlaceholderCompletion(myRepository.getLoginUrl());
//                    List<String> placeholders = createPlaceholdersList(myRepository);
//                    ((TextFieldWithAutoCompletion)myLoginURLText).setVariants(placeholders);
//                    ((TextFieldWithAutoCompletion)myTasksListURLText).setVariants(concat(placeholders, "{max}", "{since}"));
//                    ((TextFieldWithAutoCompletion)completeUrlField).setVariants(concat(placeholders, "{id}"));
//                    myPanel.repaint();
//                }
//            }
//        });

    }

    private void createUIComponents() {
        TaskUpdateConfigsState taskUpdateConfigsState = TaskUpdateConfigsState.getInstance(myProject);
        TaskUpdateConfig updateConfig = taskUpdateConfigsState.getUpdateConfig();
        List<String> placeholders = concat(new ArrayList<>(), ID, SUMMARY, STATUS, RELEASE_DATE, TAG, DESCRIPTION, TaskItemAction.BRANCH, TaskItemAction.REVISION, CUSTOM_FIELD_1, CUSTOM_FIELD_2, CUSTOM_FIELD_3, CUSTOM_FIELD_4, CUSTOM_FIELD_5);
        myDetailUrlField = SwingUtils.createTextFieldWithCompletion(myProject, updateConfig.getDetailUrl(), placeholders);
    }

    @Override
    public @NotNull @NonNls String getId() {
        return "tasks.update.servers";
    }

    @Override
    public String getDisplayName() {
        return "Task Server Detail Config";
    }

    @Override
    public @Nullable JComponent createComponent() {
        return myWholePanel;
    }

    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public void apply() throws ConfigurationException {
        TaskUpdateConfigsState settings = TaskUpdateConfigsState.getInstance(myProject);

        TaskUpdateConfig updateConfig = new TaskUpdateConfig();
        updateConfig.setName(myNameField.getText());
        updateConfig.setDetailUrl(HttpUtils.addSchemeIfNoneSpecified(settings.getSelectedTaskRepository(), myDetailUrlField.getText()));
        settings.updateConfig(updateConfig);
        updateConfig.setCompleteUrls(completeTable.getValues());
        updateConfig.setCancelUrls(cancelTable.getValues());
    }

    @Override
    public void reset() {
        TaskUpdateConfigsState settings = TaskUpdateConfigsState.getInstance(myProject);

        TaskUpdateConfig updateConfig = settings.getUpdateConfig();
        myNameField.setText(updateConfig.getName());
        myLoginUrlField.setText(settings.getSelectedTaskRepository().getUrl());
        myDetailUrlField.setText(updateConfig.getDetailUrl());
        completeTable.setValues(updateConfig.getCompleteUrls());
        cancelTable.setValues(updateConfig.getCancelUrls());
    }

}

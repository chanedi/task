package com.lufax.task.config;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.ui.EditorTextField;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.net.HTTPMethod;
import com.lufax.task.ManageTemplateVariablesDialog;
import com.lufax.task.utils.HttpUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TaskUpdateConfigurable implements SearchableConfigurable {

    private final Project project;
    private JPanel myWholePanel;
    private JTextField nameField;
    private JTextField loginUrlField;
    private JTextField detailUrlField;
    private EditorTextField completeUrlField;
    private EditorTextField cancelUrlField;
    private JComboBox completeMethodCombo;
    private JComboBox cancelMethodCombo;
    private JButton manageTemplateVariablesButton;

    public TaskUpdateConfigurable(Project project) {
        super();
        this.project = project;

        manageTemplateVariablesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                final ManageTemplateVariablesDialog dialog = new ManageTemplateVariablesDialog(manageTemplateVariablesButton, false);
                TaskUpdateConfigsState taskUpdateConfigsState = TaskUpdateConfigsState.getInstance(project);
                dialog.setTemplateVariables(taskUpdateConfigsState.getUpdateConfig().getAllTemplateVariables());
                if (dialog.showAndGet()) {
                    taskUpdateConfigsState.getUpdateConfig().setTemplateVariables(ContainerUtil.filter(dialog.getTemplateVariables(), variable -> !variable.isReadOnly()));
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
                }
            }
        });

    }

    @Override
    public @NotNull @NonNls String getId() {
        return "tasks.update.servers";
    }

    @Override
    public @NlsContexts.ConfigurableName String getDisplayName() {
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
        TaskUpdateConfigsState settings = TaskUpdateConfigsState.getInstance(project);

        TaskUpdateConfig updateConfig = new TaskUpdateConfig();
        updateConfig.setName(nameField.getText());
        updateConfig.setDetailUrl(detailUrlField.getText());
        updateConfig.setCompleteUrl(HttpUtils.addSchemeIfNoneSpecified(settings.getSelectedTaskRepository(), completeUrlField.getText()));
        updateConfig.setCancelUrl(cancelUrlField.getText());
        updateConfig.setCompleteMethod(HTTPMethod.valueOf((String) completeMethodCombo.getSelectedItem()));
        updateConfig.setCancelMethod(HTTPMethod.valueOf((String) cancelMethodCombo.getSelectedItem()));
        settings.updateConfig(updateConfig);
    }

    @Override
    public void reset() {
        TaskUpdateConfigsState settings = TaskUpdateConfigsState.getInstance(project);

        TaskUpdateConfig updateConfig = settings.getUpdateConfig();
        nameField.setText(updateConfig.getName());
        loginUrlField.setText(settings.getSelectedTaskRepository().getUrl());
        detailUrlField.setText(updateConfig.getDetailUrl());
        completeUrlField.setText(updateConfig.getCompleteUrl());
        cancelUrlField.setText(updateConfig.getCancelUrl());
        completeMethodCombo.setSelectedItem(updateConfig.getCompleteMethod());
        cancelMethodCombo.setSelectedItem(updateConfig.getCancelMethod());

//        private JButton completeManageTemplateVariablesButton;
//        private JButton cancelManageTemplateVariablesButton;
    }

}

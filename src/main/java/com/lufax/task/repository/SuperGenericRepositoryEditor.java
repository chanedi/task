package com.lufax.task.repository;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.tasks.TaskBundle;
import com.intellij.tasks.TaskManager;
import com.intellij.tasks.config.BaseRepositoryEditor;
import com.intellij.tasks.generic.ResponseType;
import com.intellij.tasks.generic.TemplateVariable;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.Consumer;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.net.HTTPMethod;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.UIUtil;
import com.lufax.task.ManageTemplateVariablesDialog;
import com.lufax.task.utils.SwingUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static com.intellij.tasks.generic.GenericRepositoryUtil.*;

/**
 * @author Evgeny.Zakrevsky
 * @author Mikhail Golubev
 */
public class SuperGenericRepositoryEditor extends BaseRepositoryEditor<SuperGenericRepository> {

  protected EditorTextField myLoginURLText;
  private EditorTextField myTasksListURLText;
  private EditorTextField mySingleTaskURLText;
  protected JBLabel myLoginURLLabel;
  protected ComboBox myLoginMethodTypeComboBox;
  private ComboBox myTasksListMethodTypeComboBox;
  private ComboBox mySingleTaskMethodComboBox;
  private JPanel myPanel;
  private JRadioButton myXmlRadioButton;
  private JRadioButton myTextRadioButton;
  private JButton myTest2Button;
  private JRadioButton myJsonRadioButton;
  private JButton myManageTemplateVariablesButton;
  private JButton myResetToDefaultsButton;
  private JPanel myCardPanel;
  private JBLabel mySingleTaskURLLabel;
  private JBCheckBox myDownloadTasksInSeparateRequests;
  private JButton myTestLoginButton;
  private JButton myTestParseTaskButton;
  private JTextField myLoginSuccessCookieNameText;
  private EditorTextField myLoginWithTokenURLText;
  private JLabel myLoginWithTokenURLLabel;
  private JLabel myLoginSuccessCookieNameLabel;
  private ComboBox myLoginWithTokenMethodTypeComboBox;
  private JButton myTestLoginWithTokenButton;

  private Map<JTextField, TemplateVariable> myField2Variable;
  private final Map<JRadioButton, ResponseType> myRadio2ResponseType;

  public SuperGenericRepositoryEditor(final Project project,
                                      final SuperGenericRepository repository,
                                      final Consumer<? super SuperGenericRepository> changeListener) {
    super(project, repository, changeListener);

    myTest2Button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        afterTestConnection(repository.testTaskConnection(project));
      }
    });
    myTestLoginButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        afterTestConnection(repository.testLoginConnection(project));
      }
    });
    myTestLoginWithTokenButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        afterTestConnection(repository.testLoginWithTokenConnection(project));
      }
    });
    myTestParseTaskButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        afterTestConnection(TaskManager.getManager(project).testConnection(repository));      }
    });

    myLoginAnonymouslyJBCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        loginUrlEnablingChanged();
      }
    });
    myUseHttpAuthenticationCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        loginUrlEnablingChanged();
      }
    });

    ActionListener radioButtonListener = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        singleTaskUrlEnablingChanged();
        doApply();
        selectCardByResponseType();
      }
    };
    myXmlRadioButton.addActionListener(radioButtonListener);
    myTextRadioButton.addActionListener(radioButtonListener);
    myJsonRadioButton.addActionListener(radioButtonListener);

    myLoginSuccessCookieNameText.setText(myRepository.getLoginSuccessCookieName());
    myLoginMethodTypeComboBox.setSelectedItem(myRepository.getLoginMethodType().toString()); //NON-NLS
    myLoginWithTokenMethodTypeComboBox.setSelectedItem(myRepository.getLoginWithTokenMethodType().toString()); //NON-NLS
    myTasksListMethodTypeComboBox.setSelectedItem(myRepository.getTasksListMethodType().toString()); //NON-NLS
    mySingleTaskMethodComboBox.setSelectedItem(myRepository.getSingleTaskMethodType().toString()); //NON-NLS

    // set default listener updating model fields
    installListener(myLoginMethodTypeComboBox);
    installListener(myLoginWithTokenMethodTypeComboBox);
    installListener(myTasksListMethodTypeComboBox);
    installListener(mySingleTaskMethodComboBox);
    installListener(myLoginURLText);
    installListener(myLoginSuccessCookieNameText);
    installListener(myLoginWithTokenURLText);
    installListener(myTasksListURLText);
    installListener(mySingleTaskURLText);
    installListener(myDownloadTasksInSeparateRequests);
    myTabbedPane.addTab(TaskBundle.message("server.configuration"), myPanel);

    // Put appropriate configuration components on the card panel
    SuperResponseHandler xmlHandler = myRepository.getResponseHandler(ResponseType.XML);
    SuperResponseHandler jsonHandler = myRepository.getResponseHandler(ResponseType.JSON);
    SuperResponseHandler textHandler = myRepository.getResponseHandler(ResponseType.TEXT);
    // Select appropriate card pane
    myCardPanel.add(xmlHandler.getConfigurationComponent(myProject), ResponseType.XML.getMimeType());
    myCardPanel.add(jsonHandler.getConfigurationComponent(myProject), ResponseType.JSON.getMimeType());
    myCardPanel.add(textHandler.getConfigurationComponent(myProject), ResponseType.TEXT.getMimeType());

    myRadio2ResponseType = new IdentityHashMap<>();
    myRadio2ResponseType.put(myJsonRadioButton, ResponseType.JSON);
    myRadio2ResponseType.put(myXmlRadioButton, ResponseType.XML);
    myRadio2ResponseType.put(myTextRadioButton, ResponseType.TEXT);

    myManageTemplateVariablesButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final ManageTemplateVariablesDialog dialog = new ManageTemplateVariablesDialog(myManageTemplateVariablesButton);
        dialog.setTemplateVariables(myRepository.getAllTemplateVariables());
        if (dialog.showAndGet()) {
          myRepository.setTemplateVariables(ContainerUtil.filter(dialog.getTemplateVariables(), variable -> !variable.isReadOnly()));
          myCustomPanel.removeAll();
          myCustomPanel.add(createCustomPanel());
          //myCustomPanel.repaint();
          myTabbedPane.getComponentAt(0).repaint();

          //myLoginURLText = createEditorFieldWithPlaceholderCompletion(myRepository.getLoginUrl());
          List<String> placeholders = createPlaceholdersList(myRepository.getAllTemplateVariables());
          ((TextFieldWithAutoCompletion) myLoginURLText).setVariants(placeholders);
          ((TextFieldWithAutoCompletion) myLoginWithTokenURLText).setVariants(concat(placeholders, "{dynamicToken}"));
          ((TextFieldWithAutoCompletion) myTasksListURLText).setVariants(concat(placeholders, "{limit}", "{offset}"));
          ((TextFieldWithAutoCompletion) mySingleTaskURLText).setVariants(concat(placeholders, "{id}"));
          myPanel.repaint();
        }
      }
    });

    myResetToDefaultsButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        myRepository.resetToDefaults();
        // TODO: look closely
        reset((SuperGenericRepository) myRepository.clone());
      }
    });

    selectRadioButtonByResponseType();
    selectCardByResponseType();
    loginUrlEnablingChanged();
    singleTaskUrlEnablingChanged();
    myDownloadTasksInSeparateRequests.setSelected(myRepository.getDownloadTasksInSeparateRequests());
  }

  private void singleTaskUrlEnablingChanged() {
    boolean enabled = !myTextRadioButton.isSelected();
    // single task URL doesn't make sense when legacy regex handler is used
    mySingleTaskURLText.setEnabled(enabled);
    mySingleTaskMethodComboBox.setEnabled(enabled);
    mySingleTaskURLLabel.setEnabled(enabled);
  }

  protected void loginUrlEnablingChanged() {
    boolean enabled = !myLoginAnonymouslyJBCheckBox.isSelected() && !myUseHttpAuthenticationCheckBox.isSelected();
    myLoginURLLabel.setEnabled(enabled);
    myLoginURLText.setEnabled(enabled);
    myLoginSuccessCookieNameLabel.setEnabled(enabled);
    myLoginSuccessCookieNameText.setEnabled(enabled);
    myLoginWithTokenURLLabel.setEnabled(enabled);
    myLoginWithTokenURLText.setEnabled(enabled);
    myLoginMethodTypeComboBox.setEnabled(enabled);
    myLoginWithTokenMethodTypeComboBox.setEnabled(enabled);
  }

  @Nullable
  @Override
  protected JComponent createCustomPanel() {
    myField2Variable = new IdentityHashMap<>();
    FormBuilder builder = FormBuilder.createFormBuilder();
    for (final TemplateVariable variable : myRepository.getTemplateVariables()) {
      if (variable.isShownOnFirstTab()) {
        JTextField field = variable.isHidden() ? new JPasswordField(variable.getValue()) : new JTextField(variable.getValue());
        myField2Variable.put(field, variable);
        installListener(field);
        JBLabel label = new JBLabel(prettifyVariableName(variable.getName()) + ":", SwingConstants.RIGHT);
        label.setAnchor(getAnchor());
        builder.addLabeledComponent(label, field);
      }
    }
    return builder.getPanel();
  }

  protected void reset(final SuperGenericRepository clone) {
    myLoginURLText.setText(clone.getLoginUrl());
    myLoginWithTokenURLText.setText(clone.getLoginWithTokenUrl());
    myLoginSuccessCookieNameText.setText(clone.getLoginSuccessCookieName());
    myTasksListURLText.setText(clone.getTasksListUrl());
    mySingleTaskURLText.setText(clone.getSingleTaskUrl());
    //myTaskPatternText.setText(clone.getTaskPattern());
    myLoginMethodTypeComboBox.setSelectedItem(clone.getLoginMethodType());
    myLoginWithTokenMethodTypeComboBox.setSelectedItem(clone.getLoginWithTokenMethodType());
    myTasksListMethodTypeComboBox.setSelectedItem(clone.getTasksListMethodType());
    mySingleTaskMethodComboBox.setSelectedItem(clone.getSingleTaskMethodType());
    selectRadioButtonByResponseType();
    selectCardByResponseType();
    loginUrlEnablingChanged();
    myDownloadTasksInSeparateRequests.setSelected(myRepository.getDownloadTasksInSeparateRequests());
  }

  private void selectRadioButtonByResponseType() {
    for (Map.Entry<JRadioButton, ResponseType> entry : myRadio2ResponseType.entrySet()) {
      if (entry.getValue() == myRepository.getResponseType()) {
        entry.getKey().setSelected(true);
      }
    }
  }

  private void selectCardByResponseType() {
    CardLayout cardLayout = (CardLayout) myCardPanel.getLayout();
    cardLayout.show(myCardPanel, myRepository.getResponseType().getMimeType());
  }

  @Override
  public void apply() {
    myRepository.setLoginUrl(myLoginURLText.getText());
    myRepository.setLoginSuccessCookieName(myLoginSuccessCookieNameText.getText());
    myRepository.setLoginWithTokenUrl(myLoginWithTokenURLText.getText());
    myRepository.setTasksListUrl(myTasksListURLText.getText());
    myRepository.setSingleTaskUrl(mySingleTaskURLText.getText());

    myRepository.setLoginMethodType(HTTPMethod.valueOf((String)myLoginMethodTypeComboBox.getSelectedItem()));
    myRepository.setLoginWithTokenMethodType(HTTPMethod.valueOf((String)myLoginWithTokenMethodTypeComboBox.getSelectedItem()));
    myRepository.setTasksListMethodType(HTTPMethod.valueOf((String)myTasksListMethodTypeComboBox.getSelectedItem()));
    myRepository.setSingleTaskMethodType(HTTPMethod.valueOf((String)mySingleTaskMethodComboBox.getSelectedItem()));

    myRepository.setDownloadTasksInSeparateRequests(myDownloadTasksInSeparateRequests.isSelected());
   for (Map.Entry<JTextField, TemplateVariable> entry : myField2Variable.entrySet()) {
      TemplateVariable variable = entry.getValue();
      JTextField field = entry.getKey();
      variable.setValue(field.getText());
    }
    for (Map.Entry<JRadioButton, ResponseType> entry : myRadio2ResponseType.entrySet()) {
      if (entry.getKey().isSelected()) {
        myRepository.setResponseType(entry.getValue());
      }
    }
    super.apply();
  }

  private void createUIComponents() {
    List<String> placeholders = createPlaceholdersList(myRepository.getAllTemplateVariables());
    myLoginURLText = SwingUtils.createTextFieldWithCompletion(myProject, myRepository.getLoginUrl(), placeholders);
    myLoginWithTokenURLText = SwingUtils.createTextFieldWithCompletion(myProject, myRepository.getLoginWithTokenUrl(), concat(placeholders, "{dynamicToken}"));
    myTasksListURLText = SwingUtils.createTextFieldWithCompletion(myProject, myRepository.getTasksListUrl(), concat(placeholders, "{limit}", "{offset}"));
    mySingleTaskURLText = SwingUtils.createTextFieldWithCompletion(myProject, myRepository.getSingleTaskUrl(), concat(placeholders, "{id}"));
  }

  @Override
  public void setAnchor(@Nullable JComponent anchor) {
    super.setAnchor(anchor);
    List<JBLabel> labels = UIUtil.findComponentsOfType(myCustomPanel, JBLabel.class);
    for (JBLabel label : labels) {
      label.setAnchor(anchor);
    }
  }

}

package com.lufax.task.toolwindow.actions.gotolocal;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.tasks.LocalTask;
import com.intellij.tasks.Task;
import com.intellij.tasks.impl.TaskUtil;
import com.intellij.tasks.ui.TaskDialogPanel;
import com.lufax.task.repository.SuperGenericTask;
import com.lufax.task.toolwindow.actions.GotoLocalTaskAction;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

public class SuperOpenTaskPanel extends TaskDialogPanel {

    private static final Logger LOG = Logger.getInstance(SuperOpenTaskPanel.class);
    private JPanel myPanel;
    private Project myProject;
    private LocalTask myLocalTask;
    private JTextField myReleaseFilePath;
    private JCheckBox myCreateReleaseFileCheckBox;

    public SuperOpenTaskPanel(Project project, LocalTask localTask) {
        myProject = project;
        myLocalTask = localTask;
        myCreateReleaseFileCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                myReleaseFilePath.setEnabled(myCreateReleaseFileCheckBox.isSelected());
            }
        });

        Date date = new Date();
        Task task = GotoLocalTaskAction.getRepositoryTask(localTask);
        if (task != null && task instanceof SuperGenericTask && StringUtil.isNotEmpty(((SuperGenericTask) task).getReleaseDate())) {
            try {
                date = DateUtils.parseDate(((SuperGenericTask) task).getReleaseDate(), new String[]{"yyyyMMdd", "yyyy-MM-dd"});
            } catch (ParseException e) {
                LOG.error("parse release date error:" + ((SuperGenericTask) task).getReleaseDate());
            }
        }
        myReleaseFilePath.setText("release/" + DateFormatUtils.format(date, "yyyyMMdd") + ".txt");
    }

    @Override
    public @NotNull JComponent getPanel() {
        return myPanel;
    }

    @Override
    public void commit() {
        if (!myCreateReleaseFileCheckBox.isSelected()) {
            return;
        }
        File file = new File(myProject.getBasePath() + "/" + myReleaseFilePath.getText());
        if (file.exists()) {
            int overwrite = Messages.showYesNoDialog("Release file " + file.getPath() + " already exists. Do you want to modify path?",
                    "Release file exists", Messages.getQuestionIcon());
            if (Messages.NO == overwrite) {
                return;
            } else {
                throw new RuntimeException();
            }
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        VirtualFile dir = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(file.getParentFile());
        PsiManager psiManager = PsiManager.getInstance(myProject);
        FileTemplate template = FileTemplateManager.getInstance(myProject).getInternalTemplate("Release File.txt");
        try {
            Method formatFromExtensionsMethod = TaskUtil.class.getDeclaredMethod("formatFromExtensions", LocalTask.class);
            formatFromExtensionsMethod.setAccessible(true);
            @Nullable Properties props = new Properties();
            props.put("defaultCommitMessage", TaskUtil.getChangeListComment(myLocalTask));
            props.putAll((Map<?, ?>) formatFromExtensionsMethod.invoke(null, myLocalTask));
            FileTemplateUtil.createFromTemplate(template, file.getName(), props, psiManager.findDirectory(dir));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

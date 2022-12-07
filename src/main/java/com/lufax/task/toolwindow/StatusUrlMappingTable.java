package com.lufax.task.toolwindow;

import com.intellij.execution.util.ListTableWithButtons;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.TaskRepository;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ComboBoxCellEditor;
import com.intellij.util.ui.ListTableModel;
import com.lufax.task.toolwindow.actions.TaskItemAction;
import com.lufax.task.utils.HttpUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lufax.task.repository.SelectorBasedSuperResponseHandler.*;

public class StatusUrlMappingTable extends ListTableWithButtons<StatusActionUrlMapping> {

    private TaskRepository taskRepository;

    public StatusUrlMappingTable(TaskRepository taskRepository, Map<String, ActionUrl> actionUrlMap) {
        this.taskRepository = taskRepository;
        setValues(actionUrlMap);
    }

    public Map<String, ActionUrl> getValues() {
        Map<String, ActionUrl> map = new HashMap<>();
        List<StatusActionUrlMapping> list = super.getElements();
        for (StatusActionUrlMapping statusActionUrlMapping : list) {
            if (statusActionUrlMapping.getStatus().equals(StatusActionUrlMapping.DEFAULT_STATUS) && !statusActionUrlMapping.isDefault()) {
                continue;
            }
            map.put(statusActionUrlMapping.getStatus(), new ActionUrl(statusActionUrlMapping.getUrl(), statusActionUrlMapping.getMethod()));

        }
        return map;
    }

    public void setValues(Map<String, ActionUrl> map) {
        List<StatusActionUrlMapping> list = new ArrayList<>(map.size());
        StatusActionUrlMapping defaultUrl = new StatusActionUrlMapping(StatusActionUrlMapping.DEFAULT_STATUS, map.get(StatusActionUrlMapping.DEFAULT_STATUS).getUrl(), map.get(StatusActionUrlMapping.DEFAULT_STATUS).getMethod());
        defaultUrl.setDefault(true);
        list.add(defaultUrl);
        for (Map.Entry<String, ActionUrl> entry : map.entrySet()) {
            if (entry.getKey().equals(StatusActionUrlMapping.DEFAULT_STATUS)) {
                continue;
            }
            StatusActionUrlMapping e = new StatusActionUrlMapping(entry.getKey(), entry.getValue().getUrl(), entry.getValue().getMethod());
            list.add(e);
        }
        super.setValues(list);
    }

    @Override
    protected ListTableModel createListModel() {
        ColumnInfo status = new ElementsColumnInfoBase<StatusActionUrlMapping>("Status") {

            @Override
            public int getWidth(JTable table) {
                return 100;
            }

            @Override
            public @Nullable String valueOf(StatusActionUrlMapping statusActionUrlMapping) {
                return statusActionUrlMapping.getStatus();
            }

            @Override
            protected @Nullable String getDescription(StatusActionUrlMapping element) {
                return element.getStatus();
            }

            @Override
            public boolean isCellEditable(StatusActionUrlMapping statusActionUrlMapping) {
                return !statusActionUrlMapping.isDefault();
            }

            @Override
            public void setValue(StatusActionUrlMapping statusActionUrlMapping, String value) {
                statusActionUrlMapping.setStatus(value);
            }

            @Override
            public @Nullable String getTooltipText() {
                return "Status can't be the same";
            }
        };
        ColumnInfo url = new ElementsColumnInfoBase<StatusActionUrlMapping>("Url") {

            @Override
            public @Nullable String valueOf(StatusActionUrlMapping statusActionUrlMapping) {
                return statusActionUrlMapping.getUrl();
            }

            @Override
            protected @Nullable String getDescription(StatusActionUrlMapping element) {
                return element.getUrl();
            }

            @Override
            public boolean isCellEditable(StatusActionUrlMapping statusActionUrlMapping) {
                return true;
            }

            @Override
            public void setValue(StatusActionUrlMapping statusActionUrlMapping, String value) {
                statusActionUrlMapping.setUrl(HttpUtils.addSchemeIfNoneSpecified(taskRepository, value));
            }

            @Override
            public @Nullable String getTooltipText() {
                return "You can use template variables: " + StringUtil.join(new String[]{ID, SUMMARY, STATUS, RELEASE_DATE, TAG, DESCRIPTION, TaskItemAction.BRANCH, TaskItemAction.REVISION, CUSTOM_FIELD_1, CUSTOM_FIELD_2, CUSTOM_FIELD_3, CUSTOM_FIELD_4, CUSTOM_FIELD_5}, ",");
            }
        };
        ColumnInfo method = new ElementsColumnInfoBase<StatusActionUrlMapping>("Method") {

            @Override
            public int getWidth(JTable table) {
                return 100;
            }

            @Override
            public @Nullable String valueOf(StatusActionUrlMapping statusActionUrlMapping) {
                return statusActionUrlMapping.getMethod().name();
            }

            @Override
            protected @Nullable String getDescription(StatusActionUrlMapping element) {
                return element.getMethod().name();
            }

            @Override
            public boolean isCellEditable(StatusActionUrlMapping statusActionUrlMapping) {
                return true;
            }

            @Override
            public void setValue(StatusActionUrlMapping statusActionUrlMapping, String value) {
                statusActionUrlMapping.setMethod(ActionUrl.HTTPMethod.valueOf(value));
            }

            @Override
            public @Nullable TableCellEditor getEditor(StatusActionUrlMapping statusActionUrlMapping) {
                return new ComboBoxCellEditor() {

                    @Override
                    protected List<String> getComboBoxItems() {
                        List list = new ArrayList();
                        for (ActionUrl.HTTPMethod value : ActionUrl.HTTPMethod.values()) {
                            list.add(value.name());
                        }
                        return list;
                    }
                };
            }
        };
        return new ListTableModel(status, url, method);
    }

    @Override
    protected StatusActionUrlMapping createElement() {
        return new StatusActionUrlMapping("", "", ActionUrl.HTTPMethod.GET);
    }

    @Override
    protected boolean isEmpty(StatusActionUrlMapping element) {
        return StringUtil.isEmpty(element.getStatus()) && StringUtil.isEmpty(element.getUrl());
    }

    @Override
    protected StatusActionUrlMapping cloneElement(StatusActionUrlMapping variable) {
        return variable.clone();
    }

    @Override
    protected boolean canDeleteElement(StatusActionUrlMapping selection) {
        if (selection.getStatus().equals(StatusActionUrlMapping.DEFAULT_STATUS)) {
            return false;
        }
        return true;
    }
}

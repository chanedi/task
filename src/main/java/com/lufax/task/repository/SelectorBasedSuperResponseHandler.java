/*
 * Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package com.lufax.task.repository;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.Task;
import com.intellij.tasks.generic.Selector;
import com.intellij.tasks.impl.TaskUtil;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.xmlb.annotations.XCollection;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Mikhail Golubev
 */
public abstract class SelectorBasedSuperResponseHandler extends SuperResponseHandler {
  private static final Logger LOG = Logger.getInstance(SelectorBasedSuperResponseHandler.class);

  // Supported selector names
  @NonNls protected static final String TASKS = "tasks";

  @NonNls public static final String SUMMARY = "summary";
  @NonNls public static final String STATUS = "status";
  @NonNls public static final String RELEASE_DATE = "releaseDate";
  @NonNls public static final String TAG = "tag";
  @NonNls public static final String DESCRIPTION = "description";
  @NonNls protected static final String ISSUE_URL = "issueUrl";
  @NonNls protected static final String CLOSED = "closed";
  @NonNls protected static final String UPDATED = "updated";
  @NonNls protected static final String CREATED = "created";
  @NonNls public static final String CUSTOM_FIELD_1 = "customField1";
  @NonNls public static final String CUSTOM_FIELD_2 = "customField2";
  @NonNls public static final String CUSTOM_FIELD_3 = "customField3";
  @NonNls public static final String CUSTOM_FIELD_4 = "customField4";
  @NonNls public static final String CUSTOM_FIELD_5 = "customField5";


  @NonNls protected static final String SINGLE_TASK_ID = "singleTask-id";
  @NonNls protected static final String SINGLE_TASK_SUMMARY = "singleTask-summary";
  @NonNls protected static final String SINGLE_TASK_STATUS = "singleTask-status";
  @NonNls protected static final String SINGLE_TASK_RELEASE_DATE = "singleTask-releaseDate";
  @NonNls protected static final String SINGLE_TASK_TAG = "singleTask-tag";
  @NonNls protected static final String SINGLE_TASK_DESCRIPTION = "singleTask-description";
  @NonNls protected static final String SINGLE_TASK_ISSUE_URL = "singleTask-issueUrl";
  @NonNls protected static final String SINGLE_TASK_CLOSED = "singleTask-closed";
  @NonNls protected static final String SINGLE_TASK_UPDATED = "singleTask-updated";
  @NonNls protected static final String SINGLE_TASK_CREATED = "singleTask-created";
  @NonNls protected static final String SINGLE_TASK_CUSTOM_FIELD_1 = "singleTask-customField1";
  @NonNls protected static final String SINGLE_TASK_CUSTOM_FIELD_2 = "singleTask-customField2";
  @NonNls protected static final String SINGLE_TASK_CUSTOM_FIELD_3 = "singleTask-customField3";
  @NonNls protected static final String SINGLE_TASK_CUSTOM_FIELD_4 = "singleTask-customField4";
  @NonNls protected static final String SINGLE_TASK_CUSTOM_FIELD_5 = "singleTask-customField5";

  @NonNls public static final String ID = "id";

  protected LinkedHashMap<String, Selector> mySelectors = new LinkedHashMap<>();

  /**
   * Serialization constructor
   */
  @SuppressWarnings("UnusedDeclaration")
  protected SelectorBasedSuperResponseHandler() {
    // empty
  }

  protected SelectorBasedSuperResponseHandler(SuperGenericRepository repository) {
    super(repository);
    // standard selectors
    setSelectors(ContainerUtil.newArrayList(
      // matched against list of tasks at whole downloaded from "taskListUrl"
      new Selector(TASKS),

      // matched against single tasks extracted from the list downloaded from "taskListUrl"
      new Selector(ID),
      new Selector(SUMMARY),
      new Selector(STATUS),
      new Selector(RELEASE_DATE),
      new Selector(TAG),
      new Selector(DESCRIPTION),
      new Selector(UPDATED),
      new Selector(CREATED),
      new Selector(CLOSED),
      new Selector(ISSUE_URL),
      new Selector(CUSTOM_FIELD_1),
      new Selector(CUSTOM_FIELD_2),
      new Selector(CUSTOM_FIELD_3),
      new Selector(CUSTOM_FIELD_4),
      new Selector(CUSTOM_FIELD_5),

      // matched against single task downloaded from "singleTaskUrl"
      new Selector(SINGLE_TASK_ID),
      new Selector(SINGLE_TASK_SUMMARY),
      new Selector(SINGLE_TASK_STATUS),
      new Selector(SINGLE_TASK_RELEASE_DATE),
      new Selector(SINGLE_TASK_TAG),
      new Selector(SINGLE_TASK_DESCRIPTION),
      new Selector(SINGLE_TASK_UPDATED),
      new Selector(SINGLE_TASK_CREATED),
      new Selector(SINGLE_TASK_CLOSED),
      new Selector(SINGLE_TASK_ISSUE_URL),
      new Selector(SINGLE_TASK_CUSTOM_FIELD_1),
      new Selector(SINGLE_TASK_CUSTOM_FIELD_2),
      new Selector(SINGLE_TASK_CUSTOM_FIELD_3),
      new Selector(SINGLE_TASK_CUSTOM_FIELD_4),
      new Selector(SINGLE_TASK_CUSTOM_FIELD_5)
    ));
  }

  @XCollection(propertyElementName = "selectors")
  @NotNull
  public List<Selector> getSelectors() {
    return new ArrayList<>(mySelectors.values());
  }

  public void setSelectors(@NotNull List<Selector> selectors) {
    mySelectors.clear();
    for (Selector selector : selectors) {
      mySelectors.put(selector.getName(), selector);
    }
  }

  /**
   * Only predefined selectors should be accessed.
   */
  @NotNull
  protected Selector getSelector(@NotNull String name) {
    return mySelectors.get(name);
  }

  @NotNull
  protected String getSelectorPath(@NotNull String name) {
    Selector s = getSelector(name);
    return s.getPath();
  }

  @NotNull
  @Override
  public JComponent getConfigurationComponent(@NotNull Project project) {
    FileType fileType = getResponseType().getSelectorFileType();
    HighlightedSelectorsTable table = new HighlightedSelectorsTable(fileType, project, getSelectors());
    return new JBScrollPane(table);
  }

  @Override
  public SelectorBasedSuperResponseHandler clone() {
    SelectorBasedSuperResponseHandler clone = (SelectorBasedSuperResponseHandler)super.clone();
    clone.mySelectors = new LinkedHashMap<>(mySelectors.size());
    for (Selector selector : mySelectors.values()) {
      clone.mySelectors.put(selector.getName(), selector.clone());
    }
    return clone;
  }

  @Override
  public boolean isConfigured() {
    Selector idSelector = getSelector(ID);
    if (StringUtil.isEmpty(idSelector.getPath())) return false;
    Selector summarySelector = getSelector(SUMMARY);
    if (StringUtil.isEmpty(summarySelector.getPath()) && !myRepository.getDownloadTasksInSeparateRequests()) return false;
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SelectorBasedSuperResponseHandler)) return false;

    SelectorBasedSuperResponseHandler handler = (SelectorBasedSuperResponseHandler)o;

    if (!mySelectors.equals(handler.mySelectors)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return mySelectors.hashCode();
  }

  @Override
  public final Task @NotNull [] parseIssues(@NotNull String response, int max) throws Exception {
    if (StringUtil.isEmpty(getSelectorPath(TASKS)) ||
        StringUtil.isEmpty(getSelectorPath(ID)) ||
        (StringUtil.isEmpty(getSelectorPath(SUMMARY)) && !myRepository.getDownloadTasksInSeparateRequests())) {
      throw new Exception("Selectors 'tasks', 'id' and 'summary' are mandatory");
    }
    List<Object> tasks = selectTasksList(response, max);
    LOG.debug(String.format("Total %d tasks extracted from response", tasks.size()));
    List<Task> result = new ArrayList<>(tasks.size());
    for (Object context : tasks) {
      String id = selectString(getSelector(ID), context);
      SuperGenericTask task;
      if (myRepository.getDownloadTasksInSeparateRequests()) {
        task = new SuperGenericTask(id, "", myRepository);
      }
      else {
        String summary = selectString(getSelector(SUMMARY), context);
        assert id != null && summary != null;
        task = new SuperGenericTask(id, summary, myRepository);
        String description = selectString(getSelector(DESCRIPTION), context);
        if (description != null) {
          task.setDescription(description);
        }
        String status = selectString(getSelector(STATUS), context);
        if (status != null) {
          task.setStatus(status);
        }
        String releaseDate = selectString(getSelector(RELEASE_DATE), context);
        if (releaseDate != null) {
          task.setReleaseDate(releaseDate);
        }
        String tag = selectString(getSelector(TAG), context);
        if (tag != null) {
          task.setTag(tag);
        }
        String customField1 = selectString(getSelector(CUSTOM_FIELD_1), context);
        if (customField1 != null) {
          task.setCustomField1(customField1);
        }
        String customField2 = selectString(getSelector(CUSTOM_FIELD_2), context);
        if (customField2 != null) {
          task.setCustomField2(customField2);
        }
        String customField3 = selectString(getSelector(CUSTOM_FIELD_3), context);
        if (customField3 != null) {
          task.setCustomField3(customField3);
        }
        String customField4 = selectString(getSelector(CUSTOM_FIELD_4), context);
        if (customField4 != null) {
          task.setCustomField4(customField4);
        }
        String customField5 = selectString(getSelector(CUSTOM_FIELD_5), context);
        if (customField5 != null) {
          task.setCustomField5(customField5);
        }
        String issueUrl = selectString(getSelector(ISSUE_URL), context);
        if (issueUrl != null) {
          task.setIssueUrl(issueUrl);
        }
        Boolean closed = selectBoolean(getSelector(CLOSED), context);
        if (closed != null) {
          task.setClosed(closed);
        }
        Date updated = selectDate(getSelector(UPDATED), context);
        if (updated != null) {
          task.setUpdated(updated);
        }
        Date created = selectDate(getSelector(CREATED), context);
        if (created != null) {
          task.setCreated(created);
        }
      }
      result.add(task);
    }
    return result.toArray(Task.EMPTY_ARRAY);
  }

  @Nullable
  private Date selectDate(@NotNull Selector selector, @NotNull Object context) throws Exception {
    String s = selectString(selector, context);
    if (s == null) {
      return null;
    }
    return TaskUtil.parseDate(s);
  }

  @Nullable
  protected Boolean selectBoolean(@NotNull Selector selector, @NotNull Object context) throws Exception {
    String s = selectString(selector, context);
    if (s == null) {
      return null;
    }
    s = StringUtil.toLowerCase(s.trim());
    if (s.equals("true")) {
      return true;
    }
    else if (s.equals("false")) {
      return false;
    }
    throw new Exception(
      String.format("Expression '%s' should match boolean value. Got '%s' instead", selector.getName(), s));
  }

  @NotNull
  protected abstract List<Object> selectTasksList(@NotNull String response, int max) throws Exception;

  @Nullable
  protected abstract @Nls String selectString(@NotNull Selector selector, @NotNull Object context) throws Exception;

  @Nullable
  @Override
  public final Task parseIssue(@NotNull String response) throws Exception {
    if (StringUtil.isEmpty(getSelectorPath(SINGLE_TASK_ID)) ||
        StringUtil.isEmpty(getSelectorPath(SINGLE_TASK_SUMMARY))) {
      throw new Exception("Selectors 'singleTask-id' and 'singleTask-summary' are mandatory");
    }
    String id = selectString(getSelector(SINGLE_TASK_ID), response);
    String summary = selectString(getSelector(SINGLE_TASK_SUMMARY), response);
    assert id != null && summary != null;
    SuperGenericTask task = new SuperGenericTask(id, summary, myRepository);
    String description = selectString(getSelector(SINGLE_TASK_DESCRIPTION), response);
    if (description != null) {
      task.setDescription(description);
    }
    String status = selectString(getSelector(SINGLE_TASK_STATUS), response);
    if (status != null) {
      task.setStatus(status);
    }
    String releaseDate = selectString(getSelector(SINGLE_TASK_RELEASE_DATE), response);
    if (releaseDate != null) {
      task.setReleaseDate(releaseDate);
    }
    String tag = selectString(getSelector(SINGLE_TASK_TAG), response);
    if (tag != null) {
      task.setTag(tag);
    }
    String customField1 = selectString(getSelector(SINGLE_TASK_CUSTOM_FIELD_1), response);
    if (customField1 != null) {
      task.setCustomField1(customField1);
    }
    String customField2 = selectString(getSelector(SINGLE_TASK_CUSTOM_FIELD_2), response);
    if (customField2 != null) {
      task.setCustomField2(customField2);
    }
    String customField3 = selectString(getSelector(SINGLE_TASK_CUSTOM_FIELD_3), response);
    if (customField3 != null) {
      task.setCustomField3(customField3);
    }
    String customField4 = selectString(getSelector(SINGLE_TASK_CUSTOM_FIELD_4), response);
    if (customField4 != null) {
      task.setCustomField4(customField4);
    }
    String customField5 = selectString(getSelector(SINGLE_TASK_CUSTOM_FIELD_5), response);
    if (customField5 != null) {
      task.setCustomField5(customField5);
    }
    String issueUrl = selectString(getSelector(SINGLE_TASK_ISSUE_URL), response);
    if (issueUrl != null) {
      task.setIssueUrl(issueUrl);
    }
    Boolean closed = selectBoolean(getSelector(SINGLE_TASK_CLOSED), response);
    if (closed != null) {
      task.setClosed(closed);
    }
    Date updated = selectDate(getSelector(SINGLE_TASK_UPDATED), response);
    if (updated != null) {
      task.setUpdated(updated);
    }
    Date created = selectDate(getSelector(SINGLE_TASK_CREATED), response);
    if (created != null) {
      task.setCreated(created);
    }
    return task;
  }
}

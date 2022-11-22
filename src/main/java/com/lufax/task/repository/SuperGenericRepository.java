// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.lufax.task.repository;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskBundle;
import com.intellij.tasks.TaskRepositorySubtype;
import com.intellij.tasks.TaskRepositoryType;
import com.intellij.tasks.generic.ResponseType;
import com.intellij.tasks.generic.TemplateVariable;
import com.intellij.tasks.impl.httpclient.NewBaseRepositoryImpl;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.net.HTTPMethod;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.XCollection;
import com.lufax.task.ProcessNeedResultException;
import com.lufax.task.utils.HttpUtils;
import com.lufax.task.utils.StringUtils;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.intellij.tasks.generic.GenericRepositoryUtil.concat;
import static com.intellij.tasks.generic.TemplateVariable.FactoryVariable;

/**
 * @author Evgeny.Zakrevsky
 */
@Tag("SuperGeneric")
public class SuperGenericRepository extends NewBaseRepositoryImpl {

    private static final Logger LOG = Logger.getInstance(SuperGenericRepository.class);

    @NonNls public static final String SERVER_URL = "serverUrl";
    @NonNls public static final String USERNAME = "username";
    @NonNls public static final String PASSWORD = "password";

    private final FactoryVariable myServerTemplateVariable = new FactoryVariable(SERVER_URL) {
        @NotNull
        @Override
        public String getValue() {
            return SuperGenericRepository.this.getUrl();
        }
    };
    private final FactoryVariable myUserNameTemplateVariable = new FactoryVariable(USERNAME) {
        @NotNull
        @Override
        public String getValue() {
            return SuperGenericRepository.this.getUsername();
        }
    };
    private final FactoryVariable myPasswordTemplateVariable = new FactoryVariable(PASSWORD, true) {
        @NotNull
        @Override
        public String getValue() {
            return SuperGenericRepository.this.getPassword();
        }
    };

    private final List<FactoryVariable> myPredefinedTemplateVariables = Arrays.asList(myServerTemplateVariable,
            myUserNameTemplateVariable,
            myPasswordTemplateVariable);
    @Attribute
    private String id;
    private String myLoginURL = "";
    @Attribute("loginWithTokenURL")
    private String myLoginWithTokenURL = "";
    private String myTasksListUrl = "";
    private String mySingleTaskUrl;
    @Attribute
    private String loginSuccessCookieName;

    private HTTPMethod myLoginMethodType = HTTPMethod.GET;
    @Attribute("loginWithTokenMethodType")
    private HTTPMethod myLoginWithTokenMethodType = HTTPMethod.GET;
    private HTTPMethod myTasksListMethodType = HTTPMethod.GET;
    private HTTPMethod mySingleTaskMethodType = HTTPMethod.GET;

    private ResponseType myResponseType = ResponseType.JSON;

    private EnumMap<ResponseType, SuperResponseHandler> myResponseHandlersMap = new EnumMap<>(ResponseType.class);

    private List<TemplateVariable> myTemplateVariables = new ArrayList<>();

    private String mySubtypeName;
    private boolean myDownloadTasksInSeparateRequests;

    /**
     * Serialization constructor
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public SuperGenericRepository() {
        resetToDefaults();
    }

    public SuperGenericRepository(final TaskRepositoryType type) {
        super(type);
        resetToDefaults();
    }

    /**
     * Cloning constructor
     */
    public SuperGenericRepository(final SuperGenericRepository other) {
        super(other);
        id = other.getId();
        myLoginURL = other.getLoginUrl();
        myLoginWithTokenURL = other.getLoginWithTokenUrl();
        loginSuccessCookieName = other.getLoginSuccessCookieName();
        myTasksListUrl = other.getTasksListUrl();
        mySingleTaskUrl = other.getSingleTaskUrl();

        myLoginMethodType = other.getLoginMethodType();
        myLoginWithTokenMethodType = other.getLoginWithTokenMethodType();
        myTasksListMethodType = other.getTasksListMethodType();
        mySingleTaskMethodType = other.getSingleTaskMethodType();

        myResponseType = other.getResponseType();
        myTemplateVariables = other.getTemplateVariables();
        mySubtypeName = other.getSubtypeName();
        myDownloadTasksInSeparateRequests = other.getDownloadTasksInSeparateRequests();
        myResponseHandlersMap = new EnumMap<>(ResponseType.class);
        for (Map.Entry<ResponseType, SuperResponseHandler> e : other.myResponseHandlersMap.entrySet()) {
            SuperResponseHandler handler = e.getValue().clone();
            handler.setRepository(this);
            myResponseHandlersMap.put(e.getKey(), handler);
        }
    }

    public void resetToDefaults() {
        myLoginURL = "";
        myLoginWithTokenURL = "";
        loginSuccessCookieName = "";
        myTasksListUrl = "";
        mySingleTaskUrl = "";
        myDownloadTasksInSeparateRequests = false;
        myLoginMethodType = HTTPMethod.GET;
        myLoginWithTokenMethodType = HTTPMethod.GET;
        myTasksListMethodType = HTTPMethod.GET;
        mySingleTaskMethodType = HTTPMethod.GET;
        myResponseType = ResponseType.JSON;
        myTemplateVariables = new ArrayList<>();
        myResponseHandlersMap = new EnumMap<>(ResponseType.class);
        myResponseHandlersMap.put(ResponseType.XML, getXmlResponseHandlerDefault());
        myResponseHandlersMap.put(ResponseType.JSON, getJsonResponseHandlerDefault());
        myResponseHandlersMap.put(ResponseType.TEXT, getTextResponseHandlerDefault());
    }

    @NotNull
    @Override
    public SuperGenericRepository clone() {
        return new SuperGenericRepository(this);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof SuperGenericRepository)) return false;
        SuperGenericRepository that = (SuperGenericRepository)o;
        if (!Objects.equals(getId(), that.getId())) return false;
        return true;
    }

    @Override
    public boolean isConfigured() {
        if (!super.isConfigured()) return false;
        for (TemplateVariable variable : getTemplateVariables()) {
            if (variable.isShownOnFirstTab() && StringUtil.isEmpty(variable.getValue())) {
                return false;
            }
        }
        return StringUtil.isNotEmpty(myTasksListUrl) && getActiveResponseHandler().isConfigured();
    }

    @Override
    public Task[] getIssues(@Nullable String query, int offset, int limit, boolean withClosed) throws Exception {
        if (StringUtil.isEmpty(myTasksListUrl)) {
            throw new Exception("'Task list URL' configuration parameter is mandatory");
        }
        if (!isLoginAnonymously() && !isUseHttpAuthentication()) {
            HttpUtils.executeRequest(getHttpClient(), getLoginUrl(), myLoginMethodType, getAllTemplateVariables());
        }
        List<TemplateVariable> variables = concat(getAllTemplateVariables(),
                new TemplateVariable("offset", String.valueOf(offset)),
                new TemplateVariable("limit", String.valueOf(limit)));
        String responseBody = HttpUtils.executeRequest(getHttpClient(), getTasksListUrl(), myTasksListMethodType, variables);
        Task[] tasks = getActiveResponseHandler().parseIssues(responseBody, limit);
        if (myResponseType == ResponseType.TEXT) {
            return tasks;
        }
        if (StringUtil.isNotEmpty(mySingleTaskUrl) && myDownloadTasksInSeparateRequests) {
            for (int i = 0; i < tasks.length; i++) {
                tasks[i] = findTask(tasks[i].getId());
            }
        }
        return tasks;
    }

    @Nullable
    @Override
    public Task findTask(@NotNull final String id) throws Exception {
        List<TemplateVariable> variables = concat(getAllTemplateVariables(), new TemplateVariable("id", id));
        return getActiveResponseHandler().parseIssue(HttpUtils.executeRequest(getHttpClient(), getSingleTaskUrl(), mySingleTaskMethodType, variables));
    }

    @Nullable
    @Override
    public CancellableConnection createCancellableConnection() {
        return new CancellableConnection() {
            @Override
            protected void doTest() throws Exception {
                getIssues("", 1, 0, false);
            }

            @Override
            public void cancel() {
            }
        };
    }

    public boolean testLoginConnection(@NotNull Project project) {
        @Nullable CancellableConnection myConnection = new CancellableConnection() {
            @Override
            protected void doTest() {
                String result = null;
                try {
                    result = HttpUtils.executeRequest(getHttpClient(), getLoginUrl(), myLoginMethodType, getAllTemplateVariables());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                throw new ProcessNeedResultException(result);
            }

            @Override
            public void cancel() {

            }
        };

        return tryConnection(project, myConnection);
    }

    public boolean testTaskConnection(@NotNull Project project) {
        @Nullable CancellableConnection myConnection = new CancellableConnection() {
            @Override
            protected void doTest() throws Exception {
//                s_executeMethod(getLoginMethod());
//                String result = getIssues("", 0, 1, false);
//                throw new ProcessNeedResultException(result);
            }

            @Override
            public void cancel() {
            }
        };

        return tryConnection(project, myConnection);
    }

    public boolean testSingleTaskConnection(@NotNull Project project) {
        @Nullable CancellableConnection myConnection = new CancellableConnection() {
            @Override
            protected void doTest() throws Exception {
//                s_executeMethod(getLoginMethod());
//                String result = s_executeMethod(getTaskListMethod(1, 0));
//                throw new ProcessNeedResultException(result);
            }

            @Override
            public void cancel() {
            }
        };

        return tryConnection(project, myConnection);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLoginUrl(final String loginUrl) {
        myLoginURL = loginUrl;
    }

    public void setLoginWithTokenUrl(final String loginWithTokenUrl) {
        myLoginWithTokenURL = loginWithTokenUrl;
    }

    public void setLoginSuccessCookieName(String loginSuccessCookieName) {
        this.loginSuccessCookieName = loginSuccessCookieName;
    }

    public void setTasksListUrl(final String tasksListUrl) {
        myTasksListUrl = tasksListUrl;
    }

    public void setSingleTaskUrl(String singleTaskUrl) {
        mySingleTaskUrl = singleTaskUrl;
    }

    public String getLoginUrl() {
        return myLoginURL;
    }

    public String getLoginWithTokenUrl() {
        return myLoginWithTokenURL;
    }

    public String getLoginSuccessCookieName() {
        return loginSuccessCookieName;
    }

    public String getTasksListUrl() {
        return myTasksListUrl;
    }

    public String getSingleTaskUrl() {
        return mySingleTaskUrl;
    }

    public void setLoginMethodType(final HTTPMethod loginMethodType) {
        myLoginMethodType = loginMethodType;
    }

    public void setLoginWithTokenMethodType(final HTTPMethod loginWithTokenMethodType) {
        myLoginWithTokenMethodType = loginWithTokenMethodType;
    }


    public void setTasksListMethodType(final HTTPMethod tasksListMethodType) {
        myTasksListMethodType = tasksListMethodType;
    }

    public void setSingleTaskMethodType(HTTPMethod singleTaskMethodType) {
        mySingleTaskMethodType = singleTaskMethodType;
    }

    public HTTPMethod getLoginMethodType() {
        return myLoginMethodType;
    }

    public HTTPMethod getLoginWithTokenMethodType() {
        return myLoginWithTokenMethodType;
    }

    public HTTPMethod getTasksListMethodType() {
        return myTasksListMethodType;
    }

    public HTTPMethod getSingleTaskMethodType() {
        return mySingleTaskMethodType;
    }

    public ResponseType getResponseType() {
        return myResponseType;
    }

    public void setResponseType(final ResponseType responseType) {
        myResponseType = responseType;
    }

    public List<TemplateVariable> getTemplateVariables() {
        return myTemplateVariables;
    }

    /**
     * Returns all template variables including both predefined and defined by user
     */
    public List<TemplateVariable> getAllTemplateVariables() {
        return ContainerUtil.concat(myPredefinedTemplateVariables, getTemplateVariables());
    }

    public void setTemplateVariables(final List<TemplateVariable> templateVariables) {
        myTemplateVariables = templateVariables;
    }

    @Override
    public Icon getIcon() {
        if (mySubtypeName == null) {
            return super.getIcon();
        }
        @SuppressWarnings("unchecked")
        List<TaskRepositorySubtype> subtypes = getRepositoryType().getAvailableSubtypes();
        for (TaskRepositorySubtype s : subtypes) {
            if (mySubtypeName.equals(s.getName())) {
                return s.getIcon();
            }
        }
        throw new AssertionError("Unknown repository subtype");
    }

    @Override
    protected int getFeatures() {
        return LOGIN_ANONYMOUSLY | BASIC_HTTP_AUTHORIZATION;
    }

    public SuperResponseHandler getResponseHandler(ResponseType type) {
        return myResponseHandlersMap.get(type);
    }

    public SuperResponseHandler getActiveResponseHandler() {
        return myResponseHandlersMap.get(myResponseType);
    }

    @XCollection(
            elementTypes = {
                    XPathSuperResponseHandler.class,
                    JsonPathSuperResponseHandler.class,
                    RegExSuperResponseHandler.class
            }
    )
    public List<SuperResponseHandler> getResponseHandlers() {
        if (myResponseHandlersMap.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(myResponseHandlersMap.values()));
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setResponseHandlers(List<SuperResponseHandler> responseHandlers) {
        myResponseHandlersMap.clear();
        for (SuperResponseHandler handler : responseHandlers) {
            myResponseHandlersMap.put(handler.getResponseType(), handler);
        }
        // ResponseHandler#repository field is excluded from serialization to prevent
        // circular dependency so it has to be done manually during serialization process
        for (SuperResponseHandler handler : myResponseHandlersMap.values()) {
            handler.setRepository(this);
        }
    }

    public SuperResponseHandler getXmlResponseHandlerDefault() {
        return new XPathSuperResponseHandler(this);
    }

    public SuperResponseHandler getJsonResponseHandlerDefault() {
        return new JsonPathSuperResponseHandler(this);
    }

    public SuperResponseHandler getTextResponseHandlerDefault() {
        return new RegExSuperResponseHandler(this);
    }

    public String getSubtypeName() {
        return mySubtypeName;
    }

    public void setSubtypeName(String subtypeName) {
        mySubtypeName = subtypeName;
    }

    public boolean getDownloadTasksInSeparateRequests() {
        return myDownloadTasksInSeparateRequests;
    }

    public void setDownloadTasksInSeparateRequests(boolean downloadTasksInSeparateRequests) {
        myDownloadTasksInSeparateRequests = downloadTasksInSeparateRequests;
    }

    public boolean tryConnection(@NotNull Project project, @Nullable CancellableConnection myConnection) {
        TestConnectionTask task = new TestConnectionTask(project, TaskBundle.message("dialog.title.test.connection"), true) {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setText(TaskBundle.message("progress.text.connecting.to", getUrl()));
                indicator.setFraction(0);
                indicator.setIndeterminate(true);
                try {
                    Future<Exception> future = ApplicationManager.getApplication().executeOnPooledThread(myConnection);
                    while (true) {
                        try {
                            myException = future.get(100, TimeUnit.MILLISECONDS);
                            return;
                        }
                        catch (TimeoutException ignore) {
                            try {
                                indicator.checkCanceled();
                            }
                            catch (ProcessCanceledException e) {
                                myException = e;
                                myConnection.cancel();
                                return;
                            }
                        }
                        catch (Exception e) {
                            myException = e;
                            return;
                        }
                    }
                }
                catch (Exception e) {
                    myException = e;
                }
            }

        };
        ProgressManager.getInstance().run(task);
        Exception e = task.myException;
        if (e == null) {
            Messages.showMessageDialog(project, TaskBundle.message("dialog.message.connection.successful"),
                    TaskBundle.message("dialog.title.connection"), Messages.getInformationIcon());
        }
        else if (e instanceof ProcessNeedResultException) {
            String message = e.getMessage();
            Messages.showMessageDialog(project, TaskBundle.message("dialog.message.connection.successful") + ". Returned message is: \n\n" + StringUtils.unicodeDecode(message),
                    TaskBundle.message("dialog.title.connection"), Messages.getInformationIcon());
        }
        else if (!(e instanceof ProcessCanceledException)) {
            String message = e.getMessage();
            if (e instanceof UnknownHostException) {
                message = TaskBundle.message("dialog.message.unknown.host", message);
            }
            if (message == null) {
                LOG.error(e);
                message = TaskBundle.message("dialog.message.unknown.error");
            }
            Messages.showErrorDialog(project, StringUtil.capitalize(message), TaskBundle.message("dialog.title.error"));
        }
        return e == null;
    }

    private abstract class TestConnectionTask extends com.intellij.openapi.progress.Task.Modal {
        protected Exception myException;

        public TestConnectionTask(@Nullable Project project, @NlsContexts.DialogTitle @NotNull String title, boolean canBeCancelled) {
            super(project, title, canBeCancelled);
        }
    }
}

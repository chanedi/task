// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.lufax.task.repository;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.io.StreamUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tasks.Task;
import com.intellij.tasks.TaskRepositorySubtype;
import com.intellij.tasks.TaskRepositoryType;
import com.intellij.tasks.generic.ResponseType;
import com.intellij.tasks.generic.TemplateVariable;
import com.intellij.tasks.impl.BaseRepositoryImpl;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.net.HTTPMethod;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.XCollection;
import com.lufax.task.NeedDynamicTokenException;
import com.lufax.task.ProcessNeedResultException;
import com.lufax.task.toolwindow.TaskUpdateConfig;
import com.lufax.task.utils.HttpUtils;
import com.lufax.task.utils.StringUtils;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpecBase;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.intellij.tasks.generic.GenericRepositoryUtil.concat;
import static com.intellij.tasks.generic.TemplateVariable.FactoryVariable;
import static com.lufax.task.utils.HttpUtils.substituteTemplateVariables;

/**
 * @author Evgeny.Zakrevsky
 */
@Tag("SuperGeneric")
public class SuperGenericRepository extends BaseRepositoryImpl {

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
    @Attribute("id")
    private String id;
    private String myLoginURL = "";
    private String myLoginWithTokenURL = "";
    private String myTasksListUrl = "";
    private String mySingleTaskUrl;
    private String myLoginSuccessCookieName;
    private TaskUpdateConfig myUpdateConfig;
    private String myCookiePolicy;

    private HTTPMethod myLoginMethodType = HTTPMethod.GET;
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
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    public SuperGenericRepository(final TaskRepositoryType type) {
        super(type);
        resetToDefaults();
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    /**
     * Cloning constructor
     */
    public SuperGenericRepository(final SuperGenericRepository other) {
        super(other);
        id = other.getId();
        myLoginURL = other.getLoginUrl();
        myLoginWithTokenURL = other.getLoginWithTokenUrl();
        myLoginSuccessCookieName = other.getLoginSuccessCookieName();
        myCookiePolicy = other.getCookiePolicy();
        myTasksListUrl = other.getTasksListUrl();
        mySingleTaskUrl = other.getSingleTaskUrl();
        myUpdateConfig = other.getUpdateConfig();

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
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    public void resetToDefaults() {
        myCookiePolicy = CookiePolicy.DEFAULT;
        myLoginURL = "";
        myLoginWithTokenURL = "";
        myLoginSuccessCookieName = "";
        myUpdateConfig = new TaskUpdateConfig();
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

    static {
        CookiePolicy.registerCookieSpec(CookiePolicy.DEFAULT, CookieSpecBase.class);
    }

    @Override
    protected void configureHttpClient(HttpClient client) {
        super.configureHttpClient(client);
        client.getParams().setCookiePolicy(myCookiePolicy);
    }

    @NotNull
    @Override
    public SuperGenericRepository clone() {
        return new SuperGenericRepository(this);
    }

    @Override
    public boolean equals(final Object o) { // equals时不会更新xml文件，编辑无法保存
        if (this == o) return true;
        if (!(o instanceof SuperGenericRepository)) return false;
        if (!super.equals(o)) return false;
        SuperGenericRepository that = (SuperGenericRepository)o;
        if (!Objects.equals(getId(), that.getId())) return false;
        if (!Objects.equals(getLoginUrl(), that.getLoginUrl())) return false;
        if (!Objects.equals(getLoginWithTokenUrl(), that.getLoginWithTokenUrl())) return false;
        if (!Objects.equals(getTasksListUrl(), that.getTasksListUrl())) return false;
        if (!Objects.equals(getSingleTaskUrl(), that.getSingleTaskUrl())) return false;
        if (!Objects.equals(getLoginSuccessCookieName(), that.getLoginSuccessCookieName())) return false;
        if (!Objects.equals(getCookiePolicy(), that.getCookiePolicy())) return false;
        if (!Comparing.equal(getLoginMethodType(), that.getLoginMethodType())) return false;
        if (!Comparing.equal(getLoginWithTokenMethodType(), that.getLoginWithTokenMethodType())) return false;
        if (!Comparing.equal(getTasksListMethodType(), that.getTasksListMethodType())) return false;
        if (!Comparing.equal(getSingleTaskMethodType(), that.getSingleTaskMethodType())) return false;
        if (!Comparing.equal(getResponseType(), that.getResponseType())) return false;
        if (!Comparing.equal(getTemplateVariables(), that.getTemplateVariables())) return false;
        if (!Comparing.equal(getResponseHandlers(), that.getResponseHandlers())) return false;
        if (!Comparing.equal(getDownloadTasksInSeparateRequests(), that.getDownloadTasksInSeparateRequests())) return false;
        if (!Comparing.equal(getUpdateConfig(), that.getUpdateConfig())) return false;
        return true;
    }

    public boolean idEquals(final Object o) {
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
    public Task[] getIssues(@Nullable final String query, final int max, final long since) throws Exception {
        if (StringUtil.isEmpty(myTasksListUrl)) {
            throw new Exception("'Task list URL' configuration parameter is mandatory");
        }
        if (!isLoginAnonymously() && !isUseHttpAuthentication()) {
            executeMethod(getLoginMethod(true));
        }
        String responseBody = executeMethod(getTaskListMethod(max, since));
        Task[] tasks = getActiveResponseHandler().parseIssues(responseBody, max);
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

    private String executeMethod(HttpMethod method) throws Exception {
        String responseBody;
        try {
            getHttpClient().executeMethod(method);
        } catch (NeedDynamicTokenException e) {
            executeMethod(getLoginWithTokenMethod(getLoginWithTokenTemplateVariables()));
        }
        Header contentType = method.getResponseHeader("Content-Type");
        if (contentType != null && contentType.getValue().contains("charset")) {
            // ISO-8859-1 if charset wasn't specified in response
            responseBody = StringUtil.notNullize(method.getResponseBodyAsString());
        }
        else {
            InputStream stream = method.getResponseBodyAsStream();
            responseBody = stream == null ? "" : StreamUtil.readText(stream, StandardCharsets.UTF_8);
        }
        LOG.info("responseBody:" + responseBody);
        if (method.getStatusCode() != HttpStatus.SC_OK) {
            throw new Exception("Request failed with HTTP error: [" + method.getStatusCode() + "]" + method.getStatusText());
        }
        return responseBody;
    }

    public HttpMethod getHttpMethod(String requestUrl, HTTPMethod type, List<TemplateVariable> requestTemplateVariables) {
        return HttpUtils.getHttpMethod(this, requestUrl, type, requestTemplateVariables);
    }

    public HttpMethod getLoginMethod(boolean needCheckCookieName) {
        try {
            if (getLoginMethodType() == HTTPMethod.GET) {
                return new GetMethod(substituteTemplateVariables(getLoginUrl(), getAllTemplateVariables())) {

                    @Override
                    protected void processResponseBody(HttpState state, HttpConnection conn) {
                        super.processResponseBody(state, conn);
                        checkCookie(state, needCheckCookieName);
                    }
                };
            } else {
                int n = getLoginUrl().indexOf('?');
                String url = n == -1 ? getLoginUrl() : getLoginUrl().substring(0, n);
                PostMethod method = new PostMethod(substituteTemplateVariables(url, getAllTemplateVariables())) {
                    @Override
                    protected void processResponseBody(HttpState state, HttpConnection conn) {
                        super.processResponseBody(state, conn);
                        checkCookie(state, needCheckCookieName);
                    }
                };
                String[] queryParams = getLoginUrl().substring(n + 1).split("&");
                method.addParameters(ContainerUtil.map2Array(queryParams, NameValuePair.class, s -> {
                    String[] nv = s.split("=");
                    try {
                        if (nv.length == 1) {
                            return new NameValuePair(substituteTemplateVariables(nv[0], getAllTemplateVariables(), false), "");
                        }
                        return new NameValuePair(substituteTemplateVariables(nv[0], getAllTemplateVariables(), false), substituteTemplateVariables(nv[1], getAllTemplateVariables(), false));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }));
                return method;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkCookie(HttpState state, boolean needCheckCookieName) {
        if (StringUtil.isEmpty(getLoginSuccessCookieName()) || !needCheckCookieName) {
            return;
        }
        LOG.info("checkCookieName: " + getLoginSuccessCookieName() + "cookies" + state.getCookies().toString());
        for (Cookie cookie : state.getCookies()) {
            if (cookie.getName().equals(getLoginSuccessCookieName())) {
                return;
            }
        }
        throw new NeedDynamicTokenException();
    }

    public HttpMethod getLoginWithTokenMethod(List<TemplateVariable> variables) {
        return getHttpMethod(getLoginWithTokenUrl(), getLoginWithTokenMethodType(), variables);
    }

    public HttpMethod getTaskListMethod(final int max, final long since) {
        List<TemplateVariable> variables = concat(getAllTemplateVariables(),
                new TemplateVariable("max", String.valueOf(max)),
                new TemplateVariable("since", String.valueOf(since)));
        return getHttpMethod(getTasksListUrl(), getTasksListMethodType(), variables);
    }

    public HttpMethod getSingleTaskMethod(@NotNull final String id) {
        List<TemplateVariable> variables = concat(getAllTemplateVariables(), new TemplateVariable("id", id));
        return getHttpMethod(getSingleTaskUrl(), getSingleTaskMethodType(), variables);
    }

    @Nullable
    @Override
    public Task findTask(@NotNull final String id) throws Exception {
        return getActiveResponseHandler().parseIssue(executeMethod(getSingleTaskMethod(id)));
    }

    @Nullable
    @Override
    public CancellableConnection createCancellableConnection() {
        return new CancellableConnection() {
            @Override
            protected void doTest() throws Exception {
                getIssues("", 1, 0);
            }

            @Override
            public void cancel() {
            }
        };
    }

    public boolean testLoginConnection(@NotNull Project project) {
        @Nullable CancellableConnection myConnection = new CancellableConnection() {
            @Override
            protected void doTest() throws Exception {
                String result = executeMethod(getLoginMethod(false));
                throw new ProcessNeedResultException(result);
            }

            @Override
            public void cancel() {

            }
        };

        return tryConnection(project, myConnection);
    }

    public boolean testLoginWithTokenConnection(@NotNull Project project) {
        List<TemplateVariable> variables = getLoginWithTokenTemplateVariables();
        @Nullable CancellableConnection myConnection = new CancellableConnection() {
            @Override
            protected void doTest() throws Exception {
                String result = executeMethod(getLoginWithTokenMethod(variables));
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
                String result = executeMethod(getTaskListMethod(1, 0));
                throw new ProcessNeedResultException(result);
            }

            @Override
            public void cancel() {
            }
        };

        return tryConnection(project, myConnection);
    }

    @Override
    public void setUrl(String url) {
        super.setUrl(url);
        if (StringUtil.isEmpty(myUpdateConfig.getName())) {
            myUpdateConfig.setName(url);
        }
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
        this.myLoginSuccessCookieName = loginSuccessCookieName;
    }

    public String getCookiePolicy() {
        return myCookiePolicy;
    }

    public void setCookiePolicy(String cookiePolicy) {
        this.myCookiePolicy = cookiePolicy;
    }

    public TaskUpdateConfig getUpdateConfig() {
        return myUpdateConfig;
    }

    public void setUpdateConfig(TaskUpdateConfig updateConfig) {
        this.myUpdateConfig = updateConfig;
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
        return myLoginSuccessCookieName;
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
        TestConnectionTask task = new TestConnectionTask(project, "Test connection", true) {

            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setText("Connecting to " + getUrl() + "...");
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
            Messages.showMessageDialog(project, "Connection is successful", "Connection", Messages.getInformationIcon());
        }
        else if (e instanceof ProcessNeedResultException) {
            String message = e.getMessage();
            Messages.showMessageDialog(project, "Connection is successful. Returned message is: \n\n" + StringUtils.unicodeDecode(message),
                    "Connection", Messages.getInformationIcon());
        }
        else if (!(e instanceof ProcessCanceledException)) {
            String message = e.getMessage();
            if (e instanceof UnknownHostException) {
                message = "Unknown host: " + message;
            }
            if (message == null) {
                LOG.error(e);
                message = "Unknown error";
            }
            Messages.showErrorDialog(project, StringUtil.capitalize(message), "Error");
        }
        return e == null;
    }

    private List<TemplateVariable> getLoginWithTokenTemplateVariables() {
        List<TemplateVariable> variables = getAllTemplateVariables();
        if (myLoginWithTokenURL.contains("{dynamicToken}")) {
            String dynamicToken = Messages.showInputDialog("Please input dynamic token for login:", "Need Login With Token", null);
            variables = concat(getAllTemplateVariables(),
                    new TemplateVariable("dynamicToken", dynamicToken));
        }
        return variables;
    }

    private abstract class TestConnectionTask extends com.intellij.openapi.progress.Task.Modal {
        protected Exception myException;

        public TestConnectionTask(@Nullable Project project, @NotNull String title, boolean canBeCancelled) {
            super(project, title, canBeCancelled);
        }
    }
}

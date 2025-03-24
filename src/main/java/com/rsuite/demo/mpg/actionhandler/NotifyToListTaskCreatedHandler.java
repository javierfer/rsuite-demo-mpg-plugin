package com.rsuite.demo.mpg.actionhandler;

import org.apache.commons.lang3.StringUtils;

import com.reallysi.rsuite.api.User;
import com.reallysi.rsuite.api.workflow.activiti.BaseTaskListener;
import com.reallysi.rsuite.api.workflow.activiti.WorkflowContext;
import com.reallysi.rsuite.api.workflow.activiti.WorkflowTask;
import com.reallysi.rsuite.service.AuthorizationService;
import com.reallysi.rsuite.service.MailService;

public class NotifyToListTaskCreatedHandler extends BaseTaskListener {

    @Override
    public void execute(WorkflowContext context) throws Exception {
        WorkflowTask task = context.getTask();
        String notificationList = context.getWorkflowVariables().getOrDefault("notification-list", "")
                .toString();

        if (StringUtils.isBlank(notificationList))
            return;

        MailService mailService = context.getMailService();
        AuthorizationService authorizationService = context.getAuthorizationService();

        String[] usersList = notificationList.split(",");

        for (String userId : usersList) {
            if (!authorizationService.hasUser(userId))
                return;

            String subject = buildSubject(context, task);
            String content = buildMessage(context, task);

            User userNotif = authorizationService.findUser(userId);

            String from = context.getConfigurationProperties().getProperty("rsuite.mail.sender.emailid", "rsuite.notifications@noreply.org");
            mailService.send(userNotif.getEmail(),
                    from,
                    subject,
                    content.toString(),
                    new String[] {});
        }
    }

    protected String buildSubject(WorkflowContext context, WorkflowTask task) {
        return "Task Created Notification";
    }

    protected String buildMessage(WorkflowContext context, WorkflowTask task) {
        StringBuilder content = new StringBuilder();

        String href = context.getRSuiteServerConfiguration().getProtocol() + "://"
                + context.getRSuiteServerConfiguration().getHostName() + ":"
                + context.getRSuiteServerConfiguration().getPort()
                + "/rsuite-cms/tasks/"
                + task.getId();

        String assignee = StringUtils.defaultString(task.getAssigneeUserId(), "the pool");
        content.append("The task ")
                .append(task.getName())
                .append(" from ")
                .append(context.getWorkflowVariables().getOrDefault("processDefinitionName", ""))
                .append(": ")
                .append(context.getWorkflowVariables().getOrDefault("workflow-title", ""))
                .append(" has been created and assigned to ")
                .append(assignee)
                .append(".")
                .append("\n\n")
                .append(href)
                .append("\n\n")
                .append("Sincerely,\n")
                .append("RSuite Enterprise");
        return content.toString();
    }
}

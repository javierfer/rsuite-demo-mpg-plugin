package com.rsuite.demo.mpg.actionhandler;

import com.reallysi.rsuite.api.workflow.activiti.WorkflowContext;
import com.reallysi.rsuite.api.workflow.activiti.WorkflowTask;

public class NotifyToListWorkflowCompletedHandler extends NotifyToListWorkflowStartedHandler {

    @Override
    protected String buildSubject(WorkflowContext context, WorkflowTask task) {
        return "Workflow Completed Notification";
    }

    @Override
    protected String buildMessage(WorkflowContext context, WorkflowTask task) {
        StringBuilder content = new StringBuilder();

        String href = context.getRSuiteServerConfiguration().getProtocol() + "://"
                + context.getRSuiteServerConfiguration().getHostName() + ":"
                + context.getRSuiteServerConfiguration().getPort()
                + "/rsuite-cms/workflows/"
                + context.getWorkflowDefinitionId()
                + "/"
                + context.getWorkflowInstanceId();

        content.append("The workflow ")
                .append(context.getWorkflowVariables().getOrDefault("processDefinitionName", ""))
                .append(": ")
                .append(context.getWorkflowVariables().getOrDefault("workflow-title", ""))
                .append(" has been completed.")
                .append("\n\n")
                .append(href)
                .append("\n\n")
                .append("Sincerely,\n")
                .append("RSuite Enterprise");
        return content.toString();
    }

}


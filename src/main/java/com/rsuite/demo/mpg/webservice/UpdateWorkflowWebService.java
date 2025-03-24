package com.rsuite.demo.mpg.webservice;

import java.util.Map;

import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.remoteapi.CallArgumentList;
import com.reallysi.rsuite.api.remoteapi.RemoteApiExecutionContext;
import com.reallysi.rsuite.api.remoteapi.RemoteApiResult;
import com.reallysi.rsuite.api.remoteapi.result.NotificationResult;
import com.reallysi.rsuite.api.workflow.activiti.WorkflowInstance;
import com.rsicms.rsuite.helpers.webservice.RemoteApiHandlerBase;
import com.rsuite.demo.mpg.util.WorkflowUtils;

public class UpdateWorkflowWebService extends RemoteApiHandlerBase {

	@Override
	public RemoteApiResult execute(RemoteApiExecutionContext context, CallArgumentList args) throws RSuiteException {
		Map<String, Object> variables = WorkflowUtils.filterVariables(args);
		variables = WorkflowUtils.convertVariables(variables);

		String taskId = args.getFirstString("taskId");

		WorkflowInstance workflowInstance = context.getWorkflowInstanceService().getWorkflowInstanceForTask(taskId);

		for (String key : variables.keySet()) {
			context.getWorkflowInstanceService().setVariable(workflowInstance.getId(), key, variables.get(key));
		}

		WorkflowUtils.updateActiveTaskDueDate(context, taskId, workflowInstance, variables, "dueDateVariable");

		return new NotificationResult("Workflows", "Workflow Updated.");
	}

}

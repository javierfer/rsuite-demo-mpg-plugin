package com.rsuite.demo.mpg.webservice;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.User;
import com.reallysi.rsuite.api.remoteapi.CallArgumentList;
import com.reallysi.rsuite.api.remoteapi.RemoteApiExecutionContext;
import com.reallysi.rsuite.api.remoteapi.RemoteApiResult;
import com.reallysi.rsuite.api.remoteapi.result.MessageDialogResult;
import com.reallysi.rsuite.api.remoteapi.result.MessageType;
import com.reallysi.rsuite.api.remoteapi.result.RestResult;
import com.reallysi.rsuite.api.workflow.activiti.WorkflowInstance;
import com.rsicms.rsuite.helpers.webservice.RemoteApiHandlerBase;
import com.rsuite.demo.mpg.util.WorkflowUtils;

public class StartWorkflowWebService extends RemoteApiHandlerBase {

	private static Log log = LogFactory.getLog(StartWorkflowWebService.class);
	private static final String PROCESS_DEFINITION_NAME_ARG = "processDefinitionName";

	@Override
	public RemoteApiResult execute(RemoteApiExecutionContext context, CallArgumentList args) throws RSuiteException {
		String processDefinitionName = args.getFirstValue(PROCESS_DEFINITION_NAME_ARG);
		String rsuiteId = args.getFirstString("rsuiteId");
		try {
			Map<String, Object> variables = WorkflowUtils.filterVariables(args);

			User user = context.getSession().getUser();

			// attach publication to workflow
			variables.put("rsuite:contents", rsuiteId);
			variables.put("rsuiteUserId", user.getUserId());
			// send task assignment notifications.
			variables.put("bpm_sendEMailNotifications", "true");

			WorkflowInstance wfInst = context.getWorkflowInstanceService().startWorkflow(processDefinitionName,
					variables);

			String comments = args.getFirstString("initial-comments");
			if (isNotBlank(comments)) {
				context.getWorkflowInstanceService().addComment(user.getUserId(), wfInst.getId(),
						comments);
			}

			log.info(format("Started workflow: '%s':%s", processDefinitionName, wfInst.getId()));

			RestResult result = new MessageDialogResult(MessageType.SUCCESS, "Worklow executed: ",
					wfInst.getWorkflowDefinitionName());

			return result;
		} catch (Throwable e) {
			log.error(e.getLocalizedMessage(), e);
			return new MessageDialogResult(MessageType.ERROR, "Worlow not executed ", processDefinitionName);
		}
	}

}

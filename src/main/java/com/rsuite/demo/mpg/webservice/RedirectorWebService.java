package com.rsuite.demo.mpg.webservice;

import java.util.Map;

import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.remoteapi.CallArgumentList;
import com.reallysi.rsuite.api.remoteapi.RemoteApiExecutionContext;
import com.reallysi.rsuite.api.remoteapi.RemoteApiResult;
import com.reallysi.rsuite.api.remoteapi.result.InvokeWebServiceAction;
import com.reallysi.rsuite.api.remoteapi.result.RestResult;
import com.reallysi.rsuite.api.workflow.activiti.WorkflowInstance;
import com.rsicms.rsuite.helpers.webservice.RemoteApiHandlerBase;

public class RedirectorWebService extends RemoteApiHandlerBase {

    @Override
    public RemoteApiResult execute(RemoteApiExecutionContext context, CallArgumentList args) throws RSuiteException {
        String invokeWebServiceAction = args.getFirstValue("invokeWebServiceAction");
        String taskId = args.getFirstString("taskId");

        WorkflowInstance workflowInstance = context.getWorkflowInstanceService().getWorkflowInstanceForTask(taskId);
        Map<String, Object> variables = workflowInstance.getInstanceVariables();

        RestResult restResult = new RestResult();
        InvokeWebServiceAction serviceAction = new InvokeWebServiceAction(invokeWebServiceAction);
        serviceAction.addServiceParameter("taskId", taskId);
        serviceAction.setFormId(variables.get("formId").toString());

        restResult.addAction(serviceAction);
        return restResult;
    }

}

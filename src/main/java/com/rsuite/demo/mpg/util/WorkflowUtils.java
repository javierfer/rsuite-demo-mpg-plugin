package com.rsuite.demo.mpg.util;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.FieldExtension;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.extensions.ExecutionContext;
import com.reallysi.rsuite.api.remoteapi.CallArgumentList;
import com.reallysi.rsuite.api.workflow.activiti.WorkflowContext;
import com.reallysi.rsuite.api.workflow.activiti.WorkflowInstance;

public class WorkflowUtils {

	private static final SimpleDateFormat DATE_FORMATTER_WF_VAR = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat DATE_FORMATTER_UI = new SimpleDateFormat("MM/dd/yyyy");

	private static final Log log = LogFactory.getLog(WorkflowUtils.class);

	private WorkflowUtils() {
	}

	public static Map<String, Object> filterVariables(CallArgumentList args) {
		Map<String, String> concatArgs = CallArgumentListUtils.concatMultipleParams(args);
			
		Map<String, Object> variables = concatArgs.entrySet().stream()
				.filter(item -> !item.getKey().startsWith("@_"))
				.filter(item -> !item.getKey().startsWith("_"))
				.filter(item -> !item.getKey().startsWith("apiName"))
				.filter(item -> !item.getKey().startsWith("initial-comments"))
				.filter(item -> !item.getKey().startsWith("rsuiteBrowseUri"))
				.filter(item -> !item.getKey().startsWith("rsuiteSessionId"))
				.filter(item -> !item.getKey().startsWith("workflowPhase"))
				
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue()));
		return variables;
	}

	/**
	 * Since RSuite 5.4.1, {@link WorkflowContext#getVariable(String)} may return
	 * arrays instead of strings.
	 * Use this method to safely return strings, avoiding class cast exceptions.
	 * 
	 * @param context
	 * @param variable
	 * @return a string
	 */
	public static String getVariableSafely(WorkflowContext context, String variable) {
		Object variableObj = context.getVariable(variable);
		return getVariableSafely(variableObj);
	}

	public static String convertFromWorkflowToUIDatePickerFormat(String workflowDate) {
		try {
			Date date = DATE_FORMATTER_WF_VAR.parse(workflowDate);
			return DATE_FORMATTER_UI.format(date);
		} catch (Exception e) {
			log.error("Unable to convert workflow date to UI date: " + workflowDate, e);
			return EMPTY;
		}
	}

	public static Map<String, Object> convertVariables(Map<String, Object> map) {

		DateTimeFormatter wfFormatter = DateTimeFormatter.ofPattern(DATE_FORMATTER_WF_VAR.toPattern());
		DateTimeFormatter uiFormatter = DateTimeFormatter.ofPattern(DATE_FORMATTER_UI.toPattern());

		return map.entrySet().stream().map(entry -> {
			String value = String.valueOf(entry.getValue());
			try {
				LocalDate date = LocalDate.parse(value, uiFormatter);
				return Map.entry(entry.getKey(), date.format(wfFormatter));
			} catch (DateTimeParseException e) {
				return entry;
			}
		}).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, HashMap::new));
	}

	/**
	 * See {@link WorkflowUtils#getVariableSafely(WorkflowContext, String)}
	 * 
	 * @param variableObj
	 * @return
	 */
	public static String getVariableSafely(Object variableObj) {
		String resolvedValue = EMPTY;
		if (variableObj instanceof String[]) {
			String[] variableArray = (String[]) variableObj;
			if (variableArray != null && variableArray.length > 0) {
				resolvedValue = variableArray[0];
			}
		} else if (variableObj instanceof List<?>) {
			@SuppressWarnings("unchecked")
			List<String> variableList = (List<String>) variableObj;
			if (variableList != null && !variableList.isEmpty()) {
				resolvedValue = variableList.get(0);
			}
		} else if (variableObj instanceof String) {
			resolvedValue = (String) variableObj;
		} else {
			if (variableObj != null) {
				log.warn("Unable to determine variable object type: " + variableObj.getClass());
			}
		}
		return resolvedValue;
	}

	public static void updateActiveTaskDueDate(ExecutionContext context, String taskId,
	        WorkflowInstance workflowInstance, Map<String, Object> variables, String dueDateVarName
	        ) throws RSuiteException {

	    TaskService taskService = ProcessEngines.getDefaultProcessEngine().getTaskService();
	    RepositoryService repositoryService = ProcessEngines.getDefaultProcessEngine().getRepositoryService();

	    // Retrieve task and BPMN model
	    Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
	    BpmnModel bpmnModel = repositoryService.getBpmnModel(workflowInstance.getWorkflowDefinitionId());

	    // Find UserTask and FieldExtension for "dueDateVariable"
	    Optional<FieldExtension> fieldExtensionOpt = bpmnModel.getProcesses().stream()
	            .flatMap(p -> p.findFlowElementsOfType(UserTask.class).stream())
	            .filter(t -> task.getTaskDefinitionKey().equals(t.getId()))
	            .flatMap(t -> t.getTaskListeners().stream())
	            .flatMap(l -> l.getFieldExtensions().stream())
	            .filter(fe -> dueDateVarName.equals(fe.getFieldName()))
	            .findFirst();

		// Process due date if FieldExtension is found
		if (fieldExtensionOpt.isPresent()) {
			FieldExtension fe = fieldExtensionOpt.get();

			String dueDateValue = defaultIfNull(variables.get(fe.getStringValue()), "").toString();
			if (isNotBlank(dueDateValue)) {
				try {
					Date dueDate = DATE_FORMATTER_WF_VAR.parse(dueDateValue);
					context.getWorkflowTaskService().setTaskDueDate(taskId, dueDate);
					context.getWorkflowInstanceService().setVariable(workflowInstance.getId(), "dateDue", dueDateValue);
				} catch (ParseException e) {
					throw new RSuiteException(String.format("Unable to parse due date var: %s - value: %s",
							fe.getStringValue(), dueDateValue), e);
				}
			}
		}
	}

}

package com.rsuite.demo.mpg.form;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.reallysi.rsuite.api.FormControlType;
import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.forms.DefaultFormHandler;
import com.reallysi.rsuite.api.forms.FormColumnInstance;
import com.reallysi.rsuite.api.forms.FormInstance;
import com.reallysi.rsuite.api.forms.FormInstanceCreationContext;
import com.reallysi.rsuite.api.forms.FormParameterInstance;
import com.reallysi.rsuite.api.remoteapi.CallArgumentList;
import com.rsuite.demo.mpg.util.WorkflowUtils;

public class WorkflowPopulatorFormHandler extends DefaultFormHandler {

    private static final String DEFAULT_DELIMITER = ",";

    @Override
    public void adjustFormInstance(FormInstanceCreationContext context, FormInstance instance) throws RSuiteException {

        CallArgumentList args = context.getArgs();

        String taskId = args.getFirstString("taskId", "");
        if (taskId.isEmpty()) return;

        FormBuilder builder = new FormBuilder(instance);
        Map<String, Object> workflowVariables = context.getWorkflowTaskService().getLocalVariables(taskId);

        for (FormColumnInstance col : instance.getColumns()) {
            for (FormParameterInstance param : col.getParams()) {
                String paramName = param.getName();

                String value = WorkflowUtils.getVariableSafely(workflowVariables.get(paramName));

                setFieldValue(builder, param, paramName, value);
            }
        }
    }

    protected void setFieldValue(FormBuilder builder, FormParameterInstance param, String paramName, String value) {
        if (StringUtils.isNotBlank(value)) {
            if (param.isAllowMultiple()) {
                String[] values = value.split(DEFAULT_DELIMITER);
                builder.param(paramName).value(values);
            } else if (FormControlType.DATEPICKER.getName().equals(param.getFormControlType().getName())) {
                builder.param(paramName).value(WorkflowUtils.convertFromWorkflowToUIDatePickerFormat(value));
            } else {
                builder.param(paramName).value(value);
            }
        }
    }
}

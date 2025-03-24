package com.rsuite.demo.mpg.form;

import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.forms.FormInstance;
import com.reallysi.rsuite.api.forms.FormInstanceCreationContext;
import com.reallysi.rsuite.api.remoteapi.CallArgumentList;

public class WorkflowAdjusterFormHandler extends WorkflowPopulatorFormHandler {

    @Override
    public void adjustFormInstance(FormInstanceCreationContext context, FormInstance instance) throws RSuiteException {
        super.adjustFormInstance(context, instance);

        CallArgumentList args = context.getArgs();
        // show initial parameters only when the workflow instance starts up
        if (args.getFirstString("workflowPhase", "").equals("init")) {

            FormBuilder builder = new FormBuilder(instance);
            builder.param("initial-comments").formControlType("textarea");
        }
    }
}

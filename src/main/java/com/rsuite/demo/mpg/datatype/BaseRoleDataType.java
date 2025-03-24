package com.rsuite.demo.mpg.datatype;

import java.util.List;

import com.reallysi.rsuite.api.DataTypeOptionValue;
import com.reallysi.rsuite.api.RSuiteException;
import com.reallysi.rsuite.api.User;
import com.reallysi.rsuite.api.forms.DataTypeProviderOptionValuesContext;
import com.reallysi.rsuite.api.forms.DefaultDataTypeOptionValuesProviderHandler;

public abstract class BaseRoleDataType extends DefaultDataTypeOptionValuesProviderHandler {

    @Override
    public void provideOptionValues(DataTypeProviderOptionValuesContext context, List<DataTypeOptionValue> optionValues)
            throws RSuiteException {

        String[] userIds = context.getAuthorizationService().getAllUsers();

        for (String userId : userIds) {
            User user = context.getAuthorizationService().findUser(userId);
            if (hasRole(user)) {
                optionValues.add(new DataTypeOptionValue(user.getUserId(), user.getFullName()));
            }
        }
    }

    protected abstract boolean hasRole(User user);
}

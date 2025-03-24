package com.rsuite.demo.mpg.datatype;

import com.reallysi.rsuite.api.User;

public class UsersDataType extends BaseRoleDataType {

    @Override
    protected boolean hasRole(User user) {
        return true;
    }

}

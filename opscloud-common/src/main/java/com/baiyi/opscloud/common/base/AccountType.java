package com.baiyi.opscloud.common.base;



public enum AccountType {
    LDAP(0);

    private int type;

    AccountType(int type) {
        this.type = type;
    }

    public int getType() {
        return this.type;
    }
}

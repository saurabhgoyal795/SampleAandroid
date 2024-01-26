package com.zonetech.online.payment;


/**
 * Created by Rahul Hooda on 14/7/17.
 */
public enum AppEnvironment {

    SANDBOX {
        @Override
        public String merchant_Key() {
            return "P7rH3JGU";
        }

        @Override
        public String merchant_ID() {
            return "5651976";
        }

        @Override
        public String furl() {
            return "https://payuresponse.firebaseapp.com/failure";
        }

        @Override
        public String surl() {
            return "https://payuresponse.firebaseapp.com/success";
        }

        @Override
        public String salt() {
            return "GJCSLIOf6x";
        }

        @Override
        public boolean debug() {
            return true;
        }
    },
    PRODUCTION {
        @Override
        public String merchant_Key() {
            return "P7rH3JGU";
        }
        @Override
        public String merchant_ID() {
            return "5651976";
        }
        @Override
        public String furl() {
            return "https://payuresponse.firebaseapp.com/failure";
        }

        @Override
        public String surl() {
            return "https://payuresponse.firebaseapp.com/success";
        }

        @Override
        public String salt() {
            return "GJCSLIOf6x";
        }

        @Override
        public boolean debug() {
            return false;
        }
    };

    public abstract String merchant_Key();

    public abstract String merchant_ID();

    public abstract String furl();

    public abstract String surl();

    public abstract String salt();

    public abstract boolean debug();
}

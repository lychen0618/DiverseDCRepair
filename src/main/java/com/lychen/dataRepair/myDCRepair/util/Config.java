package com.lychen.dataRepair.myDCRepair.util;

public class Config {
    private String mode;

    public Config(String mode){
        this.mode = mode;
    }

    public static class Builder{
        private String mode;
        public Builder(){}
        public Builder setMode(String mode){
            this.mode = mode;
            return this;
        }
        public Config builder(){
            return new Config(mode);
        }
    }

    public boolean isDebug(){
        return this.mode != null && this.mode.equals("debug");
    }
}

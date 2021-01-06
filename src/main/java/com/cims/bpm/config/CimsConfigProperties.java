package com.cims.bpm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(prefix = "cims")
@Data
public class CimsConfigProperties {

    @NestedConfigurationProperty
    public Security security = new CimsConfigProperties.Security();


    @Data
    public static class Security {

        private boolean open;

        private String sm4Key;

        public String getSm4Key() {
            return sm4Key;
        }

        public void setSm4Key(String sm4Key) {
            this.sm4Key = sm4Key;
        }

        public boolean isOpen() {
            return open;
        }

        public void setOpen(boolean open) {
            this.open = open;
        }
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }
}

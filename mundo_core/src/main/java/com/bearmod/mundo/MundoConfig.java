package com.bearmod.mundo;

/** MundoConfig - runtime config (no license keys). */
public final class MundoConfig {
    public final String sessionToken; // do not store license keys
    public final String sessionId;
    public final String hwid;
    public final boolean enableNonRoot;
    public final boolean enableAntiHook;
    public final boolean enableStealth;
    public final boolean debug;

    public MundoConfig(String sessionToken, String sessionId, String hwid,
                       boolean enableNonRoot, boolean enableAntiHook, boolean enableStealth,
                       boolean debug) {
        this.sessionToken = sessionToken;
        this.sessionId = sessionId;
        this.hwid = hwid;
        this.enableNonRoot = enableNonRoot;
        this.enableAntiHook = enableAntiHook;
        this.enableStealth = enableStealth;
        this.debug = debug;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private String sessionToken; private String sessionId; private String hwid;
        private boolean enableNonRoot = true, enableAntiHook = true, enableStealth = true, debug;
        public Builder sessionToken(String v){ this.sessionToken=v; return this; }
        public Builder sessionId(String v){ this.sessionId=v; return this; }
        public Builder hwid(String v){ this.hwid=v; return this; }
        public Builder enableNonRoot(boolean v){ this.enableNonRoot=v; return this; }
        public Builder enableAntiHook(boolean v){ this.enableAntiHook=v; return this; }
        public Builder enableStealth(boolean v){ this.enableStealth=v; return this; }
        public Builder debug(boolean v){ this.debug=v; return this; }
        public MundoConfig build(){ return new MundoConfig(sessionToken, sessionId, hwid, enableNonRoot, enableAntiHook, enableStealth, debug); }
    }
}


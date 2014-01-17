package de.gemo.smartlauncher.units;

public class VARS {

    public static String getString(String string, GameInfo gameInfo) {
        String replaced = string;
        replaced = string.replace("${version}", gameInfo.getGameVersion());
        return replaced;
    }

    public static String getString(String string, String replace, String replacement) {
        String replaced = string;
        replaced = string.replace("${" + replace + "}", replacement);
        return replaced;
    }

    public final static class DIR {
        public final static String APPDATA = System.getenv("AppData") + "\\.SmartLauncher\\";
        public final static String ASSETS = APPDATA + "assets";
        public final static String LIBRARIES = APPDATA + "libraries";
        public final static String PROFILES = APPDATA + "profiles";
        public final static String VERSIONS = APPDATA + "versions";
    }

    public final static class URL {

        public final static class MinecraftLogin {
            private static final String LOGINSERVER = "https://authserver.mojang.com/";
            public static final String REFRESH_LOGIN = LOGINSERVER + "refresh";
            public static final String GET_LOGIN = LOGINSERVER + "authenticate";
        }

        public final static class JSON {
            public final static String PACKSERVER = "http://www.djgemo.de/";
            public final static String MC_ASSETS = "https://s3.amazonaws.com/Minecraft.Download/indexes/${version}.json";
            public final static String MC_VERSIONS = "http://s3.amazonaws.com/Minecraft.Download/versions/${version}/${version}.json";
        }

        public final static class FILES {
            public final static String MC_JAR = "http://s3.amazonaws.com/Minecraft.Download/versions/${version}/${version}.jar";
            public final static String LIBRARIES = "https://libraries.minecraft.net/";
            public final static String ASSETS = "http://resources.download.minecraft.net/";
        }
    }
}

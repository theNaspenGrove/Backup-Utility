package net.mov51.helpers.config;

import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static net.mov51.helpers.config.yamlHelper.getMap;
import static net.mov51.helpers.fileHelper.copyFromStream;
import static net.mov51.helpers.fileHelper.createDirs;
import static net.mov51.helpers.logHelper.*;

public class globalConfig {

    private static final org.apache.logging.log4j.Logger Logger = LogManager.getLogger("backupConfigLogger");

    private static final Path userGlobalConfigPath = Paths.get("config" + File.separator + "globalConfig.yml");
    private static final String internalGlobalConfigPath = "/defaultGlobalConfig.yml";

    private Map<String, Object> configMap;
    private static final globalConfig INSTANCE = new globalConfig();

    public enum Keys {
        //logging
        logFolder ("logFolder",true),
        logName ("logName",true),

        //sync Logging
        syncLogFolder ("syncLogFolder",false),
        syncLogName ("syncLogName",false),

        //backup logging
        backupLogFolder ("backupLogFolder",false),
        backupLogName ("backupLogName",false),

        //backup settings
        backupName ("backupName",true);

        public final String defaultKey;
        public final boolean required;

        Keys(String defaultKey, boolean required) {
            this.defaultKey = defaultKey;
            this.required = required;
        }
    }

    public static globalConfig getInstance() {
        return INSTANCE;
    }

    private globalConfig(){
        if(userGlobalConfigPath.toFile().exists()){
            configMap = getMap(userGlobalConfigPath);
        }
        else
        {
        //creating default core config file
        logError(Logger,"No Core Global Configuration exists. Creating default Core Config file.");
            if(createDirs(userGlobalConfigPath)){
                logInfo(Logger,"Verifying default configuration directory for Global Configuration file");

                if (copyFromStream(internalGlobalConfigPath,userGlobalConfigPath)) {
                    logInfo(Logger," Global Configuration file was created :D");
                    logFatalExit(Logger,"Please update it with your values!");
                    //todo link wiki
                }
            }
        }

    }

    //--LOGGING--

    public String getLogName(){
        String key = Keys.logName.defaultKey;
        if(INSTANCE.configMap.containsKey(key)){
            return INSTANCE.configMap.get(key).toString();
        }else{
            //todo replace with verification check
            return null;
        }
    }

    public String getLogFolder(){
        //todo I'm not sure if logging while the log folder is being made makes any sense.
        String key = Keys.logFolder.defaultKey;
        if(INSTANCE.configMap.containsKey(key)){
            File file = new File(INSTANCE.configMap.get(key).toString());
            if(!file.isDirectory()){
                if(file.mkdirs()) {
                    logInfo(Logger, "logging folder created!");
                }else{
                    logFatalExit(Logger, "logging folder could not be created!");
                }
            }
            return INSTANCE.configMap.get(key).toString();
        }
        //todo replace with verification check
        return null;
    }

    public String getBackupLogFolder(){
        //Defaults to logFolder
        return loadGetter(Keys.backupLogFolder,getLogFolder());
    }
    public String getSyncLogFolder(){
        //defaults to logFolder
        return loadGetter(Keys.syncLogFolder,getLogFolder());
    }

    public String getSyncLogName(){
        return loadGetter(Keys.syncLogName);
    }

    public String getBackupLogName(){
        return loadGetter(Keys.backupLogName);
    }
    //--BACKUPS--

    public String getBackupName(){
        return loadGetter(Keys.backupName);
    }

    private String loadGetter(Keys key, String defaultOption){
        //no default
        return configMap.getOrDefault(key.defaultKey,defaultOption).toString();
    }
    private String loadGetter(Keys key){
        if(configMap.containsKey(key.defaultKey))
            return configMap.get(key.defaultKey).toString();
        logFatal(Logger,"Could not load key " + key + " from globalConfig!");
        return "";
    }

}

package sqlg2;

import sqlg2.db.specific.OracleDBSpecific;

import java.io.File;

class Options {

    protected File srcRoot = new File(".");
    protected File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    protected boolean cleanup = true;
    protected String encoding = null;
    protected String classpath = null;
    protected boolean checkTime = true;
    protected String implPack = LocalWrapperBase.DEFAULT_IMPL_PACKAGE;
    protected String wrapPack = LocalWrapperBase.DEFAULT_WRAPPER_PACKAGE;
    private int tabSize = 4;
    protected String tab = null;
    protected String driverClass = "oracle.jdbc.driver.OracleDriver";
    protected String mapperClass = MapperImpl.class.getName();
    protected String dbClass = OracleDBSpecific.class.getName();
    protected String wrapperClass = DefaultWrapperGeneratorFactory.class.getName();
    protected String user = null;
    protected String pass = null;
    protected String url = null;
    protected boolean gwt = true;
    protected SQLGWarn warn = SQLGWarn.warn;
    protected boolean log = false;
    protected String runtimeMapperClass = LocalWrapperBase.DEFAULT_MAPPER_CLASS;

    Options() {
        setTab();
    }

    Options(File srcRoot, File tmpDir, String encoding, boolean cleanup, String classpath,
            boolean checkTime, String implPack, String wrapPack, int tabSize,
            String driverClass, String mapperClass, String dbClass, String wrapperClass,
            String user, String pass, String url, boolean gwt, SQLGWarn warn, boolean log, String runtimeMapperClass) {
        this.srcRoot = srcRoot;
        this.tmpDir = tmpDir;
        this.encoding = encoding;
        this.cleanup = cleanup;
        this.classpath = classpath;
        this.checkTime = checkTime;
        this.implPack = implPack;
        this.wrapPack = wrapPack;
        this.tabSize = tabSize;
        this.driverClass = driverClass;
        this.mapperClass = mapperClass;
        this.dbClass = dbClass;
        this.wrapperClass = wrapperClass;
        this.user = user;
        this.pass = pass;
        this.url = url;
        this.gwt = gwt;
        this.warn = warn;
        this.log = log;
        this.runtimeMapperClass = runtimeMapperClass;

        setTab();
    }

    Options(Options o) {
        this(
            o.srcRoot, o.tmpDir, o.encoding, o.cleanup, o.classpath, o.checkTime, o.implPack, o.wrapPack, o.tabSize,
            o.driverClass, o.mapperClass, o.dbClass, o.wrapperClass,
            o.user, o.pass, o.url, o.gwt, o.warn, o.log, o.runtimeMapperClass
        );
    }

    private void setTab() {
        if (tabSize < 0) {
            tab = "\t";
        } else {
            StringBuilder buf = new StringBuilder(tabSize);
            for (int i = 0; i < tabSize; i++)
                buf.append(' ');
            tab = buf.toString();
        }
    }

    void setTabSize(int newSize) {
        this.tabSize = newSize;
        setTab();
    }
}

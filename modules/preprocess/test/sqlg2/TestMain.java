package sqlg2;

import sqlg2.db.specific.Postgres;

import java.io.File;

final class TestMain {

    public static void main(String[] args) throws Exception {
        Options o = new Options();
        o.cleanup = false;
        o.checkTime = false;
        o.tmpDir = new File("C:/TEMP");
        o.srcRoot = new File("doc/examples/all_databases/1_example1/src");
        o.classpath = "C:\\Users\\User\\.m2\\repository\\com\\github\\osobolev\\sqlg2\\sqlg2-preprocess\\5.0\\sqlg2-preprocess-5.0.jar;C:\\Users\\User\\.m2\\repository\\com\\github\\osobolev\\sqlg2\\sqlg2-runtime\\5.0\\sqlg2-runtime-5.0.jar;C:\\Users\\User\\.m2\\repository\\com\\github\\osobolev\\sqlg2\\sqlg2-core\\5.0\\sqlg2-core-5.0.jar";
        o.wrapPack = "rmi";
        o.implPack = "rmi";
        o.user = "sqlg2";
        o.pass = "sqlg2";
        o.dbClass = Postgres.class.getName();
        o.driverClass = "org.postgresql.Driver";
        o.url = "jdbc:postgresql://localhost:5431/sqlg2";
        //o.mapperClass = "complex.mapper.CustomMapperImpl";
        File[] files = {new File(o.srcRoot, "example1/dao/Example1.java")};
        new Main(o).workFiles(files, files);
    }
}

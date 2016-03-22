package runtime.mapper;

import sqlg2.MapperImpl;

/**
 * Preprocessor extension point: custom mapper implementation.
 * It should be specified in sqlg ANT task "mapperclass" attribute.
 */
public final class CustomMapperImpl extends MapperImpl {

    /**
     * Used for creating test input parameters
     */
    public Object getTestObject(Class<?> paramType) {
        if (UserId.class.equals(paramType)) {
            return new UserId(1L);
        } else if (RoleId.class.equals(paramType)) {
            return new RoleId(1L);
        } else {
            return super.getTestObject(paramType);
        }
    }

    /**
     * Defines mapping.
     * @param type type name in SQL query
     * @return type description for this type - includes number of resultset columns used,
     * Java class and factory method. Factory method always has 3 parameters:
     * 1. java.sql.ResultSet;
     * 2. Index of the first column in resultset to be used for object creation;
     * 3. Index of the last column in resultset to be used for object creation. 
     */
    protected SpecialType getSpecialType(String type) {
        if ("UserId".equalsIgnoreCase(type)) {
            return new SpecialType(1, UserId.class);
        } else if ("RoleId".equalsIgnoreCase(type)) {
            return new SpecialType(1, RoleId.class);
        } else {
            return super.getSpecialType(type);
        }
    }
}

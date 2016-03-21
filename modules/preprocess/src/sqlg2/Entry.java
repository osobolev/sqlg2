package sqlg2;

final class Entry {

    final String javadoc;
    final String methodHeader;
    final String returnType;
    final String methodToCall;
    final String[] paramNames;
    final boolean noTest;
    final boolean publish;

    Entry(String javadoc, String methodHeader, String returnType, String methodToCall,
          String[] paramNames, boolean noTest, boolean publish) {
        this.javadoc = javadoc;
        this.methodToCall = methodToCall;
        this.returnType = returnType;
        this.methodHeader = methodHeader;
        this.paramNames = paramNames;
        this.noTest = noTest;
        this.publish = publish;
    }
}

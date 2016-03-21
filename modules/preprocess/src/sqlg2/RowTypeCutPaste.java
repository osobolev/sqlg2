package sqlg2;

final class RowTypeCutPaste extends CutPaste {

    final String className;
    final boolean isInterface;
    final boolean editable;

    String replaceTo = null;

    RowTypeCutPaste(int from, int to, String className, boolean isInterface, boolean editable) {
        super(from, to);
        this.className = className;
        this.isInterface = isInterface;
        this.editable = editable;
    }

    protected String getPasteText() {
        return replaceTo;
    }

    public String toString() {
        return className;
    }
}

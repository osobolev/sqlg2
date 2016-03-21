package sqlg2;

abstract class CutPaste {

    private final int from;
    private final int to;

    protected CutPaste(int from, int to) {
        this.from = from;
        this.to = to;
    }

    final void cutPaste(StringBuilder buf) {
        String pasteText = getPasteText();
        if (pasteText != null) {
            buf.delete(from - 1, to - 1);
            buf.insert(from - 1, pasteText);
        }
    }

    protected abstract String getPasteText();
}

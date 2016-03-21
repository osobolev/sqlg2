package sqlg2;

final class AfterCutPaste extends CutPaste {

    String replaceTo = null;

    AfterCutPaste(int from, int to) {
        super(from, to);
    }

    protected String getPasteText() {
        return replaceTo;
    }
}

package sqlg2;

final class ParamCutPaste extends CutPaste {

    final String param;
    final boolean out;
    String replaceTo = null;

    ParamCutPaste(int from, int to, String param, boolean out) {
        super(from, to);
        this.param = param;
        this.out = out;
    }

    protected String getPasteText() {
        return replaceTo;
    }
}

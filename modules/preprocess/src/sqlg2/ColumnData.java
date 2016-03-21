package sqlg2;

import java.util.List;

final class ColumnData {

    final String entryName;
    final List<ColumnInfo> columns;
    final boolean meta;

    ColumnData(String entryName, List<ColumnInfo> columns, boolean meta) {
        this.entryName = entryName;
        this.columns = columns;
        this.meta = meta;
    }
}

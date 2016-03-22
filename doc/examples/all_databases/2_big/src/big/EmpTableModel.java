package big;

import big.dao.BigDAO;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * Employee table model
 */
final class EmpTableModel extends AbstractTableModel {

    private final List<BigDAO.EmpRow> list;

    EmpTableModel(List<BigDAO.EmpRow> list) {
        this.list = list;
    }

    public int getRowCount() {
        return list.size();
    }

    public int getColumnCount() {
        return 4;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        BigDAO.EmpRow row = getRow(rowIndex);
        switch (columnIndex) {
        case 0:
            return row.empName();
        case 1:
            return row.job();
        case 2:
            return row.mgrName();
        case 3:
            return row.hireDate();
        }
        return null;
    }

    public String getColumnName(int column) {
        switch (column) {
        case 0:
            return "Emp name";
        case 1:
            return "Job";
        case 2:
            return "Mgr name";
        case 3:
            return "Hire date";
        }
        return null;
    }

    void insert(BigDAO.EmpRow row) {
        // Adding to the end of list
        int size = list.size();
        list.add(row);
        fireTableRowsInserted(size, size);
    }

    void update(int index, BigDAO.EmpRow row) {
        list.set(index, row);
        fireTableRowsUpdated(index, index);
    }

    void remove(int index) {
        list.remove(index);
        fireTableRowsDeleted(index, index);
    }

    BigDAO.EmpRow getRow(int index) {
        return list.get(index);
    }
}

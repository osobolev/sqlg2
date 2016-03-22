package big;

import big.dao.BigDAO;

import javax.swing.*;
import java.awt.*;

/**
 * Renderer for ComboBox of manager chooser
 */
final class MgrRenderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                  boolean cellHasFocus) {
        if (value instanceof BigDAO.MgrRow) {
            BigDAO.MgrRow row = (BigDAO.MgrRow) value;
            value = row.empName();
        } else if (value == null) {
            value = " ";
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}

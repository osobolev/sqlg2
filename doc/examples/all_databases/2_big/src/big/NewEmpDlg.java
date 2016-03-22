package big;

import big.dao.BigDAO;
import big.dao.IBigDAO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Employee editor dialog
 */
final class NewEmpDlg extends JDialog {

    private final JTextField tfJob = new JTextField(10);
    private final JTextField tfName = new JTextField(10);
    private JComboBox chMgr;
    private final JButton btnOk = new JButton("OK");
    private final JButton btnCancel = new JButton("Cancel");

    private boolean ok = false;

    NewEmpDlg(Frame owner, IBigDAO dao) throws SQLException {
        super(owner, "New emp", true);
        init(dao.selectAllManagers(), null, dao.getEmpMeta());
    }

    NewEmpDlg(Frame owner, IBigDAO dao, BigDAO.EmpRow row) throws SQLException {
        super(owner, "Edit emp", true);
        init(dao.selectPossibleManagers(row.empNo()), row, dao.getEmpMeta());
    }

    /**
     * Construct GUI and fill data fields
     * @param managers list of possible managers for the employee
     * @param row employee data, null if this is a new one
     */
    private void init(List<BigDAO.MgrRow> managers, BigDAO.EmpRow row, BigDAO.EmpMetaRow meta) {
        List<BigDAO.MgrRow> newManagers = new ArrayList<BigDAO.MgrRow>(managers);
        // Adding empty value - "no manager"
        newManagers.add(0, null);
        BigDAO.MgrRow[] mgrArray = newManagers.toArray(new BigDAO.MgrRow[newManagers.size()]);
        // Creating manager chooser ComboBox
        chMgr = new JComboBox(mgrArray);
        chMgr.setRenderer(new MgrRenderer());

        JPanel main = new JPanel();
        main.add(new JLabel("Name:"));
        main.add(tfName);
        main.add(new JLabel("Job:"));
        main.add(tfJob);
        main.add(new JLabel("Manager:"));
        main.add(chMgr);

        System.out.println("Max name length = " + meta.empName().length);
        System.out.println("Max job length = " + meta.job().length);

        JPanel butt = new JPanel();
        butt.add(btnOk);
        butt.add(btnCancel);

        add(main, BorderLayout.CENTER);
        add(butt, BorderLayout.SOUTH);

        btnOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ok = true;
                dispose();
            }
        });
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        if (row != null) {
            // Putting employee data to fields
            tfName.setText(row.empName());
            tfJob.setText(row.job());
            if (row.mgr() != null) {
                // Here is some cumbersome code - since SQLG works exactly like PL/SQL, you can only choose
                // manager EMP_NO field, not entire MgrRow object, so you have to find MgrRow by its EMP_NO.
                // It is a price you have to pay for simplicity; anyway, in real systems it is often good
                // to have Data Transfer Objects instead of raw RowTypes.
                int index = 0;
                boolean found = false;
                for (BigDAO.MgrRow mgr : mgrArray) {
                    if (mgr != null && mgr.empNo() == row.mgr().longValue()) {
                        found = true;
                        break;
                    }
                    index++;
                }
                if (found) {
                    chMgr.setSelectedIndex(index);
                }
            }
        }

        pack();
        setLocationRelativeTo(getOwner());
        setVisible(true);
    }

    boolean isOk() {
        return ok;
    }

    String getEmpName() {
        return tfName.getText();
    }

    String getJob() {
        return tfJob.getText();
    }

    Integer getMgrNo() {
        BigDAO.MgrRow mgrRow = (BigDAO.MgrRow) chMgr.getSelectedItem();
        return mgrRow == null ? null : mgrRow.empNo();
    }
}

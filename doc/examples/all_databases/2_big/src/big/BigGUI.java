package big;

import big.dao.BigDAO;
import big.dao.IBigDAO;
import sqlg2.db.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Full CRUD GUI example using SQLG2
 */
public final class BigGUI extends JFrame {

    private final IDBInterface db;
    private final IBigDAO dao;

    private final EmpTableModel model;
    private final JTable table;
    private final JButton btnAdd = new JButton("Add");
    private final JButton btnRemove = new JButton("Remove");
    private final JButton btnEdit = new JButton("Edit");

    public BigGUI(IDBInterface db) throws SQLException {
        super("Emp test");
        // Getting data access interface
        this.db = db;
        dao = db.getSimpleTransaction().getInterface(IBigDAO.class);
        // Selecting all employees
        List<BigDAO.EmpRow> list = dao.selectAll();
        // Creating table of employees
        model = new EmpTableModel(list);
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel butt = new JPanel();
        butt.add(btnAdd);
        butt.add(btnEdit);
        butt.add(btnRemove);
        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);
        add(butt, BorderLayout.SOUTH);

        btnAdd.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addEmp();
            }
        });
        btnRemove.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removeEmp();
            }
        });
        btnEdit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editEmp();
            }
        });

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        pack();
        setVisible(true);
    }

    /**
     * Adding new employee
     */
    private void addEmp() {
        try {
            NewEmpDlg dlg = new NewEmpDlg(this, dao);
            if (!dlg.isOk())
                return;
            String empName = dlg.getEmpName();
            String job = dlg.getJob();
            Integer mgrNo = dlg.getMgrNo();
            BigDAO.EmpRow empRow = dao.insert(empName, job, mgrNo);
            model.insert(empRow);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Removing employee
     */
    private void removeEmp() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            BigDAO.EmpRow empRow = model.getRow(row);
            try {
                dao.delete(empRow.empNo());
                model.remove(row);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Updating employee
     */
    private void editEmp() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            BigDAO.EmpRow empRow = model.getRow(row);
            try {
                NewEmpDlg dlg = new NewEmpDlg(this, dao, empRow);
                if (!dlg.isOk())
                    return;
                String empName = dlg.getEmpName();
                String job = dlg.getJob();
                Integer mgrNo = dlg.getMgrNo();
                empRow = dao.update(empRow.empNo(), empName, job, mgrNo);
                model.update(row, empRow);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Closing connection, window and exiting
     */
    public void dispose() {
        db.close();
        super.dispose();
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        // Database properties to use
        String driver, url, username, password;
        DBSpecific dbclass;
        if (args.length < 5) {
            System.err.println("Not all DB properties specified");
            return;
        } else {
            // For Oracle database should be oracle.jdbc.driver.OracleDriver
            driver = args[0];
            url = args[1];
            username = args[2];
            password = args[3];
            // For Oracle database should be sqlg2.db.specific.Oracle
            dbclass = (DBSpecific) Class.forName(args[4]).newInstance();
        }
        SQLGLogger logger = new SQLGLogger.Simple();
        // Opening connection
        Connection connection = SingleConnectionManager.openConnection(driver, url, username, password);
        ConnectionManager cman = new SingleConnectionManager(connection);
        IDBInterface db = new LocalDBInterface(cman, dbclass, logger);
        new BigGUI(db);
    }
}

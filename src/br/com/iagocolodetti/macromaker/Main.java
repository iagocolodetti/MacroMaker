package br.com.iagocolodetti.macromaker;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author iagocolodetti
 */
public class Main extends javax.swing.JFrame implements NativeKeyListener, NativeMouseListener {

    private final DefaultTableModel model;
    
    private boolean recording = false;
    private final List<KeyboardKey> cmbRecordAndScriptKeys = Keys.getKeyboardKeys().subList(0, 12);
    private final String[] keyboardKeys;
    private final String[] mouseKeys;
    
    private Long lastTime;
    private List<Macro> macroList;
    private final Set<Integer> keysHeld;
    
    private void addMacro(int key, Hardware hardware, Action action) {
        int macroListSize = macroList.size();
        if (macroListSize > 0) {
            Long next = new Date().getTime() - lastTime;
            macroList.get(macroListSize - 1).setNext(next);
            model.setValueAt(next, macroListSize - 1, 4);
        }
        lastTime = new Date().getTime();
        Macro macro = new Macro(hardware, key, action, 0L);
        macroList.add(macro);
        String keyName = "";
        if (hardware == Hardware.KEYBOARD) {
            keyName = Keys.getKeyboardKeyName(macro.getKey());
        } else if (hardware == Hardware.MOUSE) {
            keyName = Keys.getMouseKeyName(macro.getKey());
        }
        model.addRow(new Object[]{macroList.size() - 1, macro.getHardware().name(), keyName, macro.getAction().name(), macro.getNext()});
    }
    
    /**
     * Creates new form Main
     */
    public Main() {
        initComponents();
        keyboardKeys = new String[Keys.getKeyboardKeys().size()];
        for (int i = 0; i < Keys.getKeyboardKeys().size(); i++) {
            keyboardKeys[i] = Keys.getKeyboardKeys().get(i).getName();
        }
        mouseKeys = new String[Keys.getMouseKeys().size()];
        for (int i = 0; i < Keys.getMouseKeys().size(); i++) {
            mouseKeys[i] = Keys.getMouseKeys().get(i).getName();
        }
        for (Hardware hardware : Hardware.values()) {
            cmbEditHardware.addItem(hardware.name());
            cmbAddHardware.addItem(hardware.name());
        }
        for (Action action : Action.values()) {
            cmbEditAction.addItem(action.name());
            cmbAddAction.addItem(action.name());
        }
        for (int i = 0; i < cmbRecordAndScriptKeys.size(); i++) {
            String keyName = cmbRecordAndScriptKeys.get(i).getName();
            cmbRecordStart.addItem(keyName);
            cmbRecordStop.addItem(keyName);
            cmbScriptStart.addItem(keyName);
            cmbScriptStop.addItem(keyName);
        }
        cmbEditHardware.setSelectedIndex(0);
        cmbEditKey.setModel(new DefaultComboBoxModel<>(keyboardKeys));
        cmbEditAction.setSelectedIndex(0);
        cmbAddHardware.setSelectedIndex(0);
        cmbAddKey.setModel(new DefaultComboBoxModel<>(keyboardKeys));
        cmbAddAction.setSelectedIndex(0);
        cmbRecordStart.setSelectedIndex(0);
        cmbRecordStop.setSelectedIndex(1);
        cmbScriptStart.setSelectedIndex(2);
        cmbScriptStop.setSelectedIndex(3);
        model = (DefaultTableModel) tblMacro.getModel();
        macroList = new ArrayList<>();
        keysHeld = new HashSet<>();
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent evt) {
        if (Keys.keyboardKeysContains(evt.getKeyCode())) {
            if (!recording && evt.getKeyCode() == cmbRecordAndScriptKeys.get(cmbRecordStart.getSelectedIndex()).getKey()) {
                macroList.clear();
                model.setRowCount(0);
                keysHeld.clear();
                pnlRecord.setBorder(BorderFactory.createTitledBorder("Record (recording)"));
                cmbRecordStart.setEnabled(false);
                cmbRecordStop.setEnabled(false);
                recording = true;
                Toolkit.getDefaultToolkit().beep();
            } else if(recording && evt.getKeyCode() == cmbRecordAndScriptKeys.get(cmbRecordStop.getSelectedIndex()).getKey()) {
                pnlRecord.setBorder(BorderFactory.createTitledBorder("Record"));
                cmbRecordStart.setEnabled(true);
                cmbRecordStop.setEnabled(true);
                recording = false;
                Toolkit.getDefaultToolkit().beep();
            } else if (recording && !keysHeld.contains(evt.getKeyCode()) && evt.getKeyCode() != cmbRecordAndScriptKeys.get(cmbRecordStart.getSelectedIndex()).getKey() && evt.getKeyCode() != cmbRecordAndScriptKeys.get(cmbRecordStop.getSelectedIndex()).getKey()) {
                keysHeld.add(evt.getKeyCode());
                addMacro(evt.getKeyCode(), Hardware.KEYBOARD, Action.PRESS);
            }
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent evt) {
        if (recording && evt.getKeyCode() != cmbRecordAndScriptKeys.get(cmbRecordStart.getSelectedIndex()).getKey() && evt.getKeyCode() != cmbRecordAndScriptKeys.get(cmbRecordStop.getSelectedIndex()).getKey()) {
            if (Keys.keyboardKeysContains(evt.getKeyCode())) {
                addMacro(evt.getKeyCode(), Hardware.KEYBOARD, Action.RELEASE);
                keysHeld.remove(Integer.valueOf(evt.getKeyCode()));
            }
        }
    }
    
    @Override
    public void nativeMousePressed(NativeMouseEvent evt) {
        if (recording) {
            addMacro(evt.getButton(), Hardware.MOUSE, Action.PRESS);
        }
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent evt) {
        if (recording) {
            addMacro(evt.getButton(), Hardware.MOUSE, Action.RELEASE);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rboGroup = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblMacro = new javax.swing.JTable();
        pnlRecord = new javax.swing.JPanel();
        lblRecordStart = new javax.swing.JLabel();
        cmbRecordStart = new javax.swing.JComboBox<>();
        lblRecordStop = new javax.swing.JLabel();
        cmbRecordStop = new javax.swing.JComboBox<>();
        pnlEdit = new javax.swing.JPanel();
        lblEditPos = new javax.swing.JLabel();
        txtEditPos = new javax.swing.JTextField();
        lblEditHardware = new javax.swing.JLabel();
        cmbEditHardware = new javax.swing.JComboBox<>();
        lblEditKey = new javax.swing.JLabel();
        cmbEditKey = new javax.swing.JComboBox<>();
        lblEditAction = new javax.swing.JLabel();
        cmbEditAction = new javax.swing.JComboBox<>();
        lblEditNext = new javax.swing.JLabel();
        txtEditNext = new javax.swing.JTextField();
        btnEditRemove = new javax.swing.JButton();
        btnEditSave = new javax.swing.JButton();
        pnlAdd = new javax.swing.JPanel();
        rboAddFirst = new javax.swing.JRadioButton();
        rboAddLast = new javax.swing.JRadioButton();
        rboAddAt = new javax.swing.JRadioButton();
        txtAddAt = new javax.swing.JTextField();
        lblAddHardware = new javax.swing.JLabel();
        cmbAddHardware = new javax.swing.JComboBox<>();
        lblAddKey = new javax.swing.JLabel();
        cmbAddKey = new javax.swing.JComboBox<>();
        lblAddAction = new javax.swing.JLabel();
        cmbAddAction = new javax.swing.JComboBox<>();
        lblAddNext = new javax.swing.JLabel();
        txtAddNext = new javax.swing.JTextField();
        btnAddAdd = new javax.swing.JButton();
        pnlScript = new javax.swing.JPanel();
        lblScriptStart = new javax.swing.JLabel();
        cmbScriptStart = new javax.swing.JComboBox<>();
        lblScriptStop = new javax.swing.JLabel();
        cmbScriptStop = new javax.swing.JComboBox<>();
        chkScriptLoop = new javax.swing.JCheckBox();
        btnScriptLoad = new javax.swing.JButton();
        btnScriptSave = new javax.swing.JButton();
        btnScriptGenerate = new javax.swing.JButton();
        lblDownload = new javax.swing.JLabel();

        rboGroup.add(rboAddFirst);
        rboGroup.add(rboAddLast);
        rboGroup.add(rboAddAt);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("MacroMaker");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        tblMacro.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Pos", "Hardware", "Key", "Action", "Next"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Long.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        tblMacro.setColumnSelectionAllowed(true);
        tblMacro.getTableHeader().setReorderingAllowed(false);
        tblMacro.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblMacroMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblMacro);
        tblMacro.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        if (tblMacro.getColumnModel().getColumnCount() > 0) {
            tblMacro.getColumnModel().getColumn(0).setResizable(false);
            tblMacro.getColumnModel().getColumn(0).setPreferredWidth(30);
            tblMacro.getColumnModel().getColumn(1).setResizable(false);
            tblMacro.getColumnModel().getColumn(2).setResizable(false);
            tblMacro.getColumnModel().getColumn(3).setResizable(false);
            tblMacro.getColumnModel().getColumn(4).setResizable(false);
        }

        pnlRecord.setBorder(javax.swing.BorderFactory.createTitledBorder("Record"));

        lblRecordStart.setText("Start:");

        lblRecordStop.setText("Stop:");

        javax.swing.GroupLayout pnlRecordLayout = new javax.swing.GroupLayout(pnlRecord);
        pnlRecord.setLayout(pnlRecordLayout);
        pnlRecordLayout.setHorizontalGroup(
            pnlRecordLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRecordLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblRecordStart)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbRecordStart, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 33, Short.MAX_VALUE)
                .addComponent(lblRecordStop)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbRecordStop, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pnlRecordLayout.setVerticalGroup(
            pnlRecordLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlRecordLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlRecordLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRecordStart)
                    .addComponent(cmbRecordStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRecordStop)
                    .addComponent(cmbRecordStop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlEdit.setBorder(javax.swing.BorderFactory.createTitledBorder("Edit"));

        lblEditPos.setText("Pos:");

        txtEditPos.setEditable(false);
        txtEditPos.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        lblEditHardware.setText("Hardware:");

        cmbEditHardware.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbEditHardwareActionPerformed(evt);
            }
        });

        lblEditKey.setText("Key:");

        lblEditAction.setText("Action:");

        lblEditNext.setText("Next (ms):");

        txtEditNext.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtEditNext.setText("0");

        btnEditRemove.setText("Remove");
        btnEditRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditRemoveActionPerformed(evt);
            }
        });

        btnEditSave.setText("Save");
        btnEditSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditSaveActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlEditLayout = new javax.swing.GroupLayout(pnlEdit);
        pnlEdit.setLayout(pnlEditLayout);
        pnlEditLayout.setHorizontalGroup(
            pnlEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEditLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlEditLayout.createSequentialGroup()
                        .addComponent(lblEditPos)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtEditPos, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblEditHardware)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbEditHardware, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlEditLayout.createSequentialGroup()
                        .addComponent(lblEditKey)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbEditKey, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblEditAction)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbEditAction, 0, 93, Short.MAX_VALUE))
                    .addGroup(pnlEditLayout.createSequentialGroup()
                        .addComponent(btnEditRemove, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnEditSave, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlEditLayout.createSequentialGroup()
                        .addComponent(lblEditNext)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtEditNext)))
                .addContainerGap())
        );
        pnlEditLayout.setVerticalGroup(
            pnlEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlEditLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEditPos)
                    .addComponent(txtEditPos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblEditHardware)
                    .addComponent(cmbEditHardware, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEditKey)
                    .addComponent(cmbEditKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblEditAction)
                    .addComponent(cmbEditAction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblEditNext)
                    .addComponent(txtEditNext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlEditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnEditRemove)
                    .addComponent(btnEditSave))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlAdd.setBorder(javax.swing.BorderFactory.createTitledBorder("Add"));

        rboAddFirst.setText("First");
        rboAddFirst.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rboAddFirstActionPerformed(evt);
            }
        });

        rboAddLast.setSelected(true);
        rboAddLast.setText("Last");
        rboAddLast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rboAddLastActionPerformed(evt);
            }
        });

        rboAddAt.setText("At:");
        rboAddAt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rboAddAtActionPerformed(evt);
            }
        });

        txtAddAt.setEditable(false);
        txtAddAt.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        lblAddHardware.setText("Hardware:");

        cmbAddHardware.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbAddHardwareActionPerformed(evt);
            }
        });

        lblAddKey.setText("Key:");

        lblAddAction.setText("Action:");

        lblAddNext.setText("Next (ms):");

        txtAddNext.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        txtAddNext.setText("0");

        btnAddAdd.setText("Add");
        btnAddAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddAddActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlAddLayout = new javax.swing.GroupLayout(pnlAdd);
        pnlAdd.setLayout(pnlAddLayout);
        pnlAddLayout.setHorizontalGroup(
            pnlAddLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAddLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAddLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnAddAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlAddLayout.createSequentialGroup()
                        .addComponent(rboAddFirst)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 55, Short.MAX_VALUE)
                        .addComponent(rboAddLast)
                        .addGap(55, 55, 55)
                        .addComponent(rboAddAt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtAddAt, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlAddLayout.createSequentialGroup()
                        .addGroup(pnlAddLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(pnlAddLayout.createSequentialGroup()
                                .addComponent(lblAddAction)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbAddAction, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(pnlAddLayout.createSequentialGroup()
                                .addComponent(lblAddHardware)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbAddHardware, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(pnlAddLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(pnlAddLayout.createSequentialGroup()
                                .addComponent(lblAddKey)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cmbAddKey, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(pnlAddLayout.createSequentialGroup()
                                .addComponent(lblAddNext)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtAddNext)))))
                .addContainerGap())
        );
        pnlAddLayout.setVerticalGroup(
            pnlAddLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAddLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAddLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rboAddFirst)
                    .addComponent(rboAddLast)
                    .addComponent(rboAddAt)
                    .addComponent(txtAddAt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlAddLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAddHardware)
                    .addComponent(cmbAddHardware, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblAddKey)
                    .addComponent(cmbAddKey, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlAddLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAddAction)
                    .addComponent(cmbAddAction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblAddNext)
                    .addComponent(txtAddNext, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnAddAdd)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlScript.setBorder(javax.swing.BorderFactory.createTitledBorder("Script"));

        lblScriptStart.setText("Start:");

        lblScriptStop.setText("Stop:");

        chkScriptLoop.setText("Loop");

        btnScriptLoad.setText("Load");
        btnScriptLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnScriptLoadActionPerformed(evt);
            }
        });

        btnScriptSave.setText("Save");
        btnScriptSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnScriptSaveActionPerformed(evt);
            }
        });

        btnScriptGenerate.setText("Generate Autohotkey Script (.ahk)");
        btnScriptGenerate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnScriptGenerateActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlScriptLayout = new javax.swing.GroupLayout(pnlScript);
        pnlScript.setLayout(pnlScriptLayout);
        pnlScriptLayout.setHorizontalGroup(
            pnlScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlScriptLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnScriptGenerate, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlScriptLayout.createSequentialGroup()
                        .addComponent(lblScriptStart)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbScriptStart, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lblScriptStop)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cmbScriptStop, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(chkScriptLoop))
                    .addGroup(pnlScriptLayout.createSequentialGroup()
                        .addComponent(btnScriptLoad, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnScriptSave, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        pnlScriptLayout.setVerticalGroup(
            pnlScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlScriptLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblScriptStart)
                    .addComponent(cmbScriptStart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblScriptStop)
                    .addComponent(cmbScriptStop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(chkScriptLoop))
                .addGap(18, 18, 18)
                .addGroup(pnlScriptLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnScriptLoad)
                    .addComponent(btnScriptSave, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnScriptGenerate)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        lblDownload.setFont(new java.awt.Font("Tahoma", 2, 11)); // NOI18N
        lblDownload.setForeground(new java.awt.Color(0, 0, 238));
        lblDownload.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblDownload.setText("<html><u>AutoHotkey Download</u></html>");
        lblDownload.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblDownload.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lblDownload.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblDownloadMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 367, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(lblDownload, javax.swing.GroupLayout.DEFAULT_SIZE, 307, Short.MAX_VALUE)
                    .addComponent(pnlRecord, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlAdd, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlScript, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(pnlRecord, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlEdit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlAdd, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlScript, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblDownload, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_formWindowClosing

    private void cmbEditHardwareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbEditHardwareActionPerformed
        if (cmbEditHardware.getSelectedIndex() == 0) {
            cmbEditKey.setModel(new DefaultComboBoxModel<>(keyboardKeys));
        } else if (cmbEditHardware.getSelectedIndex() == 1) {
            cmbEditKey.setModel(new DefaultComboBoxModel<>(mouseKeys));
        }
    }//GEN-LAST:event_cmbEditHardwareActionPerformed

    private void btnEditRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditRemoveActionPerformed
        if (!recording) {
            if (!txtEditPos.getText().isEmpty()) {
                int row = Integer.parseInt(txtEditPos.getText());
                model.removeRow(row);
                macroList.remove(row);
                for (int i = row; i < macroList.size(); i++) {
                    model.setValueAt(i, i, 0);
                }
                txtEditPos.setText("");
                cmbEditHardware.setSelectedIndex(0);
                cmbEditKey.setSelectedIndex(0);
                cmbAddAction.setSelectedIndex(0);
                txtEditNext.setText("0");
            } else {
                JOptionPane.showMessageDialog(this, "You must select a row from table first.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "You can not do it while recording.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnEditRemoveActionPerformed

    private void btnEditSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditSaveActionPerformed
        if (!recording) {
            if (!txtEditPos.getText().isEmpty()) {
                try {
                    Long next = Long.parseLong(txtEditNext.getText());
                    if (next > -1) {
                        int row = Integer.parseInt(txtEditPos.getText());
                        Macro macro = null;
                        String keyName = "";
                        if (cmbEditHardware.getSelectedItem().equals(Hardware.KEYBOARD.name())) {
                            int key = Keys.getKeyboardKeys().get(cmbEditKey.getSelectedIndex()).getKey();
                            keyName = Keys.getKeyboardKeyName(key);
                            macro = new Macro(Hardware.KEYBOARD, key, Action.valueOf(cmbEditAction.getSelectedItem().toString()), next);
                        } else if (cmbEditHardware.getSelectedItem().equals(Hardware.MOUSE.name())) {
                            int key = Keys.getMouseKeys().get(cmbEditKey.getSelectedIndex()).getKey();
                            keyName = Keys.getMouseKeyName(key);
                            macro = new Macro(Hardware.MOUSE, key, Action.valueOf(cmbEditAction.getSelectedItem().toString()), next);
                        }
                        macroList.set(row, macro);
                        model.setValueAt(macro.getHardware().name(), row, 1);
                        model.setValueAt(keyName, row, 2);
                        model.setValueAt(macro.getAction().name(), row, 3);
                        model.setValueAt(next, row, 4);
                    } else {
                        JOptionPane.showMessageDialog(this, "Next field must be zero or above.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Next field must be a integer number.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "You must select a row from table first.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "You can not do it while recording.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnEditSaveActionPerformed

    private void rboAddFirstActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rboAddFirstActionPerformed
        txtAddAt.setEditable(rboAddAt.isSelected());
    }//GEN-LAST:event_rboAddFirstActionPerformed

    private void rboAddLastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rboAddLastActionPerformed
        txtAddAt.setEditable(rboAddAt.isSelected());
    }//GEN-LAST:event_rboAddLastActionPerformed

    private void rboAddAtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rboAddAtActionPerformed
        txtAddAt.setEditable(rboAddAt.isSelected());
    }//GEN-LAST:event_rboAddAtActionPerformed

    private void cmbAddHardwareActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbAddHardwareActionPerformed
        if (cmbAddHardware.getSelectedIndex() == 0) {
            cmbAddKey.setModel(new DefaultComboBoxModel<>(keyboardKeys));
        } else if (cmbAddHardware.getSelectedIndex() == 1) {
            cmbAddKey.setModel(new DefaultComboBoxModel<>(mouseKeys));
        }
    }//GEN-LAST:event_cmbAddHardwareActionPerformed

    private void btnAddAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddAddActionPerformed
        if (!recording) {
            int index = -1;
            if (rboAddFirst.isSelected()) {
                index = 0;
            } else if (rboAddLast.isSelected()) {
                index = macroList.size();
            } else if (rboAddAt.isSelected()) {
                try {
                    index = Integer.valueOf(txtAddAt.getText());
                    if (index < 0 || index > macroList.size()) {
                        JOptionPane.showMessageDialog(this, "At field must between 0 and " + macroList.size() + ".", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "At field must be a integer number.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            try {
                Long next = Long.parseLong(txtEditNext.getText());
                if (next > -1) {
                    Macro macro = null;
                    String keyName = "";
                    if (cmbAddHardware.getSelectedItem().equals(Hardware.KEYBOARD.name())) {
                        int key = Keys.getKeyboardKeys().get(cmbAddKey.getSelectedIndex()).getKey();
                        keyName = Keys.getKeyboardKeyName(key);
                        macro = new Macro(Hardware.KEYBOARD, key, Action.valueOf(cmbAddAction.getSelectedItem().toString()), next);
                    } else if (cmbAddHardware.getSelectedItem().equals(Hardware.MOUSE.name())) {
                        int key = Keys.getMouseKeys().get(cmbAddKey.getSelectedIndex()).getKey();
                        keyName = Keys.getMouseKeyName(key);
                        macro = new Macro(Hardware.MOUSE, key, Action.valueOf(cmbAddAction.getSelectedItem().toString()), next);
                    }
                    macroList.add(index, macro);
                    model.insertRow(index, new Object[]{index, macro.getHardware().name(), keyName, macro.getAction().name(), macro.getNext()});
                    for (int i = index + 1; i < macroList.size(); i++) {
                        model.setValueAt(i, i, 0);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Next field must be zero or above.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Next field must be a integer number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "You can not do it while recording.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnAddAddActionPerformed

    private void btnScriptLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnScriptLoadActionPerformed
        if (!recording) {
            try {
                JFileChooser chooser = new JFileChooser();
                chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                chooser.setFileFilter(new FileNameExtensionFilter("Script data file (.dat)", "dat"));
                chooser.setDialogTitle("Select a script data file");
                if (chooser.showOpenDialog(this) == JFileChooser.OPEN_DIALOG) {
                    Script script = new Script(chooser.getSelectedFile().getPath());
                    cmbScriptStart.setSelectedItem(Keys.getKeyboardKeyName(script.getStartKey()));
                    cmbScriptStop.setSelectedItem(Keys.getKeyboardKeyName(script.getStopKey()));
                    macroList = script.getMacroList();
                    model.setRowCount(0);
                    for (int i = 0; i < macroList.size(); i++) {
                        Macro macro = macroList.get(i);
                        String keyName = "";
                        if (macro.getHardware() == Hardware.KEYBOARD) {
                            keyName = Keys.getKeyboardKeyName(macro.getKey());
                        } else if (macro.getHardware() == Hardware.MOUSE) {
                            keyName = Keys.getMouseKeyName(macro.getKey());
                        }
                        model.addRow(new Object[]{macroList.size() - 1, macro.getHardware().name(), keyName, macro.getAction().name(), macro.getNext()});
                    }
                    chkScriptLoop.setSelected(script.isLoop());
                    JOptionPane.showMessageDialog(this, "Script data file \"" + chooser.getSelectedFile().getName() + "\" successfully loaded.", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (IOException | ClassNotFoundException | IndexOutOfBoundsException ex) {
                JOptionPane.showMessageDialog(this, "Could not load script data file.", "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        } else {
            JOptionPane.showMessageDialog(this, "You can not do it while recording.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnScriptLoadActionPerformed

    private void btnScriptSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnScriptSaveActionPerformed
        if (!recording) {
            if (!macroList.isEmpty()) {
                try {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    chooser.setFileFilter(new FileNameExtensionFilter("Script data file (.dat)", "dat"));
                    chooser.setDialogTitle("Save a script data file");
                    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                        String fileName = chooser.getSelectedFile().getName().endsWith(".dat") ? chooser.getSelectedFile().getName() : chooser.getSelectedFile().getName() + ".dat";
                        Script script = new Script(cmbRecordAndScriptKeys.get(cmbScriptStart.getSelectedIndex()).getKey(), cmbRecordAndScriptKeys.get(cmbScriptStop.getSelectedIndex()).getKey(), macroList, chkScriptLoop.isSelected());
                        script.toFile(chooser.getSelectedFile().getParent() + "\\" + fileName);
                        JOptionPane.showMessageDialog(this, "Script data file \"" + fileName + "\" successfully created/saved.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (IOException | NullPointerException | IndexOutOfBoundsException ex) {
                    JOptionPane.showMessageDialog(this, "Could not save script data file.", "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Macro list is empty.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "You can not do it while recording.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnScriptSaveActionPerformed

    private void btnScriptGenerateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnScriptGenerateActionPerformed
        if (!recording) {
            if (!macroList.isEmpty()) {
                try {
                    JFileChooser chooser = new JFileChooser();
                    chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    chooser.setFileFilter(new FileNameExtensionFilter("AutoHotkey script (.ahk)", "ahk"));
                    chooser.setDialogTitle("Generate an AutoHotkey script");
                    if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                        String fileName = chooser.getSelectedFile().getName().endsWith(".ahk") ? chooser.getSelectedFile().getName() : chooser.getSelectedFile().getName() + ".ahk";
                        Script script = new Script(cmbRecordAndScriptKeys.get(cmbScriptStart.getSelectedIndex()).getKey(), cmbRecordAndScriptKeys.get(cmbScriptStop.getSelectedIndex()).getKey(), macroList, chkScriptLoop.isSelected());
                        script.generateAhkScript(chooser.getSelectedFile().getParent() + "\\" + fileName);
                        JOptionPane.showMessageDialog(this, "AutoHotkey script \"" + fileName + "\" successfully generated.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Could not generate AutoHotkey script.", "Error", JOptionPane.ERROR_MESSAGE);
                    ex.printStackTrace();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Macro list is empty.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "You can not do it while recording.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnScriptGenerateActionPerformed

    private void lblDownloadMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblDownloadMouseClicked
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI("https://www.autohotkey.com/"));
            } catch (IOException | URISyntaxException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_lblDownloadMouseClicked

    private void tblMacroMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblMacroMouseClicked
        int row = ((JTable)evt.getSource()).rowAtPoint(evt.getPoint());
        txtEditPos.setText(String.valueOf(row));
        Macro macro = macroList.get(row);
        cmbEditHardware.setSelectedItem(macro.getHardware().name());
        if (macro.getHardware() == Hardware.KEYBOARD) {
            cmbEditKey.setSelectedItem(Keys.getKeyboardKeyName(macro.getKey()));
        } else if (macro.getHardware() == Hardware.MOUSE) {
            cmbEditKey.setSelectedItem(Keys.getMouseKeyName(macro.getKey()));
        }
        cmbEditAction.setSelectedItem(macro.getAction().name());
        txtEditNext.setText(String.valueOf(macro.getNext()));
    }//GEN-LAST:event_tblMacroMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Main.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
                Main main = new Main();
                try {
                    GlobalScreen.registerNativeHook();
                } catch (NativeHookException ex) {
                    ex.printStackTrace();
                }
                GlobalScreen.addNativeKeyListener(main);
                GlobalScreen.addNativeMouseListener(main);
                main.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddAdd;
    private javax.swing.JButton btnEditRemove;
    private javax.swing.JButton btnEditSave;
    private javax.swing.JButton btnScriptGenerate;
    private javax.swing.JButton btnScriptLoad;
    private javax.swing.JButton btnScriptSave;
    private javax.swing.JCheckBox chkScriptLoop;
    private javax.swing.JComboBox<String> cmbAddAction;
    private javax.swing.JComboBox<String> cmbAddHardware;
    private javax.swing.JComboBox<String> cmbAddKey;
    private javax.swing.JComboBox<String> cmbEditAction;
    private javax.swing.JComboBox<String> cmbEditHardware;
    private javax.swing.JComboBox<String> cmbEditKey;
    private javax.swing.JComboBox<String> cmbRecordStart;
    private javax.swing.JComboBox<String> cmbRecordStop;
    private javax.swing.JComboBox<String> cmbScriptStart;
    private javax.swing.JComboBox<String> cmbScriptStop;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblAddAction;
    private javax.swing.JLabel lblAddHardware;
    private javax.swing.JLabel lblAddKey;
    private javax.swing.JLabel lblAddNext;
    private javax.swing.JLabel lblDownload;
    private javax.swing.JLabel lblEditAction;
    private javax.swing.JLabel lblEditHardware;
    private javax.swing.JLabel lblEditKey;
    private javax.swing.JLabel lblEditNext;
    private javax.swing.JLabel lblEditPos;
    private javax.swing.JLabel lblRecordStart;
    private javax.swing.JLabel lblRecordStop;
    private javax.swing.JLabel lblScriptStart;
    private javax.swing.JLabel lblScriptStop;
    private javax.swing.JPanel pnlAdd;
    private javax.swing.JPanel pnlEdit;
    private javax.swing.JPanel pnlRecord;
    private javax.swing.JPanel pnlScript;
    private javax.swing.JRadioButton rboAddAt;
    private javax.swing.JRadioButton rboAddFirst;
    private javax.swing.JRadioButton rboAddLast;
    private javax.swing.ButtonGroup rboGroup;
    private javax.swing.JTable tblMacro;
    private javax.swing.JTextField txtAddAt;
    private javax.swing.JTextField txtAddNext;
    private javax.swing.JTextField txtEditNext;
    private javax.swing.JTextField txtEditPos;
    // End of variables declaration//GEN-END:variables
}

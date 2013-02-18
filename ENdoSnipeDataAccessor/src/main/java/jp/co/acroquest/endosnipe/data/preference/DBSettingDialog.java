/*******************************************************************************
 * ENdoSnipe 5.0 - (https://github.com/endosnipe)
 * 
 * The MIT License (MIT)
 * 
 * Copyright (c) 2012 Acroquest Technology Co.,Ltd.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package jp.co.acroquest.endosnipe.data.preference;

import jp.co.acroquest.endosnipe.data.ENdoSnipeDataAccessorPlugin;
import jp.co.acroquest.endosnipe.data.db.ConnectionManager;
import jp.co.acroquest.endosnipe.data.db.DBManager;
import jp.co.acroquest.endosnipe.data.util.DataAccessorConfigUtil;
import jp.co.acroquest.endosnipe.data.util.DataAccessorMessages;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * DB�̐ݒ���s�Ȃ��_�C�A���O�ł��B<br />
 * �f�t�H���g��H2DB�𗘗p���܂��B
 * 
 * @author fujii
 *
 */
public class DBSettingDialog extends TitleAreaDialog
{
    /** �f�[�^�x�[�X���x���p���b�Z�[�W�L�[ */
    private static final String          DBLABEL_KEY   = "data.accessor.dbSettingDialog.database";

    /** �f�[�^�x�[�X��t�H���_���x���p���b�Z�[�W�L�[ */
    private static final String          DIRLABEL_KEY  = "data.accessor.dbSettingDialog.basedir";

    /** �z�X�g���x���p���b�Z�[�W�L�[ */
    private static final String          HOSTLABEL_KEY = "data.accessor.dbSettingDialog.host";

    /** �|�[�g�ԍ����x���p���b�Z�[�W�L�[ */
    private static final String          PORTLABEL_KEY = "data.accessor.dbSettingDialog.port";

    /** ���[�U�����x���p���b�Z�[�W�L�[ */
    private static final String          NAMELABEL_KEY = "data.accessor.dbSettingDialog.username";

    /** �p�X���[�h���x���p���b�Z�[�W�L�[ */
    private static final String          PASSLABEL_KEY = "data.accessor.dbSettingDialog.password";

    /** DB�ݒ�_�C�A���O���̗p���b�Z�[�W�L�[ */
    private static final String          DIALOG_KEY    = "data.accessor.dbSettingDialog.dialogName";

    /** �{�^���N���b�N���̑��삪�s��ꂽ�Ƃ��̏������s���R���g���[�� */
    private final OpenDbDialogController controller_;

    /** H2DB�I��p�̃��W�I�{�^�� */
    private Button                       h2Radio_;

    /** postgresqlDB�I��p�̃��W�I�{�^�� */
    private Button                       postgresRadio_;

    /** �f�[�^�x�[�X��t�H���_�p�e�L�X�g */
    private Text                         baseDirText_;

    /** �f�[�^�x�[�X�̃t�H���_����������{�^�� */
    private Button                       buttonReferDbFolder_;

    /** �z�X�g�p�e�L�X�g */
    private Text                         hostText_;

    /** �|�[�g�p�e�L�X�g */
    private Text                         portText_;

    /** ���[�U���p�e�L�X�g */
    private Text                         userNameText_;

    /** �p�X���[�h�p�e�L�X�g */
    private Text                         passwordText_;

    /** �e�y�[�W */
    private DataAccessorPreferencePage   parent_;

    /** �ڑ���f�[�^�x�[�X���B */
    private static final String DATABASE_NAME = "endosnipedb"; 
    
    /**
     * �R���X�g���N�^�ł��B
     * @param parentShell �eShell
     */
    public DBSettingDialog(Shell parentShell, DataAccessorPreferencePage parentPage)
    {
        super(parentShell);
        this.parent_ = parentPage;
        this.controller_ = new OpenDbDialogController();
    }

    /**
     * {@inheritDoc}<br>
     * �_�C�A���O�̃��C���������쐬����B
     */
    @Override
    protected Control createDialogArea(final Composite parent)
    {
        Composite composite = (Composite)super.createDialogArea(parent);

        GridData labelGridData = new GridData();
        labelGridData.widthHint = 120;
        labelGridData.heightHint = 20;

        createDbRadioContents(composite, labelGridData);
        createDBDirComposite(composite, labelGridData);
        createHostComposite(composite, labelGridData);
        createPortComposite(composite, labelGridData);
        createUserNameComposite(composite, labelGridData);
        createPasswordComposite(composite, labelGridData);

        initialize();
        return parent;
    }

    /**
     * DB�I��p���W�I�{�^�����쐬
     * @param composite �\���p�R���|�W�b�g
     * @param labelGridData �z�u�p���C�A�E�g
     */
    private void createDbRadioContents(Composite composite, GridData labelGridData)
    {
        Composite dbContents = new Composite(composite, SWT.NULL);
        dbContents.setLayout(new GridLayout(3, false));
        Label dbLabel = new Label(dbContents, SWT.LEFT);
        dbLabel.setText(DataAccessorMessages.getMessage(DBLABEL_KEY));
        dbLabel.setLayoutData(labelGridData);

        this.h2Radio_ = new Button(dbContents, SWT.RADIO);
        this.h2Radio_.setText(DBPreferenceUtil.H2_NAME);
        this.h2Radio_.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event)
            {
                DBSettingDialog.this.h2Radio_.setSelection(true);
                DBSettingDialog.this.postgresRadio_.setSelection(false);
                selectH2Radio();
            }

        });

        this.postgresRadio_ = new Button(dbContents, SWT.RADIO);
        this.postgresRadio_.setText(DBPreferenceUtil.POSTGRE_SQL_NAME);
        this.postgresRadio_.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event)
            {
                DBSettingDialog.this.h2Radio_.setSelection(false);
                DBSettingDialog.this.postgresRadio_.setSelection(true);
                selectPostgresRadio();
            }

        });
    }

    /**
     * �z�X�g���͗p�e�L�X�g�t�B�[���h���쐬
     * @param composite �\���p�R���|�W�b�g
     * @param labelGridData �z�u�p���C�A�E�g
     */
    private void createDBDirComposite(Composite composite, GridData labelGridData)
    {
        Composite dbdirComposite = new Composite(composite, SWT.NULL);
        dbdirComposite.setLayout(new GridLayout(3, false));

        Label url = new Label(dbdirComposite, SWT.LEFT);
        url.setText(DataAccessorMessages.getMessage(DIRLABEL_KEY));
        url.setLayoutData(labelGridData);

        GridData driverPathData = new GridData();
        driverPathData.widthHint = 200;
        driverPathData.heightHint = 15;

        this.baseDirText_ = new Text(dbdirComposite, SWT.BORDER);
        this.baseDirText_.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        String dbDir = DataAccessorConfigUtil.getDatabaseDirectory();
        this.baseDirText_.setText(dbDir);

        this.buttonReferDbFolder_ = new Button(dbdirComposite, SWT.NONE);

        String keyReference = "data.accessor.reference";
        this.buttonReferDbFolder_.setText(DataAccessorMessages.getMessage(keyReference));
        this.buttonReferDbFolder_.setLayoutData(new GridData());
        this.buttonReferDbFolder_.addSelectionListener(this.controller_);
    }

    /**
     * �z�X�g���͗p�e�L�X�g�t�B�[���h���쐬
     * @param composite �\���p�R���|�W�b�g
     * @param labelGridData �z�u�p���C�A�E�g
     */
    private void createHostComposite(Composite composite, GridData labelGridData)
    {
        Composite urlComposite = new Composite(composite, SWT.NULL);
        urlComposite.setLayout(new GridLayout(2, false));

        Label url = new Label(urlComposite, SWT.LEFT);
        url.setText(DataAccessorMessages.getMessage(HOSTLABEL_KEY));
        url.setLayoutData(labelGridData);

        GridData driverPathData = new GridData();
        driverPathData.widthHint = 200;
        driverPathData.heightHint = 15;

        this.hostText_ = new Text(urlComposite, SWT.SINGLE | SWT.BORDER);
        this.hostText_.setLayoutData(driverPathData);
    }

    /**
     * �|�[�g���͗p�e�L�X�g�t�B�[���h���쐬
     * @param composite �\���p�R���|�W�b�g
     * @param labelGridData �z�u�p���C�A�E�g
     */
    private void createPortComposite(Composite composite, GridData labelGridData)
    {
        Composite urlComposite = new Composite(composite, SWT.NULL);
        urlComposite.setLayout(new GridLayout(2, false));

        Label url = new Label(urlComposite, SWT.LEFT);
        url.setText(DataAccessorMessages.getMessage(PORTLABEL_KEY));
        url.setLayoutData(labelGridData);

        GridData driverPathData = new GridData();
        driverPathData.widthHint = 200;
        driverPathData.heightHint = 15;

        this.portText_ = new Text(urlComposite, SWT.SINGLE | SWT.BORDER);
        this.portText_.setLayoutData(driverPathData);
    }

    /**
     * ���[�U�����͗p�e�L�X�g�t�B�[���h���쐬
     * @param composite �\���p�R���|�W�b�g
     * @param labelGridData �z�u�p���C�A�E�g
     */
    private void createUserNameComposite(Composite composite, GridData labelGridData)
    {
        Composite urlComposite = new Composite(composite, SWT.NULL);
        urlComposite.setLayout(new GridLayout(2, false));

        Label url = new Label(urlComposite, SWT.LEFT);
        url.setText(DataAccessorMessages.getMessage(NAMELABEL_KEY));
        url.setLayoutData(labelGridData);

        GridData driverPathData = new GridData();
        driverPathData.widthHint = 200;
        driverPathData.heightHint = 15;

        this.userNameText_ = new Text(urlComposite, SWT.SINGLE | SWT.BORDER);
        this.userNameText_.setLayoutData(driverPathData);
    }

    /**
     * �p�X���[�h���͗p�e�L�X�g�t�B�[���h���쐬
     * @param composite �\���p�R���|�W�b�g
     * @param labelGridData �z�u�p���C�A�E�g
     */
    private void createPasswordComposite(Composite composite, GridData labelGridData)
    {
        Composite urlComposite = new Composite(composite, SWT.NULL);
        urlComposite.setLayout(new GridLayout(2, false));

        Label url = new Label(urlComposite, SWT.LEFT);
        url.setText(DataAccessorMessages.getMessage(PASSLABEL_KEY));
        url.setLayoutData(labelGridData);

        GridData driverPathData = new GridData();
        driverPathData.widthHint = 200;
        driverPathData.heightHint = 15;

        this.passwordText_ = new Text(urlComposite, SWT.PASSWORD | SWT.BORDER);
        this.passwordText_.setLayoutData(driverPathData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void okPressed()
    {
        boolean useDefaultDb = true;
        if (this.postgresRadio_.getSelection())
        {
            useDefaultDb = false;
        }
        
        if (isDirty())
        {
            ConnectionManager manager = ConnectionManager.getInstance();
            manager.closeAll();

            String dbName = DATABASE_NAME;
            DBManager.updateSettings(useDefaultDb, this.baseDirText_.getText(),
                                     this.hostText_.getText(), this.portText_.getText(), dbName,
                                     this.userNameText_.getText(), this.passwordText_.getText());
            storeSettings();
            
            this.parent_.updateDatabaseList();
        }
        
        super.okPressed();
    }

    private boolean isDirty()
    {
        boolean useDefaultDb = true;
        if (this.postgresRadio_.getSelection())
        {
            useDefaultDb = false;
        }        
        
        boolean dirty = DBManager.isDirty(useDefaultDb, this.baseDirText_.getText(),
                                 this.hostText_.getText(), this.portText_.getText(),
                                 this.userNameText_.getText(), this.passwordText_.getText());
        
        return dirty;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelPressed()
    {
        super.cancelPressed();
    }

    /**
     * �ݒ���i�[����B
     */
    public void storeSettings()
    {
        String dbName = null;
        if (this.postgresRadio_.getSelection() == true)
        {
            dbName = DBPreferenceUtil.POSTGRE_SQL_NAME;
        }
        else
        {
            dbName = DBPreferenceUtil.H2_NAME;
        }
        DBPreferenceUtil.storeSettings(dbName, this.baseDirText_.getText(),
                                       this.hostText_.getText(), this.portText_.getText(),
                                       this.userNameText_.getText(), this.passwordText_.getText());
    }

    /**
     * �f�[�^�x�[�X��ʁuH2�v���������ꂽ�ۂ̃R���|�[�l���g�̏�ԑJ�ڂ��s��
     */
    private void selectH2Radio()
    {
        this.baseDirText_.setEnabled(true);
        this.buttonReferDbFolder_.setEnabled(true);
        this.hostText_.setEnabled(false);
        this.portText_.setEnabled(false);
        this.userNameText_.setEnabled(false);
        this.passwordText_.setEnabled(false);
    }

    /**
     * �f�[�^�x�[�X��ʁuPostgres�v���������ꂽ�ۂ̃R���|�[�l���g�̏�ԑJ�ڂ��s��
     */
    private void selectPostgresRadio()
    {
        this.baseDirText_.setEnabled(false);
        this.buttonReferDbFolder_.setEnabled(false);
        this.hostText_.setEnabled(true);
        this.portText_.setEnabled(true);
        this.userNameText_.setEnabled(true);
        this.passwordText_.setEnabled(true);
    }

    /**
     * �{�^���A�e�L�X�g�̏��������s�Ȃ��܂��B
     */
    public void initialize()
    {
        IPreferenceStore store = ENdoSnipeDataAccessorPlugin.getDefault().getPreferenceStore();
        String dbName = store.getString(DBPreferenceUtil.DBNAME_STORE);
        String baseDir = store.getString(DBPreferenceUtil.BASEDIR_STORE);
        String host = store.getString(DBPreferenceUtil.HOST_STORE);
        String port = store.getString(DBPreferenceUtil.PORT_STORE);
        String userName = store.getString(DBPreferenceUtil.USERNAME_STORE);
        String password = store.getString(DBPreferenceUtil.PASSWORD_STORE);

        if (isNotEmpty(baseDir))
        {
            this.baseDirText_.setText(baseDir);
        }
        if (isNotEmpty(host))
        {
            this.hostText_.setText(host);
        }
        if (isNotEmpty(port))
        {
            this.portText_.setText(port);
        }
        if (isNotEmpty(userName))
        {
            this.userNameText_.setText(userName);
        }
        if (isNotEmpty(password))
        {
            this.passwordText_.setText(password);
        }
        if (isNotEmpty(dbName) && DBPreferenceUtil.POSTGRE_SQL_NAME.equals(dbName))
        {
            this.h2Radio_.setSelection(false);
            this.postgresRadio_.setSelection(true);
            selectPostgresRadio();
        }
        else
        {
            this.h2Radio_.setSelection(true);
            this.postgresRadio_.setSelection(false);
            selectH2Radio();
        }
    }

    /**
     * ���蕶���񂪋󂩂ǂ����𔻒�
     * @param targetStr ����p������
     * @return true  null���A�󕶎��񂾂����ꍇ
     *         false �ǂ���̏����ɂ����Ă͂܂�Ȃ������ꍇ
     */
    private boolean isNotEmpty(String targetStr)
    {
        return targetStr != null && (!targetStr.equals(""));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configureShell(final Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText(DataAccessorMessages.getMessage(DIALOG_KEY));
    }

    /**
     * �{�^���N���b�N���̑��삪�s��ꂽ�Ƃ��̏������s���N���X�B
     *
     * @author fujii
     */
    private class OpenDbDialogController implements SelectionListener
    {
        /**
         * {@inheritDoc}
         */
        public void widgetDefaultSelected(final SelectionEvent e)
        {
            // Do nothing.
        }

        /**
         * {@inheritDoc}
         */
        public void widgetSelected(SelectionEvent e)
        {
            if (DBSettingDialog.this.buttonReferDbFolder_ == e.widget)
            {
                // �t�H���_�I���_�C�A���O���J��
                String initialPath = DBSettingDialog.this.baseDirText_.getText();
                DirectoryDialog openDialog = new DirectoryDialog(getShell(), SWT.NULL);
                openDialog.setFilterPath(initialPath);
                openDialog.setMessage(DataAccessorMessages.getMessage("data.accessor.choosedb"));
                String folderName = openDialog.open();
                if (folderName != null)
                {
                    DBSettingDialog.this.baseDirText_.setText(folderName);
                }
            }
        }
    }
}

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
package jp.co.acroquest.endosnipe.data.util;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;
import jp.co.acroquest.endosnipe.common.util.SQLUtil;
import jp.co.acroquest.endosnipe.data.DBInitializer;
import jp.co.acroquest.endosnipe.data.ENdoSnipeDataAccessorPlugin;
import jp.co.acroquest.endosnipe.data.ENdoSnipeDataAccessorPluginProvider;
import jp.co.acroquest.endosnipe.data.LogMessageCodes;
import jp.co.acroquest.endosnipe.data.db.ConnectionManager;
import jp.co.acroquest.endosnipe.data.db.DBManager;
import jp.co.acroquest.endosnipe.data.preference.BaseDirectoryChangableListener;
import jp.co.acroquest.endosnipe.data.preference.DatabaseItem;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * DataAccessor �̐ݒ���������[�e�B���e�B�N���X�B<br />
 *
 * @author sakamoto
 */
public class DataAccessorConfigUtil implements LogMessageCodes
{
    /** ���K�[ */
    private static final ENdoSnipeLogger               LOGGER               =
                                                                                ENdoSnipeLogger.getLogger(
                                                                                                          DataAccessorConfigUtil.class,
                                                                                                          ENdoSnipeDataAccessorPluginProvider.INSTANCE);

    /** DB ��ʂ��擾���邽�߂̃L�[ */
    private static final String                        PREF_DB_KIND_KEY     =
                                                                                "database.dbname";

    /** DB �̃t�H���_�̃p�X���擾���邽�߂̃L�[ */
    private static final String                        PREF_DB_DIR_KEY      = "database.dir";

    /** �ڑ���DB�̃z�X�g���擾���邽�߂̃L�[ */
    private static final String                        PREF_DB_HOST_KEY     = "database.host";

    /** �ڑ���DB�̃|�[�g�ԍ����擾���邽�߂̃L�[ */
    private static final String                        PREF_DB_PORT_KEY     = "database.port";

    /** �ڑ���DB�̃��[�U�����擾���邽�߂̃L�[ */
    private static final String                        PREF_DB_USER_KEY     = "database.user";

    /** �ڑ���DB�̃p�X���[�h���擾���邽�߂̃L�[ */
    private static final String                        PREF_DB_PASS_KEY     = "database.password";

    /** DB ��ʂ̃f�t�H���g�l */
    public static final String                         PREF_DB_KIND_DEFAULT = "H2";

    /** DB �̃t�H���_�̃p�X�̃f�t�H���g�l */
    public static final String                         PREF_DB_DIR_DEFAULT  =
                                                                                ".metadata/.plugins/jp.co.acroquest.endosnipe.data/db";

    /** �ڑ���DB�̃z�X�g�̃f�t�H���g�l */
    public static final String                         PREF_DB_HOST_DEFAULT = "localhost";

    /** �ڑ���DB�̃|�[�g�ԍ��̃f�t�H���g�l */
    public static final String                         PREF_DB_PORT_DEFAULT = "5432";

    /** �ڑ���DB�̃��[�U���̃f�t�H���g�l */
    public static final String                         PREF_DB_USER_DEFAULT = "endosnipe";

    /** �ڑ���DB�̃p�X���[�h�̃f�t�H���g�l */
    public static final String                         PREF_DB_PASS_DEFAULT = "endosnipe";

    /** PostgreSQL�f�[�^�x�[�X�̃h���C�o�N���X���� */
    public static final String                         POSTGRES_DRIVER      =
                                                                                "org.postgresql.Driver";

    /** BaseDirectory
     *  */
    private static Set<BaseDirectoryChangableListener> changableListeners__ =
                                                                                new HashSet<BaseDirectoryChangableListener>();

    static
    {
        setDefaultPreference();
    }

    /**
     * �R���X�g���N�^���B�����܂��B<br />
     */
    private DataAccessorConfigUtil()
    {
        // Do nothing.
    }

    /**
     * DataAccessorPlugin �Ŏw�肳�ꂽ�f�[�^�x�[�X�̊�f�B���N�g����Ԃ��܂��B<br />
     *
     * @return �f�[�^�x�[�X�̊�f�B���N�g��
     */
    public static String getDatabaseDirectory()
    {
        IPreferenceStore store = ENdoSnipeDataAccessorPlugin.getDefault().getPreferenceStore();
        String dbdir = store.getString(PREF_DB_DIR_KEY);
        return dbdir;
    }

    /**
     * �f�[�^�x�[�X�̊�f�B���N�g�����v���t�@�����X�X�g�A�ɃZ�b�g���܂��B<br />
     *
     * @param directory �f�[�^�x�[�X�̊�f�B���N�g��
     */
    public static void setDatabaseDirectory(final String directory)
    {
        IPreferenceStore store = ENdoSnipeDataAccessorPlugin.getDefault().getPreferenceStore();
        store.setValue(PREF_DB_DIR_KEY, directory);
    }

    /**
     * �f�t�H���g�̃f�[�^�x�[�X�̊�f�B���N�g����Ԃ��܂��B<br />
     *
     * @return �f�[�^�x�[�X�̊�f�B���N�g��
     */
    public static String getDefaultDatabaseDirectory()
    {
        IPreferenceStore store = ENdoSnipeDataAccessorPlugin.getDefault().getPreferenceStore();
        String dbdir = store.getDefaultString(PREF_DB_DIR_KEY);
        return dbdir;
    }

    /**
     * �f�t�H���g�̐ݒ���v���t�@�����X�X�g�A�ɃZ�b�g���܂�
     */
    public static void setDefaultPreference()
    {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IPath workspacePath = workspace.getRoot().getLocation();
        File file = workspacePath.toFile();
        file = new File(file.getAbsolutePath(), PREF_DB_DIR_DEFAULT);
        IPreferenceStore store = ENdoSnipeDataAccessorPlugin.getDefault().getPreferenceStore();
        store.setDefault(PREF_DB_DIR_KEY, file.toString());
        store.setDefault(PREF_DB_KIND_KEY, PREF_DB_KIND_DEFAULT);
        store.setDefault(PREF_DB_HOST_KEY, PREF_DB_HOST_DEFAULT);
        store.setDefault(PREF_DB_PORT_KEY, PREF_DB_PORT_DEFAULT);
        store.setDefault(PREF_DB_USER_KEY, PREF_DB_USER_DEFAULT);
        store.setDefault(PREF_DB_PASS_KEY, PREF_DB_PASS_DEFAULT);
    }

    /**
     * ��f�B���N�g���z���ɂ���f�[�^�x�[�X�̈ꗗ��Ԃ��܂��B<br />
     *
     * @return �f�[�^�x�[�X�ꗗ
     */
    public static List<DatabaseItem> getDatabaseList()
    {
        if (DBManager.isDefaultDb() == true)
        {
            String baseDirectory = DBManager.getDbDir();
            return getH2DatabaseList(baseDirectory);
        }
        return getPostgresDatabaseList();
    }

    /**
     * �w�肳�ꂽ�f�B���N�g�������ɂ���H2�f�[�^�x�[�X�̈ꗗ��Ԃ��܂��B<br />
     *
     * @param baseDirectory �f�B���N�g��
     * @return �f�[�^�x�[�X�ꗗ
     */
    public static List<DatabaseItem> getH2DatabaseList(final String baseDirectory)
    {
        List<DatabaseItem> databaseList = new ArrayList<DatabaseItem>();

        // baseDirectory�����ɂ���f�B���N�g�����ADB�̌��
        File baseFolder = new File(baseDirectory);
        File[] directoryArray = null;
        if (baseFolder.isDirectory())
        {
            directoryArray = baseFolder.listFiles();
        }
        if (directoryArray == null)
        {
            return databaseList;
        }

        // �f�[�^�x�[�X�����݂��Ȃ��ꍇ�A�f�[�^�x�[�X���쐬���Ȃ����[�h�ɂ���
        ConnectionManager connectionManager = ConnectionManager.getInstance();
        String prevDbDir = DBManager.getDbDir();
        connectionManager.setBaseDir(baseDirectory);

        for (File folderItem : directoryArray)
        {
            if (folderItem.isDirectory())
            {
                String folderName = folderItem.getName();
                try
                {
                    DatabaseItem databaseItem = DatabaseItem.createDatabaseItem(folderName);
                    if (databaseItem != null)
                    {
                        // �������f�[�^�x�[�X�i���z�X�g��񂪑��݂���j�̂Ƃ��̂݃��X�g�ɒǉ�����
                        databaseList.add(databaseItem);
                    }
                }
                catch (SQLException ex)
                {
                    LOGGER.log(DB_ACCESS_ERROR, ex, ex.getMessage());
                }
            }
        }

        // ���̐ݒ�E���[�h�ɖ߂�
        connectionManager.setBaseDir(prevDbDir);

        return databaseList;
    }

    /**
     * �w�肳�ꂽ�ڑ���ɑ��݂���PostgreSQL�f�[�^�x�[�X�̈ꗗ��Ԃ��܂��B<br />
     * @return �f�[�^�x�[�X�ꗗ
     */
    public static List<DatabaseItem> getPostgresDatabaseList()
    {
        List<DatabaseItem> resultList = new ArrayList<DatabaseItem>();
        List<String> databaseNameList = getPostgresDatabaseNameList();

        //�e�f�[�^�x�[�X�ɑ΂���DataSource���擾
        for (String databaseName : databaseNameList)
        {
            Connection connection = null;
            try
            {
                connection =
                    ConnectionManager.getInstance().getConnection(databaseName, true, false);
                
                // ����������Ă��Ȃ��ꍇ�͖�������B
                if (DBInitializer.isInitialized(connection) == false)
                {
                    continue;
                }
            }
            catch (SQLException ex)
            {
                continue;
            }
            finally
            {
               if (connection != null)
               {
                   SQLUtil.closeConnection(connection);
               }
            }
            
            try
            {
                DatabaseItem target = DatabaseItem.createDatabaseItem(databaseName);
                if (target != null)
                {
                    resultList.add(target);
                }
            }
            catch (SQLException ex)
            {
                continue;
            }
        }
        return resultList;
    }

    /**
     * PostgreSQL�̃f�[�^�x�[�X�ꗗ���擾����B<br />
     * 
     * @return �f�[�^�x�[�X�ꗗ
     */
    private static List<String> getPostgresDatabaseNameList()
    {
        List<String> databaseNameList = new ArrayList<String>();
        try
        {
            Class.forName(POSTGRES_DRIVER);
        }
        catch (ClassNotFoundException ex)
        {
            return databaseNameList;
        }
        String baseUri = createDatabaseURI(DBManager.getHostName(), DBManager.getPort());
        Connection connection = null;
        Statement state = null;
        ResultSet rs = null;

        //pg_database�e�[�u���͖��̂����g�p���Ȃ����߁ADao���͎g�p�����A���ŃA�N�Z�X���A�P���R�[�h�ڂ��擾����
        try
        {
            connection =
                DriverManager.getConnection(baseUri, DBManager.getUserName(),
                                            DBManager.getPassword());
            state = connection.createStatement();
            rs = state.executeQuery("SELECT * FROM pg_database where datistemplate = false;");
            while (rs.next() == true)
            {
                String databaseName = rs.getString(1);
                databaseNameList.add(databaseName);
            }
        }
        catch (SQLException sqlex)
        {
            return databaseNameList;
        }
        finally
        {
            SQLUtil.closeResultSet(rs);
            SQLUtil.closeStatement(state);
            SQLUtil.closeConnection(connection);
        }
        return databaseNameList;
    }

    /**
     * �f�[�^�x�[�X�̊�f�B���N�g���̕ύX�������邩�ǂ�����ʒm���郊�X�i��ǉ����܂��B<br />
     *
     * @param listener ���X�i
     */
    public static void addBaseDirectoryChangableListener(
        final BaseDirectoryChangableListener listener)
    {
        changableListeners__.add(listener);
    }

    /**
     * ��f�B���N�g���̕ύX�������邩�ǂ�����Ԃ��܂��B<br />
     *
     * @return ��f�B���N�g���̕ύX��������ꍇ�� <code>true</code> �A
     *         �ύX�������Ȃ��ꍇ�� <code>false</code>
     */
    public static boolean isBaseDirectoryChangeAllowed()
    {
        boolean ret = true;
        for (BaseDirectoryChangableListener listener : changableListeners__)
        {
            if (!listener.isChangeBaseDirectoryAllowed())
            {
                // 1 �ł��ύX�������Ȃ����X�i������΁A�ύX�������Ȃ�
                ret = false;
                break;
            }
        }
        return ret;
    }

    /**
     * PostgreSQL�f�[�^�x�[�X�p�̐ڑ���������쐬
     * @param host �z�X�g
     * @param port �|�[�g
     * @return �ڑ�������
     */
    public static String createDatabaseURI(final String host, final String port)
    {
        String base = "jdbc:postgresql://";
        String uri = base + host + ":" + port + "/";
        return uri;
    }

}

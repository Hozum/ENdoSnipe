/*
 * Copyright (c) 2004-2011 SMG Co., Ltd. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 *
 * THE  SOFTWARE IS  PROVIDED BY  SMG Co., Ltd., WITHOUT  WARRANTY  OF
 * ANY KIND,  EXPRESS  OR IMPLIED,  INCLUDING BUT  NOT LIMITED  TO THE
 * WARRANTIES OF  MERCHANTABILITY,  FITNESS FOR A  PARTICULAR  PURPOSE
 * AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDER BE LIABLE FOR ANY
 * CLAIM, DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package jp.co.acroquest.endosnipe.report;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jp.co.acroquest.endosnipe.collector.config.AgentSetting;
import jp.co.acroquest.endosnipe.collector.config.DataCollectorConfig;
import jp.co.acroquest.endosnipe.collector.exception.InitializeException;
import jp.co.acroquest.endosnipe.common.util.IOUtil;
import jp.co.acroquest.endosnipe.data.dao.JavelinLogDao;
import jp.co.acroquest.endosnipe.data.db.DBManager;
import jp.co.acroquest.endosnipe.data.db.DatabaseType;
import jp.co.acroquest.endosnipe.data.entity.JavelinLog;
import jp.co.acroquest.endosnipe.report.controller.ReportPublishTask;
import jp.co.acroquest.endosnipe.report.controller.ReportSearchCondition;
import jp.co.acroquest.endosnipe.report.controller.ReportType;
import jp.co.acroquest.endosnipe.report.mock.MockIProgressMonitor;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;

/**
 * BottleneckEye���N�������ɁA���|�[�g�쐬���s�����߂̃N���X�ł��B<br>
 * 
 * @author iida
 */
public class ReporterMain {
	/** ���̃R�}���h�𓮍삳����̂ɕK�v�ȃR�}���h���C�������̐��B */
	private static final int REQUIRED_CMDLINE_PARAMS = 5;

	/** �I�v�V�������܂߂��ꍇ�̈����̐� */
	private static final int OPTIONAL_CMDLINE_PARAMS = 6;

	/** ���|�[�g�o�̓f�B���N�g�����̃f�t�H���g�B */
	private static final String REPORT_OUTPUT_DIR = "../reports";

	/** �J�n�^�I���������w�肷�镶����`���B */
	private static final String TIME_FORMAT = "yyyyMMdd_HHmmss";

	/** args[]: mode */
	private static final int ARGS_INDEX_MODE = 0;

	/** args[]: IndexNumber or DatabaseName */
	private static final int ARGS_INDEX_DB_IDENTITY = 1;

	/** args[]: fmTimeStr */
	private static final int ARGS_INDEX_FM_TIME_STR = 2;

	/** args[]: toTimeStr */
	private static final int ARGS_INDEX_TO_TIME_STR = 3;

	/** args[]: toOutoutReport */
	private static final int ARGS_INDEX_OUTPUT_REPORT = 4;

	/** args[]: full output */
	private static final int ARGS_INDEX_FULL_MODE = 5;

	/** �C���f�b�N�X�ԍ��w��̃I�v�V���� */
	private static final String OPTION_INDEX_SPECIFY = "-i";

	/** �f�[�^�x�[�X���w��̃I�v�V���� */
	private static final String OPTION_NAME_SPECIFY = "-d";

	/** �S�Ẵ��|�[�g���o�͂��邩�ǂ��� */
	private static final String FULL_MODE = "full";

	/** �V�X�e���v���p�e�B�ɂ��A���|�[�g�o�͐���w�肷��B */
	private static final String REPORT_PATH = "report.path";

	/**
	 * ���|�[�g�쐬���s���܂��B<br/>
	 * 
	 * @param args
	 *            �R�}���h���C�������B<br/>
	 *            [0]="-i"(�C���f�b�N�X�w��)�܂���"-d"(�f�[�^�x�[�X���w��)<br/>
	 *            [1]=�C���f�b�N�X�ԍ�or�f�[�^�x�[�X��<br/>
	 *            [2]=���|�[�g�쐬����(�J�n)�B "yyyyMMdd_HHmmss"�`���Ŏw�肷��B<br/>
	 *            [3]=���|�[�g�쐬����(�I��)�B "yyyyMMdd_HHmmss"�`���Ŏw�肷��B<br/>
	 *            [4]=�ڍ׏o��
	 */
	public static void main(String[] args) {
		// �f�[�^�x�[�X���C���f�b�N�X�w�肷�邩
		boolean use_index = true;
		// �ڍ׏o�͂��s����
		boolean full = false;

		// �R�}���h���C�������̐����`�F�b�N
		if (args.length != REQUIRED_CMDLINE_PARAMS
				&& args.length != OPTIONAL_CMDLINE_PARAMS) {
			usage();
			System.exit(1);
		}

		// ��P�A�Q�����̔���
		if (OPTION_INDEX_SPECIFY.equals(args[ARGS_INDEX_MODE])) {
			// �C���f�b�N�X�w��̏ꍇ�́A��Q���������l���`�F�b�N
			if (!checkIndexNo(args[ARGS_INDEX_DB_IDENTITY])) {
				usage();
				System.exit(1);
			}
		} else if (OPTION_NAME_SPECIFY.equals(args[ARGS_INDEX_MODE])) {
			use_index = false;
		} else {
			usage();
			System.exit(1);
		}

		// �f�[�^�擾���Ԃ̐ݒ肩���R�A�S�����𔻒肷��
		Calendar fmTime = Calendar.getInstance();
		Calendar toTime = Calendar.getInstance();

		// �����Ŏw�肳�ꂽ�����񂩂�Date�I�u�W�F�N�g����
		SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
		try {
			// �J�n����
			Date fmDate = dateFormat.parse(args[ARGS_INDEX_FM_TIME_STR]);
			fmTime.setTime(fmDate);

			// �I������
			Date toDate = dateFormat.parse(args[ARGS_INDEX_TO_TIME_STR]);
			toTime.setTime(toDate);
		} catch (ParseException exception) {
			// ��̓G���[�ƂȂ����ꍇ�͏I��
			System.err.println("�����w��̌`�����s���ł��B");
			System.exit(1);
		}

		// �J�n�������I��������薢�����w���Ă����ꍇ�̓G���[
		if (fmTime.compareTo(toTime) > 0) {
			System.err.println("�J�n�������I��������薢�����w���Ă��܂��B");
			System.exit(1);
		}

		// �ڍ׏o�͂��s����
		if (args.length == OPTIONAL_CMDLINE_PARAMS) {
			if (FULL_MODE.equals(args[ARGS_INDEX_FULL_MODE])) {
				full = true;
			} else {
				usage();
				System.exit(1);
			}
		}

		// ���O�o�̓C���X�^���X�𐶐�����
		System.err.println("ENdoSnipeReportCommand���J�n���܂��B");

		// ���|�[�g�o�͑Ώۂ̃C���f�b�N�X�ԍ��B(collector.properties�̐ݒ�)
		int IndexNo = -1;

		// �f�[�^�x�[�X�ڑ��ݒ�
		String dbHost = null;
		String dbPort = null;
		String dbUser = null;
		String dbPass = null;
		String dbName = null;

		// ���|�[�g�o�͐�f�B���N�g��
		String reportPath = null;

		if (use_index) {
			try {
				IndexNo = Integer.parseInt(args[ARGS_INDEX_DB_IDENTITY]);
			} catch (NumberFormatException ex) {
				System.err.println("�C���f�b�N�X�ԍ�(" + args[ARGS_INDEX_DB_IDENTITY]
						+ ")���s���ł��B�������w�肵�Ă��������B");
				System.exit(1);
			}
		}

		// collector.properties ����e��ݒ�l��ǂݍ���
		DataCollectorConfig config = null;
		try {
			config = ConfigLoader.loadConfig();
		} catch (InitializeException ie) {
			System.err.println(ie);
			System.exit(1);
		}

		if (config == null) {
			System.exit(1);
		}

		// DB��H2�̏ꍇ�͏I��
		DatabaseType type = config.getDatabaseType();
		if (type != DatabaseType.POSTGRES) {
			System.err
					.println("���̃v���O������PostgreSQL��p�ł��Bcollector.properties���C�����Ă��������B");
			System.exit(1);
		}

		// DB�̏��ݒ���擾
		dbHost = config.getDatabaseHost();
		dbPort = config.getDatabasePort();
		dbUser = config.getDatabaseUserName();
		dbPass = config.getDatabasePassword();

		reportPath = args[ARGS_INDEX_OUTPUT_REPORT];

		// �eDB�ݒ�̃��X�g�擾
		List<AgentSetting> agentSettings = config.getAgentSettingList();

		// �C���f�b�N�X�ԍ������DB�����擾����ꍇ
		if (use_index) {
			if (IndexNo > agentSettings.size()) {
				System.err.println("�C���f�b�N�X�ԍ�(" + IndexNo
						+ ")�ɊY������f�[�^�x�[�X�̐ݒ肪����܂���B�I�����܂��B");
				System.exit(1);
			}
			AgentSetting agentSetting = agentSettings.get(IndexNo);
			dbName = agentSetting.databaseName;
		}
		// DB���w��̏ꍇ
		else {
			dbName = args[ARGS_INDEX_DB_IDENTITY];
		}

		// ���|�[�g�쐬�Ɏg�p����DB���w�肷��
		DBManager.updateSettings(false, "", dbHost, dbPort, dbName, dbUser,
				dbPass);

		// ���|�[�g�쐬���̊e�ݒ���s��
		ReportType[] outputReportTypes;

		// ���ׂẴ��|�[�g���o�͂��邩�ǂ����������ɂ��w�肷��B
		if (full) {
			outputReportTypes = new ReportType[] { ReportType.SUMMARY,
					ReportType.SYSTEM, ReportType.PROCESS, ReportType.DATA_IO,
					ReportType.VM_STATUS, ReportType.OBJECT,
					ReportType.SERVER_POOL, ReportType.POOL_SIZE,
					ReportType.APPLICATION, ReportType.JAVELIN,
					ReportType.PERF_DOCTOR, ReportType.RESPONSE_LIST,
					ReportType.RESPONSE_SUMMARY };
		} else {
			outputReportTypes = new ReportType[] { ReportType.SUMMARY,
					ReportType.SYSTEM, ReportType.PROCESS, ReportType.DATA_IO,
					ReportType.VM_STATUS, ReportType.OBJECT,
					ReportType.SERVER_POOL, ReportType.POOL_SIZE,
					ReportType.APPLICATION, ReportType.JAVELIN,
					ReportType.PERF_DOCTOR, ReportType.RESPONSE_SUMMARY };
		}

		// TODO PerformanceDoctor �̃��[���ݒ��L���ɂ���B
		// // PerformanceDoctor�̃��[����ݒ肷��
		// int selectionIndex = 0;
		// RuleManager ruleManager = RuleManager.getInstance();
		// RuleSetConfig[] ruleSetConfigs = ruleManager.getRuleSetConfigs();
		// String id = ruleSetConfigs[selectionIndex].getId();
		// ruleManager.changeActiveRuleSetByID(id);

		Runnable callback = null;

		// ���|�[�g�o�̓f�B���N�g�������肵�A���݂��Ȃ���΍쐬����
		// ���|�[�g�o�͐�f�B���N�g���F
		// <current-dir>/reports/<db-name>/<from>-<to>/
		SimpleDateFormat format = new SimpleDateFormat(TIME_FORMAT);
		String start = format.format(fmTime.getTime());
		String end = format.format(toTime.getTime());
		String leafDirectoryName = start + "-" + end;

		String outputFilePath;
		if (use_index) {
			// �C���f�b�N�X�w��̏ꍇ�̃��|�[�g�o�͐�
			outputFilePath = reportPath + File.separator
					+ String.valueOf(IndexNo) + File.separator
					+ leafDirectoryName;
		} else {
			// �f�[�^�x�[�X���w��̏ꍇ�̃��|�[�g�o�͐�
			outputFilePath = reportPath + File.separator + dbName
					+ File.separator + leafDirectoryName;
		}

		File outputDir = new File(outputFilePath);
		if (outputDir.exists() == false) {
			outputDir.mkdirs();
		}

		// TODO �i�荞�݂̃��[����ݒ肷��
		boolean limitSameCause = false;
		boolean limitBySameRule = false;

		// ���|�[�g�o�͊��Ԃ̏�����ݒ肷��
		ReportSearchCondition searchCondition = new ReportSearchCondition();
		searchCondition.setDatabases(Arrays.asList(dbName));
		searchCondition.setStartDate(new Timestamp(fmTime.getTimeInMillis()));
		searchCondition.setEndDate(new Timestamp(toTime.getTimeInMillis()));
		searchCondition.setOutputFilePath(outputFilePath);
		searchCondition.setLimitSameCause(limitSameCause);
		searchCondition.setLimitBySameRule(limitBySameRule);

		System.err.println("���|�[�g�̐������J�n���܂��B");
		System.err.println("Parameters: Index DB = " + dbHost + ":" + dbPort
				+ "/" + dbName + ", Span = " + args[ARGS_INDEX_FM_TIME_STR]
				+ " - " + args[ARGS_INDEX_TO_TIME_STR]);
		System.err.println("���|�[�g�o�̓f�B���N�g��: " + outputFilePath);

		// ReportPublishTask�����s���A���|�[�g�쐬���s��
		try {
			ReportPublishTask reportTask = new ReportPublishTask(
					searchCondition, outputReportTypes, callback);
			reportTask.setUser(true);
			// run���\�b�h�𒼐ڌĂяo��
			reportTask.run(new MockIProgressMonitor());
		} catch (Exception e) {
			System.err.println("���|�[�g�������ɃG���[���������܂����B");
			System.exit(1);
		}

		System.err.println("���|�[�g�̐������I�����܂����B");

		System.err.println("jvn���O�̏o�͂��J�n���܂��B");
		if (!outputJvnLog(dbName, new Timestamp(fmTime.getTimeInMillis()),
				new Timestamp(toTime.getTimeInMillis()), outputFilePath
						+ File.separator + "jvn_logs"))
			System.exit(1);
		System.err.println("jvn���O�̏o�͂��I�����܂����B");

		// zip���k����
		Project project = new Project();
		project.init();

		try {
			File baseDir = new File(outputFilePath);
			Zip zipper = new Zip();
			zipper.setProject(project);
			zipper.setTaskName("zip");
			zipper.setTaskType("zip");
			zipper.setDestFile(new File(outputFilePath + ".zip"));
			zipper.setBasedir(baseDir);
			zipper.execute();
			System.err.println("���|�[�g��zip�����I�����܂����B");

			// zip���ɐ��������猳�̃f�B���N�g���͍폜����
			boolean deleted = deleteDir(baseDir);
			if (deleted == false) {
				System.err.println("���|�[�g�쐬���̃f�B���N�g���폜�Ɏ��s���܂����B");
			}
		} catch (BuildException bex) {
			System.err.println("���|�[�g��zip���Ɏ��s���܂����B");
		}
	}

	/**
	 * Javelin���O���f�[�^�x�[�X����ǂݍ��݁A�t�@�C���o�͂���B
	 * 
	 * @param database
	 *            �f�[�^�x�[�X��
	 * @param start
	 *            �J�n����
	 * @param end
	 *            �J�n����
	 * @param outputDir
	 *            �o�͐�f�B���N�g��
	 * 
	 * @return {@code true}����/{@code false}���s
	 */
	private static boolean outputJvnLog(String database, Timestamp start,
			Timestamp end, String outputDir) {
		File outputDirFile = new File(outputDir);
		boolean isSuccess = outputDirFile.mkdirs();
		if (isSuccess == false) {
			System.err.println("jvn���O�o�̓f�B���N�g���̍쐬�Ɏ��s���܂����B");
			return false;
		}

		try {
			List<JavelinLog> jvnLogList = JavelinLogDao.selectByTermWithLog(
					database, start, end);
			for (JavelinLog log : jvnLogList) {
				String fileName = log.logFileName;
				OutputStream output = null;
				try {
					output = new BufferedOutputStream(new FileOutputStream(
							outputDir + File.separator + fileName));
					IOUtil.copy(log.javelinLog, output);
				} catch (FileNotFoundException fnfe) {
					System.err.println("jvn���O�o�̓f�B���N�g����������܂���B");
				} catch (IOException ioe) {
					System.err.println("jvn���O�o�͒��ɗ�O���������܂����B");
				} finally {
					if (output != null) {
						try {
							output.close();
						} catch (IOException ioe) {
							System.err.println("jvn���O�N���[�Y���ɗ�O���������܂����B");
						}
					}
				}
			}

		} catch (SQLException sqle) {
			System.err.println("DB�����jvn���O�ǂݍ��ݒ��ɗ�O���������܂����B");
			return false;
		}

		return true;
	}

	/**
	 * �w�肵���f�B���N�g�����ƍ폜����B
	 * 
	 * @param dir
	 *            �폜����f�B���N�g���B
	 * @return �f�B���N�g���̍폜�Ɏ��s�����ꍇ�B
	 */
	private static boolean deleteDir(File dir) {
		boolean result = true;
		File[] children = dir.listFiles();
		for (File child : children) {
			if (child.isDirectory() == true) {
				// �f�B���N�g���͍ċA���č폜���s��
				result = deleteDir(child);
				if (result == false) {
					break;
				}
			} else {
				// �t�@�C���͒P�ɍ폜���s��
				result = child.delete();
				if (result == false) {
					break;
				}
			}
		}

		// �S�Ă̍폜�ɐ������Ă���Β��g�͋�Ȃ̂ŁA�����̃f�B���N�g�����폜����
		if (result == true) {
			result = dir.delete();
		}

		return result;
	}

	/**
	 * �w�肳�ꂽ�����񂪎��R�������������肷��B<br/>
	 * (0����n�܂�ꍇ�͕�����Ƃ݂Ȃ�)
	 * 
	 * @param text
	 *            ����Ώ�
	 * 
	 * @return {@code true}���l�ł���/{@code false}���l�ł͂Ȃ�
	 */
	private static boolean checkIndexNo(String text) {
		return text.matches("^[1-9][0-9]*$");
	}

	/**
	 * �R�}���h���C�������̎g�p���@��������܂��B
	 */
	private static void usage() {
		System.err
				.println("USAGE: ReporterMain <-i IndexNo>|<-d DBName> <StartTime> <EndTime> <reportPath> [full]");
		System.err.println("         StartTime/EndTime: yyyyMMdd_HHmmss");
	}
}

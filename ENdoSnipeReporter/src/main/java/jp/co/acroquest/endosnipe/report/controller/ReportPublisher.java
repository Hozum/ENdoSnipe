/*
 * Copyright (c) 2004-2009 SMG Co., Ltd. All Rights Reserved.
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
package jp.co.acroquest.endosnipe.report.controller;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import jp.co.acroquest.endosnipe.perfdoctor.rule.RuleManager;
import jp.co.acroquest.endosnipe.perfdoctor.rule.def.RuleSetConfig;
import jp.co.acroquest.endosnipe.report.controller.ReportPublishTask;
import jp.co.acroquest.endosnipe.report.controller.ReportSearchCondition;
import jp.co.acroquest.endosnipe.report.controller.ReportType;

/**
 * �f�[�^�擾�A���|�[�g�o�͂��Ǘ�����N���X
 * 
 * @author M.Yoshida
 */
public class ReportPublisher {
	/**
	 * �f�[�^�x�[�X����擾�����f�[�^���G�N�Z���ɏo�͂���
	 * 
	 * @param dbName
	 *            �f�[�^�x�[�X����
	 * @param outputReportTypes
	 *            �o�̓��|�[�g���
	 * @param outputFilePath
	 *            �o�̓p�X
	 * @param startDate
	 *            �J�n����
	 * @param endDate
	 *            �I������
	 * @param limitSameCause
	 *            PerformanceDoctor���|�[�g�o�͂ŁA���ꌴ�����i�荞�ނ��ǂ���
	 * @param limitBySameRule
	 *            ���|�[�g�o�͂ŁA���ꃋ�[�����i�荞�ނ��ǂ���
	 * @param selectionIndex
	 * @param callback 
	 */
	public void outputReport(String[] dbName, ReportType[] outputReportTypes,
            String outputFilePath, Date startDate, Date endDate, boolean limitSameCause,
            boolean limitBySameRule, int selectionIndex, Runnable callback)
    {
		
		RuleManager ruleManager = RuleManager.getInstance();
		RuleSetConfig[] ruleSetConfigs = ruleManager.getRuleSetConfigs();
		String id = ruleSetConfigs[selectionIndex].getId();

		ruleManager.changeActiveRuleSetByID(id);

		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String start = format.format(startDate);
		String end   = format.format(endDate);
		String leafDirectoryName = start + "-" + end;
		outputFilePath = outputFilePath + File.separator + leafDirectoryName;
		
		ReportSearchCondition searchCondition = new ReportSearchCondition();

		searchCondition.setDatabases(Arrays.asList(dbName));
		searchCondition.setStartDate(new Timestamp(startDate.getTime()));
		searchCondition.setEndDate(new Timestamp(endDate.getTime()));
		searchCondition.setOutputFilePath(outputFilePath);
		searchCondition.setLimitSameCause(limitSameCause);
		searchCondition.setLimitBySameRule(limitBySameRule);

		File outputDir = new File(outputFilePath);

		if (outputDir.exists() == false) {
			outputDir.mkdirs();
		}

		ReportPublishTask reportTask = new ReportPublishTask(searchCondition,
				outputReportTypes, callback);
		reportTask.setUser(true);
		reportTask.schedule();
	}

	public List<String> getRuleNameList() {
		RuleManager ruleManager = RuleManager.getInstance();
		RuleSetConfig[] ruleSetConfigs = ruleManager.getRuleSetConfigs();

		List<String> result = new ArrayList<String>(ruleSetConfigs.length);
		for (RuleSetConfig config : ruleSetConfigs) {
			result.add(config.getName());
		}

		return result;
	}
}

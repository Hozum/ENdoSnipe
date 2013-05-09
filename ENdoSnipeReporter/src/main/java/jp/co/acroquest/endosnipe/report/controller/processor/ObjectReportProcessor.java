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
package jp.co.acroquest.endosnipe.report.controller.processor;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.co.acroquest.endosnipe.report.LogIdConstants;
import jp.co.acroquest.endosnipe.report.ReporterPluginProvider;
import jp.co.acroquest.endosnipe.report.controller.ReportProcessReturnContainer;
import jp.co.acroquest.endosnipe.report.controller.ReportSearchCondition;
import jp.co.acroquest.endosnipe.report.controller.ReportType;
import jp.co.acroquest.endosnipe.report.controller.TemplateFileManager;
import jp.co.acroquest.endosnipe.report.controller.processor.util.ItemConvertUtil;
import jp.co.acroquest.endosnipe.report.converter.compressor.CompressOperator;
import jp.co.acroquest.endosnipe.report.dao.util.GraphItemAccessUtil;
import jp.co.acroquest.endosnipe.report.entity.ItemData;
import jp.co.acroquest.endosnipe.report.entity.ObjectRecord;
import jp.co.acroquest.endosnipe.report.output.RecordReporter;
import jp.co.acroquest.endosnipe.report.util.ReporterConfigAccessor;
import jp.co.acroquest.endosnipe.common.Constants;
import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;
import jp.co.acroquest.endosnipe.report.controller.processor.ObjectReportProcessor;
import jp.co.acroquest.endosnipe.report.controller.processor.ReportPublishProcessorBase;

/**
 * �I�u�W�F�N�g���̃��|�[�g�𐶐����郌�|�[�g�v���Z�b�T�B
 * 
 * @author akiba
 */
public class ObjectReportProcessor extends ReportPublishProcessorBase
{
    /** ���K�[ */
    private static final ENdoSnipeLogger LOGGER = ENdoSnipeLogger.getLogger(
            ObjectReportProcessor.class, ReporterPluginProvider.INSTANCE);

    /**
     * ReportProcessor�𐶐�����B
     * 
     * @param type ���|�[�g��ʁB
     */
    public ObjectReportProcessor(ReportType type)
    {
        super(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object getReportPlotData(ReportSearchCondition cond,
            ReportProcessReturnContainer reportContainer)
    {
        // ���������̎擾
        String database = cond.getDatabases().get(0);
        Timestamp startTime = cond.getStartDate();
        Timestamp endTime = cond.getEndDate();

        // DB���猟��
        List<ItemData> listData;
        List<ItemData> queueData;
        List<ItemData> setData;
        List<ItemData> mapData;
        List<ItemData> sizeData;
        List<ItemData> countData;
        try
        {
            listData = GraphItemAccessUtil.findItemData(
            		database, Constants.ITEMNAME_JAVAPROCESS_COLLECTION_LIST_COUNT,
    				CompressOperator.SIMPLE_AVERAGE,
    				startTime, endTime);

            queueData = GraphItemAccessUtil.findItemData(
            		database, Constants.ITEMNAME_JAVAPROCESS_COLLECTION_QUEUE_COUNT,
    				CompressOperator.SIMPLE_AVERAGE,
    				startTime, endTime);

            setData = GraphItemAccessUtil.findItemData(
            		database, Constants.ITEMNAME_JAVAPROCESS_COLLECTION_SET_COUNT,
    				CompressOperator.SIMPLE_AVERAGE,
    				startTime, endTime);

            mapData = GraphItemAccessUtil.findItemData(
            		database, Constants.ITEMNAME_JAVAPROCESS_COLLECTION_MAP_COUNT,
    				CompressOperator.SIMPLE_AVERAGE,
    				startTime, endTime);

            sizeData = GraphItemAccessUtil.findItemData(
            		database, Constants.ITEMNAME_JAVAPROCESS_COLLECTION_HISTOGRAM_SIZE,
    				CompressOperator.SIMPLE_AVERAGE,
    				startTime, endTime);

            countData = GraphItemAccessUtil.findItemData(
            		database, Constants.ITEMNAME_JAVAPROCESS_COLLECTION_HISTOGRAM_COUNT,
    				CompressOperator.SIMPLE_AVERAGE,
    				startTime, endTime);
        }
        catch (SQLException ex)
        {
            LOGGER.log(LogIdConstants.EXCEPTION_IN_READING, ex,
                    ReporterConfigAccessor.getReportName(getReportType()));
            return null;
        }

		// �擾�����f�[�^��map�ɂ܂Ƃ߂ă��^�[������
		Map<String, List<? extends Object>> data =
			new HashMap<String, List<? extends Object>>();
		data.put(Constants.ITEMNAME_JAVAPROCESS_COLLECTION_LIST_COUNT, listData);
		data.put(Constants.ITEMNAME_JAVAPROCESS_COLLECTION_QUEUE_COUNT, queueData);
		data.put(Constants.ITEMNAME_JAVAPROCESS_COLLECTION_SET_COUNT, setData);
		data.put(Constants.ITEMNAME_JAVAPROCESS_COLLECTION_MAP_COUNT, mapData);
		data.put(Constants.ITEMNAME_JAVAPROCESS_COLLECTION_HISTOGRAM_SIZE, sizeData);
		data.put(Constants.ITEMNAME_JAVAPROCESS_COLLECTION_HISTOGRAM_COUNT, countData);

		return data;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object convertPlotData(Object rawData, ReportSearchCondition cond,
            ReportProcessReturnContainer reportContainer)
    {
        // �f�[�^�ϊ��͓��ɍs���܂���B
        return rawData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void outputReport(Object convertedData, ReportSearchCondition cond,
            ReportProcessReturnContainer reportContainer)
    {
		// map �̃f�[�^��6�O���t�̌ʂ̃f�[�^�ɕ�����
		Map<String, List<? extends Object>> data =
			(Map<String, List<? extends Object>>) convertedData;

		List<ItemData> listDataList = (List<ItemData>) data
		.get(Constants.ITEMNAME_JAVAPROCESS_COLLECTION_LIST_COUNT);

		List<ItemData> queueDataList = (List<ItemData>) data
		.get(Constants.ITEMNAME_JAVAPROCESS_COLLECTION_QUEUE_COUNT);

		List<ItemData> setDataList = (List<ItemData>) data
		.get(Constants.ITEMNAME_JAVAPROCESS_COLLECTION_SET_COUNT);

		List<ItemData> mapDataList = (List<ItemData>) data
		.get(Constants.ITEMNAME_JAVAPROCESS_COLLECTION_MAP_COUNT);

		List<ItemData> sizeDataList = (List<ItemData>) data
		.get(Constants.ITEMNAME_JAVAPROCESS_COLLECTION_HISTOGRAM_SIZE);

		List<ItemData> countDataList = (List<ItemData>) data
		.get(Constants.ITEMNAME_JAVAPROCESS_COLLECTION_HISTOGRAM_COUNT);

        // �o�͂��郌�|�[�g�̎�ނɂ��킹�ăe���v���[�g�̃t�@�C���p�X���擾����
        String templateFilePath;
        try
        {
            templateFilePath =
            	TemplateFileManager.getInstance().getTemplateFile(ReportType.OBJECT_ITEM);
        }
        catch (IOException exception)
        {
            reportContainer.setHappendedError(exception);
            return;
        }

        // ���|�[�g�o�͂̈��������擾����
		String outputFolderPath = getOutputFolderName()
		+ File.separator
		+ ReporterConfigAccessor.getProperty(super.getReportType()
				.getId()
				+ ".outputFile");
        Timestamp startTime = cond.getStartDate();
        Timestamp endTime = cond.getEndDate();
        

        // ���|�[�g�o�͂����s����
        RecordReporter<ObjectRecord> reporter = new RecordReporter<ObjectRecord>(getReportType());
        reporter.outputReports(
        		templateFilePath, outputFolderPath + File.separator + Constants.ITEMNAME_JAVAPROCESS_COLLECTION_LIST_COUNT,
        		listDataList, startTime, endTime);
        reporter.outputReports(
        		templateFilePath, outputFolderPath + File.separator + Constants.ITEMNAME_JAVAPROCESS_COLLECTION_QUEUE_COUNT,
        		queueDataList, startTime, endTime);
        reporter.outputReports(
        		templateFilePath, outputFolderPath + File.separator + Constants.ITEMNAME_JAVAPROCESS_COLLECTION_SET_COUNT,
        		setDataList, startTime, endTime);
        reporter.outputReports(
        		templateFilePath, outputFolderPath + File.separator + Constants.ITEMNAME_JAVAPROCESS_COLLECTION_MAP_COUNT,
        		mapDataList, startTime, endTime);
        reporter.outputReports(
        		templateFilePath,
        		outputFolderPath + File.separator + Constants.ITEMNAME_JAVAPROCESS_COLLECTION_HISTOGRAM_SIZE,
        		sizeDataList, startTime, endTime);
        reporter.outputReports(
        		templateFilePath,
        		outputFolderPath + File.separator + Constants.ITEMNAME_JAVAPROCESS_COLLECTION_HISTOGRAM_COUNT,
        		countDataList, startTime, endTime);
    }
}

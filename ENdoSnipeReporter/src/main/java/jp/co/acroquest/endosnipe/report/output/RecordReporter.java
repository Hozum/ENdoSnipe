package jp.co.acroquest.endosnipe.report.output;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jp.co.acroquest.endosnipe.report.LogIdConstants;
import jp.co.acroquest.endosnipe.report.ReporterPluginProvider;
import jp.co.acroquest.endosnipe.report.controller.ReportType;
import jp.co.acroquest.endosnipe.report.entity.ItemData;
import jp.co.acroquest.endosnipe.report.entity.ItemRecord;
import jp.co.acroquest.endosnipe.report.util.ReporterConfigAccessor;
import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;
import jp.co.acroquest.endosnipe.common.util.CSVTokenizer;
import jp.co.acroquest.endosnipe.common.util.PathUtil;
import jp.co.acroquest.endosnipe.report.output.RecordReporter;

import org.bbreak.excella.reports.exporter.ExcelExporter;
import org.bbreak.excella.reports.model.ReportBook;
import org.bbreak.excella.reports.model.ReportSheet;
import org.bbreak.excella.reports.processor.ReportProcessor;
import org.bbreak.excella.reports.tag.BlockRowRepeatParamParser;
import org.bbreak.excella.reports.tag.RowRepeatParamParser;
import org.bbreak.excella.reports.tag.SingleParamParser;

/**
 * ���|�[�g�𐶐����邽�߂̃N���X
 * @author kimura
 *
 * @param <E>
 */
public class RecordReporter<E>
{
    /** ���K�[ */
    private static final ENdoSnipeLogger LOGGER = ENdoSnipeLogger.getLogger(
            RecordReporter.class, ReporterPluginProvider.INSTANCE);

    private static final String XLS_EXTENTION = ".xls";

    /** �Q�Ƃ���e���v���[�g�̃V�[�g���̃��X�g */
    private String[]           templateSheetNames_;

    /** �u���p�����[�^�� */
    private String[]             recordParameters_;

    /** ���Ԃ�\���p�����[�^�� */
    public static final String NUMBERS    = "numbers";

    /** �擾�����͈͂�\������p�����[�^�� */
    public static final String DATA_RANGE = "dataRange";
    
    /** �����n��O���t�̃e���v���[�g�V�[�g�� */
    public static final String TEMPLATE_SHEET_NAME = "�f�[�^";

    /** �����n��O���t�̃p�����[�^�� */
    public static final String PARAMETER_NAME = "repeatValues";

    /** �O���t���̃p�����[�^�� */
    public static final String GRAPH_TITLE = "graphTitle";
    
    /** �o�͂��������n��O���t�̃J�E���^ */
    private int counter_;

    /**
     * �R���X�g���N�^
     * @param type ���|�[�g���
     */
    public RecordReporter(ReportType type)
    {
        String id = type.getId();
        String parameterProperty = ReporterConfigAccessor.getProperty(id + ".recordParameter");
        if (parameterProperty == null)
        {
            parameterProperty = PARAMETER_NAME;
        }
        this.recordParameters_ = parameterProperty.split(" *, *");
        
        // �e���v���[�g�t�@�C���̃V�[�g���̈ꗗ���擾����B
        String templateSheetNames = ReporterConfigAccessor.getProperty(id + ".templateSheetNames");
        CSVTokenizer tokenizer = new CSVTokenizer(templateSheetNames);
        int tokenCount = tokenizer.countTokens();
        this.templateSheetNames_ = new String[tokenCount];
        int index = 0;
        while (tokenizer.hasMoreTokens())
        {
        	this.templateSheetNames_[index] = tokenizer.nextToken();
        	index++;
        }
    }
    
    /**
     * ���|�[�g�̃G�N�Z���t�@�C�����o�͂���
     * 
     * @param templateFilePath �e���v���[�g�t�@�C���̃p�X
     * @param outputFolderPath �o�͂���t�@�C���̃p�X
     * @param outputFileName �o�͂���t�@�C���̖��O
     * @param graphTitles �e�O���t�̃^�C�g��
     * @param records �o�͂���f�[�^�̃��X�g
     * @param startDate �f�[�^�擾�J�n����
     * @param endDate �f�[�^�擾�I������
     */
    public void outputReport(String templateFilePath, String outputFolderPath,
            String outputFileName, String[] graphTitles, E[] records,
            Date startDate, Date endDate)
    {
        // �w�肳�ꂽ�t�H���_���쐬����
        File outputDir = new File(outputFolderPath);

        if (outputDir.exists() == false)
        {
            boolean result = outputDir.mkdirs();
            if (result == false)
            {
                return;
            }
        }
        
        String outputFilePath = createFilePath(outputFolderPath, outputFileName);
        
        // �@�ǂݍ��ރe���v���[�g�t�@�C���̃p�X(�g���q��)
        // �A�o�͐�̃t�@�C���p�X(�g���q��Exporter�ɂ���Ď����I�ɕt�^����邽�߁A�s�v�B)
        // �B�t�@�C���t�H�[�}�b�g(ConvertConfiguration�̔z��)
        // ���w�肵�AReportBook�C���X�^���X�𐶐�����B
        ReportBook outputBook = new ReportBook(templateFilePath,
                outputFilePath, ExcelExporter.FORMAT_TYPE);
        
        for (int sheetIndex = 0; sheetIndex < this.templateSheetNames_.length; sheetIndex++)
        {
            String templateSheetName = this.templateSheetNames_[sheetIndex];
            
            // �e���v���[�g�t�@�C�����̃V�[�g���Əo�̓V�[�g�����w�肵�A
            // ReportSheet�C���X�^���X�𐶐����āAReportBook�ɒǉ�����B
            ReportSheet outputDataSheet = new ReportSheet(templateSheetName);
            outputBook.addReportSheet(outputDataSheet);

            // �u���p�����[�^��ReportSheet�I�u�W�F�N�g�ɒǉ�����B
            // (�����u���̃p�����[�^�ɂ͔z���n���B)
            List<Integer> numberList = new ArrayList<Integer>();
            for (int index = 0; index < records.length; index++)
            {
                numberList.add(index + 1);
            }
            outputDataSheet.addParam(BlockRowRepeatParamParser.DEFAULT_TAG,
                    this.recordParameters_[0], records);

            // �\�̈�ԍ��[�̗�ɍ��ڔԍ���ǉ�
            outputDataSheet.addParam(RowRepeatParamParser.DEFAULT_TAG,
                    RecordReporter.NUMBERS, numberList.toArray());

            // ��������(��) ����:���� ���� ��������(��) ����:���� �܂ł̃f�[�^�擾���ʂł�
            // �Ƃ����������\��������
            String dataRange = this.getDataRangeString(startDate, endDate);
            outputDataSheet.addParam(SingleParamParser.DEFAULT_TAG,
                    RecordReporter.DATA_RANGE, dataRange);
            
            // �O���t�̃^�C�g����\��������
            outputDataSheet.addParam(SingleParamParser.DEFAULT_TAG,
                    RecordReporter.GRAPH_TITLE, graphTitles[sheetIndex]);
        }

        // 
        // ReportProcessor�C���X�^���X�𐶐����A
        // ReportBook�����Ƀ��|�[�g���������s���܂��B
        // 
        ReportProcessor reportProcessor = new ReportProcessor();
        try
        {
            reportProcessor.process(outputBook);
        }
        catch (Exception ex)
        {
            LOGGER.log(LogIdConstants.REPORT_PUBLISH_STOPPED_WARN, ex, outputFilePath);
        }
    }

    /**
     * ���|�[�g�̃G�N�Z���t�@�C�����o�͂���
     * 
     * @param templateFilePath
     *            �e���v���[�g�t�@�C���̃p�X
     * @param outputFilePath
     *            �o�͂���t�@�C���̃p�X
     * @param records
     *            �o�͂���f�[�^�̃��X�g
     * @param startDate
     *            �f�[�^�擾�J�n����
     * @param endDate
     *            �f�[�^�擾�I������
     */
    public void outputReport(String templateFilePath, String outputFilePath, E[] records,
            Date startDate, Date endDate)
    {
        // �@�ǂݍ��ރe���v���[�g�t�@�C���̃p�X(�g���q��)
		// �A�o�͐�̃t�@�C���p�X(�g���q��Exporter�ɂ���Ď����I�ɕt�^����邽�߁A�s�v�B)
		// �B�t�@�C���t�H�[�}�b�g(ConvertConfiguration�̔z��)
		// ���w�肵�AReportBook�C���X�^���X�𐶐�����B
		ReportBook outputBook = new ReportBook(templateFilePath,
				outputFilePath, ExcelExporter.FORMAT_TYPE);
		
		for (String templateSheetName : this.templateSheetNames_)
		{
			
			// �e���v���[�g�t�@�C�����̃V�[�g���Əo�̓V�[�g�����w�肵�A
			// ReportSheet�C���X�^���X�𐶐����āAReportBook�ɒǉ�����B
			ReportSheet outputDataSheet = new ReportSheet(templateSheetName);
			outputBook.addReportSheet(outputDataSheet);

			// �u���p�����[�^��ReportSheet�I�u�W�F�N�g�ɒǉ�����B
			// (�����u���̃p�����[�^�ɂ͔z���n���B)
			List<Integer> numberList = new ArrayList<Integer>();
			for (int index = 0; index < records.length; index++)
			{
				numberList.add(index + 1);
			}
			outputDataSheet.addParam(BlockRowRepeatParamParser.DEFAULT_TAG,
					this.recordParameters_[0], records);

			// �\�̈�ԍ��[�̗�ɍ��ڔԍ���ǉ�
			outputDataSheet.addParam(RowRepeatParamParser.DEFAULT_TAG,
					RecordReporter.NUMBERS, numberList.toArray());

			// ��������(��) ����:���� ���� ��������(��) ����:���� �܂ł̃f�[�^�擾���ʂł�
			// �Ƃ����������\��������
			String dataRange = this.getDataRangeString(startDate, endDate);
			outputDataSheet.addParam(SingleParamParser.DEFAULT_TAG,
					RecordReporter.DATA_RANGE, dataRange);
		}

		// 
		// ReportProcessor�C���X�^���X�𐶐����A
		// ReportBook�����Ƀ��|�[�g���������s���܂��B
		// 
        ReportProcessor reportProcessor = new ReportProcessor();
        try
        {
            reportProcessor.process(outputBook);
        }
        catch (Exception ex)
        {
            LOGGER.log(LogIdConstants.REPORT_PUBLISH_STOPPED_WARN, ex, outputFilePath);
        }
    }

    /**
     * ���|�[�g�̃G�N�Z���t�@�C�����o�͂���
     * 
     * @param templateFilePath
     *            �e���v���[�g�t�@�C���̃p�X
     * @param outputFolderPath
     *            �o�͂���t�H���_�̃p�X
     * @param itemData
     *            �o�͂���f�[�^�̃��X�g
     * @param startDate
     *            �f�[�^�擾�J�n����
     * @param endDate
     *            �f�[�^�擾�I������
     */
    public void outputReport(String templateFilePath, String outputFolderPath, ItemData itemData,
            Date startDate, Date endDate)
    {
        List<ItemData> itemDataList = new ArrayList<ItemData>();
        itemDataList.add(itemData);
        this.outputReport(templateFilePath, outputFolderPath, itemDataList, startDate, endDate);
    }
    
    /**
     * ���|�[�g�̃G�N�Z���t�@�C�����o�͂���
     * 
     * @param templateFilePath
     *            �e���v���[�g�t�@�C���̃p�X
     * @param outputFolderPath
     *            �o�͂���t�H���_�̃p�X
     * @param itemDataList
     *            �o�͂���f�[�^�̃��X�g
     * @param startDate
     *            �f�[�^�擾�J�n����
     * @param endDate
     *            �f�[�^�擾�I������
     */
    public void outputReport(String templateFilePath, String outputFolderPath,
            List<ItemData> itemDataList, Date startDate, Date endDate)
    {
        // �w�肳�ꂽ�t�H���_���쐬����
        File outputDir = new File(outputFolderPath);

        if (outputDir.exists() == false)
        {
            boolean result = outputDir.mkdirs();
            if (result == false)
            {
                return;
            }
        }
        
        if (itemDataList == null || itemDataList.size() == 0)
        {
            return;
        }
        
    	String itemName = itemDataList.get(0).getItemName();
        String outputFilePath = createFilePath(outputFolderPath, itemName);

        
        // �@�ǂݍ��ރe���v���[�g�t�@�C���̃p�X(�g���q��)
        // �A�o�͐�̃t�@�C���p�X(�g���q��Exporter�ɂ���Ď����I�ɕt�^����邽�߁A�s�v�B)
        // �B�t�@�C���t�H�[�}�b�g(ConvertConfiguration�̔z��)
        // ���w�肵�AReportBook�C���X�^���X�𐶐�����B
        ReportBook outputBook =
            new ReportBook(templateFilePath, outputFilePath,
                           ExcelExporter.FORMAT_TYPE);

        for (String templateSheetName : this.templateSheetNames_)
		{
			// �e���v���[�g�t�@�C�����̃V�[�g���Əo�̓V�[�g�����w�肵�A
			// ReportSheet�C���X�^���X�𐶐����āAReportBook�ɒǉ�����B
			ReportSheet outputDataSheet = new ReportSheet(templateSheetName);
			outputBook.addReportSheet(outputDataSheet);

            for (int itemIndex = 0; itemIndex < itemDataList.size(); itemIndex++)
            {
                ItemData itemData = itemDataList.get(itemIndex);
                List<ItemRecord> recordList = itemData.getRecords();
				ItemRecord[] records = (ItemRecord[]) recordList
						.toArray(new ItemRecord[recordList.size()]);

				// �u���p�����[�^��ReportSheet�I�u�W�F�N�g�ɒǉ�����B
				// (�����u���̃p�����[�^�ɂ͔z���n���B)
                List<Integer> numberList = new ArrayList<Integer>();
                for (int index = 0; index < records.length; index++)
                {
                    numberList.add(index + 1);
                }
                String parameterName = RecordReporter.PARAMETER_NAME;
                if (this.recordParameters_.length > itemIndex)
                {
                    parameterName = this.recordParameters_[itemIndex];
                }
				outputDataSheet.addParam(BlockRowRepeatParamParser.DEFAULT_TAG,
						parameterName, records);

				// �\�̈�ԍ��[�̗�ɍ��ڔԍ���ǉ�
				outputDataSheet.addParam(RowRepeatParamParser.DEFAULT_TAG,
						RecordReporter.NUMBERS, numberList.toArray());

				// ��������(��) ����:���� ���� ��������(��) ����:���� �܂ł̃f�[�^�擾���ʂł�
				// �Ƃ����������\��������
				String dataRange = this.getDataRangeString(startDate, endDate);
				outputDataSheet.addParam(SingleParamParser.DEFAULT_TAG,
						RecordReporter.DATA_RANGE, dataRange);

				// �O���t�̃^�C�g����\��
				outputDataSheet.addParam(SingleParamParser.DEFAULT_TAG,
						RecordReporter.GRAPH_TITLE, itemName);
			}
		}

        // 
        // ReportProcessor�C���X�^���X�𐶐����A
        // ReportBook�����Ƀ��|�[�g���������s���܂��B
        // 
        ReportProcessor reportProcessor = new ReportProcessor();
        try
        {
            reportProcessor.process(outputBook);
        }
        catch (Exception ex)
        {
            LOGGER.log(LogIdConstants.REPORT_PUBLISH_STOPPED_WARN, ex, outputFilePath);
        }
    }

    /**
     * ���|�[�g�o�̓t�@�C�����𐶐�����B
     * 
     * @param outputFolderPath�@�o�͐�f�B���N�g��
     * @param itemName itemName
     * @return ���|�[�g�o�̓t�@�C�����B
     */
    private String createFilePath(String outputFolderPath, String itemName)
    {
        String outputFileName = PathUtil.getValidFileName(itemName);
        DecimalFormat format = new DecimalFormat("00000");
        String addtion = format.format(this.counter_);
        this.counter_++;
        String outputFilePath =
                PathUtil.getValidLengthPath(outputFolderPath + File.separator + outputFileName
                        + XLS_EXTENTION, addtion);

        outputFilePath =
                outputFilePath.substring(0, outputFilePath.length() - XLS_EXTENTION.length());
        return outputFilePath;
    }

    /**
     * ���|�[�g�̃G�N�Z���t�@�C�����o�͂���
     * 
     * @param templateFilePath
     *            �e���v���[�g�t�@�C���̃p�X
     * @param outputFolderPath
     *            �o�͂���t�H���_�̃p�X
     * @param dataList
     *            �o�͂���f�[�^�̃��X�g
     * @param startDate
     *            �f�[�^�擾�J�n����
     * @param endDate
     *            �f�[�^�擾�I������
     */
    public void outputReports(String templateFilePath, String outputFolderPath,
            List<ItemData> dataList, Date startDate, Date endDate)
    {
        // �w�肳�ꂽ�t�H���_���쐬����
        File outputDir = new File(outputFolderPath);

        if (outputDir.exists() == false)
        {
            boolean result = outputDir.mkdirs();
            if (result == false)
            {
                return;
            }
        }

        for (ItemData itemData : dataList)
        {
            outputReport(templateFilePath, outputFolderPath, itemData, startDate, endDate);
        }
    }

    /**
     * �f�[�^�擾�����͈̔͂�\�����镶����𐬌^����
     * @param startDate �f�[�^�擾�J�n����
     * @param endDate �f�[�^�擾�I������
     * @return�@�\���p�̕�����
     */
    private String getDataRangeString(Date startDate, Date endDate)
    {
        Calendar calendar = Calendar.getInstance();

        //�f�[�^�擾�J�n�����ƃf�[�^�擾�I�������𐬌^����
        calendar.setTime(startDate);
        String startDateString = String.format("%1$tY/%1$tm/%1$td(%1$ta) %1$tH:%1$tM", calendar);
        calendar.setTime(endDate);
        String endDateString = String.format("%1$tY/%1$tm/%1$td(%1$ta) %1$tH:%1$tM", calendar);

        //�\���p������𐬌^����
        StringBuilder builder = new StringBuilder();
        builder.append(startDateString);
        builder.append(" ���� ");
        builder.append(endDateString);
        builder.append(" �܂ł̃f�[�^�擾���ʂł�");

        String returnValue = builder.toString();

        return returnValue;
    }

}

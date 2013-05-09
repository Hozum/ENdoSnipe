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

import jp.co.acroquest.endosnipe.report.controller.processor.ReportPublishProcessorBase;
import jp.co.acroquest.endosnipe.report.util.ReporterConfigAccessor;
import jp.co.acroquest.endosnipe.report.controller.ReportType;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * �i���󋵁i�v���O���X�o�[�j�̃R���g���[�����s���B
 * 
 * @author M.Yoshida
 */
public class ProgressController
{
    /** �v���O���X�o�[�ɕ\������^�C�g�� */
    private static final String REPORT_TASK_TITLE       = "reporter.report.progress.title";

    /** �v���O���X�o�[�ɕ\�����郁�b�Z�[�W�e���v���[�g�̃L�[ */
    private static final String REPORT_SUBTASK_TEMPLATE = "reporter.report.progress.detail";

    /** �v���O���X�o�[�ɏ󋵂�ʒm���邽�߂̃��j�^ */
    private IProgressMonitor    progressMonitor_;

    /** �S�̂Ŏ��s���郌�|�[�g�v���Z�b�T�̐� */
    private int                 runnableProcessorNum_;
    
    /** ���ݓ��삵�Ă���H���̃C���f�b�N�X*/
    private int                 nowPhase_;

    /** ���ݓ��삵�Ă���v���Z�b�T���������郌�|�[�g��� */
    private ReportType          nowReportType_;

    /**
     * �R���X�g���N�^
     * @param monitor    �v���O���X���j�^�[
     * @param processNum �������郌�|�[�g�v���Z�b�T�̐�
     */
    public ProgressController(IProgressMonitor monitor, int processNum)
    {
        progressMonitor_ = monitor;
        runnableProcessorNum_ = processNum;
        nowPhase_ = 0;
    }

    /**
     * �������J�n����B
     */
    public void beginTask()
    {
        String taskName = ReporterConfigAccessor.getProperty(REPORT_TASK_TITLE);

        // ���^�X�N���́A�i�o�͂��郌�|�[�g�̐��j�~�i���|�[�g�v���Z�b�T�̍H���̐��j�ł���B
        // �������A�i������100%�ɂȂ�Ȃ��悤�A1�����ݒ肵�Ă����B
        int totalWork = runnableProcessorNum_ * ReportPublishProcessorBase.PROCESS_PHASE_NUM;
        progressMonitor_.beginTask(taskName, totalWork + 1);
        nowPhase_ = ReportPublishProcessorBase.PROCESS_PHASE_NUM;
    }

    /**
     * ���̃��|�[�g�v���Z�b�T���J�n����B
     * 
     * @param type     ���|�[�g�v���Z�b�T���������郌�|�[�g�̎��
     */
    public void startProcessor(ReportType type)
    {
        nowReportType_ = type;

        String reportName = ReporterConfigAccessor.getReportName(nowReportType_);
        String subTaskName =
                             ReporterConfigAccessor.getPropertyWithParam(REPORT_SUBTASK_TEMPLATE,
                                                                         reportName, "");
        
        // �O�̃��|�[�g�v���Z�b�T��nextPhase���\�b�h���H������Ă΂�Ă��Ȃ��ꍇ�A���̕������i����␳����B
        progressMonitor_.subTask(subTaskName);
        if (nowPhase_ < ReportPublishProcessorBase.PROCESS_PHASE_NUM)
        {
        	progressMonitor_.worked(ReportPublishProcessorBase.PROCESS_PHASE_NUM - nowPhase_);
        }
        
        nowPhase_ = 0;
    }

    /**
     * ���H���ɐi��
     * @param detailKey �H���̓��e�������v���p�e�B�̃L�[
     */
    public void nextPhase(String detailKey)
    {
        String reportName = ReporterConfigAccessor.getReportName(nowReportType_);
        String detailStatus = ReporterConfigAccessor.getProperty(detailKey);
        String subTaskName = ReporterConfigAccessor.getPropertyWithParam(REPORT_SUBTASK_TEMPLATE,
                                                                         reportName, detailStatus);

        // ���^�X�N���ɑ΂���i����1���₷�B
        progressMonitor_.worked(1);
        nowPhase_++;
        progressMonitor_.subTask(subTaskName);
    }

    /**
     * �������I������B
     */
    public void endTask()
    {
    	// ���̐i���̑����ŁA�i������100%�ɂȂ�B
    	progressMonitor_.worked(1);
        progressMonitor_.done();
    }

    /**
     * �L�����Z�����s���Ă��邩�`�F�b�N����B
     * 
     * @return �L�����Z������Ă����true�B�����łȂ����false�B
     */
    public boolean isCanceled()
    {
        return progressMonitor_.isCanceled();
    }
}

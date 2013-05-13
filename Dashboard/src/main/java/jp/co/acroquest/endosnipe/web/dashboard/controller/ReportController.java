/*
 * Copyright (c) 2004-2013 Acroquest Technology Co., Ltd. All Rights Reserved.
 * Please read the associated COPYRIGHTS file for more details.
 *
 * THE  SOFTWARE IS  PROVIDED BY  Acroquest Technology Co., Ltd., WITHOUT  WARRANTY  OF
 * ANY KIND,  EXPRESS  OR IMPLIED,  INCLUDING BUT  NOT LIMITED  TO THE
 * WARRANTIES OF  MERCHANTABILITY,  FITNESS FOR A  PARTICULAR  PURPOSE
 * AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDER BE LIABLE FOR ANY
 * CLAIM, DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package jp.co.acroquest.endosnipe.web.dashboard.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import jp.co.acroquest.endosnipe.web.dashboard.dto.ReportDefinitionDto;
import jp.co.acroquest.endosnipe.web.dashboard.entity.ReportDefinition;
import jp.co.acroquest.endosnipe.web.dashboard.service.ReportService;
import net.arnx.jsonic.JSON;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * レポート出力機能のコントローラクラス。
 * 
 * @author miyasaka
 *
 */
@Controller
@RequestMapping("/report")
public class ReportController
{
    /** シグナル定義のサービスクラスのオブジェクト。 */
    @Autowired
    protected ReportService reportService;

    /**
     * デフォルトコンストラクタ。
     */
    public ReportController()
    {

    }

    /**
     * レポート出力の定義をすべて取得する。
     * 
     * @return 全てのレポート出力の定義
     */
    @RequestMapping(value = "/getAllDefinition", method = RequestMethod.POST)
    @ResponseBody
    public List<ReportDefinitionDto> getAllDefinition()
    {
        List<ReportDefinitionDto> reportDefinitionDtos = new ArrayList<ReportDefinitionDto>();

        reportDefinitionDtos = this.reportService.getAllReport();

        return reportDefinitionDtos;
    }

    /**
     * 指定したレポート対象名をキーに、レポート定義の一覧を取得する。
     * @param reportName 閾値判定の定義を一意に取得するためのシグナル名
     * @return 閾値判定の定義
     */
    @RequestMapping(value = "/getDefinitionByReportName", method = RequestMethod.POST)
    @ResponseBody
    public List<ReportDefinitionDto> getByTarget(
            @RequestParam(value = "reportName") final String reportName)
    {
        List<ReportDefinitionDto> reportDefinitionDtos =
                this.reportService.getReportByReportName(reportName);
        return reportDefinitionDtos;
    }

    /**
     * レポート出力の定義を新規に追加する。
     * 
     * @param reportDefinition
     *            レポート出力定義のJSONデータ
     * @return 追加したレポート出力の定義
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public ReportDefinitionDto addReportDefinition(
            @RequestParam(value = "reportDefinition") final String reportDefinition)
    {
        ReportDefinitionDto reportDefinitionDto =
                JSON.decode(reportDefinition, ReportDefinitionDto.class);

        ReportDefinition definition =
                this.reportService.convertReportDefinition(reportDefinitionDto);

        // レポート定義をDBに登録する
        ReportDefinitionDto addedDefinitionDto =
                this.reportService.insertReportDefinition(definition);

        // レポートを生成する
        this.reportService.createReport(reportDefinitionDto);

        return addedDefinitionDto;
    }

    /**
     * レポートをダウンロードする。
     * 
     * @param res {@link HttpServletResponse}オブジェクト
     * @param reportId レポートID
     */
    @RequestMapping(value = "/download", method = RequestMethod.POST)
    @ResponseBody
    public void downloadReport(final HttpServletResponse res,
            @RequestParam(value = "reportId") final String reportId)
    {
        String fileName = "20130512_102400-20130513_102400.zip";
        this.reportService.doRequest(res, fileName);
    }
}
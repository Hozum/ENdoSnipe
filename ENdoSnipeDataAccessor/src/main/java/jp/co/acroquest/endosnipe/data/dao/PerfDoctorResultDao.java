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
package jp.co.acroquest.endosnipe.data.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import jp.co.acroquest.endosnipe.common.util.SQLUtil;
import jp.co.acroquest.endosnipe.data.dto.PerfDoctorResultDto;

/**
 * PerformanceDoctor�f�f���ʒ~�σe�[�u����DAO
 * 
 * @author hiramatsu
 *
 */
public class PerfDoctorResultDao extends AbstractDao
{

    /** �e�[�u���� */
    private static final String PERFDOCTOR_RESULT_TABLE = "PERFDOCTOR_RESULT";

    /**
     * ���R�[�h��}�����܂��B<br />
     *
     * @param database �f�[�^�x�[�X��
     * @param pDResult �}������l
     * @throws SQLException SQL ���s���ɗ�O�����������ꍇ
     */
    public static void insert(final String database, PerfDoctorResultDto pDResult)
        throws SQLException
    {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try
        {
            conn = getConnection(database);
            String sql =
                "insert into " + PERFDOCTOR_RESULT_TABLE + "(OCCURRENCE_TIME, DESCRIPTION, LEVEL, "
                    + "CLASS_NAME, METHOD_NAME, JAVELIN_LOG_NAME, MEASUREMENT_ITEM_NAME)"
                    + " values (?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            // CHECKSTYLE:OFF
            pstmt.setTimestamp(1, pDResult.getOccurrenceTime());
            pstmt.setString(2, pDResult.getDescription());
            pstmt.setString(3, pDResult.getLevel());
            pstmt.setString(4, pDResult.getClassName());
            pstmt.setString(5, pDResult.getMethodName());
            pstmt.setString(6, pDResult.getLogFileName());
            pstmt.setString(7, pDResult.getMeasurementItemName());
            // CHECKSTYLE:ON
            pstmt.execute();
        }
        finally
        {
            SQLUtil.closeStatement(pstmt);
            SQLUtil.closeConnection(conn);
        }
    }

    /**
     * ���Ԃƍ��ږ����w�肵�ăf�[�^���擾����B
     * 
     * @param dbName �f�[�^�x�[�X��
     * @param start �J�n����
     * @param end �I������
     * @param dataGroupId ID
     * @return �f�f����DTO�̃��X�g
     * @throws SQLException select���s��
     */
    public static List<PerfDoctorResultDto> selectByTermAndName(String dbName, Timestamp start,
        Timestamp end, String dataGroupId)
        throws SQLException
    {
        List<PerfDoctorResultDto> result = new ArrayList<PerfDoctorResultDto>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try
        {

            conn = getConnection(dbName, true);
            String sql =
                createSelectSqlByTermAndName(PERFDOCTOR_RESULT_TABLE, start, end, dataGroupId);
            pstmt = conn.prepareStatement(sql);
            PreparedStatement delegated = getDelegatingStatement(pstmt);
            setTimestampByTerm(delegated, start, end);
            rs = delegated.executeQuery();

            // ���ʂ����X�g�ɂP���i�[����
            while (rs.next() == true)
            {
                PerfDoctorResultDto dto = new PerfDoctorResultDto();
                //                setJavelinLogFromResultSet(dto, rs, false);
                setPerfDoctorResultFromResultSet(dto, rs, false);
                result.add(dto);
            }
        }
        finally
        {
            SQLUtil.closeResultSet(rs);
            SQLUtil.closeStatement(pstmt);
            SQLUtil.closeConnection(conn);
        }

        return result;
    }

    private static void setPerfDoctorResultFromResultSet(final PerfDoctorResultDto dto,
        final ResultSet rs, final boolean outputLog)
        throws SQLException
    { // CHECKSTYLE:OFF
        dto.setLogId(rs.getLong(1));
        dto.setOccurrenceTime(rs.getTimestamp(2));
        dto.setDescription(rs.getString(3));
        dto.setLevel(rs.getString(4));
        dto.setMethodName(rs.getString(5));
        dto.setClassName(rs.getString(6));
        dto.setLogFileName(rs.getString(7));
        dto.setMeasurementItemName(rs.getString(8));
        // CHECKSTYLE:ON
    }

    private static void setTimestampByTerm(final PreparedStatement delegated,
        final Timestamp start, final Timestamp end)
        throws SQLException
    {
        if (start != null && end != null)
        {
            delegated.setTimestamp(1, start);
            delegated.setTimestamp(2, end);
        }
        else if (start != null && end == null)
        {
            delegated.setTimestamp(1, start);
        }
        else if (start == null && end != null)
        {
            delegated.setTimestamp(1, end);
        }
    }

    private static String createSelectSqlByTermAndName(final String tableName,
        final Timestamp start, final Timestamp end, final String name)
    {
        String sql = "select * from " + tableName;
        if (start != null && end != null)
        {
            sql += " where ? <= OCCURRENCE_TIME and OCCURRENCE_TIME <= ?";
        }
        else if (start != null && end == null)
        {
            sql += " where ? <= OCCURRENCE_TIME";
        }
        else if (start == null && end != null)
        {
            sql += " where OCCURRENCE_TIME <= ?";
        }
        if (name != null)
        {
            sql +=
                ((start == null && end == null) ? " where " : " and ")
                    + "MEASUREMENT_ITEM_NAME like '" + name + "%'";
        }
        sql += " order by OCCURRENCE_TIME desc";
        return sql;
    }
}

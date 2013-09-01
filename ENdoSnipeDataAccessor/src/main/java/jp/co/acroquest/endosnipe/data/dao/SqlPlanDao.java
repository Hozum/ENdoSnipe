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

import jp.co.acroquest.endosnipe.common.util.SQLUtil;
import jp.co.acroquest.endosnipe.data.TableNames;
import jp.co.acroquest.endosnipe.data.entity.SqlPlan;

/**
 * {@link SqlPlan} �̂��߂� DAO �ł��B
 * 
 * @author miyasaka
 *
 */
public class SqlPlanDao extends AbstractDao implements TableNames
{
    /**
     * SQL���s�v��̃��R�[�h��ǉ����܂��B<br />
     * 
     * @param database �f�[�^�x�[�X��
     * @param sqlPlan �}������SQL���s�v��
     * @throws SQLException SQL ���s���ɗ�O�����������ꍇ
     */
    public static void insert(final String database, final SqlPlan sqlPlan)
        throws SQLException
    {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try
        {
            conn = getConnection(database);
            pstmt =
                conn.prepareStatement("insert into " + SQL_PLAN
                    + " (MEASUREMENT_ITEM_NAME, SQL_STATEMENT, EXECUTION_PLAN, GETTING_PLAN_TIME)"
                    + " values (?,?,?,?)");
            PreparedStatement delegated = getDelegatingStatement(pstmt);
            // CHECKSTYLE:OFF
            String measurementItemName = sqlPlan.measurementItemName;
            String sqlStatement = sqlPlan.sqlStatement;
            String executionPlan = sqlPlan.executionPlan;
            Timestamp gettingPlanTime = sqlPlan.gettingPlanTime;

            delegated.setString(1, measurementItemName);
            delegated.setString(2, sqlStatement);
            delegated.setString(3, executionPlan);
            delegated.setTimestamp(4, gettingPlanTime);
            // CHECKSTYLE:ON
            delegated.execute();
        }
        finally
        {
            SQLUtil.closeResultSet(rs);
            SQLUtil.closeStatement(pstmt);
            SQLUtil.closeConnection(conn);
        }
    }
}

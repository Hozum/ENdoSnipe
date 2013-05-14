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
package jp.co.acroquest.endosnipe.web.dashboard.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.acroquest.endosnipe.common.logger.ENdoSnipeLogger;
import jp.co.acroquest.endosnipe.data.entity.JavelinLog;
import jp.co.acroquest.endosnipe.web.dashboard.constants.EventConstants;
import jp.co.acroquest.endosnipe.web.dashboard.constants.LogMessageCodes;
import jp.co.acroquest.endosnipe.web.dashboard.util.DaoUtil;

/**
 * jvn�t�@�C���o�͗p�̃T�[�u���b�g�ł��B
 * @author tsukano
 *
 */
public class JvnDownloadServlet extends HttpServlet
{

    /** ���K�[ */
    private static final ENdoSnipeLogger LOGGER           =
                                                            ENdoSnipeLogger.getLogger(JvnDownloadServlet.class);

    /** �o�b�t�@�̃T�C�Y */
    private static final int             BUFFER_SIZE      = 1024;

    /** �V���A��ID */
    private static final long            serialVersionUID = 2070325848334763894L;

    /**
     * {@inheritDoc}
     */
    public void init()
        throws ServletException
    {
        // Do Nothing.
    }

    /**
     * {@inheritDoc}
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        doRequest(request, response);
    }

    /**
     * {@inheritDoc}
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        doRequest(request, response);
    }

    /**
     * �N���C�A���g�����jvn�t�@�C���o�̓_�E�����[�h����M���邽�߂̃T�[�u���b�g�ł��B
     * @param request {@link HttpServletRequest}�I�u�W�F�N�g
     * @param response {@link HttpServletResponse}�I�u�W�F�N�g
     */
    public void doRequest(HttpServletRequest request, HttpServletResponse response)
    {
        // �p�����[�^�擾
        String agentId = request.getParameter(EventConstants.AGENT_ID);
        String logFileName = request.getParameter(EventConstants.LOG_FILE_NAME);

        try
        {
            // Javalin���O���擾����
            JavelinLog jvnLog = DaoUtil.getJavelinLog(agentId, logFileName);
            if (jvnLog == null)
            {
                LOGGER.log(LogMessageCodes.FAIL_GET_JVNLOG);
                return;
            }
            // Javalin���O���N���C�A���g�ɕԂ�
            printOutFile(request, response, logFileName, jvnLog.javelinLog);
        }
        catch (IOException ex)
        {
            // Do Nothing.
        }
    }

    private void printOutFile(HttpServletRequest req, HttpServletResponse res, String logFileName,
            InputStream is)
        throws IOException
    {
        OutputStream os = res.getOutputStream();
        try
        {
            //���X�|���X�ݒ�  
            res.setContentType("application/octet-stream");
            res.setHeader("Content-Disposition", "filename=\"" + logFileName + "\"");

            int len = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((len = is.read(buffer)) >= 0)
            {
                os.write(buffer, 0, len);
            }

            is.close();
        }
        catch (IOException ex)
        {
            printOutNotFound(res);
        }
        finally
        {

            if (os != null)
            {
                try
                {
                    os.close();
                    is.close();
                }
                catch (IOException ex)
                {
                    LOGGER.log(LogMessageCodes.IO_ERROR);

                }
                finally
                {
                    os = null;
                    is = null;
                }
            }
        }
    }

    /**
     * �t�@�C����������Ȃ��ꍇ
     * @param res {@link HttpServletResponse}
     */
    private void printOutNotFound(HttpServletResponse res)
    {
        OutputStream toClient = null;
        try
        {
            toClient = res.getOutputStream();
            res.setContentType("text/html;charset=Shift_JIS");
            toClient.write("File not found".getBytes());
            toClient.close();
        }
        catch (IOException ex)
        {
            LOGGER.log(LogMessageCodes.IO_ERROR);
        }
        finally
        {
            if (toClient != null)
            {
                try
                {
                    toClient.close();
                }
                catch (IOException ex)
                {
                    LOGGER.log(LogMessageCodes.IO_ERROR);
                }
                finally
                {
                    toClient = null;
                }
            }
        }
    }
}
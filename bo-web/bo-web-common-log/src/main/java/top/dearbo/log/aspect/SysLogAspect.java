/*
 *
 *  *  Copyright (c) 2019-2020, 冷冷 (wangiegie@gmail.com).
 *  *  <p>
 *  *  Licensed under the GNU Lesser General Public License 3.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *  <p>
 *  * https://www.gnu.org/licenses/lgpl.html
 *  *  <p>
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package top.dearbo.log.aspect;


import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.servlet.ServletUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.dearbo.log.annotation.SysLog;
import top.dearbo.log.config.DefaultSysLogConfig;
import top.dearbo.log.config.SysLogConfig;
import top.dearbo.log.entity.SysLogEntity;
import top.dearbo.log.event.SysLogEvent;
import top.dearbo.web.core.util.ServletUtils;
import top.dearbo.web.core.util.SpringContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Objects;

/**
 * 操作日志使用spring event异步入库
 *
 * @author Bo
 */
@Aspect
public class SysLogAspect {

    private static final Logger logger = LoggerFactory.getLogger(SysLogAspect.class);

    private volatile SysLogConfig sysLogConfig;

    @Around("@annotation(sysLog)")
    public Object around(ProceedingJoinPoint point, SysLog sysLog) throws Throwable {
        checkSysLogConfig();
        String strClassName = point.getTarget().getClass().getName();
        String strMethodName = point.getSignature().getName();
        logger.debug("[类名]:{},[方法]:{}", strClassName, strMethodName);
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        if (sysLogConfig != null && sysLogConfig.checkLog(request)) {
            SysLogEntity logVo = getSysLog(request, sysLog, sysLogConfig);
            // 发送异步日志事件
            long startTime = System.currentTimeMillis();
            logVo.setRequestDate(new Date(startTime));
            Object obj = null;
            Throwable currentThrowable = null;
            try {
                obj = point.proceed();
            } catch (Throwable throwable) {
                currentThrowable = throwable;
                String localizedMessage = throwable.getLocalizedMessage();
                logVo.setException(localizedMessage == null ? ExceptionUtils.getMessage(throwable) : localizedMessage);
            }
            logVo.setTime(System.currentTimeMillis() - startTime);
            SpringContextHolder.publishEvent(new SysLogEvent(logVo));
            if (currentThrowable != null) {
                throw currentThrowable;
            }
            return obj;
        }
        return point.proceed();
    }

    public SysLogEntity getSysLog(HttpServletRequest request, SysLog sysLog, SysLogConfig sysLogConfig) {
        SysLogEntity sysLogEntity = new SysLogEntity();
        sysLogEntity.setOperator(sysLogConfig.getUserName(request));
        sysLogEntity.setOperatorId(sysLogConfig.getUserId(request));
        sysLogEntity.setType(StringUtils.isBlank(sysLog.type()) ? sysLogConfig.getType(request) : sysLog.type());
        sysLogEntity.setApplicationName(StringUtils.isBlank(sysLog.applicationName()) ? getApplicationName() : sysLog.applicationName());
        sysLogEntity.setSubject(StringUtils.isBlank(sysLog.subject()) ? sysLogConfig.getSubject(request) : sysLog.subject());
        sysLogEntity.setRemark(sysLog.value());
        sysLogEntity.setIp(clientIp(request));
        sysLogEntity.setRequestUri(URLUtil.getPath(request.getRequestURI()));
        sysLogEntity.setMethod(request.getMethod());
        sysLogEntity.setUserAgent(request.getHeader("user-agent"));
        sysLogEntity.setParams(ServletUtils.toRequestParams(request, sysLogConfig.excludeParamKey()));
        return sysLogEntity;
    }

    private String getApplicationName() {
        return SpringContextHolder.getApplicationContext().getEnvironment().getProperty("spring.application.name");
    }

    private void checkSysLogConfig() {
        if (sysLogConfig == null) {
            try {
                sysLogConfig = SpringContextHolder.getBean(SysLogConfig.class);
            } catch (Throwable e) {
                sysLogConfig = new DefaultSysLogConfig();
            }
        }
    }

    private String clientIp(HttpServletRequest request) {
        String clientIp = ServletUtil.getClientIP(request);
        if ("127.0.0.1".equals(clientIp) || "0:0:0:0:0:0:0:1".equals(clientIp)) {
            try {
                //根据网卡取本机配置的IP
                InetAddress inetAddress = InetAddress.getLocalHost();
                return inetAddress.getHostAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return clientIp;
    }

}
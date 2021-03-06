package top.dearbo.frame.common.test;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import top.dearbo.frame.common.util.excel.ExcelWriteUtils;
import top.dearbo.util.data.JsonUtil;
import top.dearbo.util.lang.DateUtil;
import top.dearbo.util.network.HttpUtils;

import java.io.IOException;
import java.util.*;

/**
 * @version 1.0
 * @author: Bo
 * @fileName: Test
 * @createDate: 2019-11-01 10:32.
 * @description:
 */
public class ProjectTest {

    @Test
    public void importExcelPackage() throws IOException {
        //导入
        ExcelWriteUtils excelWriteUtils = ExcelWriteUtils.importExcel("P:/实验智能仓储原材料明细表(已编位置码).xlsx", "合計原材注111項");
        //一行数据为null，或者""，不添加
        excelWriteUtils.setEmptyRowFlag(false);
        //显示读取行号
        excelWriteUtils.setDebugNumFlag(true);
        //设置排除行:一.下标以逗号分隔，二：传int集合
        excelWriteUtils.setExcludeLineList("0");
        //excelWriteUtils.setExcludeLineList(Arrays.asList(1, 2));
        List<List<Map<String, String>>> listList = excelWriteUtils.readExcelWithoutTitle(true, 2, false);
        for (List<Map<String, String>> listMap : listList) {
            System.out.println("listMap:" + JsonUtil.toJson(listMap));
            for (Map<String, String> map : listMap) {

                /*for (Map.Entry<String, String> entry : map.entrySet()) {
                    System.out.print(entry.getKey() + "=========" + entry.getValue() + ",");
                }*/
            }
        }
    }

    @Test
    public void testDateOneOrLastDay() {
        Date date = new Date();
        String strDate = "2019-11-15 10:00:10";
        //date = DateUtil.parseDateHMS(strDate);
        //Date dateFirstDay = DateUtil.getDateFirstDay(date);
        Date dateLastDay = DateUtil.getDateLastDay(date);
        //System.out.println("dateFirstDay:" + DateUtil.parseToString(dateFirstDay));
        System.out.println("dateLastDay:" + DateUtil.parseToString(dateLastDay));
        List<Date> dateList = DateUtil.getDatesByDay(DateUtil.getStartOfDay(date), dateLastDay);
        for (Date date1 : dateList) {
            System.out.println("date1:" + DateUtil.parseToString(date1));
        }
    }

    @Test
    public void testTencentCheckIn() {
        String url = "https://vip.video.qq.com/fcgi-bin/comm_cgi?name=hierarchical_task_system&cmd=2&_=1573203663793&callback=Zepto1573203623995";
        String cookie = "tvfe_boss_uuid=02a135417eb2a9e6; video_platform=2; video_guid=10637fbc8386aa4a; pgv_pvid=1445208576; pgv_pvi=7874613248; RK=XpghHgPJNO; ptcz=b87e50cb7a83790b60797f19d2ed9cf346866a775bde7ee7454f7b8d6476ced0; ptui_loginuin=1143311620; pgv_info=ssid=s6592245476; pgv_si=s2532952064; _qpsvr_localtk=0.5369199070964024; ptisp=cm; main_login=qq; vqq_access_token=C16114C5B554FB3944EA0518BD07A44B; vqq_appid=101483052; vqq_openid=E0AC2C8BB1058083DD4CD7972802C74D; vqq_vuserid=169496460; vqq_vusession=mabhOn6SPE3aBAHy-Sycdg..; vqq_refresh_token=8C16CD4FD8DEE125141CF02283448636; login_time_init=2019-11-11 9:22:17; uid=224268488; vqq_next_refresh_time=6598; vqq_login_time_init=1573435338; login_time_last=2019-11-11 9:22:18";
        String cookie1 = "main_login=qq;vqq_vusession=Z2tFZaT32lwPoEnl8oP-Kw..;";
        Map<String, String> headerMap = new LinkedHashMap<>();
        headerMap.put("Cookie", cookie1);
        HttpUtils httpUtils = HttpUtils.createRequest();
        HttpUtils.ResultResponse resultResponse = httpUtils.doGet(url, null, headerMap);
        System.out.println("resultResponse:" + resultResponse.toString());

    }

    @Test
    public void testTencentHZCheckIn() {
        String url = "https://vip.qzone.qq.com/fcg-bin/v2/fcg_mobile_vip_site_checkin?t=0.46869834180487055&g_tk=364202229&qzonetoken=(function(){var%20t&uin=202515345&format=json263698854";
        String cookie = "main_login=qq;vqq_vusession=Z2tFZaT32lwPoEnl8oP-Kw..;";
        Map<String, String> headerMap = new LinkedHashMap<>();
        headerMap.put("Cookie", cookie);
        //headerMap.put("User-Agent", "Dalvik/2.1.0 (Linux; U; Android 5.1.1; h60-l01 Build/LYZ28N)");
        HttpUtils httpUtils = HttpUtils.createRequest();
        HttpUtils.ResultResponse resultResponse = httpUtils.doGet(url, null, headerMap);
        System.out.println("resultResponse:" + resultResponse.toString());

    }

    @Test
    public void testDate() {
        String strDate = "2019/10/20";
        Date parseToDate = DateUtil.parseToDate(strDate, "yyyy/MM/dd");
        String parseToString = DateUtil.parseToString(parseToDate);
        System.out.println("parseToString:" + parseToString);
    }

    @Test
    public void getCookieByTicket() {
        String ticket = "AAEAMJuTZLWamQDXR1CMRIrz30-C9fLSksCLUOe0FC9jRbiT0N6oI6AMlWw4nfVH-CoMZQ";
        String url = "https://passport.jd.com/uc/qrCodeTicketValidation?t=" + ticket;
        Map<String, String> headMap = new LinkedHashMap<>();
        headMap.put("Referer", "https://passport.jd.com/new/login.aspx?ReturnUrl=https%3A%2F%2Fwww.jd.com%2F");
        headMap.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36");
        HttpUtils httpUtils = HttpUtils.createRequest();
        HttpUtils.ResultResponse resultResponse = httpUtils.doGet(url, null, headMap);
        Map<String, String> cookies = resultResponse.getCookies();
        String cookiesToString = resultResponse.getCookiesToString();
        System.out.println("resultResponse:" + resultResponse);
    }


    @Test
    public void testHttpParam() {
        Map<String, Object> paramMap = new HashMap<>(4);
        paramMap.put("娃娃", "你好啊 aaa");
        paramMap.put("name", "你好啊 aaa");
        paramMap.put("startProduceDate", "2021-04-07 00:00:00");
        paramMap.put("endProduceDate", "2021-04-13 23:59:59");
        paramMap.put("workOrderNo", "%0901%");
        String url = "http://117.41.37.22:808/javso/plan/listProductPlanToDrawingNoByProduceDate.do";
        HttpUtils.ResultResponse response = HttpUtils.createRequest().doPost(url, paramMap);
        System.out.println(response.getData());
    }

    @Test
    public void testHttpResultCharset() {
        String url = "https://passport.jd.com/user/petName/getUserInfoForMiniJd.action";
        String headerJson = "{\"Content-Type\":\"application/json\",\"Cookie\":\"_pst=%E5%B7%B4%E6%8B%89%E6%8B%89%E5%A4%A7%E9%AD%94%E4%BB%99%E5%85%A8%E8%BA%AB%E5%8F%98;logining=1;_tp=HlpQZFlXZJ6%2BWXEcfuTeeErwo0DMbwd%2FGPbcq%2B7eE0txy4fhYtnLysRQ61XpE5Qh7Vj4B%2BBeFXoUSrBViNMaVxh0TF7snCFqA1NdFcqov1Rr%2B9PBh70UNt4%2B3TYZfmNM;ceshi3.com=103;unick=%E5%B7%B4%E6%8B%89%E6%8B%89%E5%A4%A7%E9%AD%94%E4%BB%99%E5%85%A8%E8%BA%AB%E5%8F%98;pin=%E5%B7%B4%E6%8B%89%E6%8B%89%E5%A4%A7%E9%AD%94%E4%BB%99%E5%85%A8%E8%BA%AB%E5%8F%98;pinId=EOa3aEon90prBgOWLPrO0mFVGk0CAeR7MMLt5T2GPTU;thor=9103C8AF25C1218C1A2A1725C7C96FCCEC7119F4F2C819893C8983B95A83A466ECB961A6B29BE44BE2FA1FADE7EA6CEA7BA0DB81CDBC9FB47D2FA6A18A7D0834ED84BEED7C3AAAA84E2C84C0239D40D6F9ED9EE1F35C1A6BC2DFE069C549515BC74F8DD67255348EBCA964A5629299B7EB207AA71504E1F2DA3000895D6484E1675B4488C31FD0A572445ED71CB33168;TrackID=1mfig1Wkon3j_0CjaP0lGZjYB4LBs7uI44xp4-GsnHcxNRO8_cti8IBDajehw22nTvPiXD1gSsRXaA_7f80cjVvh7BM1ncWd19D61dRG2uudJwd8DworclyIEzTcaK2Dc;DeviceSeq=878580db9e3244ffbb12c967b3dcaeb2;\",\"Referer\":\"https://wqs.jd.com/\"}";
        Map<String, String> headerMap = null;
        if (StringUtils.isNotBlank(headerJson)) {
            headerMap = JsonUtil.fromJson(headerJson, LinkedHashMap.class);
        }
        HttpUtils.ResultResponse resultResponse = HttpUtils.createRequest().doPost(url, null, headerMap);
        System.out.println(JSON.parseObject(resultResponse.getData()));
    }

}

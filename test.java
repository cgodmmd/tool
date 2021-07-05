package com.ruoyi.system.controller;

import cn.hutool.core.codec.Base64Encoder;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.system.domain.SysPluginActionDef;
import com.ruoyi.system.domain.SysPluginActionParamDef;
import com.ruoyi.system.domain.SysPluginDef;
import com.ruoyi.system.domain.SysPluginEquip;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class test {
    @Log(title = "图片上传", businessType = BusinessType.UPDATE)
    @PostMapping(value = "/addImg", produces = {"application/json;charset=UTF-8"})
    public AjaxResult multiPicturesUpload(@RequestParam("file") MultipartFile file) throws IOException {
        //RuoYiConfig.getForumPath() 获取自定义该模块的图片保存路径
        if (!file.isEmpty()){
            //获取上传成功的图片保存路径，并返回给前台
            System.out.println("-====================="+file);
            String picture = FileUploadUtils.upload(RuoYiConfig.getUploadPath(),file);
            if (!StringUtils.isEmpty(picture)){
                AjaxResult ajax = AjaxResult.success();
                ajax.put("imgUrl", picture);
                ajax.put("head",RuoYiConfig.getUploadPath());
                return ajax;
            }
        }
        return AjaxResult.error("上传图片异常，请联系管理员");
    }

    @PreAuthorize("@ss.hasPermi('system:def:export')")
    @Log(title = "插件导出", businessType = BusinessType.EXPORT)
    @GetMapping("/exportTest/{id}")
    public void  exportTest(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String,Object> exportmap = new HashMap<String,Object>();
        SysPluginDef list = sysPluginDefService.selectSysPluginDefById(id);
        exportmap.put("id",list.getId());
        exportmap.put("name",list.getName());

        String logo = RuoYiConfig.getUploadPath()+"/"+extractString(list.getLogo());
        System.out.println("图片二进制流"+ImageToBase64(logo));
        exportmap.put("logo",ImageToBase64(logo));
        exportmap.put("description",list.getDescription());
        SysPluginEquip sysPluginEquip = new SysPluginEquip();
        sysPluginEquip.setPluginDefId(list.getId());
        List<SysPluginEquip> test2 =sysPluginEquipService.selectSysPluginEquipList(sysPluginEquip);
//        SysPluginEquip list2 =sysPluginEquipService.selectSysPluginEquipById(list.getId());

        for(int i = 0; i<test2.size() ; i++){
            exportmap.put("eq_id"+i,test2.get(i).getId());
            exportmap.put("eq_pluginDefId"+i,test2.get(i).getPluginDefId());
            exportmap.put("eq_name"+i,test2.get(i).getName());
        }
        exportmap.put("eq_count",test2.size());
        SysPluginActionDef sysPluginActionDef = new SysPluginActionDef();
        sysPluginActionDef.setPluginDefId(list.getId());
        List<SysPluginActionDef> list3 = sysPluginActionDefService.selectSysPluginActionDefList(sysPluginActionDef);
        SysPluginActionParamDef sysPluginActionParamDef = new  SysPluginActionParamDef();
        for(int i = 0; i<list3.size() ; i++){
            exportmap.put("pad_id"+i,list3.get(i).getId());
            exportmap.put("pad_plugin_def_id"+i,list3.get(i).getPluginDefId());
            exportmap.put("pad_name"+i,list3.get(i).getName());
        }
        exportmap.put("pad_count",list3.size());
        sysPluginActionParamDef.setPluginActionId(list3.get(0).getId());
        List<SysPluginActionParamDef> list4 = sysPluginActionParamDefService.selectSysPluginActionParamDefList(sysPluginActionParamDef);
        for(int i=0;i<list4.size();i++){
            exportmap.put("papd_id"+i,list4.get(i).getId());
            exportmap.put("papd_plugin_action_id"+i,list4.get(i).getPluginActionId());
            exportmap.put("papd_name"+i,list4.get(i).getName());
            exportmap.put("papd_unit"+i,list4.get(i).getUnit());
            exportmap.put("papd_min_value"+i,list4.get(i).getMinValue());
            exportmap.put("papd_max_value"+i,list4.get(i).getMaxValue());
        }
        exportmap.put("papd_count",list4.size());
//        System.out.println(exportmap);
        ArrayList<Map<String,Object>> rows = CollUtil.newArrayList(exportmap);
        ExcelWriter writer = ExcelUtil.getWriter();
//        ExcelWriter writer = cn.hutool.poi.excel.ExcelUtil.getWriter("c://插件定义.xlsx");
        // 关闭writer，释放内存
//        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        response.setContentType("application/ms-excel;charset=utf-8");
        String fileName = encodeFileName("test", request);
        response.setHeader("Content-Disposition","attachment;fileName="+fileName);
        ServletOutputStream out=response.getOutputStream();
        writer.write(rows, true);
        writer.flush(out, true);
//        return  writer;
        writer.close();
        IoUtil.close(out);
//        return fileName;
    }

    public static String encodeFileName(String fileNames, HttpServletRequest request) {
        String codedFilename = null;
        try {
            String agent = request.getHeader("USER-AGENT");
            if (null != agent && -1 != agent.indexOf("MSIE") || null != agent && -1 != agent.indexOf("Trident") || null != agent && -1 != agent.indexOf("Edge")) {// ie浏览器及Edge浏览器
                String name = java.net.URLEncoder.encode(fileNames, "UTF-8");
                codedFilename = name;
            } else if (null != agent && -1 != agent.indexOf("Mozilla")) {
                // 火狐,Chrome等浏览器
                codedFilename = new String(fileNames.getBytes("UTF-8"), "iso-8859-1");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return codedFilename;
    }

    private static String NetImageToBase64(String netImagePath) {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        try {
            // 创建URL
            URL url = new URL(netImagePath);
            byte[] by = new byte[1024];
            // 创建链接
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            InputStream is = conn.getInputStream();
            // 将内容读取内存中
            int len = -1;
            while ((len = is.read(by)) != -1) {
                data.write(by, 0, len);
            }

            // 关闭流
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 对字节数组Base64编码
        Base64Encoder encoder = new Base64Encoder();
//                        strNetImageToBase64 = encoder.encode(data.toByteArray());
//        System.out.println("网络图片转换Base64:" + encoder.encode(data.toByteArray()));
        return encoder.encode(data.toByteArray());
    }



    private static String ImageToBase64(String imgPath) {
        byte[] data = null;
        // 读取图片字节数组
        try {
            InputStream in = new FileInputStream(imgPath);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 对字节数组Base64编码
        Base64Encoder encoder = new Base64Encoder();
        // 返回Base64编码过的字节数组字符串
        return encoder.encode(Objects.requireNonNull(data));
    }


    private static String extractString(String s){

        for(int i = 0; i < 4; i++){
            s = s.substring(s.indexOf("/")+1 );
        }
        return s;
    }
}

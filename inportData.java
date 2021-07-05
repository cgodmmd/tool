package com.ruoyi.apiserver.controller;

import cn.hutool.poi.excel.ExcelReader;
import com.ruoyi.apiserver.domain.*;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class inportData {
    @ApiOperation(value="导入",notes ="通过上传文件导入信息到对应数据库的表中")
    @UserToken.UserLoginToken
    @PostMapping(value = "/importData",produces = {"application/json;charset=UTF-8"})
    public Map<String,Object> importData(@RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws Exception{


        ExcelReader reader = cn.hutool.poi.excel.ExcelUtil.getReader(file.getInputStream());
        List<Map<String,Object>> readAll = reader.readAll();

        SysPluginTypeDef sysPluginTypeDef = new SysPluginTypeDef();
        sysPluginTypeDef.setName("PLC-KEP");
        List<SysPluginTypeDef> map =  sysPluginTypeDefService.selectSysPluginTypeDefList(sysPluginTypeDef);

        SysPluginDef sysPluginDef = new SysPluginDef();
        sysPluginDef.setId((String) readAll.get(0).get("id"));
        sysPluginDef.setPluginTypeId(map.get(0).getId());
        sysPluginDef.setSearch_name((String) readAll.get(0).get("name"));
        String path = DownloadConfig.DownLoad;
        String logo =(String) readAll.get(0).get("logo");

        sysPluginDef.setLogo(toimg(logo));
        sysPluginDef.setDescription((String) readAll.get(0).get("description"));
        sysPluginDefService.insertSysPluginDef(sysPluginDef);
        for(int i = 0 ;i<Integer.parseInt(readAll.get(0).get("eq_count").toString());i++){
            SysPluginEquip sysPluginEquip= new SysPluginEquip();
            sysPluginEquip.setId((String) readAll.get(0).get("eq_id"+i));
            sysPluginEquip.setPluginDefId((String) readAll.get(0).get("id"));
            sysPluginEquip.setName((String) readAll.get(0).get("eq_name"+i));
            sysPluginEquipService.insertSysPluginEquip(sysPluginEquip);
        }

        for(int j = 0 ;j<Integer.parseInt(readAll.get(0).get("pad_count").toString());j++){
            SysPluginActionDef sysPluginActionDef= new SysPluginActionDef();
            sysPluginActionDef.setId((String) readAll.get(0).get("pad_id"+j));
            sysPluginActionDef.setPluginDefId((String) readAll.get(0).get("pad_plugin_def_id"+j));
            sysPluginActionDef.setName((String) readAll.get(0).get("pad_name"+j));
            sysPluginActionDefService.insertSysPluginActionDef(sysPluginActionDef);
        }

        for(int i = 0 ;i<Integer.parseInt(readAll.get(0).get("papd_count").toString());i++){
            SysPluginActionParamDef sysPluginActionParamDef= new SysPluginActionParamDef();
            sysPluginActionParamDef.setId((String) readAll.get(0).get("papd_id"+i));
            sysPluginActionParamDef.setPluginActionId((String) readAll.get(0).get("papd_plugin_action_id"+i));
            sysPluginActionParamDef.setName((String) readAll.get(0).get("papd_name"+i));
            sysPluginActionParamDef.setUnit((String) readAll.get(0).get("papd_unit"+i));
            sysPluginActionParamDef.setMinValue((String) readAll.get(0).get("papd_min_value"+i));
            sysPluginActionParamDef.setMaxValue((String) readAll.get(0).get("papd_max_value"+i));
            sysPluginActionParamDefService.insertSysPluginActionParamDef(sysPluginActionParamDef);
        }
        Map<String,Object> map1 = new HashMap<>();
        map1.put("success","true");
        map1.put("code","200");
        map1.put("mes","导入成功");
        map1.put("data",null);
        return map1;
    }

    public  static String  toimg(String base64String) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String dist = DownloadConfig.ImportFile+"/"+uuid+".png";
        FileOutputStream fileOutputStream = new FileOutputStream(dist,true);
        Base64 decoder = new Base64();
        byte[] bytes = decoder.decodeBase64(base64String);
        fileOutputStream.write(bytes);
        fileOutputStream.close();

        return "/images/"+uuid+".png";
    }


}

package com.eservice.iot.web;

import com.eservice.iot.core.Result;
import com.eservice.iot.core.ResultGenerator;
import com.eservice.iot.model.Staff;
import com.eservice.iot.model.Tag;
import com.eservice.iot.service.StaffService;
import com.eservice.iot.service.TagService;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Class Description: xxx
 *
 * @author Wilson Hu
 * @date 2018/08/21.
 */
@RestController
@RequestMapping("/staff")
public class StaffController {
    private final static Logger logger = LoggerFactory.getLogger(StaffController.class);

    @Resource
    private StaffService staffService;
    @Resource
    private TagService tagService;
    @Resource
    private RestTemplate restTemplate;

    private SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");
    @Value("${park_base_url}")
    private String PARK_BASE_URL;

    @GetMapping("/getStaffsByTagName")
    public Result getStaffsByTagName(@RequestParam String tagName){
        ArrayList<Staff> resultList = new ArrayList<>();
        String targetTagId = tagService.getTagIdByName(tagName);
        if(!targetTagId.equals("")) {
            for (int i = 0; i < staffService.getStaffList().size(); i++) {
                if(staffService.getStaffList().get(i).getTag_id_list().contains(targetTagId)) {
                    resultList.add(staffService.getStaffList().get(i));
                }
            }
        }
        return ResultGenerator.genSuccessResult(resultList);
    }

    @GetMapping("/deleteByTagName")
    public Result deleteStaffByTagName(@RequestParam String name) {
        if (name == null || "".equals(name)) {
            return ResultGenerator.genFailResult("标签名字不能为空！");
        } else {
            if (tagService == null) {
                return ResultGenerator.genFailResult("标签服务没有启动！");
            } else {
                ArrayList<Tag> allTagList = tagService.getmAllTagList();
                String targetTagId = null;
                for (Tag item : allTagList) {
                    if (item.getTag_name().equals(name)) {
                        targetTagId = item.getTag_id();
                        break;
                    }
                }
                if (targetTagId == null) {
                    return ResultGenerator.genFailResult("找不到标签名字！");
                } else {
                    ArrayList<Staff> allStaffList = staffService.getStaffList();
                    ArrayList<Staff> allDeleteStaffList = new ArrayList<>();
                    for (Staff item : allStaffList) {
                        if (item.getTag_id_list().contains(targetTagId)) {
                            allDeleteStaffList.add(item);
                        }
                    }
                    int deleteCount = 0;
                    String resultStr = "";
                    ArrayList<String> failedList = new ArrayList<>();
                    resultStr += "需删除staff总数：" + allDeleteStaffList.size();
                    for (int i = 0; i < allDeleteStaffList.size(); i++) {
                        if (staffService.deleteStaff(allDeleteStaffList.get(i).getStaffId())) {
                            deleteCount++;
                        } else {
                            failedList.add(allDeleteStaffList.get(i).getPerson_information().getName());
                        }
                    }
                    resultStr += "; 删除成功staff总数：" + deleteCount;
                    resultStr += "; 删除失败staff数：" + failedList.size();
                    resultStr += "; 失败列表：" + failedList.toString();
                    return ResultGenerator.genSuccessResult(resultStr);
                }
            }
        }
    }

    @GetMapping("/excel")
    public Result excel() {
        if (staffService.getStaffList().size() > 0) {
            HSSFWorkbook workbook = new HSSFWorkbook();
            HSSFSheet sheet = workbook.createSheet("staff");

            ///设置要导出的文件的名字
            String fileName = formatter2.format(new Date()) + ".xls";
            //新增数据行，并且设置单元格数据
            insertDataInSheet(workbook, sheet, staffService.getStaffList());

            try {
                FileOutputStream out = new FileOutputStream("./" + fileName);
                workbook.write(out);
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ResultGenerator.genSuccessResult();
        } else {
            return ResultGenerator.genFailResult("Staff数量为0");
        }
    }

    private void insertDataInSheet(HSSFWorkbook wb, HSSFSheet sheet, List<Staff> list) {
        int rowNum = 1;
        //画图的顶级管理器，一个sheet只能获取一个（一定要注意这点）
        HSSFPatriarch patriarch = sheet.createDrawingPatriarch();
        String[] excelHeaders = {"照片", "姓名", "员工号", "卡号", "标签"};
        //headers表示excel表中第一行的表头
        HSSFRow row3 = sheet.createRow(0);
        //在excel表中添加表头
        for (int i = 0; i < excelHeaders.length; i++) {
            HSSFCell cell = row3.createCell(i);
            HSSFRichTextString text = new HSSFRichTextString(excelHeaders[i]);
            cell.setCellValue(text);
        }
        //在表中存放查询到的数据放入对应的列
        int index = 1;
        for (Staff staff : list) {
            if(staff.getFace_list().size() > 0) {
                HSSFRow row = sheet.createRow(rowNum);
                row.setHeight((short) 1000);
                ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
                String url = PARK_BASE_URL + "/image/" + staff.getFace_list().get(0).getFace_image_id();
                BufferedImage image = null;
                try {
                    image = ImageIO.read(new URL(url));
                    ImageIO.write(image, "jpg", byteArrayOut);
                    //anchor主要用于设置图片的属性
                    HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 0, 0,(short) 0, index, (short) 1, index+1);
                    anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_AND_RESIZE);
                    patriarch.createPicture(anchor, wb.addPicture(byteArrayOut.toByteArray(), HSSFWorkbook.PICTURE_TYPE_JPEG));
                    index++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //row.createCell(0).setCellValue(staff.getFace_list().get(0).getFace_image_id());
                row.createCell(1).setCellValue(staff.getPerson_information().getName());
                row.createCell(2).setCellValue(staff.getPerson_information().getId());
                if(staff.getCard_numbers() != null) {
                    row.createCell(3).setCellValue(listToString(staff.getCard_numbers()));
                } else {
                    row.createCell(3).setCellValue(staff.getPerson_information().getCard_no());
                }
                row.createCell(4).setCellValue(tagService.tagIdToName(staff.getTag_id_list()));
                rowNum++;
            } else {
                logger.warn("Face ID list is zero: {}", staff.getPerson_information().getName());
            }
        }
    }

    private String listToString(List<String> list) {
        String result = "";
        if(list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if(i != list.size() -1) {
                    result += list.get(i) + " | ";
                } else {
                    result += list.get(i);
                }
            }
        }
        return result;
    }

}

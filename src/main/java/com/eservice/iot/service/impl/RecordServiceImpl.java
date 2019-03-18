package com.eservice.iot.service.impl;

import com.eservice.iot.core.AbstractService;
import com.eservice.iot.dao.RecordMapper;
import com.eservice.iot.model.record.Record;
import com.eservice.iot.service.RecordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;


/**
* Class Description: xxx
* @author Wilson Hu
* @date 2019/03/16.
*/
@Service
@Transactional
public class RecordServiceImpl extends AbstractService<Record> implements RecordService {
    @Resource
    private RecordMapper recordMapper;

    public List<Record> getPushedMsg(Integer status, String queryStartTime, String queryEndTime) {
        return recordMapper.getPushedMsg(status, queryStartTime, queryEndTime);
    }

}

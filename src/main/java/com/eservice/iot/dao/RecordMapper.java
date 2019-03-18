package com.eservice.iot.dao;

import com.eservice.iot.core.Mapper;
import com.eservice.iot.model.record.Record;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RecordMapper extends Mapper<Record> {

    List<Record> getPushedMsg(@Param("status")Integer status, @Param("query_start_time")String queryStartTime, @Param("query_end_time")String queryEndTime);
}
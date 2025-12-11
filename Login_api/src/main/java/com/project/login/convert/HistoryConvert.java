package com.project.login.convert;

import com.project.login.model.dataobject.RemarkDO;
import com.project.login.model.vo.HistoryNoteVO;
import com.project.login.model.vo.HistoryRemarkVO;
import com.project.login.model.dataobject.NoteDO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HistoryConvert {
    HistoryNoteVO toHistoryNoteVO(NoteDO noteDO);
    HistoryRemarkVO toHistoryRemarkVO(RemarkDO remarkDO);
}
